package com.unime.ctrlbeacon.beacon;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

public class PermissionsChecker {
    public static boolean checkBluetoothPermission(Context context, BluetoothAdapter mBluetoothAdapter) {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
            return false;
        } else {
            return true;
        }
    }
}
