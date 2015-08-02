/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.UndoButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFile;
import uk.org.platitudes.scribble.googledrive.GoogleDriveFolder;
import uk.org.platitudes.scribble.googledrive.GoogleDriveStuff;
import uk.org.platitudes.scribble.io.BundleScribbleReader;
import uk.org.platitudes.scribble.io.BundleScribbleWriter;
import uk.org.platitudes.scribble.io.FileScribbleReader;
import uk.org.platitudes.scribble.io.ScribbleReader;


public class ScribbleMainActivity extends Activity  {

    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 10000;
    public static final int GOOGLE_DRIVE_FILE_SELECT = 10020;
    public static final int GOOGLE_DRIVE_FILE_CREATE = 10030;

    private ScribbleView mMainView;
    private Button mDrawToolButton;
    private ZoomButtonHandler mZoomButtonHandler;
    private DrawToolButtonHandler mDrawToolButtonHandler;
    private GoogleDriveStuff mGoogleStuff;
//    private File mCurrentlyOpenFile;

    public static ScribbleMainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = this;

        mGoogleStuff = new GoogleDriveStuff((this));

        setContentView(R.layout.activity_scribble_main);
        mMainView = (ScribbleView) findViewById(R.id.main_content);
        mMainView.setmMainActivity(this);

        setupButtonHandlers();

        if (savedInstanceState != null) {
            // only reads button states, zoom and offset
            // data is read in from file
            readState(savedInstanceState);
        }

    }

    private void setupButtonHandlers () {
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainView.getDrawing().onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putFloat("zoom", ZoomButtonHandler.getsZoom());
        outState.putCharSequence("drawTool", mDrawToolButton.getText());
        PointF mainViewScrollOffset = mMainView.getmScrollOffset();
        outState.putFloat("offset_X", mainViewScrollOffset.x);
        outState.putFloat("offset_Y", mainViewScrollOffset.y);

        BundleScribbleWriter bsw = new BundleScribbleWriter(this, outState);
        bsw.write();
    }

    private void readState (Bundle savedInstanceState) {
        mZoomButtonHandler.setsZoom(savedInstanceState.getFloat("zoom"));
        mDrawToolButton.setText(savedInstanceState.getCharSequence("drawTool", "free"));
        float x = savedInstanceState.getFloat("offset_X");
        float y = savedInstanceState.getFloat("offset_Y");
        mMainView.setmScrollOffset(x, y);

        BundleScribbleReader bsr = new BundleScribbleReader(this, savedInstanceState);
        bsr.read(mMainView.getDrawing());
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
        AlertDialog.Builder alert = new AlertDialog.Builder(ScribbleMainActivity.mainActivity);
        AlertDialog dialog = alert.setMessage(s).setCancelable(true).create();
        dialog.show();
//        makeToast(s);
    }

    @Override
    protected void onStart() {
        // https://developers.google.com/drive/android/auth#connecting_and_authorizing_the_google_drive_android_api
        super.onStart();
        mGoogleStuff.connect();
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleStuff.connect();
                }
                break;
            case GOOGLE_DRIVE_FILE_SELECT:
                if (resultCode == RESULT_OK) {
                    mGoogleStuff.readFromGoogleDrive(data);
                }
                break;
            case GOOGLE_DRIVE_FILE_CREATE:
                if (resultCode == RESULT_OK) {
                    mGoogleStuff.writeToGoogleDrive(data);
                }
                break;
        }
    }

    public GoogleDriveStuff getmGoogleStuff() {return mGoogleStuff;}
}
