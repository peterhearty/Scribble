/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import android.os.Environment;
import android.support.annotation.NonNull;

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
import java.util.Iterator;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class GoogleDriveFolder extends File implements ResultCallback<DriveApi.MetadataBufferResult> {

    private DriveFolder mDriveFolder;
    private GoogleApiClient mGoogleApiClient;
    private File[] contents;
    private PendingResult<DriveApi.MetadataBufferResult> mPendingResult;

    public GoogleDriveFolder(ScribbleMainActivity activity) {
        // Need to cal a super of some kind.
        super ("/");
        mGoogleApiClient = activity.getmGoogleStuff().getmGoogleApiClient();
        mDriveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        contents = new File[0];
    }

    public boolean isDirectory () {return true;}
    public boolean canRead () {return true;}
    public boolean exists () {return true;}
    public File[] listFiles () {
        return contents;
    }

    @NonNull
    @Override
    public String getCanonicalPath() throws IOException {
        return "GoogleDrive /";
    }

    public void requestContents () {
        // Start off an async request to get the children.
        mPendingResult = mDriveFolder.listChildren(mGoogleApiClient);
        mPendingResult.setResultCallback(this);
    }

    @Override
    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
        Status status = metadataBufferResult.getStatus();
        if (status.isSuccess()) {
            MetadataBuffer mtb = metadataBufferResult.getMetadataBuffer();
            int count = mtb.getCount();
            ArrayList<File> files = new ArrayList<>(count);
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
            contents = files.toArray(new File[files.size()]);
        } else {
            String msg = status.getStatusMessage();
            ScribbleMainActivity.log("GoogleDriveFolder", "Get root contents failure "+msg, null);
        }
        metadataBufferResult.release();
        mPendingResult = null;
    }
}
