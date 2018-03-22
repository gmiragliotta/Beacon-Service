package com.unime.beacontest.objectinteraction;

import android.os.Handler;

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

    public SmartObjectInteraction(BeaconService beaconService, BeaconCommand beaconCommand,  CustomFilter customFilter) {
        this.beaconService = beaconService;
        this.beaconCommand = beaconCommand;
        this.customFilter = customFilter;
    }

    private CustomFilter AckFilter()

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
        // TODO ADD ACTION CUSTOM TYPE TO THE SCAN OPERATION
        delayScan.postDelayed(
                () -> beaconService.scanning(customFilter, Settings.SIGNAL_THRESHOLD, SCANNING_DURATION_MILLIS),
                SCANNING_DELAY_MILLIS
        );
    }

    public void verifyAck(BeaconResults beaconResults) {

    }

}
