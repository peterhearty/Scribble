package drawItem;

import android.app.Application;
import android.content.Intent;
import android.content.ServiceConnection;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * This extends the Robolectric shadow app in order to be able to set a breakpoint
 * on one of its methods. Something like the following is needed to use this class.
 * Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class, shadows=ShadowApp.class)
 */
@Implements(Application.class)
public class ShadowApp extends ShadowApplication {

    @Implementation
    public boolean bindService(final Intent intent, final ServiceConnection serviceConnection, int i) {
        ScribbleMainActivity.log("Bind service: ", intent.toString(), null);
//        boolean result = super.bindService(intent, serviceConnection, i);
//        return result;
        return true;
    }
}
