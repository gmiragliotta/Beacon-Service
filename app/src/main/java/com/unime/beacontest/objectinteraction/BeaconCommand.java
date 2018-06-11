package com.unime.beacontest.objectinteraction;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedLong;
import com.unime.beacontest.Settings;

import org.altbeacon.beacon.Beacon;

import java.security.SecureRandom;
import java.util.Arrays;

import static com.unime.beacontest.AES256.decrypt;
import static com.unime.beacontest.AES256.encrypt;

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

public class BeaconCommand implements Parcelable {
    private static final String BEACON_COMMAND_TAG = "BeaconCommand";

    private static final int COUNTER_SIZE = 8;
    private static final int COMMAND_SIZE = 6;
    private static final int RESERVED_SIZE = 2;
    private static final int USER_ID_SIZE = 2;
    private static final int OBJECT_ID_SIZE = 2;

    private static final int DATA_PAYLOAD_SIZE = 16;
    private static final int ENCRYPTED_DATA_PAYLOAD_SIZE = 16;

    private static final int COUNTER_INDEX = 0;
    private static final int COMMAND_INDEX = COUNTER_INDEX + COUNTER_SIZE;
    private static final int RESERVED_INDEX = COMMAND_INDEX + COMMAND_SIZE;
    private static final int USER_ID_INDEX = 0;
    private static final int OBJECT_ID_INDEX = 0;

    // Command components offsets
    private static final int COMMAND_TYPE_OFFSET = COMMAND_INDEX;
    private static final int COMMAND_CLASS_OFFSET = COMMAND_TYPE_OFFSET + 1;
    private static final int COMMAND_OP_CODE_OFFSET = COMMAND_CLASS_OFFSET + 1;
    private static final int PARAMETERS_OFFSET = COMMAND_OP_CODE_OFFSET + 1;
    private static final int BITMAP_OFFSET = PARAMETERS_OFFSET + 2;

    private byte[] dataPayload = new byte[DATA_PAYLOAD_SIZE];
    private byte[] encryptedDataPayload = new byte[ENCRYPTED_DATA_PAYLOAD_SIZE];
    private byte[] userId = new byte[USER_ID_SIZE]; // hex
    private byte[] objectId = new byte [OBJECT_ID_SIZE]; // hex

    public BeaconCommand() {
        zeros(getDataPayload());
        ones(getDataPayload(), BITMAP_OFFSET, 1);
    }

    public byte[] getDataPayload() {
        return dataPayload;
    }

    public void setCounter(UnsignedLong counter) {
        byte[] counterBytes = Longs.toByteArray(counter.longValue());

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

    // Bitmap input parameter: (byte)0b11111111
    public void setBitmap(byte bitmap) {
        dataPayload[BITMAP_OFFSET] = bitmap;
    }

    public void setReserved(String hexReserved) {
        byte[] reservedBytes = BaseEncoding.base16().decode(hexReserved);
        System.arraycopy(reservedBytes, 0, dataPayload, RESERVED_INDEX, RESERVED_SIZE);
    }

    public void randomizeReserved() {
        byte[] reservedBytes = new byte[RESERVED_SIZE];

        // randomize reserved bytes
        SecureRandom random = new SecureRandom();
        random.nextBytes(reservedBytes);

        System.arraycopy(reservedBytes, 0, dataPayload, RESERVED_INDEX, RESERVED_SIZE);
    }

    public void setUserId(String hexUserId) {
        userId = BaseEncoding.base16().decode(hexUserId);
    }

    public void setObjectId(String hexObjectId) {
        objectId = BaseEncoding.base16().decode(hexObjectId);
    }

    public String getObjectId() {
        Log.d(BEACON_COMMAND_TAG, "getObjectId: " + BaseEncoding.base16().lowerCase().encode(objectId));
        return BaseEncoding.base16().lowerCase().encode(objectId);
    }

    public Beacon build() {
        // Encrypt Payload Data (16 Bytes)
        byte[] payloadToEncrypt = new byte[ENCRYPTED_DATA_PAYLOAD_SIZE];
        System.arraycopy(dataPayload, 0, payloadToEncrypt, 0, ENCRYPTED_DATA_PAYLOAD_SIZE);

        try {
            encryptedDataPayload = encrypt(payloadToEncrypt, Settings.key, Settings.iv);
            Log.d(BEACON_COMMAND_TAG, "encrypted: " + BaseEncoding.base16().lowerCase().encode(encryptedDataPayload));

            // Just for debugging purposes
            String decryptedPayload = decrypt(encryptedDataPayload, Settings.key, Settings.iv);
            Log.d(BEACON_COMMAND_TAG, "decrypted: counter: " + decryptedPayload.substring(0,16) +
                    " command: " + decryptedPayload.substring(16,28) + " reserved: " +
                    decryptedPayload.substring(28, 32));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Beacon.Builder()
                .setId1(BaseEncoding.base16().encode(encryptedDataPayload))
                .setId2(BaseEncoding.base16().encode(userId))
                .setId3(BaseEncoding.base16().encode(objectId))
                .setManufacturer(Settings.MANUFACTURER_ID)
                .setTxPower(Settings.TX_POWER)
                .setRssi(Settings.RSSI)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
    }

    private void zeros(byte[] data) {
        for(int i=0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    // Bitmap set to 11111111
    private void ones(byte[] data, int off, int len) {
        for(int i = off, size = off + len; i < size; i++) {
            data[i] = (byte) 0xFF;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.dataPayload);
        dest.writeByteArray(this.encryptedDataPayload);
        dest.writeByteArray(this.userId);
        dest.writeByteArray(this.objectId);
    }

    protected BeaconCommand(Parcel in) {
        this.dataPayload = in.createByteArray();
        this.encryptedDataPayload = in.createByteArray();
        this.userId = in.createByteArray();
        this.objectId = in.createByteArray();
    }

    public static final Parcelable.Creator<BeaconCommand> CREATOR = new Parcelable.Creator<BeaconCommand>() {
        @Override
        public BeaconCommand createFromParcel(Parcel source) {
            return new BeaconCommand(source);
        }

        @Override
        public BeaconCommand[] newArray(int size) {
            return new BeaconCommand[size];
        }
    };
}
