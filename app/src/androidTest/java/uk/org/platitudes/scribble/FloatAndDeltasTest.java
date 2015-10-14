/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.test.ActivityInstrumentationTestCase2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.drawitem.freehand.floatAndDeltas;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;
import uk.org.platitudes.scribble.io.ScribbleReader;

/**
 */
public class FloatAndDeltasTest extends AbstractDrawItem {


    public FloatAndDeltasTest () {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    private floatAndDeltas addFloats (float[] values) {
        floatAndDeltas floats = new floatAndDeltas();
        for (int i=0; i< values.length; i++) {
            floats.addPoint(values[i]);
        }
        return floats;
    }

    private void verifyFloats(floatAndDeltas floats, float[] values, float[] tolerances) {
        float result = floats.firstFloat() ;
        for (int i=0; i< values.length; i++) {
            assertTrue(closeTo(result, values[i], tolerances[i]));
            result = floats.nextFloat(1);
        }
    }

    private float max (float[] values) {
        float result = values[0];
        for (int i=1; i<values.length; i++) {
            float value = values[i];
            if (value > result) {
                result = value;
            }
        }
        return result;
    }

    private float min (float[] values) {
        float result = values[0];
        for (int i=1; i<values.length; i++) {
            float value = values[i];
            if (value < result) {
                result = value;
            }
        }
        return result;
    }


    // test some typical values
    private static final float[] basicFloats            = {123.456f, 123.456f, 128, 160, 100, 60,   20,   0,    0.1f};
    private static final float[] basicFloatTolerances   = {  0.01f,    0.01f,    1,   1,   1, 0.1f, 0.1f, 0.1f, 0.01f};


    public void testBasicTest () {
        ScribbleMainActivity.log ("-- FloatAndDeltasTest", "basicTest --", null);
        // start with a single point
        floatAndDeltas floats = addFloats(basicFloats);
        verifyFloats(floats, basicFloats, basicFloatTolerances);
        float max = floats.max;
        float min = floats.min;
        assertTrue(closeTo(max, 160, 1));
        assertTrue(closeTo(min, 0, 0.1f));
    }

    // test some large values
    private static final float[] largeFloats            = {12000, 12000, 12200, 10000, 8000, 6000, 2000, 0,    0.1f};
    private static final float[] largeFloatTolerances   = {  100,   100,   100,   100,  100,  100,   50, 0.1f, 0.01f};

    /**
     * Tests larger numbers
     */
    public void testBasicLargeTest () {
        ScribbleMainActivity.log ("-- FloatAndDeltasTest", "basicLargeTest --", null);
        // start with a single point
        floatAndDeltas floats = addFloats(largeFloats);
        verifyFloats(floats, largeFloats, largeFloatTolerances);
        float max = floats.max;
        float min = floats.min;
        assertTrue(closeTo(max, 12200, 100));
        assertTrue(closeTo(min, 0, 0.1f));
    }

    public void testSaveRestore () throws IOException {
        ScribbleMainActivity.log ("-- FloatAndDeltasTest", "saveRestore --", null);
        // Use another test to setup some data
        floatAndDeltas floats = addFloats(basicFloats);

        // Now save to an output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
        ScribbleOutputStream sos = ScribbleOutputStream.newScribbleOutputStream(baos, false);
        floats.write(sos);
        byte[] bytes = baos.toByteArray();
        sos.close();

        // Now read it back in
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ScribbleInputStream sis = new ScribbleInputStream(bais);
        floatAndDeltas rereadFloats = new floatAndDeltas();
        rereadFloats.read(sis, ScribbleReader.FILE_FORMAT_VERSION);

        // test that values are correct
        verifyFloats(floats, basicFloats, basicFloatTolerances);
        float max = rereadFloats.max;
        float min = rereadFloats.min;
        assertTrue(closeTo(max, 160, 1));
        assertTrue(closeTo(min, 0, 0.1f));
    }

//    @Test
//    public void logTest () {
//        ScribbleMainActivity.log ("test tag", "test msg", new Exception("test exception"));
//    }
}
