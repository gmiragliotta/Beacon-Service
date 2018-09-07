package com.unime.beacontest.smartcoreinteraction;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.unime.beacontest.AES256;
import com.unime.beacontest.Settings;
import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.BeaconService;
import com.unime.beacontest.beacon.utils.Filter;
import com.unime.beacontest.beacon.utils.ScanFilterUtils;

import org.altbeacon.beacon.Beacon;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.unime.beacontest.Settings.HELLO_BROADCAST_ID;
import static com.unime.beacontest.Settings.HELLO_BROADCAST_MAJOR;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_WIFI_CONN;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_SMART_ENV;

public class SmartCoreInteraction {
    private static final String SMART_CORE_INTERACTION_TAG = "SmartCoreInteraction";
    private static final String HELLO_ACK_ID = "ffffffff";

    private static final int SCANNING_DURATION_SMART_ENV = 1000;
    private static final int SCANNING_DURATION_WIFI_PSK = 1500; // todo occhio
    private static final int SCANNING_DELAY_MILLIS = 0;
    private static final int SCANNING_DELAY_MILLIS_PSK = 150;
    private static final int SENDING_DURATION_MILLIS = 1300; // todo occhio
    private static final int ENCRYPTED_DATA_PAYLOAD_SIZE = 16;
    private static final int HELLO_IV_SIZE = 16;

    public static final String NET_ID_PREF_KEY = "NetworkId";
    public static final String SHARED_PREF_NAME = "SmartCorePreferences";

    public static final int MAX_ACK_RETRY = 2;
    public static final int MAX_CONN_RETRY = 2;

    private static SmartCoreInteraction instance = null;
    private BeaconService beaconService;

    private String helloIv;
    private String objectId;

    private int ackRetryCounter = 0;
    private int connRetryCounter = 0;

    private SmartCoreInteraction(Context context) {
        this.beaconService = new BeaconService(context);
    }

    public static SmartCoreInteraction getInstance(Context context) {
        if(instance == null) {
            instance = new SmartCoreInteraction(context);
        }

        return instance;
    }

    private String getObjectId(){
        return objectId;
    }

    private void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getHelloIv() {
        return helloIv;
    }

    private void setHelloIv(String helloIv) {
        this.helloIv = helloIv;
    }

    public void incAckRetryCounter() {
        ackRetryCounter++;

        if(ackRetryCounter == MAX_ACK_RETRY) {
            ackRetryCounter = 0;
        }
    }

    // todo check if it's necessary
    public void resetAckRetryCounter() {

        if(ackRetryCounter == MAX_ACK_RETRY) {
            ackRetryCounter = 0;
        }
    }

    public void resetConnRetryCounter() {
        if(connRetryCounter == MAX_CONN_RETRY) {
            connRetryCounter = 0;
        }
    }

    public int getAckRetryCounter() {
        return ackRetryCounter;
    }

    public int getConnRetryCounter() {
        return connRetryCounter;
    }

    public void incConnRetryCounter() {
        connRetryCounter++;
    }

    private Filter helloBroadcastFilter = (data) -> {
        //String hexData = BaseEncoding.base16().encode(data);
        int manufacturerId = ScanFilterUtils.getManufacturerId(data);
        String helloBroadcastId = ScanFilterUtils.getHelloBroadcastId(data);

        if(BeaconModel.isAltBeacon(data) &&
                (manufacturerId == Settings.MANUFACTURER_ID) &&
                helloBroadcastId.equals(HELLO_BROADCAST_ID) &&
                BeaconModel.findMajor(data).equals(HELLO_BROADCAST_MAJOR) )
        {
            Log.d(SMART_CORE_INTERACTION_TAG, "helloBroadcastFilter: " + BaseEncoding.base16().lowerCase().encode(data));

            setHelloIv(ScanFilterUtils.getHelloIv(data)); // 16 bytes
            setObjectId(getHelloIv().substring(28,32)); // 2 bytes - minor

            Log.d(SMART_CORE_INTERACTION_TAG, "helloiv: " + getHelloIv());

            return true;
        }
        return false;
    };

    private Filter wifiFilter = (data) -> {
        int manufacturerId = ScanFilterUtils.getManufacturerId(data);

        if(BeaconModel.isAltBeacon(data) &&
                (manufacturerId == Settings.MANUFACTURER_ID) &&
                BeaconModel.findMajor(data).equals(Settings.USER_ID) &&
                BeaconModel.findMinor(data).equals(Settings.OBJECT_ID) )
        {

            return true;
        }
        return false;
    };

    public List<String> getPasswords(List<BeaconModel> results) {
        List<String> passwords = new ArrayList<>();

        for(BeaconModel result : results) {
            // decrypt payload

                String psk = decryptPsk(result);
                passwords.add(psk);

        }
        return passwords;
    }

