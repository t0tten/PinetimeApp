package com.example.infinitimeapp.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.infinitimeapp.MainActivity;
import com.example.infinitimeapp.R;
import com.example.infinitimeapp.bluetooth.BluetoothCallback;
import com.example.infinitimeapp.bluetooth.BluetoothDevices;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> implements View.OnClickListener {
    private Context context;
    BluetoothDevices devices;
    private final BluetoothCallback gattCallback;

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

    public RecycleViewAdapter(Context ct) {
        context = ct;
        devices = BluetoothDevices.getInstance();
        gattCallback = new BluetoothCallback();
    }

    @NonNull
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.getDevices().get(position);

        holder.alias.setText(device.getAlias());
        holder.mac.setText(device.getAddress());
        holder.image.setImageResource(R.drawable.watch);
    }

    @Override
    public int getItemCount() {
        return devices.getSize();
    }

    @Override
    public void onClick(View v) {
        int index = MainActivity.recyclerView.getChildLayoutPosition(v);
        BluetoothDevice device = devices.getDeviceFromIndex(index);
        Toast.makeText(context, "Trying to connect to watch", Toast.LENGTH_LONG).show();
        device.connectGatt(context, true, gattCallback);
    }
}
