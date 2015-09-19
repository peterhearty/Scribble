/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import java.io.File;

import uk.org.platitudes.scribble.googledrive.GoogleDriveFile;
import uk.org.platitudes.scribble.io.FileScribbleReader;

/**
 * Loads and saves changes to the current file.
 */
public class BackgroundThread implements Runnable {

    /**
     * Set to true when the background thread should exit.
     */
    private volatile boolean stopBackgroundThread;

    /**
     * How londs (in ms) that the thread should pause between checks.
     */
    long mThreadWait;

    private Thread backgroundThread;
    private int reasonForInterrupt;
    private Drawing mDrawing;

    private static final String[] interruptReason = {
            "exit",
            "new activity",
            "pause"
    };

    public static final int STATE_PROGRAM_EXIT = 0;
    public static final int STATE_NEW_ACITIVY = 1;
    public static final int STATE_PRORAM_PAUSE = 2;
    private static final long SHORTEST_THREAD_WAIT = 500;
    private static final long MAX_NORMAL_THREAD_WAIT = 4000;
    private static final long PAUSED_WAIT = 600000; // 10 MINS

    public BackgroundThread (Drawing d) {
        mDrawing = d;
        mThreadWait = SHORTEST_THREAD_WAIT;
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

    public void interrupt (int reason) {
        ScribbleMainActivity.log("Background thread", "interrupt requested, reason: "+reason, null);
        reasonForInterrupt = reason;

        if (reason > 2 || reason < 0) {
            // Make sure reason in range
            reason = STATE_NEW_ACITIVY;
        }

        if (reason == STATE_PRORAM_PAUSE) {
            // very long sleeps from now until some new activity happens
            mThreadWait = PAUSED_WAIT;
            // NO INTERRUPT DONE - JUST WAIT FOR IT TO WAKE AND THEN GO TO SLEEP FOR A LONG TIME
        }

        if (reason == STATE_NEW_ACITIVY) {
            if (mThreadWait >= MAX_NORMAL_THREAD_WAIT) {
                // new activity only causes an immediate interrupt when the background thread
                // is on a long wait or idling
                mThreadWait = SHORTEST_THREAD_WAIT;
//                ScribbleMainActivity.log("Background thread", "doing interrupt", null);
                backgroundThread.interrupt();
            }
        }
        if (reason == STATE_PROGRAM_EXIT) {
            stopBackgroundThread = true;
//            ScribbleMainActivity.log("Background thread", "doing interrupt", null);
            backgroundThread.interrupt();
        }

//        ScribbleMainActivity.log("Background thread", "thread wait = "+mThreadWait, null);

    }

    @Override
    public void run() {
        while (!stopBackgroundThread) {
            try {
//                ScribbleMainActivity.log("Background thread", "Sleeping for "+mThreadWait, null);
                Thread.sleep(mThreadWait);
                File file = mDrawing.getCurrentlyOpenFile();
                if (mDrawing.modified()) {
                    ScribbleMainActivity.log("Background thread", "Writing file", null);
                    mDrawing.write();
                } else if (file != null && file instanceof GoogleDriveFile) {
                    // Read the current file contents
                    GoogleDriveFile gdf = (GoogleDriveFile) file;
                    if (gdf.fileHasChanged) {
                        ScribbleMainActivity.log("Background thread", "File has changed, reading file "+gdf, null);
                        FileScribbleReader fsr = new FileScribbleReader(ScribbleMainActivity.mainActivity, file);
                        fsr.read(mDrawing);
                    }
                    // Force another read of file contents.
                    // NOTE - tried adding an update listener, a subscription, or checking for
                    // metadata modifiedDate change. None of them worked properly. So while the file
                    // is open, we keep rereading the contents. If an AsyncRead detects a change in the file
                    // then it will read it into mCurrentlyOpenFile.mFileContents and set the
                    // mCurrentlyOpenFile.fileHasChanged flag
                    gdf.forceReRead();
                }

                if (mThreadWait < 4000) {
                    mThreadWait *= 2;
                    ScribbleMainActivity.log("Background thread", "Increased poll interval to "+mThreadWait, null);
                }
            } catch (InterruptedException e) {
                ScribbleMainActivity.log("Background thread", "Interrupted: "+interruptReason[reasonForInterrupt], null);
            }
        }
        ScribbleMainActivity.log("Background thread", "exiting ", null);
    }
}
