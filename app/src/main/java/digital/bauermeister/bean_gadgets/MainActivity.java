package digital.bauermeister.bean_gadgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import de.greenrobot.event.EventBus;
import digital.bauermeister.bean_gadgets.events.BeanDiscoveredEvent;
import digital.bauermeister.bean_gadgets.events.BtResetDoneEvent;
import digital.bauermeister.bean_gadgets.events.BtResetRequestEvent;
import digital.bauermeister.bean_gadgets.events.ScanFinishedEvent;
import digital.bauermeister.bean_gadgets.events.ScanRequestEvent;


/**
 * The principal and so far only activity.
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private TextView logTv;
    private TextView lastLogTv;
    private ScrollView logScroll;
    private View logDetails;

    private ListView listView;
    private TextView emptyTv;
    private DeviceListAdapter adapter;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "*** Activity created ***");

        EventBus.getDefault().register(this);

        // init view
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        emptyTv = (TextView) findViewById(R.id.emptyTv);

        logTv = (TextView) findViewById(R.id.logTv);
        lastLogTv = (TextView) findViewById(R.id.lastLogTv);
        logScroll = (ScrollView) findViewById(R.id.logScroll);
        logDetails = findViewById(R.id.logDetails);
        Log2.init(this, lastLogTv, logTv, logScroll);

        // no BLE?
        if (!BluetoothHandler.INSTANCE.hasBle()) {
            new AlertDialog.Builder(this)
                    .setTitle("BLE not found")
                    .setMessage("This app needs an Android device with Bluetooth Low-Energy (BLE).")
                    .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    }).show();
            return;
        }

        // init list view
        adapter = new DeviceListAdapter(this);
        listView.setAdapter(adapter);
        refreshView();

        // initial scan
        listView.post(new Runnable() {
            @Override
            public void run() {
                startScan();
            }
        });

        // init logs panel
        SlidingUpPanelLayout panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        logDetails.setVisibility(View.GONE);
        panel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                logDetails.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelCollapsed(View view) {
                logDetails.setVisibility(View.GONE);
                refreshView();
            }

            @Override
            public void onPanelExpanded(View view) {
                logScroll.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelAnchored(View view) {
                logScroll.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelHidden(View view) {
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_bt_scan:
                startScan();
                return true;

            case R.id.action_bt_reset:
                resetBt();
                return true;

            case R.id.action_about:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/pbauermeister/android-bean-stuff"));
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshView() {
        boolean isScanning = BluetoothHandler.INSTANCE.isScanning();
        adapter.notifyDataSetChanged();
        emptyTv.setVisibility(adapter.isEmpty() && !isScanning ? View.VISIBLE : View.GONE);
        listView.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /*
     * BLE
     */

    private void resetBt() {
        EventBus.getDefault().post(new BtResetRequestEvent());
        menu.findItem(R.id.action_bt_scan).setEnabled(false);
        menu.findItem(R.id.action_bt_scan).setIcon(R.mipmap.ic_bt_scan_48px);

        menu.findItem(R.id.action_bt_reset).setEnabled(false);
    }

    private void startScan() {
        if (BluetoothHandler.INSTANCE.isScanning()) return;

        EventBus.getDefault().post(new ScanRequestEvent());
        menu.findItem(R.id.action_bt_scan).setEnabled(false);
        menu.findItem(R.id.action_bt_scan).setIcon(R.drawable.ic_bt_scanning);
        ((AnimationDrawable) menu.findItem(R.id.action_bt_scan).getIcon()).start();
    }

    /*
     * Handlers
     */

    public void onEventMainThread(BeanDiscoveredEvent event) {
        Log.d(TAG, "--> BeanDiscoveredEvent");
        refreshView();
    }

    public void onEventMainThread(ScanFinishedEvent event) {
        Log.d(TAG, "--> ScanFinishedEvent");
        menu.findItem(R.id.action_bt_scan).setEnabled(true);
        menu.findItem(R.id.action_bt_scan).setIcon(R.mipmap.ic_bt_scan_48px);
        refreshView();
    }

    public void onEventMainThread(BtResetDoneEvent event) {
        Log.d(TAG, "--> BtResetDoneEvent");
        menu.findItem(R.id.action_bt_scan).setEnabled(true);
        menu.findItem(R.id.action_bt_scan).setIcon(R.mipmap.ic_bt_scan_48px);
        menu.findItem(R.id.action_bt_reset).setEnabled(true);
    }

}