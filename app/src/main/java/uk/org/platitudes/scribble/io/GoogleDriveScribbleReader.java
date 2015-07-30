package uk.org.platitudes.scribble.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFile;

/**
 */
public class GoogleDriveScribbleReader extends FileScribbleReader {

    /**
     * Used when reading or writing.
     */
    public GoogleDriveScribbleReader(ScribbleMainActivity sma) {
        super(sma, null);
    }

    private static InputStream getInputStreamFromFile (File f) {
        InputStream result = null;
        GoogleDriveFile gdf = (GoogleDriveFile) f;
        result = gdf.getInputStream();
        return result;
    }


}
