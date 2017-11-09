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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;;
import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author Viam
 */
public class PacketStructure {
    
    
   /**
    * Packet Structure definition for the command module
    *   
    * Section Header : Version / Packet Section Count / Data block Size / Sequence / FEC Size / Data type / Packet size 
    * Section Data : Data blocks of encoded data
    * Section FEC : FEC bytes
    */ 
    
    public static String version = "1.1.0";
    public byte[] structure;
    public static int packetSize;
    public static int packetMaxSize = 36;
    public static int PacketSectionCount  = 3;
    public static int dataType; // 0 = binary 1 = hex, 2 = ascii, 3 = decimal
    public boolean ack = false;
    
    
    public StringBuilder createPacketStructure(byte[] data, int dataBlockSize, int sequence, int dataType ) throws FYPException{
        if(data.length == 0){
            throw new FYPException("O bytes of data");
        }
        if(data.length > 24){
            throw new FYPException("Larger than allowed data size " + data.length );
        }
        
        StringBuilder  createStructure = new StringBuilder(); // This is probably the worst way to do it lel
        
        createStructure = createStructure.append(version);
        createStructure = createStructure.append(PacketSectionCount);        
        createStructure = createStructure.append(dataBlockSize);    
        createStructure = createStructure.append(sequence);    
        createStructure = createStructure.append(FYPCreateFrequency.ERROR_BITS);
        createStructure = createStructure.append(dataType);
        
        if(createStructure.length() > packetMaxSize){
                throw new FYPException("Packet size exceeds maximum size");
        }
        createStructure = createStructure.append(createStructure.length());
        System.out.println("Packet is : " + createStructure);
        
        return createStructure;
    }
    
    
    
        public void readPackets() throws FYPException, IOException{
            
            
            Path path = Paths.get("path/to/file");
            byte[] data = Files.readAllBytes(path);
            
        }
    
    
    public static void main(String args[]) throws FYPException, EncoderDecoder.DataTooLargeException, ReedSolomonException, UnsupportedEncodingException, LineUnavailableException, InterruptedException{
    
        PacketStructure make = new PacketStructure();
        
        String test = "dffffff55555555d555555dd";
       
         
        FYPCreateFrequency firsttest = new   FYPCreateFrequency();      
        String teststring = firsttest.encodeData(make.createPacketStructure(test.getBytes(),test.getBytes().length,2, 1 ).toString());
        firsttest.generateFrequency(teststring);
        
    }         
   }
    
    
    

