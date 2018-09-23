package com.unime.ctrlbeacon.beacon.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.unime.ctrlbeacon.beacon.BeaconReceiver;
import com.unime.ctrlbeacon.beacon.PermissionsChecker;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

public class BeaconService {
    private static final String BEACON_SERVICE_TAG = "BeaconService";

    private Context context;

    private BeaconTransmitter beaconTransmitter;
    private BluetoothAdapter mBluetoothAdapter;

    public BeaconService(Context context) {
        this.context = context;

        BeaconParser beaconParser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getContext(), beaconParser);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);

    }

    public Context getContext() {
        return context;
    }

    public void scanning (Filter filter, int signalThreshold, int scanDuration, String action, HandlerThread handlerThread) {
        if(PermissionsChecker.checkBluetoothPermission(getContext(), mBluetoothAdapter)) {
            BeaconReceiver mBeaconReceiver = new BeaconReceiver(getContext(), mBluetoothAdapter, filter, handlerThread);
            mBeaconReceiver.setAction(action);
            mBeaconReceiver.startScanning(signalThreshold, scanDuration);
        }
    }

    public void sending (Beacon beacon, long delayMillis) {
        if (PermissionsChecker.checkBluetoothPermission(getContext(), mBluetoothAdapter)) {

            if (beaconTransmitter.isStarted()) {
                beaconTransmitter.stopAdvertising();
            }

            beaconTransmitter.startAdvertising(beacon);
            Log.d(BEACON_SERVICE_TAG, "sending: " + beacon);

            HandlerThread handlerThread = new HandlerThread("BeaconAdvHandlerThread");
            handlerThread.start();
            Looper looper = handlerThread.getLooper();

            // stop advertising after  delayMillis
            Handler mCanceller = new Handler(looper);
            mCanceller.postDelayed(() -> {
                beaconTransmitter.stopAdvertising();
                Log.d(BEACON_SERVICE_TAG, "Advertisement stopped after " + delayMillis + " ms");
                handlerThread.quit();
            }, delayMillis);
        }
    }
}
