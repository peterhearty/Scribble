/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;

import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.ItemList;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFile;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFolder;
import uk.org.platitudes.scribble.io.FileScribbleReader;
import uk.org.platitudes.scribble.io.FileScribbleWriter;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;
import uk.org.platitudes.scribble.io.ScribbleReader;

/**
 * Holds the list of DrawItems for a drawing. Also holds the storage file
 * that backs them up. There is always a storage file, even if it is the
 * default file. The file can be local or on the Google Drive. The latter
 * involves a lot of asynchronous activity. A separate background thread
 * handles most of this as well as file writes.
 */
public class Drawing implements Runnable {

    /**
     * The draw list, the list of DrawItems on this page.
     */
    private ItemList mDrawItems;

    /**
     * Items get moved from the draw list to here when undo is pressed.
     * They move the oppoiste way on redo.
     */
    private ItemList mUndoList;

    /**
      * Gets set true whenever anything is added, moved etc from the draw list.
      * Writes don't happen immediately but when the background thread wakes up.
      */
    private boolean modifiedSinceLastWrite;

    private ScribbleView mScribbleView;
    private ScribbleMainActivity mMainActivity;
    private File mCurrentlyOpenFile;

    private Thread backgroundThread;
    private volatile boolean stopBackgroundThread;
    boolean writeInProgress;
    long mThreadWait;




    public Drawing (ScribbleView scribbleView) {
        mScribbleView = scribbleView;
        mMainActivity = ScribbleMainActivity.mainActivity;
        mDrawItems = new ItemList();
        mUndoList = new ItemList();

        mThreadWait = 1000;
        backgroundThread = new Thread(this);
        backgroundThread.start();
    }

    public boolean inBackgroundThread () {
        Thread thisThread = Thread.currentThread();
        if (thisThread.equals(backgroundThread)) {
            return true;
        }
        return false;
    }

