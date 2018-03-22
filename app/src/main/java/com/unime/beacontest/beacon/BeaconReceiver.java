package com.unime.beacontest.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.BeaconResults;
import com.unime.beacontest.beacon.utils.CustomFilter;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_COMPLETE;
import static com.unime.beacontest.beacon.utils.ScanFilterUtils.toFilter;

public class BeaconReceiver {
    public static final String TAG = "BeaconReceiver";

    private Context context;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback callback;
    private ScanSettings settings;
    private boolean wasDetected = false;
    private int signalThreshold;
    private int scanDuration;
    private String action;     // Purpose of this scan

    private BeaconResults beaconResults = new BeaconResults();

    public BeaconReceiver(Context context, BluetoothAdapter mBluetoothAdapter) {
        this.context = context;
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.action = ACTION_SCAN_COMPLETE;
    }

    public Context getContext() {
        return context;
    }


    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    // start scanning and return a BeaconResults instance
    public void startScanning(CustomFilter customFilter, int signalThreshold, int scanDuration) {
        this.signalThreshold = signalThreshold;
        this.scanDuration = scanDuration;
        settings = getScanSettings();
        callback = getScanCallback();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(null, settings, callback);
    }

    private static ScanSettings getScanSettings() {
        final ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(0);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        // TODO scansettings CALLBACK_TYPE_FIRST_MATCH = 2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
        }
        return builder.build();
    }

    private ScanCallback getScanCallback() {
        final Handler scanHandler = new Handler();
        final Handler mCanceller = new Handler();

        mCanceller.postDelayed(() -> {
            if (!wasDetected) {
                Log.d(TAG, "No beacon detected after " + scanDuration  + " ms: stopScanning");
            }
            Log.d(TAG, "Stop scanning after " + scanDuration  + " ms");
            mBluetoothLeScanner.stopScan(callback);

            // broadcast ActionScanningComplete message
            Intent intent = new Intent();
            Log.d(TAG, "getScanCallback: action " + getAction());
            intent.setAction(getAction());
            //intent.putExtra(BEACON_RESULTS, beaconResults);
            getContext().sendBroadcast(intent);
        }, scanDuration);

        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, final ScanResult result) {
                final int RSSI = result.getRssi();

                if (RSSI >= signalThreshold) {
                    scanHandler.post(
                            () -> {
                                byte[] data = result.getScanRecord().getBytes();


                                //Log.d(TAG, "run: " + ConversionUtils.byteToHex(data) + data.length);

                                if (toFilter(data)) {

                                    BluetoothDevice device = result.getDevice();

                                    //Log.d(BEACON_COMMAND_TAG, "run: " + ConversionUtils.byteToHex(data));
                                    BeaconModel beaconDetected = new BeaconModel(
                                            BeaconModel.findUUID(data),
                                            BeaconModel.findMajor(data),
                                            BeaconModel.findMinor(data),
                                            BeaconModel.findTxPower(data), // API 26 required getTxPower method
                                            result.getRssi(),
                                            result.getTimestampNanos(),
                                            device.getAddress()
                                    );
                                    beaconResults.addResults(beaconDetected);

                                    try {
                                        Log.d(TAG, "uuid: " + beaconDetected.getUuid() +
                                                " major: " + beaconDetected.getMajor() +
                                                " minor: " + beaconDetected.getMinor() + "\n");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //Log.d(BEACON_COMMAND_TAG, "run: " + ++numberOfBeaconDetected);
                                    wasDetected = true;
                                }
                            });
                }
            }
        };
    }
}
