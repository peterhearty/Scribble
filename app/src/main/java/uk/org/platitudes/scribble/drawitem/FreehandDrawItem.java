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
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

public class FreehandDrawItem implements DrawItem {

    private ArrayList<PointF> mPoints;
    private Paint mPpaint;

    public FreehandDrawItem (MotionEvent event, ScribbleView scribbleView) {
        mPoints = new ArrayList<>();
        createPaint();
        handleMoveEvent(event, scribbleView);
    }

    private void createPaint () {
        mPpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPpaint.setColor(Color.BLACK);
        mPpaint.setStrokeWidth(5f);
    }



    @Override
    public void draw(Canvas c, ScribbleView scribbleView) {
        for (int i=0; i<mPoints.size()-1; i++) {
            PointF startPoint = mPoints.get(i);
            PointF endPoint = mPoints.get(i+1);
            float startX = scribbleView.storedXtoScreen(startPoint.x);
            float startY = scribbleView.storedYtoScreen(startPoint.y);
            float endX   = scribbleView.storedXtoScreen(endPoint.x);
            float endY   = scribbleView.storedYtoScreen(endPoint.y);
            c.drawLine(startX, startY, endX, endY, mPpaint);
        }
    }

    private void addPoint (float x, float y, ScribbleView scribbleView) {
        float storedX = scribbleView.screenXtoStored(x);
        float storedY = scribbleView.screenYtoStored(y);
        PointF p = new PointF (storedX, storedY);
        mPoints.add(p);
    }

    @Override
    public void handleMoveEvent(MotionEvent event, ScribbleView scribbleView) {
        final int historySize = event.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            addPoint(event.getHistoricalX(h), event.getHistoricalY(h), scribbleView);
        }
        addPoint(event.getX(), event.getY(), scribbleView);

    }

    public void handleUpEvent (MotionEvent event, ScribbleView scribbleView) {
        scribbleView.addItem(this);
    }

    /**
     * Constructor to read data from file.
     */
    public FreehandDrawItem (DataInputStream dis, int version) {
        createPaint();
        try {
            readFromFile(dis, version);
        } catch (IOException e) {
            ScribbleMainActivity.makeToast("Error reading FreehandDrawItem " + e);
        }
    }

    public void saveToFile (DataOutputStream dos, int version) throws IOException {
        dos.writeByte(FREEHAND);
        dos.writeInt(mPoints.size());
        for (int i=0; i<mPoints.size(); i++) {
            PointF p = mPoints.get(i);
            dos.writeFloat(p.x);
            dos.writeFloat(p.y);
        }
    }

    public DrawItem readFromFile (DataInputStream dis, int version) throws IOException {
        int numPoints = dis.readInt();
        mPoints = new ArrayList<>(numPoints);
        for (int i=0; i<numPoints; i++) {
            PointF p = new PointF();
            p.x = dis.readFloat();
            p.y = dis.readFloat();
            mPoints.add(p);
        }
        return this;
    }

}
