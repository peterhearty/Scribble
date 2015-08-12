/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.graphics.Color;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.drawitem.GroupItem;
import uk.org.platitudes.scribble.drawitem.MoveItem;
import uk.org.platitudes.scribble.drawitem.ResizeItem;
import uk.org.platitudes.scribble.drawitem.freehand.FreehandCompressedDrawItem;
import uk.org.platitudes.scribble.drawitem.LineDrawItem;
import uk.org.platitudes.scribble.drawitem.ScrollItem;
import uk.org.platitudes.scribble.drawitem.text.TextItem;

public class DrawToolButtonHandler implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, View.OnTouchListener {

    private Button mDrawToolButton;

    public DrawToolButtonHandler (Button b) {
        mDrawToolButton = b;
        mDrawToolButton.setBackgroundColor(ScribbleMainActivity.grey);
        mDrawToolButton.setOnTouchListener(this);
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
            result = new LineDrawItem(event, scribbleView, false);
        } else if (mDrawToolButton.getText().equals("box")) {
            result = new LineDrawItem(event, scribbleView, true);
        } else if (mDrawToolButton.getText().equals("text")) {
            result = new TextItem(event, scribbleView);
        } else if (mDrawToolButton.getText().equals("group")) {
            result = new GroupItem(event, scribbleView);
        }
        return result;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        CharSequence menuTitle = item.getTitle();
        mDrawToolButton.setText(menuTitle);
        return true;
    }

    /**
     * A simple color change on being pressed.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            mDrawToolButton.setBackgroundColor(Color.LTGRAY);
        }
        if (event.getAction() == MotionEvent.ACTION_UP){
            mDrawToolButton.setBackgroundColor(ScribbleMainActivity.grey);
        }
        return false;
    }
}
