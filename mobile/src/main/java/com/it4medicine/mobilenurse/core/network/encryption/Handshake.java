package com.it4medicine.mobilenurse.core.network.encryption;

import android.os.AsyncTask;
import android.util.Log;

import com.it4medicine.mobilenurse.core.network.encryption.algorithms.AES;
import com.it4medicine.mobilenurse.core.network.encryption.algorithms.RSA;
import com.it4medicine.mobilenurse.core.network.packets.DefaultPayload;
import com.it4medicine.mobilenurse.core.network.packets.NetworkPacket;
import com.it4medicine.mobilenurse.core.network.vkMQ;


/**
 *
 *
 * Created by Vladimir Korshak on 04.08.15.
 */
public class Handshake extends AsyncTask<Void, Void, Void> {

    public static final String MASTER_SERVER_HOST = "rabbitmq.it4medicine.com";
    public static final String EXCHANGE_FIELD = "message";
    public static final int    THREAD_WAIT_INTERVAL = 1000;
    public static final int    MQ_RESPONSE_WAIT_INTERVAL = 100;
    public static final int    MAX_WAIT_TIME = 30000;


    public  HandshakeParameters parameters;
    private HANDSHAKE_STATE handshakeState;
    private byte[] pending_message;
    private vkMQ mq;
    private int waitTime;

    enum HANDSHAKE_STATE {
        GENERATE_KEYS,
        SEND_PLAIN_PING, WAITING_FOR_PLAIN_PONG,
        SEND_CLIENT_PUBLIC, WAITING_FOR_SERVER_PUBLIC,
        SEND_CLIENT_IV, WAITING_FOR_SERVER_KEY,
        SEND_ENCODED_PING, WAITING_FOR_ENCODED_PONG,
        SUCCESS, FAILURE
    }

    private void setupVariables(){
        mq = null;
        pending_message = null;
        handshakeState = HANDSHAKE_STATE.GENERATE_KEYS;
        waitTime = 0;

        parameters = new HandshakeParameters();
    }

    public boolean isStillValid(){
        return false;
    }


