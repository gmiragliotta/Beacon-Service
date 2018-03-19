package com.unime.beacontest;

import android.util.Log;

import com.unime.beacontest.beacon.utils.ConversionUtils;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// https://gist.github.com/itarato/abef95871756970a9dad

public class AES256 {
    public static final String TAG = "AES256";
    public static final int ivSize = 16;
    public static final int keySize = 32;




        // Generating IV.

        //System.out.println("dec: " + decrypted);


    public  static byte[] encrypt(String plainText, byte[] key, byte[] iv) throws Exception {
        Log.d(TAG, "encrypt iv: " + ConversionUtils.byteToHex(iv));

        byte[] clean = plainText.getBytes();


        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key);
        byte[] keyBytes = new byte[keySize];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        // Combine IV and encrypted part.
        byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);

        System.out.println(encrypted.length);
        return encrypted;
    }

    public static String decrypt(byte[] encryptedTextBytes, byte[] key, byte[] iv) throws Exception {

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Extract encrypted part.
        int encryptedSize = encryptedTextBytes.length;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedTextBytes, 0, encryptedBytes, 0, encryptedSize);

        // Hash key.
        byte[] keyBytes = new byte[keySize];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key);
        System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return new String(decrypted);
    }
}

