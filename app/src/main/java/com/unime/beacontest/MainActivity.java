package com.unime.beacontest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.unime.beacontest.beacon.BeaconReceiver;
import com.unime.beacontest.beacon.ReceiverService;
import com.unime.beacontest.beacon.ReceiverService.LocalBinder;
import com.unime.beacontest.beacon.utils.CustomFilter;
import com.unime.beacontest.beacon.utils.Filter;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    ReceiverService mService;
    boolean mBound = false;

    IntentFilter mReceivedBeaconIntentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        AltBeaconManager altBeaconManager = new AltBeaconManager(this);
//        altBeaconManager.startAltBeaconService(AltBeaconUtils.ACTION_ALT_BEACON_SCANNING);
        mReceivedBeaconIntentFilter = new IntentFilter();
        mReceivedBeaconIntentFilter.addAction(BeaconReceiver.ACTION_BEACON_RECEIVED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, ReceiverService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute) */
    public void onButtonClick(View v) {
        Log.d(TAG, "onButtonClick: start");
        if (mBound) {
            Log.d(TAG, "onButtonClick: bounded");
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
            CustomFilter customFilter = new CustomFilter();
            customFilter.addFilter(new Filter(Filter.UUID_TYPE, "0000", 14, 15));
            customFilter.addFilter(new Filter(Filter.MAJOR_TYPE, "01", 1, 1));
            customFilter.addFilter(new Filter(Filter.MINOR_TYPE, "01", 1, 1));
            customFilter.build();
            mService.scanning(customFilter);
//            Toast.makeText(this, "number: " + num, Toast.LENGTH_SHORT).show();
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.d(TAG, "onServiceConnected: start");
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.d(TAG, "onServiceConnected: end");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    // TODO BroadcastReceiver
    private class ReceivedBeaconBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            
        }
    }
}
