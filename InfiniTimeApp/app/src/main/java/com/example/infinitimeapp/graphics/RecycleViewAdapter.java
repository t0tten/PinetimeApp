package com.example.infinitimeapp.graphics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.infinitimeapp.ScanActivity;
import com.example.infinitimeapp.R;
import com.example.infinitimeapp.bluetooth.BluetoothDevices;
import com.example.infinitimeapp.bluetooth.BluetoothService;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> implements View.OnClickListener {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView alias, mac;
        ImageView image;

        public ViewHolder(View v) {
            super(v);
            alias = v.findViewById(R.id.alias);
            mac = v.findViewById(R.id.mac_address);
            image = v.findViewById(R.id.image_view);
        }
    }

    private final Context mContext;
    private final BluetoothDevices mDevices;
    private final BluetoothService mBluetoothService;

    public RecycleViewAdapter(Context context, BluetoothService bluetoothService) {
        mContext = context;
        mBluetoothService = bluetoothService;
        mDevices = BluetoothDevices.getInstance();
    }

    @NonNull
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevices.BTDeviceModel device = mDevices.getDeviceFromIndex(position);

        holder.alias.setText(device.name);
        holder.mac.setText(device.mac);
        holder.image.setImageResource(R.drawable.watch);
    }

    @Override
    public int getItemCount() {
        return mDevices.getSize();
    }

    @Override
    public void onClick(View v) {
        int index = ScanActivity.recyclerView.getChildLayoutPosition(v);
        BluetoothDevices.BTDeviceModel device = mDevices.getDeviceFromIndex(index);
        Toast.makeText(mContext, "Trying to connect to watch", Toast.LENGTH_LONG).show();
        mBluetoothService.connect(device.mac);
    }
}
