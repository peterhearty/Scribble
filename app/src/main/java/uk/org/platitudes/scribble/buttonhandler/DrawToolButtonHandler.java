/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.view.View;
import android.widget.Button;

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

    public boolean isFree () {
        if (mDrawToolButton == null) return false;
        boolean result = false;
        if (mDrawToolButton.getText().equals(buttonTexts[FREE])) {
            result = true;
        }
        return result;
    }

}
