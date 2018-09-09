package com.unime.beacontest.smartcoreinteraction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
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

    private boolean smartCoreConn;
    private final int CONN_CHECK_DELAY_MILLIS = 7500;

    private WifiReceiver wifiReceiver = new WifiReceiver();
    private IntentFilter wifiIntentFilter = new IntentFilter();

    private String TAG = "SmartCoreService";

    public SmartCoreService() {
        super("SmartCoreService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mSmartCoreInteraction = SmartCoreInteraction.getInstance(this);

        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, wifiIntentFilter);
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
                    //Log.d(TAG, "try password: " + passwords.get(0));
                    smartCoreConn = false;

                    Handler handler = new Handler();

                    // Check if the connection was successful after 7500 ms
                    handler.postDelayed(() -> {
                        Log.d(TAG, "onHandleIntent: what the heck " + smartCoreConn);
                        // TODO broadcast just if false, but retry 2 times before that
                    } , CONN_CHECK_DELAY_MILLIS);
                }

                break;
            default:
                Log.e(TAG, "onHandleIntent: Invalid action");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        unregisterReceiver(wifiReceiver);
    }

    public class WifiReceiver extends BroadcastReceiver {
        private final String TAG = "WifiReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if(info != null && info.isConnected()) {
                // Do your work.

                // e.g. To check the Network Name or other info:
                WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = null;
                if (wifiManager != null) {
                    wifiInfo = wifiManager.getConnectionInfo();

                    // It's a little bit tricky, but this call return the name of the ssid between ""
                    String ssid = wifiInfo.getSSID().replace("\"", "");
                    int netId = wifiInfo.getNetworkId();

                    Log.d(TAG, "onReceive:  " + wifiInfo);

                    // todo use netId
                    if(ssid.equals(Settings.ssid)) {
                        smartCoreConn = true;
                        // todo broadcast just if true
                    }
                }

            }

        }
    }
}
