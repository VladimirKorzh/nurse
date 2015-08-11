package com.it4medicine.mobilenurse.core.network.encryption.algorithms;

import com.it4medicine.mobilenurse.core.network.Encryptor;
import com.it4medicine.mobilenurse.core.network.packets.NetworkPacket;

import org.apache.commons.codec.binary.Base64;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;


/**
 *
 *
 *  USED RSA/ECB/PKCS1PADDING Algorithm
 *
 *
 *
 * Created by Vladimir Korshak on 03.08.15.
 */
public class RSA {

    public static  PublicKey  publicKey  = null;
    public static  PrivateKey privateKey = null;
    private static Signature  signature  = null;

    public static byte[] getKeyBytes(Key key){
        return key.getEncoded();
    }

    public static PublicKey readPublicKeyBytes(byte[] keyBytes){
        PublicKey key = null;
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            key = keyFactory.generatePublic(spec);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return key;
    }

    public static PrivateKey readPrivateKeyBytes(byte[] keyBytes){
        PrivateKey key = null;
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            key = fact.generatePrivate(keySpec);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return key;
    }

    public static void generateRSAkeys(){
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

            SecureRandom random = new SecureRandom();
            kpg.initialize(2048, random);

            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();

            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec publicKeySpec = factory.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
            RSAPrivateKeySpec privateKeySpec = factory.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

            publicKey = factory.generatePublic(publicKeySpec);
            privateKey = factory.generatePrivate(privateKeySpec);

            signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(privateKey, new SecureRandom());
//
//            System.out.println("Public m: " + publicKeySpec.getModulus() + " e: " + publicKeySpec.getPublicExponent());
//            System.out.println("Private m: " + privateKeySpec.getModulus() + " e: " + privateKeySpec.getPrivateExponent());
//            System.out.println("Public key: " + publicKey.toString());
//            System.out.println("Private key: " + privateKey.toString());

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static NetworkPacket encrypt(byte[] msg, PublicKey publicKey){
        try {
            String msgString = new String(msg);
            System.out.println("\nRSA Encrypt func call: " + msgString);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            signature.update(msg);
            byte[] sigBytes = signature.sign();
            byte[] encrypted = cipher.doFinal(msg);

            NetworkPacket pkt = new NetworkPacket()
                    .setPayload(Base64.encodeBase64String(encrypted).getBytes())
                    .setSignature(sigBytes)
                    .setEncrypted(true);

            pkt.print();
            return pkt;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static NetworkPacket decrypt(NetworkPacket pkt, PrivateKey privateKey){
        try {
            NetworkPacket result = new NetworkPacket();

            System.out.println("\nRSA Decrypt func call: ");
            if (pkt.isEncrypted()) {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);

                byte[] original = cipher.doFinal(Base64.decodeBase64(pkt.getPayload()));

                if (verifySignature(original, pkt.getSignature()))
                    result.setPayload(original)
                            .setSignature(pkt.getSignature())
                            .setEncrypted(false);
            }
            result.print();
            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean verifySignature(byte[] rcvdBytes, byte[] claimedSignature){
        boolean result = false;
        try {
            signature.initVerify(publicKey);
            signature.update(rcvdBytes);
            result = signature.verify(claimedSignature);

            System.out.println("-- signature check: " + result);
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void runTests(){
        main(new String[] {});
    }

    public static void main(String[] args) {
        /**
         *  TEST encryption\decryption
         */
        System.out.println(" --- RSA ENCRYPTION MODULE TESTS ---");
        long startTime=0, currTime=0, elapsedTime =0;

        startTime = System.currentTimeMillis();
        generateRSAkeys();

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Key generation took: " + elapsedTime + " millis");

        NetworkPacket networkPacket = Encryptor.getTestingPacket();
        System.out.println("-- Testing packet size: " + networkPacket.dump().length + " bytes");


        startTime = System.currentTimeMillis();
        NetworkPacket encryptedPacket = encrypt(networkPacket.getPayload(), publicKey);

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Encryption took: "+ elapsedTime+" millis");


        startTime = System.currentTimeMillis();
        NetworkPacket decryptedPacket = decrypt(encryptedPacket, privateKey);

        currTime = System.currentTimeMillis();
        elapsedTime = currTime - startTime;
        System.out.println("-- Decryption took: "+ elapsedTime+" millis");

        System.out.println();

        String payloadA = new String(networkPacket.getPayload());
        String payloadB = new String(decryptedPacket.getPayload());

        System.out.println("A: "+payloadA);
        System.out.println("B: "+payloadB);
        System.out.println();


        if (payloadA.equals(payloadB)) {
            System.out.println("RSA ENCRYPTION TESTING: SUCCESS");
        } else {
            System.out.println("RSA ENCRYPTION TESTING: FAILURE");
        }

        /**
         *  TEST KEY CONVERSIONS
         */
        byte[] bytePublicKey = getKeyBytes(publicKey);
        byte[] bytePrivateKey = getKeyBytes(privateKey);
        PublicKey pubKey = readPublicKeyBytes(bytePublicKey);
        PrivateKey privKey = readPrivateKeyBytes(bytePrivateKey);

        if ( (publicKey.equals(pubKey)) && (privateKey.equals(privKey))){
            System.out.println("RSA KEY CONVERSIONS TESTING: SUCCESS");
        } else {
            System.out.println("RSA KEY CONVERSIONS TESTING: FAILURE");
        }
    }

}
