/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

/**
 * Used to group together a collection of DrawItems
 */
public class GroupItem extends DrawItem {

    private PointF mStartPoint;
    private PointF mCurrentPosition;
    private ItemList mSelectedItems;


    public GroupItem(MotionEvent event, ScribbleView scribbleView) {
        super(event, scribbleView);
        addPoint(event.getX(), event.getY(), scribbleView);
    }


    private void addPoint (float x, float y, ScribbleView scribbleView) {
        float storedX = scribbleView.screenXtoStored(x);
        float storedY = scribbleView.screenYtoStored(y);
        if (mStartPoint == null) {
            mStartPoint = new PointF (storedX, storedY);
        } else {
            mCurrentPosition = new PointF(storedX, storedY);
        }
    }

    @Override
    public void handleMoveEvent(MotionEvent event) {
        addPoint (event.getX(), event.getY(), mScribbleView);
    }

    @Override
    public void handleUpEvent(MotionEvent event) {
        // Rearrange so that start is top left and end is bottom right
        // just makes comparisons easier.
        float minX = Math.min (mStartPoint.x, mCurrentPosition.x);
        float maxX = Math.max(mStartPoint.x, mCurrentPosition.x);
        float minY = Math.min (mStartPoint.y, mCurrentPosition.y);
        float maxY = Math.max(mStartPoint.y, mCurrentPosition.y);
        mStartPoint.x = minX;
        mStartPoint.y = minY;
        mCurrentPosition.x = maxX;
        mCurrentPosition.y = maxY;

        // Get the selected items
        ItemList drawItems = mScribbleView.getmDrawItems();
        mSelectedItems = drawItems.findSelectedItems(mStartPoint, mCurrentPosition);
        drawItems.removeItems(mSelectedItems);
        mScribbleView.addItem(this);
    }

    @Override
    public void draw(Canvas c) {
        if (mSelectedItems == null) {
            // Selection box still being drawn by user.
            if (mStartPoint == null || mCurrentPosition == null) return;
            float startX = mScribbleView.storedXtoScreen(mStartPoint.x);
            float startY = mScribbleView.storedYtoScreen(mStartPoint.y);
            float endX   = mScribbleView.storedXtoScreen(mCurrentPosition.x);
            float endY   = mScribbleView.storedYtoScreen(mCurrentPosition.y);
            c.drawLine(startX, startY, startX, endY, mPaint);
            c.drawLine(startX, endY, endX, endY, mPaint);
            c.drawLine(endX, endY, endX, startY, mPaint);
            c.drawLine(endX, startY, startX, startY, mPaint);
        } else {
            mSelectedItems.onDraw(c);
        }
    }

    public void move(float deltaX, float deltaY) {
        if (mSelectedItems == null) return;

        mSelectedItems.move(deltaX, deltaY);
    }

    @Override
    public boolean selectItem(PointF p) {
        mSelected = false;
        DrawItem selectedItem = mSelectedItems.findFirstSelectedItem(p);
        if (selectedItem != null) {
            mSelected = true;
            mSelectedItems.selectedAll();
        }
        return mSelected;
    }

    // TODO groups of groups not working

    public void deselectItem () {
        super.deselectItem();
        if (mSelectedItems == null) return;

        mSelectedItems.deSelectedAll();
    }


    public void saveToFile (ScribbleOutputStream dos, int version) throws IOException {
        // ignore empty groups
        if (mSelectedItems == null) return;

        dos.writeByte(GROUP);
        mSelectedItems.write(dos, version);
    }

    /**
     * Constructor to read data from file.
     */
    public GroupItem (ScribbleInputStream dis, int version, ScribbleView sv) {
        super(null, sv);
        try {
            readFromFile(dis, version);
        } catch (IOException e) {
            ScribbleMainActivity.log("GroupItem", "", e);
        }
    }

    public DrawItem readFromFile (ScribbleInputStream dis, int version) throws IOException {
        mSelectedItems = new ItemList(dis, version, mScribbleView);
        return this;
    }

    public void undo () {
        if (mSelectedItems != null) {
            mScribbleView.getmDrawItems().moveFromList(mSelectedItems);
            // At the moment, undoing a group is irreversible
            mSelectedItems = null;
            ScribbleMainActivity.makeToast("Group undone");
        }
    }

    public RectF getBounds () {
        if (mSelectedItems != null) {
            RectF result = mSelectedItems.getBounds();
            return result;
        }
        return null;
    }



}
