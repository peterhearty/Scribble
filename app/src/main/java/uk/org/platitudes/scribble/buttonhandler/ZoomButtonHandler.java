package uk.org.platitudes.scribble.buttonhandler;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.ItemList;

public class ZoomButtonHandler implements View.OnClickListener, View.OnLongClickListener {

    private static float sZoom = 1.0f;
    private ScribbleView mScribbleView;
    private Button mButton;

    // 0 = zoom 1, offset 0, 1 = prev state, 2 == full view
    private int zoomState;

    // Saved values for quick restore
    private float savedZoom;
    private PointF savedOffset;

    public ZoomButtonHandler (ScribbleView v, Button b) {
        mScribbleView = v;
        mButton = b;
        mButton.setOnLongClickListener(this);
        zoomState = 0;
    }

    private void setButtonText () {
        String zoomSize = Float.toString(sZoom);
        if (zoomSize.length() > 4) {
            zoomSize = zoomSize.substring(0,4);
        }
//        String newButtonText = zoomSize;
        mButton.setText(zoomSize);
    }

    @Override
    public void onClick(View v) {
        sZoom *= 2.0f;
        if (sZoom >= 4.05f)
            sZoom = 0.25f;
        setButtonText();
        mScribbleView.invalidate();
    }

    public static float getsZoom() {return sZoom;}

    public void setsZoom(float sZoom) {
        ZoomButtonHandler.sZoom = sZoom;
        setButtonText ();
        // set to custom zoom state so zoom gets saved on next long click
        zoomState=1;
    }

    @Override
    public boolean onLongClick(View v) {
        zoomState++;
        if (zoomState==3)
            zoomState = 0;
        if (zoomState == 1 && savedOffset == null)
            zoomState = 2;

        switch (zoomState) {
            case 0:
                // switch to initial layout
                sZoom = 1.0f;
                mScribbleView.setmScrollOffset(0f, 0f);
                break;
            case 1:
                // return to previous layout
                sZoom = savedZoom;
                mScribbleView.setmScrollOffset(savedOffset.x, savedOffset.y);
                savedOffset = null;
                break;
            case 2:
                // save current layout
                savedZoom = sZoom;
                savedOffset = new PointF();
                savedOffset.x = mScribbleView.getmScrollOffset().x;
                savedOffset.y = mScribbleView.getmScrollOffset().y;

                // show everything on screen - first get bounds of all items
                ItemList drawItems = mScribbleView.getmDrawItems();
                RectF bounds = drawItems.getBounds();
                if (bounds == null) {
                    // nothing to show - goto zoomState 0
                    onLongClick (v);
                    return true;
                }

                // leave a short border around them
                Point displaySize = ScribbleMainActivity.mainActivity.mDisplaySize;
                int border = Math.min(displaySize.x, displaySize.y)/10;
                bounds.left -= border;
                bounds.top -= border;
                bounds.right += border;
                bounds.bottom += border;

                mScribbleView.setmScrollOffset(bounds.left, bounds.top);
                float width = bounds.right - bounds.left;
                float height = bounds.bottom - bounds.top;

                sZoom = 1.0f;
                if (displaySize.x < width) {
                    sZoom = displaySize.x / width;
                }
                if (displaySize.y < height) {
                    float yZoom = displaySize.y / height;
                    if (yZoom < sZoom) {
                        sZoom = yZoom;
                    }
                }

                break;

        }

        setButtonText();
        mScribbleView.invalidate();
        return true;
    }
}
