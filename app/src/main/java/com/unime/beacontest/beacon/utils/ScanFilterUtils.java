package com.unime.beacontest.beacon.utils;

import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.unime.beacontest.beacon.Settings;

import java.math.BigInteger;

public class ScanFilterUtils {
    private static final String TAG = "ScanFilterUtils";

    private static final int MANUFACTURER_ID_INDEX = 5;
    private static final int MANUFACTURER_ID_SIZE = 2;

    //private static final int MANUFACTURER_ID = 34952; // custom
    //private static final int MANUFACTURER_ID = 76; // apple
    //private static final int MANUFACTURER_ID = 280; // radius network
    private static final int MANUFACTURER_ID = 280;

    public static final int MANUFACTURER_DATAMASK_OFFSET = 2;
    public static final int MANUFACTURER_DATAMASK_SIZE = 21 + MANUFACTURER_DATAMASK_OFFSET;

    private static final int UUID_INDEX_START = MANUFACTURER_DATAMASK_OFFSET;
    private static final int MAJOR_INDEX_START = UUID_INDEX_START + 16;
    private static final int MINOR_INDEX_START = MAJOR_INDEX_START + 2;

    public static int getManufacturerId(byte[] data) {
        byte[] manufacturerIdBytes = new byte[MANUFACTURER_ID_SIZE];
        System.arraycopy(data, MANUFACTURER_ID_INDEX, manufacturerIdBytes, 0, MANUFACTURER_ID_SIZE);
        return new BigInteger(BaseEncoding.base16().encode(manufacturerIdBytes),16).intValue();
    }


    public static boolean toFilter(byte[] data) {
        //Log.d(TAG, "toFilter: " + BaseEncoding.base16().lowerCase().encode(data));
        //Log.d(TAG, "toFilter: minor " +  BeaconModel.findMinor(data));

        String hexData = BaseEncoding.base16().encode(data);
        int manufacturerId = ScanFilterUtils.getManufacturerId(data);

        if(BeaconModel.isAltBeacon(data) && (manufacturerId == Settings.MANUFACTURER_ID) &&
                BeaconModel.findMinor(data).equals("0000")) {
            Log.d(TAG, "toFilter: " + BaseEncoding.base16().lowerCase().encode(data));
            return true;
        }
        return false;
    }

}
