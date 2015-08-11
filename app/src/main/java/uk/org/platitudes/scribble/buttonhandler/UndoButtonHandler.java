/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleView;

public class UndoButtonHandler implements View.OnClickListener, View.OnLongClickListener {

    private ScribbleView mScribbleView;
    private boolean mRedo;

    public UndoButtonHandler (ScribbleView v, Button b) {

        mScribbleView = v;
        b.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mRedo)
            mScribbleView.redo();
        else
            mScribbleView.undo();
    }

    @Override
    public boolean onLongClick(View v) {
        Button b = (Button) v;
        if (mRedo) {
//            b.setImageResource(R.drawable.undo);
            b.setText("undo");
            b.setBackgroundColor(Color.WHITE);
            mRedo = false;
        } else {
//            b.setImageResource(R.drawable.redo);
            b.setText("redo");
            b.setBackgroundColor(Color.LTGRAY);
            mRedo = true;
        }
        return true;
    }
}
