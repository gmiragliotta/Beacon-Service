package com.unime.beacontest.objectinteraction;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.unime.beacontest.Config;
import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.BeaconService;
import com.unime.beacontest.beacon.utils.Filter;
import com.unime.beacontest.beacon.utils.ScanFilterUtils;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_ACK;

/**
 *
 */

public class SmartObjectInteraction {
    private static SmartObjectInteraction instance;

    private static final String SMART_OBJECT_INTERACTION_TAG = "SmartObjectInteraction";
    private static final int SENDING_DURATION_MILLIS = 300;
    // private static final int SENDING_DELAY = 0;
    private static final int SCANNING_DURATION_MILLIS = SENDING_DURATION_MILLIS;
    private static final int SCANNING_DELAY_MILLIS = 100;
    public static final int MAX_ACK_RETRY = 2;
    public static final String ACK_VALUE = "ffffff";

    private BeaconCommand beaconCommand;
    private BeaconService beaconService;
    private int retryCounter = 0;

    private Config mConfig;

    private SmartObjectInteraction(Context context) {
        this.beaconService = new BeaconService(context);

        mConfig = Config.getInstance(context);
    }

    public static SmartObjectInteraction getInstance(Context context) {
        if(instance == null) {
            instance = new SmartObjectInteraction(context);
        }

        return instance;
    }

    public void incRetryCounter() {
        retryCounter++;
    }

    public void resetCounter() {
        retryCounter = 0;
    }

    public int getRetryCounter() {
        return retryCounter;
    }

    public void setBeaconCommand(BeaconCommand beaconCommand) {
        this.beaconCommand = beaconCommand;
    }

    private Filter ackFilter = (data) -> {
        //String hexData = BaseEncoding.base16().encode(data);
        int manufacturerId = ScanFilterUtils.getManufacturerId(data);

        if(BeaconModel.isAltBeacon(data) && (manufacturerId == mConfig.getManufacturerId()) &&
                mConfig.getObjectsId().contains(BeaconModel.findMinor(data))) { // TODO bella storia
            Log.d(SMART_OBJECT_INTERACTION_TAG, "ackFilter: " + BaseEncoding.base16().lowerCase().encode(data));
            return true;
        }
        return false;
    };

    public void interact() {
        beaconService.sending(beaconCommand.build(), SENDING_DURATION_MILLIS);

        HandlerThread handlerThread = new HandlerThread("BeaconScanHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();

        Handler delayScan = new Handler(looper);
        delayScan.postDelayed(
                () -> {
                    beaconService.scanning(
                            ackFilter,
                            mConfig.getSignalThreshold(),
                            SCANNING_DURATION_MILLIS,
                            ACTION_SCAN_ACK,
                            handlerThread
                    );
                }, SCANNING_DELAY_MILLIS
        );
    }

    // BeaconModel.findMinor(data).(Config.OBJECT_ID)) {
}
