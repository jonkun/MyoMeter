package lt.joku.runnandrest.activity;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.lujun.lmbluetoothsdk.BluetoothLEController;
import co.lujun.lmbluetoothsdk.base.BluetoothLEListener;
import lt.joku.runnandrest.R;
import lt.joku.runnandrest.utils.BluetoothUtils;
import lt.joku.runnandrest.utils.SampleGattAttributes;

import static co.lujun.lmbluetoothsdk.base.State.STATE_GOT_CHARACTERISTICS;

/**
 * @author Jonas Kundra
 */

public class ChartActivity extends AppCompatActivity {

    public final static UUID UUID_HEART_RATE_CHARACTERISTIC = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_HEART_RATE_SERVICE = UUID.fromString(SampleGattAttributes.HEART_RATE_SERVICE);

    BluetoothLEController mBLEController;

    @BindView(R.id.text_connection_state) TextView connStateText;
    @BindView(R.id.text_result) TextView resultText;
    @BindView(R.id.sparkview) SparkView sparkView;

    ChartAdapter chartAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        ButterKnife.bind(this);
        String mac = getIntent().getStringExtra("mac");
        mBLEController = BluetoothLEController.getInstance().build(this);
        mBLEController.setBluetoothListener(getBtListener());
        mBLEController.connect(mac);
        connStateText = (TextView) findViewById(R.id.text_connection_state);
        chartAdapter = new ChartAdapter(new ArrayList<>());
        sparkView.setAdapter(chartAdapter);
        sparkView.setScrubEnabled(true);

    }

    @NonNull
    private BluetoothLEListener getBtListener() {
        return new BluetoothLEListener() {

            @Override
            public void onReadData(BluetoothGattCharacteristic characteristic) {
                XLog.d("characteristic: %s", characteristic.getStringValue(0));
            }

            @Override
            public void onWriteData(BluetoothGattCharacteristic characteristic) {
                XLog.d("onWriteData");
            }
            public int unsignedToBytes(byte b) {
                return b & 0xFF;
            }

            @Override
            public void onDataChanged(BluetoothGattCharacteristic characteristic) {
                int ch1 = unsignedToBytes(characteristic.getValue()[1]);
                int ch2 = unsignedToBytes(characteristic.getValue()[2]);
                int a = (ch2 * 256) + ch1;

                float numberFromBt = (float)((float) a / 100.0);
                String numberString = String.format("%.02f", numberFromBt);
                XLog.d("onDataChanged, %s", numberFromBt);
                runOnUiThread(() -> {
                    resultText.setText(numberString);
//                    resizeImageView(numberFromBt * 100);
                    chartAdapter.addValue(numberFromBt * 100);
                });
            }

            @Override
            public void onDiscoveringCharacteristics(List<BluetoothGattCharacteristic> characteristics) {
                XLog.d("onDiscoveringCharacteristics");
            }

            @Override
            public void onDiscoveringServices(List<BluetoothGattService> services) {
                XLog.d("onDiscoveringServices, count: %d", services.size());
            }

            @Override
            public void onActionStateChanged(int preState, int state) {
                XLog.d("onActionStateChanged");
            }

            @Override
            public void onActionDiscoveryStateChanged(String discoveryState) {
                XLog.d("onActionDiscoveryStateChanged: %s", discoveryState);
            }

            @Override
            public void onActionScanModeChanged(int preScanMode, int scanMode) {
                XLog.d("onActionScanModeChanged: %d; %d", preScanMode, scanMode);
            }

            @Override
            public void onBluetoothServiceStateChanged(int state) {
                XLog.d("State: %d, Device connection state: %d", state, mBLEController.getConnectionState());
                if (state == STATE_GOT_CHARACTERISTICS) {
                    turnOnHartRateNotifications();
                }
                runOnUiThread(() -> {
                    connStateText.setText(BluetoothUtils.transConnStateAsString(state));
                });
            }

            private void turnOnHartRateNotifications() {
                // Turn on hard rate notifications
                BluetoothGatt gatt = mBLEController.getService().getGatt();
                BluetoothGattCharacteristic characteristic = gatt.getService(UUID_HEART_RATE_SERVICE).getCharacteristic(UUID_HEART_RATE_CHARACTERISTIC);
                gatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }

            @Override
            public void onActionDeviceFound(BluetoothDevice device, short rssi) {
                XLog.d("onActionDeviceFound");
            }
        };
    }

//    private void resizeImageView(float persent) {
//        ImageView layout = (ImageView) findViewById(R.id.image_hider);
//        // Gets the layout params that will allow you to resize the layout
//        ViewGroup.LayoutParams params = layout.getLayoutParams();
//        // Changes the height and width to the specified *pixels*
//
//
//        params.height = dpToPx((int) (250 - (persent * 2.5)));
//        layout.setLayoutParams(params);
//
//    }

//    private int dpToPx(int dp) {
//        return (int) (dp * getResources().getDisplayMetrics().density);
//    }


    public class ChartAdapter extends SparkAdapter {
        private List<Float> yData;

        public ChartAdapter(List<Float> yData) {
            this.yData = yData;
        }

        @Override
        public int getCount() {
            return yData.size();
        }

        @Override
        public Object getItem(int index) {
            return yData.get(index);
        }

        @Override
        public float getY(int index) {
            return yData.get(index);
        }

        public void addValue(float newValue) {
            yData.add(newValue);
            if (yData.size() > 50) {
                yData.remove(0);
            }
            notifyDataSetChanged();
        }
    }
}
