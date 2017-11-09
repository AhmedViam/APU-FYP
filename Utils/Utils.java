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
package githubfyp.Utils;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 *
 * @author Viam
 */
public class Utils {
    
      
      /**
     * Calculate the bit values of the encoded data
     * 
     * @param bytes                       Byte array to be converted to bit String
     * @return output                     Bit String     
     */  
    
    public static String toBitString(final byte[] bytes) {
        final char[] bits = new char[8 * bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            final byte byteval = bytes[i];
            int byteHolder = i << 3;
            int mask = 0x1;
            for (int j = 7; j >= 0; j--) {
                final int bitval = byteval & mask;
                if (bitval == 0) {
                    bits[byteHolder + j] = '0';
                } else {
                    bits[byteHolder + j] = '1';
                }
                mask <<= 1;
            }
        }
        return String.valueOf(bits);
    }
    
        public void printFreqs() {
        /*for (int i = 0; i < settings.SOUND_FRAMES / 4; i++) {
            System.out.println("bin " + i + ", freq: " + (settings.SAMPLE_RATE * i) / settings.SOUND_FRAMES);
        }*/
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { 
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; 
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
     
        public static byte[][] splitByChunk(byte[] array, int chunkSize) {
        int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        byte[][] output = new byte[numOfChunks][];

        for(int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            byte[] temp = new byte[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }

        return output;
    }
    
     
    public static class RangeTest {
        
        int lowerBound;
        int upperBound;
    
         public boolean checkRange(Integer candidate, int lower_bound, int upper_bound) {
            this.lowerBound = lower_bound;
            this.upperBound = upper_bound;
            Range<Integer> range = Ranges.closed(lower_bound,upper_bound);
            return range.contains(candidate);
        }
    }
}