    public void openCurrentFile () {
        String currentFilePath = getCurrentFilenameFromPreferences();

        try {
            mCurrentlyOpenFile = new File (currentFilePath);
            if (GoogleDriveFolder.isGoogleDriveFile(currentFilePath)) {
                // google drive file - have to read later
                // GoogleDriveStuff will call setmCurrentlyOpenFile when the file is available
                // The backgroundThread will then load the contents shortly afterwards
                mMainActivity.getmGoogleStuff().setFileToReadWhenReady(currentFilePath);
            } else {
                // a local file, read it now
                FileScribbleReader fs = new FileScribbleReader(mMainActivity, mCurrentlyOpenFile);
                fs.read(this);
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("Error opening file ", currentFilePath, e);
            useDefaultFile();
        }

    }

    private String getCurrentFilenameFromPreferences () {
        Context context = mScribbleView.getContext();
        String defaultFile = context.getFilesDir()+File.separator+ ScribbleReader.DEFAULT_FILE;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String currentFilePath = sharedPref.getString(ScribbleReader.CURRENT_FILE_PREFERENCE_KEY, defaultFile);
        return currentFilePath;
    }

    public void useDefaultFile () {
        Context context = mScribbleView.getContext();
        String defaultFile = context.getFilesDir()+File.separator+ ScribbleReader.DEFAULT_FILE;
        File f = new File (defaultFile);
        setmCurrentlyOpenFile(f);
    }

    /**
     * Sets the currently stored PREFERENCE.
     */
    public void setmCurrentlyOpenFile(File f) {
        if (f == null) return;

        mCurrentlyOpenFile = f;
        mThreadWait = 1000;

        Context context = mScribbleView.getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor e = sharedPref.edit();
        try {
            e.putString(ScribbleReader.CURRENT_FILE_PREFERENCE_KEY, mCurrentlyOpenFile.getCanonicalPath());
        } catch (Exception e1) {
            ScribbleMainActivity.log("ScribbleMainActivity", "setmCurrentlyOpenFile", e1);
        }
        e.commit();
    }

    public File getmCurrentlyOpenFile() {return mCurrentlyOpenFile;}

    public void about () {
        String path = "";
        try {
            path = mCurrentlyOpenFile.getCanonicalPath();
            path += " size="+mCurrentlyOpenFile.length();
        } catch (IOException e) {
            ScribbleMainActivity.log("ScribbleMainActivity", "about", e);
        }
        ScribbleMainActivity.log(path, "", null);
    }

    public void onDestroy () {
        stopBackgroundThread = true;

        // Note that we always ensure the modifiedSinceLastWrite
        // flag is clear before stopping the IO thread.
        if (modifiedSinceLastWrite) {
            write();
            backgroundThread.interrupt();
        } else {
            backgroundThread.interrupt();
        }
    }

    public synchronized void save (ScribbleOutputStream dos, int version) throws IOException {
        mDrawItems.write(dos, version);
        mUndoList.write(dos, version);
    }

    public synchronized void read (ScribbleInputStream dis, int version) throws IOException {
        if (writeInProgress) {
            ScribbleMainActivity.log ("Read during write", "", null);
        } else {
            mDrawItems = new ItemList(dis, version, mScribbleView);
            mUndoList = new ItemList(dis, version, mScribbleView);

            // Each item list has to scan both item lists for MoveItems and matching targets.
            mDrawItems.tieMoveItemsToTargets(mDrawItems);
            mDrawItems.tieMoveItemsToTargets(mUndoList);
            mUndoList.tieMoveItemsToTargets(mDrawItems);
            mUndoList.tieMoveItemsToTargets(mUndoList);

            // The background thread reads Google Drive files, so invalidate cannot
            // be called directly. The " post" method is similar to Handler.post for 
            // to a thread's message queue.
            mScribbleView.post(new Runnable() {
                @Override
                public void run() {
                    mScribbleView.invalidate();
                }
            });
        }
    }

    /*
     * Undo always involves a move from the draw list to the undo list.
     * For real drawing items this is all that is needed. Virtual items, like
     * MoveItem, need to perform additional actions via their undo method.
     */
    public synchronized void undo () {
        DrawItem movedItem = mDrawItems.moveLastTo(mUndoList);
        if (movedItem != null) {
            movedItem.undo();
        }
        modifiedSinceLastWrite = true;
    }

    public synchronized void redo () {
        DrawItem movedItem = mUndoList.moveLastTo(mDrawItems);
        if (movedItem != null) {
            movedItem.redo();
        }
        modifiedSinceLastWrite = true;
    }

    public synchronized void addItem (DrawItem item) {
        mDrawItems.add(item);
        modifiedSinceLastWrite = true;
    }

    public synchronized void clear () {
        if (modifiedSinceLastWrite) {
            // Save any changes before clearing
            // up to caller to decide if backing file should change
            write();
        }
        mDrawItems.clear();
        mUndoList.clear();
    }

    public synchronized void clearUndos () {
        int numCleared = mUndoList.clear();
        numCleared += mDrawItems.clean();
        modifiedSinceLastWrite = true;
        ScribbleMainActivity.log ("Cleared "+numCleared, "", null);
    }

    public ItemList getmDrawItems() {return mDrawItems;}
    public ItemList getUndoItems() {return mUndoList;}

    public synchronized void write () {
        modifiedSinceLastWrite = false;
// What's wrong with mMainActivity below???
        ScribbleMainActivity activity = ScribbleMainActivity.mainActivity;
        if (mCurrentlyOpenFile != null) {
            if (writeInProgress) {
                ScribbleMainActivity.log("Nested write", "", null);
                return;
            }
            writeInProgress = true;
            FileScribbleWriter fsw = new FileScribbleWriter(activity, mCurrentlyOpenFile);
            fsw.write();
            writeInProgress = false;
        }
    }

    @Override
    public void run() {
        while (!stopBackgroundThread) {
            try {
                Thread.sleep(mThreadWait);
                if (modifiedSinceLastWrite) {
                    write();
                } else if (mCurrentlyOpenFile != null && mCurrentlyOpenFile instanceof GoogleDriveFile) {
                    // Read the current file contents
                    GoogleDriveFile gdf = (GoogleDriveFile) mCurrentlyOpenFile;
                    if (gdf.fileHasChanged) {
                        gdf.fileHasChanged = false;
                        FileScribbleReader fsr = new FileScribbleReader(mMainActivity, mCurrentlyOpenFile);
                        fsr.read(this);
                    }
                    // Force another read of file contents.
                    // NOTE - tried adding an update listener, a subscription, or checking for
                    // metadata modifiedDate change. None of them worked properly. So while the file
                    // is open, we keep rereading the contents, checking each time for a change.
                    gdf.forceReRead();
                }

                if (mThreadWait < 4000) {
                    mThreadWait *= 2;
                }
            } catch (InterruptedException e) {
                stopBackgroundThread = true;
            }
        }
    }
}
