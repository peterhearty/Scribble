/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

/**
 */
public class GridButtonHandler implements View.OnClickListener, View.OnTouchListener {

    private ScribbleView mScribbleView;
    private ImageButton mButton;
    private Paint mPaint;

    public static GridButtonHandler gridButtonHandler;
    public static int sGridStatus; // 0=off 1=dots 2=lines
    private static float sInterval = 100;


    public static final int GRID_OFF   = 0;
    public static final int GRID_ON    = 1;
    public static final int GRID_LINES = 2;


    public GridButtonHandler(ScribbleView v, ImageButton b) {
        mScribbleView = v;
        mButton = b;
        mButton.setBackgroundColor(ScribbleMainActivity.grey);
        mButton.setOnTouchListener(this);
        gridButtonHandler = this;
        mPaint = new Paint();
        mPaint.setColor(Color.LTGRAY);
    }

    /**
     * A simple color change on being pressed.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            v.setBackgroundColor(Color.LTGRAY);
        }
        if (event.getAction() == MotionEvent.ACTION_UP){
            v.setBackgroundColor(ScribbleMainActivity.grey);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        sGridStatus++;
        if (sGridStatus == 3) {
            mScribbleView.drawAllBorders = !mScribbleView.drawAllBorders;
            sGridStatus = 0;
        }
        mScribbleView.invalidate();
    }

    public void onDraw (Canvas c) {
        if (sGridStatus == 0) return;

        PointF viewOffset = mScribbleView.getmScrollOffset();

        // get nearest grid point to view offset
        float gridStartX = (float) Math.floor(viewOffset.x/sInterval)*sInterval;
        float gridStarty = (float) Math.floor(viewOffset.y/sInterval)*sInterval;


        float rightEdge = mScribbleView.getWidth();
        float bottomEdge = mScribbleView.getHeight();

        if (sGridStatus == GRID_ON) {
            // draw a grid of dots
            float storedx = gridStartX;
            float screenx = mScribbleView.storedXtoScreen(storedx);
            while (screenx < rightEdge) {
                float storedy = gridStarty;
                float screeny = mScribbleView.storedYtoScreen(storedy);
                while (screeny < bottomEdge) {
                    c.drawCircle(screenx,screeny,5.0f, mPaint);
                    storedy += sInterval;
                    screeny = mScribbleView.storedYtoScreen(storedy);
                }
                storedx += sInterval;
                screenx = mScribbleView.storedXtoScreen(storedx);
            }

        } else {
            // draw lines
            float storedy = gridStarty;
            float screeny = mScribbleView.storedYtoScreen(storedy);
            while (screeny < bottomEdge) {
                c.drawLine(0f,screeny,rightEdge,screeny, mPaint);
                storedy += sInterval;
                screeny = mScribbleView.storedYtoScreen(storedy);
            }
        }
    }

    private static float nearestLateralCoordinate (float z) {
        float result = (float) Math.floor(z/sInterval)*sInterval;
        float remainder = z - result;
        if (remainder > sInterval/2) {
            result += sInterval;
        }
        return result;
    }

    public static PointF nearestGridPoint (float inX, float inY) {
        float x = nearestLateralCoordinate(inX);
        float y = nearestLateralCoordinate(inY);
        PointF result = new PointF(x,y);
        return result;
    }
}
