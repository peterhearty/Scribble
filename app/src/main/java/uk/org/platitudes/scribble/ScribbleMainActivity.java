/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.UndoButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;


public class ScribbleMainActivity extends Activity {

    private ScribbleView mMainView;
    public static ScribbleMainActivity mainActivity;
    public static final int FILE_FORMAT_VERSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = this;

        setContentView(R.layout.activity_scribble_main);
        mMainView = (ScribbleView) findViewById(R.id.main_content);

        Button b = (Button) findViewById(R.id.more_button);
        b.setOnClickListener(new MoreButtonHandler(b, this));

        b = (Button) findViewById(R.id.undo_button);
        b.setOnClickListener(new UndoButtonHandler(mMainView, b));

        b = (Button) findViewById(R.id.drawtool_button);
        DrawToolButtonHandler dtbh = new DrawToolButtonHandler(b);
        b.setOnClickListener(dtbh);
        mMainView.setmDrawToolButtonHandler(dtbh);

        b = (Button) findViewById(R.id.zoom_in_button);
        b.setOnClickListener(new ZoomButtonHandler(mMainView, b));

        if (savedInstanceState != null) {
            readState(savedInstanceState);
        }

    }

    public static void makeToast (String s) {
        Context context = mainActivity.getApplicationContext();
        Toast toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
        toast.show();

    }

    public void saveEverything (DataOutputStream dos) throws IOException {
        dos.writeInt(FILE_FORMAT_VERSION);
        // TODO have to save and restore button states
        mMainView.saveEverything(dos, FILE_FORMAT_VERSION);
    }

    public void readeverything (DataInputStream dis) throws IOException {
        int fileFormatVersion = dis.readInt();
        mMainView.readEverything(dis, fileFormatVersion);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Used to save dynamic data, e.g. when the screen is turned round.
        // Different from onPause, which gets called when a process is being put in the background
        // and might not come back.
        // "In general onSaveInstanceState(Bundle) is used to save per-instance state in the
        // activity and this method is used to store global persistent data (in content providers, files, etc.)"

        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            saveEverything(dos);
            byte[] bytes = baos.toByteArray();
            outState.putByteArray("everything", bytes);
            dos.close();
            baos.close();
        } catch (Exception e) {
            makeToast("onSaveInstanceState "+e);
        }
    }

    private void readState (Bundle savedInstanceState) {
        byte[] bytes = savedInstanceState.getByteArray("everything");
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        try {
            readeverything(dis);
        } catch (Exception e) {
            makeToast("readState " + e);
        }
    }

    public ScribbleView getmMainView() {return mMainView;}


}