    /**
     *
     * 0.  Generate RSA keys for handshake
     * 1.  Client introduces himself to master server "PING"
     * 1a. Client receives a response from master server "PONG"
     * 2.  Client sends his RSA public key
     * 2a. Client receives server's RSA public key
     *
     * --- REST OF CONVERSATION IS ASSUMED TO BE ENCRYPTED WITH RSA
     *
     * 3.  Client sends encrypted IV for AES
     * 3a. Client receives server's AES encryption KEY
     *
     * --- REST OF CONVERSATION IS ASSUMED TO BE ENCRYPTED WITH RSA+AES
     *
     * 4.  Client sends "PING"
     * 4a. Client receives "PONG"
     * 5.  HANDSHAKE COMPLETE
     *
     */
    protected void handshakeProcessStateMachine() throws Exception{

        DefaultPayload pkt = null;
        NetworkPacket netPkt = null;

        switch (handshakeState){
            case GENERATE_KEYS:
                RSA.generateRSAkeys();
                AES.generateKeys();

                parameters.map.put(HandshakeParameters.FIELDS.RSA_CLIENT_PUBLIC_KEY, RSA.getKeyBytes(RSA.publicKey));
                parameters.map.put(HandshakeParameters.FIELDS.RSA_CLIENT_PRIVATE_KEY, RSA.getKeyBytes(RSA.privateKey));
                parameters.map.put(HandshakeParameters.FIELDS.AES_INIT_VECTOR, AES.iv);
                parameters.map.put(HandshakeParameters.FIELDS.AES_ENCRYPTION_KEY, AES.key);

                handshakeState = HANDSHAKE_STATE.SEND_PLAIN_PING;

                Log.d("Handshake","Keys generated");

            case SEND_PLAIN_PING:
                pkt = new DefaultPayload();
                pkt.payload.put(EXCHANGE_FIELD,
                        parameters.map.get(HandshakeParameters.FIELDS.PING_MSG));

                mq.send(pkt.wrap().dump(), handshakeState.toString());
                handshakeState = HANDSHAKE_STATE.WAITING_FOR_PLAIN_PONG;

                Log.d("Handshake","ping sent");

                break;

            case WAITING_FOR_PLAIN_PONG:
                netPkt = NetworkPacket.load(pending_message);
                pkt = DefaultPayload.unwrap(netPkt);
                pending_message = null;

                if (parameters.map.get(HandshakeParameters.FIELDS.PONG_MSG).equals( pkt.payload.get(EXCHANGE_FIELD) ) ) {
                    handshakeState = HANDSHAKE_STATE.SEND_CLIENT_PUBLIC;
                }
                else {
                    handshakeState = HANDSHAKE_STATE.FAILURE;
                }
                Log.d("Handshake","pong received");


            case SEND_CLIENT_PUBLIC:
                pkt = new DefaultPayload();
                pkt.payload.put(EXCHANGE_FIELD, parameters.map.get(HandshakeParameters.FIELDS.RSA_CLIENT_PUBLIC_KEY));

                mq.send(pkt.wrap().dump(), handshakeState.toString());
                handshakeState = HANDSHAKE_STATE.WAITING_FOR_SERVER_PUBLIC;
                Log.d("Handshake","sent public");
                break;

            case WAITING_FOR_SERVER_PUBLIC:
                netPkt = NetworkPacket.load(pending_message);
                pkt = DefaultPayload.unwrap(netPkt);
                pending_message = null;

                parameters.map.put(HandshakeParameters.FIELDS.RSA_SERVER_PUBLIC_KEY, (byte[]) pkt.payload.get(EXCHANGE_FIELD));
                handshakeState = HANDSHAKE_STATE.SEND_CLIENT_IV;

                Log.d("Handshake","server public received");

            case SEND_CLIENT_IV:
                pkt = new DefaultPayload();
                pkt.payload.put(EXCHANGE_FIELD, parameters.map.get(HandshakeParameters.FIELDS.AES_INIT_VECTOR));

                netPkt = RSA.encrypt(pkt.toJson().getBytes(),
                        RSA.readPublicKeyBytes(parameters.map.get(HandshakeParameters.FIELDS.RSA_SERVER_PUBLIC_KEY)));

                mq.send(netPkt.dump(), handshakeState.toString());

                handshakeState = HANDSHAKE_STATE.WAITING_FOR_SERVER_KEY;

                Log.d("Handshake","sent iv");

                break;

            case WAITING_FOR_SERVER_KEY:
                netPkt = NetworkPacket.load(pending_message);
                netPkt = RSA.decrypt(netPkt,
                        RSA.readPrivateKeyBytes(parameters.map.get(HandshakeParameters.FIELDS.RSA_CLIENT_PRIVATE_KEY)));

                pkt = DefaultPayload.unwrap(netPkt);
                pending_message = null;

                parameters.map.put(HandshakeParameters.FIELDS.AES_ENCRYPTION_KEY, (byte[]) pkt.payload.get(EXCHANGE_FIELD));

                handshakeState = HANDSHAKE_STATE.SEND_ENCODED_PING;
                Log.d("Handshake","key received");


            case SEND_ENCODED_PING:
                pkt = new DefaultPayload();
                pkt.payload.put(EXCHANGE_FIELD, parameters.map.get(HandshakeParameters.FIELDS.PING_MSG));

                netPkt = RSA.encrypt(pkt.toJson().getBytes(),
                        RSA.readPublicKeyBytes(parameters.map.get(HandshakeParameters.FIELDS.RSA_SERVER_PUBLIC_KEY)));

                byte[] aes_encrypted_bytes = AES.encrypt(parameters.map.get(HandshakeParameters.FIELDS.AES_ENCRYPTION_KEY),
                        parameters.map.get(HandshakeParameters.FIELDS.AES_INIT_VECTOR),
                        netPkt.dump());

                mq.send(aes_encrypted_bytes, handshakeState.toString());

                handshakeState = HANDSHAKE_STATE.WAITING_FOR_ENCODED_PONG;
                Log.d("Handshake","encoded ping sent");
                break;

            case WAITING_FOR_ENCODED_PONG:

                byte[] aes_decrypted_bytes = AES.decrypt(parameters.map.get(HandshakeParameters.FIELDS.AES_ENCRYPTION_KEY),
                        parameters.map.get(HandshakeParameters.FIELDS.AES_INIT_VECTOR),
                        pending_message);

                netPkt = NetworkPacket.load(aes_decrypted_bytes);

                netPkt = RSA.decrypt(netPkt, RSA.readPrivateKeyBytes(parameters.map.get(HandshakeParameters.FIELDS.RSA_CLIENT_PRIVATE_KEY)));
                pkt = DefaultPayload.unwrap(netPkt);

                pending_message = null;

                if (pkt.payload.get(EXCHANGE_FIELD).equals(HandshakeParameters.FIELDS.PONG_MSG)){
                    handshakeState = HANDSHAKE_STATE.SUCCESS;
                }
                else {
                    handshakeState = HANDSHAKE_STATE.FAILURE;
                }
                Log.d("Handshake","encoded ping received");

                break;
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Connect to RabbitMQ Server
        // create MQ object
        mq = new vkMQ()
                .setHost(MASTER_SERVER_HOST)
                .setRequestQueueName(vkMQ.QUEUE_HANDSHAKE)
                .setReplyQueueName("")
                .setOnReceiveMessageHandler(msgHandler);

        parameters = new HandshakeParameters();
        try {
            // Initialize connection
            mq.connect();
            Log.d("Handshake", "connected to mq");

            handshakeState = HANDSHAKE_STATE.GENERATE_KEYS;
            handshakeProcessStateMachine();

            while(  handshakeState == HANDSHAKE_STATE.SUCCESS ||
                    handshakeState == HANDSHAKE_STATE.FAILURE) {
                wait(THREAD_WAIT_INTERVAL);
                waitTime += THREAD_WAIT_INTERVAL;
                if (waitTime >= MAX_WAIT_TIME) break;
            }

            // close the connection
            mq.disconnect();
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // define a handler that deals with every message
    vkMQ.OnReceiveMessageHandler msgHandler = new vkMQ.OnReceiveMessageHandler() {
        @Override
        public void onReceiveMessage(byte[] message) {
            pending_message = message;
            try {
                handshakeProcessStateMachine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
