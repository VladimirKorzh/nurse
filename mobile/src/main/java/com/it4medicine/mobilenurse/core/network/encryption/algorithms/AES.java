package com.it4medicine.mobilenurse.core.network.encryption.algorithms;

import com.it4medicine.mobilenurse.core.network.Encryptor;
import com.it4medicine.mobilenurse.core.network.packets.NetworkPacket;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.security.SecureRandom;
import java.util.Arrays;


/**
 *
 *
 * Created by Vladimir Korshak on 03.08.15.
 */


public class AES {
    public static byte[] key;
    public static byte[] iv;

    public static void generateKeys(){
        try {
            // generate a key
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);  // To use 256 bit keys, we need the "unlimited strength" encryption policy files from Sun.
            key = keygen.generateKey().getEncoded();
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            // build the initialization vector (randomly).
            SecureRandom random = new SecureRandom();
            iv = new byte[16]; // generate random 16 byte IV AES is always 16bytes
            random.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(byte[] key, byte[] iVector, byte[] value) {
        System.out.println("\nAES Encrypt func call");
        try {
            IvParameterSpec iv = new IvParameterSpec(iVector);

            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(value);
            System.out.println("-- encrypted string: " + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted).getBytes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] key, byte[] iVector, byte[] encrypted) {
        System.out.println("\nAES Decrypt func call");
        try {
            IvParameterSpec iv = new IvParameterSpec(iVector);

            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            System.out.println("-- decrypted string: " + Base64.encodeBase64String(original));

            return original;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void runTests(){
        main(new String[]{});
    }

    public static void main(String[] args) {
        /**
         *  AES encryption\decryption test
         */
        System.out.println(" --- AES ENCRYPTION MODULE TESTS ---");
        long startTime=0, currTime=0, elapsedTime =0;

        startTime = System.currentTimeMillis();
        generateKeys();

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Key generation took: " + elapsedTime + " millis");

        NetworkPacket networkPacket = Encryptor.getTestingPacket();
        System.out.println("-- Testing packet size: " + networkPacket.dump().length + " bytes");


        startTime = System.currentTimeMillis();
        byte[] encrypted = encrypt(key, iv, networkPacket.dump());

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Encryption took: "+ elapsedTime+" millis");


        startTime = System.currentTimeMillis();
        byte[] decrypted = decrypt(key, iv, encrypted);

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Decryption took: "+ elapsedTime+" millis");

        System.out.println();
        System.out.println("A: "+ new String(networkPacket.dump()));
        System.out.println("B: "+ new String(decrypted));
        System.out.println();

        if (Arrays.equals(networkPacket.dump(), decrypted)){
            System.out.println("AES ENCRYPTION TESTING: SUCCESS");
        } else {
            System.out.println("AES ENCRYPTION TESTING: ERROR");
        }
    }
}