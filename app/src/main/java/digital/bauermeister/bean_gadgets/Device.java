package digital.bauermeister.bean_gadgets;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.punchthrough.bean.sdk.Bean;

import org.assertj.core.util.Preconditions;

import java.util.HashMap;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by pascal on 7/22/15.
 * <p/>
 * Model of a device, that is stored in DB.
 * <p/>
 * A device represents the what we know of a Bean.
 */
public class Device extends RealmObject {

    private static final String TAG = "Device";
    static HashMap<String, Bean> beans;
    @PrimaryKey
    private String bdAddress; // BT device MAC address
    private String name; // device BT name
    private int rssi; // signal strength at scan
    private boolean isSelected; // selected by user
    @Ignore
    private String error;

    public Device(Bean bean, int rssi) {
        this.bdAddress = bean.getDevice().getAddress();
        this.name = bean.getDevice().getName();
        this.rssi = rssi;
        clearCache();
    }

    public Device() {
        clearCache();
    }

    public static void clearCache() {
        beans = new HashMap<String, Bean>();
    }

    public static Bean getBean(Device device) {
//        for (Bean bean : BeanManager.getInstance().getBeans()) {
//            if (bean.getDevice().getAddress().equals(device.getBdAddress()))
//                Log.d(TAG, "-Use discovered bean-");
//            return bean;
//        }
        String bdAddress = device.getBdAddress();
        if (beans.containsKey(bdAddress)) {
            Log.d(TAG, "-Bean from cache-");
            return beans.get(bdAddress);
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(bdAddress);
            Log.d(TAG, "-Use created bean with BtAdapter-");
            Bean bean = new Bean(btDevice);
            beans.put(bdAddress, bean);
            return bean;
        }

        Log.d(TAG, "-No bean-");
        return null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getBdAddress() {
        return bdAddress;
    }

    public void setBdAddress(String bdAddress) {
        Preconditions.checkNotNull(null, "bdAddress shall never be changed after construction");
        this.bdAddress = bdAddress;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
