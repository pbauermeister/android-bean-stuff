package digital.bauermeister.bean_gadgets;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.greenrobot.event.EventBus;
import digital.bauermeister.bean_gadgets.events.BeanDiscoveredEvent;
import digital.bauermeister.bean_gadgets.events.BtResetDoneEvent;
import digital.bauermeister.bean_gadgets.events.BtResetRequestEvent;
import digital.bauermeister.bean_gadgets.events.ScanFinishedEvent;
import digital.bauermeister.bean_gadgets.events.ScanRequestEvent;

/**
 * This singleton handles BT management such as discovery and stack reset.
 * <p/>
 * It mainly run in a background thread, but due to the LightBlue SDK, performs some tasks
 * in the UI thread.
 * <p/>
 * Requests for action shall be sent via the EventBus. Results are return via the bus as well.
 * <p/>
 * Being an enum makes this class a singleton, instantiated on first reference.
 * Created by pascal on 8/22/15.
 */
public enum BluetoothHandler implements BeanDiscoveryListener {

    INSTANCE;

    private static final String TAG = "BackgroundHandler";
    private static final int DELAY_AFTER_BT_ON = 2 * 1000;
    private static final int DELAY_AFTER_BT_OFF = 2 * 1000;
    private boolean scanning;
    private boolean resetting;
    private BluetoothAdapter bluetoothAdapter;

    public void init(Context context) {
        Log.d(TAG, "*** BG handler initialized ***");

        // init BT
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null ||
                !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothAdapter = null;
        }
        EventBus.getDefault().register(this);
    }

    public void uninit() {
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "*** BG handler uninitialized ***");
    }

    public boolean isScanning() {
        return scanning;
    }

    public boolean isResetting() {
        return resetting;
    }

    public boolean hasBle() {
        return bluetoothAdapter != null;
    }

    /*
     *  BeanDiscoveryListener
     */

    @Override
    public void onBeanDiscovered(Bean bean, int rssi) {
        EventBus.getDefault().post(new BeanDiscoveredEvent(bean, rssi));
        int nb = BeanManager.getInstance().getBeans().size();
        Log2.i(TAG, "Scanning... (" + nb + ") Discovered \"" + bean.getDevice().getName() + ".");
        Log2.d(TAG, "- rssi:   " + rssi);
        Log2.d(TAG, "- device: " + bean.getDevice());
        DeviceDatabase.INSTANCE.updateDeviceFromBean(bean, rssi);
    }

    @Override
    public void onDiscoveryComplete() {
        scanning = false;

        int nb = BeanManager.getInstance().getBeans().size();
        Log2.i(TAG, "Scan done. Found " + nb + ".");

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
        for (Device device : absent) {
            if (!device.isSelected())
                db.removeDevice(device);
        }

        EventBus.getDefault().post(new ScanFinishedEvent());
    }

    /*
     * Bus handlers
     */

    public void onEventBackgroundThread(ScanRequestEvent event) {
        Log.d(TAG, "--> ScanRequestEvent");

        if (bluetoothAdapter == null) {
            Log2.i(TAG, "No BLE. Cannot scan.");
            return;
        }

        if (scanning) {
            Log2.i(TAG, "Already scanning");
            return;
        }

        scanning = true;
        Log2.i(TAG, "Scanning...");

        boolean btOn = bluetoothAdapter.isEnabled();
        if (!btOn)
            bluetoothAdapter.enable();

        delayedRun(btOn ? 0 : DELAY_AFTER_BT_ON, new Runnable() {
            @Override
            public void run() {
                BeanManager.getInstance().cancelDiscovery();
                // TDOD check if SDK bug
                // maybe SDK bug? have to disconnect all devices, otherwise connected beans
                // are not re-discovered and their old Bean objects are unusable,
                for (Bean bean : BeanManager.getInstance().getBeans()) {
                    bean.disconnect();
                }
                // scan now
                BeanManager.getInstance().startDiscovery(BluetoothHandler.this);
            }
        });
    }

    public void onEventBackgroundThread(BtResetRequestEvent event) {
        Log.d(TAG, "--> BtResetRequestEvent");

        if (bluetoothAdapter == null) {
            Log2.i(TAG, "No BLE. Cannot reset.");
            return;
        }

        Log2.i(TAG, "BLE reset.");
        if (bluetoothAdapter.isEnabled()) {
            BeanManager.getInstance().cancelDiscovery();
            bluetoothAdapter.disable();
        }

        scanning = false;
        resetting = true;

        delayedRun(DELAY_AFTER_BT_OFF, new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.enable();
                delayedRun(DELAY_AFTER_BT_ON, new Runnable() {
                    @Override
                    public void run() {
                        resetting = false;
                        EventBus.getDefault().post(new BtResetDoneEvent());
                    }
                });
            }
        });
    }

    /*
     * Helpers
     */

    private void delayedRun(final long after, final Runnable job) {
        Runnable delayedRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(after);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // TDOD check if SDK bug: SDK works only in UI thread
                // If the LightBlue SDK would run in the BG thread, we could just call:
                //   job.run();
                // Instead, we have to hav it run in the UI, thread, which is very easy
                // with EventBus:
                EventBus.getDefault().post(job);
            }
        };
        Thread thread = new Thread(delayedRunnable);
        thread.start();
    }

    public void onEventMainThread(Runnable job) {
        job.run();
    }
}
