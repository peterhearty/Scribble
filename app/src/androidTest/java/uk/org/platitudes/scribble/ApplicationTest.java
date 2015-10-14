package uk.org.platitudes.scribble;

import android.app.Application;
import android.graphics.PointF;
import android.test.ApplicationTestCase;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestResult;
import junit.framework.Test;

import java.lang.Exception;
import java.lang.Override;

import uk.org.platitudes.scribble.drawitem.Handle;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);

    }

    /**
     * This runs before every test method.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SmallTest
    public void testA () {
        Application app = getApplication();
        app.onCreate();

    }
}