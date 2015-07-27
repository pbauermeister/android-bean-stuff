package digital.bauermeister.bean_stuff;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
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
                logTv.append(tsPrefix() + msg + "\n");
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public static void i(String tag, final String msg) {
        Log.i(tag, msg);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                summaryTv.setText(msg);
                logTv.append(tsPrefix() + msg + "\n");
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public static void clear() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                summaryTv.setText("");
                logTv.setText("");
            }
        });
    }

    private static String tsPrefix() {
        Date date = new Date();
        String dateString = new SimpleDateFormat("[yyyy-MM-dd]").format(date);
        String timeString = new SimpleDateFormat("  [HH:mm:ss] ").format(date);
        if (dateString.equals(previousDateString)) {
            return timeString;
        } else {
            previousDateString = dateString;
            return dateString + "\n" + timeString;
        }
    }
}
