package com.unime.beacontest;

import android.util.Log;

import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.ConversionUtils;

public class BeaconCommand {
    private String userId;
    private String smartObjectId;
    private Long counter;
    private String command;

    // 2 byte are free
    private String extra = "0000";

    private byte[] data = new byte[20];


    public BeaconCommand(String userId, String objId, long counter, String command) {
        this.userId = userId;
        this.smartObjectId = objId;
        this.counter = counter;
        this.command = command;
    }

    public String getUserId() {
        return userId;
    }

    public String getObjId() {
        return smartObjectId;
    }

    public long getCounter() {
        return counter;
    }

    public String getCommand() {
        return command;
    }

    public BeaconModel getBeaconModel() {
        byte[] userIdByte = getUserId().getBytes();
        byte[] commandByte = getCommand().getBytes();
        byte[] counterByte = ConversionUtils.longToBytes(getCounter());


        Log.d("BeaconCommand", "getBeaconModel: " + userIdByte.length + "-" +
                commandByte.length + "-" + counterByte.length);

        return null;
    }
}
