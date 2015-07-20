/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.FreehandCompressedDrawItem;
import uk.org.platitudes.scribble.drawitem.FreehandDrawItem;
import uk.org.platitudes.scribble.drawitem.LineDrawItem;
import uk.org.platitudes.scribble.drawitem.ScrollItem;

public class DrawToolButtonHandler implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private Button mDrawToolButton;

    public DrawToolButtonHandler (Button b) {
        mDrawToolButton = b;
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        CharSequence curText = b.getText();
        createMenu();
    }

    private void createMenu () {
        PopupMenu popup = new PopupMenu(mDrawToolButton.getContext(), mDrawToolButton);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.draw_button_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public DrawItem generateDrawItem (MotionEvent event, ScribbleView scribbleView) {
        if (mDrawToolButton == null) return null;

        DrawItem result = null;
        if (mDrawToolButton.getText().equals("free")) {
            result = new FreehandCompressedDrawItem(event, scribbleView);
        } else if (mDrawToolButton.getText().equals("line")) {
            result = new LineDrawItem(event, scribbleView);
        } else if (mDrawToolButton.getText().equals("scroll")) {
            result = new ScrollItem(event, scribbleView);
        }
        return result;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        CharSequence menuTitle = item.getTitle();
        mDrawToolButton.setText(menuTitle);
        return true;
    }
}
