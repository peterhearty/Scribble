package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleView;

/**
 */
public class FreehandCompressedDrawItem implements  DrawItem{

    private float mBaseX;
    private float mBaseY;
    private byte[] mDeltaX;
    private byte[] mDeltaY;
    private float mLastX;
    private float mLastY;
    private int numPoints;

    private Paint mPpaint;

    public FreehandCompressedDrawItem (MotionEvent event, ScribbleView scribbleView) {
        createPaint();
        mDeltaX = new byte[100];
        mDeltaY = new byte[100];
        handleMoveEvent(event, scribbleView);
    }

    public FreehandCompressedDrawItem(DataInputStream dis, int version) throws IOException {
        createPaint();
        readFromFile(dis, version);
    }

    private void createPaint () {
        mPpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPpaint.setColor(Color.BLACK);
        mPpaint.setStrokeWidth(5f);
    }

    private float deltaToFloat (byte b, float f) {
//        float r = b*f;
//        r = r/ 1000;
//        r = r+f;
        float r = b+f;
        return r;
    }

    @Override
    public void draw(Canvas c, ScribbleView scribbleView) {
        float x = mBaseX;
        float y = mBaseY;
        for (int i=0; i<numPoints-1; i++) {
            float nextX = deltaToFloat(mDeltaX[i], x);
            float nextY = deltaToFloat(mDeltaY[i], y);

            float startX = scribbleView.storedXtoScreen(x);
            float startY = scribbleView.storedYtoScreen(y);
            float endX   = scribbleView.storedXtoScreen(nextX);
            float endY   = scribbleView.storedYtoScreen(nextY);
            c.drawLine(startX, startY, endX, endY, mPpaint);

            x = nextX;
            y = nextY;
        }

    }

    private byte[] checkArraySize (byte[] old, int reqSize) {
        if (old.length > reqSize) return old;

        byte[] newArray = new byte[reqSize*2];
        System.arraycopy(old, 0, newArray, 0, reqSize-1);
        return newArray;
    }

    private void addPoint (float x, float y, ScribbleView scribbleView) {
        float storedX = scribbleView.screenXtoStored(x);
        float storedY = scribbleView.screenYtoStored(y);

        if (numPoints == 0) {
            // This is the first point in the curve
            mBaseX = mLastX = storedX;
            mBaseY = mLastY = storedY;
            numPoints = 1;
            return;
        }

        // Calculate difference as 0.1% from last point added
//        float dx = (storedX - mLastX)/mLastX*1000;
//        float dy = (storedY - mLastY)/mLastY*1000;
        float dx = storedX - mLastX;
        float dy = storedY - mLastY;
        float absdx = Math.abs(dx);
        float absdy = Math.abs(dy);

        // Ignore tiny changes
        if (absdx< 1 && absdy < 1) {
            return;
        }

        if (absdx > 127 || absdy > 127) {
            // Point is too far away from last point to encode in 0.1% values
            // Invent new point
            // TODO add intermediate point
            if (dx < -127) dx = -127;
            if (dx > 127) dx = 127;
            if (dy < -127) dy = -127;
            if (dy > 127) dy = 127;
        }

        mDeltaX = checkArraySize (mDeltaX, numPoints);
        mDeltaY = checkArraySize (mDeltaY, numPoints);

        mDeltaX[numPoints-1] = (byte)dx;
        mDeltaY[numPoints-1] = (byte)dy;

        float xx = deltaToFloat(mDeltaX[numPoints-1], mLastX);
        float yy = deltaToFloat(mDeltaY[numPoints-1], mLastY);

        numPoints++;
        mLastX = storedX;
        mLastY = storedY;
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
        dos.writeFloat(mBaseX);
        dos.writeFloat(mBaseY);
        dos.writeFloat(mLastX); // don't really need lastx lasty
        dos.writeFloat(mLastY);
        dos.write(mDeltaX, 0, numPoints - 1);
        dos.write(mDeltaY,0,numPoints-1);
    }

    @Override
    public DrawItem readFromFile(DataInputStream dis, int version) throws IOException {
        numPoints = dis.readInt();
        mBaseX = dis.readFloat();
        mBaseY = dis.readFloat();
        mLastX = dis.readFloat();
        mLastY = dis.readFloat();
        mDeltaX = new byte[numPoints];
        mDeltaY = new byte[numPoints];
        dis.read(mDeltaX,0,numPoints-1);
        dis.read(mDeltaY,0,numPoints-1);

        return null;
    }
}
