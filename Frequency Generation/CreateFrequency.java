/*
 * The MIT License
 *
 * Copyright 2017 Viam.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package githubfyp;

import static java.lang.Thread.sleep;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;


import com.casualcoding.reedsolomon.EncoderDecoder;
import com.casualcoding.reedsolomon.ReedSolomonException;
import com.casualcoding.reedsolomon.Util;
import githubfyp.Utils.Utils;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Viam
 */

public class FYPCreateFrequency {

    
       protected static final int  SAMPLE_RATE = 64000;
       protected static final int  ERROR_BITS = 6;
       protected static final int  SIGNAL_TIMER = 26;
   
    
    /**
     * Calculate the Sin wave data for the given frequency
     * 
     * @param  frequency                  The frequency of wave
     * @param  timer                      The number of millisecond to hold
     * @return output                     Byte array of the Sin wave     
     * @throws githubfyp.FYPException     If frequency is out of bounds.
     */ 
    
  public static byte[] calculateSinWave(double frequency, int timer) throws FYPException {
        if (frequency >= 10000 || frequency <= 200)
            throw new FYPException("Frequency is out of bounds");

        int samples = (int)((timer * SAMPLE_RATE) / 1000);
        byte[] output = new byte[samples];
        double period = (double) SAMPLE_RATE / frequency;

        for (int i = 0; i < output.length; i++) {
            double angle = 2.0 * Math.PI * i / period;
            output[i] = (byte)(Math.sin(angle) * 127f);
        }
        return output;
    }
  
      /**
     * Encode the data with Reed Solomon
     * 
     * @param message                     The string of data to be encoded
     * @return output                     Reed Solomon encoded String     
     * @throws com.casualcoding.reedsolomon.EncoderDecoder.DataTooLargeException     
     * @throws com.casualcoding.reedsolomon.ReedSolomonException     
     * @throws java.io.UnsupportedEncodingException     
     * @throws IllegalArgumentException   If frequency is out of bounds.
     */ 
  
  public static String encodeData(String message) throws EncoderDecoder.DataTooLargeException, ReedSolomonException, UnsupportedEncodingException {           
        EncoderDecoder encoderDecoder = new EncoderDecoder();
                
        byte[] data = message.getBytes();
        byte[] encodedData = encoderDecoder.encodeData(data, ERROR_BITS);

        System.out.println(String.format("Message: %s", Util.toHex(data))); 
        System.out.println(String.format("Encoded Message: %s with lengeth %s", Util.toHex(encodedData), encodedData.length) );
           
        return(Utils.toBitString(encodedData));                  
  }
  
      /**
     * Calculate the Sin wave data for the given frequency
     *    
     * @param bitStream                   Bit Stream of data for frequency generation
     * @throws javax.sound.sampled.LineUnavailableException
     * @throws java.lang.InterruptedException
     * @throws githubfyp.FYPException
     * @throws IllegalArgumentException   If frequency is out of bounds.
     */ 
  
    public static void generateFrequency(String bitStream) throws LineUnavailableException, InterruptedException, FYPException {
        final AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        try (SourceDataLine Data = AudioSystem.getSourceDataLine(audioFormat)) {
            
            String frequencyString;
            int bitStringLength = 8;
            int index =0;
            int outPutFrequency = 0;
            ToneMap tonemap = new ToneMap();
           // String g = toBitString(bitStream);
            for (int i = 0; i < (bitStream.length() / 8 ); i++) {       
                frequencyString = bitStream.substring(index,bitStringLength);
                outPutFrequency = tonemap.frequency.get(frequencyString);
                bitStringLength = bitStringLength +8;
                index = index + 8;
               
     /**
      * It's a pretty bad idea to use thread sleep inside a loop like this 
      * but if there is no delay the receiver cannot keep up with the generated tones ¯\_(ツ)_/¯ 
      * There has to be a better way.
      */           
            Data.open(audioFormat, SAMPLE_RATE);
            Data.start();          
               byte[] signalaccumulator;
               
               signalaccumulator = calculateSinWave(9000, SIGNAL_TIMER);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               sleep(5); 
               
               signalaccumulator = calculateSinWave(outPutFrequency, SIGNAL_TIMER);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               sleep(5); 
               
               signalaccumulator = calculateSinWave(9200, SIGNAL_TIMER);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               sleep(5);
               
               Data.drain();             
            }
             
            byte[] signalaccumulator;
            
               signalaccumulator = calculateSinWave(9000, 60);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               sleep(5); 
               
               signalaccumulator = calculateSinWave(9500, 100);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               sleep(5); 
               
               signalaccumulator = calculateSinWave(9200, 100);
               Data.write(signalaccumulator, 0, signalaccumulator.length);
               sleep(5);
               
               Data.drain();            
        }
    }
}
