package digital.bauermeister.bean_stuff;

import android.content.Context;

import com.punchthrough.bean.sdk.Bean;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by pascal on 4/9/15.
 */
public enum DeviceDatabase {
    INSTANCE;

    private static final String TAG = "DeviceDatabase";
    private Realm realm;

    public synchronized void init(Context context) {
        try {
            realm = Realm.getInstance(context);
        } catch (RealmMigrationNeededException e) {
            Realm.deleteRealm(new RealmConfiguration.Builder(context).build()); // TODO: actually migrate
            realm = Realm.getInstance(context);
        }
    }

    public Realm getRealm() {
        return realm;
    }

    public void updateDeviceFromBean(Bean bean, int rssi, boolean present) {
        Device device = getDevice(bean.getDevice().getAddress());
        if (device == null) {
            device = new Device(bean, rssi, present);
            putDevice(device);
        } else {
            updateDevice(device, bean, rssi, present);
        }
    }

    public synchronized void clearAll() {
        try {
            Log2.d(TAG, "clearAll...");
            realm.beginTransaction();
            realm.clear(Device.class);
            realm.commitTransaction();
            Log2.d(TAG, "clearAll OK");
        } catch (RealmException e) {
            realm.cancelTransaction();
            Log2.d(TAG, "clearAll FAILED");
            throw e;
        }
    }

    public synchronized void putDevice(Device device) {
        try {
            Log2.d(TAG, "putDevice...");
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(device);
            realm.commitTransaction();
            Log2.d(TAG, "putDevice OK");
        } catch (RealmException e) {
            realm.cancelTransaction();
            Log2.d(TAG, "putDevice FAILED");
            throw e;
        }
    }

    public synchronized void updateDevice(Device device, Bean bean, int rssi, boolean present) {
        try {
            Log2.d(TAG, "updateDevice...");
            realm.beginTransaction();
            {
                device.setName(bean.getDevice().getName());
                device.setRssi(rssi);
                device.setIsPresent(present);
            }
            realm.commitTransaction();
            Log2.d(TAG, "updateDevice OK");
        } catch (RealmException e) {
            realm.cancelTransaction();
            Log2.d(TAG, "updateDevice FAILED");
            throw e;
        }
    }

    public synchronized void updateDeviceSelected(Device device, boolean selected) {
        try {
            Log2.d(TAG, "updateDeviceSelected... " + selected);
            realm.beginTransaction();
            {
                device.setIsSelected(selected);
            }
            realm.commitTransaction();
            Log2.d(TAG, "updateDeviceSelected OK");
        } catch (RealmException e) {
            realm.cancelTransaction();
            Log2.d(TAG, "updateDeviceSelected FAILED");
            throw e;
        }
    }

    public synchronized void updateDevicePresent(Device device, boolean present) {
        try {
            Log2.d(TAG, "updateDevicePresent...");
            realm.beginTransaction();
            {
                device.setIsPresent(present);
            }
            realm.commitTransaction();
            Log2.d(TAG, "updateDevicePresent OK");
        } catch (RealmException e) {
            realm.cancelTransaction();
            Log2.d(TAG, "updateDevicePresent FAILED");
            throw e;
        }
    }

    public Device getDevice(String bdAddress) {
        RealmResults<Device> results = realm
                .where(Device.class)
                .equalTo("bdAddress", bdAddress)
                .findAll();
        return results.isEmpty() ? null : results.first();
    }

    public RealmResults<Device> getDevices() {
        RealmResults<Device> results = realm
                .where(Device.class)
                .findAll();
        return results;
    }


    public synchronized void removeDevice(Device device) {
        try {
            Log2.d(TAG, "removeDevice...");
            realm.beginTransaction();
            RealmResults<Device> result = realm.where(Device.class)
                    .equalTo("bdAddress", device.getBdAddress())
                    .findAll();
            result.clear();
            realm.commitTransaction();
            Log2.d(TAG, "removeDevice OK");
        } catch (RealmException e) {
            realm.cancelTransaction();
            Log2.d(TAG, "removeDevice FAILED");
            throw e;
        }
    }
}
