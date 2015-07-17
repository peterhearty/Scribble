/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import uk.org.platitudes.scribble.R;

public class MoreButtonHandler implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private Button mMoreButton;

    public MoreButtonHandler (Button b) {
        mMoreButton = b;
    }


    private void createMenu () {
        PopupMenu popup = new PopupMenu(mMoreButton.getContext(), mMoreButton);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.more_actions_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }


    @Override
    public void onClick(View v) {
        createMenu();
    }

    private void overrideVisibilitychanges () {
        // Consider making the default UI flags
        // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION as in
        // http://developer.sonymobile.com/knowledge-base/tutorials/android_tutorial/how-to-provide-your-app-users-with-maximum-screen-estate-tutorial/

        int visibility = mMoreButton.getSystemUiVisibility();
        // Note that the visibility might not be zero if other flags have been set.
        // should really check which flags are set.
        // We have to put the visibility checks in, otherwise setSystemUiVisibility
        // generates another call to overrideVisibilitychanges and this goes on forever.
        int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        mMoreButton.setSystemUiVisibility(flags);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        CharSequence menuTitle = item.getTitle();
        if (menuTitle.equals("fullscreen")) {
            overrideVisibilitychanges ();
        }
        return true;
    }

}
