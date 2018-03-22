package com.unime.beacontest.objectinteraction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.unime.beacontest.beacon.BeaconService;
import com.unime.beacontest.beacon.Settings;
import com.unime.beacontest.beacon.utils.BeaconResults;
import com.unime.beacontest.beacon.utils.CustomFilter;

/**
 *
 */

public class SmartObjectInteraction {
    private static final int SENDING_DURATION_MILLIS = 300;
    private static final int SENDING_DELAY = 0;
    private static final int SCANNING_DURATION_MILLIS = SENDING_DURATION_MILLIS;
    private static final int SCANNING_DELAY_MILLIS = 100;

    private BeaconCommand beaconCommand;
    private BeaconService beaconService;
    private CustomFilter customFilter;
    BeaconResults beaconResults;

    public SmartObjectInteraction(BeaconService beaconService, BeaconCommand beaconCommand,  CustomFilter customFilter) {
        this.beaconService = beaconService;
        this.beaconCommand = beaconCommand;
        this.customFilter = customFilter;
    }

//    public BeaconService getBeaconService() {
//        return beaconService;
//    }
//
//    public void setBeaconCommand(BeaconCommand beaconCommand) {
//        this.beaconCommand = beaconCommand;
//    }

    public void interact() {
        beaconService.sending(beaconCommand.build(), SENDING_DURATION_MILLIS);

        Handler delayScan = new Handler();

        delayScan.postDelayed(
                () -> beaconResults = beaconService.scanning(customFilter, Settings.SIGNAL_THRESHOLD, SCANNING_DURATION_MILLIS),
                SCANNING_DELAY_MILLIS
        );
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
