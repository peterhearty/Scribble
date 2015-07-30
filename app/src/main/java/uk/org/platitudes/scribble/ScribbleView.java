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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.ItemList;
import uk.org.platitudes.scribble.drawitem.ScrollItem;
import uk.org.platitudes.scribble.io.FileScribbleWriter;

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

    private ScribbleMainActivity mMainActivity;


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
        mScrollOffset = new PointF(0f, 0f);
    }

    public void saveScrollOffset (DataOutputStream dos, int version) throws IOException {
        dos.writeFloat(mScrollOffset.x);
        dos.writeFloat(mScrollOffset.y);
    }

    public void readScrollOffset (DataInputStream dis, int version) throws IOException {
        mScrollOffset = new PointF();
        mScrollOffset.x = dis.readFloat();
        mScrollOffset.y = dis.readFloat();
    }

    private void writeToCurrentFile() {
        FileScribbleWriter fsw = new FileScribbleWriter(mMainActivity, mMainActivity.getmCurrentlyOpenFile());
        fsw.write();
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
        drawing.clear();
        mMainActivity.useDefaultFile();
        writeToCurrentFile();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);

        int pointerCount = event.getPointerCount();
        if (pointerCount == 2 && mCurrentItem != null && !(mCurrentItem instanceof ScrollItem)) {
//            mCurrentItem.handleUpEvent(event);
            mCurrentItem = new ScrollItem(event, this);
            return true;
        }

//        Log.d("onTouchEvent", event.toString());
        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                if (mCurrentItem != null) {
                    // no UP event received - pretend we got one
                    mCurrentItem.handleUpEvent(event);
                    mCurrentItem = null;
                }
                if (mMainActivity != null) {
                    DrawToolButtonHandler dth = mMainActivity.getmDrawToolButtonHandler();
                    // The draw tool button knows what type of draw tool is currently selected
                    mCurrentItem = dth.generateDrawItem(event, this);
                }
                break;
            case (MotionEvent.ACTION_MOVE) :
                if (mCurrentItem != null) {
                    mCurrentItem.handleMoveEvent(event);
                }
                break;
            case (MotionEvent.ACTION_UP) :
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

    protected void onDraw(Canvas canvas) {
        drawing.getmDrawItems().onDraw(canvas);
        if (mCurrentItem != null) {
            mCurrentItem.draw(canvas);
        }
    }

    public ItemList getmDrawItems() {return drawing.getmDrawItems();}
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
