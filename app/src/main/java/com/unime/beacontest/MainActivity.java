package com.unime.beacontest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.unime.beacontest.objectinteraction.SmartObjectIntentService;
import com.unime.beacontest.objectinteraction.SmartObjectInteraction;
import com.unime.beacontest.smartcoreinteraction.SmartCoreInteraction;
import com.unime.beacontest.smartcoreinteraction.SmartCoreService;

import java.util.Arrays;

import static com.unime.beacontest.beacon.utils.BeaconResults.BEACON_RESULTS;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private EditText editTextCounter;
    private EditText editTextCommand;
    private EditText editIdObj;
    private EditText editIdUser;

    private SmartObjectInteraction mSmartObjectInteraction;
    private SmartCoreInteraction mSmartCoreInteraction;

    private SmartCoreReceiver mSmartCoreReceiver = new SmartCoreReceiver();;
    private IntentFilter mIntentFilter = new IntentFilter();
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        editTextCounter = (EditText) findViewById(R.id.counter);
        editIdObj = (EditText) findViewById(R.id.idobj);
        editTextCommand = (EditText) findViewById(R.id.command);
        editIdUser = (EditText) findViewById(R.id.iduser);

        mIntentFilter.addAction(SmartCoreService.ACTION_SMARTCORE_CONN);
        mIntentFilter.addAction(SmartCoreService.ACTION_SMARTCORE_SCAN);
        mIntentFilter.addAction(SmartObjectIntentService.ACTION_COMMAND_EXEC);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        localBroadcastManager.registerReceiver(mSmartCoreReceiver, mIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        localBroadcastManager.unregisterReceiver(mSmartCoreReceiver);
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute) */
    public void onButtonClick(View v) {
        int buttonClickedId = v.getId();
        Log.d(TAG, "onButtonClick: start");

            Log.d(TAG, "onButtonClick: bounded");
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.


            if(buttonClickedId == R.id.btnSend) {

                String[] objectsId = {"0000", "0001"};

                Config.getInstance(this).setObjectsId(Arrays.asList(objectsId));

//                // test invio comando
//                BeaconCommand beaconCommand = new BeaconCommand();
//                // beaconCommand.setBitmap((byte)0b11111111); // it works!
//                beaconCommand.setCounter(Config.getInstance(this).getCounter());
//                beaconCommand.setCommandType("01");
//                beaconCommand.setCommandClass("00");
//                beaconCommand.setCommandOpCode("01");
//                beaconCommand.setParameters("00", "00");
//                beaconCommand.setUserId(Config.getInstance(this).getUserId());
//                beaconCommand.setObjectId("0000");
//
//                SmartObjectIntentService.sendCommand(beaconCommand, this);


//                // it works fine
                mSmartCoreInteraction = SmartCoreInteraction.getInstance(this);
                mSmartCoreInteraction.checkForSmartEnvironment();
                CtrlBeacon.startSmartEnvScan(this);
                // mSmartCoreInteraction.connectToWifi(Config.ssid, "starwars");
                Handler handler = new Handler();

                handler.postDelayed(() -> {
                    CtrlBeacon.startSmartCoreConn(this);
                }, 2000);

        }
    }

    public class SmartCoreReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = (intent.getAction() != null) ? intent.getAction() : "";

            switch(action) {
                case SmartCoreService.ACTION_SMARTCORE_SCAN:
                    Log.d(TAG, "onReceive: smart env " + intent.getParcelableExtra(BEACON_RESULTS));
                    break;
                case SmartCoreService.ACTION_SMARTCORE_CONN:
                    Log.d(TAG, "onReceive: conn status " + intent.getBooleanExtra(SmartCoreService.SMARTCORE_CONN_STATUS, false));
                    break;
                case SmartObjectIntentService.ACTION_COMMAND_EXEC:
                    Log.d(TAG, "onReceive: command exec " + intent.getBooleanExtra(SmartObjectIntentService.SMART_OBJ_COMMAND_EXEC, false));
                    break;
                default:
                    Log.d(TAG, "onReceive: incorrect action");
            }
        }
    }
}
