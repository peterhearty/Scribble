/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

/**
 * Used to track selected items.
 */
public class MoveItem extends DrawItem {


    private PointF mStartPoint;
    private PointF mPreviousPosition;
    private PointF mCurrentPosition;
    private DrawItem mSelectedItem;

    /**
     * Used when the MoveItem is read from storage. The hastag is the value provided by
     * mSelectedItem when MoveItem is saved. When it has been reread, it is held until the
     * ScribbleView can match each MoveItem to a target DrawItem.
     */
    private int mHashTag;



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

    /**
     * Uses a hashtag to identify the target item.
     */
    public void saveToFile (ScribbleOutputStream dos, int version) throws IOException {
        dos.writeByte(MOVE);
        dos.writeFloat(mStartPoint.x);
        dos.writeFloat(mStartPoint.y);
        dos.writeFloat(mCurrentPosition.x);
        dos.writeFloat(mCurrentPosition.x);
        int hashTag = mSelectedItem.getHashTag();
        dos.writeInt(hashTag);
    }

    public MoveItem (ScribbleInputStream dis, int version, ScribbleView sv) throws IOException {
        super(null, sv);
        readFromFile(dis, version);
    }

    public DrawItem readFromFile (ScribbleInputStream dis, int version) throws IOException {
        mStartPoint = new PointF();
        mStartPoint.x = dis.readFloat();
        mStartPoint.y = dis.readFloat();
        //TODO - not quite getting corrdinates right.
        mCurrentPosition = new PointF();
        mCurrentPosition.x = dis.readFloat();
        mCurrentPosition.y = dis.readFloat();
        mHashTag = dis.readInt();
        return this;
    }

    /**
     * Called to match up this MoveItem with a potential target
     */
    public boolean matchDrawItem (ArrayList<DrawItem> listOfDrawItems) {
        for (DrawItem d : listOfDrawItems) {
            if (d.getHashTag() == mHashTag) {
                mSelectedItem = d;
                return true;
            }
        }
        return false;
    }


}
