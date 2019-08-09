package com.sasken.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sasken.bluetoothdemo.adapter.DiscoverDevicesAdapter;
import com.sasken.bluetoothdemo.adapter.PairedDevicesAdapter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1043;
    private static final int REQUEST_ENABLE_DISCOVERABILITY = 5131;
    private static final int DISCOVERABLE_DURATION = 300;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1304;

    private Button enableBluetoothButton, enableDiscoverabilityButton, discoverDevicesButton, pairedDevicesButton, startConnectionButton, sendMessageButton;
    private EditText etMessage;
    private TextView tvChatbox;
    private ProgressBar progressBar;
    private RecyclerView discoverDevicesRecyclerView, pairedDevicesRecyclerView;

    private DiscoverDevicesAdapter discoverDevicesAdapter;
    private PairedDevicesAdapter pairedDevicesAdapter;
    private BluetoothChatService bluetoothChatService;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mDevice;

    private ArrayList<BluetoothDevice> discoveredDeviceList = new ArrayList<>();
    private ArrayList<BluetoothDevice> pairedDeviceList = new ArrayList<>();

    private StringBuilder stringBuilder;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR);

                switch (previousState) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "From STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "From STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "From STATE_TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "From STATE_TURNING_OFF");
                        break;
                }

                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "to STATE_ON");
                        maintainUiState();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "to STATE_OFF");
                        maintainUiState();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "to STATE_TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "to STATE_TURNING_OFF");
                        break;
                }
            }
            else if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                int previousScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (previousScanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "From SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "From SCAN_MODE_CONNECTABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "From SCAN_MODE_NONE");
                        break;
                }

                switch (scanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        enableDiscoverabilityButton.setText(getString(R.string.disable_discoverability));
                        enableDiscoverabilityButton.setEnabled(false);
                        Log.d(TAG, "to SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        enableDiscoverabilityButton.setText(getString(R.string.enable_discoverability));
                        enableDiscoverabilityButton.setEnabled(true);
                        Log.d(TAG, "to SCAN_MODE_CONNECTABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        enableDiscoverabilityButton.setText(getString(R.string.enable_discoverability));
                        enableDiscoverabilityButton.setEnabled(true);
                        Log.d(TAG, "to SCAN_MODE_NONE");
                        break;
                }
            }
            else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDeviceList.add(device);
                Log.d(TAG, "Device Found:: Name: " + device.getName() + " Address: " + device.getAddress());
                discoverDevicesAdapter.notifyDataSetChanged();
            }
            else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (bluetoothDevice.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(TAG, bluetoothDevice.getName()+" :BOND_BONDED");
                        mDevice = bluetoothDevice;
                        bluetoothChatService = new BluetoothChatService(MainActivity.this,progressBar);
                        stringBuilder = new StringBuilder();
                        startConnectionButton.setEnabled(true);
                        sendMessageButton.setEnabled(true);
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, bluetoothDevice.getName()+" :BOND_BONDING");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d(TAG, bluetoothDevice.getName()+" :BOND_NONE");
                        startConnectionButton.setEnabled(false);
                        sendMessageButton.setEnabled(false);
                        break;
                }
            }
        }
    };

    private BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra(BluetoothChatService.EXTRA_TEXT);
            stringBuilder.append(text+"\n");
            tvChatbox.setText(stringBuilder);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableBluetoothButton = findViewById(R.id.btn_enable_bluetooth);
        enableDiscoverabilityButton = findViewById(R.id.btn_enable_discoverability);
        discoverDevicesButton = findViewById(R.id.btn_discover_devices);
        pairedDevicesButton = findViewById(R.id.btn_paired_devices);
        startConnectionButton = findViewById(R.id.btn_start_connection);
        sendMessageButton = findViewById(R.id.btn_send_msg);

        etMessage = findViewById(R.id.et_msg);

        tvChatbox = findViewById(R.id.tv_chatbox);

        progressBar = findViewById(R.id.progress_bar);

        discoverDevicesRecyclerView = findViewById(R.id.rv_discover_devices);
        pairedDevicesRecyclerView = findViewById(R.id.rv_paired_devices);
        discoverDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        discoverDevicesAdapter = new DiscoverDevicesAdapter(discoveredDeviceList);
        discoverDevicesRecyclerView.setAdapter(discoverDevicesAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            maintainUiState();
            enableDiscoverabilityButton.setText(getString(R.string.enable_discoverability));
            enableBluetoothButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnOnOrOffBT();
                }
            });
            enableDiscoverabilityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enableDiscoverability();
                }
            });
            discoverDevicesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkFineLocationPermission()) {
                        discoverDevices();
                    }
                }
            });
            pairedDevicesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPairedDevices();
                }
            });
            startConnectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBluetoothConnection(mDevice,BluetoothChatService.MY_UUID_SECURE);
                }
            });
            sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    byte[] bytes = etMessage.getText().toString().getBytes(Charset.defaultCharset());
                    bluetoothChatService.write(bytes);
                    etMessage.setText("");
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bluetoothAdapter != null) {
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(broadcastReceiver, intentFilter);
            LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver,new IntentFilter(BluetoothChatService.ACTION_TO_SEND_TEXT));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothAdapter != null) {
            unregisterReceiver(broadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        }
    }

    private void maintainUiState() {
        enableBluetoothButton.setText(bluetoothAdapter.isEnabled() ? getString(R.string.turn_off_bt) : getString(R.string.turn_on_bt));
        discoverDevicesButton.setText(getString(R.string.discover_devices));
        discoverDevicesButton.setEnabled(bluetoothAdapter.isEnabled());
        pairedDevicesButton.setText(getString(R.string.pair_devices));
        pairedDevicesButton.setEnabled(bluetoothAdapter.isEnabled());
        startConnectionButton.setText(getString(R.string.start_connection));
        sendMessageButton.setText(getString(R.string.send));
        startConnectionButton.setEnabled(false);
        sendMessageButton.setEnabled(false);
        if (!bluetoothAdapter.isEnabled()) {
            discoverDevicesRecyclerView.setVisibility(View.GONE);
            pairedDevicesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void turnOnOrOffBT() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
    }

    private void enableDiscoverability() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(intent, REQUEST_ENABLE_DISCOVERABILITY);
    }

    private boolean checkFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    private void discoverDevices() {
        discoverDevicesRecyclerView.setVisibility(View.VISIBLE);
        pairedDevicesRecyclerView.setVisibility(View.GONE);
        discoveredDeviceList.clear();
        discoverDevicesAdapter.notifyDataSetChanged();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void getPairedDevices() {
        discoverDevicesRecyclerView.setVisibility(View.GONE);
        pairedDevicesRecyclerView.setVisibility(View.VISIBLE);
        pairedDeviceList.clear();
        pairedDeviceList = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        Log.d(TAG, "getPairedDevices: " + pairedDeviceList);
        pairedDevicesAdapter = new PairedDevicesAdapter(pairedDeviceList);
        pairedDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pairedDevicesRecyclerView.setAdapter(pairedDevicesAdapter);
    }

    private void startBluetoothConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBluetoothConnection: Initializing RFCOM Bluetooth connection");
        bluetoothChatService.startClient(device,uuid);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "User grant access to turn on Bluetooth");
            } else {
                Log.d(TAG, "User denied access to turn on Bluetooth");
            }
        } else if (requestCode == REQUEST_ENABLE_DISCOVERABILITY) {
            if (resultCode == DISCOVERABLE_DURATION) {
                Log.d(TAG, "User grant access to turn on Bluetooth Discoverability");
            } else {
                Log.d(TAG, "User denied access to turn on Bluetooth Discoverability");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    discoverDevices();
                } else {
                    checkFineLocationPermission();
                }
                break;
        }
    }
}
