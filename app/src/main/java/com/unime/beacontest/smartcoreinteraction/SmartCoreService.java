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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.unime.beacontest.Config;
import com.unime.beacontest.beacon.utils.BeaconResults;

import java.util.List;
import java.util.Random;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_PSK;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_SMART_ENV;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_WIFI_CONN;
import static com.unime.beacontest.beacon.utils.BeaconResults.BEACON_RESULTS;

public class SmartCoreService extends NonStopIntentService {
    public static final String ACTION_SMARTCORE_CONN = "SmartCoreConn";
    public static final String SMARTCORE_CONN_STATUS = "SmartCoreStatus";

    private SmartCoreInteraction mSmartCoreInteraction;
    private LocalBroadcastManager localBroadcastManager;

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
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

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
                // pick one password from the scan result
                if (passwords.size() >= 1) {
                    mSmartCoreInteraction.resetAckRetryCounter(); // password received

                    Random rand = new Random();
                    int i = rand.nextInt(passwords.size());

                    mSmartCoreInteraction.connectToWifi(Config.getInstance(this).getSsid(), passwords.get(i));
                    Log.d(TAG, "try password 0: " + passwords.get(0));
                    smartCoreConn = false;

                    Handler handler = new Handler();

                    // Check if the connection was successful after 7500 ms
                    handler.postDelayed(() -> {
                        Log.d(TAG, "onHandleIntent: what the heck " + smartCoreConn);
                        // local broadcast connection status just if it's false, but retry 2 times before that
                        if(mSmartCoreInteraction.getConnRetryCounter() == SmartCoreInteraction.MAX_CONN_RETRY && !smartCoreConn){
                            localBroadcastManager.sendBroadcast(
                                new Intent(ACTION_SMARTCORE_CONN).putExtra(SMARTCORE_CONN_STATUS, smartCoreConn)
                            );

                            mSmartCoreInteraction.resetConnRetryCounter();
                        } else if (!smartCoreConn) {
                            mSmartCoreInteraction.incConnRetryCounter();
                            mSmartCoreInteraction.checkForWifiPassword();
                        } else { // conn successful
                            mSmartCoreInteraction.resetConnRetryCounter();
                        }
                    } , CONN_CHECK_DELAY_MILLIS);
                } else { // i haven't received password
                    if(mSmartCoreInteraction.getAckRetryCounter() == SmartCoreInteraction.MAX_ACK_RETRY) {
                        mSmartCoreInteraction.resetAckRetryCounter();
                    } else {
                        mSmartCoreInteraction.incAckRetryCounter();
                        //startService(new Intent(this, SmartCoreService.class).setAction(ACTION_SCAN_PSK));
                        onHandleIntent(new Intent(this, SmartCoreService.class).setAction(ACTION_SCAN_PSK));
                    }
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
                    if(ssid.equals(Config.getInstance(context).getSsid())) {
                        smartCoreConn = true;
                        localBroadcastManager.sendBroadcast(new Intent(ACTION_SMARTCORE_CONN).putExtra(SMARTCORE_CONN_STATUS, true));

                    }
                }

            }

        }
    }
}
