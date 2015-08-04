package digital.bauermeister.bean_stuff;

import android.app.Application;

/**
 * Created by pascal on 7/22/15.
 * <p/>
 * Here goes the initializations.
 */
public class TheApplication extends Application {
    public void onCreate() {
        super.onCreate();

        DeviceDatabase.INSTANCE.init(this); // init devices database
    }
}
