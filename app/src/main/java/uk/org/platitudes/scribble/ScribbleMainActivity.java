/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import uk.org.platitudes.scribble.buttonhandler.DrawToolButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.UndoButtonHandler;
import uk.org.platitudes.scribble.buttonhandler.ZoomButtonHandler;


public class ScribbleMainActivity extends Activity {

    private ScribbleView mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scribble_main);
        mMainView = (ScribbleView) findViewById(R.id.main_content);

        Button b = (Button) findViewById(R.id.more_button);
        b.setOnClickListener(new MoreButtonHandler(b));

        b = (Button) findViewById(R.id.undo_button);
        b.setOnClickListener(new UndoButtonHandler(mMainView, b));

        b = (Button) findViewById(R.id.drawtool_button);
        DrawToolButtonHandler dtbh = new DrawToolButtonHandler(b);
        b.setOnClickListener(dtbh);
        mMainView.setmDrawToolButtonHandler(dtbh);

        b = (Button) findViewById(R.id.zoom_in_button);
        b.setOnClickListener(new ZoomButtonHandler(mMainView, b));

    }

}
