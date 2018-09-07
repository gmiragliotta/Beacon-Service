package com.unime.beacontest.smartcoreinteraction;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.unime.beacontest.Settings;
import com.unime.beacontest.beacon.utils.BeaconResults;

import java.util.List;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_PSK;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_SMART_ENV;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_WIFI_CONN;
import static com.unime.beacontest.beacon.utils.BeaconResults.BEACON_RESULTS;

public class SmartCoreService extends NonStopIntentService {
    private SmartCoreInteraction mSmartCoreInteraction;

    private String TAG = "SmartCoreService";

    public SmartCoreService() {
        super("SmartCoreService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSmartCoreInteraction = SmartCoreInteraction.getInstance(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if ((intent != null ? intent.getAction() : null) == null) {
            return;
        }

        BeaconResults beaconResults;

        switch (intent.getAction()) {
            case ACTION_SCAN_SMART_ENV:
                beaconResults = intent.getParcelableExtra(BEACON_RESULTS);
                Log.d(TAG, "beaconResults: " + beaconResults.getResults());

                // todo send local broadcast
                // bla bla bla
                break;
            case ACTION_SCAN_PSK:
                if (mSmartCoreInteraction.getHelloIv() != null) {
                    mSmartCoreInteraction.sendHelloAck();
                    mSmartCoreInteraction.checkForWifiPassword();
                }
                break;
            case ACTION_WIFI_CONN:
                beaconResults = intent.getParcelableExtra(BEACON_RESULTS);
                List<String> passwords = mSmartCoreInteraction.getPasswords(beaconResults.getResults());

                Log.d(TAG, "onHandleIntent: passwords " + passwords);
                // todo try all passwords, for now just the first one? use rand func, interesting
                if (passwords.size() >= 1) {
                    mSmartCoreInteraction.connectToWifi(Settings.ssid, passwords.get(0));
                    Log.d(TAG, "try password: " + passwords.get(0));
                }

                break;
            default:
                Log.e(TAG, "onHandleIntent: Invalid action");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
