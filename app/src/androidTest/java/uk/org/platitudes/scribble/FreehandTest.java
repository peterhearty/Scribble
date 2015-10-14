/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.graphics.RectF;

import java.io.IOException;

import uk.org.platitudes.scribble.drawitem.freehand.FreehandCompressedDrawItem;

/**
 */
public class FreehandTest extends AbstractDrawItem {

    public FreehandTest () {
        super();
    }

    @Override
    public void setUp() throws Exception {
        ScribbleMainActivity.log("<<<<<<<<<< FreehandTest", "setUp >>>>>>>>>>>>>", null);
        super.setUp();
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

    private boolean checkCanvasPositions (float[] xs, float[] ys) {
        boolean result = true;
        for (int i=0; i<xs.length-1; i++) {
            RectF line = canvas.history.get(i);
            if (line.left != xs[i]) {
                result = false;
                break;
            }
            if (line.top != ys[i]) {
                result = false;
                break;
            }
            if (line.right != xs[i+1]) {
                result = false;
                break;
            }
            if (line.bottom != ys[i+1]) {
                result = false;
                break;
            }
        }
        return result;
    }

    private float[] x1s = {0,10,20,30,20,10,0};
    private float[] y1s = {0,10,20,30,40,50,60};

    public void testSimpleFreehandTest () {
        ScribbleMainActivity.log ("-- FreehandTest", "simpleFreehandTest --", null);
        FreehandCompressedDrawItem free = createFreehand(x1s, y1s);
        free.draw(canvas);
        assertTrue(canvas.lineCount==x1s.length-1);
        assertTrue(checkCanvasPositions(x1s, y1s));
    }

    public void testMmoveSizeTest () {
        ScribbleMainActivity.log ("-- FreehandTest", "moveSizeTest --", null);
        resetEverything();
        FreehandCompressedDrawItem line = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        moveSizeTest(line);
    }

    public void testLocalZoom () {
        ScribbleMainActivity.log ("-- FreehandTest", "localZoom --", null);
        // 2 lines one zoomed one not
        resetEverything();
        FreehandCompressedDrawItem line = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        FreehandCompressedDrawItem zoomLine = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        localZoom(line, zoomLine);
    }

    public void testSaveRestore () throws IOException {
        ScribbleMainActivity.log ("-- FreehandTest", "saveRestore --", null);
        // Set up a line
        resetEverything();
        FreehandCompressedDrawItem line = new FreehandCompressedDrawItem(motionEvent, scribbleView);
        saveRestore(line);
    }


}
