/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem.freehand;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.freehand.floatAndDeltas;
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
        int result = (int) (COMPRESSED_FREEHAND + x.min +lastX+y.min+lastY);
        return result;
    }



    @Override
    public void draw(Canvas c) {
        float x_val = x.firstFloat();
        float y_val = y.firstFloat();
        float startX = mScribbleView.storedXtoScreen(x_val);
        float startY = mScribbleView.storedYtoScreen(y_val);

        for (int i=0; i<numPoints-1; i++) {
            float nextX = x.nextFloat();
            float nextY = y.nextFloat();

            float endX   = mScribbleView.storedXtoScreen(nextX);
            float endY   = mScribbleView.storedYtoScreen(nextY);
            c.drawLine(startX, startY, endX, endY, mPaint);

            startX = endX;
            startY = endY;
        }
    }

    private void addPoint (float x_val, float y_val, ScribbleView scribbleView) {
        float storedX = scribbleView.screenXtoStored(x_val);
        float storedY = scribbleView.screenYtoStored(y_val);

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
        if (numPoints > 1)
            mScribbleView.addItem(this);
    }

    @Override
    public void saveToFile(ScribbleOutputStream dos, int version) throws IOException {
        dos.writeByte(COMPRESSED_FREEHAND);
        dos.writeInt(numPoints);
        x.write(dos);
        y.write(dos);
    }

    @Override
    public DrawItem readFromFile(ScribbleInputStream dis, int version) throws IOException {
        numPoints = dis.readInt();
        x.read(dis);
        y.read(dis);

        return null;
    }

    @Override
    public boolean selectItem(PointF p) {
        float minX = x.min-FUZZY;
        float maxX = x.max+FUZZY;
        float minY = y.min-FUZZY;
        float maxY = y.max+FUZZY;
        if (minX < p.x && p.x < maxX && minY < p.y && p.y < maxY) {
            mSelected = true;
            mPaint.setColor(Color.RED);
        }
        return mSelected;
    }

    public void move(float deltaX, float deltaY) {
        x.moveStart(deltaX);
        y.moveStart(deltaY);
    }
}
