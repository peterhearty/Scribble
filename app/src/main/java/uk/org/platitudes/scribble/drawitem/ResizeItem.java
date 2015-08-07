/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.PointF;
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

            // the zoom moves the object so try to bring it back
//            if (mInitialItemBounds != null) {
//                // Get the stored values we'd like to have
//                float newStoredY = storedYtoScreen(mInitialScreenTop);
//                float newStoredX = screenXtoStored(mInitialScreenLeft);
//
//                // Get the stored values we currently have
//                RectF currentBounds = mSelectedItem.getBounds();
//
//                float deltaX = newStoredX - currentBounds.left;
//                float deltaY = newStoredY - currentBounds.top;
//                mSelectedItem.move(deltaX, deltaY);
//            }
        }
    }


}
