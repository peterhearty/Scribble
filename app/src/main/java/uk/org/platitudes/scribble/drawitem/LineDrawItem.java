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
public class LineDrawItem  implements DrawItem {

    private PointF mStartPoint;
    private PointF mEndPoint;
    private Paint mPpaint;
    boolean selected;

    public LineDrawItem (MotionEvent event, ScribbleView scribbleView) {
        createPaint();
        handleMoveEvent(event, scribbleView);
    }

    private void createPaint () {
        mPpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPpaint.setColor(Color.BLACK);
        mPpaint.setStrokeWidth(5f);
    }

    @Override
    public void draw(Canvas c, ScribbleView scribbleView) {
        if (mStartPoint == null || mEndPoint == null) return;
        float startX = scribbleView.storedXtoScreen(mStartPoint.x);
        float startY = scribbleView.storedYtoScreen(mStartPoint.y);
        float endX   = scribbleView.storedXtoScreen(mEndPoint.x);
        float endY   = scribbleView.storedYtoScreen(mEndPoint.y);
        c.drawLine(startX, startY, endX, endY, mPpaint);
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
    public void handleMoveEvent(MotionEvent event, ScribbleView scribbleView) {
        addPoint(event.getX(), event.getY(), scribbleView);

    }

    public void handleUpEvent (MotionEvent event, ScribbleView scribbleView) {
        scribbleView.addItem(this);
    }

    /**
     * Constructor to read data from file.
     */
    public LineDrawItem (DataInputStream dis, int version) {
        createPaint();
        mStartPoint = new PointF();
        mEndPoint = new PointF();
        try {
            readFromFile(dis, version);
        } catch (IOException e) {
            ScribbleMainActivity.makeToast("Error reading LineDrawItem "+e);
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
    public boolean toggleSelected(PointF p) {
        float minX = Math.min(mStartPoint.x, mEndPoint.x)-FUZZY;
        float maxX = Math.max(mStartPoint.x, mEndPoint.x)+FUZZY;
        float minY = Math.min(mStartPoint.y, mEndPoint.y)-FUZZY;
        float maxY = Math.max(mStartPoint.y, mEndPoint.y)+FUZZY;
        boolean selectChanged = false;
        if (minX < p.x && p.x < maxX && minY < p.y && p.y < maxY) {
            selectChanged = true;
            if (selected) {
                selected = false;
                mPpaint.setColor(Color.BLACK);
            } else {
                selected = true;
                mPpaint.setColor(Color.RED);
            }
        }
        return selectChanged;
    }

    public void move(float deltaX, float deltaY) {
        mStartPoint.x += deltaX;
        mStartPoint.y += deltaY;
        mEndPoint.x += deltaX;
        mEndPoint.y += deltaY;
    }


    public boolean isSelected() {return selected;}


}
