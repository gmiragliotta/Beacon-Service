package com.unime.beacontest.objectinteraction;

import android.os.Handler;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.unime.beacontest.AES256;
import com.unime.beacontest.beacon.BeaconService;
import com.unime.beacontest.beacon.Settings;
import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.BeaconResults;
import com.unime.beacontest.beacon.utils.CustomFilter;
import com.unime.beacontest.beacon.utils.Filter;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_ACK;

/**
 *
 */

public class SmartObjectInteraction {
    private static final String SMART_OBJECT_INTERACTION_TAG = "SmartObjectInteraction";
    private static final int SENDING_DURATION_MILLIS = 300 + 1700;
    private static final int SENDING_DELAY = 0;
    private static final int SCANNING_DURATION_MILLIS = SENDING_DURATION_MILLIS + 0;
    private static final int SCANNING_DELAY_MILLIS = 100;

    private BeaconCommand beaconCommand;
    private BeaconService beaconService;

    public SmartObjectInteraction(BeaconService beaconService) {
        this.beaconService = beaconService;
    }

    public void setBeaconCommand(BeaconCommand beaconCommand) {
        this.beaconCommand = beaconCommand;
    }

    private CustomFilter AckFilter() {
        CustomFilter.Builder builder = new CustomFilter.Builder();
        Log.d(SMART_OBJECT_INTERACTION_TAG, "AckFilter: " + beaconCommand.getObjectId());
        builder.addFilter(new Filter(Filter.MINOR_TYPE, beaconCommand.getObjectId(), 0, 1));
        return builder.build();
    }

    public void interact() {
        beaconService.sending(beaconCommand.build(), SENDING_DURATION_MILLIS);

        Handler delayScan = new Handler();
        // TODO ADD ACTION CUSTOM TYPE TO THE SCAN OPERATION
        delayScan.postDelayed(
                () -> beaconService.scanning(
                        AckFilter(),
                        Settings.SIGNAL_THRESHOLD,
                        SCANNING_DURATION_MILLIS,
                        ACTION_SCAN_ACK
                ), SCANNING_DELAY_MILLIS
        );
    }

    public void verifyAck(BeaconResults beaconResults) {
        //TODO async

        Handler mHandler = new Handler();

        mHandler.postDelayed(() -> {
            boolean ackFounded = false;
            for(BeaconModel beaconModel : beaconResults.getResults()) {
                try {
                    String clear = AES256.decrypt(BaseEncoding.base16().lowerCase().decode(beaconModel.getUuid()), Settings.key, Settings.iv);
                    if(clear.substring(16, 26).equals("FFFFFF" + Settings.USER_ID)){ // TODO IL COUNTER
                        Log.d("", "verifyAck: ok");
                        ackFounded = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(!ackFounded) {
                Log.d(SMART_OBJECT_INTERACTION_TAG, "verifyAck: Not Founded");
                interact();
            }
        }, 0);
    }

}
