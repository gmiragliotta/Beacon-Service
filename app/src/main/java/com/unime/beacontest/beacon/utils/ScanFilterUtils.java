package com.unime.beacontest.beacon.utils;

import android.bluetooth.le.ScanFilter;

import java.util.UUID;

public class ScanFilterUtils {
    private static final String TAG = "ScanFilterUtils";

    private static final int MANUFACTURER_ID = 280;
    public static final int MANUFACTURER_DATAMASK_OFFSET = 2;
    public static final int MANUFACTURER_DATAMASK_SIZE = 23;

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

        // copy UUID (with no dashes) into data array
        UUID uuid = UUID.fromString(beaconLightModel.getUuid());
        int major = Integer.parseInt(beaconLightModel.getMajor());
        int minor = Integer.parseInt(beaconLightModel.getMinor());

        // copy major into data array
        System.arraycopy(ConversionUtils.UuidToByteArray(uuid), 0, manufacturerData, 2, 16);

        // copy minor into data array
        System.arraycopy(ConversionUtils.integerToByteArray(major), 0, manufacturerData, 18, 2);

        // copy minor into data array
        System.arraycopy(ConversionUtils.integerToByteArray(minor), 0, manufacturerData, 20, 2);

        builder.setManufacturerData(
                MANUFACTURER_ID,
                manufacturerData,
                manufacturerDataMask);

        return builder.build();
    }
}
