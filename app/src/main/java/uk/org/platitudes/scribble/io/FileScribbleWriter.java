/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

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
    private File lastSuccessfulFileWrite;

    /**
     * Used when writing.
     */
    public FileScribbleWriter(ScribbleMainActivity sma, File dir, String fileName) {
        super(sma);
        mdir = dir;
        mfilename = fileName;
    }

    public FileScribbleWriter(ScribbleMainActivity sma, File file) {
        super(sma);
        mdir = file.getParentFile();
        mfilename = file.getName();
    }

    public File getLastSuccessfulFileWrite() {
        return lastSuccessfulFileWrite;
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
                lastSuccessfulFileWrite = f;
                os = f.getOutputStream();
            } else {
                // A local file
                String dirName = mdir.getCanonicalPath();
                String pathName = dirName+ File.separator+mfilename;
                os = new FileOutputStream(pathName);
                lastSuccessfulFileWrite = new File (mdir, mfilename);
            }
            boolean asText = false;
            if (mfilename.toLowerCase().endsWith(".txt")) {
                asText = true;
            }
            writeToOutputStream(os, asText);
            os.close();
        } catch (Exception e) {
            lastSuccessfulFileWrite = null;
            ScribbleMainActivity.log("FileSaver", "writeToFile", e);
        }
    }

    public static OutputStream getOutputStreamFromFile (File f) {
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

}
