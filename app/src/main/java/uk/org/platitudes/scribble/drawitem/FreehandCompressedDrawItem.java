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
public class FreehandCompressedDrawItem implements  DrawItem {

    public class floatAndDeltas {
        float mStart;
        byte[] mDeltas;
        float mLastCalculated;
        int mNextFreeDelta;
        int mCurExponent;
        float mCurMultiplier;
        int mPointer;

        public void write (DataOutputStream dos) throws IOException {
            dos.writeFloat(mStart);
            dos.writeInt(mNextFreeDelta);
            dos.write(mDeltas, 0, mNextFreeDelta);
        }
        public void read (DataInputStream dis) throws IOException {
            mStart = dis.readFloat();
            mNextFreeDelta = dis.readInt();
            mDeltas = new byte[mNextFreeDelta];
            dis.read(mDeltas, 0, mNextFreeDelta);
        }

        private void addPoint (float f) {
            float dx = f - mLastCalculated;
            if (f != 0 && Math.abs(dx/f) < 0.0001) {
                // less than 0.01% change, ignore
                dx = 0;
            }
            int exp = 0;
            if (dx != 0 ) {
                // get in range [-126,126]
                while (Math.abs(dx) > 126) {
                    exp++;
                    dx /= 10;
                }
                while (Math.abs(dx) < 10) {
                    exp--;
                    dx *= 10;
                }
                while (exp > mCurExponent) {
                    addByte((byte) 127);
                    mCurExponent++;
                    mCurMultiplier *= 10;
                }
                while (exp < mCurExponent) {
                    addByte((byte) -127);
                    mCurExponent--;
                    mCurMultiplier /= 10;
                }
            }
            byte byteDelta = (byte)Math.round(dx);// ROUND
            addByte(byteDelta);
            mLastCalculated = mLastCalculated + byteDelta * mCurMultiplier;
        }

        public floatAndDeltas () {
            mDeltas = new byte[100];
            mCurExponent = 0;
            mCurMultiplier = 1;
        }

        public float firstFloat () {
            mPointer=0;
            mCurMultiplier=1;
            mLastCalculated = mStart;
            return mStart;
        }

        public float nextFloat () {
            byte b = mDeltas[mPointer++];
            while (b==127) {
                mCurMultiplier *= 10;
                b = mDeltas[mPointer++];
            }
            while (b==-127) {
                mCurMultiplier /= 10;
                b = mDeltas[mPointer++];
            }
            float result = mLastCalculated + b * mCurMultiplier;
            mLastCalculated = result;
            return result;
        }

        private void checkArraySize () {
            if (mNextFreeDelta < mDeltas.length) return;

            byte[] newArray = new byte[mNextFreeDelta*2];
            System.arraycopy(mDeltas, 0, newArray, 0, mNextFreeDelta);
            mDeltas = newArray;
            return;
        }

        public void addByte (byte b) {
            checkArraySize ();
            mDeltas[mNextFreeDelta++] = b;
        }
    }

    private floatAndDeltas x;
    private floatAndDeltas y;
    private int numPoints;
    private Paint mPpaint;

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
        try {
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

        } catch (Exception e) {
            String s = e.toString();
        }

    }

    float lastX,lastY;

    private void addPoint (float x_val, float y_val, ScribbleView scribbleView) {
        float storedX = scribbleView.screenXtoStored(x_val);
        float storedY = scribbleView.screenYtoStored(y_val);

        if (numPoints == 0) {
            // This is the first point in the curve
            x.mStart = x.mLastCalculated = storedX;
            y.mStart = y.mLastCalculated = storedY;
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
        try {
            final int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; h++) {
                addPoint(event.getHistoricalX(h), event.getHistoricalY(h), scribbleView);
            }
            addPoint(event.getX(), event.getY(), scribbleView);

        } catch (Exception e) {
            String s = e.toString();
        }

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
