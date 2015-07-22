package uk.org.platitudes.scribble.drawitem;

import android.graphics.PointF;
import android.view.MotionEvent;

import uk.org.platitudes.scribble.ScribbleView;

/**
 * Used to track selected items.
 */
public class MoveItem extends DrawItem {


    private PointF mStartPoint;
    private PointF mPreviousPosition;
    private PointF mCurrentPosition;
    private DrawItem mSelectedItem;



    public MoveItem(MotionEvent event, ScribbleView scribbleView) {
        super (event, scribbleView);
        addPoint(event.getX(), event.getY(), scribbleView);
        ItemList drawItems = scribbleView.getmDrawItems();
        mSelectedItem = drawItems.findFirstSelectedItem(mStartPoint);
    }

    private void addPoint (float x, float y, ScribbleView scribbleView) {
        float storedX = scribbleView.screenXtoStored(x);
        float storedY = scribbleView.screenYtoStored(y);
        if (mStartPoint == null) {
            mStartPoint = mPreviousPosition = new PointF (storedX, storedY);
        } else {
            mCurrentPosition = new PointF(storedX, storedY);
        }
    }


    @Override
    public void handleMoveEvent(MotionEvent event) {
        if (mSelectedItem != null) {
            addPoint (event.getX(), event.getY(), mScribbleView);
            float deltaX = mCurrentPosition.x - mPreviousPosition.x;
            float deltaY = mCurrentPosition.y - mPreviousPosition.y;
            mPreviousPosition = mCurrentPosition;
            mSelectedItem.move(deltaX, deltaY);
        }
    }

    @Override
    public void handleUpEvent(MotionEvent event) {
        if (mSelectedItem != null) {
            mSelectedItem.deselectItem();
            mScribbleView.addItem(this);
        }
    }

    public void undo () {
        if (mSelectedItem != null) {
            float deltaX = mStartPoint.x - mCurrentPosition.x;
            float deltaY = mStartPoint.y - mCurrentPosition.y;
            mSelectedItem.move(deltaX, deltaY);
        }
    }

    public void redo () {
        if (mSelectedItem != null) {
            float deltaX = mCurrentPosition.x - mStartPoint.x;
            float deltaY = mCurrentPosition.y - mStartPoint.y;
            mSelectedItem.move(deltaX, deltaY);
        }
    }

    // MoveItem objects do not currently get saved or restored, so movements are lost.
    // We could reaquire the moved object since the moved object's position will
    // have the same position in the ItemList provided all DrawItems, including this
    // one, restore themselves correctly.
}
