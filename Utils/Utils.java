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

/**
 *
 * @author Viam
 */
public class Utils {
    
      
    /**
     * Calculate the bit values of the encoded data
     * 
     * @param bytes                 Byte array to be converted to bit String
     * @return output               Bit String     
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
    
    /**
     * Parse and return a String from binary of Hex
     * 
     * @param binary                A string of binary characters
     * @return output               Parsed to Hex     
     */
    
    public static String binaryToHex(String binary) {
        return String.format("%21X", Long.parseLong(bin, 2));
    }

    /**
     * Parse and return a String from String of Hex
     * 
     * @param string                A string
     * @return output               Parsed to Hex     
     */
    
    public String toHex(String string) {
        return String.format("%040x", new BigInteger(1, arg.getBytes()));
    }
    
    
    
    
}
