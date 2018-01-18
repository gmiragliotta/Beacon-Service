package com.unime.beacontest.beacon;

import android.content.Context;
import android.content.Intent;

import com.unime.beacontest.beacon.utils.AltBeaconUtils;

public class AltBeaconManager {
    private String action;
    private Context context;

    public AltBeaconManager(String action, Context context) {
        this.action = action;
        this.context = context;
    }

    public String getAction() {
        return action;
    }

    public void startAltBeaconService(String action) {
        switch (action) {
            case AltBeaconUtils.ACTION_ALT_BEACON_SCANNING:
                Intent startReceiverIntentService = new Intent(context, ReceiverIntentService.class);
                break;
        }
    }




}
