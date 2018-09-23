package com.unime.beacontest.objectinteraction;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import com.unime.beacontest.beacon.utils.AES256;
import com.unime.beacontest.Config;
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
    public static final String ACTION_COMMAND_EXEC = "BeaconCommandExecution";
    public static final String SMART_OBJ_COMMAND_EXEC = "CommandExecutionStatus";

    private static final String TAG = "SmartObjIntentService";

    private static final int COUNTER_INDEX_START = 0;
    private static final int COUNTER_INDEX_END = 16; // excluded
    private static final int COMMAND_INDEX_START = 16;
    private static final int COMMAND_INDEX_END = 26;

    private Config mConfig;
    private LocalBroadcastManager localBroadcastManager;


    public SmartObjectIntentService() {
        super("SmartObjectIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mSmartObjectInteraction = SmartObjectInteraction.getInstance(getApplicationContext());
        mConfig = Config.getInstance(getApplicationContext());
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        Log.d(TAG, "onCreate: " + getApplicationContext() + " " + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if ((intent != null ? intent.getAction() : null) == null) {
            return;
        }

        switch (intent.getAction()) {
            case ACTION_SEND_COMMAND_OBJ:
                BeaconCommand beaconCommand = intent.getParcelableExtra(EXTRA_BEACON_COMMAND);
                mSmartObjectInteraction.setBeaconCommand(beaconCommand);
                mSmartObjectInteraction.interact();
                break;
            case ACTION_SCAN_ACK:
                BeaconResults beaconResults = intent.getParcelableExtra(BEACON_RESULTS);

                Log.d(TAG, "onReceive: " + beaconResults.getResults());

                if (null != mSmartObjectInteraction) {
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

            UnsignedLong counter = mConfig.getCounter();

            for (BeaconModel beaconModel : beaconResults.getResults()) {
                try {
                    String clear = AES256.decrypt(BaseEncoding.base16().lowerCase().decode(
                            beaconModel.getClearUuid()), mConfig.getKey(), mConfig.getIv());

                    UnsignedLong counterReceived =
                            UnsignedLong.valueOf(new BigInteger(clear.substring(COUNTER_INDEX_START, COUNTER_INDEX_END), 16));

                    // Start Debug logs
                    Log.d(TAG, "verifyAck clear: " + clear);
                    Log.d(TAG, "verifyAck: check ackValue and userId -> " +
                            clear.substring(16, 26).equals(ACK_VALUE + mConfig.getUserId()));
                    // End Debug logs

                    // i have to increment my counter if the ack counter is less then mine
                    if (clear.substring(COMMAND_INDEX_START, COMMAND_INDEX_END).equals(ACK_VALUE + mConfig.getUserId())) {
                        Log.d(TAG, "Is it an ack: yes");

                        UnsignedLong counterPlusOne = counter.plus(UnsignedLong.ONE);

                        if (counterReceived.compareTo(counterPlusOne) == 0) {
                            mConfig.setCounter(counterPlusOne.toString());
                            Log.d(TAG, "New counter value -> " + mConfig.getCounter());

                            Log.d(TAG, "Counter match: ok");
                            ackFounded = true;
                            localBroadcastManager.sendBroadcast(new Intent(ACTION_COMMAND_EXEC).putExtra(SMART_OBJ_COMMAND_EXEC, true));
                        } else if (counter.compareTo(counterReceived) < 0) {
                            mConfig.setCounter(counterPlusOne.toString());
                            Log.d(TAG, "New counter value -> " + mConfig.getCounter());
                        } else {
                            Log.d(TAG, "Counter do not match!");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "verifyAck: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (!ackFounded) {
                Log.d(TAG, "verifyAck: Not Founded");
                Log.d(TAG, "verifyAck: counter " + mConfig.getCounter());

                if ((mSmartObjectInteraction.getRetryCounter() < SmartObjectInteraction.MAX_ACK_RETRY)) {
                    mSmartObjectInteraction.incRetryCounter();
                    mSmartObjectInteraction.interact();
                } else {
                    mSmartObjectInteraction.resetCounter();
                    localBroadcastManager.sendBroadcast(new Intent(ACTION_COMMAND_EXEC).putExtra(SMART_OBJ_COMMAND_EXEC, false));
                }
            }

            handlerThread.quit();
        });
    }
}
