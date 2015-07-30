/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFile;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFolder;

/**
 * File writer.
 */
public class FileScribbleWriter extends ScribbleWriter {

    private File mdir;
    private String mfilename;

    /**
     * Used when writing.
     */
    public FileScribbleWriter(ScribbleMainActivity sma, File dir, String fileName) {
        super(sma);
        mdir = dir;
        mfilename = fileName;
    }

    public FileScribbleWriter(ScribbleMainActivity sma) {
        super(sma);
    }

    public void writeToDefaultFile () {
        try {
            synchronized (ScribbleReader.sDataLock) {
                FileOutputStream fos = mMainView.getContext().openFileOutput(ScribbleReader.DATAFILE, Context.MODE_WORLD_READABLE);
                writeToOutputStream(fos);
                fos.close();
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeToDefaultFile", e);
        }
    }

    public void write () {
        try {
            OutputStream os = null;
            if (mdir instanceof GoogleDriveFolder) {
                GoogleDriveFolder gdf = (GoogleDriveFolder) mdir;
                GoogleDriveFile f = gdf.getFile(mfilename);
                if (f == null) {
                    // create new file
                    f = gdf.createFile(mfilename);
                }
                os = f.getOutputStream();
            } else {
                // A local file
                String dirName = mdir.getCanonicalPath();
                String pathName = dirName+ File.separator+mfilename;
                os = new FileOutputStream(pathName);
            }
            writeToOutputStream(os);
            os.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeToFile", e);
        }
    }

    private OutputStream getOutputStreamFromFile (File f) {
        OutputStream result = null;
        try {
            if (f instanceof  GoogleDriveFile) {
                GoogleDriveFile gdf = (GoogleDriveFile) f;
                result = gdf.getOutputStream();
            } else {
                result = new FileOutputStream(f);
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "getOutputStreamFromFile", e);
        }
        return result;
    }

    public void deleteDefaultFile () {
        try {
            File dir = mMainView.getContext().getFilesDir();
            String path = dir.getCanonicalPath() + File.separator + ScribbleReader.DATAFILE;
            File f = new File(path);
            boolean result = f.delete();
            if (!result) {
                ScribbleMainActivity.log(f.getCanonicalPath(), " not deleted", null);

            }
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "copyDefaultFile", e);
        }

    }



}
