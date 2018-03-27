package com.unime.beacontest.beacon.utils;

public interface Filter {

    boolean apply(byte[] data);
}
