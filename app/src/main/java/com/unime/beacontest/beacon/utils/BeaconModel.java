package com.unime.beacontest.beacon.utils;
import java.util.ArrayList;


public class BeaconModel {

    // UUID of beacon
    private String uuid;
    // string representing arguments inside Beacon
    //private String arguments;
    private String major;
    private String minor;
    // reference power
    private int txPower;
    // current RSSI
    private int rssi;
    // timestamp when this beacon was last time scanned
    private long timestamp;
    // ID of the beacon, in case of Android it will be Bluetooth MAC address
    private String address;

    //lista contenente il MAC address di ogni timbratrice compatibile
    public static final ArrayList<String> lista_timbratrici = new ArrayList<String>();
    static{
        lista_timbratrici.add("00:1A:7D:DA:71:03");
        lista_timbratrici.add("B8:27:EB:6A:57:34");
    }

    private static final int PROTOCOL_OFFSET = 3;
    private static final int AD_LENGTH_INDEX = 0 + PROTOCOL_OFFSET;
    private static final int AD_TYPE_INDEX = 1 + PROTOCOL_OFFSET;
    private static final int BEACON_CODE_INDEX = 4 + PROTOCOL_OFFSET;
    private static final int UUID_START_INDEX = 6 + PROTOCOL_OFFSET;
    private static final int UUID_STOP_INDEX = UUID_START_INDEX + 15;
    private static final int ARGS_START_INDEX = UUID_STOP_INDEX + 1;
    private static final int TXPOWER_INDEX = ARGS_START_INDEX + 4;
    private static final int AD_LENGTH_VALUE = 0x1b;
    private static final int AD_TYPE_VALUE = 0xff;
    private static final int BEACON_CODE_VALUE = 0xbeac;

    public BeaconModel(String uuid, String major, String minor, int txPower, int rssi, long timestamp, String address){
        this.uuid = uuid;
        //this.arguments = arguments;
        this.major = major;
        this.minor = minor;
        this.txPower = txPower;
        this.rssi = rssi;
        this.timestamp = timestamp;
        this.address = address;

    }

    public String getUuid(){
        return this.uuid;
    }

    /*protected String getArguments(){
        return this.arguments;
    }*/

    public String getMajor(){
        return this.major;
    }

    public String getMinor(){
        return this.minor;
    }

    public int getTxPower(){
        return this.txPower;
    }

    public int getRssi(){
        return this.rssi;
    }

    public long getTimestamp(){
        return this.timestamp;
    }

    public String getAddress(){
        return this.address;
    }

    /*protected void setArguments(String arguments){
        this.arguments = arguments;
    }*/

    public void setMajor(String major){
        this.major = major;
    }

    public void setMinor(String minor){
        this.minor = minor;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static boolean isBeacon(final byte[] data) {
        if ((data[AD_LENGTH_INDEX] & 0xff) != AD_LENGTH_VALUE)
            return false;
        if ((data[AD_TYPE_INDEX] & 0xff) != AD_TYPE_VALUE)
            return false;
        final int code = ((data[BEACON_CODE_INDEX] << 8) & 0x0000ff00) | ((data[BEACON_CODE_INDEX + 1]) & 0x000000ff);
        if(code != BEACON_CODE_VALUE)
            return false;
        return true;
    }

    public static String findUUID(final byte[] data){
        StringBuilder sb = new StringBuilder();
        for(int i = UUID_START_INDEX, offset = 0; i <= UUID_STOP_INDEX; ++i, ++offset) {
            sb.append(String.format("%02x", (int)(data[i] & 0xff)));
            if (offset == 3 || offset == 5 || offset == 7 || offset == 9) {
                sb.append("-");
            }
        }
        return sb.toString();
    }

    public static String findMajor(final byte[] data){

        String major = String.format("%02x%02x", data[ARGS_START_INDEX], data[ARGS_START_INDEX + 1]);
        return major;
    }

    public static String findMinor(final byte[] data){

        String minor = String.format("%02x%02x", data[ARGS_START_INDEX + 2], data[ARGS_START_INDEX + 3]);
        return minor;
    }

    protected static BeaconModel updateBeaconById(ArrayList<BeaconModel> founded, String address, String major, String minor){
        BeaconModel aux;
        for (int i=0; i<founded.size(); i++){
            if(address.equals(founded.get(i).getAddress())){
                aux = founded.get(i);
                //aux.setArguments(arguments);
                aux.setMajor(major);
                aux.setMinor(minor);
                founded.set(i, aux);
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return "Uuid: "+ this.uuid + "\nMajor: " + this.major + "\nMinor: " + this.minor + " TxPower: " + this.txPower + " RSSI: " + this.rssi + "\nAddress: " + this.address + "\n";
    }

    @Override
    public boolean equals(Object obj) {
        try {
            BeaconModel beaconModel = (BeaconModel) obj;
            if(null == beaconModel){
                return true;
            } else if (beaconModel.getUuid().equals(this.getUuid())
                    && beaconModel.getMinor().equals(this.getMinor())
                    && beaconModel.getMajor().equals(this.getMajor())) {
                return true;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
