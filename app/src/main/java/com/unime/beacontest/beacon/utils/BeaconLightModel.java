package com.unime.beacontest.beacon.utils;

public class BeaconLightModel {
    // UUID of beacon
    private String uuid;
    // string representing arguments inside Beacon
    //private String arguments;
    private String major;
    private String minor;

    public BeaconLightModel() {

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        char[] uuidCharArray = uuid.toCharArray();

        StringBuilder sb = new StringBuilder();

        for(int i=0, dash=1; i < uuid.length(); i++, dash++) {
            sb.append(uuidCharArray[i]);
            if(dash == 8 || dash == 12 || dash == 16 || dash == 20){
                sb.append("-");
            }
        }

        this.uuid = sb.toString();
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }
}
