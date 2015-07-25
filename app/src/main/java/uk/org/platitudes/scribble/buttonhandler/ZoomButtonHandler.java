package uk.org.platitudes.scribble.buttonhandler;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.org.platitudes.scribble.ScribbleView;

public class ZoomButtonHandler implements View.OnClickListener, View.OnLongClickListener {

    private static float sZoom = 1.0f;
    private ScribbleView mScribbleView;
    private Button mButton;

    public ZoomButtonHandler (ScribbleView v, Button b) {
        mScribbleView = v;
        mButton = b;
        mButton.setOnLongClickListener(this);
    }

    private void setButtonText () {
        String newButtonText = "zoom "+Float.toString(sZoom);
        mButton.setText(newButtonText);
    }

    @Override
    public void onClick(View v) {
        sZoom *= 2.0f;
        if (sZoom >= 4.05f)
            sZoom = 0.25f;
        setButtonText();
        mScribbleView.invalidate();
    }

    public static float getsZoom() {return sZoom;}

    public void setsZoom(float sZoom) {
        ZoomButtonHandler.sZoom = sZoom;
        setButtonText ();
    }

    @Override
    public boolean onLongClick(View v) {
        sZoom = 1.0f;
        setButtonText();
        mScribbleView.setmScrollOffset(0f, 0f);
        mScribbleView.invalidate();
        return true;
    }
}
