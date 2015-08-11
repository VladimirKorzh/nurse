package com.it4medicine.mobilenurse.core.network.tests;

import com.it4medicine.mobilenurse.core.network.Encryptor;
import com.it4medicine.mobilenurse.core.network.encryption.HandshakeParameters;
import com.it4medicine.mobilenurse.core.network.encryption.algorithms.AES;
import com.it4medicine.mobilenurse.core.network.encryption.algorithms.RSA;
import com.it4medicine.mobilenurse.core.network.packets.NetworkPacket;

/**
 * Created by root on 05.08.15.
 */
public class EncryptionTests {

    public static void tst_rsa(){
        RSA.runTests();
    }

    public static void tst_aes(){
        AES.runTests();
    }

    public static void tst_full_encryption(){
        /**
         *  Test encryption/decryption validity
         *  using a mockup handshake and a mockup packet
         */
        long startTime = 0, currTime = 0, elapsedTime = 0;

        System.out.println(" --- RUNNING GENERAL SYSTEM TEST ---");

        HandshakeParameters h = new HandshakeParameters();

        // both sides AES
        AES.generateKeys();
        h.map.put(HandshakeParameters.FIELDS.AES_ENCRYPTION_KEY, AES.key);
        h.map.put(HandshakeParameters.FIELDS.AES_INIT_VECTOR, AES.iv);

        // client side RSA
        RSA.generateRSAkeys();

        h.map.put(HandshakeParameters.FIELDS.RSA_CLIENT_PRIVATE_KEY, RSA.getKeyBytes(RSA.privateKey));
        h.map.put(HandshakeParameters.FIELDS.RSA_CLIENT_PUBLIC_KEY, RSA.getKeyBytes(RSA.publicKey));

        // server side RSA
        RSA.generateRSAkeys();
        h.map.put(HandshakeParameters.FIELDS.RSA_SERVER_PUBLIC_KEY, RSA.getKeyBytes(RSA.publicKey));
        h.map.put(HandshakeParameters.FIELDS.RSA_SERVER_PRIVATE_KEY, RSA.getKeyBytes(RSA.privateKey));


        NetworkPacket networkPacket = Encryptor.getTestingPacket();

        // encrypt the packet before sending to master server
        startTime = System.currentTimeMillis();
        byte[] raw_bytes = Encryptor.encrypt(h, networkPacket);

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Full encryption took: " + elapsedTime + " millis");

        // decrypt the packet on master server
        startTime = System.currentTimeMillis();
        NetworkPacket rcvdNetworkPacket = Encryptor.decrypt(h, raw_bytes);

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Full decryption took: " + elapsedTime + " millis");


        System.out.println();

        String payloadA = new String(networkPacket.getPayload());
        String payloadB = new String(rcvdNetworkPacket.getPayload());

        System.out.println("A: "+payloadA);
        System.out.println("B: "+payloadB);
        System.out.println();

        // check validity of data
        if (payloadA.equals(payloadB)){
            System.out.println("ENCRYPTOR MODULE TEST: SUCCESS");
        }
        else {
            System.out.println("ENCRYPTOR MODULE TEST: ERROR");
        }
    }
}
