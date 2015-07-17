/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;

import uk.org.platitudes.scribble.ScribbleView;

public interface DrawItem {

    void draw (Canvas c, ScribbleView scribbleView);
    void handleMoveEvent (MotionEvent event, ScribbleView scribbleView);

    /**
     * Note that the event passed might not actuallly be an UP event. ScribbleView calls this
     * on a new DOWN event when an item is still being created.
     */
    void handleUpEvent (MotionEvent event, ScribbleView scribbleView);

}
