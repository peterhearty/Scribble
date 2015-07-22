/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.drawitem.DrawItem;

/**
 */
public class TextItem implements DrawItem {

    private float mStartX;
    private float mStartY;
    private String mText;
    private Rect bounds = new Rect();
    private Paint mPaint;
    private boolean selected;

    public TextItem (MotionEvent event, ScribbleView scribbleView) {

        float screenX = event.getX();
        float screenY = event.getY();
        mStartX = scribbleView.screenXtoStored(screenX);
        mStartY = scribbleView.screenYtoStored(screenY);

        createPaint();
        mText = "";
        bounds = new Rect();

        EditTextDialog dialog = new EditTextDialog();
        dialog.setTextItem(this);
        dialog.show(ScribbleMainActivity.mainActivity.getFragmentManager(), "");

        scribbleView.addItem(this);
    }

    private void createPaint () {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5f);
        mPaint.setTextSize(20f);
    }


    @Override
    public void draw(Canvas c, ScribbleView scribbleView) {
        float screenX = scribbleView.storedXtoScreen(mStartX);
        float screenY = scribbleView.storedYtoScreen(mStartY);

        float zoom = ZoomButtonHandler.getsZoom();
        mPaint.setTextSize(30 * zoom);
        c.drawText(mText, screenX, screenY, mPaint);
    }

    public void handleMoveEvent(MotionEvent event, ScribbleView scribbleView) {}
    public void handleUpEvent(MotionEvent event, ScribbleView scribbleView) {}

    @Override
    public void saveToFile(DataOutputStream dos, int version) throws IOException {
        dos.writeByte(TEXT);
        dos.writeFloat(mStartX);
        dos.writeFloat(mStartY);
        dos.writeUTF(mText);
    }

    public TextItem (DataInputStream dis, int version) throws IOException {
        createPaint();
        readFromFile(dis, version);
    }

    @Override
    public DrawItem readFromFile(DataInputStream dis, int version) throws IOException {
        mStartX = dis.readFloat();
        mStartY = dis.readFloat();
        String s = dis.readUTF();
        setmText(s);
        return null;
    }

    @Override
    public boolean toggleSelected(PointF p) {
        float minX = mStartX-FUZZY;
        float maxX = mStartX+bounds.right+FUZZY;
        float minY = mStartY-FUZZY;
        float maxY = mStartY+bounds.bottom+FUZZY;
        boolean selectChanged = false;
        if (minX < p.x && p.x < maxX && minY < p.y && p.y < maxY) {
            selectChanged = true;
            if (selected) {
                selected = false;
                mPaint.setColor(Color.BLACK);
            } else {
                selected = true;
                mPaint.setColor(Color.RED);
            }
        }
        return selectChanged;
    }

    public boolean isSelected() {return selected;}
    public String getmText() {return mText;}

    public void setmText(String mText) {
        this.mText = mText;
        mPaint.setTextSize(30f);
        mPaint.getTextBounds(mText, 0, mText.length(), bounds);
    }

    public void move(float deltaX, float deltaY) {
        mStartX += deltaX;
        mStartY += deltaY;
    }


}
