package com.it4medicine.mobilenurse.core.network.tests;

import com.it4medicine.mobilenurse.core.network.vkMQ;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by root on 05.08.15.
 */
public class MQTests {

    public static void tst_ConnectAndSendOnce(final int amount, final int size){
        System.out.println(" --- TEST: Connect and send msg ---");
        System.out.println(" --- Amount: " + amount + " size: " + size);
        long startTime=0, currTime=0, elapsedTime =0;
        long waitTime = 0;

        startTime = System.currentTimeMillis();

        vkMQ mq = new vkMQ();
        mq.setHost("rabbitmq.it4medicine.com")
                .setRequestQueueName(vkMQ.QUEUE_HANDSHAKE)
                .setReplyQueueName(vkMQ.QUEUE_TEST_RESPONSE);
        try{
            mq.connect();

            byte[] bytes = new byte[size];
            Arrays.fill(bytes, (byte) 0);

            ArrayList<Thread> threads = new ArrayList<>();

            int THREADS_MAX = 10000;

            for (int i=0; i<amount; ++i) {
                threads.add(mq.send(bytes));
                System.out.print("\rSent " + i);

                if (threads.size() >= THREADS_MAX) {
                    for(Thread t : threads){
                        t.join();
                    }
                }
            }
            for(Thread t : threads){
                t.join();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (mq.isConnectionOpen()) try {

                mq.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Cycle  took: " + elapsedTime + " millis");

    }


    public static void tst_ConnectAndSendRAW(final int amount, final int size){
        System.out.println(" --- TEST: Connect and send msg ---");
        System.out.println(" --- Amount: " + amount + " size: " + size);
        long startTime=0, currTime=0, elapsedTime =0;
        long waitTime = 0;

        startTime = System.currentTimeMillis();

        vkMQ mq = new vkMQ();
        mq.setHost("rabbitmq.it4medicine.com")
                .setRequestQueueName(vkMQ.QUEUE_HANDSHAKE)
                .setReplyQueueName(vkMQ.QUEUE_TEST_RESPONSE);
        try{
            mq.connect();

            byte[] bytes = new byte[size];
            Arrays.fill(bytes, (byte) 0);

            for (int i=0; i<amount; ++i) {
                mq.RAW_SEND(bytes, "ta-nu-nahui");
                System.out.print("\rSent " + i);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (mq.isConnectionOpen()) try {

                mq.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Cycle  took: " + elapsedTime + " millis");
    }


    public static void tst_ConnectDisconnect(){
        System.out.println(" --- TEST: Connect to and disconnect from master server ---");
        long startTime=0, currTime=0, elapsedTime =0;

        startTime = System.currentTimeMillis();
        vkMQ mq = new vkMQ();
        mq.setHost("rabbitmq.it4medicine.com")
                .setRequestQueueName(vkMQ.QUEUE_TEST_REQUEST)
                .setReplyQueueName(vkMQ.QUEUE_TEST_RESPONSE);
        try{
            mq.connect();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (mq.isConnectionOpen()) try {
                mq.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Connection cycle (no msg sent) took: " + elapsedTime + " millis");
    }
}
