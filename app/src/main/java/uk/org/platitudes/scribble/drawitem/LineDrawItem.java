/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

/**
 * Draws a straight line.
 */
public class LineDrawItem  extends DrawItem {

    private PointF mEndPoint;
    private boolean firstPointAdded;

    public LineDrawItem (MotionEvent event, ScribbleView scribbleView) {
        super(event, scribbleView);
        handleMoveEvent(event);
    }

    @Override
    public int getHashTag() {
        int result = (int) (LINE*1000 + mStart.x +mStart.y +mEndPoint.x +mEndPoint.y);
        return result;
    }

    @Override
    public void draw(Canvas c) {
        drawBounds(c);

        if (mStart == null || mEndPoint == null) return;
        float startX = mScribbleView.storedXtoScreen(mStart.x);
        float startY = mScribbleView.storedYtoScreen(mStart.y);

        float deltaX = mEndPoint.x - mStart.x;
        float deltaY = mEndPoint.y - mStart.y;

        float zoomedX = mStart.x + deltaX * mZoom;
        float zoomedY = mStart.y + deltaY * mZoom;

        float endX   = mScribbleView.storedXtoScreen(zoomedX);
        float endY   = mScribbleView.storedYtoScreen(zoomedY);

        c.drawLine(startX, startY, endX, endY, mPaint);
    }

    private void addPoint (float x, float y, ScribbleView scribbleView) {
        float storedX = mScribbleView.screenXtoStored(x);
        float storedY = mScribbleView.screenYtoStored(y);
        if (!firstPointAdded) {
            mStart = new PointF (storedX, storedY);
            firstPointAdded = true;
        } else {
            mEndPoint = new PointF(storedX, storedY);
        }
    }

    @Override
    public void handleMoveEvent(MotionEvent event) {
        addPoint(event.getX(), event.getY(), mScribbleView);

    }

    public void handleUpEvent (MotionEvent event) {
        addPoint(event.getX(), event.getY(), mScribbleView);
        mScribbleView.addItem(this);
    }

    /**
     * Constructor to read data from file.
     */
    public LineDrawItem (ScribbleInputStream dis, int version, ScribbleView sv) {
        super(null, sv);
        mEndPoint = new PointF();
        try {
            readFromFile(dis, version);
        } catch (IOException e) {
            ScribbleMainActivity.log("LineDrawItem", "", e);
        }
    }

    public void saveToFile (ScribbleOutputStream dos, int version) throws IOException {
        dos.writeByte(LINE);
        dos.writeFloat(mZoom);
        dos.writeByte(deleted ? 1:0);
        dos.writeFloat(mStart.x);
        dos.writeFloat(mStart.y);
        dos.writeFloat(mEndPoint.x);
        dos.writeFloat(mEndPoint.y);
    }

    public DrawItem readFromFile (ScribbleInputStream dis, int version) throws IOException {
        if (version >= 1002) {
            mZoom = dis.readFloat();
            byte deletedByte = dis.readByte();
            if (deletedByte==1) {
                deleted = true;
            }
        }
        mStart.x = dis.readFloat();
        mStart.y = dis.readFloat();
        mEndPoint.x = dis.readFloat();
        mEndPoint.y = dis.readFloat();
        return this;
    }

    public void move(float deltaX, float deltaY) {
        mStart.x += deltaX;
        mStart.y += deltaY;
        mEndPoint.x += deltaX;
        mEndPoint.y += deltaY;
    }

    public RectF getBounds () {
        if (mStart == null || mEndPoint == null) return null;

        float deltaX = mEndPoint.x - mStart.x;
        float deltaY = mEndPoint.y - mStart.y;

        float zoomedX = mStart.x + deltaX * mZoom;
        float zoomedY = mStart.y + deltaY * mZoom;

        float minX = Math.min(mStart.x, zoomedX);
        float maxX = Math.max(mStart.x, zoomedX);
        float minY = Math.min(mStart.y, zoomedY);
        float maxY = Math.max(mStart.y, zoomedY);

        maxX = maxX+FUZZY;
        minX = minX-FUZZY;
        maxY = maxY+FUZZY;
        minY = minY-FUZZY;


        RectF result = new RectF(minX, minY, maxX, maxY);
        return result;
    }

}
