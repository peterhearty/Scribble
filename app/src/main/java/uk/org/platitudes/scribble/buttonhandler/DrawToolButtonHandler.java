/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.FreehandDrawItem;
import uk.org.platitudes.scribble.drawitem.ScrollItem;

public class DrawToolButtonHandler implements View.OnClickListener {

    public static int FREE = 0;
    public static int LINE = 1;
    public static int TEXT = 2;
    public static int SELECT = 3;
    public static int SCROLL = 4;

    public static String[] buttonTexts = {"Free", "Line", "Text", "select", "scroll"} ;

    private Button mDrawToolButton;

    public DrawToolButtonHandler (Button b) {
        mDrawToolButton = b;
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        CharSequence curText = b.getText();

        for (int i=0; i<buttonTexts.length; i++) {
            if (curText.equals(buttonTexts[i])) {
                int j = i+1;
                if (j == buttonTexts.length) j=0;
                    b.setText(buttonTexts[j]);
                break;
            }
        }
    }

    public DrawItem generateDrawItem (MotionEvent event, ScribbleView scribbleView) {
        if (mDrawToolButton == null) return null;

        DrawItem result = null;
        if (mDrawToolButton.getText().equals(buttonTexts[FREE])) {
            result = new FreehandDrawItem(event, scribbleView);
        } else if (mDrawToolButton.getText().equals(buttonTexts[SCROLL])) {
            result = new ScrollItem(event, scribbleView);
        }
        return result;
    }

}
