package digital.bauermeister.bean_gadgets;

import android.app.Application;
import android.content.Intent;

/**
 * Here goes the app's initializations.
 * <p/>
 * Created by pascal on 7/22/15.
 */
public class TheApplication extends Application {
    public void onCreate() {
        super.onCreate();
        BluetoothHandler.INSTANCE.init(this);
        DeviceDatabase.INSTANCE.init(this); // init devices database
        startService(new Intent(this, TheService.class));
    }
}
