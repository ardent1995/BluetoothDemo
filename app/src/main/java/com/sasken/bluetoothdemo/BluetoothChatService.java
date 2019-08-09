package com.sasken.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import static com.sasken.bluetoothdemo.MainActivity.TAG;

public class BluetoothChatService {

    private static final String appName = "BluetoothDemo";
    static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    static final String ACTION_TO_SEND_TEXT = "com.sasken.bluetoothdemo.BluetoothChatService.incomingmessage";
    static final String EXTRA_TEXT = "message";

    private final BluetoothAdapter bluetoothAdapter;
    private Context context;
    private BluetoothDevice bluetoothDevice;
    private UUID deviceUUID;
    private ProgressBar progressBar;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    public BluetoothChatService(Context context, ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket bluetoothServerSocket;


        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(appName,MY_UUID_SECURE);
                Log.d(TAG, "AcceptThread: Setting up server using: "+MY_UUID_SECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothServerSocket = tmp;
        }

        @Override
        public void run() {
            super.run();
            BluetoothSocket bluetoothSocket = null;
            while (true) {
                try {
                    Log.d(TAG, "run: RFCOM server socket start....");
                    bluetoothSocket = bluetoothServerSocket.accept();
                    Log.d(TAG, "run: RFCOM server socket accepted connection.");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if(bluetoothSocket != null){
                    manageMyConnectedSocket(bluetoothSocket, bluetoothDevice);
                    try {
                        bluetoothServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel(){
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread{

        private final BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            bluetoothDevice = device;
            deviceUUID = uuid;
            BluetoothSocket tmp = null;
            Log.d(TAG, "Run ConnectThread");
            try {
                Log.d(TAG, "Trying to create RFCOMMSocket using UUID: "+MY_UUID_SECURE);
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothSocket = tmp;
        }

        @Override
        public void run() {
            super.run();
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
                Log.d(TAG, "ConnectThread Connected");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
            manageMyConnectedSocket(bluetoothSocket, bluetoothDevice);
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void start(){
        Log.d(TAG, "start");
        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }
        if(acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient");
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        connectThread = new ConnectThread(device,uuid);
        connectThread.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            Log.d(TAG, "ConnectedThread: starting");
            this.bluetoothSocket = bluetoothSocket;
            InputStream tmpInputStream = null;
            OutputStream tmpOutputStream = null;
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });

            try {
                tmpInputStream = bluetoothSocket.getInputStream();
                tmpOutputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tmpInputStream;
            outputStream = tmpOutputStream;
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[1024];
            int bytes;
            while (true){
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer,0,bytes);
                    Log.d(TAG, "InputStream: "+incomingMessage);

                    Intent intent = new Intent(ACTION_TO_SEND_TEXT);
                    intent.putExtra(EXTRA_TEXT,incomingMessage);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to the output stream: "+text);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket,BluetoothDevice device){
        Log.d(TAG, "manageMyConnectedSocket: starting");
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void write(byte[] out){
        Log.d(TAG, "write: Write called");
        connectedThread.write(out);
    }
}
