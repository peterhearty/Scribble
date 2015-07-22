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

    /**
     * DrawItem types used when saving to a DataOutputStream.
     */
    static byte LINE = 100;
    static byte FREEHAND = 101;
    static byte TEXT = 102;
    static byte COMPRESSED_FREEHAND = 103;

    /**
     * Used during selection to allow some imprecision in selecting DrawItems.
     * It's up to each individual DrawItem whether ot use this or not.
     */
    static float FUZZY = 5.0f;


    void draw (Canvas c, ScribbleView scribbleView);
    void handleMoveEvent (MotionEvent event, ScribbleView scribbleView);

    /**
     * Note that the event passed might not actuallly be an UP event. ScribbleView calls this
     * on a new DOWN event when an item is still being created.
     */
    void handleUpEvent (MotionEvent event, ScribbleView scribbleView);

    void saveToFile (DataOutputStream dos, int version) throws IOException;

    DrawItem readFromFile (DataInputStream dis, int version) throws IOException;

    /**
     * Tests a DrawItem's stored coordinates against the given point to see if
     * the item should be selected/deselcted. The supplied point will already be converted
     * to stored coordinates from screen coordinates. i.e. The screen offset and
     * zoom factor will already have been removed.
     *
     * @param p     The stored coordinate point to test.
     * @return      true if the DrawItem has changed its selection status.
     */
    boolean toggleSelected(PointF p);

    boolean isSelected();

    void move (float deltaX, float deltaY);
}
