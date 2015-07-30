/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.ItemList;
import uk.org.platitudes.scribble.io.FileScribbleWriter;

/**
 * Holds the list of DrawItems for a drawing.
 */
public class Drawing  implements Runnable {

    /**
     * The draw list, the list of DrawItems on this page.
     */
    private ItemList mDrawItems;

    /**
     * Items get moved from the draw list to here when undo is pressed.
     * They move the oppoiste way on redo.
     */
    private ItemList mUndoList;

    //
    private boolean modifiedSinceLastWrite;

    private ScribbleView mScribbleView;

    // TODO - make this the drawing backinh file
    private File mCurrentlyOpenFile;

    private Thread backgroundThread;
    private volatile boolean stopBackgroundThread;



    public Drawing (ScribbleView scribbleView) {
        mScribbleView = scribbleView;
        mDrawItems = new ItemList();
        mUndoList = new ItemList();
        backgroundThread = new Thread(this);
        backgroundThread.start();
    }

    public void onDestroy () {
        stopBackgroundThread = true;
        backgroundThread.interrupt();
        if (modifiedSinceLastWrite) {
            write();
        }
    }

    public synchronized void save (DataOutputStream dos, int version) throws IOException {
        mDrawItems.write(dos, version);
        mUndoList.write(dos, version);
    }

    public synchronized void read (DataInputStream dis, int version) throws IOException {
        mDrawItems = new ItemList(dis, version, mScribbleView);
        mUndoList = new ItemList(dis, version, mScribbleView);
    }

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
        mDrawItems.clear();
        mUndoList.clear();
    }

    public ItemList getmDrawItems() {return mDrawItems;}

    private synchronized void write () {
        modifiedSinceLastWrite = false;
        ScribbleMainActivity activity = ScribbleMainActivity.mainActivity;
        File f = activity.getmCurrentlyOpenFile();
        if (f != null) {
            FileScribbleWriter fsw = new FileScribbleWriter(activity, f);
            fsw.write();
        }
    }

    @Override
    public void run() {
        while (!stopBackgroundThread) {
            try {
                Thread.sleep(5000);
                if (modifiedSinceLastWrite) {
                    write();
                }
            } catch (InterruptedException e) {
                stopBackgroundThread = true;
            }
        }
    }
}
