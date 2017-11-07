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

import com.casualcoding.reedsolomon.EncoderDecoder;
import com.casualcoding.reedsolomon.ReedSolomonException;
import com.casualcoding.reedsolomon.Util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.sound.sampled.*;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.*;
import org.apache.commons.math3.complex.Complex;
import githubfyp.Utils.Utils;




public class ReadAcousticData {

    AudioSettings settings = new  AudioSettings();
    ToneMetrics metrics = new ToneMetrics();
    protected static final float  LOWER_LIMIT = 200;
    protected static final float  UPPER_LIMIT = 10000;
    
    byte byteData[]; 
    double doubleData[]; 
    
    static boolean startCollection = false;
    boolean collectDataFrequency = false;
    double dataCarrierFrequency = 0;
    static StringBuilder dataString = new StringBuilder();
    int toneMapIndex;
    String precesionFrequency;
    String input = "0100100001100101011011000110110001101111001000000111011101101111011100100110110001100100111000001010011000110110111000101110011101110101";


    public ReadAcousticData() {    
        byteData = new byte[settings.SOUND_FRAMES];  
        doubleData = new double[settings.SOUND_FRAMES]; 
        settings.transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        settings.format = new AudioFormat(settings.SAMPLE_RATE, settings.BITS_PER_SECOND, settings.numchannels, settings.SIGNED, settings.BIG_ENDIAN);
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, settings.format);  

        if (!AudioSystem.isLineSupported(info)) {
            System.err.print("isLineSupported failed");
            System.exit(1);
        }
        try {
            settings.microphone = (TargetDataLine) AudioSystem.getLine(info);
            settings.microphone.open(settings.format);
            System.out.print("ViamFYP Sound CMProtocol version 1.1.7: " + settings.format.toString() + "\n");
            settings.microphone.start();
        } catch (LineUnavailableException ex) {
            
        System.out.println("Microphone failed: " + ex.getMessage());
        System.exit(1);
        }
    }

    
    public int readBytes() {
        int numberOfBytes = settings.microphone.read(byteData, 0, byteData.length);
        if (numberOfBytes != byteData.length) {
        System.out.println("Warning: Number of bytes read is less than buffer size");
        System.exit(1);      
        }
        return numberOfBytes;
    }


    public void byteToDouble() {
        ByteBuffer buf = ByteBuffer.wrap(byteData);
        buf.order(ByteOrder.BIG_ENDIAN);
        int index = 0;
        
        while (buf.remaining() > 2) {
            short s = buf.getShort();
            doubleData[index] = (new Short(s)).doubleValue();
            ++index;
        }
    }
    

    private static String findNearest(Map < Integer, String > map, int value) {
        Map.Entry < Integer, String > previousEntry = null;
        for (Map.Entry < Integer, String > e: map.entrySet()) {
            if (e.getKey().compareTo(value) >= 0) {
                if (previousEntry == null) {
                    return e.getValue();
                } else {
                    if (e.getKey() - value >= value - previousEntry.getKey()) {
                        return previousEntry.getValue();
                    } else {
                        return e.getValue();
                    }
                }
            }
             previousEntry = e;
            }
        return previousEntry.getValue();
    }





    /**
     *
     * @param bin
     * @return
     * @throws java.io.IOException
     * @throws com.casualcoding.reedsolomon.EncoderDecoder.DataTooLargeException
     * @throws com.casualcoding.reedsolomon.ReedSolomonException
     * @throws githubfyp.FYPException
     */


