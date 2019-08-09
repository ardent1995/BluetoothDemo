package com.sasken.bluetoothdemo.adapter;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasken.bluetoothdemo.MainActivity;
import com.sasken.bluetoothdemo.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PairedDevicesAdapter extends RecyclerView.Adapter<PairedDevicesAdapter.ViewHolder> {

    private ArrayList<BluetoothDevice> deviceList;

    public PairedDevicesAdapter(ArrayList<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_discovery_recyclerview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final BluetoothDevice device = deviceList.get(position);
        holder.deviceName.setText(device.getName());
        Log.d(MainActivity.TAG, "Device Name: " + device.getName());
        holder.deviceAddress.setText(device.getAddress());
        Log.d(MainActivity.TAG, "Device Address: " + device.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(MainActivity.TAG, "You clicked on a device: " + device.getName());
                try {
                    Method method = device.getClass().getMethod("removeBond", null);
                    method.invoke(device, null);
                    Log.d(MainActivity.TAG, "onClick: " + position);
                    deviceList.remove(position);
                    notifyItemRemoved(position);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView deviceName, deviceAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.tv_device_name);
            deviceAddress = itemView.findViewById(R.id.tv_device_address);
        }
    }
}
