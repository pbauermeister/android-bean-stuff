package digital.bauermeister.bean_stuff;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * The principal and so far only activity.
 */
public class MainActivity extends Activity {

    private static final int DELAY_AFTER_BT_ON = 3 * 1000;
    private static final int DELAY_AFTER_BT_OFF = 3 * 1000;

    private static final String TAG = "MainActivity";

    private TextView logTv;
    private TextView lastLogTv;
    private ScrollView logScroll;
    private View logDetails;

    private ListView listView;
    private TextView emptyTv;
    private DeviceListAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private Menu menu;

    private boolean scanning;

    private BeanDiscoveryListener listener = new BeanDiscoveryListener() {
        @Override
        public void onBeanDiscovered(Bean bean, int rssi) {
            updateBean(bean, rssi, true);
            int nb = BeanManager.getInstance().getBeans().size();
            Log2.i(TAG, "Scanning... (" + nb + ") Discovered \"" + bean.getDevice().getName() + ".");
            Log2.d(TAG, "- rssi:   " + rssi);
            Log2.d(TAG, "- device: " + bean.getDevice());
        }

        @Override
        public void onDiscoveryComplete() {
            scanning = false;

            // mark presence
            List<Device> present = new ArrayList<Device>();
            List<Device> absent = new ArrayList<Device>();

            DeviceDatabase db = DeviceDatabase.INSTANCE;
            Collection<Bean> allSeen = BeanManager.getInstance().getBeans();
            for (Device device : db.getDevices()) {
                boolean here = false;
                for (Bean seen : allSeen) {
                    if (seen.getDevice().getAddress().equals(device.getBdAddress())) {
                        here = true;
                        break;
                    }
                }
                if (here) present.add(device);
                else absent.add(device);
            }
            for (Device device : present) db.updateDevicePresent(device, true);
            for (Device device : absent) db.updateDevicePresent(device, false);
            for (Device device : absent) {
                if (!device.isSelected())
                    db.removeDevice(device);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int nb = BeanManager.getInstance().getBeans().size();
                    Log2.i(TAG, "Scan done. Found " + nb + ".");
                    menu.findItem(R.id.action_bt_scan).setEnabled(true);
                    menu.findItem(R.id.action_bt_scan).setIcon(R.mipmap.ic_bt_scan_48px);
                    refreshView();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init view
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        emptyTv = (TextView) findViewById(R.id.emptyTv);

        logTv = (TextView) findViewById(R.id.logTv);
        lastLogTv = (TextView) findViewById(R.id.lastLogTv);
        logScroll = (ScrollView) findViewById(R.id.logScroll);
        logDetails = findViewById(R.id.logDetails);
        Log2.init(this, lastLogTv, logTv, logScroll);

        // init BT
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null ||
                !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
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

        // initial scan
        listView.post(new Runnable() {
            @Override
            public void run() {
                startScan();
            }
        });

        SlidingUpPanelLayout p = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
//        p.setAnchorPoint(0.4f);
        logDetails.setVisibility(View.GONE);
        p.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
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
    public void onPause() {
        super.onPause();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            BeanManager.getInstance().cancelDiscovery();
            menu.findItem(R.id.action_bt_scan).setEnabled(true);
            menu.findItem(R.id.action_bt_scan).setIcon(R.mipmap.ic_bt_scan_48px);
        }
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

            case R.id.action_settings:
                // TODO
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateBean(Bean bean, int rssi, boolean present) {
        DeviceDatabase.INSTANCE.updateDeviceFromBean(bean, rssi, present);
        refreshView();
    }

    private void refreshView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                emptyTv.setVisibility(adapter.isEmpty() && !scanning ? View.VISIBLE : View.GONE);
                listView.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    /*
     * BLE
     */

    private void resetBt() {
        Log2.i(TAG, "BLE reset.");
        if (bluetoothAdapter.isEnabled()) {
            BeanManager.getInstance().cancelDiscovery();
            bluetoothAdapter.disable();
        }
        menu.findItem(R.id.action_bt_scan).setEnabled(true);
        menu.findItem(R.id.action_bt_scan).setIcon(R.mipmap.ic_bt_scan_48px);


        delayedRun(DELAY_AFTER_BT_OFF, new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.enable();
                delayedRun(DELAY_AFTER_BT_ON, new Runnable() {
                    @Override
                    public void run() {
                        refreshView();
                    }
                });
            }
        });
    }

    private void startScan() {
        scanning = true;
        refreshView();

        menu.findItem(R.id.action_bt_scan).setEnabled(false);
        menu.findItem(R.id.action_bt_scan).setIcon(R.drawable.ic_bt_scanning);
        ((AnimationDrawable) menu.findItem(R.id.action_bt_scan).getIcon()).start();

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            delayedRun(DELAY_AFTER_BT_ON, new Runnable() {
                @Override
                public void run() {
                    scan();
                }
            });
        } else {
            scan();
        }
    }

    private void scan() {
        Log2.i(TAG, "Scanning...");
        BeanManager.getInstance().startDiscovery(listener);
    }

    /*
     * Misc
     */

    private void delayedRun(final long after, final Runnable r) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(after);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        r.run();
                        return null;
                    }
                }.execute(null, null, null);
            }
        });
    }
}
