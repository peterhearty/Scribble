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

import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;

/**
 * This isn't a real DrawItem. It pretends to be a draw item while the user is scrolling
 * the drawing area.
 */
public class ScrollItem implements DrawItem {

    private float mStartX, mStartY;
    private float mStartOffsetX, mStartOffsetY;
    private ScribbleView mScribbleView;

    public ScrollItem (MotionEvent event, ScribbleView scribbleView) {
        mStartX = event.getX();
        mStartY = event.getY();
        PointF scrollOffset = scribbleView.getmScrollOffset();
        mStartOffsetX = scrollOffset.x;
        mStartOffsetY = scrollOffset.y;
        mScribbleView = scribbleView;
    }

    /**
     * Do nothing methods.
     */
    public void draw(Canvas c, ScribbleView scribbleView) {}
    public void handleUpEvent(MotionEvent event, ScribbleView scribbleView) {}
    public void saveToFile (DataOutputStream dos, int version) throws IOException {}
    public DrawItem readFromFile (DataInputStream dis, int version) throws IOException {return this;}


    @Override
    public void handleMoveEvent(MotionEvent event, ScribbleView scribbleView) {
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
