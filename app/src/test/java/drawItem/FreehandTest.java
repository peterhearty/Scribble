/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package drawItem;

import android.os.Build;
import android.view.MotionEvent;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import mockClasses.TestCanvas;
import uk.org.platitudes.scribble.BuildConfig;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.LineDrawItem;
import uk.org.platitudes.scribble.drawitem.freehand.FreehandCompressedDrawItem;

/**
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class)
public class FreehandTest extends TestCase{

    private ScribbleView scribbleView;
    private ScribbleMainActivity activity;
    private MotionEvent motionEvent;
    private TestCanvas canvas;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = Robolectric.buildActivity(ScribbleMainActivity.class).create().get();
//activity = new ScribbleMainActivity();
        scribbleView = activity.getmMainView();
        motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        canvas = new TestCanvas();
    }

    private void resetEverything() {
        canvas.testReset();
        motionEvent.setLocation(10f, 10f);
        scribbleView.setmScrollOffset(0, 0);
        activity.getmZoomButtonHandler().setsZoom(1);
    }

    private FreehandCompressedDrawItem createFreehand (float[] xs, float[] ys) {
        motionEvent.setLocation(xs[0], ys[0]);
        FreehandCompressedDrawItem free = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        for (int i=1; i<xs.length; i++) {
            motionEvent.setLocation(xs[i], ys[i]);
            free.handleMoveEvent(motionEvent);
        }
        return free;
    }

    private boolean testCanvasPositions (float[] xs, float[] ys) {
        boolean result = true;
        for (int i=0; i<xs.length-1; i++) {
            if (!canvas.testStartPosition(i, xs[i], ys[i])) {
                result = false;
                break;
            }
            if (!canvas.testEndPosition(i, xs[i+1], ys[i+1])) {
                result = false;
                break;
            }
        }
        return result;
    }

    private float[] x1s = {0,10,20,30,20,10,0};
    private float[] y1s = {0,10,20,30,40,50,60};

    @Test
    public void simpleFreehandTest () {
        FreehandCompressedDrawItem free = createFreehand(x1s, y1s);
        free.draw(canvas);
        assertTrue(canvas.testDrawCount(x1s.length-1));
        assertTrue(testCanvasPositions(x1s, y1s));
    }

    @Test
    public void moveSizeTest () {
        LineTest.resetEverything(canvas, motionEvent, scribbleView, activity);
        FreehandCompressedDrawItem line = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        LineTest.moveSizeTest(motionEvent, scribbleView, line, canvas, activity);
    }

    @Test
    public void localZoom () {
        // 2 lines one zoomed one not
        LineTest.resetEverything(canvas, motionEvent, scribbleView, activity);
        FreehandCompressedDrawItem line = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        FreehandCompressedDrawItem zoomLine = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        LineTest.localZoom(motionEvent, scribbleView, line, zoomLine, canvas);
    }


    @Test
    public void saveRestore () throws IOException {
        // Set up a line
        LineTest.resetEverything(canvas, motionEvent, scribbleView, activity);
        FreehandCompressedDrawItem line = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        LineTest.saveRestore(motionEvent, line, scribbleView, canvas);
    }


}
