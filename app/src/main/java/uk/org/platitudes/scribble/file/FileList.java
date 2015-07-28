/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import java.io.File;
import java.io.IOException;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class FileList extends SimpleList implements PopupMenu.OnMenuItemClickListener {

    private EditText mFileName;
    private File mLongClickedFile;

    public FileList (View v) {
        super(v, R.id.file_list);
        setOrderObjects(true);
        mFileName = (EditText) v.findViewById(R.id.file_name);

    }

    public String getName (Object o){
        File f = (File) o;
        String s = f.getName();
        return s;
    }

    public boolean isLessThan (Object a, Object b) {
        String stringA = a.toString();
        String stringB = b.toString();
        if (stringA.compareToIgnoreCase(stringB) < 0) {
            return true;
        }
        return false;
    }

    public void setmFileName (String s) {
        if (mFileName != null) {
            mFileName.setText(s);
        }
    }

    public void onClick (Object o) {
        File f = (File) o;
        if (!f.isDirectory()) {
            mFileName.setText(f.getName());
        }
    }

    public String getFileName () {
        String result = null;
        if (mFileName != null ) {
            result = mFileName.getText().toString();
        }
        return result;
    }

    private void createMenu (View v) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.file_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public void onLongClick(Object o, View v) {
        mLongClickedFile = (File) o;
        createMenu (v);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        CharSequence menuText = item.getTitle();
        if (menuText.equals("delete")) {
            boolean deleted = mLongClickedFile.delete();
            if (deleted) {
                // refresh list
            } else {
                ScribbleMainActivity.log("Failed to delete ", mLongClickedFile.getName(), null);
            }
        } else if (menuText.equals("rename")) {
            try {
                String newFileName = mFileName.getText().toString();
                if (newFileName.length() > 0) {
                    String newFilePath = mLongClickedFile.getCanonicalPath();
                    String newFileFullName = newFilePath + File.separator + newFileName;
                    File newFile = new File (newFileFullName);
                    boolean renamed = mLongClickedFile.renameTo(newFile);
                    if (renamed ) {
                        // refresh list
                    } else {
                        ScribbleMainActivity.log("Failed to rename ", mLongClickedFile.getName(), null);
                    }
                } else {
                    ScribbleMainActivity.log("New name in box above must be filled in", "", null);
                }
            } catch (Exception e) {
                ScribbleMainActivity.log("FileList", "rename menu handler", e);
            }
        }
        return true;
    }
}
