/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.RectF;
import android.view.MotionEvent;

import uk.org.platitudes.scribble.ScribbleView;

/**
 *
 */
public class ResizeItem extends DrawItem {

    private DrawItem mSelectedItem;
    private float mStartYdiff;
    private RectF mInitialItemBounds;
    private float mInitialScreenTop;
    private float mInitialScreenLeft;


    public ResizeItem(MotionEvent event, DrawItem item, ScribbleView scribbleView) {
        super(event, scribbleView);
        mSelectedItem = item;

        mInitialItemBounds = mSelectedItem.getBounds();
        if (mInitialItemBounds != null) {
            mInitialScreenTop = mScribbleView.storedYtoScreen(mInitialItemBounds.top);
            mInitialScreenLeft = mScribbleView.storedXtoScreen(mInitialItemBounds.left);
        }

        if (event.getPointerCount() == 2) {
            mStartYdiff = Math.abs(event.getY(0) - event.getY(1));
        }
    }

    @Override
    public void handleMoveEvent(MotionEvent event) {

        if (event.getPointerCount() == 2) {
            if (mStartYdiff == 0) {
                mStartYdiff = Math.abs(event.getY(0) - event.getY(1));
                return;
            }
            float newYdiff = Math.abs(event.getY(0) - event.getY(1));
            float zoom = newYdiff/mStartYdiff;
            mSelectedItem.mZoom = zoom;

        }
    }


}
