/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Environment;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.ItemList;
import uk.org.platitudes.scribble.file.FileChooser;

public class MoreButtonHandler implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, View.OnTouchListener {

    private Button mMoreButton;
    private ScribbleMainActivity mActivity;

    public MoreButtonHandler (Button b, ScribbleMainActivity sma) {
        mMoreButton = b;
        mActivity = sma;
        mMoreButton.setBackgroundColor(ScribbleMainActivity.grey);
        mMoreButton.setOnTouchListener(this);
    }


    private void createMenu () {
        PopupMenu popup = new PopupMenu(mMoreButton.getContext(), mMoreButton);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.more_actions_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    /**
     * A simple color change on being pressed.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            mMoreButton.setBackgroundColor(Color.LTGRAY);
        }
        if (event.getAction() == MotionEvent.ACTION_UP){
            mMoreButton.setBackgroundColor(ScribbleMainActivity.grey);
        }
        return false;
    }



    @Override
    public void onClick(View v) {
        createMenu();
    }

    private void overrideVisibilitychanges () {
        // Consider making the default UI flags
        // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION as in
        // http://developer.sonymobile.com/knowledge-base/tutorials/android_tutorial/how-to-provide-your-app-users-with-maximum-screen-estate-tutorial/

        int visibility = mMoreButton.getSystemUiVisibility();
        // Note that the visibility might not be zero if other flags have been set.
        // should really check which flags are set.
        // We have to put the visibility checks in, otherwise setSystemUiVisibility
        // generates another call to overrideVisibilitychanges and this goes on forever.
        int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        mMoreButton.setSystemUiVisibility(flags);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        CharSequence menuTitle = item.getTitle();
        if (menuTitle.equals("fullscreen")) {
            overrideVisibilitychanges ();
        } else if (menuTitle.equals("open")) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setParameters(mActivity, false);
            fileChooser.show(ScribbleMainActivity.mainActivity.getFragmentManager(), "");
            mActivity.getmMainView().invalidate();
        } else if (menuTitle.equals("save")) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setParameters(mActivity, true);
            fileChooser.show(ScribbleMainActivity.mainActivity.getFragmentManager(), "");
        } else if (menuTitle.equals("export")) {
            // Get the bounds of the draw items
            ScribbleView view = mActivity.getmMainView();
            ItemList drawItemList = view.getmDrawItems();
            RectF bounds = drawItemList.getBounds();
            int width = (int) (bounds.right - bounds.left);
            int height = (int) (bounds.bottom - bounds.top);

            // Createa bitmap and fill it with a white background
            Bitmap b = Bitmap.createBitmap(width+40, height+40, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            c.drawColor(Color.WHITE, PorterDuff.Mode.ADD);

            // save the current zoom and offset then set new values
            float oldZoom = ZoomButtonHandler.getsZoom();
            PointF offset = view.getmScrollOffset();
            float x = offset.x;
            float y = offset.y;
            mActivity.getmZoomButtonHandler().setsZoom(1.0f);
            view.setmScrollOffset(bounds.left-20, bounds.top-20);

            // draw the items onto the bitmap
            drawItemList.onDraw(c);

            // restore old zoom and offset
            mActivity.getmZoomButtonHandler().setsZoom(oldZoom);
            view.setmScrollOffset(x, y);


//            view.setDrawingCacheEnabled(true);
//            view.setBackgroundColor(Color.WHITE);       // Don't know why I have to do this bit, but it comes it black if i don't
//            view.buildDrawingCache(true);
//            Bitmap bitmap = view.getDrawingCache();

            // Construct a file name
            String currentFilename = view.getDrawing().getmCurrentlyOpenFile().getName();
            int lastDot = currentFilename.lastIndexOf('.');
            if (lastDot > 0) {
                // n.b. ignores files of form ".something"
                // strip off extension
                currentFilename = currentFilename.substring(0, lastDot);
            }
            currentFilename += ".png";

            // Setup a destination drawing file
            File dir = Environment.getExternalStorageDirectory();
            String filePath = dir.getPath()+File.separator+currentFilename;
            File destFile = new File(filePath);

            // and save the file
            try {
                FileOutputStream fos = new FileOutputStream(destFile);
                boolean result = b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                if (result) {
                    ScribbleMainActivity.log ("Exported to ", filePath, null);
                } else {
                    ScribbleMainActivity.log ("Failed to export to ", filePath, null);
                }
            } catch (IOException e) {
                ScribbleMainActivity.log ("Error exported to ", filePath, e);
            }

        } else if (menuTitle.equals("exit")) {
            mActivity.finish();
        } else if (menuTitle.equals("clear")) {
            mActivity.getmMainView().clear();
        } else if (menuTitle.equals("clear undos")) {
            mActivity.getmMainView().getDrawing().clearUndos();
        } else if (menuTitle.equals("about")) {
            mActivity.getmMainView().getDrawing().about();
        }

        return true;
    }

}
