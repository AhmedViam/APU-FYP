/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.casualcoding.reedsolomon;

import com.casualcoding.reedsolomon.EncoderDecoder.DataTooLargeException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Silfvro
 */
public class main {
    public static void main(String [] args) throws UnsupportedEncodingException
	{

            EncoderDecoder encoderDecoder = new EncoderDecoder();

try {

String message = new String("EncoderDecoder Example");

byte[] data = message.getBytes();

byte[] encodedData = encoderDecoder.encodeData(data, 5);

System.out.println(String.format("Message: %s", Util.toHex(data))); System.out.println(String.format("Encoded Message: %s", Util.toHex(encodedData)));

encodedData[0] = (byte)(Integer.MAX_VALUE & 0xFF); // Intentionally screw up the first 2 bytes encodedData[1] = (byte)(Integer.MAX_VALUE & 0xFF);

System.out.println(String.format("Flawed Encoded Message: %s", Util.toHex(encodedData)));

byte[] decodedData = encoderDecoder.decodeData(encodedData, 5);

System.out.println(String.format("Decoded/Repaired Message: %s", Util.toHex(decodedData)));



String text = new String(decodedData, 0, data.length, "ASCII");
 
    System.out.println(text);



} catch (DataTooLargeException e) { e.printStackTrace(); } catch (ReedSolomonException e) { e.printStackTrace(); }


            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
        }
}
