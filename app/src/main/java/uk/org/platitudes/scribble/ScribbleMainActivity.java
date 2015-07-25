/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.UndoButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;
import uk.org.platitudes.scribble.file.FileSaver;


public class ScribbleMainActivity extends Activity {

    private ScribbleView mMainView;
    private Button mDrawToolButton;
    private ZoomButtonHandler mZoomButtonHandler;
    private DrawToolButtonHandler mDrawToolButtonHandler;
    public static ScribbleMainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = this;

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
        Context context = mainActivity.getApplicationContext();
        Toast toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void log (String tag, String msg, Exception e) {
        String s = tag + " " + msg;
        if (e != null) {
            s = s + " " + e;
            Log.e(tag, msg, e);
        }
        makeToast(s);
    }


}
