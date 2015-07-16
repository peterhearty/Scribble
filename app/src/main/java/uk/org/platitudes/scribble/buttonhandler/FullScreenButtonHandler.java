/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.buttonhandler;

import android.view.View;

public class FullScreenButtonHandler implements View.OnClickListener{

    // Consider making the default UI flags
    // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION as in
    // http://developer.sonymobile.com/knowledge-base/tutorials/android_tutorial/how-to-provide-your-app-users-with-maximum-screen-estate-tutorial/

    @Override
    public void onClick(View v) {
        int visibility = v.getSystemUiVisibility();
        // Note that the visibility might not be zero if other flags have been set.
        // should really check which flags are set.
        if (visibility==0) {
            int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            v.setSystemUiVisibility(flags);
        } else {
            v.setSystemUiVisibility(0);
        }

    }
}
