package ViamFYP;

//import static java.lang.Thread.sleep;

import static ViamFYP.ReadAcousticData.builder;
import com.casualcoding.reedsolomon.EncoderDecoder;
import com.casualcoding.reedsolomon.ReedSolomonException;
import com.casualcoding.reedsolomon.Util;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import static java.lang.Thread.sleep;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.StringUtils;

public class GenerateCarrierSignal {
    //


   protected static final int SAMPLE_RATE = 64000;
 static byte[] fromBinary;

   public static byte[] generateFreq(double freq, int ms) {
       int samples = (int)((ms * SAMPLE_RATE) / 1000);
       byte[] output = new byte[samples];
           //
       double period = (double)SAMPLE_RATE / freq;
       for (int i = 0; i < output.length; i++) {
           double angle = 2.0 * Math.PI * i / period;
           output[i] = (byte)(Math.sin(angle) * 127f);  
       }
        
        

       return output;
   }

   

   
   public static String toBitString(final byte[] b) {
    final char[] bits = new char[8 * b.length];
    for(int i = 0; i < b.length; i++) {
        final byte byteval = b[i];
        int bytei = i << 3;
        int mask = 0x1;
        for(int j = 7; j >= 0; j--) {
            final int bitval = byteval & mask;
            if(bitval == 0) {
                bits[bytei + j] = '0';
            } else {
                bits[bytei + j] = '1';
            }
            mask <<= 1;
        }
    }
    return String.valueOf(bits);
  }
public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
    }
    return data;
}
   
   
 public static void main(String[] args) throws LineUnavailableException, InterruptedException,IOException, EncoderDecoder.DataTooLargeException, ReedSolomonException {
  final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
       try (SourceDataLine Data = AudioSystem.getSourceDataLine(af)) {
           Data.open(af, SAMPLE_RATE);
           Data.start();
           
           
           String value =  "";
           

              EncoderDecoder encoderDecoder = new EncoderDecoder();

try {

String message = new String("Hello my name is Viam and this is a very long big string");

byte[] data = message.getBytes();

byte[] encodedData = encoderDecoder.encodeData(data, 6);

System.out.println(String.format("Message: %s", Util.toHex(data))); 
System.out.println(String.format("Encoded Message: %s", Util.toHex(encodedData)));

System.out.println(encodedData.length);

System.out.println(Arrays.toString(encodedData));




/* Testing block, time to screw up some bytes purposely  */

//encodedData[8] = (byte)(Integer.MAX_VALUE & 0x24); // Purposely screw up the first 2 bytes encodedData[1] = (byte)(Integer.MAX_VALUE & 0xFF);
//encodedData[1] = (byte)(Integer.MAX_VALUE & 0x22);
//encodedData[16] = (byte)(Integer.MAX_VALUE & 0x08);
//encodedData[3] = (byte)(Integer.MAX_VALUE & 0x08);

/*
byte[] filteredByteArray = Arrays.copyOfRange(encodedData,3, encodedData.length);

//System.out.println(Arrays.toString(filteredByteArray));

System.out.println(String.format("Flawed Encoded Message: %s", Util.toHex(encodedData)));

String g = "12f";
// create a destination array that is the size of the two arrays
byte[] destination = new byte[filteredByteArray.length + 3];

// copy ciphertext into start of destination (from pos 0, copy ciphertext.length bytes)
System.arraycopy(g.getBytes(), 0, destination, 0, g.getBytes().length);

// copy mac into end of destination (from pos ciphertext.length, copy mac.length bytes)
System.arraycopy(filteredByteArray, 0, destination, g.getBytes().length, filteredByteArray.length);


System.out.println(Arrays.toString(destination));
*/



byte[] decodedData = encoderDecoder.decodeData(encodedData, 6);

System.out.println(String.format("Decoded/Repaired Message: %s", Util.toHex(decodedData)));
String test;
test =toBitString(encodedData);

String text = new String(decodedData, 0, data.length, "ASCII");
 
    System.out.println(text);

value = test;

} catch (ReedSolomonException e) { e.printStackTrace(); }
           
           
           
           
           
           
           
           
           
           
           
           

           

       
            String hold;
            int cn = 8;
            int j =0;
            int outf;
              ToneMap tm = new ToneMap();
           for (int i = 0; i < (value.length() / 8 ); i++) {
                          
               hold = value.substring(j,cn);
              // System.out.println(hold);
               outf = tm.map.get(hold);
               System.out.println(i);
               cn = cn +8;
               j = j + 8;

        
             //  }
               
               // Calculate the delta then assign the time delay between 2 adjacent sequences / packets
               byte[] signalaccumulator;
               signalaccumulator = generateFreq(9000, 30);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               
               sleep(5); // Make sure the relay is synchronized 8 ms works for now
               signalaccumulator = generateFreq(outf, 30);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               
               //System.out.println(signalaccumulator.length);
               sleep(5); // Make sure the relay is synchronized 8 ms works for now
               
               signalaccumulator = generateFreq(9200, 30);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
                // Make sure the relay is synchronized 8 ms works for now
               sleep(5);
               
            
               Data.drain();
           } 
            byte[] signalaccumulator;
               signalaccumulator = generateFreq(9000, 60);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               
               sleep(5); // Make sure the relay is synchronized 8 ms works for now
               signalaccumulator = generateFreq(9500, 100);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               
               //System.out.println(signalaccumulator.length);
               sleep(5); // Make sure the relay is synchronized 8 ms works for now
               
               signalaccumulator = generateFreq(9200, 60);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
                // Make sure the relay is synchronized 8 ms works for now
               sleep(5);
               
            
               Data.drain();
       }
 }
}

 
 




