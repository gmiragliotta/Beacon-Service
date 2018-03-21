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
import android.widget.EditText;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import com.unime.beacontest.beacon.BeaconService;
import com.unime.beacontest.beacon.BeaconService.LocalBinder;
import com.unime.beacontest.beacon.utils.BeaconResults;
import com.unime.beacontest.beacon.utils.CustomFilter;
import com.unime.beacontest.beacon.utils.Filter;
import com.unime.beacontest.objectinteraction.BeaconCommand;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    BeaconService mService;
    boolean mBound = false;
    private BeaconResults beaconResults;

    private BeaconBroadcastReceiver beaconBroadcastReceiver = new BeaconBroadcastReceiver();
    private IntentFilter mIntentFilter = new IntentFilter();

    private EditText editTextCounter;
    private EditText editTextCommand;
    private EditText editIdObj;
    private EditText editIdUser;

    private byte[] key = BaseEncoding.base16().lowerCase().decode("9bd9cdf6be2b9d58fbd2ef3ed83769a0caf56fd0acc3e052f07afab8dd013f45");
    private byte[] iv = BaseEncoding.base16().lowerCase().decode("efaa299f48510f04181eb53b42ff1c01");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntentFilter.addAction("ActionScanningComplete");

        editTextCounter = (EditText) findViewById(R.id.counter);
        editIdObj = (EditText) findViewById(R.id.idobj);
        editTextCommand = (EditText) findViewById(R.id.command);
        editIdUser = (EditText) findViewById(R.id.iduser);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, BeaconService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        registerReceiver(beaconBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
        unregisterReceiver(beaconBroadcastReceiver);
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute) */
    public void onButtonClick(View v) {
        int buttonClickedId = v.getId();
        Log.d(TAG, "onButtonClick: start");
        if (mBound) {
            Log.d(TAG, "onButtonClick: bounded");
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.


            if(buttonClickedId == R.id.btnSend) {

                //String command = editTextCommand.getText().toString();
                //String counter = editTextCounter.getText().toString();
                //String idObject = editIdObj.getText().toString();
                //String idUser = editIdUser.getText().toString();
                //String command = "0123456789";
                String counter = "255";
                //String idObject = "1234";
                //String idUser = "7689";


                BeaconCommand beaconCommand = new BeaconCommand();
                beaconCommand.setBitmap((byte)0b11111111); // it works!
                beaconCommand.setCounter(UnsignedLong.valueOf(counter));
                beaconCommand.setCommandType("01"); // TODO cast all to integer ? Maybe no
                beaconCommand.setCommandClass("01");
                beaconCommand.setCommandOpCode("00");
                beaconCommand.setParameters("00", "00");
                beaconCommand.setUserId("0001");
                beaconCommand.setObjectId("01", "02");
                // beaconCommand.randomizeReserved();
                beaconCommand.setKey(key);
                beaconCommand.setIv(iv);

                mService.sending(beaconCommand.build(), 15000);
            } else {
                CustomFilter.Builder builder = new CustomFilter.Builder();
                builder.addFilter(new Filter(Filter.UUID_TYPE, "0000", 0,1));
                builder.addFilter(new Filter(Filter.UUID_TYPE, "0001", 3,4));
                builder.addFilter(new Filter(Filter.UUID_TYPE, "0000", 14, 15));
                builder.addFilter(new Filter(Filter.MAJOR_TYPE, "07", 1, 1));
                //builder.addFilter(new Filter(Filter.MINOR_TYPE, "09", 1, 1));
                CustomFilter customFilter = builder.build();
                beaconResults = mService.scanning(customFilter, -70, 3);
            }
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

    public class BeaconBroadcastReceiver extends BroadcastReceiver {
        public static final String ACTION_SCANNING_COMPLETE = "ActionScanningComplete";
        private static final String TAG = "BeaconBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO do something with this data
            if(intent.getAction().equals(ACTION_SCANNING_COMPLETE)) {
                Log.d(TAG, "onReceive: " + beaconResults.getResults());
            }

        }
    }
}
