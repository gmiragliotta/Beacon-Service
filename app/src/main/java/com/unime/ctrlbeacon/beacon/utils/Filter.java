package com.unime.ctrlbeacon.beacon.utils;

public interface Filter {

    boolean apply(byte[] data);
}
