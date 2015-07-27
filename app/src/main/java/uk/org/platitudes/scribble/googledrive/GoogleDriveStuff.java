/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.InputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.file.FileSaver;

/**
 */
public class GoogleDriveStuff
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DriveApi.DriveContentsResult> {

    private ScribbleMainActivity mScribbleMainActivity;
    private GoogleApiClient mGoogleApiClient;
    private int mGoogleDriveConnectionFailedCount;
    public boolean mGoogleDriveConnected;
    private GoogleDriveFolder mRootGoogleDriveFolder;

    public GoogleDriveStuff (ScribbleMainActivity scribbleMainActivity) {
        mScribbleMainActivity = scribbleMainActivity;

        // https://developers.google.com/drive/android/auth#connecting_and_authorizing_the_google_drive_android_api
        mGoogleApiClient = new GoogleApiClient.Builder(mScribbleMainActivity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)         // Note, this only gives access to files created by this app.
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    public void connect () {
        if (mGoogleDriveConnectionFailedCount<2) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleDriveConnected = true;

        mRootGoogleDriveFolder = new GoogleDriveFolder(mScribbleMainActivity);

        if (mRootGoogleDriveFolder != null) {
            mRootGoogleDriveFolder.requestContents();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleDriveConnected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mGoogleDriveConnectionFailedCount++;
        // https://developers.google.com/drive/android/auth#connecting_and_authorizing_the_google_drive_android_api
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mScribbleMainActivity, ScribbleMainActivity.RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), mScribbleMainActivity, 0).show();
        }

    }

    public void readFromGoogleDrive (Intent data) {
        Bundle extras = data.getExtras();
        DriveId driveID = (DriveId) extras.get(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
        int resourceType = driveID.getResourceType();
        if (resourceType == DriveId.RESOURCE_TYPE_FOLDER) {
            // folder
        } else if (resourceType == DriveId.RESOURCE_TYPE_FILE) {
            DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, driveID);
            driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(this);
        } else {
            // not a folder or a file
        }
    }

    @Override
    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
        Status s = driveContentsResult.getStatus();
        if (s.isSuccess()) {
            DriveContents driveContents = driveContentsResult.getDriveContents();
            InputStream is = driveContents.getInputStream();
            FileSaver fs = new FileSaver(mScribbleMainActivity);
            fs.readFromInputStream(is);
        } else {
            // error
        }

    }

    public void startGoogleDriveFileSelector () {
        if (mGoogleDriveConnected) {
            // https://stackedonheap.wordpress.com/2014/09/17/google-drive-android-api-drive-scope_file-access-what-exactly-that-means/
            // https://developers.google.com/android/reference/com/google/android/gms/drive/OpenFileActivityBuilder
//        String[] mime_type = new String[]{DriveFolder.MIME_TYPE};
            String[] mime_type = new String[]{};
            OpenFileActivityBuilder ofab = Drive.DriveApi.newOpenFileActivityBuilder();
            ofab.setMimeType(mime_type);
            IntentSender intentSender  = ofab.build(mGoogleApiClient);
            try {
                mScribbleMainActivity.startIntentSenderForResult(intentSender, mScribbleMainActivity.GOOGLE_DRIVE_FILE_SELECT, null, 0, 0, 0);
                // result in onActivityResult in mMainActivity
            } catch (IntentSender.SendIntentException e) {
                ScribbleMainActivity.log("ScribbleMainActivity", "startGoogleDriveFileSelector", e);
            }
        }
    }




    public GoogleApiClient getmGoogleApiClient() {return mGoogleApiClient;}

}
