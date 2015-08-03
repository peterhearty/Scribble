/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFile;

/**
 * File reader.
 */
public class FileScribbleReader extends ScribbleReader {

    private File mFile;
    private File lastSuccessfulFileRead;

    /**
     * Used when reading.
     */
    public FileScribbleReader(ScribbleMainActivity sma, File f) {
        super(sma);
        mFile = f;
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
            ScribbleMainActivity.log("FileScribbleReader", "getInputStreamFromFile", e);
        }
        return result;
    }

    public File getLastSuccessfulFileRead() {
        return lastSuccessfulFileRead;
    }

    public void read (Drawing drawing) {
        InputStream is = getInputStreamFromFile(mFile);
        if (is !=  null) {
            try {
                lastSuccessfulFileRead = mFile;
                readFromInputStream(is, drawing);
                is.close();
            } catch (Exception e) {
                lastSuccessfulFileRead = null;
                ScribbleMainActivity.log("FileScribbleReader", "read", e);
            }
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
                    ScribbleInputStream dis = new ScribbleInputStream(fis);
                    long magicNumber = dis.readLong();
                    if (magicNumber == MAGIC_NUMBER) {
                        result = true;
                    }
                    dis.close();
                    fis.close();
                }
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("FileScribbleReader", "isScribbleFile", e);
        }
        return result;
    }

    public void copyFile (File dst) {
        try {
            if (mFile.getCanonicalPath().equals(dst.getCanonicalPath())) {
                throw new Exception ("source and destination files are the same");
            }
            if (dst.exists() && !isScribbleFile(dst)) {
                throw new Exception ("Copy destination is not a scribble file");
            }
            InputStream is = getInputStreamFromFile(mFile);
            OutputStream os = FileScribbleWriter.getOutputStreamFromFile(dst);
            while (is.available()>0) {
                int nextByte = is.read();
                os.write(nextByte);
            }
            is.close();
            os.close();
        } catch (Exception e) {
            ScribbleMainActivity.log("FileSaver", "copyFile", e);
        }

    }


}
