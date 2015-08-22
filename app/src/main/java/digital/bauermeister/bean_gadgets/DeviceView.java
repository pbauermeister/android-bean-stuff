package digital.bauermeister.bean_gadgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.punchthrough.bean.sdk.Bean;

import de.greenrobot.event.EventBus;
import digital.bauermeister.bean_gadgets.events.BeanChangedEvent;

/**
 * Manages the visual representation of a device.
 * <p/>
 * Created by pascal on 7/26/15.
 */
public class DeviceView extends FrameLayout {
    private static final String TAG = "DeviceView";
    private TextView nameTv;
    private TextView rssiTv;
    private TextView addrTv;
    private TextView hintTv;
    private View bg;
    private ImageView rssiIv;
    private ImageView connIv;

    private Button button1;
    private CheckBox selectedCb;

    private String bdAddress = "";

    public DeviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public DeviceView(Context context) {
        super(context);
        initLayout();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }


    private void initLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.device_view, this);
        bg = findViewById(R.id.bg);
        nameTv = (TextView) findViewById(R.id.name);
        rssiTv = (TextView) findViewById(R.id.rssiTv);
        addrTv = (TextView) findViewById(R.id.addr);
        hintTv = (TextView) findViewById(R.id.hint);
        rssiIv = (ImageView) findViewById(R.id.rssiIv);
        connIv = (ImageView) findViewById(R.id.connIv);

        button1 = (Button) findViewById(R.id.button1);
        selectedCb = (CheckBox) findViewById(R.id.selectedCb);

        EventBus.getDefault().register(this);
    }

    public void init(final Device device,
                     Bean bean,
                     String buttonText,
                     final Listener listener) {

        bdAddress = device.getBdAddress();

        final boolean selected = device.isSelected();
        Log.d(TAG, "deviceView init() dev --> " + device);
        Log.d(TAG, "                  sel --> " + selected);

        nameTv.setText(device.getName());
        rssiTv.setText("" + device.getRssi());
        addrTv.setText(bdAddress);

        // 1m distance -40dB
        // 2m          -46dB
        // 4m          -52dB
        // 8m          -58dB
        // 16m         -64dB
        int res;
        int rssi = device.getRssi();
        if (rssi > -64) res = R.mipmap.ic_signal_cellular_4_bar_18px;
        else if (rssi > -70) res = R.mipmap.ic_signal_cellular_3_bar_18px;
        else if (rssi > -76) res = R.mipmap.ic_signal_cellular_2_bar_18px;
        else if (rssi > -82) res = R.mipmap.ic_signal_cellular_1_bar_18px;
        else if (rssi > -88) res = R.mipmap.ic_signal_cellular_0_bar_18px;
        else res = R.mipmap.ic_signal_cellular_0_bar_18px;
        rssiIv.setImageResource(res);

        selectedCb.setChecked(selected);

        bg.setBackgroundResource(R.drawable.device_bg);

        button1.setEnabled(selected);
        button1.setVisibility(selected ? VISIBLE : GONE);
        button1.setText(buttonText);

        hintTv.setVisibility(!selected ? VISIBLE : GONE);
        hintTv.setText(R.string.label_hint_unselected);

        selectedCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                button1.setEnabled(isChecked);
                listener.onSelectedChanged(device, isChecked);
            }
        });

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        listener.onButtonDown(device, (Button) v);
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        listener.onButtonUp(device, (Button) v);
                        return false;
                    default:
                        return false;
                }
            }
        });

        updateState(bean);
    }

    private void updateState(Bean bean) {
        boolean conn = bean == null ? false : bean.isConnected();
        connIv.setImageResource(conn ? R.mipmap.ic_connected_24px : R.mipmap.ic_disconnected_24px);
        // TODO: figure out RSSI and update it
    }

    public void onEventMainThread(BeanChangedEvent event) {
        Log.d(TAG, "--> Bean");
        if (bdAddress.equals(event.bean.getDevice().getAddress())) {
            Log.d(TAG, "-EVENT FOR ME- " + event);
            updateState(event.bean);
        }
    }

    public interface Listener {
        void onSelectedChanged(Device device, boolean selected);

        void onButtonDown(Device device, Button button);

        void onButtonUp(Device device, Button button);

    }
}