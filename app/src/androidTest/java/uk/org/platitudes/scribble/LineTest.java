/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.Instrumentation;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;

import java.io.IOException;

import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.LineDrawItem;
import uk.org.platitudes.scribble.drawitem.ResizeItem;
import uk.org.platitudes.scribble.mock.TestCanvas;

/**
 */
public class LineTest extends AbstractDrawItem {

    public LineTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        ScribbleMainActivity.log("<<<<<<<<<< LineTest", "setUp >>>>>>>>>>>>>", null);
        super.setUp();
    }

    public void testBasicLine () {
        ScribbleMainActivity.log ("-- LineTest", "testBasicLine --", null);
        resetEverything();
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);

        // No end point means the line should not draw anything
        line.draw(canvas);
        assertTrue(canvas.lineCount == 0);

        // complete the line
        motionEvent.setLocation(20f, 20f);
        line.handleMoveEvent(motionEvent);

        // check that it draws
        line.draw(canvas);
        assertTrue(canvas.lineCount==1);

        // select the line - should draw line (1 line), border, (4 lines), 2 handles (2x4 = 8 lines)
        PointF selectionPoint = new PointF(15f, 15f);
        line.selectItem(selectionPoint);
        canvas.testReset();
        line.draw(canvas);
        assertTrue(canvas.lineCount == 13);

        // deslect should return to just drawing the line
        line.deselectItem();
        canvas.testReset();
        line.draw(canvas);
        assertTrue(canvas.lineCount==1);
    }

    public void testMoveSizeTest () {
        ScribbleMainActivity.log ("-- LineTest", "setUp --", null);
        resetEverything();
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        moveSizeTest(line);
    }

    public void testlocalZoom () {
        ScribbleMainActivity.log ("-- LineTest", "localZoom --", null);
        // 2 lines one zoomed one not
        resetEverything();
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        LineDrawItem zoomLine = new LineDrawItem(motionEvent, scribbleView, false);
        localZoom(line, zoomLine);
    }

    public void testSsaveRestore () throws IOException {
        ScribbleMainActivity.log ("-- LineTest", "saveRestore --", null);
        // Set up a line
        resetEverything();
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        saveRestore(line);
    }

}
