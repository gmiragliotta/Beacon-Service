package com.unime.beacontest;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;

public class Settings {
    public static final int MANUFACTURER_ID = 0x8888;
    public static final int TX_POWER = -59;
    public static final int RSSI = -59;
    public static final int SIGNAL_THRESHOLD = -70;
    public static final String HELLO_BROADCAST_ID = "00000000";
    public static final String HELLO_BROADCAST_MAJOR = "ffff";
    public static String USER_ID = "0001";
    public static String OBJECT_ID = "0000";
    public static UnsignedLong counter = UnsignedLong.valueOf("0");
    public static byte[] key = BaseEncoding.base16().lowerCase().decode("9bd9cdf6be2b9d58fbd2ef3ed83769a0caf56fd0acc3e052f07afab8dd013f45");
    public static byte[] iv = BaseEncoding.base16().lowerCase().decode("efaa299f48510f04181eb53b42ff1c01");
    public static final String ssid = "SmartAP";
}