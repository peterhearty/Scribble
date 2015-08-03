/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.InputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class AsyncRead implements ResultCallback<DriveApi.DriveContentsResult> {

    private GoogleDriveFile mFile;
    private GoogleApiClient mGoogleApiClient;
    private DriveId mDriveId;

    public AsyncRead (GoogleDriveFile gdf, GoogleApiClient apiClient, DriveId id) {
        mFile = gdf;
        mGoogleApiClient = apiClient;
        mDriveId = id;

        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
        driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(this);
    }

    @Override
    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
        Status s = driveContentsResult.getStatus();
        if (!s.isSuccess()) {
            // error
            return;
        }
        DriveContents driveContents = driveContentsResult.getDriveContents();

        int size = (int) mFile.length();
        byte[] contents = new byte[size];
        try {
            InputStream is = driveContents.getInputStream();
            int available = is.available();
            if (available != size) {
                if (available > size) {
                    contents = new byte[available];
                    size = available;
                }
            }
            is.read(contents, 0, size);
            is.close();
            driveContents.discard(mGoogleApiClient);

            // Check for contents change. Only modify Drawing if contents have changed.
            // This is to prevent the view abrubtly changing in the
            // middle of an extended move, pan, or zoom operation.
            byte[] currentContents = mFile.getmFileContents();
            boolean updateContents = false;
            if (currentContents == null) {
                updateContents = true;
            } else {
                if (currentContents.length != contents.length) {
                    updateContents = true;
                } else {
                    // old and new same length, check contents
                    // Scribble files have a changeByte near the start that changes on each write.
                    // This should prevent the need for comparing the full contents every time
                    // for a change.
                    for (int i=0; i<contents.length; i++) {
                        if (currentContents[i] != contents[i]) {
                            // contents have changed
                            updateContents = true;
                            break;
                        }
                    }
                }
            }

            if (updateContents) {
                mFile.setmFileContents(contents);
                mFile.fileHasChanged = true;
            }

            mFile.setReadRequest(null);
        } catch (Exception e) {
            ScribbleMainActivity.log("GoogleDriveFile", "getInputStream", e);
        }
    }

}
