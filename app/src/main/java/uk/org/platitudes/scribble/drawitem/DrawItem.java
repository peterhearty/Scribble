/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

public abstract class DrawItem {

    /**
     * DrawItem types used when saving to a DataOutputStream.
     */
    public final static byte LOWEST_TYPE = 100;
    public final static byte LINE = 100;
    public final static byte FREEHAND = 101;
    public final static byte TEXT = 102;
    public final static byte COMPRESSED_FREEHAND = 103;
    public final static byte DEFAULT_ITEM = 104;
    public final static byte MOVE = 105;
    public final static byte GROUP = 106;
    public final static byte HIGHEST_TYPE = 106;

    /**
     * Used during selection to allow some imprecision in selecting DrawItems.
     * It's up to each individual DrawItem whether to use this or not.
     * Gets overriden by ScribbleMainAcitivity.getDisplaySize.
     */
    public static float FUZZY = 5.0f;

    protected Paint mPaint;
    protected ScribbleView mScribbleView;
    protected boolean mSelected;
    protected float mZoom = 1.0f;
    protected PointF mStart;
    private ArrayList<Handle> handles;

    /**
     * True if item has been moved to waste bin by a move operation. Deleted items don't go
     * on the undo list. Instead, this flag is set and ItemList just doesn't draw them. The
     * MoveItem remembers that it performed a delete and simply cancels the flag when a undo
     * gets executed. If this flag is set when ItemList.clean is called then the item is
     * deleted permanently.
     */
    public boolean deleted;

