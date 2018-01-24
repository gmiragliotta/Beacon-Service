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
import android.os.Handler;
import android.util.Log;

import com.unime.beacontest.beacon.utils.BeaconModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BeaconReceiver {
    public static final String TAG = "BeaconReceiver";
    private static final int SIGNAL_THRESHOLD = -70;

    private Context context;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback callback;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private List<BeaconModel> founded;

    public BeaconReceiver(Context context) {
        this.context = context;

        mBluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        founded = new ArrayList<>();

    }

    public Context getContext() {
        return context;
    }


    public void startScanning(String s) {
        callback = getScanCallback();
        settings = getScanSettings(ScanSettings.SCAN_MODE_LOW_LATENCY);
        filters = getScanFilters();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, callback);
    }

    private static ScanSettings getScanSettings(int mode)
    {
        final ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(0);
        builder.setScanMode(mode);
        return builder.build();
    }

    private static ScanCallback getScanCallback(){
        final Handler scanHandler = new Handler();
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, final ScanResult result) {
//                Log.i("callbackType", String.valueOf(callbackType));
//                Log.i("result", result.toString());
                final int RSSI = result.getRssi();

                if (RSSI >= SIGNAL_THRESHOLD) {
                    scanHandler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    BluetoothDevice device = result.getDevice();

                                    Log.d(TAG, "Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");
                                    
                                }
                            });
                }
            }
        };
    }

    private static String getBeaconID(ScanResult result){
        String UUIDx = UUID
        // ToastMakers.message(scannerActivity.getApplicationContext(), UUIDx);

        Log.e("UUID", " as String ->>" + UUIDx);
        return UUIDx;
    }

    private static List<ScanFilter> getScanFilters(){
        // TODO ADD FILTERS HERE
        return new ArrayList<>();
    }

    public class BeaconID {

    }


}
