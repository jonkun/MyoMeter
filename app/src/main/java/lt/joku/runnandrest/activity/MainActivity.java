package lt.joku.runnandrest.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.List;

import co.lujun.lmbluetoothsdk.BluetoothLEController;
import co.lujun.lmbluetoothsdk.base.BluetoothLEListener;
import lt.joku.runnandrest.R;
import lt.joku.runnandrest.service.PermissionService;
import lt.joku.runnandrest.service.PermissionsCallback;
import lt.joku.runnandrest.utils.BluetoothUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int SCAN_TIMEOUT = 8_000; // sec
    private static int RC_ACCESS_COARSE_LOCATION = 4445;

    private TextView connStateText;
    private List<String> devicesList = new ArrayList<>();
    private ArrayAdapter devicesAdapter;
    private Button scanBtn;

    BluetoothLEController mBLEController;
    PermissionService permissionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanBtn = (Button) findViewById(R.id.button_scan);
        scanBtn.setOnClickListener(this);

        connStateText = (TextView) findViewById(R.id.text_connection_state);

        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesList);
        ListView devicesListView = (ListView) findViewById(R.id.list_devices);
        devicesListView.setAdapter(devicesAdapter);
        devicesListView.setOnItemClickListener(this);

        permissionService = new PermissionService(RC_ACCESS_COARSE_LOCATION, R.string.contact_permission_not_granted, Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionService.onGrant(this, new PermissionsCallback() {
            @Override
            public void onGrant() {
//                Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
        });

        mBLEController = BluetoothLEController.getInstance().build(MainActivity.this);
        if (mBLEController.isAvailable() && mBLEController.isEnabled()) {
            mBLEController.setScanTime(SCAN_TIMEOUT);
            mBLEController.setBluetoothListener(getBtListener());
            onClickScanBtn();
        } else {
            Toast.makeText(this, "Please turn on bluetooth!!!", Toast.LENGTH_LONG).show();
        }
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

            @Override
            public void onDataChanged(BluetoothGattCharacteristic characteristic) {
                XLog.d("onDataChanged, %d", characteristic.getValue()[1]);
            }

            @Override
            public void onDiscoveringCharacteristics(List<BluetoothGattCharacteristic> characteristics) {
                XLog.d("onDiscoveringCharacteristics");
                for(BluetoothGattCharacteristic characteristic : characteristics){
                    XLog.d("\tcharacteristic: %s", characteristic.getUuid());
                }
            }

            @Override
            public void onDiscoveringServices(List<BluetoothGattService> services) {
                XLog.d("onDiscoveringServices, count: %d", services.size());
            }

            @Override
            public void onActionStateChanged(int preState, int state) {
                // Callback when bluetooth power state changed.
                XLog.d("onActionStateChanged");
            }

            @Override
            public void onActionDiscoveryStateChanged(String discoveryState) {
                // Callback when local Bluetooth adapter discovery process state changed.
                XLog.d("onActionDiscoveryStateChanged: %s", discoveryState);
                if (discoveryState.equals("android.bluetooth.adapter.action.DISCOVERY_STARTED")) {
                    scanBtn.setEnabled(false);
                    connStateText.setText("scanning...");
                } else if (discoveryState.equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
                    scanBtn.setEnabled(true);
                    connStateText.setText("scanning finished");
                }
            }

            @Override
            public void onActionScanModeChanged(int preScanMode, int scanMode) {
                // Callback when the current scan mode changed.
                XLog.d("onActionScanModeChanged: %d; %d", scanBtn, scanMode);
            }

            @Override
            public void onBluetoothServiceStateChanged(int state) {
                XLog.d("State: %d, Device connection state: %d", state, mBLEController.getConnectionState());
                runOnUiThread(() -> {
                    connStateText.setText(BluetoothUtils.transConnStateAsString(state));
                });
            }

            @Override
            public void onActionDeviceFound(BluetoothDevice device, short rssi) {
                String btDevice = String.format(String.format("%s|%s", device.getName(), device.getAddress()));
                if (!devicesList.contains(btDevice)) {
                    runOnUiThread(() -> {
                        devicesList.add(btDevice);
                        devicesAdapter.notifyDataSetChanged();
                    });
                }
            }
        };
    }

    public void onClickConnectBtn(String mac) {
        mBLEController.cancelScan();
        Toast.makeText(this, "Selected MAC: " + mac, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,  ChartActivity.class);
//        intent.putExtra("name", itemStr.substring(0, itemStr.length() - 18));
        intent.putExtra("mac", mac);
        startActivity(intent);
//        mBLEController.connect(mac);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        permissionService.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_scan:
                onClickScanBtn();
                break;
        }
    }

    private void onClickScanBtn() {
        scanBtn.setEnabled(false);
        connStateText.setText("scanning...");
        mBLEController.startScan();
        devicesList.clear();
        devicesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String nameWithMac = devicesList.get(position);
        onClickConnectBtn(nameWithMac.substring(nameWithMac.indexOf("|") + 1, nameWithMac.length()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBLEController.release();
    }
}
