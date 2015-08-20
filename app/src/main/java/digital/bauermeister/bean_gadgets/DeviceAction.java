package digital.bauermeister.bean_gadgets;

import android.content.Context;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.message.BeanError;
import com.punchthrough.bean.sdk.message.LedColor;
import com.punchthrough.bean.sdk.message.ScratchBank;

/**
 * Actions that can be performed (by the user) on a device.
 */
public class DeviceAction {

    private static final String TAG = "DeviceAction";
    private static final int DELAY_DISCONNECT = 1 * 1000;

    private final DeviceActionHandler handler;
    private final Context context;

    private final ConnectedAction onAction = new ConnectedAction() {
        @Override
        public void doAction(Bean bean, Device device) {
            Log2.i(TAG, "Action ON on " + device.getName() + "...");
            int luma = 255;
            bean.setLed(LedColor.create(luma, luma, luma));
        }
    };

    private final ConnectedAction offAction = new ConnectedAction() {
        @Override
        public void doAction(Bean bean, Device device) {
            Log2.i(TAG, "Action OFF on " + device.getName() + "...");
            int luma = 0;
            bean.setLed(LedColor.create(luma, luma, luma));
        }
    };

    public DeviceAction(Context context, DeviceActionHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void doOnAction(Device device) {
        doConnectedCommand(device, onAction);
    }

    public void doOffAction(Device device) {
        doConnectedCommand(device, offAction);
    }

    private void doConnectedCommand(final Device device, final ConnectedAction action) {
        final Bean bean = Device.getBean(device);

        if (bean == null) {
            Log2.i(TAG, device + " is not available.");
            return;
        }

        if (bean.isConnected()) {
            Log2.i(TAG, "Is already connected: " + device.getName());
            action.doAction(bean, device);
        } else {
            Log2.i(TAG, "Connecting to " + device.getName() + "...");
            MyBeanListener beanListener = new MyBeanListener(device, bean, action);
            handler.onChangeState(bean);
            try {
                bean.connect(context, beanListener);
            } catch (Exception e) {
                Log2.i(TAG, "Connecting to " + device.getName() + " FAILED");
            }
        }
    }

    public interface DeviceActionHandler {
        void onChangeState(Bean bean);

        void onChangeList();
    }

    private interface ConnectedAction {
        void doAction(Bean bean, Device device);
    }

    private class MyBeanListener implements BeanListener {
        private final String name;
        private final Device device;
        private final Bean bean;
        private final ConnectedAction action;

        public MyBeanListener(Device device, Bean bean, ConnectedAction action) {
            name = device.getName();
            this.device = device;
            this.bean = bean;
            this.action = action;
        }

        @Override
        public void onConnected() {
            Log2.i(TAG, "Connected to " + name + ".");
            device.setError(null);
            handler.onChangeState(bean);
            action.doAction(bean, device);
        }

        @Override
        public void onConnectionFailed() {
            Log2.i(TAG, "Connection to " + name + " failed.");
            handler.onChangeState(bean);
        }

        @Override
        public void onDisconnected() {
            Log2.i(TAG, "Disconnected from " + name + ".");
            handler.onChangeState(bean);
        }

        @Override
        public void onSerialMessageReceived(byte[] data) {
        }

        @Override
        public void onScratchValueChanged(ScratchBank bank, byte[] value) {
        }

        @Override
        public void onError(BeanError error) {
        }
    }

    ;
}