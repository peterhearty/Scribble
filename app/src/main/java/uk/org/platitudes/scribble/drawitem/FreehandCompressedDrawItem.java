/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleView;

/**
 * Freehand drawing tool.
 *
 * Currently assumes that coordinates are stored as floatAndDeltas, but nothing in the class
 * assumes this. We could abstract the coordinates as an interface and have multiple storage
 * options.
 */
public class FreehandCompressedDrawItem implements  DrawItem {

    private floatAndDeltas x;
    private floatAndDeltas y;
    private int numPoints;
    private Paint mPpaint;
    private float lastX,lastY;


    public FreehandCompressedDrawItem (MotionEvent event, ScribbleView scribbleView) {
        createPaint();
        x = new floatAndDeltas();
        y = new floatAndDeltas();
        handleMoveEvent(event, scribbleView);
    }

    public FreehandCompressedDrawItem(DataInputStream dis, int version) throws IOException {
        createPaint();
        x = new floatAndDeltas();
        y = new floatAndDeltas();
        readFromFile(dis, version);
    }

    private void createPaint () {
        mPpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPpaint.setColor(Color.BLACK);
        mPpaint.setStrokeWidth(5f);
    }

    @Override
    public void draw(Canvas c, ScribbleView scribbleView) {
        float x_val = x.firstFloat();
        float y_val = y.firstFloat();
        float startX = scribbleView.storedXtoScreen(x_val);
        float startY = scribbleView.storedYtoScreen(y_val);

        for (int i=0; i<numPoints-1; i++) {
            float nextX = x.nextFloat();
            float nextY = y.nextFloat();

            float endX   = scribbleView.storedXtoScreen(nextX);
            float endY   = scribbleView.storedYtoScreen(nextY);
            c.drawLine(startX, startY, endX, endY, mPpaint);

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
    public void handleMoveEvent(MotionEvent event, ScribbleView scribbleView) {
        final int historySize = event.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            addPoint(event.getHistoricalX(h), event.getHistoricalY(h), scribbleView);
        }
        addPoint(event.getX(), event.getY(), scribbleView);
    }

    @Override
    public void handleUpEvent(MotionEvent event, ScribbleView scribbleView) {
        scribbleView.addItem(this);
    }

    @Override
    public void saveToFile(DataOutputStream dos, int version) throws IOException {
        dos.writeByte(COMPRESSED_FREEHAND);
        dos.writeInt(numPoints);
        x.write(dos);
        y.write(dos);
    }

    @Override
    public DrawItem readFromFile(DataInputStream dis, int version) throws IOException {
        numPoints = dis.readInt();
        x.read(dis);
        y.read(dis);

        return null;
    }
}
