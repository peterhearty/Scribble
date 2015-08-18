package drawItem;

import android.graphics.PointF;
import android.os.Build;
import android.view.MotionEvent;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import mockClasses.TestCanvas;
import uk.org.platitudes.scribble.BuildConfig;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.drawitem.LineDrawItem;
import uk.org.platitudes.scribble.drawitem.ResizeItem;

/**
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class)
public class LineTest extends TestCase {

    private ScribbleView scribbleView;
    private ScribbleMainActivity activity;
    private MotionEvent motionEvent;
    private TestCanvas canvas;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = Robolectric.buildActivity(ScribbleMainActivity.class).create().get();
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

    @Test
    public void testBasicLine () {
        resetEverything();
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);

        // No end point means the line should not draw anything
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(0));

        // complete the line
        motionEvent.setLocation(20f, 20f);
        line.handleMoveEvent(motionEvent);

        // check that it draws
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(1));

        // select the line - should draw line (1 line), border, (4 lines), 2 handles (2x4 = 8 lines)
        PointF selectionPoint = new PointF(15f, 15f);
        line.selectItem(selectionPoint);
        canvas.testReset();
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(13));

        // deslect should return to just drawing the line
        line.deselectItem();
        canvas.testReset();
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(1));
    }

    @Test
    public void moveSizeTest () {
        // create an initial line from (10,10) to (20,20)
        resetEverything();
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        motionEvent.setLocation(20f, 20f);
        line.handleMoveEvent(motionEvent);
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(1));
        assertTrue(canvas.testStartPosition(0, 10f, 10f));
        assertTrue(canvas.testEndPosition(0, 20f, 20f));

        // now we move the view offset
        scribbleView.setmScrollOffset(5, 5);
        canvas.testReset();
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(1));
        assertTrue(canvas.testStartPosition(0, 5, 5));
        assertTrue(canvas.testEndPosition(0, 15, 15));

        // now change the zoom
        activity.getmZoomButtonHandler().setsZoom(2);
        canvas.testReset();
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(1));
        assertTrue(canvas.testStartPosition(0, 10, 10));
        assertTrue(canvas.testEndPosition(0, 30, 30));

        // move the line from [(10,10),(20,20)] to [(15,20),(25,30)]
        // and repeat tests
        resetEverything();
        line.move(5, 10);
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(1));
        assertTrue(canvas.testStartPosition(0, 15, 20));
        assertTrue(canvas.testEndPosition(0, 25, 30));

        // now we move the view offset
        scribbleView.setmScrollOffset(5, 5);
        canvas.testReset();
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(1));
        assertTrue(canvas.testStartPosition(0, 10, 15));
        assertTrue(canvas.testEndPosition(0, 20, 25));

        // now change the zoom
        activity.getmZoomButtonHandler().setsZoom(2);
        canvas.testReset();
        line.draw(canvas);
        assertTrue(canvas.testDrawCount(1));
        assertTrue(canvas.testStartPosition(0, 20, 30));
        assertTrue(canvas.testEndPosition(0, 40, 50));
    }

    @Test
    public void localZoom () {
        // 2 lines one zoomed one not
        resetEverything();
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        LineDrawItem zoomLine = new LineDrawItem(motionEvent, scribbleView, false);
        motionEvent.setLocation(20f, 20f);
        line.handleMoveEvent(motionEvent);
        zoomLine.handleMoveEvent(motionEvent);

        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[2];
        pointerCoords[0] = new MotionEvent.PointerCoords();
        pointerCoords[0].x = 10;
        pointerCoords[0].y = 10;
        pointerCoords[1] = new MotionEvent.PointerCoords();
        pointerCoords[1].x = 15;
        pointerCoords[1].y = 15;
        MotionEvent twoPointerEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 2,null, pointerCoords, 0, 0, 0, 0, 0, 0, 0, 0);
        ResizeItem resizer = new ResizeItem(twoPointerEvent, zoomLine, scribbleView);

    }

}
