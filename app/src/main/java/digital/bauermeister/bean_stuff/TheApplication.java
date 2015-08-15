package digital.bauermeister.bean_stuff;

import android.app.Application;
import android.content.Intent;

/**
 * Created by pascal on 7/22/15.
 * <p/>
 * Here goes the initializations.
 */
public class TheApplication extends Application {
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, TheService.class));

        DeviceDatabase.INSTANCE.init(this); // init devices database
    }
}
