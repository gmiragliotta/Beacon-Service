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
import android.os.HandlerThread;
import android.util.Log;

import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.BeaconResults;
import com.unime.beacontest.beacon.utils.Filter;
import com.unime.beacontest.objectinteraction.SmartObjectIntentService;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.*;
import static com.unime.beacontest.beacon.utils.BeaconResults.BEACON_RESULTS;

public class BeaconReceiver {
    public static final String TAG = "BeaconReceiver";

    private Context context;
    private Filter filter;
    private HandlerThread handlerThread;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback callback;
    private ScanSettings settings;
    private boolean wasDetected = false;
    private int signalThreshold;
    private int scanDuration;
    private String action;     // Purpose of this scan

    private BeaconResults beaconResults = new BeaconResults();

    public BeaconReceiver(Context context, BluetoothAdapter mBluetoothAdapter, Filter filter, HandlerThread handlerThread) {
        this.context = context;
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.filter = filter;
        this.handlerThread = handlerThread;
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
    public void startScanning(int signalThreshold, int scanDuration) { // todo qui
        Log.d(TAG, "startScanning");
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
           // Log.d(TAG, "Stop scanning after " + scanDuration  + " ms " + getAction());
            mBluetoothLeScanner.stopScan(callback);

            // start the right service and pass the results
            Intent intent = new Intent(getContext(), chooseService(getAction()));

            intent.setAction(getAction());
            Log.d(TAG, "getScanCallback: " + beaconResults);
            intent.putExtra(BEACON_RESULTS, beaconResults);

            getContext().startService(intent);

            // Terminate thread
            handlerThread.quitSafely();
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
                                if(null != filter) {
                                    if (filter.apply(data)) {
                                        processResult(data, result);
                                        //Log.d(BEACON_COMMAND_TAG, "run: " + ++numberOfBeaconDetected);
                                        wasDetected = true;
                                    }
                                } else { // No filter!
                                    processResult(data, result);
                                }
                            });
                }
            }
        };
    }

    private void processResult (final byte[] data, final ScanResult result) {
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
        beaconResults.addResult(beaconDetected);

        try {
            Log.d(TAG, "uuid: " + beaconDetected.getUuid() +
                    " major: " + beaconDetected.getMajor() +
                    " minor: " + beaconDetected.getMinor() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Class chooseService (String action) {
        Class chosenService;

        switch(action) {
            case ACTION_SCAN_ACK:
            case ACTION_SEND_COMMAND_OBJ:
                chosenService = SmartObjectIntentService.class;
                break;
            case ACTION_SCAN_SMART_ENV:
            case ACTION_SCAN_PSK:
                chosenService = null; // TODO implement intent service
                break;
            default:
                chosenService = null;
        }

        return chosenService;
    }

}
