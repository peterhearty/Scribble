/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.graphics.PointF;
import android.test.ActivityInstrumentationTestCase2;

import junit.framework.TestCase;

import uk.org.platitudes.scribble.drawitem.Handle;
import uk.org.platitudes.scribble.mock.TestCanvas;

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

public class HandleTest extends AbstractDrawItem {

    public HandleTest () {
        super();
    }


    @Override
    public void setUp() throws Exception {
        ScribbleMainActivity.log ("<<<<<<<<<< HandleTest", "setUp >>>>>>>>>>>>>", null);
        super.setUp();
    }

    public void testNearHandle () {
        ScribbleMainActivity.log ("-- testNearHandle", "setUp --", null);
        PointF p = new PointF(20f, 20f);
        Handle h = new Handle(p, scribbleView);
        boolean result = h.nearPoint(21f, 21f);
        assertTrue(result);
    }

    public void testHandleDraw () {
        ScribbleMainActivity.log ("-- testHandleDraw", "setUp --", null);
        PointF p = new PointF(20f, 20f);
        Handle h = new Handle(p, scribbleView);

        TestCanvas c = new TestCanvas();
        h.drawSelectionHandle(c);

        assertTrue(c.history.size() == 4);
    }


}
