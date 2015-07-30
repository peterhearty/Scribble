/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

import java.io.File;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFile;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFolder;
import uk.org.platitudes.scribble.io.FileScribbleReader;

/**
 */
public class FileList extends SimpleList implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {

    private EditText mFileName;
    private File mLongClickedFile;
    private DirList mDirList;
    private File mCurFile;
    private File mPasteFile;
    private Button mPasteButton;

    public FileList (View v) {
        super(v, R.id.file_list);
        setOrderObjects(true);
        mFileName = (EditText) v.findViewById(R.id.file_name);

        mPasteButton = (Button) v.findViewById(R.id.paste_button);
        mPasteButton.setOnClickListener(this);
        mPasteButton.setEnabled(false);
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
        if (f.isDirectory()) return;
        if (!f.canRead()) {
            ScribbleMainActivity.log ("File not readable", "", null);
            return;
        }
        if (!FileScribbleReader.isScribbleFile(f)) {
            ScribbleMainActivity.log ("Not a Scribble file", "", null);
            return;
        }

        mFileName.setText(f.getName());
        mCurFile = f;
    }

    public File getFile () {return mCurFile;}

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
        if (FileScribbleReader.isScribbleFile(mLongClickedFile)) {
            createMenu(v);
        } else {
            ScribbleMainActivity.log("Not a Scribble file", "", null);
            mLongClickedFile = null;
        }
    }

    /**
     * Creates either a java.io.File object or a GoogleDriveFile object, depending on a
     * supplied template. The new File is then set to the supplied name.
     */
    private File createFile (File parent, String name) {
        File result = null;
        if (parent instanceof GoogleDriveFolder) {
            GoogleDriveFolder gdf = (GoogleDriveFolder) parent;
            // Note, the following will create a file on the Google Drive
            GoogleDriveFile newFile = new GoogleDriveFile(gdf, name, false);
            result = newFile;
        } else {
            result = new File (parent, name);
        }
        return result;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try {
            CharSequence menuText = item.getTitle();
            if (menuText.equals("delete")) {
                boolean deleted = mLongClickedFile.delete();
                if (deleted) {
                    mDirList.resetContents();
                } else {
                    ScribbleMainActivity.log("Failed to delete ", mLongClickedFile.getName(), null);
                }
            } else if (menuText.equals("rename")) {
                String newFileName = mFileName.getText().toString();
                if (newFileName.length() > 0) {
                    String newFilePath = mLongClickedFile.getParentFile().getCanonicalPath();
                    String newFileFullName = newFilePath + File.separator + newFileName;
                    File newFile = new File(newFileFullName);
                    boolean renamed = mLongClickedFile.renameTo(newFile);
                    if (renamed) {
                        mDirList.resetContents();
                    } else {
                        ScribbleMainActivity.log("Failed to rename ", mLongClickedFile.getName(), null);
                    }
                } else {
                    ScribbleMainActivity.log("New name in box above must be filled in", "", null);
                }
            } else if (menuText.equals("copy")) {
                String newFileName = mFileName.getText().toString();
                if (newFileName.length() > 0) {
                    // Copy to new file in same directory

                    File newFile = createFile(mLongClickedFile.getParentFile(), newFileName);

                    FileScribbleReader fs = new FileScribbleReader(ScribbleMainActivity.mainActivity, mLongClickedFile);
                    fs.copyFile(newFile);
                    mDirList.resetContents();
                } else {
                    // copy later to a different directory
                    mPasteFile = mLongClickedFile;
                    mPasteButton.setEnabled(true);
                    mPasteButton.setText("Paste "+mPasteFile.getName());
                }
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("FileList", "onMenuItemClick", e);
        }
        mLongClickedFile = null;
        return true;
    }

    public void setmDirList(DirList mDirList) {this.mDirList = mDirList;}

    /**
     * Handles clicks on the "Paste" button.
     * @param v
     */
    @Override
    public void onClick(View v) {
        // copy a file to the current directory
        String newFilePath = null;
        try {
            String newFileName = mPasteFile.getName();
            File newFile = createFile(mDirList.getmCurDir(), newFileName);

            FileScribbleReader fs = new FileScribbleReader(ScribbleMainActivity.mainActivity, mPasteFile);
            fs.copyFile(newFile);
            mDirList.resetContents();

        } catch (Exception e) {
            ScribbleMainActivity.log("FileList", "onClick", e);
        }
    }
}
