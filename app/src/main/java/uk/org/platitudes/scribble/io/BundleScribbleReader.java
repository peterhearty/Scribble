/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * Bundle reader.
 */
public class BundleScribbleReader extends ScribbleReader {

    private Bundle mBundle;

    /**
     */
    public BundleScribbleReader(ScribbleMainActivity sma, Bundle b) {
        super(sma);
        mBundle = b;
    }

    @Override
    public void read (Drawing drawing) {
        byte[] bytes = mBundle.getByteArray(EVERYTHING_KEY);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        readFromInputStream(bais, drawing);
        try {
            bais.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("BundleScribbleReader", "read from Bundle", e);
        }
    }


}
