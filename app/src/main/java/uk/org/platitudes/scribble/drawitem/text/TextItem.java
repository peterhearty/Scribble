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

    private static final float DEFAULT_TEXT_SIZE = 100.0f;
    private float mTextSize;
    private String mText;

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
        mStart.x = mScribbleView.screenXtoStored(screenX);
        mStart.y = mScribbleView.screenYtoStored(screenY);

        mText = "";

        EditTextDialog dialog = new EditTextDialog();
        dialog.setTextItem(this);
        dialog.show(ScribbleMainActivity.mainActivity.getFragmentManager(), "");

        scribbleView.addItem(this);
    }

    @Override
    public int getHashTag() {
        int result = (int) (TEXT*1000 + mStart.x +mStart.y +mText.length());
        return result;
    }



    @Override
    public void draw(Canvas c) {
        float screenX = mScribbleView.storedXtoScreen(mStart.x);
        float screenY = mScribbleView.storedYtoScreen(mStart.y);

        float zoom = ZoomButtonHandler.getsZoom() *mZoom;
        mPaint.setTextSize(mTextSize * zoom);
        c.drawText(mText, screenX, screenY, mPaint);

        drawBounds(c);
    }

    @Override
    public void saveToFile(ScribbleOutputStream dos, int version) throws IOException {
        dos.writeByte(TEXT);
        dos.writeFloat(mZoom);
        dos.writeByte(deleted ? 1 : 0);
        dos.writeFloat(mStart.x);
        dos.writeFloat(mStart.y);
        dos.writeUTF(mText);
    }

    public TextItem (ScribbleInputStream dis, int version, ScribbleView sv) throws IOException {
        super(null, sv);
        mTextSize = DEFAULT_TEXT_SIZE;
        mPaint.setTextSize(mTextSize);

        if (version >= 1002) {
            mZoom = dis.readFloat();
            byte deletedByte = dis.readByte();
            if (deletedByte==1) {
                deleted = true;
            }
        }

        readFromFile(dis, version);
    }

    @Override
    public DrawItem readFromFile(ScribbleInputStream dis, int version) throws IOException {
        mTextSize = DEFAULT_TEXT_SIZE;
        mStart.x = dis.readFloat();
        mStart.y = dis.readFloat();
        String s = dis.readUTF();
        setmText(s);
        return null;
    }

    public RectF getBounds () {
        mPaint.setTextSize(mTextSize * mZoom);
        Rect bounds = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), bounds);

        float minX = mStart.x;
        float maxX = mStart.x+bounds.right;
        float minY = mStart.y+bounds.top; // getTextBounds relative to the BOTTOM left, not TOP left
        float maxY = mStart.y;
        RectF result = new RectF(minX, minY, maxX, maxY);
        return result;
    }


    public String getmText() {return mText;}

    public void setmText(String mText) {
        this.mText = mText;
        if (mText.length() == 0) {
            deleted = true;
        }
    }

    public void move(float deltaX, float deltaY) {
        mStart.x += deltaX;
        mStart.y += deltaY;
    }


}
