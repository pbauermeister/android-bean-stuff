package digital.bauermeister.bean_stuff;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanManager;

import org.assertj.core.util.Preconditions;

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

    @PrimaryKey
    private String bdAddress; // BT device MAC address

    private String name; // device BT name
    private int rssi; // signal strength at scan
    private boolean isSelected; // selected by user
    private boolean isPresent; // seen in last BT scan

    @Ignore
    private String error;

    public Device(Bean bean, int rssi, boolean present) {
        this.bdAddress = bean.getDevice().getAddress();
        this.name = bean.getDevice().getName();
        this.rssi = rssi;
        this.isPresent = present;
    }

    public Device() {
    }

    public static Bean getBean(Device device) {
        for (Bean bean : BeanManager.getInstance().getBeans()) {
            if (bean.getDevice().getAddress().equals(device.getBdAddress()))
                return bean;
        }
        return null;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
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
