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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import uk.org.platitudes.scribble.R;

/**
 * See Android/Sdk/docs/guide/topics/ui/dialogs.html
 */
public class FileChooser extends DialogFragment implements DialogInterface.OnClickListener {

    private DeviceList mDeviceList;
    private FileList mFileList;
    private DirList mDirList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.file_chooser, null);

        mFileList = new FileList(v);
        mDirList = new DirList(v, mFileList);
        mDeviceList = new DeviceList(v, mDirList);

        builder.setView(v);
        builder.setMessage("Choose a file");

        builder.setPositiveButton("ok", this);
        builder.setNegativeButton("cancel", this);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
}
