package com.unime.beacontest.beacon.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.unime.beacontest.beacon.utils.ScanFilterUtils.MANUFACTURER_DATAMASK_OFFSET;
import static com.unime.beacontest.beacon.utils.ScanFilterUtils.MANUFACTURER_DATAMASK_SIZE;

public class CustomFilter {
    // TODO check for singleton design pattern
    private static List<Filter> filters;
    private static BeaconLightModel beaconLightModel;
    private static byte[] manufacturerDataMask;

    public static final String TAG = "CustomFilter";

    public CustomFilter() {
        filters = new ArrayList<>();
        beaconLightModel = new BeaconLightModel();
        manufacturerDataMask = new byte[MANUFACTURER_DATAMASK_SIZE];
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public BeaconLightModel getBeaconLightModel() {
        return beaconLightModel;
    }

    public byte[] getManufacturerDataMask() {
        return manufacturerDataMask;
    }


    private static final int UUID_SIZE_HEX = 32;
    private static final int UUID_SIZE = 16;
    private static final int MAJOR_SIZE = 2;
    private static final int MINOR_SIZE = 2;
    private static final int MAJOR_SIZE_HEX = 4;
    private static final int MINOR_SIZE_HEX = 4;
    private static final int UUID_SELECT = 0;
    private static final int MAJOR_SELECT = 1;
    private static final int MINOR_SELECT = 2;
    private static final int MANUFACTURER_DATAMASK_SELECT = 3;

    private char[] uuid = new char[UUID_SIZE_HEX];
    private char[] major = new char[MAJOR_SIZE_HEX];
    private char[] minor = new char[MINOR_SIZE_HEX];


    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void build() {
        if (filters.size() == 0)
            return;

        zeros(UUID_SELECT);
        zeros(MAJOR_SELECT);
        zeros(MINOR_SELECT);
        zeros(MANUFACTURER_DATAMASK_SELECT);

        // TODO add a control statement to not reuse startDataPosition end endDataPosition
        // in the same build() method call.
        for (Filter filter : filters) {
            switch (filter.getType()) {
                case Filter.UUID_TYPE:
                    buildUuidToFilter(
                            filter.getData(),
                            filter.getStartDataPosition(),
                            filter.getEndDataPosition());
                    break;
                case Filter.MAJOR_TYPE:
                    buildMajorToFilter(
                            filter.getData(),
                            filter.getStartDataPosition(),
                            filter.getEndDataPosition());
                    break;
                case Filter.MINOR_TYPE:
                    buildMinorToFilter(
                            filter.getData(),
                            filter.getStartDataPosition(),
                            filter.getEndDataPosition());
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid type passed!");
            }
        }
//        String prova = "";
//        for(int i=0; i<UUID_SIZE_HEX; i++) {
//            prova += uuid[i];
//        }
//        Log.d(TAG, "build: " + prova);

//        for(int i=0; i<MANUFACTURER_DATAMASK_SIZE; i++) {
//            prova += manufacturerDataMask[i];
//            if(i==1 || i==17 || i== 19 || i == 21)
//                prova += "-";
//        }
//        Log.d(TAG, "build: " + prova);

        // now we have uuid, major and minor char array filled with our data.
        beaconLightModel.setUuid(uuid);
        beaconLightModel.setMajor(major);
        beaconLightModel.setMinor(minor);
    }

    private void zeros(int select) {
        switch (select) {
            case UUID_SELECT:
                for (int i = 0; i < UUID_SIZE_HEX; i++) {
                    uuid[i] = '0';
                }
                break;
            case MAJOR_SELECT:
                for (int i = 0; i < MAJOR_SIZE_HEX; i++) {
                    major[i] = '0';
                }
                break;
            case MINOR_SELECT:
                for (int i = 0; i < MINOR_SIZE_HEX; i++) {
                    minor[i] = '0';
                }
                break;
            case MANUFACTURER_DATAMASK_SELECT:
                for (int i = 0; i < MANUFACTURER_DATAMASK_SIZE; i++) {
                    manufacturerDataMask[i] = 0;
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid select passed!");
        }
    }

    private void buildUuidToFilter(String data, int startDataPosition, int endDataPosition) {
        char[] dataCharArray = data.toCharArray();
        int range = (endDataPosition - startDataPosition) + 1;

        // TODO MODIFY THIS, byte and hex array size mismatch
        for (int i = 0; i <= range; i++) {
            manufacturerDataMask[i + MANUFACTURER_DATAMASK_OFFSET + startDataPosition] = 1;
        }

        for (int hexi = 0; hexi < range * 2; hexi++) {
            uuid[hexi + (startDataPosition*2)] = dataCharArray[hexi];
        }

        StringBuilder sb = new StringBuilder();
        for(int i=0; i < UUID_SIZE_HEX; i++){
            sb.append(uuid[i]);
        }

        Log.d("CustomFilter", "buildUuidToFilter: " + sb.toString());
    }

    private void buildMajorToFilter(String data, int startDataPosition, int endDataPosition) {
        char[] dataCharArray = data.toCharArray();
        int range = (endDataPosition - startDataPosition) + 1;
        int offset = startDataPosition + UUID_SIZE;


        for (int i = 0; i < range; i++) {
            manufacturerDataMask[i + MANUFACTURER_DATAMASK_OFFSET + offset] = 1;
        }
        for (int hexi = 0; hexi < range * 2; hexi++) {
            major[hexi + (startDataPosition * 2)] = dataCharArray[hexi];
        }
        Log.d("CustomFilter", "buildMajorToFilter: " + major.toString());
    }

    private void buildMinorToFilter(String data, int startDataPosition, int endDataPosition) {
        char[] dataCharArray = data.toCharArray();
        int range = (endDataPosition - startDataPosition) + 1;
        int offset = startDataPosition + UUID_SIZE + MAJOR_SIZE;

        for (int i = 0; i < range; i++) {
            manufacturerDataMask[i + MANUFACTURER_DATAMASK_OFFSET + offset] = 1;
        }

        for (int hexi = 0; hexi < range * 2; hexi++) {
            minor[hexi + (startDataPosition * 2)] = dataCharArray[hexi];
        }

        Log.d("CustomFilter", "buildMinorToFilter: " + minor.toString());
    }

}