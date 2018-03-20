package com.unime.beacontest.objectinteraction;

import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.unime.beacontest.beacon.utils.BeaconModel;

import static com.unime.beacontest.AES256.decrypt;
import static com.unime.beacontest.AES256.encrypt;
import static com.unime.beacontest.beacon.utils.ConversionUtils.byteToHex;
import static com.unime.beacontest.beacon.utils.ConversionUtils.hexToBytes;

/* BeaconCommand format.
 *
 * High level view
 * | 8 Bytes Counter | 6 Bytes Command | 2 Bytes Reserved |
 * | 2 Bytes User ID | 2 Bytes Object ID |
 *
 * 6 Bytes Command:
 * | 1 Byte CMD Type | 1 Byte CMD Class | 1 Byte CMD OP Code | 2 Bytes Parameters | 1 Byte BITMAP |
 *
 * 2 Bytes Object ID:
 * | 1 Byte Category | 1 Byte ID |
 */

public class BeaconCommand {
    public static final String TAG = "BeaconCommand";

    private static final int COUNTER_SIZE = 8;
    private static final int COMMAND_SIZE = 6;
    private static final int RESERVED_SIZE = 2;
    private static final int USER_ID_SIZE = 2;
    private static final int OBJECT_ID_SIZE = 2;

    private static final int DATA_PAYLOAD_SIZE = 20;
    private static final int ENCRYPTED_DATA_PAYLOAD_SIZE = 16;

    private static final int COUNTER_INDEX = 0;
    private static final int COMMAND_INDEX = COUNTER_INDEX + COUNTER_SIZE;
    private static final int RESERVED_INDEX = COMMAND_INDEX + COMMAND_SIZE;
    private static final int USER_ID_INDEX = RESERVED_INDEX + RESERVED_SIZE;
    private static final int OBJECT_ID_INDEX = USER_ID_INDEX + USER_ID_SIZE;

    // Command components offsets
    private static final int COMMAND_TYPE_OFFSET = COMMAND_INDEX;
    private static final int COMMAND_CLASS_OFFSET = COMMAND_TYPE_OFFSET + 1;
    private static final int COMMAND_OP_CODE_OFFSET = COMMAND_CLASS_OFFSET + 1;
    private static final int PARAMETERS_OFFSET = COMMAND_OP_CODE_OFFSET + 1;
    private static final int BITMAP_OFFSET = PARAMETERS_OFFSET + 2;

    private byte[] counter; // long
    private byte[] command; // hex
    // 2 byte are free
    private byte[] reserved = new byte[RESERVED_SIZE]; // random?
    private byte[] userId; // hex
    private byte[] objectId; // hex

    private byte[] dataPayload = new byte[DATA_PAYLOAD_SIZE];
    private byte[] encryptedDataPayload = new byte[ENCRYPTED_DATA_PAYLOAD_SIZE];


    public BeaconCommand() {
        zeroInit(getDataPayload());
    }

    public byte[] getDataPayload() {
        return dataPayload;
    }

    public void setCounter(Long counter) {
        byte[] counterBytes = Longs.toByteArray(counter);

        System.arraycopy(counterBytes, 0, dataPayload, COUNTER_INDEX, COUNTER_SIZE);
    }

    public void setCommandType(String hexCommandType) {
        dataPayload[COMMAND_TYPE_OFFSET] = BaseEncoding.base16().decode(hexCommandType)[0];
    }

    public void setCommandClass(String hexCommandClass) {
        dataPayload[COMMAND_CLASS_OFFSET] = BaseEncoding.base16().decode(hexCommandClass)[0];
    }

    public void setCommandOpCode(String hexCommandOpCode) {
        dataPayload[COMMAND_OP_CODE_OFFSET] = BaseEncoding.base16().decode(hexCommandOpCode)[0];
    }

    public void setParameters(String hexParameter1, String hexParameter2) {
        dataPayload[PARAMETERS_OFFSET] = BaseEncoding.base16().decode(hexParameter1)[0];
        dataPayload[PARAMETERS_OFFSET + 1] = BaseEncoding.base16().decode(hexParameter2)[0];
    }

