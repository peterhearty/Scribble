/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package drawItem;

import android.graphics.Canvas;
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
import org.robolectric.shadows.ShadowCanvas;
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
    private Canvas canvas;
    private ShadowCanvas shadowCanvas;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = Robolectric.buildActivity(ScribbleMainActivity.class).create().get();
//activity = new ScribbleMainActivity();
        scribbleView = activity.getmMainView();
        motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        canvas = new Canvas();
        shadowCanvas = Shadows.shadowOf(canvas);
    }

    public static void resetEverything(ShadowCanvas sc, MotionEvent motionEvent, ScribbleView scribbleView, ScribbleMainActivity activity) {
        sc.resetCanvasHistory();
        motionEvent.setLocation(10f, 10f);
        scribbleView.setmScrollOffset(0, 0);
        activity.getmZoomButtonHandler().setsZoom(1);
    }

    @Test
    public void testBasicLine () {
        resetEverything(shadowCanvas, motionEvent, scribbleView, activity);
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);

        // No end point means the line should not draw anything
        line.draw(canvas);
        assertTrue(shadowCanvas.getLinePaintHistoryCount() == 0);

        // complete the line
        motionEvent.setLocation(20f, 20f);
        line.handleMoveEvent(motionEvent);

        // check that it draws
        line.draw(canvas);
        assertTrue(shadowCanvas.getLinePaintHistoryCount()==1);

        // select the line - should draw line (1 line), border, (4 lines), 2 handles (2x4 = 8 lines)
        PointF selectionPoint = new PointF(15f, 15f);
        line.selectItem(selectionPoint);
        shadowCanvas.resetCanvasHistory();
        line.draw(canvas);
        assertTrue(shadowCanvas.getLinePaintHistoryCount() == 13);

        // deslect should return to just drawing the line
        line.deselectItem();
        shadowCanvas.resetCanvasHistory();
        line.draw(canvas);
        assertTrue(shadowCanvas.getLinePaintHistoryCount()==1);
    }

    public static void checkPositions (ShadowCanvas sc, int expectedDrawCount, float startX, float startY, float endX, float endY) {
        assertTrue(sc.getLinePaintHistoryCount()==expectedDrawCount);

        ShadowCanvas.LinePaintHistoryEvent line = sc.getDrawnLine(0);
        assertTrue(line.startX==startX);
        assertTrue(line.startY==startY);
        assertTrue(line.stopX==endX);
        assertTrue(line.stopY==endY);
    }

    /**
     * This is a public static method so that it can be used by FreehandTest as well
     * (and possibly others).
     */
    public static void moveSizeTest (
            MotionEvent event,
            ScribbleView view,
            DrawItem drawItem,
            Canvas canvas,
            ScribbleMainActivity activity
    ) {
        // create an initial line from (10,10) to (20,20)
        event.setLocation(20f, 20f);
        drawItem.handleMoveEvent(event);
        drawItem.draw(canvas);
        ShadowCanvas sc = Shadows.shadowOf(canvas);
        checkPositions(sc, 1, 10, 10, 20, 20);

        // now we move the view offset
        view.setmScrollOffset(5, 5);
        sc.resetCanvasHistory();
        drawItem.draw(canvas);
        checkPositions(sc, 1, 5, 5, 15, 15);

        // now change the zoom
        activity.getmZoomButtonHandler().setsZoom(2);
        sc.resetCanvasHistory();
        drawItem.draw(canvas);
        checkPositions(sc, 1, 10, 10, 30, 30);

        // move the line from [(10,10),(20,20)] to [(15,20),(25,30)]
        // and repeat tests
        resetEverything(sc, event, view, activity);
        drawItem.move(5, 10);
        drawItem.draw(canvas);
        checkPositions(sc, 1, 15, 20, 25, 30);

        // now we move the view offset
        view.setmScrollOffset(5, 5);
        sc.resetCanvasHistory();
        drawItem.draw(canvas);
        checkPositions(sc, 1, 10, 15, 20, 25);

        // now change the zoom
        activity.getmZoomButtonHandler().setsZoom(2);
        sc.resetCanvasHistory();
        drawItem.draw(canvas);
        checkPositions(sc, 1, 20, 30, 40, 50);

    }

    @Test
    public void moveSizeTest () {
        resetEverything(shadowCanvas, motionEvent, scribbleView, activity);
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        LineTest.moveSizeTest(motionEvent, scribbleView, line, canvas, activity);
    }

    public static void localZoom (
            MotionEvent event,
            ScribbleView view,
            DrawItem drawItem,
            DrawItem zoomItem,
            Canvas canvas
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

        ShadowCanvas sc = Shadows.shadowOf(canvas);
        assertTrue(sc.getLinePaintHistoryCount() == 2);
        checkPositions(sc, 2, 10, 10, 20, 20);

        ShadowCanvas.LinePaintHistoryEvent line = sc.getDrawnLine(1);
        assertTrue(line.startX==10);
        assertTrue(line.startY==10);
        assertTrue(line.stopX==30);
        assertTrue(line.stopY==30);
    }

    @Test
    public void localZoom () {
        // 2 lines one zoomed one not
        resetEverything(shadowCanvas, motionEvent, scribbleView, activity);
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        LineDrawItem zoomLine = new LineDrawItem(motionEvent, scribbleView, false);
        localZoom(motionEvent, scribbleView, line, zoomLine, canvas);
    }

    public static void saveRestore (MotionEvent motionEvent, DrawItem drawItem, ScribbleView scribbleView, Canvas canvas) throws IOException {
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
        ShadowCanvas sc = Shadows.shadowOf(canvas);
        checkPositions(sc, 1, 10, 10, 20, 20);
//        assertTrue(newLine.deleted == false);
    }

    @Test
    public void saveRestore () throws IOException {
        // Set up a line
        resetEverything(shadowCanvas, motionEvent, scribbleView, activity);
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        saveRestore(motionEvent, line, scribbleView, canvas);
    }

}
