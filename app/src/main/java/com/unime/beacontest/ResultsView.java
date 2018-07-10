package com.unime.beacontest;

import com.unime.beacontest.Classifier.Recognition;

import java.util.List;

public interface ResultsView {
    void setResults(final List<Recognition> results);
}
