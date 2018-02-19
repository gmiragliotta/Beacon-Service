package com.unime.beacontest.beacon.utils;

import android.util.Log;

public class BeaconLightModel {
    // UUID of beacon
    private String uuid;
    // string representing arguments inside Beacon
    //private String arguments;
    private String major;
    private String minor;

    public static final String TAG = "BeaconLightModel";

    public BeaconLightModel() {

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(char[] uuidCharArray) {
        StringBuilder sb = new StringBuilder();

        for(int i=0, dash=1; i < uuidCharArray.length; i++, dash++) {
            sb.append(uuidCharArray[i]);
            if(dash == 8 || dash == 12 || dash == 16 || dash == 20) {
                sb.append("-");
            }
        }
        Log.d("BeaconLightModel", "setUuid: " + sb.toString());
        this.uuid = sb.toString();
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(char[] major) {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<major.length; i++)
            sb.append(major[i]);

        Log.d(TAG, "setMajor: " + sb.toString());
        this.major = sb.toString();
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(char[] minor) {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<minor.length; i++)
            sb.append(minor[i]);

        Log.d(TAG, "setMinor: "+sb.toString());
        this.minor = sb.toString();
    }
}

