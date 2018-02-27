package com.unime.beacontest.beacon;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.BeaconResults;
import com.unime.beacontest.beacon.utils.CustomFilter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

public class BeaconService extends Service {
    public static final String TAG = "ReceiverService";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private BeaconTransmitter beaconTransmitter;
    private BeaconParser beaconParser;
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconResults beaconResults;
    private BeaconBroadcastReceiver beaconBroadcastReceiver = new BeaconBroadcastReceiver();
    private IntentFilter mIntentFilter = new IntentFilter();

    @Override
    public void onCreate() {
        super.onCreate();

        beaconParser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mIntentFilter.addAction("ActionScanningComplete");
        registerReceiver(beaconBroadcastReceiver, mIntentFilter);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public BeaconService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BeaconService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(beaconBroadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void scanning (CustomFilter customFilter) {
        if(PermissionsChecker.checkBluetoothPermission(getApplicationContext(), mBluetoothAdapter)) {
            BeaconReceiver mBeaconReceiver = new BeaconReceiver(this, mBluetoothAdapter);
            beaconResults = mBeaconReceiver.startScanning(customFilter);
        }
    }

    // TODO add sending functionality Wednesday 28 implement!!!!!
    public void sending (BeaconModel beaconModel, long delayMillis) {
        if (PermissionsChecker.checkBluetoothPermission(getApplicationContext(), mBluetoothAdapter)) {

            if (beaconTransmitter.isStarted()) {
                beaconTransmitter.stopAdvertising();
            }

            Beacon beacon = createBeacon(beaconModel);
            beaconTransmitter.startAdvertising(beacon);

            // stop advertising after 3 seconds
            Handler mCanceller = new Handler();
            mCanceller.postDelayed(() -> beaconTransmitter.stopAdvertising(), delayMillis);
        }
    }

    private Beacon createBeacon(BeaconModel beaconModel) {
        return new Beacon.Builder()
                .setId1(beaconModel.getUuid())
                .setId2(beaconModel.getMajor())
                .setId3(beaconModel.getMinor())
                .setManufacturer(0x0118) // Radius network
                .setTxPower(-59)
                .setRssi(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
    }

    public class BeaconBroadcastReceiver extends BroadcastReceiver {
        public static final String ACTION_SCANNING_COMPLETE = "ActionScanningComplete";
        private static final String TAG = "BeaconBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO do something with this data
            if(intent.getAction().equals(ACTION_SCANNING_COMPLETE)) {
                Log.d(TAG, "onReceive: " + beaconResults.getResults());
            }

        }
    }

}