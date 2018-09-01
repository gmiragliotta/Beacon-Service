package com.unime.beacontest.objectinteraction;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import com.unime.beacontest.AES256;
import com.unime.beacontest.Settings;
import com.unime.beacontest.beacon.utils.BeaconModel;
import com.unime.beacontest.beacon.utils.BeaconResults;

import java.math.BigInteger;

import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SCAN_ACK;
import static com.unime.beacontest.beacon.ActionsBeaconBroadcastReceiver.ACTION_SEND_COMMAND_OBJ;
import static com.unime.beacontest.beacon.utils.BeaconResults.BEACON_RESULTS;
import static com.unime.beacontest.objectinteraction.SmartObjectInteraction.ACK_VALUE;

public class SmartObjectIntentService extends IntentService {
    private SmartObjectInteraction mSmartObjectInteraction;

    public static final String EXTRA_BEACON_COMMAND = "BeaconCommand";
    private static final String TAG = "SmartObjIntentService";

    private static final int COUNTER_INDEX_START = 0;
    private static final int COUNTER_INDEX_END = 16; // excluded
    private static final int COMMAND_INDEX_START = 16;
    private static final int COMMAND_INDEX_END = 26;

    public SmartObjectIntentService() {
        super("SmartObjectIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mSmartObjectInteraction = SmartObjectInteraction.getInstance(getApplicationContext());
        Log.d(TAG, "onCreate: " + getApplicationContext() + " " + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if((intent != null ? intent.getAction() : null) == null) {
            return;
        }

        switch(intent.getAction()) {
            case ACTION_SEND_COMMAND_OBJ:
                BeaconCommand beaconCommand = intent.getParcelableExtra(EXTRA_BEACON_COMMAND);
                mSmartObjectInteraction.setBeaconCommand(beaconCommand);
                mSmartObjectInteraction.interact();
                break;
            case ACTION_SCAN_ACK: // TODO rename this
                BeaconResults beaconResults = intent.getParcelableExtra(BEACON_RESULTS);

                Log.d(TAG, "onReceive: " + beaconResults.getResults());

                if(null != mSmartObjectInteraction) {
                    verifyAck(beaconResults);
                }
                break;
        }
    }

    private void verifyAck(BeaconResults beaconResults) {
        HandlerThread handlerThread = new HandlerThread("SmartObjectVerifyAckThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();

        Handler mHandler = new Handler(looper);

        mHandler.post(() -> {
            mSmartObjectInteraction = SmartObjectInteraction.getInstance(getApplicationContext());

            boolean ackFounded = false;

            UnsignedLong counter = Settings.counter;

            for(BeaconModel beaconModel : beaconResults.getResults()) {
                try {
                    String clear = AES256.decrypt(BaseEncoding.base16().lowerCase().decode(
                            beaconModel.getClearUuid()), Settings.key, Settings.iv);

                    UnsignedLong counterReceived =
                            UnsignedLong.valueOf(new BigInteger(clear.substring(COUNTER_INDEX_START, COUNTER_INDEX_END), 16));

                    // Start Debug logs
                    Log.d(TAG, "verifyAck clear: " + clear);
                    Log.d(TAG, "verifyAck first check -> " +
                            counterReceived.equals(counter.plus(UnsignedLong.valueOf(1))) + " " + counter.toString());
                    Log.d(TAG, "verifyAck: second check -> " +
                            clear.substring(16, 26).equals(ACK_VALUE + Settings.USER_ID));
                    // End Debug logs

                    // i have to increment my counter if the ack counter is less then mine
                    if(clear.substring(COMMAND_INDEX_START, COMMAND_INDEX_END).equals(ACK_VALUE + Settings.USER_ID)) {
                        Log.d(TAG, "Is it an ack: ok");

                       if (counterReceived.compareTo(counter.plus(UnsignedLong.valueOf(1))) == 0) {
                            Settings.counter = Settings.counter.plus(UnsignedLong.valueOf(1));
                            Log.d(TAG, "New counter value -> " + Settings.counter.toString());

                            Log.d(TAG, "Counter match: ok");
                            ackFounded = true;
                        } else if(counter.compareTo(counterReceived) < 0) {
                           Settings.counter = Settings.counter.plus(UnsignedLong.valueOf(1));
                           Log.d(TAG, "New counter value -> " + Settings.counter.toString());
                       } else {
                           // TODO My counter is g.t. rasp counter. Should decrement it?
                            Log.d(TAG, "Counter do not match!");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "verifyAck: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if(!ackFounded) {
                Log.d(TAG, "verifyAck: Not Founded");
                Log.d(TAG, "verifyAck: counter " + Settings.counter);

                if((mSmartObjectInteraction.getRetryCounter() < SmartObjectInteraction.MAX_ACK_RETRY)) {
                    mSmartObjectInteraction.incRetryCounter();
                    mSmartObjectInteraction.interact();
                } else {
                    mSmartObjectInteraction.resetCounter();
                }
            }

            handlerThread.quitSafely();
        });
    }
}
