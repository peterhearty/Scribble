/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.GridButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.ItemList;
import uk.org.platitudes.scribble.drawitem.MoveItem;
import uk.org.platitudes.scribble.drawitem.ResizeItem;
import uk.org.platitudes.scribble.drawitem.ScrollItem;

/**
 * Provides the main drawing view.
 */
public class ScribbleView extends View {

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
     * The items to draw
     */
    private Drawing drawing;

    /**
     * The main activity.
     */
    private ScribbleMainActivity mMainActivity;

    /**
     * Set when an existing item is selected.
     */
    private DrawItem mSelectedItem;

    /**
     * Set true when it's useful to have borders visible, e.g. when a group is being created.
     */
    public boolean drawAllBorders;

    public ScribbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ScribbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }


    private void setup () {
        drawing = new Drawing(this);
        drawing.openCurrentFile();
        mScrollOffset = new PointF(0f, 0f);
    }

    public void undo () {
        drawing.undo();
        invalidate();
    }

    public void redo () {
        drawing.redo();
        invalidate();
    }

    public void addItem (DrawItem item) {
        drawing.addItem(item);
    }

    public void clear () {
        drawing.clear(); // will save any changes before clearing
        drawing.useDefaultFile();
        drawing.write();;
        invalidate();
    }

    // NOTE, we have to store the down coords as floats. Don't store the MotionEvent
    // object as these seem to get reused.
    private PointF downLocation;

    private boolean nearLastDownEvent (MotionEvent e) {
        if (downLocation == null) {
            return false;
        }
        if (Math.abs(e.getX()-downLocation.x) < 2 && Math.abs(e.getY()-downLocation.y) < 2 ) {
            return true;
        } else {
            // there has been some motion
//            lastDownEvent = null;
        }
        return false;
    }

    private boolean longClick (MotionEvent e) {
        if (nearLastDownEvent(e)) {
            if (e.getEventTime() - e.getDownTime() > 1000) {
                return true;
            }
        }
        return false;
    }

    /**
     * There are two ways of getting here.
     *
     * 1. A DOWN, followed by nothing for a while.
     * 2. A DOWN followed by a MOVE that's very close by.
     *
     */
    private void handleLongClick () {
        if (downLocation == null) return;

        if (mSelectedItem != null) {
            mSelectedItem.deselectItem();
            mSelectedItem = null;
        } else {
            PointF coords = pointToStoredCoordinates(downLocation);
            mSelectedItem = getmDrawItems().findFirstSelectedItem(coords);
            if (mSelectedItem != null) {
                // discard any drawitem being built and replace with move
                mCurrentItem = new MoveItem(downLocation.x, downLocation.y, mSelectedItem, this);
//                        mCurrentItem = null;
                // selected item means click has been handled
            }
        }
        invalidate();
        downLocation = null;
    }

    /**
     * This gets added to the View message queue with a delay when a DOWN event is detected.
     * It gets removed if any other event is received.
     */
    private Runnable longClickDetector = new Runnable() {
        @Override
        public void run() {
            handleLongClick();
        }
    };

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);

        // remove any long click callback added by a DOWN event
        removeCallbacks(longClickDetector);

        if (mSelectedItem != null) {
            // check for changes in its status
            if (!mSelectedItem.isSelected() || mSelectedItem.deleted) {
                mSelectedItem = null;
            }
        }

        int pointerCount = event.getPointerCount();
        if (pointerCount == 2) {
            if (mSelectedItem != null) {
                // resize applies to selected item only
                if (mCurrentItem != null && !(mCurrentItem instanceof ResizeItem)) {
                    mCurrentItem = new ResizeItem(event, mSelectedItem, this);
                }
            } else {
                // resize applies to whole view
                if (mCurrentItem != null && !(mCurrentItem instanceof ScrollItem)) {
                    mCurrentItem = new ScrollItem(event, this);
                    return true;
                }
            }
        }

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                downLocation = new PointF(event.getX(), event.getY());
                postDelayed(longClickDetector, 1000);

                if (mCurrentItem != null) {
                    // no UP event received - pretend we got one
                    mCurrentItem.handleUpEvent(event);
                    mCurrentItem = null;
                }

                if (mSelectedItem != null) {
                    mCurrentItem = new MoveItem(event.getX(), event.getY(), mSelectedItem, this);
                    break;
                }

                if (mMainActivity != null) {
                    DrawToolButtonHandler dth = mMainActivity.getmDrawToolButtonHandler();
                    // The draw tool button knows what type of draw tool is currently selected
                    mCurrentItem = dth.generateDrawItem(event, this);
                }
                break;
            case (MotionEvent.ACTION_MOVE) :
                if (longClick(event)) {
                    handleLongClick();
                    break;
                }

                if (mCurrentItem != null) {
                    mCurrentItem.handleMoveEvent(event);
                }
                break;
            case (MotionEvent.ACTION_UP) :
//                drawAllBorders = false;
                if (mCurrentItem != null) {
                    mCurrentItem.handleUpEvent(event);
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

    public PointF pointToStoredCoordinates (PointF point) {
        PointF result = new PointF();
        result.x = screenXtoStored(point.x);
        result.y = screenYtoStored(point.y);
        return result;
    }

    protected void onDraw(Canvas canvas) {
        GridButtonHandler gbh = GridButtonHandler.gridButtonHandler;
        if (gbh != null) {
            gbh.onDraw(canvas);
        }

        drawing.getmDrawItems().onDraw(canvas);
        if (mCurrentItem != null) {
            mCurrentItem.draw(canvas);
        }
    }

    public ItemList getmDrawItems() {return drawing.getmDrawItems();}
    public ItemList getUndoItems() {return drawing.getUndoItems();}
    public PointF getmScrollOffset() {return mScrollOffset;}
    public void setmScrollOffset(PointF mScrollOffset) {this.mScrollOffset = mScrollOffset;}
    public void setmScrollOffset(float x,float y) {
        mScrollOffset.x = x;
        mScrollOffset.y = y;
    }
    public ScribbleMainActivity getmMainActivity() {return mMainActivity;}
    public void setmMainActivity(ScribbleMainActivity mMainActivity) {this.mMainActivity = mMainActivity;}
    public Drawing getDrawing() {return drawing;}

}
