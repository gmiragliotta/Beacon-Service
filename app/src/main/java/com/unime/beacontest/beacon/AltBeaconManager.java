package com.unime.beacontest.beacon;

import android.content.Context;
import android.content.Intent;

import com.unime.beacontest.beacon.utils.AltBeaconUtils;

public class AltBeaconManager {
    private static final String TAG = "AltBeaconManager";

    private Context context;

    public AltBeaconManager(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void startAltBeaconService(String action) {
        switch (action) {
            case AltBeaconUtils.ACTION_ALT_BEACON_SCANNING:
                // Log.d(TAG, "startAltBeaconService: start");
                Intent startReceiverIntentService = new Intent(getContext(), BeaconService.class);
                getContext().startService(startReceiverIntentService);
                break;
        }
    }


}
