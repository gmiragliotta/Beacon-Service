package com.unime.beacontest.smartcoreinteraction;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.unime.beacontest.AES256;
import com.unime.beacontest.Settings;
import com.unime.beacontest.beacon.BeaconService;
import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.Filter;
import com.unime.beacontest.beacon.utils.ScanFilterUtils;

import org.altbeacon.beacon.Beacon;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_ACK;

public class SmartCoreInteraction {
    public static final String SMART_CORE_INTERACTION_TAG = "SmartCoreInteraction";

    private static final String HELLO_BROADCAST_ID = "00000000";
    private static final String HELLO_BROADCAST_MAJOR = "ffff";
    private static final String HELLO_ACK_ID = "ffffffff";

    private static final int SCANNING_DURATION_MILLIS = 1000;
    private static final int SCANNING_DELAY_MILLIS = 0;
    private static final int SENDING_DURATION_MILLIS = 300;
    private static final int ENCRYPTED_DATA_PAYLOAD_SIZE = 16;
    private static final int HELLO_IV_SIZE = 16;

    public static final int MAX_ACK_RETRY = 2;

    private BeaconService beaconService;
    private String helloIv;
    private String objectId;
    private int retryCounter = 0;

    public SmartCoreInteraction(BeaconService beaconService, String objectId) {
        this.beaconService = beaconService;
    }

    private String getObjectId(){
        return objectId;
    }

    private void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    private String getHelloIv() {
        return helloIv;
    }

    private void setHelloIv(String helloIv) {
        this.helloIv = helloIv;
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
            setObjectId(getHelloIv().substring(14,16));
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
            // decrypt payload
            byte[] encryptedPayload = new byte[ENCRYPTED_DATA_PAYLOAD_SIZE];
            byte[] helloIvBytes = new byte[HELLO_IV_SIZE];
            System.arraycopy(data, 0, encryptedPayload, 0, ENCRYPTED_DATA_PAYLOAD_SIZE);
            System.arraycopy(BaseEncoding.base16().decode(getHelloIv()), 0, helloIvBytes, 0, HELLO_IV_SIZE);

            try {
                String psk =
                        new String(AES256.decrypt(encryptedPayload, Settings.key, helloIvBytes)
                                .getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8); // TODO check conversion hex to utf8

                return connectToWifi(Settings.ssid, psk);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    };

    private boolean connectToWifi(String ssid, String psk) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = String.format("\"%s\"", Settings.ssid);
        wifiConfiguration.preSharedKey = String.format("\"%s\"", psk);
        WifiManager wifiManager = (WifiManager) beaconService.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // remember network id
        if(wifiManager == null)
            return false;

        int netId = wifiManager.addNetwork(wifiConfiguration);
        if(netId != -1) {
            boolean isDisconnetted = wifiManager.disconnect();
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isDisconnected " + isDisconnetted);

            boolean isEnabled = wifiManager.enableNetwork(netId, true);
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isEnabled " + isEnabled);

            boolean isReconnected = wifiManager.reconnect();
            Log.d(SMART_CORE_INTERACTION_TAG, "connectToWifi: isReconnected " + isReconnected);

            return isReconnected;
        }
        return false;
    }

    public void checkForSmartEnvironment () {
        Handler delayScan = new Handler();
        delayScan.postDelayed(
                () -> beaconService.scanning(
                        helloBroadcastFilter,
                        Settings.SIGNAL_THRESHOLD,
                        SCANNING_DURATION_MILLIS,
                        ACTION_SCAN_ACK
                ), SCANNING_DELAY_MILLIS
        );
    }

    public void sendHelloAck () {
        Beacon helloAck = new Beacon.Builder()
                .setId1(HELLO_ACK_ID.concat(getHelloIv().substring(0,12)))
                .setId2(Settings.USER_ID)
                .setId3(getObjectId())
                .setManufacturer(Settings.MANUFACTURER_ID)
                .setTxPower(Settings.TX_POWER)
                .setRssi(Settings.RSSI)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();

        beaconService.sending(helloAck, SENDING_DURATION_MILLIS);
    }

    public void checkForWifiPassword() {
        Handler delayScan = new Handler();
        delayScan.postDelayed(
                () -> beaconService.scanning(
                        wifiFilter,
                        Settings.SIGNAL_THRESHOLD,
                        SCANNING_DURATION_MILLIS,
                        ACTION_SCAN_ACK
                ), SCANNING_DELAY_MILLIS
        );
    }


}
