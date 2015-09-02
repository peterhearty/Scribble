/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.GridButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.UndoButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.drawitem.DrawItem;
import uk.org.platitudes.scribble.googledrive.GoogleDriveStuff;
import uk.org.platitudes.scribble.io.BundleScribbleReader;
import uk.org.platitudes.scribble.io.BundleScribbleWriter;


public class ScribbleMainActivity extends Activity  {

    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 10000;

    // Button background color
    public static final int grey = Color.argb(255, 222, 222, 222);

    private ScribbleView mMainView;
    private Button mDrawToolButton;
    private ZoomButtonHandler mZoomButtonHandler;
    private DrawToolButtonHandler mDrawToolButtonHandler;
    private GoogleDriveStuff mGoogleStuff;
    private static PrintWriter logFile ;
    public Point mDisplaySize;

    public static ScribbleMainActivity mainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
//            setupLogFile();

            mainActivity = this;

            ScribbleMainActivity.log ("ScribbleMainActivity", "onCreate", null);
            mGoogleStuff = new GoogleDriveStuff((this));

            getDisplaySize();

            setContentView(R.layout.activity_scribble_main);
            mMainView = (ScribbleView) findViewById(R.id.main_content);
            mMainView.setmMainActivity(this);

            setupButtonHandlers();

            if (savedInstanceState != null) {
                readState(savedInstanceState);
            }
        } catch (Throwable e) {
            ScribbleMainActivity.log ("ScribbleMainActivity", "onCreate", e);
        }
    }

    private void getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        mDisplaySize = new Point();
        display.getSize(mDisplaySize);
        int max = Math.max(mDisplaySize.x, mDisplaySize.y);
        DrawItem.FUZZY = max/200;
        if (DrawItem.FUZZY < 10) {
            DrawItem.FUZZY = 10;
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

        ImageButton ib = (ImageButton) findViewById(R.id.grid_button);
        GridButtonHandler gbh = new GridButtonHandler(mMainView, ib);
        ib.setOnClickListener(gbh);
    }

    @Override
    protected void onDestroy() {
        ScribbleMainActivity.log ("ScribbleMainActivity", "onDestroy", null);
        super.onDestroy();
        mMainView.getDrawing().onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        ScribbleMainActivity.log ("ScribbleMainActivity", "onSaveInstanceState", null);
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

    public static void makeToast (String s) {
        if (mainActivity != null) {
            log ("makeToast", s, null);
            Context context = mainActivity.getApplicationContext();
            Toast toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private static String intTo2digit (int i) {
        String result = Integer.toString(i);
        if (result.length() == 1) {
            result = "0"+result;
        }
        return result;
    }

    private static void setupLogFile () {
        if (logFile == null) {
            File dir = Environment.getExternalStorageDirectory();
            if (dir != null && dir.canWrite()) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get (Calendar.MONTH)+1;
                int day = cal.get(Calendar.DAY_OF_MONTH);

                String logName = "scribblelog_"
                        +year+intTo2digit(month)+intTo2digit(day)+".log";
                try {
                    String dirName = dir.getCanonicalPath();
                    if (dirName.indexOf("robolectric") != -1) {
                        // a test system
                        dirName = "/tmp";
                    }
                    String pathName = dirName+ File.separator+logName;
                    FileOutputStream fos = new FileOutputStream(pathName, true);
                    logFile = new PrintWriter(fos);
                } catch (Exception e) {
                    // ignore
                }

            }
        }
    }

    public static void log (String tag, String msg, Throwable e) {
        String s = tag + " " + msg;
        if (e != null) {
            s = s + " " + e;
            Log.e(tag, msg, e);
        }
        if (logFile == null) {
            setupLogFile();
        }
        if (logFile != null) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR);
            int min = cal.get(Calendar.MINUTE);
            int secs = cal.get(Calendar.SECOND);
            int centiseconds = cal.get(Calendar.MILLISECOND)/10;

            String time = intTo2digit(hour)+":"+intTo2digit(min)+":"+intTo2digit(secs)+"."+intTo2digit(centiseconds);
            logFile.println(time+" "+s);
            if (e != null) {
                e.printStackTrace(logFile);
            }
            logFile.flush();
        }
//        makeToast(s);
    }

    @Override
    protected void onStart() {
        // https://developers.google.com/drive/android/auth#connecting_and_authorizing_the_google_drive_android_api
        ScribbleMainActivity.log("ScribbleMainActivity", "onStart", null);
        super.onStart();
        mGoogleStuff.connect();
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        ScribbleMainActivity.log("ScribbleMainActivity", "onActivityResult", null);
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleStuff.connect();
                }
                break;
        }
    }

    public GoogleDriveStuff getmGoogleStuff() {return mGoogleStuff;}
}
