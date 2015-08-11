/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;
import android.widget.ImageButton;

import uk.org.platitudes.scribble.ScribbleView;

/**
 */
public class GridButtonHandler implements View.OnClickListener {

    private ScribbleView mScribbleView;
    private ImageButton mButton;
    private Paint mPaint;

    public static GridButtonHandler gridButtonHandler;
    public static int sGridStatus; // 0=off 1=dots 2=lines

    public static final int GRID_OFF   = 0;
    public static final int GRID_ON    = 1;
    public static final int GRID_LINES = 2;


    public GridButtonHandler(ScribbleView v, ImageButton b) {
        mScribbleView = v;
        mButton = b;
        gridButtonHandler = this;
        mPaint = new Paint();
        mPaint.setColor(Color.LTGRAY);
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

        float interval = 100;

        PointF viewOffset = mScribbleView.getmScrollOffset();

        // get nearest grid point to view offset
        float gridStartX = (float) Math.floor(viewOffset.x/interval)*interval;
        float gridStarty = (float) Math.floor(viewOffset.y/interval)*interval;


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
                    storedy += interval;
                    screeny = mScribbleView.storedYtoScreen(storedy);
                }
                storedx += interval;
                screenx = mScribbleView.storedXtoScreen(storedx);
            }

        } else {
            // draw lines
            float storedy = gridStarty;
            float screeny = mScribbleView.storedYtoScreen(storedy);
            while (screeny < bottomEdge) {
                c.drawLine(0f,screeny,rightEdge,screeny, mPaint);
                storedy += interval;
                screeny = mScribbleView.storedYtoScreen(storedy);
            }
        }
    }
}
