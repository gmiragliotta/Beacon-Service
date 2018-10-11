package com.unime.ctrlbeacon.smartcoreinteraction;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.unime.ctrlbeacon.beacon.utils.AES256;
import com.unime.ctrlbeacon.Config;
import com.unime.ctrlbeacon.beacon.utils.BeaconModel;
import com.unime.ctrlbeacon.beacon.BeaconService;
import com.unime.ctrlbeacon.beacon.utils.Filter;
import com.unime.ctrlbeacon.beacon.utils.ScanFilterUtils;

import org.altbeacon.beacon.Beacon;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.unime.ctrlbeacon.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_SMART_ENV_RESULTS;
import static com.unime.ctrlbeacon.beacon.ActionsBeaconBroadcastReceiver.ACTION_WIFI_CONN;

public class SmartCoreInteraction {
    private static final String SMART_CORE_INTERACTION_TAG = "SmartCoreInteraction";
    private static final String HELLO_ACK_ID = "ffffffff";

    private static final int SCANNING_DURATION_SMART_ENV = 450;
    private static final int SCANNING_DURATION_WIFI_PSK = 450;
    private static final int SCANNING_DELAY_MILLIS = 0;
    private static final int SCANNING_DELAY_MILLIS_PSK = 150;
    private static final int SENDING_DURATION_MILLIS = 450;
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

    private Config mConfig;

    private SmartCoreInteraction(Context context) {
        this.beaconService = new BeaconService(context);
        mConfig = Config.getInstance(context);
    }

    public static SmartCoreInteraction getInstance(Context context) {
        if (instance == null) {
            instance = new SmartCoreInteraction(context);
        }

        return instance;
    }

    private String getObjectId() {
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

        Log.d(SMART_CORE_INTERACTION_TAG, "incAckRetryCounter: " + ackRetryCounter);
    }

    public void resetAckRetryCounter() {
        ackRetryCounter = 0;
        Log.d(SMART_CORE_INTERACTION_TAG, "resetAckRetryCounter");
    }

    public void resetConnRetryCounter() {
        connRetryCounter = 0;
        Log.d(SMART_CORE_INTERACTION_TAG, "resetConnRetryCounter");
    }

    public int getAckRetryCounter() {
        return ackRetryCounter;
    }

    public int getConnRetryCounter() {
        return connRetryCounter;
    }

    public void incConnRetryCounter() {
        connRetryCounter++;
        Log.d(SMART_CORE_INTERACTION_TAG, "incConnRetryCounter: " + connRetryCounter);
    }

    private Filter helloBroadcastFilter = (data) -> {
        //String hexData = BaseEncoding.base16().encode(data);
        int manufacturerId = ScanFilterUtils.getManufacturerId(data);
        String helloBroadcastId = ScanFilterUtils.getHelloBroadcastId(data);

        if (BeaconModel.isAltBeacon(data) &&
                (manufacturerId == mConfig.getManufacturerId()) &&
                helloBroadcastId.equals(mConfig.getHelloBroadcastId()) &&
                BeaconModel.findMajor(data).equals(mConfig.getHelloBroadcastMajor())) {
            Log.d(SMART_CORE_INTERACTION_TAG, "helloBroadcastFilter: " + BaseEncoding.base16().lowerCase().encode(data));

            setHelloIv(ScanFilterUtils.getHelloIv(data)); // 16 bytes
            setObjectId(getHelloIv().substring(28, 32)); // 2 bytes - minor

            Log.d(SMART_CORE_INTERACTION_TAG, "helloiv: " + getHelloIv());

            return true;
        }
        return false;
    };

    private Filter wifiFilter = (data) -> {
        int manufacturerId = ScanFilterUtils.getManufacturerId(data);

        if (BeaconModel.isAltBeacon(data) &&
                (manufacturerId == mConfig.getManufacturerId()) &&
                BeaconModel.findMajor(data).equals(mConfig.getUserId()) &&
                BeaconModel.findMinor(data).equals(mConfig.getSmartCoreId())) {

            return true;
        }
        return false;
    };

    public List<String> getPasswords(List<BeaconModel> results) {
        List<String> passwords = new ArrayList<>();

        for (BeaconModel result : results) {
            // decrypt payload

            String psk = decryptPsk(result);
            passwords.add(psk);

        }
        return passwords;
    }

