/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.buttonhandler.GridButtonHandler;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

/**
 * Used to track selected items.
 */
public class MoveItem extends DrawItem {


    /**
     * The position of the selected item when the move starts. An UNDO operation will move it back to here.
     */
    private PointF mStartPoint;

    /**
     * Used while the move is being constructed to provide intermediate small moves. Note stored
     * permanently and not used in UNDO or REDO operations.
     */
    private PointF mPreviousPosition;

    /**
     * The final position when the move completes. A REDO operation will also move the selected
     * item to here.
     */
    private PointF mCurrentPosition;

    /**
     * The item being moved.
     */
    private DrawItem mSelectedItem;

    /**
     * Set when a MOVE event is first received, cleared when an UP is received. Used to display the
     * waste bin when the selected item begins to move. Note that this is not set when the selected
     * item handles its own move events (e.g. when it tracks an edit handle).
     */
    private boolean mMoveInProgress;

    /**
     * Gets set if the selected item moves over the waste bin. If it is still set when the UP
     * event is received then the selected item is marked as deleted. Deleted items can be
     * undeleted via an UNDO operation.
     */
    private boolean mDeleteItem;

    /**
     * Set if the selected item is handling the move itself. We need to know this so that
     * UNDO events can also be passed to the selected item. This object still records the start
     * and end positions for UNDO/REDO events.
     */
    private Handle mSelectionHandle;

    /**
     * Icons for the waste bin.
     */
    private static Bitmap sTrashCan;
    private static Bitmap sOrangeTrashCan;

    /**
     * Used when the MoveItem is read from storage. The hastag is the value provided by
     * mSelectedItem when MoveItem is saved. When it has been reread, it is held until the
     * ScribbleView can match each MoveItem to a target DrawItem.
     */
    private int mHashTag;

    public MoveItem(float startX, float startY, DrawItem selected, ScribbleView scribbleView) {
        super (null, scribbleView);
        addPoint(startX, startY);
        mSelectedItem = selected;
    }

    private void addPoint (float x, float y) {
        float storedX = mScribbleView.screenXtoStored(x);
        float storedY = mScribbleView.screenYtoStored(y);
        if (mStartPoint == null) {
            mStartPoint = mPreviousPosition = new PointF (storedX, storedY);
        } else {
            mCurrentPosition = new PointF(storedX, storedY);
        }
    }

    @Override
    public void draw(Canvas c) {
        // Used to draw a trashcan as the target for deleting DrawItems
        if (sTrashCan == null) {
            sTrashCan = BitmapFactory.decodeResource(mScribbleView.getContext().getResources(), R.drawable.waste);
            sOrangeTrashCan = BitmapFactory.decodeResource(mScribbleView.getContext().getResources(), R.drawable.wasteorange);
        }
        if (mMoveInProgress) {
            float yPosn = mScribbleView.getHeight()/ 2;
            float screenX = mScribbleView.storedXtoScreen(mCurrentPosition.x);
            float screenY = mScribbleView.storedYtoScreen(mCurrentPosition.y);
            int border=mScribbleView.getmMainActivity().mDisplaySize.x/20;
            if (screenX < sTrashCan.getWidth()+border &&
                    screenY > yPosn-border  &&
                    screenY < yPosn+sTrashCan.getHeight()+border) {
                c.drawBitmap(sOrangeTrashCan, 20, yPosn, mPaint);
                mDeleteItem = true;
            } else {
                c.drawBitmap(sTrashCan, 20, yPosn, mPaint);
                mDeleteItem = false;
            }
        }
    }

    @Override
    public void handleMoveEvent(MotionEvent event) {
        if (mSelectedItem != null) {
            mMoveInProgress = true;

            addPoint (event.getX(), event.getY());
            float deltaX = mCurrentPosition.x - mPreviousPosition.x;
            float deltaY = mCurrentPosition.y - mPreviousPosition.y;
            mPreviousPosition = mCurrentPosition;

            // Give the selected item an opportunity to handle the move
            // e.g. by movin one of its handles
            mSelectionHandle = mSelectedItem.handleEditEvent(mStart, event.getX(), event.getY());
            if (mSelectionHandle == null) {
                // item did not handle the move, so we move the whole item
                mSelectedItem.move(deltaX, deltaY);
            }
        }
    }

    @Override
    public void handleUpEvent(MotionEvent event) {
        if (mCurrentPosition == null) {
            addPoint(event.getX(), event.getY());
        }
        mScribbleView.addItem(this);
        mMoveInProgress = false;

        if (mSelectedItem != null) {
            if (mDeleteItem) {
                // item was moved to the waste bin
                mSelectedItem.deleted = true;
                mSelectedItem.deselectItem();
            }
            if (mSelectionHandle != null) {
                // This object did not move the selected item.
                // The selected item did it itself. We now check for
                // presence of a grid to snap to.
                if (GridButtonHandler.sGridStatus == GridButtonHandler.GRID_ON) {
                    // snap to nearest grid point
                    PointF nearestGridPoint = GridButtonHandler.nearestGridPoint(mCurrentPosition.x, mCurrentPosition.y);
                    mSelectionHandle.setPosition(nearestGridPoint.x, nearestGridPoint.y);
                    mCurrentPosition = nearestGridPoint;
                }

            }
        }
    }

    public void undo () {
        if (mSelectedItem != null) {
            if (mSelectedItem.deleted) {
                mSelectedItem.deleted = false;
            }
            if (mSelectionHandle != null) {
                // selected item handled it itself
                mSelectionHandle.setPosition(mStartPoint.x, mStartPoint.y);
            } else {
                // move whole object back
                float deltaX = mStartPoint.x - mCurrentPosition.x;
                float deltaY = mStartPoint.y - mCurrentPosition.y;
                mSelectedItem.move(deltaX, deltaY);
            }
        }
    }

    public void redo () {
        if (mSelectedItem != null) {
            if (mSelectionHandle != null) {
                // move a selection handle
                mSelectionHandle.setPosition(mCurrentPosition.x, mCurrentPosition.y);
            } else {
                // move whole object
                float deltaX = mCurrentPosition.x - mStartPoint.x;
                float deltaY = mCurrentPosition.y - mStartPoint.y;
                mSelectedItem.move(deltaX, deltaY);
            }
        }
    }

    /**
     * Uses a hashtag to identify the target item.
     */
    public void saveToFile (ScribbleOutputStream dos, int version) throws IOException {
        if (mSelectedItem == null) {
            // An empty MoveItem or one that has become detached in error.
            // write as a default item, it will be discarded when next read in.
            super.saveToFile(dos, version);
            return;
        }

        int hashTag = mSelectedItem.getHashTag();
        if (mStartPoint == null || mCurrentPosition == null || hashTag == 0) {
            ScribbleMainActivity.log("Cannot save Move", "", null);
            super.saveToFile(dos, version);
            return;
        }

        dos.writeByte(MOVE);
        dos.writeFloat(mStartPoint.x);
        dos.writeFloat(mStartPoint.y);
        dos.writeFloat(mCurrentPosition.x);
        dos.writeFloat(mCurrentPosition.y);
        dos.writeInt(hashTag);
    }

    /**
     * Constructor used when reading in from storage. Uses readFromFile below to do most
     * of the work.
     */
    public MoveItem (ScribbleInputStream dis, int version, ScribbleView sv) throws IOException {
        super(null, sv);
        readFromFile(dis, version);
    }

    public DrawItem readFromFile (ScribbleInputStream dis, int version) throws IOException {
        mStartPoint = new PointF();
        mStartPoint.x = dis.readFloat();
        mStartPoint.y = dis.readFloat();
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
