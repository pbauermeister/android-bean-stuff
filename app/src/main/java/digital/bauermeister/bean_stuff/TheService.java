package digital.bauermeister.bean_stuff;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * {{{#!umlsequence
 * :
 * MA: MainActivity
 * DA: DeviceAction
 * S:  Service
 * :
 * MA ===> S  ready=reset()
 * :
 * MA ===> S  ready=scan(BeanDiscoveryListener)
 * :
 * DA ===> S  done=doAction(ConnectedAction)
 * :
 * }}}
 */

public class TheService extends Service {
    private static final String TAG = "TheService";

    @Override
    public IBinder onBind(Intent intent) {
        // No binding provided
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
