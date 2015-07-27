/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import android.support.annotation.NonNull;

import com.google.android.gms.drive.Metadata;

import java.io.File;

/**
 */
public class GoogleDriveFile extends File {

    private String mName;
    private GoogleDriveFolder mParentFolder;

    public GoogleDriveFile(GoogleDriveFolder parent, Metadata m) {
        super("/");
        mName = m.getTitle();
    }

    public boolean isDirectory () {return false;}
    public boolean canRead () {return true;}
    public boolean exists () {return true;}

    @NonNull
    @Override
    public String getName() {
        return mName;
    }
}
