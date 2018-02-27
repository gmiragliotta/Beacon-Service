package com.unime.beacontest.beacon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.ConversionUtils;

public class BeaconBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_BEACON_RECEIVED = "ActionBeaconReceived";
    private static final String TAG = "BeaconBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO asynchronize
        ScanResult result = intent.getParcelableExtra(BeaconReceiver.RECEIVED_BEACON_EXTRA);

        BluetoothDevice device = result.getDevice();

        byte[] data = result.getScanRecord().getBytes();
        Log.d(TAG, "run: " + ConversionUtils.byteToHex(data));
        BeaconModel beaconDetected = new BeaconModel(
                    BeaconModel.findUUID(data),
                    BeaconModel.findMajor(data),
                    BeaconModel.findMinor(data),
                    BeaconModel.findTxPower(data), // API 26 required getTxPower method
                    result.getRssi(),
                    result.getTimestampNanos(),
                    device.getAddress()
            );

        try {
            Log.d(TAG, "uuid: " + beaconDetected.getUuid() +
                    " major: " + beaconDetected.getMajor() +
                    " minor: " + beaconDetected.getMinor() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}