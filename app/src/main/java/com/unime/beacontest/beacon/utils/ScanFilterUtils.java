package com.unime.beacontest.beacon.utils;

import android.bluetooth.le.ScanFilter;
import android.util.Log;

import java.util.UUID;

public class ScanFilterUtils {
    private static final String TAG = "ScanFilterUtils";

    private static final int MANUFACTURER_ID = 34952;
    //private static final int MANUFACTURER_ID = 76;
    public static final int MANUFACTURER_DATAMASK_OFFSET = 2;
    public static final int MANUFACTURER_DATAMASK_SIZE = 21 + MANUFACTURER_DATAMASK_OFFSET;

    private static final int UUID_INDEX_START = MANUFACTURER_DATAMASK_OFFSET;
    private static final int MAJOR_INDEX_START = UUID_INDEX_START + 16;
    private static final int MINOR_INDEX_START = MAJOR_INDEX_START + 2;


    public static ScanFilter getScanFilter(CustomFilter customFilter) {
        final ScanFilter.Builder builder = new ScanFilter.Builder();

        // the manufacturer data byte is the filter! Don't change this!
        final byte[] manufacturerData = new byte[]
                {
                        0,0,

                        // uuid
                        0,0,0,0,
                        0,0,
                        0,0,
                        0,0,0,0,0,0,0,0,

                        // major
                        0,0,

                        // minor
                        0,0,

                        0
                };

        // the mask tells what bytes in the filter need to match, 1 if it has to match, 0 if not
        byte[] manufacturerDataMask = customFilter.getManufacturerDataMask();
        BeaconLightModel beaconLightModel = customFilter.getBeaconLightModel();

        UUID uuid = UUID.fromString(beaconLightModel.getUuid());
        int major = Integer.parseInt(beaconLightModel.getMajor());
        int minor = Integer.parseInt(beaconLightModel.getMinor());

        // copy uuid into data array
        System.arraycopy(ConversionUtils.UuidToByteArray(uuid), 0, manufacturerData, UUID_INDEX_START, 16);

        // copy major into data array
        System.arraycopy(ConversionUtils.integerToByteArray(major), 0, manufacturerData, MAJOR_INDEX_START, 2);

        // copy minor into data array
        System.arraycopy(ConversionUtils.integerToByteArray(minor), 0, manufacturerData, MINOR_INDEX_START, 2);

        Log.d(TAG, "getScanFilter: "+ ConversionUtils.byteToHex(manufacturerData).toString());

        builder.setManufacturerData(
                MANUFACTURER_ID,
                manufacturerData,
                manufacturerDataMask);

        return builder.build();
    }
}
