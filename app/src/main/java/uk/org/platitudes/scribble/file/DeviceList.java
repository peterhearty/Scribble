/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import uk.org.platitudes.scribble.R;

/**
 */
public class DeviceList extends SimpleList {

    private DirList mFileList;
    private static final String[] deviceNames = {"App private", "External private", "External public", "Root", "Parent dir"};


    public DeviceList (View v, DirList fileList) {
        super (v, "Devices", R.id.device_list, R.id.device_name);
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
            dir = mParentView.getContext().getExternalFilesDir(null);
        } else if (text.equals(deviceNames[2])) {
            dir = Environment.getExternalStorageDirectory();
        } else if (text.equals(deviceNames[3])) {
            dir = Environment.getRootDirectory();
        } else if (text.equals(deviceNames[4])) {
            mFileList.setParentDirectory();
        }
        if (dir != null) {
            mFileList.setContents(dir);
        }
    }

}
