package com.unime.beacontest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.unime.beacontest.beacon.utils.BeaconResults;
import com.unime.beacontest.objectinteraction.BeaconCommand;
import com.unime.beacontest.objectinteraction.SmartObjectIntentService;
import com.unime.beacontest.objectinteraction.SmartObjectInteraction;
import com.unime.beacontest.smartcoreinteraction.SmartCoreInteraction;

import java.util.List;
import java.util.Objects;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_ACK;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_WIFI_CONN;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_SMART_ENV;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SEND_COMMAND_OBJ;
import static com.unime.beacontest.beacon.utils.BeaconResults.BEACON_RESULTS;
import static com.unime.beacontest.objectinteraction.SmartObjectIntentService.EXTRA_BEACON_COMMAND;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private BeaconBroadcastReceiver beaconBroadcastReceiver = new BeaconBroadcastReceiver();
    private NetworkStateBroadcastReceiver networkStateBroadcastReceiver = new NetworkStateBroadcastReceiver();
    private IntentFilter beaconIntentFilter = new IntentFilter();
    private IntentFilter networkIntentFilter = new IntentFilter();

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
        beaconIntentFilter.addAction(ACTION_SCAN_ACK);
        beaconIntentFilter.addAction(ACTION_SCAN_SMART_ENV);
        beaconIntentFilter.addAction(ACTION_WIFI_CONN);

        networkIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        editTextCounter = (EditText) findViewById(R.id.counter);
        editIdObj = (EditText) findViewById(R.id.idobj);
        editTextCommand = (EditText) findViewById(R.id.command);
        editIdUser = (EditText) findViewById(R.id.iduser);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(beaconBroadcastReceiver, beaconIntentFilter);
        registerReceiver(networkStateBroadcastReceiver, networkIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(beaconBroadcastReceiver);
        unregisterReceiver(networkStateBroadcastReceiver);
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

                // test invio comando
                BeaconCommand beaconCommand = new BeaconCommand();
                // beaconCommand.setBitmap((byte)0b11111111); // it works!
                beaconCommand.setCounter(Settings.counter);
                beaconCommand.setCommandType("01"); // TODO cast all to integer ? Maybe no
                beaconCommand.setCommandClass("00");
                beaconCommand.setCommandOpCode("01");
                beaconCommand.setParameters("00", "00");
                beaconCommand.setUserId(Settings.USER_ID);
                beaconCommand.setObjectId(Settings.OBJECT_ID);

                Intent myIntent = new Intent(this, SmartObjectIntentService.class);
                myIntent.setAction(ACTION_SEND_COMMAND_OBJ);
                myIntent.putExtra(EXTRA_BEACON_COMMAND, beaconCommand);

                Log.d(TAG, "onButtonClick: context " + this + " " + getBaseContext());
                startService(myIntent);


                //mSmartObjectInteraction = new SmartObjectInteraction(mService);
                //mSmartObjectInteraction.setBeaconCommand(beaconCommand);
                //mSmartObjectInteraction.interact();


             /*mSmartCoreInteraction = new SmartCoreInteraction(mService);
             // mSmartCoreInteraction.connectToWifi(Settings.ssid, "starwars");
                mSmartCoreInteraction.checkForSmartEnvironment();
                */

        }
    }


    public class BeaconBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "BeaconBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO do something with this data
            Log.d(TAG, "BeaconBroadcastReceiver: action " + intent.getAction());

            if (Objects.equals(intent.getAction(), ACTION_SCAN_SMART_ENV)) { // TODO check if it works
                BeaconResults beaconResults = (BeaconResults) intent.getSerializableExtra(BEACON_RESULTS);

                Log.d(TAG, "onReceive: " + beaconResults.getResults());

                if(null != mSmartCoreInteraction) {
                    Log.d(TAG, "onReceive: sendHelloAck");
                    if(mSmartCoreInteraction.getHelloIv() != null) {
                        mSmartCoreInteraction.sendHelloAck();
                        mSmartCoreInteraction.checkForWifiPassword();
                    }
                }
            } else if (Objects.equals(intent.getAction(), ACTION_WIFI_CONN)) {
                BeaconResults beaconResults = (BeaconResults) intent.getSerializableExtra(BEACON_RESULTS);
                List<String> passwords = mSmartCoreInteraction.getPasswords(beaconResults.getResults());

                Log.d(TAG, "onReceive: Passwords " + passwords);
                // todo try all passwords, for now just the first one?
                if (passwords.size() >= 1) {
                    mSmartCoreInteraction.connectToWifi(Settings.ssid, passwords.get(0));
                    Log.d(TAG, "onReceive: ooooooooooo password: " + passwords.get(0));
                }
            }
        }
    }

    public class NetworkStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//            if(wifiManager == null) {
//                return;
//            }
//
//            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//
//            SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
//            int netId = sharedPref.getInt(NET_ID_PREF_KEY, -1);
//
//            if(wifiInfo.getNetworkId() != netId && mSmartCoreInteraction != null) {
//                // something went wrong
//                Log.d(TAG, "onReceive: retrying connection");
//                mSmartCoreInteraction.incConnRetryCounter();
//                if(mSmartCoreInteraction.getConnRetryCounter() <= MAX_CONN_RETRY) {
//                    mSmartCoreInteraction.checkForWifiPassword();
//                } else {
//                    mSmartCoreInteraction.resetConnRetryCounter();
//                }
//            }

            // todo unregister receiver insmartcore interaction
        }
    }
}
