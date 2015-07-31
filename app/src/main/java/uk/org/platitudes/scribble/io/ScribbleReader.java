/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import java.io.DataInputStream;
import java.io.InputStream;

import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

/**
 * The base class for all scribble readers.
 */
abstract public class ScribbleReader {

//    protected ScribbleView mMainView;
    protected ScribbleMainActivity mScribbleMainActivity;

    protected static final String EVERYTHING_KEY = "everything";
    protected static final int FILE_FORMAT_VERSION = 1000;
    protected static final long MAGIC_NUMBER = 0x5C81881EF11EL; // sort of says SCRIBBLEFILE
    public static final String DEFAULT_FILE = "defaultDataFile";
    public static final String CURRENT_FILE_PREFERENCE_KEY = "current_file_key";

    // See http://developer.android.com/guide/topics/data/backup.html
    public static final Object sDataLock = new Object();

    /**
     * Used when reading or writing.
     *
     */
    public ScribbleReader (ScribbleMainActivity sma) {
        mScribbleMainActivity = sma;
//        mMainView = mScribbleMainActivity.getmMainView();
    }

    public abstract void read (Drawing drawing);

    protected void readMainView(DataInputStream dis, int fileFormatVersion, Drawing drawing) {
        try {
            drawing.read(dis, fileFormatVersion);
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "readMainView", e);
        }
    }

    public void readFromInputStream(InputStream fis, Drawing drawing) {
        try {
            DataInputStream dis = new DataInputStream(fis);
            long magNumber = dis.readLong();
            if (magNumber != MAGIC_NUMBER) {
                ScribbleMainActivity.log("Not a scribble file", "", null);
            } else {
                int fileFormatVersion = dis.readInt();
                readMainView(dis, fileFormatVersion, drawing);
                // following set offset to zero and zoom to 1
//                mScribbleMainActivity.getmZoomButtonHandler().onLongClick(null);
            }
            dis.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "readFromInputStream", e);
        }

    }


}
