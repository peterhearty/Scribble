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
import com.google.android.gms.drive.metadata.CustomPropertyKey;

import java.util.Date;
import java.util.Map;

/**
 */
public class CheckMetadata implements ResultCallback<DriveResource.MetadataResult> {

    private GoogleDriveFile mFile;
    private GoogleApiClient mGoogleApiClient;
    private DriveId mDriveId;

    public CheckMetadata (GoogleDriveFile gdf, GoogleApiClient apiClient, DriveId id) {
        mFile = gdf;
        mGoogleApiClient = apiClient;
        mDriveId = id;

        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
        PendingResult<DriveResource.MetadataResult> result = driveFile.getMetadata(apiClient);
        result.setResultCallback(this);
    }

    @Override
    public void onResult(DriveResource.MetadataResult metadataResult) {
        Status status = metadataResult.getStatus();
        if (!status.isSuccess()) {
            return;
        }

        Metadata m = metadataResult.getMetadata();
        long modifiedDate = m.getModifiedDate().getTime();
        long prevModDate = mFile.getLastModifiedDate();
        // DOESN'T WORK - DATE DOESN'T SEEMS TO GET UPDATED
        if (modifiedDate > prevModDate) {
            // file has changed
            mFile.setLastModifiedDate(modifiedDate);
            mFile.forceReRead();
        }
    }
}
