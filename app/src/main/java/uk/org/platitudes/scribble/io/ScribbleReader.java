/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import java.io.InputStream;

import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * The base class for all scribble readers. This class knows about Scribble storage layout. The
 * layout format is as follows.
 *
 * long MAGIC_NUMBER
 * int  format_version
 * byte changeByte          number that increments on each write
 * drawing                  the format of this is known only to the Drawing class
 *
 * Subclasses override the read(Drawing) method. Subclasses use the read(Drawing) method to
 * create an input stream for their storage and then call
 * readFromInputStream(InputStream fis, Drawing drawing) to read from this into the drawing.
 */
abstract public class ScribbleReader {

    protected ScribbleMainActivity mScribbleMainActivity;

    protected static final String EVERYTHING_KEY = "everything";
    protected static final int FILE_FORMAT_VERSION = 1001;
    protected static final long MAGIC_NUMBER = 0x5C81881EF11EL; // sort of says SCRIBBLEFILE
    public static final String DEFAULT_FILE = "defaultDataFile";
    public static final String CURRENT_FILE_PREFERENCE_KEY = "current_file_key";

    /**
     * Used when reading or writing.
     *
     */
    public ScribbleReader (ScribbleMainActivity sma) {
        mScribbleMainActivity = sma;
    }

    public abstract void read (Drawing drawing);

    protected void readFromInputStream(InputStream fis, Drawing drawing) {
        try {
            ScribbleInputStream dis = new ScribbleInputStream(fis);
            long magNumber = dis.readLong();
            if (magNumber != MAGIC_NUMBER) {
                ScribbleMainActivity.log("Not a scribble file", "", null);
            } else {
                int fileFormatVersion = dis.readInt();
                if (fileFormatVersion > FILE_FORMAT_VERSION) {
                    ScribbleMainActivity.log("Created with a newer version of Sscribble", "", null);
                    return;
                }
                if (fileFormatVersion >= 1001) {
                    // This is just a byte near the start that changes every time the file gets
                    // updated. It lets file changes be detected quickly without necessarily
                    // scanning the whole file.
                    byte changeByte = dis.readByte();
                }
                drawing.read(dis, fileFormatVersion);
            }
            dis.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("ScribbleReader", "readFromInputStream", e);
        }
    }

}
