/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.Instrumentation;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;

import com.google.android.gms.common.ConnectionResult;

import junit.framework.TestCase;

import java.io.File;

import uk.org.platitudes.scribble.mock.TestCanvas;

/**
 */
public class LocalFileTest extends AbstractDrawItem {

    public LocalFileTest () {
        super();
    }

    @Override
    public void setUp() throws Exception {
        ScribbleMainActivity.log("<<<<<<<<<< LocalFileTest", "setUp >>>>>>>>>>>>>", null);
        super.setUp();
    }

    public void testLoadComplexFile () {
        ScribbleMainActivity.log("-- LocalFileTest", "loadComplexFile --", null);
        Drawing drawing = scribbleView.getDrawing();

        File f = getTestFile("vectorcalculus");
        drawing.setmCurrentlyOpenFile(f);

        drawing.openCurrentFile();
        drawing.getmDrawItems().onDraw(canvas);
        assertTrue(canvas.lineCount==13231);
        assertTrue(canvas.circleCount==12);
        assertTrue(canvas.textCount==15);

        // save as a text file
        // Note - FileChooser does this in a diff order: write followed by setmCurrentlyOpenFile
        File g = constructTestFile("test_vectorcalculus.txt");
        drawing.setmCurrentlyOpenFile(g);
        drawing.write();

        // read it back in again
        canvas.testReset();
        drawing.openCurrentFile();
        drawing.getmDrawItems().onDraw(canvas);
        assertTrue(canvas.lineCount==13231);
        assertTrue(canvas.circleCount==12);
        assertTrue(canvas.textCount==15);
    }

    public void testLoadBrokenFile () {
        ScribbleMainActivity.log("-- LocalFileTest", "loadBrokenFile --", null);
        Drawing drawing = scribbleView.getDrawing();
        File f = getTestFile("broken_text.txt");
        drawing.setmCurrentlyOpenFile(f);

        drawing.openCurrentFile();
        drawing.getmDrawItems().onDraw(canvas);
        assertTrue(canvas.lineCount==321);
        assertTrue(canvas.circleCount==0);
        assertTrue(canvas.textCount == 0);

    }
}
