package com.it4medicine.mobilenurse.core.network.encryption;

import java.util.HashMap;

/**
 * Created by root on 05.08.15.
 */
public class HandshakeParameters {
    public HashMap<FIELDS, byte[]> map;

    public enum FIELDS {
        PING_MSG,
        PONG_MSG,
        RSA_CLIENT_PUBLIC_KEY,
        RSA_CLIENT_PRIVATE_KEY,
        RSA_SERVER_PUBLIC_KEY,
        RSA_SERVER_PRIVATE_KEY,
        AES_INIT_VECTOR,
        AES_ENCRYPTION_KEY
    }

    public HandshakeParameters(){
        this.map = new HashMap<>();
        this.map.put(FIELDS.PING_MSG, "PING".getBytes());
        this.map.put(FIELDS.PONG_MSG, "PONG".getBytes());
    }
}
