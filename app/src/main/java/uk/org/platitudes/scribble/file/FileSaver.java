/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFile;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFolder;

/**
 * Save data to a file or restores data from a file.
 *
 * Should really be two classes: a reader and a writer. Each object should also control what it
 * saves and restores. However it's handy to be able to see exactly what goes where all in one
 * place.
 */
public class FileSaver {


    private ScribbleView mMainView;
    private ScribbleMainActivity mScribbleMainActivity;

    private static final String EVERYTHING_KEY = "everything";
    private static final int FILE_FORMAT_VERSION = 1000;
    private static final long MAGIC_NUMBER = 0x5C81881EF11EL; // sort of says SCRIBBLEFILE
    public static final String DATAFILE = "currentDataFile";

    // See http://developer.android.com/guide/topics/data/backup.html
    public static final Object sDataLock = new Object();



    /**
     * Used when reading or writing.
     *
     */
    public FileSaver (ScribbleMainActivity sma) {
        mScribbleMainActivity = sma;
        mMainView = mScribbleMainActivity.getmMainView();
    }

    /************** WRITING METHODS *******************/

    public void writeToBundle (Bundle bundle) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        DataOutputStream dos = new DataOutputStream(baos);

        writeMainView(dos);
        byte[] bytes = baos.toByteArray();
        bundle.putByteArray(EVERYTHING_KEY, bytes);

        try {
            dos.close();
            baos.close();
        } catch (IOException e) {
            ScribbleMainActivity.log("FileSaver", "writeToBundle", e);
        }
    }

    public void writeToOutputStream(OutputStream fos) {
        try {
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeLong(MAGIC_NUMBER);
            dos.writeInt(FILE_FORMAT_VERSION);
            writeMainView(dos);
            dos.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeToOutputStream", e);
        }

    }

    public void writeToDefaultFile () {
        try {
            synchronized (sDataLock) {
                FileOutputStream fos = mMainView.getContext().openFileOutput(DATAFILE, Context.MODE_WORLD_READABLE);
                writeToOutputStream(fos);
                fos.close();
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeToDefaultFile", e);
        }
    }

    public void writeToFile (File dir, String fileName) {
        try {
            OutputStream os = null;
            if (dir instanceof GoogleDriveFolder) {
                GoogleDriveFolder gdf = (GoogleDriveFolder) dir;
                GoogleDriveFile f = gdf.getFile(fileName);
                if (f == null) {
                    // create new file
                    f = gdf.createFile(fileName);
                }
                os = f.getOutputStream();
            } else {
                // A local file
                String dirName = dir.getCanonicalPath();
                String pathName = dirName+ File.separator+fileName;
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

    public void writeToFile (File f) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            writeToOutputStream(fos);
            fos.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeToFile", e);
        }
    }

    private void writeMainView(DataOutputStream dos) {
        try {
            mMainView.saveDrawList(dos, FILE_FORMAT_VERSION);
            mMainView.saveUndoList(dos, FILE_FORMAT_VERSION);
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeMainView", e);
        }
    }


    /*************** READING METHODS *******************/


    private void readMainView(DataInputStream dis, int fileFormatVersion) {
        try {
            mMainView.readDrawList(dis, fileFormatVersion);
            mMainView.readUndoList(dis, fileFormatVersion);
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "readMainView", e);
        }
    }

    public void readFromFile (String dirName, String fileName) {
        String pathName = dirName+ File.separator+fileName;
        try {
            File f = new File (pathName);
            readFromFile(f);
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "readFromFile", e);
        }
    }

    private static InputStream getInputStreamFromFile (File f) {
        InputStream result = null;
        try {
            if (f instanceof GoogleDriveFile) {
                GoogleDriveFile gdf = (GoogleDriveFile) f;
                result = gdf.getInputStream();
            } else {
                result = new FileInputStream(f);
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "getInputStreamFromFile", e);
        }
        return result;
    }

    public void readFromFile (File f) {
        InputStream is = getInputStreamFromFile(f);
        if (is !=  null) {
            try {
                readFromInputStream(is);
                is.close();
            } catch (Exception e) {
                ScribbleMainActivity.log("FileSaver", "readFromFile", e);
            }
        }
    }


    public void readFromInputStream(InputStream fis) {
        try {
            DataInputStream dis = new DataInputStream(fis);
            long magNumber = dis.readLong();
            if (magNumber != MAGIC_NUMBER) {
                ScribbleMainActivity.log ("Not a scribble file", "", null);
            } else {
                int fileFormatVersion = dis.readInt();
                readMainView(dis, fileFormatVersion);
                // following set offset to zero and zoom to 1
                mScribbleMainActivity.getmZoomButtonHandler().onLongClick(null);
            }
            dis.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "readFromInputStream", e);
        }

    }

    public void readFromDefaultFile () {
        try {
            synchronized (sDataLock) {
                FileInputStream fis = mMainView.getContext().openFileInput(DATAFILE);
                readFromInputStream(fis);
                fis.close();
            }
        } catch (FileNotFoundException fnfe) {
            // do nothing
        } catch (IOException e) {
            ScribbleMainActivity.log("FileSaver", "readFromDefaultFile", e);
        }
    }

    public void readFromBundle (Bundle bundle) {
        byte[] bytes = bundle.getByteArray(EVERYTHING_KEY);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        readMainView(dis, FILE_FORMAT_VERSION);
        try {
            dis.close();
            bais.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "read from Bundle", e);
        }
    }

    /**
     * Tests a file to check that it's a Scribble file.
     */
    public static boolean isScribbleFile (File f) {
        boolean result = false;
        try {
            if (f.length() > 12) {
                InputStream fis = getInputStreamFromFile(f);
                if (fis != null) {
                    DataInputStream dis = new DataInputStream(fis);
                    long magicNumber = dis.readLong();
                    if (magicNumber == MAGIC_NUMBER) {
                        result = true;
                    }
                    dis.close();
                    fis.close();
                }
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "isScribbleFile", e);
        }
        return result;
    }


    /**************** Utility methods ***********************/

    public void copyFile (File src, File dst) {
        try {
            if (src.getCanonicalPath().equals(dst.getCanonicalPath())) {
                throw new Exception ("source and destination files are the same");
            }
            if (dst.exists() && !isScribbleFile(dst)) {
                throw new Exception ("Copy destination is not a scribble file");
            }
            FileInputStream fis = new FileInputStream(src);
            FileOutputStream fos = new FileOutputStream(dst);
            while (fis.available()>0) {
                int nextByte = fis.read();
                fos.write(nextByte);
            }
            fis.close();
            fos.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "copyFile", e);
        }

    }

    public void delete () {
        try {
            File dir = mMainView.getContext().getFilesDir();
            String path = dir.getCanonicalPath() + File.separator + DATAFILE;
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
