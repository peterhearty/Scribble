/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package drawItem;

import android.graphics.Canvas;
import android.os.Build;
import android.view.MotionEvent;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowCanvas;

import java.io.File;

import mockClasses.TestCanvas;
import uk.org.platitudes.scribble.BuildConfig;
import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

/**
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class)
public class LocalFileTest extends TestCase {

    private ScribbleView scribbleView;
    private ScribbleMainActivity activity;
    private MotionEvent motionEvent;
    private Canvas canvas;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = Robolectric.buildActivity(ScribbleMainActivity.class).create().get();
//activity = new ScribbleMainActivity();
        scribbleView = activity.getmMainView();
        motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        canvas = new Canvas();
    }

//    public static void resetEverything(TestCanvas canvas, MotionEvent motionEvent, ScribbleView scribbleView, ScribbleMainActivity activity) {
//        canvas.testReset();
//        motionEvent.setLocation(10f, 10f);
//        scribbleView.setmScrollOffset(0, 0);
//        activity.getmZoomButtonHandler().setsZoom(1);
//    }

    @Test
    public void loadComplexFile () {
        Drawing drawing = scribbleView.getDrawing();
        File f = new File("/home/pete/Dropbox/AndroidDev/testFiles/vectorcalculus");
        drawing.setmCurrentlyOpenFile(f);
        drawing.openCurrentFile();
        drawing.getmDrawItems().onDraw(canvas);
        ShadowCanvas shadowCanvas = Shadows.shadowOf(canvas);
        int lineCount = shadowCanvas.getLinePaintHistoryCount();
        assertTrue(lineCount>0);
    }
}
