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



/**
 * @author Viam
 * Class to capture frequencies, decode and re-construct data
 */

public class ReadAcousticData {

    AudioSettings settings = new  AudioSettings();
    ToneMetrics metrics = new ToneMetrics();
    protected static final float  LOWER_LIMIT = 200;
    protected static final float  UPPER_LIMIT = 10000;
    
    byte byteData[]; 
    double doubleData[]; 
    static StringBuilder packetsize = new StringBuilder();
    
    
    boolean startCollection = false;
    static boolean endCollection = false;
    boolean collectDataFrequency = false;
    boolean getPacketSize = false;
    static StringBuilder dataString = new StringBuilder();
    int toneMapIndex;
    String translatedBytes;

    /**
     * All data sources and few settings are initialized in the constructor
     *
     */ 
    
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

    /**
     * Read bytes of a set length from the data source, in this case the microphone
     * @return number of bytes read
     */
    
    public int readBytes() {
        int numberOfBytes = settings.microphone.read(byteData, 0, byteData.length);
        if (numberOfBytes != byteData.length) {
        System.out.println("Warning: Number of bytes read is less than buffer size");
        System.exit(1);      
        }
        return numberOfBytes;
    }

    /**
     * Convert byte buffer from byte to Double
     */

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
    
    /**
     * Find the nearest frequency match from the tone map
     * Given a frequency say 7789, it checks the tone map for the closet frequency to 7789
     * This eliminates the discrepancies of receiving a slightly inaccurate but correct frequency.
     */

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
     * Identify the Specific frequencies of use such as start / data / stop / ending
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
    ToneMap tonemap = new ToneMap();
    Utils.RangeTest rangeTest = new Utils.RangeTest() ;
  
        if (rangeTest.checkRange((int)frequency, (int)LOWER_LIMIT, (int)UPPER_LIMIT ) && endCollection == false) {
            if (rangeTest.checkRange((int)frequency, (int)metrics.START_METRIC - (int)metrics.LOWER_PRECESION_METRIC, (int)metrics.START_METRIC + (int)metrics.LOWER_PRECESION_METRIC) && endCollection == false) {
                System.out.println("Received START_METRIC: " +metrics.START_METRIC );
                startCollection = true;
                collectDataFrequency = true;  
            }
        
            if (startCollection == true && rangeTest.checkRange((int)frequency, (int)LOWER_LIMIT, (int)metrics.START_METRIC - (int)metrics.LOWER_PRECESION_METRIC) == true && collectDataFrequency == true && endCollection == false) {
                metrics.DATA_METRIC = frequency;
                toneMapIndex = (int) Math.round( metrics.DATA_METRIC);
                translatedBytes = findNearest(tonemap.bitString, toneMapIndex);
                
                System.out.println("Received DATA_METRIC: " + metrics.DATA_METRIC + " " +  metrics.DATA_METRIC  + translatedBytes  );
                System.out.println("Decoded DATA_METRIC: " + translatedBytes);
                collectDataFrequency = false;
                dataString.append(translatedBytes);
            }
        
            if (rangeTest.checkRange((int)frequency, (int)metrics.END_METRIC - (int)metrics.LOWER_PRECESION_METRIC, (int)metrics.END_METRIC + (int)metrics.LOWER_PRECESION_METRIC) && endCollection == false){
                System.out.println("Received END_METRIC: " + metrics.END_METRIC );
                startCollection = false;
            }
        
            if (rangeTest.checkRange((int)frequency, (int)metrics.STOP_METRIC - (int)metrics.LOWER_PRECESION_METRIC, (int)metrics.STOP_METRIC + (int)metrics.LOWER_PRECESION_METRIC) && endCollection == false) {
                endCollection = true;
                EncoderDecoder encoderDecoder = new EncoderDecoder();
            
                BigInteger c = new BigInteger(dataString.toString(), 2);

                String result = c.toString(16);
                System.out.println(result);
                result = result.replace("ff", "");
                BigInteger b = new BigInteger(result, 16);           
                
                byte[] decodedData = encoderDecoder.decodeData(b.toByteArray(), 6);
                System.out.println(String.format("Decoded/Repaired Message: %s", Util.toHex(decodedData)));
           
                byte[] finalholder = Arrays.copyOfRange(decodedData, 2, decodedData.length);          
                String FinalText = new String(finalholder, 0, finalholder.length, "ASCII");
                System.out.println("Re-constructed data: " + FinalText);
                System.exit(0);            
            }
        }
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, InterruptedException, EncoderDecoder.DataTooLargeException, ReedSolomonException, FYPException, LineUnavailableException {
  
        
        ReadAcousticData data = new ReadAcousticData();

        while (System.in.available() == 0) {
            
        data.readBytes();
        data.byteToDouble();
        data.identifyFrequency();
        
        } 
    }
}
