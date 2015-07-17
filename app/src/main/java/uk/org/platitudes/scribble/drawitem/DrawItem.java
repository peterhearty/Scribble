/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleView;

public interface DrawItem {

    public static final byte LINE = 100;
    public static final byte FREEHAND = 101;
    public static final byte TEXT = 102;


    void draw (Canvas c, ScribbleView scribbleView);
    void handleMoveEvent (MotionEvent event, ScribbleView scribbleView);

    /**
     * Note that the event passed might not actuallly be an UP event. ScribbleView calls this
     * on a new DOWN event when an item is still being created.
     */
    void handleUpEvent (MotionEvent event, ScribbleView scribbleView);

    void saveToFile (DataOutputStream dos, int version) throws IOException;

    DrawItem readFromFile (DataInputStream dis, int version) throws IOException;


}
