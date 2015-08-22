package digital.bauermeister.bean_gadgets;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * This service is only here to maintain the Application object in existence for as
 * long as possible, even if no UI elements are sown.
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
        Log.d(TAG, "*** Service created ***");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "*** Service destroyed ***");
        BluetoothHandler.INSTANCE.uninit();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }
}
