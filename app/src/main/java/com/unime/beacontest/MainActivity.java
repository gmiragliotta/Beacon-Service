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

import com.unime.beacontest.beacon.BeaconService;
import com.unime.beacontest.beacon.BeaconService.LocalBinder;
import com.unime.beacontest.beacon.utils.BeaconResults;

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
        Log.d(TAG, "onButtonClick: start");
        if (mBound) {
            Log.d(TAG, "onButtonClick: bounded");
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.

            /* CustomFilter.Builder builder = new CustomFilter.Builder();
            builder.addFilter(new Filter(Filter.UUID_TYPE, "0000", 0,1));
            builder.addFilter(new Filter(Filter.UUID_TYPE, "0001", 3,4));
            builder.addFilter(new Filter(Filter.UUID_TYPE, "0000", 14, 15));
            builder.addFilter(new Filter(Filter.MAJOR_TYPE, "07", 1, 1));
            //builder.addFilter(new Filter(Filter.MINOR_TYPE, "09", 1, 1));
            CustomFilter customFilter = builder.build();
            beaconResults = mService.scanning(customFilter, -70, 3); */

            String command = editTextCommand.getText().toString();
            String counter = editTextCounter.getText().toString();
            String idObject = editIdObj.getText().toString();
            String idUser = editIdUser.getText().toString();

            BeaconCommand beaconCommand = new BeaconCommand(
                 idUser, idObject, Long.parseLong(counter), command
            );

            beaconCommand.getBeaconModel();
            //Long prova = Long.parseLong(counter);

            // String encryptedUuid = "";

            //Log.d(TAG, "onButtonClick: " + ConversionUtils.longToBytes(prova).length);
            //mService.sending(new BeaconModel(encryptedUuid, idObject, "0000"), 5000);

//            mService.sending(
//                    new BeaconModel(
//                            "00000001-0002-025f-1234-0000fd200001",
//                            "0001",
//                            "0000"
//                    ),5000);
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
