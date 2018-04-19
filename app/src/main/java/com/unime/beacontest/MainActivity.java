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
import com.unime.beacontest.objectinteraction.SmartObjectInteraction;
import com.unime.beacontest.smartcoreinteraction.SmartCoreInteraction;

import java.util.Objects;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_ACK;
import static com.unime.beacontest.beacon.utils.BeaconResults.BEACON_RESULTS;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    BeaconService mService;
    boolean mBound = false;

    private BeaconBroadcastReceiver beaconBroadcastReceiver = new BeaconBroadcastReceiver();
    private IntentFilter mIntentFilter = new IntentFilter();

    private EditText editTextCounter;
    private EditText editTextCommand;
    private EditText editIdObj;
    private EditText editIdUser;

    private SmartObjectInteraction mSmartObjectInteraction;
    private SmartCoreInteraction mSmartCoreInteraction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO add actions here when adding custom actions
        mIntentFilter.addAction(ACTION_SCAN_ACK);

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
                /*
                //String command = editTextCommand.getText().toString();
                //String counter = editTextCounter.getText().toString();
                //String idObject = editIdObj.getText().toString();
                //String idUser = editIdUser.getText().toString();
                //String command = "0123456789";
                String counter = "255";
                //String idObject = "1234";
                //String idUser = "7689";


                BeaconCommand beaconCommand = new BeaconCommand();
                // beaconCommand.setBitmap((byte)0b11111111); // it works!
                beaconCommand.setCounter(UnsignedLong.valueOf(counter));
                beaconCommand.setCommandType("01"); // TODO cast all to integer ? Maybe no
                beaconCommand.setCommandClass("01");
                beaconCommand.setCommandOpCode("00");
                beaconCommand.setParameters("00", "00");
                beaconCommand.setUserId("0001");
                beaconCommand.setObjectId("01", "02");
                // beaconCommand.randomizeReserved();

                mService.sending(beaconCommand.build(), 15000);
            } else {
                CustomFilter.Builder builder = new CustomFilter.Builder();
                //builder.addFilter(new Filter(Filter.UUID_TYPE, "0000", 0,1));
                //builder.addFilter(new Filter(Filter.UUID_TYPE, "0001", 3,4));
                //builder.addFilter(new Filter(Filter.UUID_TYPE, "0000", 14, 15));
                //builder.addFilter(new Filter(Filter.MAJOR_TYPE, "07", 1, 1));
                //builder.addFilter(new Filter(Filter.MINOR_TYPE, "09", 1, 1));
                CustomFilter customFilter = builder.build();
                mService.scanning(customFilter, -70, 5000);
            }
            */

             /*   // test invio comando
                BeaconCommand beaconCommand = new BeaconCommand();
                // beaconCommand.setBitmap((byte)0b11111111); // it works!
                beaconCommand.setCounter(Settings.counter);
                beaconCommand.setCommandType("01"); // TODO cast all to integer ? Maybe no
                beaconCommand.setCommandClass("00");
                beaconCommand.setCommandOpCode("01");
                beaconCommand.setParameters("00", "00");
                beaconCommand.setUserId(Settings.USER_ID);
                beaconCommand.setObjectId(Settings.OBJECT_ID);

                mSmartObjectInteraction = new SmartObjectInteraction(mService);
                mSmartObjectInteraction.setBeaconCommand(beaconCommand);
                mSmartObjectInteraction.interact();
                */
             mSmartCoreInteraction = new SmartCoreInteraction(mService);
             mSmartCoreInteraction.connectToWifi(Settings.ssid, "starwars");
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
        private static final String TAG = "BeaconBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO do something with this data
            if(Objects.equals(intent.getAction(), ACTION_SCAN_ACK)) { // TODO check if it works
                BeaconResults beaconResults = (BeaconResults) intent.getSerializableExtra(BEACON_RESULTS);
                Log.d(TAG, "onReceive: " + beaconResults.getResults());

                if(null != mSmartObjectInteraction && mBound)
                    mService.verifyAck(beaconResults, mSmartObjectInteraction);
            }

        }
    }
}
