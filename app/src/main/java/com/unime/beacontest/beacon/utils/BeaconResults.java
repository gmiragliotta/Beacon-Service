package com.unime.beacontest.beacon.utils;

import java.util.HashSet;
import java.util.Set;

public class BeaconResults {
    private Set<BeaconModel> founded;


    public BeaconResults() {
        founded = new HashSet<>();;
    }

    public Set<BeaconModel> getResults() {
        return founded;
    }

    public void addResults(BeaconModel beaconDetected) {
        founded.add(beaconDetected);
    }
}
