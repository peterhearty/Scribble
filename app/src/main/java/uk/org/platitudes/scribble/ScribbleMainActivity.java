package uk.org.platitudes.scribble;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class ScribbleMainActivity extends Activity implements View.OnClickListener {

    private View mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scribble_main);
        mMainView = findViewById(R.id.main_content);
        // Consider making the default UI flags
        // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION as in
        // http://developer.sonymobile.com/knowledge-base/tutorials/android_tutorial/how-to-provide-your-app-users-with-maximum-screen-estate-tutorial/
        mMainView.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scribble_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int h = mMainView.getHeight();
        System.out.println(h);
        int visibility = mMainView.getSystemUiVisibility();
        // Note that the visibility might not be zero if other flags have been set.
        // should really check which flags are set.
        if (visibility==0) {
            int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            mMainView.setSystemUiVisibility(flags);
        } else {
            mMainView.setSystemUiVisibility(0);
        }
    }
}
