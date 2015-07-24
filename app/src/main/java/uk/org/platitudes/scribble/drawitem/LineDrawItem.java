/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
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

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

/**
 * Draws a straight line.
 */
public class LineDrawItem  extends DrawItem {

    private PointF mStartPoint;
    private PointF mEndPoint;

    public LineDrawItem (MotionEvent event, ScribbleView scribbleView) {
        super(event, scribbleView);
        handleMoveEvent(event);
    }

    @Override
    public void draw(Canvas c) {
        if (mStartPoint == null || mEndPoint == null) return;
        float startX = mScribbleView.storedXtoScreen(mStartPoint.x);
        float startY = mScribbleView.storedYtoScreen(mStartPoint.y);
        float endX   = mScribbleView.storedXtoScreen(mEndPoint.x);
        float endY   = mScribbleView.storedYtoScreen(mEndPoint.y);
        c.drawLine(startX, startY, endX, endY, mPaint);
    }

    private void addPoint (float x, float y, ScribbleView scribbleView) {
        float storedX = scribbleView.screenXtoStored(x);
        float storedY = scribbleView.screenYtoStored(y);
        if (mStartPoint == null) {
            mStartPoint = new PointF (storedX, storedY);
        } else {
            mEndPoint = new PointF(storedX, storedY);
        }
    }

    @Override
    public void handleMoveEvent(MotionEvent event) {
        addPoint(event.getX(), event.getY(), mScribbleView);

    }

    public void handleUpEvent (MotionEvent event) {
        mScribbleView.addItem(this);
    }

    /**
     * Constructor to read data from file.
     */
    public LineDrawItem (DataInputStream dis, int version, ScribbleView sv) {
        super(null, sv);
        mStartPoint = new PointF();
        mEndPoint = new PointF();
        try {
            readFromFile(dis, version);
        } catch (IOException e) {
            ScribbleMainActivity.log("LineDrawItem", "", e);
        }
    }

    public void saveToFile (DataOutputStream dos, int version) throws IOException {
        dos.writeByte(LINE);
        dos.writeFloat(mStartPoint.x);
        dos.writeFloat(mStartPoint.y);
        dos.writeFloat(mEndPoint.x);
        dos.writeFloat(mEndPoint.y);
    }

    public DrawItem readFromFile (DataInputStream dis, int version) throws IOException {
        mStartPoint.x = dis.readFloat();
        mStartPoint.y = dis.readFloat();
        mEndPoint.x = dis.readFloat();
        mEndPoint.y = dis.readFloat();
        return this;
    }

    @Override
    public boolean selectItem(PointF p) {
        float minX = Math.min(mStartPoint.x, mEndPoint.x)-FUZZY;
        float maxX = Math.max(mStartPoint.x, mEndPoint.x)+FUZZY;
        float minY = Math.min(mStartPoint.y, mEndPoint.y)-FUZZY;
        float maxY = Math.max(mStartPoint.y, mEndPoint.y)+FUZZY;
        if (minX < p.x && p.x < maxX && minY < p.y && p.y < maxY) {
            mSelected = true;
            mPaint.setColor(Color.RED);
        }
        return mSelected;
    }

    public void move(float deltaX, float deltaY) {
        mStartPoint.x += deltaX;
        mStartPoint.y += deltaY;
        mEndPoint.x += deltaX;
        mEndPoint.y += deltaY;
    }

}
