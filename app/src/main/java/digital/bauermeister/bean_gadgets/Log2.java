package digital.bauermeister.bean_gadgets;

import android.app.Activity;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logging facility, feeding both the system logs, and the log panel.
 * <p/>
 * Created by pascal on 7/26/15.
 */
public class Log2 {
    private static TextView summaryTv;
    private static TextView logTv;
    private static ScrollView scrollView;
    private static Activity activity;
    private static String previousDateString;

    public static void init(Activity activity, TextView summary, TextView log, ScrollView scrollView) {
        Log2.activity = activity;
        Log2.summaryTv = summary;
        Log2.logTv = log;
        Log2.scrollView = scrollView;
    }

    public static void d(String tag, final String msg) {
        Log.d(tag, msg);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                add(msg);
            }
        });
    }

    public static void i(String tag, final String msg) {
        Log.i(tag, msg);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                summaryTv.setText(msg);
                add(msg);
            }
        });
    }

    public static void clear() {
        previousDateString = null;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                summaryTv.setText("");
                logTv.setText("");
            }
        });
    }

    private static void add(final String msg) {
        Date date = new Date();
        String dateString = new SimpleDateFormat("[yyyy-MM-dd]").format(date);
        String tsPostfix = dateString.equals(previousDateString) ? "" : "\n" + dateString;
        previousDateString = dateString;
        String tsPrefix = new SimpleDateFormat("[HH:mm:ss] ").format(date);
        logTv.setText(tsPrefix + msg + tsPostfix + "\n" + logTv.getText());
    }
}
