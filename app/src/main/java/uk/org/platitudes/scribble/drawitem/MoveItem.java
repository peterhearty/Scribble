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
    private boolean mMoveInProgress;
    private boolean mDeleteItem;

    private static Bitmap sTrashCan;
    private static Bitmap sOrangeTrashCan;

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
            if (mDeleteItem) {
                mSelectedItem.deleted = true;
            }
        }
        mScribbleView.addItem(this);
        mMoveInProgress = false;
    }

    public void undo () {
        if (mSelectedItem != null) {
            if (mSelectedItem.deleted) {
                mSelectedItem.deleted = false;
            }
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
        if (mSelectedItem == null) {
            // An empty MoveItem or one that has become detached in error.
            // write as a default item, it will be discarded when next read in.
            super.saveToFile(dos, version);
            return;
        }

        dos.writeByte(MOVE);
        dos.writeFloat(mStartPoint.x);
        dos.writeFloat(mStartPoint.y);
        dos.writeFloat(mCurrentPosition.x);
        dos.writeFloat(mCurrentPosition.y);
        int hashTag = mSelectedItem.getHashTag();
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
