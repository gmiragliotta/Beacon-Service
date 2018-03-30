package com.unime.beacontest.beacon;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import com.unime.beacontest.AES256;
import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.BeaconResults;
import com.unime.beacontest.beacon.utils.Filter;
import com.unime.beacontest.objectinteraction.SmartObjectInteraction;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import static com.unime.beacontest.objectinteraction.SmartObjectInteraction.ACK_VALUE;

public class BeaconService extends Service {
    public static final String BEACON_SERVICE_TAG = "BeaconService";
    private static final int COUNTER_INDEX_START = 0;
    private static final int COUNTER_INDEX_END = 16; // excluded
    private static final int COMMAND_INDEX_START = 16;
    private static final int COMMAND_INDEX_END = 26;


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
        beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
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
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void scanning (Filter filter, int signalThreshold, int scanDuration, String action) {
        if(PermissionsChecker.checkBluetoothPermission(getApplicationContext(), mBluetoothAdapter)) {
            BeaconReceiver mBeaconReceiver = new BeaconReceiver(this, mBluetoothAdapter, filter);
            mBeaconReceiver.setAction(action);
            mBeaconReceiver.startScanning(signalThreshold, scanDuration);
        }
    }

    public void sending (Beacon beacon, long delayMillis) {
        if (PermissionsChecker.checkBluetoothPermission(getApplicationContext(), mBluetoothAdapter)) {

            if (beaconTransmitter.isStarted()) {
                beaconTransmitter.stopAdvertising();
            }

            beaconTransmitter.startAdvertising(beacon);

            // stop advertising after  delayMillis
            Handler mCanceller = new Handler();
            mCanceller.postDelayed(() -> {
                beaconTransmitter.stopAdvertising();
                Log.d(BEACON_SERVICE_TAG, "Advertisement stopped after " + delayMillis + " ms");
                }, delayMillis);
        }
    }

    public void verifyAck(BeaconResults beaconResults, SmartObjectInteraction mSmartObjectInteraction) {
        //TODO async

        Handler mHandler = new Handler();

        mHandler.postDelayed(() -> {
            boolean ackFounded = false;

            UnsignedLong counter = Settings.counter;

            for(BeaconModel beaconModel : beaconResults.getResults()) {
                try {
                    String clear = AES256.decrypt(BaseEncoding.base16().lowerCase().decode(
                            beaconModel.getClearUuid()), Settings.key, Settings.iv);
                    Log.d(BEACON_SERVICE_TAG, "verifyAck clear: " + clear);

                    Log.d(BEACON_SERVICE_TAG, "verifyAck first check -> " +
                            UnsignedLong.valueOf(clear.substring(COUNTER_INDEX_START, COUNTER_INDEX_END))
                                    .equals(counter.plus(UnsignedLong.valueOf(1))) + " " + counter.toString());
                    Log.d(BEACON_SERVICE_TAG, "verifyAck: second check -> " +
                            clear.substring(16, 26).equals(ACK_VALUE + Settings.USER_ID));

                    if(UnsignedLong.valueOf(clear.substring(COUNTER_INDEX_START, COUNTER_INDEX_END))
                            .equals(counter.plus(UnsignedLong.valueOf(1))) &&
                            clear.substring(COMMAND_INDEX_START, COMMAND_INDEX_END).equals(ACK_VALUE + Settings.USER_ID)
                            ) {
                        Log.d(BEACON_SERVICE_TAG, "verifyAck: ok");
                        Settings.counter = Settings.counter.plus(UnsignedLong.valueOf(1));
                        Log.d(BEACON_SERVICE_TAG, "New counter value -> " + Settings.counter.toString());
                        ackFounded = true;
                    }
                } catch (Exception e) {
                    Log.e(BEACON_SERVICE_TAG, "verifyAck: " + e.getMessage());
                }
            }

            if(!ackFounded && (mSmartObjectInteraction.getRetryCounter() < SmartObjectInteraction.MAX_ACK_RETRY)) {
                Log.d(BEACON_SERVICE_TAG, "verifyAck: Not Founded");
                mSmartObjectInteraction.incRetryCounter();
                mSmartObjectInteraction.interact();
            }
        }, 0);
    }
}