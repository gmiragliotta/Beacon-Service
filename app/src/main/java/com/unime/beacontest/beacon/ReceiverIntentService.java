package com.unime.beacontest.beacon;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class ReceiverIntentService extends IntentService {

    public ReceiverIntentService() {
        super("ReceiverIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
