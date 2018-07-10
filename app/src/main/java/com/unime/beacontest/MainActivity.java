package com.unime.beacontest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.common.primitives.UnsignedLong;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private EditText editTextCounter;
    private EditText editIdObj;
    private EditText editIdUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCounter = (EditText) findViewById(R.id.counter);
        editIdObj = (EditText) findViewById(R.id.idobj);
        editIdUser = (EditText) findViewById(R.id.iduser);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute) */
    public void onButtonClick(View v) {
        int buttonClickedId = v.getId();


            Log.d(TAG, "onButtonClick: bounded");
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.


            if(buttonClickedId == R.id.btnStart) {
                String counter = editTextCounter.getText().toString();
                String idObject = editIdObj.getText().toString();
                String idUser = editIdUser.getText().toString();

                if(!counter.equals("counter")) {
                    Settings.counter = UnsignedLong.valueOf(counter);
                }

                if(!idObject.equals("idobj")) {
                    Settings.OBJECT_ID = idObject;
                }

                if(!idUser.equals("iduser")) {
                    Settings.USER_ID = idUser;
                }


                /*
                BeaconCommand beaconCommand = new BeaconCommand();
                // beaconCommand.setBitmap((byte)0b11111111); // it works!
                beaconCommand.setCounter(Settings.counter);
                beaconCommand.setCommandType("01");
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

                */
            }

    }


}
