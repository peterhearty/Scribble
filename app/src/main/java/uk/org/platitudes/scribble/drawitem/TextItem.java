/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.EditTextDialog;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;

/**
 */
public class TextItem implements DrawItem {

    private float mStartX;
    private float mStartY;
    private String mText;
    private Paint mPaint;

    public TextItem (float x, float y, String s) {
        mStartX = x;
        mStartY = y;
        mText = s;
        createPaint();
    }

    public TextItem (MotionEvent event, ScribbleView scribbleView) {

        float screenX = event.getX();
        float screenY = event.getY();
        mStartX = scribbleView.screenXtoStored(screenX);
        mStartY = scribbleView.screenYtoStored(screenY);

        createPaint();
        mText = "";

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
        mText = dis.readUTF();
        return null;
    }


    public String getmText() {return mText;}
    public void setmText(String mText) {this.mText = mText;}

}
