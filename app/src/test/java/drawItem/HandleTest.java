/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package drawItem;

import android.graphics.PointF;
import android.os.Build;

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
import uk.org.platitudes.scribble.drawitem.Handle;

/**
 * This has to be in a subdirectory of app/src/test (created using right click New directory as
 * required by http://tools.android.com/tech-docs/unit-testing-support).
 * Module build.gradle updated with lines
 *
 * dependencies {
 *   compile fileTree(dir: 'libs', include: ['*.jar'])
 *  compile 'com.android.support:appcompat-v7:22.0.0'
 *  // https://developers.google.com/android/guides/setup
 *  compile 'com.google.android.gms:play-services-drive:7.5.0'
 *  testCompile 'junit:junit:4.12'
 *  testCompile "org.robolectric:robolectric:3.0"
 *  }
 *
 * Edit the run configurations to set the working directory to
 * /home/pete/AndroidStudioProjects/Scribble/app
 *
 * Need to manually add import static org.junit.Assert.*; to use assertThat();
 *
 * This imports static members e.g. double r = Math.cos(Math.PI * theta);
 * becomes import static java.lang.Math.*;
 * double r = cos(PI * theta);
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class)
public class HandleTest extends TestCase {

    private ScribbleView scribbleView;
    private ScribbleMainActivity activity;

    @Before
    @Override
    public void setUp() throws Exception {
        ScribbleMainActivity.log ("<<<<<<<<<< HandleTest", "setUp >>>>>>>>>>>>>", null);
        super.setUp();
        activity = Robolectric.buildActivity(ScribbleMainActivity.class).create().get();
        scribbleView = activity.getmMainView();
    }

    @Test
    public void testNearHandle () {
        ScribbleMainActivity.log ("-- testNearHandle", "setUp --", null);
        PointF p = new PointF(20f, 20f);
        uk.org.platitudes.scribble.drawitem.Handle h = new uk.org.platitudes.scribble.drawitem.Handle(p, scribbleView);
        boolean result = h.nearPoint(21f, 21f);
        assertTrue(result);
    }

    @Test
    public void testHandleDraw () {
        ScribbleMainActivity.log ("-- testHandleDraw", "setUp --", null);
        PointF p = new PointF(20f, 20f);
        uk.org.platitudes.scribble.drawitem.Handle h = new uk.org.platitudes.scribble.drawitem.Handle(p, scribbleView);

        TestCanvas c = new TestCanvas();
        h.drawSelectionHandle(c);

        assertTrue(c.history.size() == 4);
    }


}
