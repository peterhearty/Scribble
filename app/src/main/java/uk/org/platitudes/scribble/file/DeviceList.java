/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.app.AlertDialog;
import android.content.IntentSender;
import android.os.Environment;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.File;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class DeviceList extends SimpleList {

    private boolean isWriter;
    private DirList mFileList;
    private ScribbleMainActivity mMainActivity;
    private AlertDialog mAlertDialog;
    private static final String[] deviceNames = {"Private", "Public", "Root", "Google Drive", "GDrive (private)", "Parent dir"};

    // Took out place "Private (external)" corresponding to getContext().getExternalFilesDir(null)

    public DeviceList (View v, DirList fileList, ScribbleMainActivity activity) {
        super (v, R.id.device_list);
        mFileList = fileList;
        mMainActivity = activity;
        setContents(deviceNames);
    }

    public void setParameters (AlertDialog ad, boolean writer) {
        mAlertDialog = ad;
        isWriter = writer;
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
            mAlertDialog.dismiss();
            if (isWriter) {
                mMainActivity.getmGoogleStuff().startGoogleDriveFileCreator();
            } else {
                mMainActivity.getmGoogleStuff().startGoogleDriveFileSelector();
            }
        } else if (text.equals(deviceNames[4])) {
            dir = mMainActivity.getmGoogleStuff().getmRootGoogleDriveFolder();
        } else if (text.equals(deviceNames[5])) {
            mFileList.setParentDirectory();
        }
        if (dir != null) {
            mFileList.setContents(dir);
        }
    }

}
