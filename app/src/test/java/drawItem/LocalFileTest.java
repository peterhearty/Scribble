/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package drawItem;

import android.graphics.Canvas;
import android.os.Build;
import android.os.Looper;
import android.view.MotionEvent;

import com.google.android.gms.common.ConnectionResult;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowCanvas;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.gms.ShadowGooglePlayServicesUtil;
import org.robolectric.util.ActivityController;

import java.io.File;

import uk.org.platitudes.scribble.BuildConfig;
import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

/**
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class, shadows=ShadowApp.class)
public class LocalFileTest extends TestCase {

    private ScribbleView scribbleView;
    private ScribbleMainActivity activity;
    private MotionEvent motionEvent;
    private Canvas canvas;
    private ShadowCanvas shadowCanvas;

    @Before
    @Override
    public void setUp() throws Exception {
        ScribbleMainActivity.log("<<<<<<<<<< LocalFileTest", "setUp >>>>>>>>>>>>>", null);
        super.setUp();

        // Turn off Google Analytics - Does not need to work anymore
        // http://stackoverflow.com/questions/29915790/robolectric-3-googleplayservicesnotavailableexception
        final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
        shadowApplication.declareActionUnbindable("com.google.android.gms.drive.ApiService.START");
        shadowApplication.declareActionUnbindable("com.google.android.gms.signin.service.START");
//        shadowApplication.declareActionUnbindable("com.google.android.gms.analytics.service.START");
        ShadowGooglePlayServicesUtil.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        // Don't try and set a breakpoint and step over any of the ActivityController lines - seems to hang
        ActivityController<ScribbleMainActivity> activityController =  Robolectric.buildActivity(ScribbleMainActivity.class);
        activityController.create();
        try {
            activityController.start();
        } catch (Throwable t) {
            ScribbleMainActivity.log("LocalFileTest", "setUp", t);
        }
        activity = activityController.get();
//        activity = Robolectric.buildActivity(ScribbleMainActivity.class).create().start().resume().visible().get();
//activity = new ScribbleMainActivity();
        scribbleView = activity.getmMainView();
        motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        canvas = new Canvas();
        shadowCanvas = Shadows.shadowOf(canvas);
    }

//    public static void resetEverything(TestCanvas canvas, MotionEvent motionEvent, ScribbleView scribbleView, ScribbleMainActivity activity) {
//        canvas.testReset();
//        motionEvent.setLocation(10f, 10f);
//        scribbleView.setmScrollOffset(0, 0);
//        activity.getmZoomButtonHandler().setsZoom(1);
//    }

    @Test
    public void loadComplexFile () {
        ScribbleMainActivity.log("-- LocalFileTest", "loadComplexFile --", null);
        Drawing drawing = scribbleView.getDrawing();
        File f = new File("/home/pete/Dropbox/AndroidDev/testFiles/vectorcalculus");
        drawing.setmCurrentlyOpenFile(f);

        ShadowView sv = Shadows.shadowOf(scribbleView);
        Looper looper = Looper.getMainLooper();

        drawing.openCurrentFile();
        drawing.getmDrawItems().onDraw(canvas);
        int lineCount = shadowCanvas.getLinePaintHistoryCount();
        int circleCount = shadowCanvas.getCirclePaintHistoryCount();
        int textCount = shadowCanvas.getTextHistoryCount();
        assertTrue(lineCount==13231);
        assertTrue(circleCount==12);
        assertTrue(textCount==15);

        // save as a text file
        // Note - FileChooser does this in a diff order: write followed by setmCurrentlyOpenFile
        File g = new File("/tmp/test_vectorcalculus.txt");
        drawing.setmCurrentlyOpenFile(g);
        drawing.write();

        // read it back in again
        shadowCanvas.resetCanvasHistory();
        drawing.openCurrentFile();
        drawing.getmDrawItems().onDraw(canvas);
        lineCount = shadowCanvas.getLinePaintHistoryCount();
        circleCount = shadowCanvas.getCirclePaintHistoryCount();
        textCount = shadowCanvas.getTextHistoryCount();
        assertTrue(lineCount == 13231);
        assertTrue(circleCount == 12);
        assertTrue(textCount==15);
    }

    @Test
    public void loadBrokenFile () {
        ScribbleMainActivity.log("-- LocalFileTest", "loadBrokenFile --", null);
        Drawing drawing = scribbleView.getDrawing();
        File f = new File("/home/pete/Dropbox/AndroidDev/testFiles/text.txt");
        drawing.setmCurrentlyOpenFile(f);

        drawing.openCurrentFile();
        drawing.getmDrawItems().onDraw(canvas);
        int lineCount = shadowCanvas.getLinePaintHistoryCount();
        int circleCount = shadowCanvas.getCirclePaintHistoryCount();
        int textCount = shadowCanvas.getTextHistoryCount();
        assertTrue(lineCount==13231);
        assertTrue(circleCount==12);
        assertTrue(textCount==15);

    }
}
