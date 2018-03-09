package com.unime.beacontest.beacon.utils;

public class Filter {
    public static final int MANUFACTURER_OFFSET = 2;

    public static final int UUID_START_INDEX = 2 + MANUFACTURER_OFFSET;
    public static final int UUID_END_INDEX = UUID_START_INDEX + 15;
    public static final int MAJOR_START_INDEX = UUID_END_INDEX + 1;
    public static final int MAJOR_END_INDEX = MAJOR_START_INDEX + 1;
    public static final int MINOR_START_INDEX = MAJOR_END_INDEX + 1;
    public static final int MINOR_END_INDEX = MINOR_START_INDEX + 1;

    public static final int UUID_TYPE = 0;
    public static final int MAJOR_TYPE = 1;
    public static final int MINOR_TYPE = 2;


    private int type;
    private String data;
    private int startDataPosition;
    private int endDataPosition;

    public Filter(int type, String data, int startDataPosition, int endDataPosition) {
        // check if arguments are valid
        switch(type) {
            case UUID_TYPE:
                if(startDataPosition < 0 || endDataPosition > 15)
                    throw new IllegalArgumentException("startDataPosition < 0 or endDataPosition >= 16");
                break;
            case MAJOR_TYPE:
            case MINOR_TYPE:
                if(startDataPosition < 0 || endDataPosition > 1)
                    throw new IllegalArgumentException("startDataPosition < 0 or endDataPosition >= 2");
                break;
            default:
                throw new UnsupportedOperationException("Invalid type: " + type);
        }

        if(data.length() != ((endDataPosition+1) - startDataPosition) * 2)
            throw new IllegalArgumentException("string length must be of " +
                    ((endDataPosition+1) - startDataPosition) * 2);

        this.type = type;
        this.data = data;
        this.startDataPosition = startDataPosition;
        this.endDataPosition = endDataPosition;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStartDataPosition() {
        return startDataPosition;
    }

    public void setStartDataPosition(int startDataPosition) {
        this.startDataPosition = startDataPosition;
    }

    public int getEndDataPosition() {
        return endDataPosition;
    }

    public void setEndDataPosition(int endDataPosition) {
        this.endDataPosition = endDataPosition;
    }
}
