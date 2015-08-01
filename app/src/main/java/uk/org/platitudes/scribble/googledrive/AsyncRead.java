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
import com.google.android.gms.drive.events.ChangeListener;

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
            is.read(contents, 0, size);
            is.close();
            driveContents.discard(mGoogleApiClient);
            mFile.setmFileContents(contents);
            //TODO - getting occasional EOFEception below
            ScribbleMainActivity.mainActivity.getmGoogleStuff().checkFileLoadPending(mFile);

            fileChangeListener listener = mFile.getChangeListener();
            if (listener == null) {
                // First time we've read the file contents, listen for changes
                listener = new fileChangeListener(mFile, mGoogleApiClient, mDriveId);
                DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
                driveFile.addChangeListener(mGoogleApiClient, listener);
                mFile.setChangeListener(listener);
            }

            mFile.setReadRequest(null);
        } catch (Exception e) {
            ScribbleMainActivity.log("GoogleDriveFile", "getInputStream", e);
        }
    }

}