    public DrawItem (MotionEvent event, ScribbleView scribbleView) {
        mScribbleView = scribbleView;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5f);
        mSelected = false;
        mStart = new PointF();
    }


    public void draw (Canvas c) {}
    public void handleMoveEvent (MotionEvent event) {}

    /**
     * Provides a way of identifying a particular DrawItem. This enables relationships between
     * DrawItems, e.g. between a MoveItem and the item it moves, to be saved when written to file.
     * MoveItem can ask the object it moves to identify itself via its hashTag. when it is read in
     * again, it scans the list of DrawItems to fint eh one that matches the supplied hashTag.
     *
     * A combination of start point, end point and item type will usually be sufficient to identify
     * and item.
     */
    public int getHashTag() {return 0;}

    /**
     * Note that the event passed might not actuallly be an UP event. ScribbleView calls this
     * on a new DOWN event when an item is still being created.
     */
    public void handleUpEvent (MotionEvent event) {};

    /**
     * Drawing items override this method. Some DrawItems, like MoveItem, remain on the
     * draw list so that they can be undone. They do not write themselves to disk and will
     * appear as DEFAULT_ITEMs on being read. DEFAULT_ITEMs do not get added to the draw
     * list and so disappear on subsequent writes.
     */
    public void saveToFile (ScribbleOutputStream dos, int version) throws IOException {
        dos.writeByte(DEFAULT_ITEM);
    };

    public DrawItem readFromFile (ScribbleInputStream dis, int version) throws IOException {return null;};

    /**
     * Tests a DrawItem's stored coordinates to see if a supplied selection point is
     * within it's bounds. The supplied point will already be converted
     * to stored coordinates from screen coordinates. i.e. The screen offset and
     * zoom factor will already have been removed. If the DrawItem can be selected
     * by the supplied point then its status will change to selected and it may
     * change its onscreen appearance.
     *
     * @param p     The stored coordinate point to test.
     * @return      true if the DrawItem has been selected by the call.
     */
    public boolean selectItem(PointF p) {
        RectF bounds = getBounds();
        if (bounds != null) {
            if (bounds.left < p.x && p.x < bounds.right && bounds.top < p.y && p.y < bounds.bottom) {
                mSelected = true;
                addHandles();
                mPaint.setColor(Color.RED);
            }
        }
        return mSelected;
    };

    /**
     * Tests to see if a DrawItem is fully contained within the given box. The supplied
     * box is in stored coordinates, with zoom and offset removed. If the test succeeds then
     * the DrawItem is marked as selected and the method returns true.
     *
     */
    public boolean selectItem (PointF start, PointF end) {
        RectF bounds = getBounds();
        if (bounds != null) {
            if (bounds.left >= start.x && bounds.right<=end.x && bounds.top >= start.y && bounds.bottom <= end.y) {
                mSelected = true;
                addHandles();
                mPaint.setColor(Color.RED);
            }
        }
        return mSelected;
    }

    public void deselectItem () {
        mSelected = false;
        removeHandles();
        mPaint.setColor(Color.BLACK);
    }

    public void selectItem () {
        mSelected = true;
        addHandles();
        mPaint.setColor(Color.RED);
    }

    public boolean isSelected() {return mSelected;};

    public void move (float deltaX, float deltaY){};

    /**
     * Most DrawItems simply get moved from the list of DrawItems to the undo list,
     * or the other way around. Some, such as MoveItem, are not drawing items
     * themselves but must perform an operation as they move between lists.
     */
    public void undo () {}
    public void redo () {}

    /**
     * Bounds are needed to perform view resizes;
     */
    public RectF getBounds () {return null;}

    public void drawBounds (Canvas c) {
        if (mScribbleView == null) return;
        if (mSelected || mScribbleView.drawAllBorders) {

            RectF bounds = getBounds();
            if (bounds == null) return;
            float screenTop = mScribbleView.storedYtoScreen(bounds.top);
            float screenBottom = mScribbleView.storedYtoScreen(bounds.bottom);
            float screenLeft = mScribbleView.storedXtoScreen(bounds.left);
            float screenRight = mScribbleView.storedXtoScreen(bounds.right);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(Color.LTGRAY);
            p.setStrokeWidth(3f);

            c.drawLine(screenLeft, screenTop, screenRight, screenTop, p);
            c.drawLine(screenRight, screenTop, screenRight, screenBottom, p);
            c.drawLine(screenRight, screenBottom,screenLeft, screenBottom, p);
            c.drawLine(screenLeft, screenBottom, screenLeft, screenTop, p);
        }
    }


    /**
     * Subclasses should override this if they want to add handles when the item is selected.
     * They should call addHandle (PointF) for each handle they need to add.
     */
    public void addHandles () {}

    /**
     * Subclasses call this method from their own addHandles method.
     */
    public void addHandle (PointF point) {
        if (handles == null) {
            handles = new ArrayList<>();
        }
        Handle h = new Handle(point, mScribbleView);
        handles.add(h);
    }

    public void drawHandles (Canvas c) {
        if (handles == null) return;

        for (Handle h: handles) {
            h.drawSelectionHandle(c);
        }
    }

    public void removeHandles () {
        handles = null;
    }

    /**
     * Tests to see if a Motionevent is on top of one of the DrawItem's handles.
     * If it is then it returns the Handle, otherwise returns null.
     */
    public Handle nearHandle (float screenx, float screeny) {
        Handle result = null;

        if (handles != null) {
            for (Handle h: handles) {
                if (h.nearPoint(screenx, screeny, false)) {
                    result = h;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Test each handle to see if any of them are under the supplied MotionEvent.
     *
     * @return      True if one of the handles is under the MotionEvent.
     */
    public Handle updateUsingHandles (float screenx, float screeny) {
        if (handles == null) return null;

        Handle result = null;

        for (Handle h: handles) {
            if (h.nearPoint(screenx, screeny, true)) {
                result = h;
                break;
            }
        }

        return result;
    }

    /**
     * When a long click on the main view selects an item, any movement causes a
     * MoveItem to be created for the selected item. The MoveItem handles overall
     * movement of the object. Before performing a move it gives the object itself
     * a chance to handle the event. For example, a user might click on a handle
     * of a selected item in order to reshape it without moving the item's location.
     *
     * If the item handles the event then it should return true, othwerwise it
     * should return false and allow the MoveItem to consume the event instead.
     *
     * @param motionStart   The position where the DOWN event happened.
     * @param screenx         The current move event.
     *
     * @return
     */
    public Handle handleEditEvent (PointF motionStart, float screenx, float screeny) {
        return null;
    }

}