    public void setBitmap(byte bitmap) {        // TODO Use binary literals 0b11111111 to test
        dataPayload[BITMAP_OFFSET] = bitmap;
    }

    public void setReserved(String hexReserved) {
        byte[] reservedBytes = BaseEncoding.base16().decode(hexReserved);
        System.arraycopy(reservedBytes, 0, dataPayload, RESERVED_INDEX, RESERVED_SIZE);
    }

    public void setUserId(String hexUserId) {
        byte[] userIdBytes = BaseEncoding.base16().decode(hexUserId);
        System.arraycopy(userIdBytes, 0, dataPayload, USER_ID_INDEX, USER_ID_SIZE);
    }

    public void setObjectId(String hexCategory, String hexObjectId) {
        dataPayload[OBJECT_ID_INDEX] = BaseEncoding.base16().decode(hexCategory)[0];
        dataPayload[OBJECT_ID_INDEX + 1] = BaseEncoding.base16().decode(hexObjectId)[0];
    }




    




    public BeaconModel getBeaconModel() {
        byte[] userIdBytes = BaseEncoding.base16().decode(getUserId());
        byte[] commandBytes = BaseEncoding.base16().decode(getCommand());
        System.out.println("commandbytes: " + commandBytes.length);
        byte[] counterBytes = Longs.toByteArray(getCounter());
        System.out.println("counterbytes: " + counterBytes.length);
        byte[] fillByte = BaseEncoding.base16().decode("00");
        byte[] objectIdBytes = BaseEncoding.base16().decode(getObjId());
        byte[] extraBytes = BaseEncoding.base16().decode(getExtra());

        System.arraycopy(userIdBytes, 0, dataPayload, USER_ID_INDEX, USER_ID_SIZE);
        System.arraycopy(counterBytes, 0, dataPayload, COUNTER_INDEX, COUNTER_SIZE);
        System.arraycopy(commandBytes, 0, dataPayload, COMMAND_INDEX, COMMAND_SIZE);
        System.arraycopy(fillByte, 0, dataPayload, FILL_INDEX, RESERVED_SIZE);

        String key = "9bd9cdf6be2b9d58fbd2ef3ed83769a0caf56fd0acc3e052f07afab8dd013f45";
        byte[] clean = Bytes.concat(Bytes.concat(userIdBytes, commandBytes),
                Bytes.concat(counterBytes,fillByte));
        System.out.println("Problem: " + clean.length);


        byte[] iv = hexToBytes("efaa299f48510f04181eb53b42ff1c01");

        byte[] encrypted = null;
        try {
            encrypted = encrypt(clean, hexToBytes(key), iv);
            Log.d(TAG, "encrypted: " + byteToHex(encrypted));

            String decrypted = decrypt(encrypted, hexToBytes(key), iv);
            Log.d(TAG, "onButtonClick: decrypted " + decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // major and minor
        System.arraycopy(objectIdBytes, 0, dataPayload, OBJECT_ID_INDEX, OBJECT_ID_SIZE);
        System.arraycopy(extraBytes, 0, dataPayload, EXTRA_INDEX, EXTRA_SIZE);

        Log.d("BeaconCommand", "getBeaconModel: " + userIdBytes.length + "-" +
                commandBytes.length + "-" + counterBytes.length);

        // let's build the beacon
        return new BeaconModel(findUUID(encrypted), findMajor(dataPayload), findMinor(dataPayload));
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

        String major = String.format("%02x%02x", data[USER_ID_INDEX], data[USER_ID_INDEX + 1]);
        return major;
    }

    private String findMinor(final byte[] data){

        String minor = String.format("%02x%02x", data[OBJECT_ID_INDEX], data[OBJECT_ID_INDEX + 1]);
        return minor;
    }

    private void zeroInit(byte[] data) {
        for(int i=0; i < data.length; i++) {
            data[i] = 0;
        }
    }


}
