package digital.bauermeister.bean_stuff;

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

/**
 * Created by pascal on 7/26/15.
 * <p/>
 * Manages the visual representation of a device.
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
    private Button deleteBtn;

    public DeviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public DeviceView(Context context) {
        super(context);
        initLayout();
    }

    private void initLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.device_view, this);
        bg = findViewById(R.id.bg);
        nameTv = (TextView) findViewById(R.id.name);
        rssiTv = (TextView) findViewById(R.id.rssi);
        addrTv = (TextView) findViewById(R.id.addr);
        hintTv = (TextView) findViewById(R.id.hint);
        rssiIv = (ImageView) findViewById(R.id.rssiIv);
        connIv = (ImageView) findViewById(R.id.connIv);

        button1 = (Button) findViewById(R.id.button1);
        selectedCb = (CheckBox) findViewById(R.id.selectedCb);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);

    }

    public void init(final Device device,
                     Boolean conn,
                     String buttonText,
                     final Listener listener) {

        final boolean present = device.isPresent();
        final boolean selected = device.isSelected();
        Log.d(TAG, "deviceView init() dev --> " + device);
        Log.d(TAG, "                  sel --> " + selected);

        nameTv.setText(device.getName());
        rssiTv.setText(present ? "" + device.getRssi() : "---");
        connIv.setImageResource(conn ? R.mipmap.ic_connected_24px : R.mipmap.ic_disconnected_24px);
        addrTv.setText(device.getBdAddress());

        // 1m distance -40dB
        // 2m          -46dB
        // 4m          -52dB
        // 8m          -58dB
        // 16m         -64dB
        int res;
        int rssi = device.getRssi();
        if (!present) res = R.mipmap.ic_signal_cellular_connected_no_internet_0_bar_18px;
        else if (rssi > -64) res = R.mipmap.ic_signal_cellular_4_bar_18px;
        else if (rssi > -70) res = R.mipmap.ic_signal_cellular_3_bar_18px;
        else if (rssi > -76) res = R.mipmap.ic_signal_cellular_2_bar_18px;
        else if (rssi > -82) res = R.mipmap.ic_signal_cellular_1_bar_18px;
        else if (rssi > -88) res = R.mipmap.ic_signal_cellular_0_bar_18px;
        else res = R.mipmap.ic_signal_cellular_0_bar_18px;
        rssiIv.setImageResource(res);

        selectedCb.setChecked(selected);
        selectedCb.setEnabled(present);

        selectedCb.setVisibility(present ? VISIBLE : INVISIBLE);
        deleteBtn.setVisibility(present ? INVISIBLE : VISIBLE);

        bg.setBackgroundResource(present ? R.drawable.device_bg : R.drawable.device_absent_bg);

        button1.setEnabled(present && selected);
        button1.setVisibility(present && selected ? VISIBLE : GONE);
        button1.setText(buttonText);

        hintTv.setVisibility(!present || !selected ? VISIBLE : GONE);
        hintTv.setText(!present ? R.string.label_hint_absent : R.string.label_hint_unselected);

        selectedCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                button1.setEnabled(present && isChecked);
                listener.onSelectedChanged(device, isChecked);
            }
        });

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    listener.onButtonDown(device, (Button) v);
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    listener.onButtonUp(device, (Button) v);
                    return false;
                } else return false;
            }
        });

        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(device);
            }
        });

    }

    private void delete(final Device device) {
        DeviceDatabase.INSTANCE.removeDevice(device);

//        final ScaleAnimation shrinkAnim = new ScaleAnimation(1.15f, 1.0f, 1.15f, 1.0f);
//        shrinkAnim.setDuration(2000);
//        shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                DeviceDatabase.INSTANCE.removeDevice(device);
//            }
//        });
//        setAnimation(shrinkAnim);
//        shrinkAnim.start();
    }

    public interface Listener {
        void onSelectedChanged(Device device, boolean selected);

        void onButtonDown(Device device, Button button);

        void onButtonUp(Device device, Button button);

    }
}