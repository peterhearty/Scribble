/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.UndoButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.file.FileSaver;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFolder;


public class ScribbleMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 10000;
    private static final int GOOGLE_DRIVE_FILE_SELECT = 10020;

    private ScribbleView mMainView;
    private Button mDrawToolButton;
    private ZoomButtonHandler mZoomButtonHandler;
    private DrawToolButtonHandler mDrawToolButtonHandler;
    public static ScribbleMainActivity mainActivity;
    private GoogleApiClient mGoogleApiClient;
    private int mGoogleDriveConnectionFailedCount;
    public boolean mGoogleDriveConnected;
    private GoogleDriveFolder mRootGoogleDriveFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = this;

        // https://developers.google.com/drive/android/auth#connecting_and_authorizing_the_google_drive_android_api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)         // Note, this only gives access to files created by this app.
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_scribble_main);
        mMainView = (ScribbleView) findViewById(R.id.main_content);
        mMainView.setmMainActivity(this);

        Button b = (Button) findViewById(R.id.more_button);
        b.setOnClickListener(new MoreButtonHandler(b, this));

        b = (Button) findViewById(R.id.undo_button);
        b.setOnClickListener(new UndoButtonHandler(mMainView, b));

        mDrawToolButton = (Button) findViewById(R.id.drawtool_button);
        mDrawToolButtonHandler = new DrawToolButtonHandler(mDrawToolButton);
        mDrawToolButton.setOnClickListener(mDrawToolButtonHandler);

        b = (Button) findViewById(R.id.zoom_in_button);
        mZoomButtonHandler = new ZoomButtonHandler(mMainView, b);
        b.setOnClickListener(mZoomButtonHandler);

        if (savedInstanceState != null) {
            readState(savedInstanceState);
        } else {
            FileSaver fs = new FileSaver(this);
            fs.readFromDefaultFile();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putFloat("zoom", ZoomButtonHandler.getsZoom());
        outState.putCharSequence("drawTool", mDrawToolButton.getText());
        PointF mainViewScrollOffset = mMainView.getmScrollOffset();
        outState.putFloat("offset_X", mainViewScrollOffset.x);
        outState.putFloat("offset_Y", mainViewScrollOffset.y);

        FileSaver fs = new FileSaver(this);
        fs.writeToBundle(outState);
    }

    private void readState (Bundle savedInstanceState) {
        mZoomButtonHandler.setsZoom(savedInstanceState.getFloat("zoom"));
        mDrawToolButton.setText(savedInstanceState.getCharSequence("drawTool", "free"));
        float x = savedInstanceState.getFloat("offset_X");
        float y = savedInstanceState.getFloat("offset_Y");
        mMainView.setmScrollOffset(x, y);

        FileSaver fs = new FileSaver(this);
        fs.readFromBundle(savedInstanceState);
    }

    public ScribbleView getmMainView() {return mMainView;}
    public ZoomButtonHandler getmZoomButtonHandler() {return mZoomButtonHandler;}
    public DrawToolButtonHandler getmDrawToolButtonHandler() {return mDrawToolButtonHandler;}

    private static void makeToast (String s) {
        if (mainActivity != null) {
            Context context = mainActivity.getApplicationContext();
            Toast toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static void log (String tag, String msg, Exception e) {
        String s = tag + " " + msg;
        if (e != null) {
            s = s + " " + e;
            Log.e(tag, msg, e);
        }
        makeToast(s);
    }

    @Override
    protected void onStart() {
        // https://developers.google.com/drive/android/auth#connecting_and_authorizing_the_google_drive_android_api
        super.onStart();
        mGoogleApiClient.connect();
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
                startIntentSenderForResult(intentSender, GOOGLE_DRIVE_FILE_SELECT, null, 0, 0, 0);
                // result in onActivityResult below
            } catch (IntentSender.SendIntentException e) {
                log("ScribbleMainActivity", "startGoogleDriveFileSelector", e);
            }
        }


    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleDriveConnected = true;

        mRootGoogleDriveFolder = new GoogleDriveFolder(this);

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
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK && mGoogleDriveConnectionFailedCount<2) {
                    mGoogleApiClient.connect();
                }
                break;
            case GOOGLE_DRIVE_FILE_SELECT:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    DriveId driveID = (DriveId) extras.get(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, driveID);
                    String s = driveID.encodeToString();
                    String s1 = driveID.toInvariantString();
                    int resourceType = driveID.getResourceType();
                    if (resourceType == DriveId.RESOURCE_TYPE_FOLDER) {

                    } else if (resourceType == DriveId.RESOURCE_TYPE_FILE) {

                    } else {

                    }
                }
                break;
        }
    }


    public GoogleApiClient getmGoogleApiClient() {return mGoogleApiClient;}


}
