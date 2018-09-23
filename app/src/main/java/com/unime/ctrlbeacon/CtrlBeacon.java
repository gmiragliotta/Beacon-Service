package com.unime.ctrlbeacon;

import android.content.Context;
import android.content.Intent;

import com.unime.ctrlbeacon.objectinteraction.BeaconCommand;
import com.unime.ctrlbeacon.objectinteraction.SmartObjectIntentService;
import com.unime.ctrlbeacon.smartcoreinteraction.SmartCoreService;

import static com.unime.ctrlbeacon.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_PSK;
import static com.unime.ctrlbeacon.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_SMART_ENV;
import static com.unime.ctrlbeacon.beacon.ActionsBeaconBroadcastReceiver.ACTION_SEND_COMMAND_OBJ;
import static com.unime.ctrlbeacon.objectinteraction.SmartObjectIntentService.EXTRA_BEACON_COMMAND;

public class CtrlBeacon {
    public static void startSmartEnvScan(Context context) {
        context = context.getApplicationContext();

        Intent myIntent = new Intent(context, SmartCoreService.class);
        myIntent.setAction(ACTION_SCAN_SMART_ENV);
        context.startService(myIntent);
    }

    public static void startSmartCoreConn(Context context) {
        context = context.getApplicationContext();

        Intent myIntent = new Intent(context, SmartCoreService.class);
        myIntent.setAction(ACTION_SCAN_PSK);
        context.startService(myIntent);
    }

    public static void sendCommand(BeaconCommand beaconCommand, Context context) {
        context = context.getApplicationContext();

        Intent myIntent = new Intent(context, SmartObjectIntentService.class);
        myIntent.setAction(ACTION_SEND_COMMAND_OBJ);
        myIntent.putExtra(EXTRA_BEACON_COMMAND, beaconCommand);

        context.startService(myIntent);
    }
}
