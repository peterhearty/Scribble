/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem.freehand;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

/**
 * Freehand drawing tool.
 *
 * Currently assumes that coordinates are stored as floatAndDeltas, but nothing in the class
 * depends this. We could abstract the coordinates as an interface and have multiple storage
 * options.
 */
public class FreehandCompressedDrawItem extends DrawItem {

    private floatAndDeltas x;
    private floatAndDeltas y;
    private int numPoints;
    private float lastX,lastY;


    public FreehandCompressedDrawItem (MotionEvent event, ScribbleView scribbleView) {
        super(event, scribbleView);
        x = new floatAndDeltas();
        y = new floatAndDeltas();
        handleMoveEvent(event);
    }

    public FreehandCompressedDrawItem(ScribbleInputStream dis, int version, ScribbleView scribbleView) throws IOException {
        super(null, scribbleView);
        x = new floatAndDeltas();
        y = new floatAndDeltas();
        readFromFile(dis, version);
    }

    @Override
    public int getHashTag() {
        // Note - use start x/y vals not min or max.
        // Latter don't get calculated on being first read in.
        int result = (int) (COMPRESSED_FREEHAND*1000 + x.firstFloat() + y.firstFloat());
        return result;
    }

    private void privateDraw (Canvas c) {
        drawBounds(c);

        float x_val = x.firstFloat();
        float y_val = y.firstFloat();

        float startX = mScribbleView.storedXtoScreen(x_val);
        float startY = mScribbleView.storedYtoScreen(y_val);

        if (numPoints == 1) {
            // draw a small dot at teh single point
            c.drawCircle(startX, startY, 5, mPaint);
            return;
        }

        for (int i = 0; i < numPoints - 1; i++) {
            float nextX = x.nextFloat(mZoom);
            float nextY = y.nextFloat(mZoom);

            float endX = mScribbleView.storedXtoScreen(nextX);
            float endY = mScribbleView.storedYtoScreen(nextY);
            c.drawLine(startX, startY, endX, endY, mPaint);

            startX = endX;
            startY = endY;
        }
    }

    @Override
    public void draw(Canvas c) {
        // Usually the UI thread is the only thing that performs drawing. However, during testing
        // we can draw onto a test canvas as well. Each floatAndDeltas expects to be called from
        // first to last float in strict order. Two threads drawing will muck this up, so we
        // synchonize access to the object when testing.
        if (ScribbleMainActivity.testInProgress) {
            synchronized (this) {
                privateDraw (c);
            }
        } else {
            // no test in progress, don't slow things down getting locks
            privateDraw (c);
        }
    }

    private void addPoint (float x_val, float y_val, ScribbleView scribbleView) {
        float storedX = mScribbleView.screenXtoStored(x_val);
        float storedY = mScribbleView.screenYtoStored(y_val);

        if (numPoints == 0) {
            // This is the first point in the curve
            x.addPoint(storedX);
            y.addPoint(storedY);
            lastX = x_val;
            lastY = y_val;
            numPoints = 1;
            return;
        }

        if (lastX == x_val && lastY == y_val) {
            // Ignore duplicate points
            return;
        }

        lastX = x_val;
        lastY = y_val;

        x.addPoint(storedX);
        y.addPoint(storedY);

        numPoints++;
    }


    @Override
    public void handleMoveEvent(MotionEvent event) {
        final int historySize = event.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            addPoint(event.getHistoricalX(h), event.getHistoricalY(h), mScribbleView);
        }
        addPoint(event.getX(), event.getY(), mScribbleView);
    }

    @Override
    public void handleUpEvent(MotionEvent event) {
        mScribbleView.addItem(this);
    }

    @Override
    public void saveToFile(ScribbleOutputStream dos, int version) throws IOException {
        dos.writeByte(COMPRESSED_FREEHAND);
        dos.writeFloat(mZoom);
        dos.writeByte(deleted ? 1:0);
        dos.writeInt(numPoints);
        x.write(dos);
        y.write(dos);
    }

    @Override
    public DrawItem readFromFile(ScribbleInputStream dis, int version) throws IOException {
        if (version >= 1002) {
            mZoom = dis.readFloat();
            byte deletedByte = dis.readByte();
            if (deletedByte==1) {
                deleted = true;
            }
        }
        numPoints = dis.readInt();
        x.read(dis, version);
        y.read(dis, version);

        return null;
    }

    public RectF getBounds () {
        RectF result = new RectF(x.min-FUZZY, y.min-FUZZY, x.max+FUZZY, y.max+FUZZY);
        return result;
    }


    public void move(float deltaX, float deltaY) {
        x.moveStart(deltaX);
        y.moveStart(deltaY);
    }
}
