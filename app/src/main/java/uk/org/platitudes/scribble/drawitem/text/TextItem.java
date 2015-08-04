/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

/**
 */
public class TextItem extends DrawItem {

    private static final float DEFAULT_TEXT_SIZE = 200.0f;
    private float mTextSize;
    private float mStartX;
    private float mStartY;
    private String mText;
    private Rect bounds = new Rect();

    public TextItem (MotionEvent event, ScribbleView scribbleView) {
        super (event, scribbleView);

        mTextSize = DEFAULT_TEXT_SIZE;
        mPaint.setTextSize(mTextSize);

        // Tried to use a TextPaint as shown here:
        // http://stackoverflow.com/questions/6756975/draw-multi-line-text-to-canvas
        // Caused lots of strange screen effects. New Freehand DrawItems appeared in
        // wrong place. don't understand.

        float screenX = event.getX();
        float screenY = event.getY();
        mStartX = scribbleView.screenXtoStored(screenX);
        mStartY = scribbleView.screenYtoStored(screenY);

        mText = "";
        bounds = new Rect();

        EditTextDialog dialog = new EditTextDialog();
        dialog.setTextItem(this);
        dialog.show(ScribbleMainActivity.mainActivity.getFragmentManager(), "");

        scribbleView.addItem(this);
    }

    @Override
    public int getHashTag() {
        int result = (int) (TEXT*1000 + mStartX +mStartY +mText.length());
        return result;
    }



    @Override
    public void draw(Canvas c) {
        float screenX = mScribbleView.storedXtoScreen(mStartX);
        float screenY = mScribbleView.storedYtoScreen(mStartY);

        float zoom = ZoomButtonHandler.getsZoom();
        mPaint.setTextSize(mTextSize * zoom);
        c.drawText(mText, screenX, screenY, mPaint);
    }

    @Override
    public void saveToFile(ScribbleOutputStream dos, int version) throws IOException {
        dos.writeByte(TEXT);
        dos.writeFloat(mStartX);
        dos.writeFloat(mStartY);
        dos.writeUTF(mText);
    }

    public TextItem (ScribbleInputStream dis, int version, ScribbleView sv) throws IOException {
        super(null, sv);
        mTextSize = DEFAULT_TEXT_SIZE;
        mPaint.setTextSize(mTextSize);

        readFromFile(dis, version);
    }

    @Override
    public DrawItem readFromFile(ScribbleInputStream dis, int version) throws IOException {
        mTextSize = DEFAULT_TEXT_SIZE;
        mStartX = dis.readFloat();
        mStartY = dis.readFloat();
        String s = dis.readUTF();
        setmText(s);
        return null;
    }

    @Override
    public boolean selectItem(PointF p) {
        float minX = mStartX-FUZZY;
        float maxX = mStartX+bounds.right+FUZZY;
        float minY = mStartY-FUZZY;
        float maxY = mStartY+bounds.bottom+FUZZY;
        if (minX < p.x && p.x < maxX && minY < p.y && p.y < maxY) {
            mSelected = true;
            mPaint.setColor(Color.RED);
        }
        return mSelected;
    }

    public boolean selectItem (PointF start, PointF end) {
        float minX = mStartX;
        float maxX = mStartX+bounds.right;
        float minY = mStartY;
        float maxY = mStartY+bounds.bottom;
        if (minX >= start.x && maxX<=end.x && minY >= start.y && maxY <= end.y) {
            mSelected = true;
            mPaint.setColor(Color.RED);
        }
        return mSelected;
    }

    public RectF getBounds () {
        float minX = mStartX;
        float maxX = mStartX+bounds.right;
        float minY = mStartY;
        float maxY = mStartY+bounds.bottom;
        RectF result = new RectF(minX, minY, maxX, maxY);
        return result;
    }


    public String getmText() {return mText;}

    public void setmText(String mText) {
        this.mText = mText;
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(mText, 0, mText.length(), bounds);
    }

    public void move(float deltaX, float deltaY) {
        mStartX += deltaX;
        mStartY += deltaY;
    }


}
