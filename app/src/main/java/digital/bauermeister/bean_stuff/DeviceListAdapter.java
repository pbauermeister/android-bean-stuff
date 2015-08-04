package digital.bauermeister.bean_stuff;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;

import com.punchthrough.bean.sdk.Bean;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Adapter used by the ListView to display Devices, and backed by the RealmDB database.
 */
public class DeviceListAdapter extends RealmBaseAdapter<Device> implements ListAdapter {
    private static final String TAG = "DeviceListAdapter";

    private final DeviceAction deviceAction;

    public DeviceListAdapter(Activity activity) {
        super(activity, getList(), true);
        deviceAction = new DeviceAction(activity, new DeviceAction.DeviceActionHandler() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        });
    }

    public static RealmResults<Device> getList() {
        RealmResults<Device> result = DeviceDatabase.INSTANCE.getRealm().allObjects(Device.class);
        result.sort(
//                "isPresent", RealmResults.SORT_ORDER_DESCENDING,
//                "isSelected", RealmResults.SORT_ORDER_DESCENDING,
                "name", RealmResults.SORT_ORDER_ASCENDING);

        for (Device device : result) Log.d(TAG, "getList() --> " + device);
        return result;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Device item = realmResults.get(position);
        return makeView(position, convertView, parent, item);
    }

    public RealmResults<Device> getRealmResults() {
        return realmResults;
    }

    public View makeView(int position,
                         View convertView, ViewGroup parent,
                         final Device device) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = new DeviceView(context);
            viewHolder = new ViewHolder();
            viewHolder.itemView = (DeviceView) convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Bean bean = Device.getBean(device);

        Log.d(TAG, "makeView() --> " + device);

        final String initialLabel = context.getResources().getString(R.string.label_pushbutton);
        viewHolder.itemView.init(
                device,
                bean == null ? false : bean.isConnected(),
                initialLabel,
                new DeviceView.Listener() {
                    @Override
                    public void onSelectedChanged(Device device, boolean selected) {
                        DeviceDatabase.INSTANCE.updateDeviceSelected(device, selected);
                    }

                    @Override
                    public void onButtonDown(Device device, Button button) {
                        Log2.d(TAG, "Button pressed");
                        deviceAction.doOnAction(device);
                    }

                    @Override
                    public void onButtonUp(Device device, Button button) {
                        Log2.d(TAG, "Button released");
                        deviceAction.doOffAction(device);
                        button.setText(initialLabel);
                    }
                }
        );

        return convertView;
    }

    private static class ViewHolder {
        public DeviceView itemView;
    }
}