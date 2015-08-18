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
import uk.org.platitudes.scribble.drawitem.LineDrawItem;

/**
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class)
public class LineTest extends TestCase {

    private ScribbleView scribbleView;
    private ScribbleMainActivity activity;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = Robolectric.buildActivity(ScribbleMainActivity.class).create().get();
        scribbleView = activity.getmMainView();
    }

    @Test
    public void testBasicLine () {
        MotionEvent motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        LineDrawItem line = new LineDrawItem(motionEvent, scribbleView, false);
        TestCanvas canvas = new TestCanvas();

        // No end point means the line should not draw anything
        line.draw(canvas);
        int linesDrawn = canvas.history.size();
        assertTrue(linesDrawn == 0);

        // complete the line
        motionEvent.setLocation(20f, 20f);
        line.handleMoveEvent(motionEvent);

        // check that it draws
        line.draw(canvas);
        linesDrawn = canvas.history.size();
        assertTrue(linesDrawn == 1);

        // select the line - should draw line (1 line), border, (4 lines), 2 handles (2x4 = 8 lines)
        PointF selectionPoint = new PointF(15f, 15f);
        line.selectItem(selectionPoint);
        canvas.testReset();
        line.draw(canvas);
        linesDrawn = canvas.history.size();
        assertTrue(linesDrawn == 13);

        // deslect should return to just drawing the line
        line.deselectItem();
        canvas.testReset();
        line.draw(canvas);
        linesDrawn = canvas.history.size();
        assertTrue(linesDrawn == 1);

    }

}
