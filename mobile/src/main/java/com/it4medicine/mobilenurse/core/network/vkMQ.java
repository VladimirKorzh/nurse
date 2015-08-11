package com.it4medicine.mobilenurse.core.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * It is expected that handling of all of the exceptions is done by the user of this class
 *
 * Created by Vladimir Korshak on 04.08.15.
 */
public class vkMQ {

    // Usage specific constant parameters
    private final static String DEFAULT_HOST = "localhost";
    private final static int    X_MESSAGE_TTL = 60000;
    private final static int    PRE_FETCH_COUNT = 1;
    private final static int    HEARTBEAT = 5;

    public final static String QUEUE_HANDSHAKE = "handshake";
    public final static String QUEUE_ENCRYPTED = "encrypted";
    public final static String QUEUE_CATALOG   = "catalog";
    public final static String QUEUE_SYNC      = "sync";
    public final static String QUEUE_PUSH      = "push";
    public final static String QUEUE_AUTH      = "auth";
    public final static String QUEUE_TEST_REQUEST  = "testQueries";
    public final static String QUEUE_TEST_RESPONSE = "testResponses";

    // member variables
    private Connection connection;
    private Channel channel;
    private QueueingConsumer consumer;

    // Dynamic variables specified by user
    private OnReceiveMessageHandler mOnReceiveMessageHandler;
    private String mReplyQueueName;
    private String mRequestQueueName;
    private String mHost;

    // default constructor
    public vkMQ(){
        this.mHost = DEFAULT_HOST;
    }

    public boolean isConnectionOpen(){
        return this.connection.isOpen();
    }


    public vkMQ setHost(String host){
        this.mHost = host;
        return this;
    }

    public vkMQ setReplyQueueName(String replyTo){
        this.mReplyQueueName = replyTo;
        return this;
    }

    public vkMQ setRequestQueueName(String queueName){
        this.mRequestQueueName = queueName;
        return this;
    }

    public vkMQ connect() throws Exception {
        // initialize connection to master server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(mHost);
        factory.setRequestedHeartbeat(HEARTBEAT);

        connection = factory.newConnection();
        channel = connection.createChannel();

        /**
         *  limit the number of unacknowledged messages on a channel (or connection) when consuming (aka "prefetch count").
         */
        channel.basicQos(PRE_FETCH_COUNT);


        // set the requirements for our queue
        Map<String, Object> args = new HashMap<String, Object>();

        // let all the messages expire in 60sec
        args.put("x-message-ttl", X_MESSAGE_TTL);

        /**
         *  queue - the name of the queue
         *  durable - true if we are declaring a durable queue (the queue will survive a server restart)
         *  exclusive - true if we are declaring an exclusive queue
         *  autoDelete - true if we are declaring an autodelete queue (server will delete it when no longer in use)
         *  arguments - other properties (construction arguments) for the queue
         */

        // declare the queue of our encrypted channel
        // durable, non-exclusive, non-autodelete
        channel.queueDeclare(mRequestQueueName, true, false, false, args);

        // declare the queue of our responses
        // non-durable, exclusive, autodelete //TODO set to EXCLUSIVE TRUE
        channel.queueDeclare(mReplyQueueName, false, false, true, args);


        // create a consumer thread that is going to react to the messages that we receive
        consumer = new QueueingConsumer(channel);

        /**
         * queue - the name of the queue
         * autoAck - true if the server should consider messages acknowledged once delivered; false if the server should expect explicit acknowledgements
         * callback - an interface to the consumer object
         */
        // setup the consumer, no auto ack
        channel.basicConsume(mReplyQueueName, false, consumer);

        return this;
    }


    // executes a simple RPC request with random Correlation ID
    // used in case if user doesn't care about the followup conversation
    // between server and client
    public Thread send(byte[] bytes) throws Exception{
        String corrId = java.util.UUID.randomUUID().toString();
        return send(bytes, corrId);
    }

    public void RAW_SEND(final byte[] bytes, String corrId) throws Exception{
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(mReplyQueueName)
                .build();

        channel.basicPublish("", mRequestQueueName, props, bytes);
    }

    public Thread send(final byte[] bytes, String corrId) throws Exception {
        final String cId = corrId;
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    AMQP.BasicProperties props = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(cId)
                            .replyTo(mReplyQueueName)
                            .build();

                    channel.basicPublish("", mRequestQueueName, props, bytes);

//                    while (true) {
//                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//                        if (delivery.getProperties().getCorrelationId().equals(cId)) {
//                            mOnReceiveMessageHandler.onReceiveMessage(delivery.getBody());
//                            break;
//                        }
//                    }
                } catch (IOException e){
                    System.out.println(getClass().toString() + " " + e.getMessage());
                }
            }
        });
        t.start();
        return t;
    }

    public void disconnect() throws Exception {
        connection.close();
    }

    public interface OnReceiveMessageHandler {
        public void onReceiveMessage(byte[] message);
    }

    public vkMQ setOnReceiveMessageHandler(OnReceiveMessageHandler handler) {
        mOnReceiveMessageHandler = handler;
        return this;
    }

    public static void runTests(){
        main(new String[]{});
    }

    public static void main(String[] args) {
        /**
         * Message Queue testing and timings
         */
//          MQTests.tst_ConnectDisconnect();
//            MQTests.tst_ConnectAndSendRAW(10, 1000);
//          MQTests.tst_ConnectAndSendOnce(10, 100);
    }
}
