package digital.bauermeister.bean_gadgets.events;

import com.punchthrough.bean.sdk.Bean;

/**
 * Created by pascal on 8/22/15.
 */
public class BeanChangedEvent {
    public final Bean bean;

    public BeanChangedEvent(Bean bean) {
        this.bean = bean;
    }
};

