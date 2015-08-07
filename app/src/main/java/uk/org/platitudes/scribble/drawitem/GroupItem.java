/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Bitmap;
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
        float storedX = mScribbleView.screenXtoStored(x);
        float storedY = mScribbleView.screenYtoStored(y);
        if (mStartPoint == null) {
            mStartPoint = new PointF (storedX, storedY);
        } else {
            mCurrentPosition = new PointF(storedX, storedY);
        }
    }

    @Override
    public int getHashTag() {
        int result = 0;
        if (mSelectedItems != null) {
            result = mSelectedItems.getHashTag();
        }
        return result;
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
            RectF bounds = mSelectedItems.getBounds();
            if (bounds == null || mZoom == 1.0f) {
                mSelectedItems.onDraw(c);
            } else {
                Bitmap b = Bitmap.createBitmap((int) bounds.width() + 1, (int) bounds.height() + 1, Bitmap.Config.ARGB_8888);
                Canvas scaleCanvas = new Canvas(b);
                mSelectedItems.onDraw(scaleCanvas);

                float newRight = bounds.left + bounds.width() * mZoom;
                float newBottom = bounds.top + bounds.height() * mZoom;
                RectF newBounds = new RectF(bounds.left, bounds.top, newRight, newBottom);
                c.drawBitmap(b, null, newBounds, mPaint);
            }
        }
    }

    public void move(float deltaX, float deltaY) {
        if (mSelectedItems == null) return;

        mSelectedItems.move(deltaX, deltaY);
    }

    public boolean isEmpty () {
        boolean result = true;
        if (mSelectedItems != null) {
            if (!mSelectedItems.isEmpty()) {
                result = false;
            }
        }
        return result;
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

    public boolean selectItem (PointF start, PointF end) {
        mSelected = false;
        if (mSelectedItems != null) {
            RectF bounds = mSelectedItems.getBounds();
            if (bounds != null) {
                if (bounds.left >= start.x && bounds.right <= end.x && bounds.top >= start.y && bounds.bottom <= end.y) {
                    mSelected = true;
                    mSelectedItems.selectedAll();
                }
            }
        }
        return mSelected;
    }

    @Override
    public void selectItem() {
        if (mSelectedItems != null) {
            mSelectedItems.selectedAll();
        }
    }

    public void deselectItem () {
        super.deselectItem();
        if (mSelectedItems == null) return;

        mSelectedItems.deSelectedAll();
    }


    public void saveToFile (ScribbleOutputStream dos, int version) throws IOException {
        // ignore empty groups
        if (mSelectedItems == null) return;

        dos.writeByte(GROUP);
        dos.writeFloat(mZoom);
        dos.writeByte(deleted ? 1:0);
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
        if (version >= 1002) {
            mZoom = dis.readFloat();
            byte deletedByte = dis.readByte();
            if (deletedByte==1) {
                deleted = true;
            }
        }
        mSelectedItems = new ItemList(dis, version, mScribbleView);
        return this;
    }

    public void undo () {
        if (mSelectedItems != null) {
            deselectItem();
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
