package com.it4medicine.mobilenurse.core.network.packets;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 04.08.15.
 */
public class DefaultPayload {
    public Map<String, Object> payload = new HashMap<String, Object>();

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static DefaultPayload fromJson(String obj){
        Gson gson = new Gson();
        return gson.fromJson(obj, DefaultPayload.class);
    }

    public NetworkPacket wrap(){
        NetworkPacket pkt = new NetworkPacket();
        pkt.setPayload(this.toJson().getBytes());
        return pkt;
    }

    public static DefaultPayload unwrap(NetworkPacket pkt){
        return fromJson( new String (pkt.getPayload()) );
    }

}
