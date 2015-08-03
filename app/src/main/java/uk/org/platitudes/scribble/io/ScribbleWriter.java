/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import java.io.DataOutputStream;
import java.io.OutputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

/**
 * Base class for writers.
 */
abstract public class ScribbleWriter {

    protected ScribbleView mMainView;
    protected ScribbleMainActivity mScribbleMainActivity;
    private static byte changeByte;

    /**
     * Used when reading or writing.
     *
     */
    public ScribbleWriter (ScribbleMainActivity sma) {
        mScribbleMainActivity = sma;
        mMainView = mScribbleMainActivity.getmMainView();
    }

    public abstract void write ();

    public void writeToOutputStream(OutputStream fos) {
        try {
            ScribbleOutputStream dos = new ScribbleOutputStream(fos, false);
            dos.writeLong(ScribbleReader.MAGIC_NUMBER);
            dos.writeInt(ScribbleReader.FILE_FORMAT_VERSION);
            dos.writeByte(++changeByte);
            writeMainView(dos);
            dos.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeToOutputStream", e);
        }

    }

    protected void writeMainView(ScribbleOutputStream dos) {
        try {
            mMainView.getDrawing().save(dos, ScribbleReader.FILE_FORMAT_VERSION);
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeMainView", e);
        }
    }



}
