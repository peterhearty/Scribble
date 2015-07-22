package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleView;

/**
 * Used to track selected items.
 */
public class SelectItem  implements DrawItem {


    private Paint mPpaint;
    private PointF mStartPoint;
    private PointF mEndPoint;



    public SelectItem (MotionEvent event, ScribbleView scribbleView) {
        createPaint();
        addPoint(event.getX(), event.getY(), scribbleView);
        ItemList drawItems = scribbleView.getmDrawItems();
        drawItems.toggleSelected(mStartPoint, false);
    }

    private void createPaint () {
        mPpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPpaint.setColor(Color.BLACK);
        mPpaint.setStrokeWidth(5f);
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
        ItemList drawItems = scribbleView.getmDrawItems();
        DrawItem item = drawItems.getmLastSelected();
        if (item.isSelected()) {
            addPoint (event.getX(), event.getY(), scribbleView);
            float deltaX = mEndPoint.x - mStartPoint.x;
            float deltaY = mEndPoint.y - mStartPoint.y;
            mStartPoint = mEndPoint;
            item.move(deltaX, deltaY);
        }
    }

    @Override
    public void handleUpEvent(MotionEvent event, ScribbleView scribbleView) {

    }

    public void saveToFile(DataOutputStream dos, int version) throws IOException {

    }

    @Override
    public DrawItem readFromFile(DataInputStream dis, int version) throws IOException {
        return null;
    }

    /* Do nothing methods */
    public void draw(Canvas c, ScribbleView scribbleView) {}
    public boolean toggleSelected(PointF p) {return false;}
    public boolean isSelected() {return false;}
    public void move(float deltaX, float deltaY) {}
}
