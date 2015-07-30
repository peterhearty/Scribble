/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * Bundle reader.
 */
public class BundleScribbleReader extends ScribbleReader {

    private Bundle mBundle;

    /**
     * Used when reading or writing.
     */
    public BundleScribbleReader(ScribbleMainActivity sma, Bundle b) {
        super(sma);
        mBundle = b;
    }

    @Override
    public void read () {
        byte[] bytes = mBundle.getByteArray(EVERYTHING_KEY);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        readMainView(dis, FILE_FORMAT_VERSION);
        try {
            dis.close();
            bais.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "read from Bundle", e);
        }
    }


}