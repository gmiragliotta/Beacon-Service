package com.unime.beacontest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.unime.beacontest.beacon.AltBeaconManager;
import com.unime.beacontest.beacon.utils.AltBeaconUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AltBeaconManager altBeaconManager = new AltBeaconManager(this);
        altBeaconManager.startAltBeaconService(AltBeaconUtils.ACTION_ALT_BEACON_SCANNING);
    }


}
