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
        super (v, R.id.file_list);
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

}
