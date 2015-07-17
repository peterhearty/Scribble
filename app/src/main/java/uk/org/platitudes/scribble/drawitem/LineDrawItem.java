/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleView;

/**
 * Draws a straight line.
 */
public class LineDrawItem  implements DrawItem {

    private PointF mStartPoint;
    private PointF mEndPoint;
    private Paint mPpaint;

    public LineDrawItem (MotionEvent event, ScribbleView scribbleView) {
        mPpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPpaint.setColor(Color.BLACK);
        mPpaint.setStrokeWidth(5f);
        handleMoveEvent(event, scribbleView);
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

}
