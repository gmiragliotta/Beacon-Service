package com.unime.beacontest.beacon.utils;

import android.bluetooth.le.ScanFilter;
import android.util.Log;

import java.util.UUID;

public class ScanFilterUtils {
    private static final String TAG = "ScanFilterUtils";

    private static final int MANUFACTURER_ID = 280;

    public static ScanFilter getScanFilter(CustomFilter uuidFilter, CustomFilter majorFilter, CustomFilter minorFilter) {
        final ScanFilter.Builder builder = new ScanFilter.Builder();
        Log.d(TAG, "getScanFilter: " + uuidFilter.getData() + " " + majorFilter.getData() + " " +
            minorFilter.getData());

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
        byte[] manufacturerDataMask = new byte[]
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



        // copy UUID (with no dashes) into data array
        UUID uuid = UUID.fromString(uuidFilter.getData());
        int major = Integer.parseInt(majorFilter.getData());
        int minor = Integer.parseInt(minorFilter.getData());

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

    public static class CustomFilter {
        private String data;
        private int startIndexFilter;
        private int endIndexFilter;

        public CustomFilter (String data, int startIndexFilter, int endIndexFilter){
            this.data = data;
            this.startIndexFilter = startIndexFilter;
            this.endIndexFilter = endIndexFilter;
        }

        public String getData() {
            return data;
        }

        public int getStartIndexFilter() {
            return startIndexFilter;
        }

        public int getEndIndexFilter() {
            return endIndexFilter;
        }
    }
}
