/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;

/**
 * This isn't a real DrawItem. It pretends to be a draw item while the user is scrolling
 * the drawing area.
 */
public class ScrollItem extends DrawItem {

    private float mStartX, mStartY;
    private float mStartOffsetX, mStartOffsetY;
    private float mstartZoom;
    private float mStartYdiff;

    public ScrollItem (MotionEvent event, ScribbleView scribbleView) {
        super (event, scribbleView);
        mStartX = event.getX();
        mStartY = event.getY();
        PointF scrollOffset = scribbleView.getmScrollOffset();
        mStartOffsetX = scrollOffset.x;
        mStartOffsetY = scrollOffset.y;

        mstartZoom = ZoomButtonHandler.getsZoom();
        if (event.getPointerCount() == 2) {
            mStartYdiff = Math.abs(event.getY(0) - event.getY(1));
        }
    }


    @Override
    public void handleMoveEvent(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float curYdiff = Math.abs(event.getY(0) - event.getY(1));
            int viewHeight = mScribbleView.getHeight();
            if (Math.abs(curYdiff-mStartYdiff) > viewHeight/30) {
                float newZoom = curYdiff / mStartYdiff * mstartZoom;
                ScribbleMainActivity.mainActivity.getmZoomButtonHandler().setsZoom(newZoom);
            }
        }

        // If the DOWN event happened at 40,40 and the MOVE
        // happens at 100,100 then the distance moved is 60,60.
        float deltaX = event.getX()-mStartX;
        float deltaY = event.getY()-mStartY;

        // If the zoom is 2.0 then the real distance moved is only 30,30
        float zoom = ZoomButtonHandler.getsZoom();
        deltaX /= zoom;
        deltaY /= zoom;

        float newScrollXoffset = mStartOffsetX - deltaX;
        float newScrollYoffset = mStartOffsetY - deltaY;
        mScribbleView.setmScrollOffset(newScrollXoffset, newScrollYoffset);
    }

}
