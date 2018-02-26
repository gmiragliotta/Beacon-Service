package com.unime.beacontest.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.ConversionUtils;
import com.unime.beacontest.beacon.utils.CustomFilter;
import com.unime.beacontest.beacon.utils.ScanFilterUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeaconReceiver {
    public static final String TAG = "BeaconReceiver";
    private static final int SIGNAL_THRESHOLD = -100;
    private static final int SCAN_DURATION = 10 * 1000; // 3 seconds

    public static final String RECEIVED_BEACON_EXTRA = "ReceivedBeaconExtra";

    private Context context;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback callback = getScanCallback();
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private static Set<BeaconModel> founded = new HashSet<>();
    private boolean wasDetected = false;


    public BeaconReceiver(Context context) {
        this.context = context;

        try {
            mBluetoothAdapter = ((BluetoothManager)
                    context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public Context getContext() {
        return context;
    }


    public void startScanning(CustomFilter customFilter) {
        settings = getScanSettings(ScanSettings.SCAN_MODE_LOW_LATENCY);
        filters = getScanFilters(customFilter);
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, callback);
    }

    private static ScanSettings getScanSettings(int mode) {
        final ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(0);
        builder.setScanMode(mode);
        return builder.build();
    }

    private ScanCallback getScanCallback() {
        final Handler scanHandler = new Handler();

        // if no beacon detected in 3 seconds, stopscan

            final Handler mCanceller = new Handler();
            mCanceller.postDelayed(() -> {
                if (!wasDetected) {
                    Log.d(TAG, "No beacon detected after " + SCAN_DURATION/1000 + " seconds: stopScanning");
                    mBluetoothLeScanner.stopScan(callback);
                }
            }, SCAN_DURATION);

        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, final ScanResult result) {
                final int RSSI = result.getRssi();

                if (RSSI >= SIGNAL_THRESHOLD) {
                    scanHandler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    BluetoothDevice device = result.getDevice();

                                    byte[] data = result.getScanRecord().getBytes();
                                    Log.d(TAG, "run: " + ConversionUtils.byteToHex(data));
                                    BeaconModel beaconDetected = null;

                                    if (BeaconModel.isBeacon(data)) {
                                        beaconDetected = new BeaconModel(
                                                BeaconModel.findUUID(data),
                                                BeaconModel.findMajor(data),
                                                BeaconModel.findMinor(data),
                                                BeaconModel.findTxPower(data), // API 26 required getTxPower method
                                                result.getRssi(),
                                                result.getTimestampNanos(),
                                                device.getAddress()
                                        );

                                        Intent beaconReceivedIntent =
                                                new Intent(getContext(), BeaconBroadcastReceiver.class);
                                        beaconReceivedIntent.setAction(BeaconBroadcastReceiver.ACTION_BEACON_RECEIVED);
                                        beaconReceivedIntent.putExtra(RECEIVED_BEACON_EXTRA, beaconDetected);
                                        getContext().sendBroadcast(beaconReceivedIntent);
                                        //wasDetected = true;
                                        //mBluetoothLeScanner.stopScan(callback);
//                                        Log.d(TAG, "Beacon detected: stopScanning");
//                                        try {
//                                            Log.d(TAG, "uuid: " + beaconDetected.getUuid() +
//                                                    " major: " + beaconDetected.getMajor() +
//                                                    " minor: " + beaconDetected.getMinor() + "\n");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
                                    }
//                                    Log.d(TAG, "set: " + founded);
                                }
                            });
                }
            }
        };
    }

    private static List<ScanFilter> getScanFilters(CustomFilter customFilter) {
        List<ScanFilter> filters = new ArrayList<>();

        filters.add(ScanFilterUtils.getScanFilter(customFilter));

        return filters;
        //return null;
    }
}
