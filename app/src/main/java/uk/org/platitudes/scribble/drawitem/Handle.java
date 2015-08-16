/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import uk.org.platitudes.scribble.ScribbleView;

/**
 * A handle is a small square added to a DrawItem when it has been selected.
 * Users click on these to edit DrawItems.
 */
public class Handle {

    /**
     * The handle position in stored (not screen) coordinates. This is supplied as
     * a PointF object and stored unaltered. If this is a direct reference to a
     * DrawItem point then it can be updated directly by
     *
     * nearPoint (MotionEvent event, boolean update)
     */
    private PointF mPosition;   // in stored, not screen, coordinates

    /**
     * Need this to translate between screen and stored coords.
     */
    private ScribbleView mScribbleView;

    /**
     * Same handle size is used for all handles.
     */
    private static float sHalfSize;

    /**
     * Same Paint object is used for all handles.
     */
    private static Paint sPaint;


    public Handle (PointF position, ScribbleView scribbleView) {
        mPosition = position;
        mScribbleView = scribbleView;

        if (sPaint == null) {
            // Setup handle drawing info for all handles.
            sPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sPaint.setColor(Color.BLACK);
            sPaint.setStrokeWidth(5f);

            // Set up a reasonable size for the square taking size of View into account.
            int windowHeight = mScribbleView.getHeight();

            sHalfSize = windowHeight/100;
            if (sHalfSize < 5)
                sHalfSize = 5;
        }
    }

    /**
     * Draw s small square at a given point.
     */
    public void drawSelectionHandle (Canvas c) {
        float x = mScribbleView.storedXtoScreen(mPosition.x);
        float y = mScribbleView.storedYtoScreen(mPosition.y);

        c.drawLine(x - sHalfSize, y - sHalfSize, x - sHalfSize, y + sHalfSize, sPaint);
        c.drawLine(x - sHalfSize, y + sHalfSize, x + sHalfSize, y + sHalfSize, sPaint);
        c.drawLine(x+sHalfSize, y+sHalfSize, x+sHalfSize, y-sHalfSize, sPaint);
        c.drawLine(x+sHalfSize, y-sHalfSize, x-sHalfSize, y-sHalfSize, sPaint);
    }

    public boolean nearPoint (float x, float y) {
        float testSize = sHalfSize*2;
        if (Math.abs(x-mPosition.x)<testSize && Math.abs(y-mPosition.y)<testSize )
            return true;
        return false;
    }

    /**
     * Test to see if an event takes place near this handle. Optionally updates the
     * handle to set itself to the event location. This is used by LineDrawItem to
     * handle move events on handles when it has been selected.
     */
    public boolean nearPoint (float screenx, float screeny, boolean update) {
        float x = mScribbleView.screenXtoStored(screenx);
        float y = mScribbleView.screenYtoStored(screeny);
        boolean result = nearPoint(x,y);
        if (result && update) {
            mPosition.x = x;
            mPosition.y = y;
        }
        return result;
    }

    public void setPosition (float x, float y) {
        mPosition.x = x;
        mPosition.y = y;
    }

}
