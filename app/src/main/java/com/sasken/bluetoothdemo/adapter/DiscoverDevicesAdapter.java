package com.sasken.bluetoothdemo.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasken.bluetoothdemo.BluetoothChatService;
import com.sasken.bluetoothdemo.MainActivity;
import com.sasken.bluetoothdemo.R;

import java.util.ArrayList;

public class DiscoverDevicesAdapter extends RecyclerView.Adapter<DiscoverDevicesAdapter.ViewHolder> {

    private ArrayList<BluetoothDevice> deviceList;

    public DiscoverDevicesAdapter(ArrayList<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_discovery_recyclerview_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final BluetoothDevice device = deviceList.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                Log.d(MainActivity.TAG, "You clicked on a device: "+ device.getName());
                device.createBond();
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView deviceName,deviceAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.tv_device_name);
            deviceAddress = itemView.findViewById(R.id.tv_device_address);
        }
    }
}
