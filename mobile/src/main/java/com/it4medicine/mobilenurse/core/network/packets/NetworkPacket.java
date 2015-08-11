package com.it4medicine.mobilenurse.core.network.packets;

import com.google.gson.Gson;

/**
 * Created by Vladimir Korshak on 03.08.15.
 */
public class NetworkPacket {
    private byte[] mPayload = null;
    private byte[] mSignature = null;
    private boolean mIsEncrypted = false;

    public NetworkPacket setSignature(byte[] signature) {
        this.mSignature = signature;
        return this;
    }

    public NetworkPacket setPayload(byte[] payload){
        this.mPayload = payload;
        return this;
    }

    public NetworkPacket setEncrypted(boolean value){
        this.mIsEncrypted = value;
        return this;
    }

    public boolean isEncrypted(){
        return this.mIsEncrypted;
    }

    public byte[] getPayload(){ return mPayload; }
    public byte[] getSignature(){ return mSignature; }

    public void print(){
        System.out.println("-- payload: " + mPayload);
        System.out.println("-- payload string: " + new String(mPayload));
        System.out.println("-- signature: " + mSignature);
        System.out.println("-- encrypted: " + mIsEncrypted);
    }

    public byte[] dump(){
        byte[] result = null;
        Gson gson = new Gson();
        result = gson.toJson(this).getBytes();
        return result;
    }
    public static NetworkPacket load(byte[] bytes){
        Gson gson = new Gson();
        return gson.fromJson(new String(bytes), NetworkPacket.class);
    }


}
