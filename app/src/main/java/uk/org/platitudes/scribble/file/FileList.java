/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.view.View;
import android.widget.EditText;

import java.io.File;

import uk.org.platitudes.scribble.R;

/**
 */
public class FileList extends SimpleList {

    private EditText mFileName;

    public FileList (View v) {
        super (v, "File list", R.id.file_list, R.id.device_name);

        mFileName = (EditText) v.findViewById(R.id.file_name);

    }

    public String getName (Object o){
        File f = (File) o;
        String s = f.getName();
        return s;
    }

    public void onClick (Object o) {
        File f = (File) o;
        if (!f.isDirectory()) {
            mFileName.setText(f.getName());
        }
    }

}
