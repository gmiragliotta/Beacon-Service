package com.unime.beacontest;

import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Longs;
import com.unime.beacontest.beacon.utils.BeaconModel;

public class BeaconCommand {
    public static final String TAG = "BeaconCommand";

    private static final int USER_ID_SIZE = 2;
    private static final int COUNTER_SIZE = 8;
    private static final int COMMAND_SIZE = 5;
    private static final int FILL_SIZE = 1;
    private static final int OBJECT_ID_SIZE = 2;
    private static final int EXTRA_SIZE = 2;

    private static final int DATA_SIZE = 20;

    private static final int USER_ID_INDEX = 0;
    private static final int COUNTER_INDEX = USER_ID_INDEX + USER_ID_SIZE;
    private static final int COMMAND_INDEX = COUNTER_INDEX + COUNTER_SIZE;
    private static final int FILL_INDEX = COMMAND_INDEX + COMMAND_SIZE;
    private static final int OBJECT_ID_INDEX = FILL_INDEX + FILL_SIZE;
    private static final int EXTRA_INDEX = OBJECT_ID_INDEX + OBJECT_ID_SIZE;

    private String userId;
    private String smartObjectId;
    private Long counter;
    private String command;

    // 2 byte are free
    private String extra = "0000";

    private byte[] data = new byte[DATA_SIZE];


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

    public String getExtra() {
        return extra;
    }

    public BeaconModel getBeaconModel() {
        byte[] userIdBytes = BaseEncoding.base16().decode(getUserId());
        byte[] commandBytes = BaseEncoding.base16().decode(getCommand());
        byte[] counterBytes = Longs.toByteArray(getCounter());
        byte[] fillByte = BaseEncoding.base16().decode("00");
        byte[] objectIdBytes = BaseEncoding.base16().decode(getObjId());
        byte[] extraBytes = BaseEncoding.base16().decode(getExtra());

        System.arraycopy(userIdBytes, 0, data, USER_ID_INDEX, USER_ID_SIZE);
        System.arraycopy(counterBytes, 0, data, COUNTER_INDEX, COUNTER_SIZE);
        System.arraycopy(commandBytes, 0, data, COMMAND_INDEX, COMMAND_SIZE);
        System.arraycopy(fillByte, 0, data, FILL_INDEX, FILL_SIZE);

        System.arraycopy(objectIdBytes, 0, data, OBJECT_ID_INDEX, OBJECT_ID_SIZE);
        System.arraycopy(extraBytes, 0, data, EXTRA_INDEX, EXTRA_SIZE);

        Log.d("BeaconCommand", "getBeaconModel: " + userIdBytes.length + "-" +
                commandBytes.length + "-" + counterBytes.length);

        // let's build the beacon
        return new BeaconModel(findUUID(data), findMajor(data), findMinor(data));
    }

    private String findUUID(final byte[] data){
        StringBuilder sb = new StringBuilder();
        for(int i = USER_ID_INDEX, offset = 0; i <= OBJECT_ID_INDEX-1; ++i, ++offset) {

            sb.append(String.format("%02x", (int)(data[i] & 0xff)));
            if (offset == 3 || offset == 5 || offset == 7 || offset == 9) {
                sb.append("-");
            }
        }
        Log.d(TAG, "hex: "+sb.toString());
        return sb.toString();
    }

    private String findMajor(final byte[] data){

        String major = String.format("%02x%02x", data[OBJECT_ID_INDEX], data[OBJECT_ID_INDEX + 1]);
        return major;
    }

    private String findMinor(final byte[] data){

        String minor = String.format("%02x%02x", data[EXTRA_INDEX], data[EXTRA_INDEX + 1]);
        return minor;
    }
}