public void identifyFrequency() throws IOException, EncoderDecoder.DataTooLargeException, ReedSolomonException, FYPException {

    double frequency;
    Complex[] complex = settings.transformer.transform(doubleData, TransformType.FORWARD);
    double real;
    double imaginary;
    double magnitude[] = new double[complex.length];

    for (int i = 0; i < complex.length; i++) {
        real = complex[i].getReal();
        imaginary = complex[i].getImaginary();
        magnitude[i] = Math.sqrt((real * real) + (imaginary * imaginary));
    }
    double peak = -1.0;
    int index = -1;
    for (int i = 0; i < complex.length; i++) {
        if (peak < magnitude[i]) {
            index = i;
            peak = magnitude[i];
        }
    }
  frequency = (settings.SAMPLE_RATE * index) / settings.SOUND_FRAMES;
  ToneMap tm = new ToneMap();
  Utils.RangeTest rangeTest = new Utils.RangeTest() ;
  
    if (rangeTest.checkRange((int)frequency, (int)LOWER_LIMIT, (int)UPPER_LIMIT)) {
        if (rangeTest.checkRange((int)frequency, (int)metrics.START_METRIC - (int)metrics.LOWER_PRECESION_METRIC, (int)metrics.START_METRIC + (int)metrics.LOWER_PRECESION_METRIC)) {
            System.out.println("Received start padding: telemetry 9000 hz");
            startCollection = true;
            collectDataFrequency = true;
        }

        if (startCollection == true && rangeTest.checkRange((int)frequency, (int)LOWER_LIMIT, (int)metrics.START_METRIC - (int)metrics.LOWER_PRECESION_METRIC) == true && collectDataFrequency == true) {
            dataCarrierFrequency = frequency;
            toneMapIndex = (int) Math.round(dataCarrierFrequency);
            precesionFrequency = findNearest(tm.bitString, toneMapIndex);

            System.out.println(frequency);
            System.out.println(precesionFrequency);
            collectDataFrequency = false;
            dataString.append(precesionFrequency);
        }

        if (rangeTest.checkRange((int)frequency, 9150, 9250)){
            System.out.println("Received end padding: telemetry 9200 hz");
            System.out.println(dataString + "\n");
            System.out.print("ViamFYP Sound CMProtocol version 1.2.6\nRS integrity check:correct ");
            System.out.print("Decoded data chunk: ");

            Arrays.stream(dataString.toString().split("(?<=\\G.{8})")).forEach(s -> System.out.print((char) Integer.parseInt(s, 2)));
            System.out.print('\n');
            printSimilarity(dataString.toString(), input);
            startCollection = false;
        }

        if (rangeTest.checkRange((int)frequency, 9400, 9550)) {
            EncoderDecoder encoderDecoder = new EncoderDecoder();
            
            BigInteger c = new BigInteger(dataString.toString(), 2);

            String result = c.toString(16);

            result = result.replace("ff", "");
            System.out.println(c.toString(16));
            BigInteger b = new BigInteger(result, 16);

            System.out.println("added is " + result);



            System.out.println(Arrays.toString(result.getBytes()));

            byte[] decodedData = encoderDecoder.decodeData(b.toByteArray(), 6);


            System.out.println(Arrays.toString(dataString.toString().getBytes()));
            System.out.println(String.format("Decoded/Repaired Message: %s", Util.toHex(decodedData)));

            String text = new String(decodedData, 0, decodedData.length, "ASCII");
            System.out.println(text);
        }
    }
}

    public void printFreqs() {
        for (int i = 0; i < settings.SOUND_FRAMES / 4; i++) {
            System.out.println("bin " + i + ", freq: " + (settings.SAMPLE_RATE * i) / settings.SOUND_FRAMES);
        }
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }


public static int editDistance(String s1, String s2) {
  s1 = s1.toLowerCase();
  s2 = s2.toLowerCase();

  int[] costs = new int[s2.length() + 1];
  for (int i = 0; i <= s1.length(); i++) {
   int lastValue = i;
   for (int j = 0; j <= s2.length(); j++) {
    if (i == 0)
     costs[j] = j;
    else {
     if (j > 0) {
      int newValue = costs[j - 1];
      if (s1.charAt(i - 1) != s2.charAt(j - 1))
       newValue = Math.min(Math.min(newValue, lastValue),
        costs[j]) + 1;
      costs[j - 1] = lastValue;
      lastValue = newValue;
     }
    }
   }
   if (i > 0)
    costs[s2.length()] = lastValue;
  }
  return costs[s2.length()];
 }

 public static void printSimilarity(String s, String t) {
  // System.out.println(String.format(
  //"%.3f is the percentage accuracy between \"%s\" (data sent) and \"%s\" (data received)", similarity(s, t) * 100, s, t));


  System.out.println(String.format(
   "%.3f is the percentage accuracy between (data sent) (data received)", similarity(s, t) * 100));


 }
 private static char convert(String bs) {
  return (char) Integer.parseInt(bs, 2);
 }
 public static void main(String[] args) throws IOException, FileNotFoundException, InterruptedException, EncoderDecoder.DataTooLargeException, ReedSolomonException, FYPException {
  ReadAcousticData ai = new ReadAcousticData();


  // Do whatever you want

  while (System.in.available() == 0) {
   ai.readBytes();


   ai.byteToDouble();
   ai.identifyFrequency();
  }





  String yu = dataString.toString();

  StringBuilder b = new StringBuilder();
  int len = yu.length();
  int i = 0;
  while (i + 8 <= len) {
   char c = convert(yu.substring(i, i + 8));
   i += 8;
   b.append(c);
  }
  System.out.println(b.toString());
 }






}
