package com.unime.beacontest.beacon.utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class BeaconResults implements Serializable {
    public static final String BEACON_RESULTS = "BeaconResults";
    private Set<BeaconModel> founded;


    public BeaconResults() {
        founded = new HashSet<>();;
    }

    public Set<BeaconModel> getResults() {
        return founded;
    }

    public void addResult(BeaconModel beaconDetected) {
        founded.add(beaconDetected);
    }

    public void clear() {
        if(!founded.isEmpty()) {
            founded.clear();
        }
    }

}
