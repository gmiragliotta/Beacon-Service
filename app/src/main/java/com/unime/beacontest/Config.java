package com.unime.beacontest;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;

import java.util.List;

public class Config {
    private final static String SHARED_PREF_CONFIG = "SharedPrefConfig";
    private static final String MANUFACTURER_ID_KEY = "ManufacturerIdKey";
    private static final String SIGNAL_THRESHOLD_KEY = "SignalThresholdKey";
    private static final String HELLO_BROADCAST_ID_KEY = "HelloBroadcastIdKey";
    private static final String HELLO_BROADCAST_MAJOR_KEY = "HelloBroadcastMajorKey";
    private static final String USER_ID_KEY = "UserIdKey";
    private static final String COUNTER_KEY = "CounterKey";
    private static final String SSID_KEY = "SsidKey";
    private static final String KEY_KEY = "KeyKey";
    private static final String IV_KEY = "IvKey";
    private static final String SMART_CORE_ID_KEY = "SmartCoreIdKey";

    private static int MANUFACTURER_ID = 0x8888;
    private static int SIGNAL_THRESHOLD = -70;

    private static String HELLO_BROADCAST_ID = "00000000";
    private static String HELLO_BROADCAST_MAJOR = "ffff";
    private static String ssid = "SmartAP";
    private String USER_ID = "0001";
    private String SMART_CORE_ID = "0000";

    private List<String> objectsId;

    private static UnsignedLong counter = UnsignedLong.valueOf("0");

    public static final int TX_POWER = -59;
    public static final int RSSI = -59;

    private final Context mAppContext;
    private static Config mInstance;

    private Config(Context context) {
        this.mAppContext = context;
    }

    public static Config getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Config(context.getApplicationContext());
        }
        return mInstance;
    }

    public void setManufacturerId(int hexValue) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(MANUFACTURER_ID_KEY, hexValue);
        editor.apply();
    }

    public void setSignalThreshold(int value) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(SIGNAL_THRESHOLD_KEY, value);
        editor.apply();
    }

    public void setHelloBroadcastId(String hexString) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(HELLO_BROADCAST_ID_KEY, hexString);
        editor.apply();
    }

    public void setHelloBroadcastMajor(String hexString) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(HELLO_BROADCAST_MAJOR_KEY, hexString);
        editor.apply();
    }

    public void setSmartCoreId(String hexString) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(SMART_CORE_ID_KEY, hexString);
        editor.apply();
    }

    public void setUserId(String hexString) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(USER_ID_KEY, hexString);
        editor.apply();
    }

    public void setCounter(String value) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(COUNTER_KEY, value);
        editor.apply();
    }

    public void setSsid(Context context, String name) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(SSID_KEY, name);
        editor.apply();
    }

    public void setKey(String value) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(KEY_KEY, value);
        editor.apply();
    }

    public void setIv(String value) {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(IV_KEY, value);
        editor.apply();
    }

    public void setObjectsId (List<String> objectsId) {
        this.objectsId = objectsId;
    }

    public int getManufacturerId() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPref.getInt(MANUFACTURER_ID_KEY, MANUFACTURER_ID);
    }

    public int getSignalThreshold() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPref.getInt(SIGNAL_THRESHOLD_KEY, SIGNAL_THRESHOLD);
    }

    public String getHelloBroadcastId() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPref.getString(HELLO_BROADCAST_ID_KEY, HELLO_BROADCAST_ID);
    }

    public String getHelloBroadcastMajor() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPref.getString(HELLO_BROADCAST_MAJOR_KEY, HELLO_BROADCAST_MAJOR);
    }

    public String getSsid() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPref.getString(SSID_KEY, ssid);
    }

    public String getUserId() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPref.getString(USER_ID_KEY, USER_ID);
    }

    public String getSmartCoreId() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPref.getString(SMART_CORE_ID_KEY, SMART_CORE_ID);
    }

    public UnsignedLong getCounter() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        String value = sharedPref.getString(COUNTER_KEY, "0");

        return UnsignedLong.valueOf(value);
    }

    public byte[] getKey() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        String keyString = sharedPref.getString(KEY_KEY, "9bd9cdf6be2b9d58fbd2ef3ed83769a0caf56fd0acc3e052f07afab8dd013f45");

        return BaseEncoding.base16().lowerCase().decode(keyString);
    }

    public byte[] getIv() {
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        String ivString = sharedPref.getString(KEY_KEY, "efaa299f48510f04181eb53b42ff1c01");

        return BaseEncoding.base16().lowerCase().decode(ivString);
    }

    public List<String> getObjectsId() {
        return objectsId;
    }

}