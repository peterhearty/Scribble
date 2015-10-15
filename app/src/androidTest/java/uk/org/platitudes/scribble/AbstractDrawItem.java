package uk.org.platitudes.scribble;

import android.app.Instrumentation;
import android.graphics.RectF;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;

import java.io.File;
import java.io.IOException;

import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.ResizeItem;
import uk.org.platitudes.scribble.mock.TestCanvas;

/**
 * Created by pete on 14/10/15.
 */
public class AbstractDrawItem extends ActivityInstrumentationTestCase2 {

    public ScribbleView scribbleView;
    public ScribbleMainActivity activity;
    public MotionEvent motionEvent;
    public TestCanvas canvas;
    public Instrumentation instrumentation;
    public Drawing drawing;

    public AbstractDrawItem() {
        super(ScribbleMainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        ScribbleMainActivity.log("AbstractDrawItem", "setUp", null);
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = (ScribbleMainActivity) getActivity();
        scribbleView = activity.getmMainView();
        ScribbleMainActivity.testInProgress = true;
        motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0);
        canvas = new TestCanvas();
        instrumentation = getInstrumentation();
        drawing = scribbleView.getDrawing();

        drawing.useDefaultFile();
        drawing.clear();
    }

    public static boolean closeTo (float value, float target, float allowedDelta) {
        boolean result = true;
        float delta = Math.abs(value-target);
        if (delta > allowedDelta) {
            result = false;
        }
        return result;
    }

    public void setZoom (final float zoom) {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.getmZoomButtonHandler().setsZoom(zoom);
            }
        });
    }

    public void resetEverything() {
        canvas.testReset();
        motionEvent.setLocation(10f, 10f);
        scribbleView.setmScrollOffset(0, 0);
        setZoom(1);
    }

    public void checkPositions (int expectedDrawCount, float startX, float startY, float endX, float endY) {
        assertTrue(canvas.lineCount==expectedDrawCount);

        RectF line = canvas.history.get(0);
        assertTrue(line.left == startX);
        assertTrue(line.top == startY);
        assertTrue(line.right == endX);
        assertTrue(line.bottom == endY);
    }

    /**
     * This is a public static method so that it can be used by FreehandTest as well
     * (and possibly others).
     */
    public void moveSizeTest (DrawItem drawItem) {
        // create an initial line from (10,10) to (20,20)
        motionEvent.setLocation(20f, 20f);
        drawItem.handleMoveEvent(motionEvent);
        drawItem.draw(canvas);
        checkPositions(1, 10, 10, 20, 20);

        // now we move the view offset
        scribbleView.setmScrollOffset(5, 5);
        canvas.testReset();
        drawItem.draw(canvas);
        checkPositions(1, 5, 5, 15, 15);

        // now change the zoom
        setZoom(2);
        canvas.testReset();
        drawItem.draw(canvas);
        checkPositions(1, 10, 10, 30, 30);

        // move the line from [(10,10),(20,20)] to [(15,20),(25,30)]
        // and repeat tests
        resetEverything();
        drawItem.move(5, 10);
        drawItem.draw(canvas);
        checkPositions(1, 15, 20, 25, 30);

        // now we move the view offset
        scribbleView.setmScrollOffset(5, 5);
        canvas.testReset();
        drawItem.draw(canvas);
        checkPositions(1, 10, 15, 20, 25);

        // now change the zoom
        setZoom(2);
        canvas.testReset();
        drawItem.draw(canvas);
        checkPositions(1, 20, 30, 40, 50);

    }

    public static MotionEvent get2PointerEvent (float x1, float y1, float x2, float y2) {
        // See http://stackoverflow.com/questions/11523423/how-to-generate-zoom-pinch-gesture-for-testing-for-android

        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[2];
        MotionEvent.PointerProperties pp1 = new MotionEvent.PointerProperties();
        pp1.id = 0;
        pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
        MotionEvent.PointerProperties pp2 = new MotionEvent.PointerProperties();
        pp2.id = 1;
        pp2.toolType = MotionEvent.TOOL_TYPE_FINGER;
        properties[0] = pp1;
        properties[1] = pp2;

        //specify the coordinations of the two touch points
        //NOTE: you MUST set the pressure and size value, or it doesn't work
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[2];
        MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();
        pc1.x = x1;
        pc1.y = y1;
        pc1.pressure = 1;
        pc1.size = 1;
        MotionEvent.PointerCoords pc2 = new MotionEvent.PointerCoords();
        pc2.x = x2;
        pc2.y = y2;
        pc2.pressure = 1;
        pc2.size = 1;
        pointerCoords[0] = pc1;
        pointerCoords[1] = pc2;

        /*
        public static MotionEvent obtain (
            long downTime, long eventTime,
            int action, int pointerCount, PointerProperties[] pointerProperties, PointerCoords[] pointerCoords,
            int metaState, int buttonState,
            float xPrecision, float yPrecision,
            int deviceId, int edgeFlags, int source, int flags)
         */
        MotionEvent twoPointerEvent = MotionEvent.obtain(
                0L, 0L,
                MotionEvent.ACTION_DOWN, 2, properties, pointerCoords,
                0,  0,
                1, 1,
                0, 0, 0, 0);

        return twoPointerEvent;
    }

    public void localZoom (DrawItem drawItem, DrawItem zoomItem) {
        motionEvent.setLocation(20f, 20f);
        drawItem.handleMoveEvent(motionEvent);
        zoomItem.handleMoveEvent(motionEvent);

        MotionEvent twoPointerEvent = get2PointerEvent(10, 10, 15, 15);

        // Zoom the line
        ResizeItem resizer = new ResizeItem(twoPointerEvent, zoomItem, scribbleView);
        twoPointerEvent = get2PointerEvent(10, 10, 20, 20);
        resizer.handleMoveEvent(twoPointerEvent);

        drawItem.draw(canvas);
        zoomItem.draw(canvas);

        assertTrue(canvas.lineCount == 2);
        checkPositions(2, 10, 10, 20, 20);

        RectF line = canvas.history.get(1);
        assertTrue(line.left==10);
        assertTrue(line.top==10);
        assertTrue(line.right==30);
        assertTrue(line.bottom==30);
    }

    public void saveRestore (DrawItem drawItem) throws IOException {
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
        checkPositions(1, 10, 10, 20, 20);
//        assertTrue(newLine.deleted == false);
    }

    public static File getTestFile (File[] files, String name) {
        for (File f : files) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    public static File getTestFileDirectory () {
        File extstorage = Environment.getExternalStorageDirectory();
        File[] dircontemts = extstorage.listFiles();
        File testFilesDir = getTestFile(dircontemts, "testFiles");
        return testFilesDir;
    }

    public static File constructTestFile (String name) {
        File testFilesDir = getTestFileDirectory();
        if (testFilesDir == null)
            return null;
        String fullPath = testFilesDir.getAbsolutePath()+File.separator+name;
        File f = new File (fullPath);
        return f;
    }

    public static File getTestFile (String name) {
        File testFilesDir = getTestFileDirectory();
        if (testFilesDir == null) {
            return null;
        }

        File[] dircontemts = testFilesDir.listFiles();
        File f = getTestFile(dircontemts, name);
        return f;
    }



}
