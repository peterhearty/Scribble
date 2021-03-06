/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.AlertDialog;
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
public class Drawing {

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
    boolean writeInProgress;
    private BackgroundThread mBackgroundThread;


    public Drawing (ScribbleView scribbleView) {
        mScribbleView = scribbleView;
        mMainActivity = ScribbleMainActivity.mainActivity;
        mDrawItems = new ItemList();
        mUndoList = new ItemList();
        mBackgroundThread= new BackgroundThread(this);
    }

    public void openCurrentFile () {
        String currentFilePath = getCurrentFilenameFromPreferences();

        try {
            mCurrentlyOpenFile = new File (currentFilePath);
            if (GoogleDriveFolder.isGoogleDriveFile(currentFilePath)) {
                // google drive file - have to read later
                // GoogleDriveStuff will call setmCurrentlyOpenFile when the file is available
                // The backgroundThread will then load the contents shortly afterwards
                ScribbleMainActivity.log("Drawing", "Will read file later "+currentFilePath, null);
                mMainActivity.getmGoogleStuff().setFileToReadWhenReady(currentFilePath);
            } else {
                if (mCurrentlyOpenFile.canRead()) {
                    // a local file, read it now
                    ScribbleMainActivity.log("Drawing", "Trying to read local file "+currentFilePath, null);
                    FileScribbleReader fs = new FileScribbleReader(mMainActivity, mCurrentlyOpenFile);
                    fs.read(this);
                } else {
                    ScribbleMainActivity.log("File not readable or does not exist: ", currentFilePath, null);
                }
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
        mBackgroundThread.interrupt(BackgroundThread.STATE_NEW_ACITIVY);

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
        AlertDialog.Builder alert = new AlertDialog.Builder(ScribbleMainActivity.mainActivity);
        AlertDialog dialog = alert.setMessage(path).setCancelable(true).create();
        dialog.show();
    }

    public void onDestroy () {
        // Note that we always ensure the modifiedSinceLastWrite
        // flag is clear before stopping the IO thread.
        if (modifiedSinceLastWrite) {
            write();
            mBackgroundThread.interrupt(BackgroundThread.STATE_PROGRAM_EXIT);
        } else {
            mBackgroundThread.interrupt(BackgroundThread.STATE_PROGRAM_EXIT);
        }
    }

    public synchronized void save (ScribbleOutputStream dos, int version) throws IOException {
        mDrawItems.write(dos, version);
        mUndoList.write(dos, version);
    }

    public synchronized void read (ScribbleInputStream dis, int version) throws IOException {
        if (writeInProgress) {
            ScribbleMainActivity.log ("Drawing ", "Read during write", null);
        } else {
            ScribbleMainActivity.log("Drawing", "Reading file ", null);
            mDrawItems = new ItemList(dis, version, mScribbleView);
            mUndoList = new ItemList(dis, version, mScribbleView);
            ScribbleMainActivity.log("Drawing", "Item count "+mDrawItems.toString(), null);
            ScribbleMainActivity.log("Drawing", "Undo count "+mUndoList.toString(), null);

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
        ScribbleMainActivity.log("Drawing", "undo", null);
        DrawItem movedItem = mDrawItems.moveLastTo(mUndoList);
        if (movedItem != null) {
            movedItem.undo();
        }
        modifiedSinceLastWrite = true;
        mBackgroundThread.interrupt(BackgroundThread.STATE_NEW_ACITIVY);
    }

    public synchronized void redo () {
        ScribbleMainActivity.log("Drawing", "redo", null);
        DrawItem movedItem = mUndoList.moveLastTo(mDrawItems);
        if (movedItem != null) {
            movedItem.redo();
        }
        modifiedSinceLastWrite = true;
        mBackgroundThread.interrupt(BackgroundThread.STATE_NEW_ACITIVY);
    }

    public synchronized void addItem (DrawItem item) {
        ScribbleMainActivity.log("Drawing", "addItem "+item.getClass(), null);
        mDrawItems.add(item);
        modifiedSinceLastWrite = true;
        mBackgroundThread.interrupt(BackgroundThread.STATE_NEW_ACITIVY);
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
        ScribbleMainActivity.log("Drawing", "clearUndos", null);
        int numCleared = mUndoList.clear();
        numCleared += mDrawItems.clean();
        modifiedSinceLastWrite = true;
        mBackgroundThread.interrupt(BackgroundThread.STATE_NEW_ACITIVY);
        ScribbleMainActivity.makeToast("Cleared " + numCleared);
    }

    public ItemList getmDrawItems() {return mDrawItems;}
    public ItemList getUndoItems() {return mUndoList;}
    public boolean modified () {return modifiedSinceLastWrite;}
    public File getCurrentlyOpenFile () {return mCurrentlyOpenFile;}
    public BackgroundThread getBackgroundThread () {return mBackgroundThread;}

    public synchronized void write () {
        modifiedSinceLastWrite = false;
// What's wrong with mMainActivity below???
//        ScribbleMainActivity activity = ScribbleMainActivity.mainActivity;
        if (mCurrentlyOpenFile != null) {
            ScribbleMainActivity.log("Drawing", "Writing file "+mCurrentlyOpenFile, null);
            if (writeInProgress) {
                ScribbleMainActivity.log("Nested write", "", null);
                return;
            }
            writeInProgress = true;
            FileScribbleWriter fsw = new FileScribbleWriter(mMainActivity, mCurrentlyOpenFile);
            fsw.write();
            writeInProgress = false;
        }
    }

    public void requestWrite () {
        ScribbleMainActivity.log("Drawing", "requestWrite", null);
        modifiedSinceLastWrite = true;
        mBackgroundThread.interrupt(BackgroundThread.STATE_NEW_ACITIVY);
    }

}
