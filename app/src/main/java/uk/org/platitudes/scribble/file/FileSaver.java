/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.content.Context;
import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

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

    private void writeToOpenFile (FileOutputStream fos) {
        try {
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeLong(MAGIC_NUMBER);
            dos.writeInt(FILE_FORMAT_VERSION);
            writeMainView(dos);
            dos.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeToOpenFile", e);
        }

    }

    public void writeToDefaultFile () {
        try {
            FileOutputStream fos = mMainView.getContext().openFileOutput(DATAFILE, Context.MODE_PRIVATE);
            writeToOpenFile(fos);
            fos.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "writeToDefaultFile", e);
        }
    }

    public void writeToFile (String dirName, String fileName) {
        String pathName = dirName+ File.separator+fileName;
        try {
            FileOutputStream fos = new FileOutputStream(pathName);
            writeToOpenFile (fos);
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
            FileInputStream fis = new FileInputStream(pathName);
            readFromOpenFile(fis);
            fis.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "readFromFile", e);
        }
    }


    private void readFromOpenFile (FileInputStream fis) {
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
            ScribbleMainActivity.log("FileSaver", "readFromDefaultFile", e);
        }

    }

    public void readFromDefaultFile () {
        try {
            FileInputStream fis = mMainView.getContext().openFileInput(DATAFILE);
            readFromOpenFile (fis);
            fis.close();
        } catch (Exception e) {
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

}