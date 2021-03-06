/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class FileChangeListener implements ChangeListener, ResultCallback<DriveResource.MetadataResult> {

    private final GoogleApiClient mGoogleApiClient;
    private final DriveId mDriveId;
    private final GoogleDriveFile mGoogleDriveFile;
    private boolean ignoreNextChangeEvent;

    public FileChangeListener(GoogleDriveFile f, GoogleApiClient apiClient, DriveId id) {
        mGoogleApiClient = apiClient;
        mDriveId = id;
        mGoogleDriveFile = f;

        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
        PendingResult<Status> result = driveFile.addChangeListener(mGoogleApiClient, this);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (!status.isSuccess()) {
                    ScribbleMainActivity.log ("Problem setting listener", mGoogleDriveFile.toString(), null);
                }
            }
        });
    }

    @Override
    public void onChange(ChangeEvent changeEvent) {
        ScribbleMainActivity.log ("PFileChangeListener", "Change event received: "+mGoogleDriveFile.toString(), null);
        if (ignoreNextChangeEvent) {
            // change due to our own update
            ScribbleMainActivity.log ("PFileChangeListener", "Change event ignored: "+mGoogleDriveFile.toString(), null);
            ignoreNextChangeEvent = false;
            return;
        }
        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
        PendingResult<DriveResource.MetadataResult> metRes = driveFile.getMetadata(mGoogleApiClient);
        metRes.setResultCallback(this);
    }

    public boolean isIgnoreNextChangeEvent() {return ignoreNextChangeEvent;}
    public void setIgnoreNextChangeEvent(boolean ignoreNextChangeEvent) {this.ignoreNextChangeEvent = ignoreNextChangeEvent;}

    @Override
    public void onResult(DriveResource.MetadataResult metadataResult) {
        Status s = metadataResult.getStatus();
        if (s.isSuccess()) {
            Metadata m = metadataResult.getMetadata();
            long size = m.getFileSize();
//            mGoogleDriveFile.setmSize(size);

            AsyncRead readRequest = new AsyncRead(mGoogleDriveFile, mGoogleApiClient, mDriveId);
            mGoogleDriveFile.setReadRequest(readRequest);
        }

    }
}
