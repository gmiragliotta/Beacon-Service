package com.unime.beacontest.beacon;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.unime.beacontest.beacon.utils.CustomFilter;

public class ReceiverService extends Service {
    public static final String TAG = "ReceiverService";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ReceiverService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ReceiverService.this;
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




}