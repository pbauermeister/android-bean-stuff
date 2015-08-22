package digital.bauermeister.bean_gadgets.events;

import com.punchthrough.bean.sdk.Bean;

/**
 * Created by pascal on 8/22/15.
 */
public class BeanDiscoveredEvent {
    public final Bean bean;
    public final int rssi;

    public BeanDiscoveredEvent(Bean bean, int rssi) {
        this.bean = bean;
        this.rssi = rssi;
    }
};

