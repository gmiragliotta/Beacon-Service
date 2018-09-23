package com.unime.beacontest.beacon.utils;

import android.util.Log;

import com.google.common.io.BaseEncoding;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// https://gist.github.com/itarato/abef95871756970a9dad

public class AES256 {
    public static final String TAG = "AES256";

    public  static byte[] encrypt(byte[] clean, byte[] key, byte[] iv) throws Exception {
        Log.d(TAG, "encrypt key: " + BaseEncoding.base16().lowerCase().encode(key));
        Log.d(TAG, "encrypt iv: " + BaseEncoding.base16().lowerCase().encode(iv));

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        return encrypted;
    }

    public static String decrypt(byte[] encryptedBytes, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/NoPadding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return BaseEncoding.base16().lowerCase().encode(decrypted);
    }
}

