package uk.org.platitudes.scribble.file;

import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import uk.org.platitudes.scribble.R;

/**
 */
public class DirList extends SimpleList {

    private TextView mDirectoryName;
    private FileList mFileList;


    public DirList(View v, FileList fileList) {
        super(v, R.id.dir_list);
        mFileList = fileList;
        mDirectoryName = (TextView) v.findViewById(R.id.directory_name);

        File startDir = v.getContext().getFilesDir();
        setOrderObjects(true);
        setContents(startDir);

    }

    public void setContents (File dirFile) {
        if (!dirFile.exists()) return;
        if (!dirFile.canRead()) return;
        if (!dirFile.isDirectory()) return;

        File[] files = dirFile.listFiles();
        ArrayList<File> dirList = new ArrayList<>();
        ArrayList<File> fileList = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory()) {
                dirList.add(f);
            } else {
                fileList.add(f);
            }
        }
        Object[] dirArray = dirList.toArray();
        setContents(dirArray);
        Object[] fileArray = fileList.toArray();
        mFileList.setContents(fileArray);

        String dirPath = "";
        try {
            dirPath = dirFile.getCanonicalPath();
        } catch (Exception e) {
            // do nothing
        }

        mDirectoryName.setText(dirPath);
        mFileList.setmFileName("");
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

    public void setParentDirectory() {
        try {
            File f = new File(mDirectoryName.getText().toString());
            String parentPath = f.getParent();
            f = new File (parentPath);
            setContents(f);
        } catch (Exception e) {
            // do nothing
        }
    }

    public void onClick (Object o) {
        File f = (File) o;
        if (f.isDirectory()) {
            setContents(f);
        }
    }


}