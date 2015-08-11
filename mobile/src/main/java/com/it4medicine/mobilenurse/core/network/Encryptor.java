package com.it4medicine.mobilenurse.core.network;

import com.it4medicine.mobilenurse.core.network.encryption.HandshakeParameters;
import com.it4medicine.mobilenurse.core.network.encryption.algorithms.AES;
import com.it4medicine.mobilenurse.core.network.encryption.algorithms.RSA;
import com.it4medicine.mobilenurse.core.network.packets.DefaultPayload;
import com.it4medicine.mobilenurse.core.network.packets.NetworkPacket;
import com.it4medicine.mobilenurse.core.network.tests.EncryptionTests;

import java.util.HashMap;

/**
 *
 *
 * Created by Vladimir Korshak on 04.08.15.
 */
public class Encryptor {

    public static byte[] encrypt(HandshakeParameters h, NetworkPacket p){
        System.out.println("\n --- Encryption start --- \n");

        byte[] result = null;

        // encrypt internal payload with RSA
        NetworkPacket internalPacket = RSA.encrypt(p.getPayload(), RSA.readPublicKeyBytes(h.map.get(HandshakeParameters.FIELDS.RSA_CLIENT_PUBLIC_KEY)));

        if (internalPacket != null) {
            result = AES.encrypt(h.map.get(HandshakeParameters.FIELDS.AES_ENCRYPTION_KEY),
                    h.map.get(HandshakeParameters.FIELDS.AES_INIT_VECTOR),
                    internalPacket.dump());
        }

        return result;
    }

    public static NetworkPacket decrypt(HandshakeParameters h, byte[] bytes){
        System.out.println("\n --- Decryption start --- \n");
        NetworkPacket netPkt = null;

        byte[] aes_decrypted_bytes = AES.decrypt(h.map.get(HandshakeParameters.FIELDS.AES_ENCRYPTION_KEY),
                h.map.get(HandshakeParameters.FIELDS.AES_INIT_VECTOR),
                bytes);

        netPkt = NetworkPacket.load(aes_decrypted_bytes);

        netPkt = RSA.decrypt(netPkt, RSA.readPrivateKeyBytes(h.map.get(HandshakeParameters.FIELDS.RSA_CLIENT_PRIVATE_KEY)));
        return netPkt;
    }


    public static NetworkPacket getTestingPacket(){
        // prepare a mockup packet
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("subvalue",1);
        hashMap.put("substring", "string");

        DefaultPayload packet = new DefaultPayload();
        packet.payload.put("test","string");
        packet.payload.put("value", 1);
        packet.payload.put("map", hashMap);

        // create a network packet based on the payload of our choice
        return packet.wrap();
    }

    public static void runTests(){
        main(new String[]{});
    }

    public static void main(String[] args) {
        EncryptionTests.tst_rsa();
        EncryptionTests.tst_aes();
        EncryptionTests.tst_full_encryption();
    }
}