    private String decryptPsk(BeaconModel result) {
        String psk = "";
        // Log.d(SMART_CORE_INTERACTION_TAG, "decryptPsk: clearuuid "+result.getClearUuid() +"\n" +
        //     getHelloIv());
        try {
            psk = new String(
                    BaseEncoding.base16().lowerCase().decode(
                            AES256.decrypt(
                                    BaseEncoding.base16().lowerCase().decode(result.getClearUuid()),
                                    mConfig.getKey(),
                                    BaseEncoding.base16().lowerCase().decode(getHelloIv())
                            )), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return psk;
    }


    public void connectToWifi(String ssid, String psk) {

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = String.format("\"%s\"", mConfig.getSsid());
        // wifiConfiguration.hiddenSSID = true;
        wifiConfiguration.preSharedKey = String.format("\"%s\"", psk);

        WifiManager wifiManager = (WifiManager) beaconService.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        SharedPreferences sharedPref = beaconService.getContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        boolean wifiConfigFounded = false;
        int netId;

        if (wifiManager == null) {
            return;
        }

        if (!wifiManager.isWifiEnabled()) {
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: wifiDisabled. Enabling wifi...");
            wifiManager.setWifiEnabled(true);
        }

        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
        // Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi wificonf " + wifiConfigurations);
        for (WifiConfiguration wifiConfig : wifiConfigurations) {

            // Check if ssid and the networkId match. We check even the networkId, because the user
            // could add a wifi with the same ssid.
            // N.B. if there is already a network with the same ssid, wifiManager.addNetwork(wifiConfiguration)
            // return -1
            if (wifiConfig.SSID.equals("\"" + ssid + "\"") &&
                    sharedPref.getInt(NET_ID_PREF_KEY, -1) == wifiConfig.networkId) {
                wifiConfigFounded = true;
                wifiConfiguration.networkId = wifiConfig.networkId;
                Log.d(SMART_CORE_INTERACTION_TAG, "ssid: " + wifiConfig.SSID + " password: " + psk + " " + wifiConfiguration.networkId);
            }


//            Log.d(SMART_CORE_INTERACTION_TAG, "ssid: " + wifiConfig.SSID + " password: " + wifiConfig.preSharedKey + " netId " + wifiConfig.networkId);
        }

        if (wifiConfigFounded) {
            netId = wifiManager.updateNetwork(wifiConfiguration);
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: updateNetwork " + netId);
        } else {
            netId = wifiManager.addNetwork(wifiConfiguration);
            if (netId != -1) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(NET_ID_PREF_KEY, netId);
                editor.apply();
                Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: addNetwork " + netId);
            }
        }

        if (netId != -1) {
            boolean isDisconnected = wifiManager.disconnect();
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isDisconnected " + isDisconnected);

            boolean isEnabled = wifiManager.enableNetwork(netId, true);
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isEnabled " + isEnabled);

            boolean isReconnected = wifiManager.reconnect();
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isReconnected " + isReconnected);
        }
    }

    public void checkForSmartEnvironment() {
        Log.d(SMART_CORE_INTERACTION_TAG, "checkForSmartEnvironment: start");
        HandlerThread handlerThread = new HandlerThread("BeaconScanSmartEnv");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();

        Handler delayScan = new Handler(looper);
        delayScan.postDelayed(
                () -> {
                    beaconService.scanning(
                            helloBroadcastFilter,
                            mConfig.getSignalThreshold(),
                            SCANNING_DURATION_SMART_ENV,
                            ACTION_SCAN_SMART_ENV_RESULTS,
                            handlerThread
                    );
                }, SCANNING_DELAY_MILLIS
        );
    }

    public void sendHelloAck() {
        assert getHelloIv() != null;

        Beacon helloAck = new Beacon.Builder()
                .setId1(HELLO_ACK_ID.concat(getHelloIv().substring(0, 24)))
                .setId2(mConfig.getUserId())
                .setId3(getObjectId())
                .setManufacturer(mConfig.getManufacturerId())
                .setTxPower(Config.TX_POWER)
                .setRssi(Config.RSSI)
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
                            mConfig.getSignalThreshold(),
                            SCANNING_DURATION_WIFI_PSK,
                            ACTION_WIFI_CONN,
                            handlerThread
                    );
                }, SCANNING_DELAY_MILLIS_PSK
        );
    }

}
