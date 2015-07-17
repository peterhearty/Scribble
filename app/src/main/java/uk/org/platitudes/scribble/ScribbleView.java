/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.drawitem.DrawItem;

/**
 * Provides the main drawing view.
 */
public class ScribbleView extends View {

    /**
     * The draw list, the list of DrawItems on this page.
     */
    private ArrayList<DrawItem> mDrawItems;

    /**
     * Items get moved from the draw list to here when undo is pressed.
     * They move the oppoiste way on redo.
     */
    private ArrayList<DrawItem> mUndoList;

    /**
     * The current DrawItem. Created on a DOWN event based on the current
     * draw tool selection. Moved to the draw item list when an UP, or a new
     * DOWN is received.
     */
    private DrawItem mCurrentItem;

    /**
     * Coords of all DrawItems are based on a zoom value of 1.0 (no zoom).
     * The screen displays a window on the DrawItems, starting at this
     * member variable.
     */
    private PointF mScrollOffset;

    /**
     * We need the DrawToolButtonHandler because it knows what type of
     * draw tool has ben selected.
     */
    private DrawToolButtonHandler mDrawToolButtonHandler;


    public ScribbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ScribbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }


    private void setup () {
        mDrawItems = new ArrayList<>();
        mUndoList = new ArrayList<>();
        mScrollOffset = new PointF(0f, 0f);
    }

    public void undo () {
        int numItems = mDrawItems.size();

        if (numItems==0) return;

        DrawItem last = mDrawItems.remove(numItems - 1);
        mUndoList.add(last);

        invalidate();
    }

    public void redo () {
        int numItems = mUndoList.size();

        if (numItems==0) return;

        DrawItem last = mUndoList.remove(numItems - 1);
        mDrawItems.add(last);

        invalidate();

    }

    public void addItem (DrawItem item) {
        mDrawItems.add(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);

//        Log.d("onTouchEvent", event.toString());
        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                if (mCurrentItem != null) {
                    // no UP event received - pretend we got one
                    mCurrentItem.handleUpEvent(event, this);
                    mCurrentItem = null;
                }
                if (mDrawToolButtonHandler != null) {
                    mCurrentItem = mDrawToolButtonHandler.generateDrawItem(event, this);
                }
                break;
            case (MotionEvent.ACTION_MOVE) :
                if (mCurrentItem != null) {
                    mCurrentItem.handleMoveEvent(event, this);
                }
                break;
            case (MotionEvent.ACTION_UP) :
                if (mCurrentItem != null) {
                    mCurrentItem.handleUpEvent(event, this);
                    mCurrentItem = null;
                }
                break;
            case (MotionEvent.ACTION_CANCEL) :
                mCurrentItem = null;
                break;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d("onTouchEvent", "OUTOFBOUNDS");
                break;
            default :
                return super.onTouchEvent(event);
        }

        invalidate();
        return true;

    }

    public float storedXtoScreen (float storedX) {
        float zoom = ZoomButtonHandler.getsZoom();
        float result = (storedX-mScrollOffset.x)*zoom;
        return result;
    }

    public float storedYtoScreen (float storedY) {
        float zoom = ZoomButtonHandler.getsZoom();
        float result = (storedY-mScrollOffset.y)*zoom;
        return result;
    }

    public float screenXtoStored (float screenX) {
        float zoom = ZoomButtonHandler.getsZoom();
        float result = screenX/zoom+mScrollOffset.x;
        return result;
    }

    public float screenYtoStored (float screenY) {
        float zoom = ZoomButtonHandler.getsZoom();
        float result = screenY/zoom+mScrollOffset.y;
        return result;
    }

    protected void onDraw(Canvas canvas) {
        for (DrawItem d : mDrawItems) {
            d.draw(canvas, this);
        }
        if (mCurrentItem != null) {
            mCurrentItem.draw(canvas, this);
        }
    }

    public void setmDrawToolButtonHandler(DrawToolButtonHandler dtbh) {this.mDrawToolButtonHandler = dtbh;}
    public PointF getmScrollOffset() {return mScrollOffset;}
    public void setmScrollOffset(PointF mScrollOffset) {this.mScrollOffset = mScrollOffset;}
    public void setmScrollOffset(float x,float y) {
        mScrollOffset.x = x;
        mScrollOffset.y = y;
    }



}
