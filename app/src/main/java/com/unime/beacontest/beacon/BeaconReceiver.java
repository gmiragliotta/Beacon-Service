package com.unime.beacontest.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;

public class BeaconReceiver {
    public final String TAG = "BeaconReceiver";

    private Context context;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;

    public BeaconReceiver(Context context) {
        this.context = context;

        mBluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

    }

    public Context getContext() {
        return context;
    }


    private void startScanning(String s) {
        mBluetoothLeScanner.startScan(filters, settings, callback);
    }

    



}
