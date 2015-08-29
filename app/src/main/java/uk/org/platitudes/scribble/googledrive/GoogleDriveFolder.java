/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class GoogleDriveFolder extends File implements ResultCallback<DriveApi.MetadataBufferResult> {

    private DriveFolder mDriveFolder;
    private GoogleApiClient mGoogleApiClient;
    private GoogleDriveFile[] mContents;
    private PendingResult<DriveApi.MetadataBufferResult> mPendingResult;

    public static final String PATH_PREFIX = ":GoogleDrive:";

    public String toString () {
        return PATH_PREFIX;
    }

    public GoogleDriveFolder(ScribbleMainActivity activity) {
        // Need to cal a super of some kind.
        super("/");
        mGoogleApiClient = activity.getmGoogleStuff().getmGoogleApiClient();
        mDriveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        mContents = new GoogleDriveFile[0];
        requestContents();
    }

    public static boolean isGoogleDriveFile (String path) {
        if (path.contains(PATH_PREFIX)) {
            return true;
        }
        return false;
    }

    public static String extractGoogleDriveFilename (String path) {
        int lastSlash = path.lastIndexOf('/');
        String name = path.substring(lastSlash+1);
        return name;
    }

    public GoogleApiClient getmGoogleApiClient() {return mGoogleApiClient;}
    public DriveFolder getmDriveFolder() {return mDriveFolder;}
    public boolean isDirectory () {return true;}
    public boolean canRead () {return true;}
    public boolean exists () {return true;}
    public File[] listFiles () {return mContents;}
    public String getCanonicalPath() throws IOException {return PATH_PREFIX;}

    public GoogleDriveFile createFile (String name) {
        GoogleDriveFile result = getFile(name);
        if (result == null) {
            // file does not exist, create a new one
            result = new GoogleDriveFile(this, name, true);

            // add new file to local dir contents
            addFileToList(result);
        }
        return result;
    }

    public void addFileToList (GoogleDriveFile newFile) {
        // add new file to local dir contents
        GoogleDriveFile[] newContents = new GoogleDriveFile[mContents.length+1];
        System.arraycopy(mContents, 0, newContents, 0, mContents.length);
        newContents[newContents.length-1] = newFile;
        mContents = newContents;
    }

    /**
     * Deletes a file from the locally held list. It's the caller's responsibility
     * to make sure it exists, otherwise an ArrayOutOfBounds is likely.
     *
     * Caller performs the GoogleDrive delete function.
     */
    public void deleteFile (GoogleDriveFile deletedFile) {
        GoogleDriveFile[] newContents = new GoogleDriveFile[mContents.length-1];
        int newPosn = 0;
        for (GoogleDriveFile f : mContents) {
            if (f != deletedFile) {
                newContents[newPosn++] = f;
            }
        }
        mContents = newContents;
    }

    public GoogleDriveFile getFile (String name) {
        for (GoogleDriveFile f : mContents) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }



    private void requestContents () {
        // Start off an async request to get the children.
        ScribbleMainActivity.log("GoogleDriveFolder", "requesting countents", null);
        mPendingResult = mDriveFolder.listChildren(mGoogleApiClient);
        mPendingResult.setResultCallback(this);
    }

    @Override
    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
        Status status = metadataBufferResult.getStatus();
        ScribbleMainActivity.log("GoogleDriveFolder", "contents received status="+status.toString(), null);
        if (status.isSuccess()) {
            MetadataBuffer mtb = metadataBufferResult.getMetadataBuffer();
            int count = mtb.getCount();
            ArrayList<GoogleDriveFile> files = new ArrayList<>(count);
            for (Metadata m : mtb) {
                if (m.isTrashed()) {
                    continue;
                }
                if (m.isFolder()) {
                    // create a GoogleDriveFolder
                } else {
                    GoogleDriveFile f = new GoogleDriveFile(this, m);
                    files.add(f);
                }
            }
            mtb.release();
            mContents = files.toArray(new GoogleDriveFile[files.size()]);
        } else {
            String msg = status.getStatusMessage();
            ScribbleMainActivity.log("GoogleDriveFolder", "Get root contents failure "+msg, null);
        }
        metadataBufferResult.release();
        mPendingResult = null;
    }
}
