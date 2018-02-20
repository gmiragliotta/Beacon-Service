package com.unime.beacontest.beacon;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.unime.beacontest.beacon.utils.BeaconModel;
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

    @Override
    public void onCreate() {
        super.onCreate();
        beaconParser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void scanning (CustomFilter customFilter) {
        BeaconReceiver mBeaconReceiver = new BeaconReceiver(this);
        mBeaconReceiver.startScanning(customFilter);
    }

    // TODO add sending functionality

    public void starting (BeaconModel beaconModel) {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getApplicationContext().startActivity(enableBtIntent);
        }

        if (beaconTransmitter.isStarted()) {
            beaconTransmitter.stopAdvertising();
        }

        Beacon beacon = createBeacon(beaconModel);
        beaconTransmitter.startAdvertising(beacon);

        // stop advertising after 3 seconds
        Handler mCanceller = new Handler();
        mCanceller.postDelayed(() -> beaconTransmitter.stopAdvertising(), 3000);

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

}