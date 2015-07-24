/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.file.FileChooser;

public class MoreButtonHandler extends RestoreObserver implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    public static final String DATAFILE = "currentDataFile";
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
        } else if (menuTitle.equals("save")) {
            try {
                FileOutputStream fos = mMoreButton.getContext().openFileOutput(DATAFILE, Context.MODE_PRIVATE);
                DataOutputStream dos = new DataOutputStream(fos);
                mActivity.saveEverything(dos);
                dos.close();
                fos.close();
            } catch (Exception e) {
                ScribbleMainActivity.log("onMenuItemClick ", "", e);
            }
        } else if (menuTitle.equals("file")) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.show(ScribbleMainActivity.mainActivity.getFragmentManager(), "");
        } else if (menuTitle.equals("open")) {
            try {
                FileInputStream fis = mMoreButton.getContext().openFileInput(DATAFILE);
                DataInputStream dis = new DataInputStream(fis);
                mActivity.readeverything(dis);
                dis.close();
                fis.close();
            } catch (Exception e) {
                ScribbleMainActivity.log("onMenuItemClick", "", e);
            }
            mActivity.getmMainView().invalidate();

        }  else if (menuTitle.equals("exit")) {
            ScribbleMainActivity.mainActivity.finish();
        } else if (menuTitle.equals("backup")) {
            BackupManager bm = new BackupManager(mActivity.getmMainView().getContext());
            bm.dataChanged();
        } else if (menuTitle.equals("restore")) {
            BackupManager bm = new BackupManager(mActivity.getmMainView().getContext());
            bm.requestRestore(this);
        }

        return true;
    }

    public void onUpdate(int nowBeingRestored, String currentPackage) {
        ScribbleMainActivity.log("Restoring ", currentPackage, null);
    }
    public void restoreFinished(int error) {
        ScribbleMainActivity.log("Restore finished", "", null);
    }
    public void restoreStarting(int numPackages) {
        ScribbleMainActivity.log ("Restore starting", "", null);
    }

}