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
        String newButtonText = Float.toString(sZoom);
        mButton.setText(newButtonText);
    }

    @Override
    public void onClick(View v) {
        sZoom *= 2.0f;
        if (sZoom >= 8.1f)
            sZoom = 0.125f;
//        Toast t = Toast.makeText(mScribbleView.getContext(), "Zoom set to "+sZoom, Toast.LENGTH_SHORT);
//        t.show();
        setButtonText();
        mScribbleView.invalidate();
    }

    public static float getsZoom() {return sZoom;}
    public static void setsZoom(float sZoom) {ZoomButtonHandler.sZoom = sZoom;}

    @Override
    public boolean onLongClick(View v) {
        sZoom = 1.0f;
        setButtonText();
        mScribbleView.invalidate();
        return true;
    }
}
