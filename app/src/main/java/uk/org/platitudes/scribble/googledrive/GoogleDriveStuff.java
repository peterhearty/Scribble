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
import com.google.android.gms.drive.CreateFileActivityBuilder;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.io.FileScribbleReader;
import uk.org.platitudes.scribble.io.FileScribbleWriter;

/**
 */
public class GoogleDriveStuff implements GoogleApiClient.ConnectionCallbacks,
                                         GoogleApiClient.OnConnectionFailedListener {

    private ScribbleMainActivity mScribbleMainActivity;
    private GoogleApiClient mGoogleApiClient;
    private int mGoogleDriveConnectionFailedCount;
    public boolean mGoogleDriveConnected;
    private GoogleDriveFolder mRootGoogleDriveFolder;
    private String fileToReadWhenReady;

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

    public void setFileToReadWhenReady (String s) {
        fileToReadWhenReady = GoogleDriveFolder.extractGoogleDriveFilename(s);
    }

    public void connect () {
        if (mGoogleDriveConnectionFailedCount<2) {
            mGoogleApiClient.connect();
        }
    }

    public void checkFileLoadPending (GoogleDriveFile f) {
        if (fileToReadWhenReady == null) return;

        if (f.getName().equals(fileToReadWhenReady)) {
            fileToReadWhenReady = null;
            Drawing drawing = mScribbleMainActivity.getmMainView().getDrawing();
//            FileScribbleReader fsr = new FileScribbleReader(mScribbleMainActivity, f);
//            fsr.read(drawing);
            drawing.setmCurrentlyOpenFile(f);
            f.forceReRead(); // this just speeds up the firat read from the background thread
//            mScribbleMainActivity.getmMainView().invalidate();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleDriveConnected = true;
        mRootGoogleDriveFolder = new GoogleDriveFolder(mScribbleMainActivity);
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
        if (resourceType == DriveId.RESOURCE_TYPE_FILE) {

            final ResultCallback<DriveApi.DriveContentsResult> readCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    Status s = driveContentsResult.getStatus();
                    if (!s.isSuccess()) {
                        // error
                        return;
                    }

                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    InputStream is = driveContents.getInputStream();
                    FileScribbleReader fsr = new FileScribbleReader(mScribbleMainActivity, null);
                    fsr.readFromInputStream(is, mScribbleMainActivity.getmMainView().getDrawing());
                    try {
                        is.close();
                        driveContents.discard(mGoogleApiClient);
                    } catch (IOException e) {
                        ScribbleMainActivity.log("GoogleDriveStuff", "close file for reading", e);
                    }
                }
            };

            DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, driveID);
            driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(readCallback);
        }
    }

    public void writeToGoogleDrive (Intent data) {
        Bundle extras = data.getExtras();
        DriveId driveID = (DriveId) extras.get(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
        int resourceType = driveID.getResourceType();
        if (resourceType == DriveId.RESOURCE_TYPE_FILE) {

            final ResultCallback<DriveApi.DriveContentsResult> writeCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    Status s = driveContentsResult.getStatus();
                    if (!s.isSuccess()) {
                        // error
                        return;
                    }

                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    OutputStream os = driveContents.getOutputStream();
                    FileScribbleWriter fsw = new FileScribbleWriter(mScribbleMainActivity);
                    fsw.writeToOutputStream(os);
                    try {
                        os.close();
                    } catch (IOException e) {
                        ScribbleMainActivity.log("GoogleDriveStuff", "close file for writing", e);
                    }
                    driveContents.commit(mGoogleApiClient, null);
                }
            };

            DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, driveID);
            driveFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                    .setResultCallback(writeCallback);
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
                mScribbleMainActivity.startIntentSenderForResult(intentSender, ScribbleMainActivity.GOOGLE_DRIVE_FILE_SELECT, null, 0, 0, 0);
                // result in onActivityResult in mMainActivity
            } catch (IntentSender.SendIntentException e) {
                ScribbleMainActivity.log("GoogleDriveStuff", "startGoogleDriveFileSelector", e);
            }
        }
    }

    public void startGoogleDriveFileCreator () {
        if (mGoogleDriveConnected) {

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle("")
                    .setMimeType("").build();

            CreateFileActivityBuilder cfab = Drive.DriveApi.newCreateFileActivityBuilder();
            cfab.setInitialMetadata(changeSet);
            cfab.setInitialDriveContents(null);

            IntentSender intentSender  = cfab.build(mGoogleApiClient);
            try {
                mScribbleMainActivity.startIntentSenderForResult(intentSender, ScribbleMainActivity.GOOGLE_DRIVE_FILE_CREATE, null, 0, 0, 0);
                // result in onActivityResult in mMainActivity
            } catch (IntentSender.SendIntentException e) {
                ScribbleMainActivity.log("GoogleDriveStuff", "startGoogleDriveFileSelector", e);
            }
        }
    }



    public GoogleApiClient getmGoogleApiClient() {return mGoogleApiClient;}
    public GoogleDriveFolder getmRootGoogleDriveFolder() {return mRootGoogleDriveFolder;}


}
