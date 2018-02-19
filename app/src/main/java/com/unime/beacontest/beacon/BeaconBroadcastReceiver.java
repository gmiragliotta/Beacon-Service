package com.unime.beacontest.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.unime.beacontest.beacon.utils.BeaconModel;

public class BeaconBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_BEACON_RECEIVED = "ActionBeaconReceived";

    @Override
    public void onReceive(Context context, Intent intent) {
        BeaconModel beaconReceived = (BeaconModel) intent.getSerializableExtra(BeaconReceiver.RECEIVED_BEACON_EXTRA);
        try {
            Log.d("prova", "uuid: " + beaconReceived.getUuid() +
                    " major: " + beaconReceived.getMajor() +
                    " minor: " + beaconReceived.getMinor() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}