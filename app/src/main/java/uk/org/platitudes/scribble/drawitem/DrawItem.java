/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;

import uk.org.platitudes.scribble.ScribbleView;

public interface DrawItem {

    public void draw (Canvas c, ScribbleView scribbleView);
    public void handleTouchEvent (MotionEvent event, ScribbleView scribbleView);

}
