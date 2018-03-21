package com.unime.beacontest.beacon;

import com.google.common.io.BaseEncoding;

public class Settings {
    public static int MANUFACTURER_ID = 0x8888;
    public static int TX_POWER = -59;
    public static int RSSI = -59;
    public static byte[] key = BaseEncoding.base16().lowerCase().decode("9bd9cdf6be2b9d58fbd2ef3ed83769a0caf56fd0acc3e052f07afab8dd013f45");
    public static byte[] iv = BaseEncoding.base16().lowerCase().decode("efaa299f48510f04181eb53b42ff1c01");
}