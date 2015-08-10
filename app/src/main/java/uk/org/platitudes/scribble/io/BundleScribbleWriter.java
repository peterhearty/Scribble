/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * Bundle writer.
 */
public class BundleScribbleWriter extends ScribbleWriter{

    private Bundle mBundle;

    /**
     * Used when reading or writing.
     */
    public BundleScribbleWriter(ScribbleMainActivity sma, Bundle b) {
        super(sma);
        mBundle = b;
    }

    public void write () {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
//        ScribbleOutputStream dos = new ScribbleOutputStream(baos, false);

        writeToOutputStream(baos, false);
//        writeMainView(dos);
        byte[] bytes = baos.toByteArray();
        mBundle.putByteArray(ScribbleReader.EVERYTHING_KEY, bytes);

        try {
//            dos.close();
            baos.close();
        } catch (IOException e) {
            ScribbleMainActivity.log("FileSaver", "writeToBundle", e);
        }
    }


}
