package com.unime.beacontest.beacon.utils;

import java.util.HashSet;
import java.util.Set;

public class BeaconResults {
    private Set<BeaconModel> founded = new HashSet<>();

    public Set<BeaconModel> getResults() {
        return founded;
    }
}
