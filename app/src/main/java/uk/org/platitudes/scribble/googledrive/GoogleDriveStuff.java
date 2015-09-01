/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import android.app.Dialog;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import uk.org.platitudes.scribble.Drawing;
import uk.org.platitudes.scribble.ScribbleMainActivity;

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
        ScribbleMainActivity.log("GoogleDriveStuff", "constructor", null);
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
        ScribbleMainActivity.log("GoogleDriveStuff", "setFileToReadWhenReady: "+s, null);
        fileToReadWhenReady = GoogleDriveFolder.extractGoogleDriveFilename(s);
    }

    public void connect () {
        ScribbleMainActivity.log("GoogleDriveStuff", "connect", null);
        if (mGoogleDriveConnectionFailedCount<2 && !mGoogleDriveConnected) {
            ScribbleMainActivity.log("GoogleDriveStuff", "trying to connect to google drive", null);
            mGoogleApiClient.connect();
        }
    }

    public void checkFileLoadPending (GoogleDriveFile f) {
        ScribbleMainActivity.log("GoogleDriveStuff", "checkFileLoadPending: "+f.toString(), null);
        if (fileToReadWhenReady == null) return;

        if (f.getName().equals(fileToReadWhenReady)) {
            ScribbleMainActivity.log("GoogleDriveStuff", "file "+f.toString()+" ready to read", null);
            fileToReadWhenReady = null;
            Drawing drawing = mScribbleMainActivity.getmMainView().getDrawing();
            drawing.setmCurrentlyOpenFile(f);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        ScribbleMainActivity.log("GoogleDriveStuff", "onConnected", null);
        mGoogleDriveConnected = true;
        mRootGoogleDriveFolder = new GoogleDriveFolder(mScribbleMainActivity);
    }

    @Override
    public void onConnectionSuspended(int i) {
        ScribbleMainActivity.log("GoogleDriveStuff", "onConnectionSuspended", null);
        mGoogleDriveConnected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mGoogleDriveConnectionFailedCount++;
        ScribbleMainActivity.log("GoogleDriveStuff", "connection failed, failure count="+mGoogleDriveConnectionFailedCount+" result = "+connectionResult, null);
        // https://developers.google.com/drive/android/auth#connecting_and_authorizing_the_google_drive_android_api
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mScribbleMainActivity, ScribbleMainActivity.RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            int errCode = connectionResult.getErrorCode();
            try {
                Dialog d = GooglePlayServicesUtil.getErrorDialog(errCode, mScribbleMainActivity, 0);
                d.show();
            } catch (Exception e) {
                ScribbleMainActivity.log("GoogleDriveStuff", "onConnectionFailed", e);
            }
        }

    }

    public GoogleApiClient getmGoogleApiClient() {return mGoogleApiClient;}
    public GoogleDriveFolder getmRootGoogleDriveFolder() {return mRootGoogleDriveFolder;}


}
