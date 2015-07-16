/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.view.View;
import android.widget.Button;

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
            b.setText("undo");
            mRedo = false;
        } else {
            b.setText("redo");
            mRedo = true;
        }
        return true;
    }
}