    private String decryptPsk (BeaconModel result) {
        String psk = "";
        // Log.d(SMART_CORE_INTERACTION_TAG, "decryptPsk: clearuuid "+result.getClearUuid() +"\n" +
        //     getHelloIv());
        try {
            psk = new String(
                BaseEncoding.base16().lowerCase().decode(
                        AES256.decrypt(
                                BaseEncoding.base16().lowerCase().decode(result.getClearUuid()),
                                Settings.key,
                                BaseEncoding.base16().lowerCase().decode(getHelloIv())
                )),    StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return psk;
    }


    public void connectToWifi(String ssid, String psk) {

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = String.format("\"%s\"", Settings.ssid);
        // wifiConfiguration.hiddenSSID = true;
        wifiConfiguration.preSharedKey = String.format("\"%s\"", psk);

        WifiManager wifiManager = (WifiManager) beaconService.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        SharedPreferences sharedPref = beaconService.getContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);;

        boolean wifiConfigFounded = false;
        int netId;

        // TODO hidden ssid wificonfig flag

        if(wifiManager == null) {
            return;
        }

        if(!wifiManager.isWifiEnabled()) {
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: wifiDisabled. Enabling wifi...");
            wifiManager.setWifiEnabled(true);
            return;
        }

        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
        // Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi wificonf " + wifiConfigurations);
        for (WifiConfiguration wifiConfig : wifiConfigurations) {
            // Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: " + wifiConfig.SSID);
            if (wifiConfig.SSID.equals("\"" + ssid + "\"") &&
                    sharedPref.getInt(NET_ID_PREF_KEY, -1) == wifiConfig.networkId) {
                wifiConfigFounded = true;
                wifiConfiguration.networkId = wifiConfig.networkId;
                Log.d(SMART_CORE_INTERACTION_TAG, "ssid: " + wifiConfig.SSID + " password: " + psk);
            }
        }

        if(wifiConfigFounded) {
            netId = wifiManager.updateNetwork(wifiConfiguration);
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: updateNetwork " + netId);
        } else {
            netId = wifiManager.addNetwork(wifiConfiguration);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(NET_ID_PREF_KEY, netId);
            editor.apply();
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: addNetwork " + netId);
        }

        if(netId != -1) {
            boolean isDisconnected = wifiManager.disconnect();
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isDisconnected " + isDisconnected);

            boolean isEnabled = wifiManager.enableNetwork(netId, true);
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isEnabled " + isEnabled);

            // TODO reconnect() doesn't return true if the connection to our network is successfull.
            boolean isReconnected = wifiManager.reconnect();
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isReconnected " + isReconnected);

            // TODO stampa dati precedenti alla riconnessione a volte. Una soluzione è quella di utilizzare il braodcast receiver
            // Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: " + wifiManager.getConnectionInfo());
        }
    }



    public void checkForSmartEnvironment () {
        Log.d(SMART_CORE_INTERACTION_TAG, "checkForSmartEnvironment: start");
        HandlerThread handlerThread = new HandlerThread("BeaconScanSmartEnv");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();

        Handler delayScan = new Handler(looper);
        delayScan.postDelayed(
                () -> {
                    beaconService.scanning(
                            helloBroadcastFilter,
                            Settings.SIGNAL_THRESHOLD,
                            SCANNING_DURATION_SMART_ENV,
                            ACTION_SCAN_SMART_ENV,
                            handlerThread
                    );
                }, SCANNING_DELAY_MILLIS
        );
    }

    public void sendHelloAck () {
        assert getHelloIv() != null;

        Beacon helloAck = new Beacon.Builder()
                .setId1(HELLO_ACK_ID.concat(getHelloIv().substring(0,24)))
                .setId2(Settings.USER_ID)
                .setId3(getObjectId())
                .setManufacturer(Settings.MANUFACTURER_ID)
                .setTxPower(Settings.TX_POWER)
                .setRssi(Settings.RSSI)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
        Log.d(SMART_CORE_INTERACTION_TAG, "sendHelloAck: " + helloAck);
        beaconService.sending(helloAck, SENDING_DURATION_MILLIS);
    }

    public void checkForWifiPassword() {
        HandlerThread handlerThread = new HandlerThread("BeaconScanWifi");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();

        Handler delayScan = new Handler(looper);
        delayScan.postDelayed(
                () -> {
                    beaconService.scanning(
                            wifiFilter,
                            Settings.SIGNAL_THRESHOLD,
                            SCANNING_DURATION_WIFI_PSK,
                            ACTION_WIFI_CONN,
                            handlerThread
                    );
                }, SCANNING_DELAY_MILLIS_PSK
        );
    }

}
