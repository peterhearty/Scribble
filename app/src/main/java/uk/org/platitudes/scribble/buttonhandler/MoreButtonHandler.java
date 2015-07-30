/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.file.FileChooser;
import uk.org.platitudes.scribble.io.FileScribbleReader;
import uk.org.platitudes.scribble.io.FileScribbleWriter;

public class MoreButtonHandler implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private Button mMoreButton;
    private ScribbleMainActivity mActivity;

    public MoreButtonHandler (Button b, ScribbleMainActivity sma) {
        mMoreButton = b;
        mActivity = sma;
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
        } else if (menuTitle.equals("open")) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setParameters(mActivity, false);
            fileChooser.show(ScribbleMainActivity.mainActivity.getFragmentManager(), "");
            mActivity.getmMainView().invalidate();
        } else if (menuTitle.equals("save")) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setParameters(mActivity, true);
            fileChooser.show(ScribbleMainActivity.mainActivity.getFragmentManager(), "");
        }  else if (menuTitle.equals("exit")) {
            ScribbleMainActivity.mainActivity.finish();
        } else if (menuTitle.equals("clear")) {
            mActivity.getmMainView().clear();
        } else if (menuTitle.equals("about")) {
            mActivity.about();
        }

        return true;
    }

}
