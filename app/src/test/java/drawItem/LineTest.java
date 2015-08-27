/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowMotionEvent;

import java.io.IOException;

import mockClasses.TestCanvas;
import uk.org.platitudes.scribble.BuildConfig;
import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.DrawItem;
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
//activity = new ScribbleMainActivity();
        scribbleView = activity.getmMainView();
        motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        canvas = new TestCanvas();
    }

    public static void resetEverything(TestCanvas canvas, MotionEvent motionEvent, ScribbleView scribbleView, ScribbleMainActivity activity) {
        canvas.testReset();
        motionEvent.setLocation(10f, 10f);
        scribbleView.setmScrollOffset(0, 0);
        activity.getmZoomButtonHandler().setsZoom(1);
    }

    @Test
    public void testBasicLine () {
        resetEverything(canvas, motionEvent, scribbleView, activity);
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

    public static void checkPositions (TestCanvas canvas, int expectedDrawCount, float startX, float startY, float endX, float endY) {
        assertTrue(canvas.testDrawCount(expectedDrawCount));
        assertTrue(canvas.testStartPosition(0, startX, startY));
        assertTrue(canvas.testEndPosition(0, endX, endY));
    }

    /**
     * This is a public static method so that it can be used by FreehandTest as well
     * (and possibly others).
     */
    public static void moveSizeTest (
            MotionEvent event,
            ScribbleView view,
            DrawItem drawItem,
            TestCanvas canvas,
            ScribbleMainActivity activity
    ) {
        // create an initial line from (10,10) to (20,20)
        event.setLocation(20f, 20f);
        drawItem.handleMoveEvent(event);
        drawItem.draw(canvas);
        checkPositions(canvas, 1, 10, 10, 20, 20);

        // now we move the view offset
        view.setmScrollOffset(5, 5);
        canvas.testReset();
        drawItem.draw(canvas);
        checkPositions(canvas, 1, 5, 5, 15, 15);

        // now change the zoom
        activity.getmZoomButtonHandler().setsZoom(2);
        canvas.testReset();
        drawItem.draw(canvas);
        checkPositions(canvas, 1, 10, 10, 30, 30);

        // move the line from [(10,10),(20,20)] to [(15,20),(25,30)]
        // and repeat tests
        resetEverything(canvas, event, view, activity);
        drawItem.move(5, 10);
        drawItem.draw(canvas);
        checkPositions(canvas, 1, 15, 20, 25, 30);

        // now we move the view offset
        view.setmScrollOffset(5, 5);
        canvas.testReset();
        drawItem.draw(canvas);
        checkPositions(canvas, 1, 10, 15, 20, 25);

        // now change the zoom
        activity.getmZoomButtonHandler().setsZoom(2);
        canvas.testReset();
        drawItem.draw(canvas);
        checkPositions(canvas, 1, 20, 30, 40, 50);

    }

    @Test
    public void moveSizeTest () {
        resetEverything(canvas, motionEvent, scribbleView, activity);
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        LineTest.moveSizeTest(motionEvent, scribbleView, line, canvas, activity);
    }

    public static void localZoom (
            MotionEvent event,
            ScribbleView view,
            DrawItem drawItem,
            DrawItem zoomItem,
            TestCanvas canvas
    ) {
        event.setLocation(20f, 20f);
        drawItem.handleMoveEvent(event);
        zoomItem.handleMoveEvent(event);

        MotionEvent twoPointerEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0);

// Some of the Roboelectric stuff may seem unwieldy, but without it, or without Mockito or PowerMock,
// you quickly find yourself emulating dozens of classes and hundreds of methods.
        ShadowMotionEvent shadowEvent = Shadows.shadowOf(twoPointerEvent);
        shadowEvent.setPointer2(15, 15);

        // Zoom the line
        ResizeItem resizer = new ResizeItem(twoPointerEvent, zoomItem, view);
        shadowEvent.setPointer2(20, 20);
        resizer.handleMoveEvent(twoPointerEvent);

        drawItem.draw(canvas);
        zoomItem.draw(canvas);
        assertTrue(canvas.testDrawCount(2));
        checkPositions(canvas, 2, 10, 10, 20, 20);
        assertTrue(canvas.testStartPosition(1, 10, 10));
        assertTrue(canvas.testEndPosition(1, 30, 30));

    }

    @Test
    public void localZoom () {
        // 2 lines one zoomed one not
        resetEverything(canvas, motionEvent, scribbleView, activity);
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        LineDrawItem zoomLine = new LineDrawItem(motionEvent, scribbleView, false);
        localZoom(motionEvent, scribbleView, line, zoomLine, canvas);
    }

    public static void saveRestore (MotionEvent motionEvent, DrawItem drawItem, ScribbleView scribbleView, TestCanvas canvas) throws IOException {
        motionEvent.setLocation(20f, 20f);
        drawItem.handleMoveEvent(motionEvent);

        // We have to do an UP event to add to the Drawing
        drawItem.handleUpEvent(motionEvent);

        // Create file and save it
        Drawing drawing = scribbleView.getDrawing();
        drawing.write();
        drawing.openCurrentFile();
        drawing.getmDrawItems().onDraw(canvas);

        // checking the line position implicitly checks that mDrawBox is false and zoom is 1
        checkPositions(canvas, 1, 10, 10, 20, 20);
//        assertTrue(newLine.deleted == false);
    }

    @Test
    public void saveRestore () throws IOException {
        // Set up a line
        resetEverything(canvas, motionEvent, scribbleView, activity);
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        saveRestore(motionEvent, line, scribbleView, canvas);
    }

}
