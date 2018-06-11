package com.unime.beacontest.beacon.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeaconResults implements Parcelable {
    public static final String BEACON_RESULTS = "BeaconResults";
    private Set<BeaconModel> founded;
    private List<BeaconModel> foundedList;

    public BeaconResults() {
        founded = new HashSet<>();;
    }

    public List<BeaconModel> getResults() {
        return foundedList;
    }

    public void addResult(BeaconModel beaconDetected) {
        founded.add(beaconDetected);
    }

    public void clear() {
        if(!founded.isEmpty()) {
            founded.clear();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        foundedList = new ArrayList<>(this.founded);
        dest.writeTypedList(this.foundedList);
    }

    protected BeaconResults(Parcel in) {
        this.foundedList = in.createTypedArrayList(BeaconModel.CREATOR);
    }

    public static final Parcelable.Creator<BeaconResults> CREATOR = new Parcelable.Creator<BeaconResults>() {
        @Override
        public BeaconResults createFromParcel(Parcel source) {
            return new BeaconResults(source);
        }

        @Override
        public BeaconResults[] newArray(int size) {
            return new BeaconResults[size];
        }
    };
}
