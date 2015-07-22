/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleView;

public abstract class DrawItem {

    /**
     * DrawItem types used when saving to a DataOutputStream.
     */
    public final static byte LINE = 100;
    public final static byte FREEHAND = 101;
    public final static byte TEXT = 102;
    public final static byte COMPRESSED_FREEHAND = 103;

    /**
     * Used during selection to allow some imprecision in selecting DrawItems.
     * It's up to each individual DrawItem whether ot use this or not.
     */
    public static final float FUZZY = 5.0f;

    protected Paint mPaint;
    protected ScribbleView mScribbleView;
    protected boolean mSelected;

    public DrawItem (MotionEvent event, ScribbleView scribbleView) {
        mScribbleView = scribbleView;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5f);
        mSelected = false;
    }


    public void draw (Canvas c) {}
    public void handleMoveEvent (MotionEvent event) {}

    /**
     * Note that the event passed might not actuallly be an UP event. ScribbleView calls this
     * on a new DOWN event when an item is still being created.
     */
    public void handleUpEvent (MotionEvent event) {};

    public void saveToFile (DataOutputStream dos, int version) throws IOException {};

    public DrawItem readFromFile (DataInputStream dis, int version) throws IOException {return null;};

    /**
     * Tests a DrawItem's stored coordinates against the given point to see if
     * the item should be selected. The supplied point will already be converted
     * to stored coordinates from screen coordinates. i.e. The screen offset and
     * zoom factor will already have been removed. If the DrawItem can be selected
     * by the supplied point then its status will change to selected and it may
     * change its onscreen appearance.
     *
     * @param p     The stored coordinate point to test.
     * @return      true if the DrawItem has been selected by the call.
     */
    public boolean selectItem(PointF p) {return false;};

    public void deselectItem () {
        mSelected = false;
        mPaint.setColor(Color.BLACK);
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
}
