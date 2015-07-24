/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.os.Environment;
import android.view.View;

import java.io.File;

import uk.org.platitudes.scribble.R;

/**
 */
public class DeviceList extends SimpleList {

    private DirList mFileList;
    private static final String[] deviceNames = {"Private", "Public", "Root", "Parent dir"};

    // Took out place "Private (external)" corresponding to getContext().getExternalFilesDir(null)


    public DeviceList (View v, DirList fileList) {
        super (v, R.id.device_list);
        mFileList = fileList;
        setContents(deviceNames);
    }

    @Override
    public void onClick(Object o) {
        String text = o.toString();
        File dir = null;
        if (text.equals(deviceNames[0])) {
            dir = mParentView.getContext().getFilesDir();
        } else if (text.equals(deviceNames[1])) {
            dir = Environment.getExternalStorageDirectory();
        } else if (text.equals(deviceNames[2])) {
            dir = Environment.getRootDirectory();
        } else if (text.equals(deviceNames[3])) {
            mFileList.setParentDirectory();
        }
        if (dir != null) {
            mFileList.setContents(dir);
        }
    }

}
