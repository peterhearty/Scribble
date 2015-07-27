/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * Pops up a dialog to let a user choose a file.
 *
 * See Android/Sdk/docs/guide/topics/ui/dialogs.html
 */
public class FileChooser extends DialogFragment implements DialogInterface.OnClickListener {

    private FileList mFileList;
    private DirList mDirList;
    private boolean mSaveFike;
    private ScribbleMainActivity mMainActivity;
    private AlertDialog mAlertDialog;

    public void setParameters (ScribbleMainActivity sma, boolean writer) {
        mMainActivity = sma;
        mSaveFike = writer;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.file_chooser, null);

        mFileList = new FileList(v);
        mDirList = new DirList(v, mFileList);
        DeviceList deviceList = new DeviceList(v, mDirList, mMainActivity);

        builder.setView(v);
        builder.setMessage("Choose a file");

        builder.setPositiveButton("ok", this);
        builder.setNegativeButton("cancel", this);
        // Create the AlertDialog object and return it
        mAlertDialog = builder.create();

        deviceList.setmAlertDialog(mAlertDialog);

        return mAlertDialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which == DialogInterface.BUTTON_POSITIVE) {
            String dirName = mDirList.getDirectoryName();
            String fileName = mFileList.getFileName();
            FileSaver fs = new FileSaver(mMainActivity);
            if (mSaveFike) {
                fs.writeToFile(dirName, fileName);
            } else {
                fs.readFromFile(dirName, fileName);
            }
        }
    }
}
