/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

public class UndoButtonHandler implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    private ScribbleView mScribbleView;
    private boolean mRedo;

    public UndoButtonHandler (ScribbleView v, Button b) {

        mScribbleView = v;
        b.setOnLongClickListener(this);
        b.setBackgroundColor(ScribbleMainActivity.grey);
        b.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mRedo)
            mScribbleView.redo();
        else
            mScribbleView.undo();
    }

    /**
     * A simple color change on being pressed.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            v.setBackgroundColor(Color.LTGRAY);
        }
        if (event.getAction() == MotionEvent.ACTION_UP){
            v.setBackgroundColor(ScribbleMainActivity.grey);
        }
        return false;
    }


    @Override
    public boolean onLongClick(View v) {
        Button b = (Button) v;
        if (mRedo) {
//            b.setImageResource(R.drawable.undo);
            b.setBackgroundColor(ScribbleMainActivity.grey);
            b.setText("undo");
            mRedo = false;
        } else {
//            b.setImageResource(R.drawable.redo);
            b.setText("redo");
            b.setBackgroundColor(Color.WHITE);
            mRedo = true;
        }
        return true;
    }
}
