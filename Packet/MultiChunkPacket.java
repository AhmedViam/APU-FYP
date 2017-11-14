/*
 * The MIT License
 *
 * Copyright 2017 Silfvro.
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
import static githubfyp.FYPCreateFrequency.SAMPLE_RATE;
import static githubfyp.Utils.Utils.chunkArray;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static java.lang.Thread.sleep;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;


/**
 *
 * @author Viam
 */
public class MultiChunkPacket {


    /**
     * Packet Structure definition for the command module
     *
     * Section Header : Version / Packet Section Count / Data block Size / Sequence / FEC Size / Data type / Packet size
     * Section Data : Data blocks of encoded data
     * Section FEC : FEC bytes
     */

    public static String version = "1";
    public byte[] structure;
    public static int packetSize;
    public static int dataSize = 3;
    public static int packetMaxSize = 36;
    public static int numberOfChunks;
    public static int PacketSectionCount  = 3;
    public static int dataType; // 0 = binary 1 = hex, 2 = ascii, 3 = decimal
    public boolean ack = false;


    //Other variables
    byte byteData[];
    double doubleData[];
    AudioSettings settings = new  AudioSettings();
    ToneMetrics metrics = new ToneMetrics();
    boolean startCollection = false;
    static boolean endCollection = false;
    boolean collectDataFrequency = false;
    boolean getPacketSize = false;
    int toneMapIndex;
    static StringBuilder dataString = new StringBuilder();




    public byte[] createPacketStructure(byte[] data, int dataBlockSize, int sequence, int dataType ) throws FYPException {
        if(data.length == 0) {
            throw new FYPException("O bytes of data");
        }
        if(data.length > dataSize) {
            throw new FYPException("Larger than allowed data size " + data.length );
        }

        StringBuilder  createStructure = new StringBuilder(); // This is probably the worst way to do it lel
        /*createStructure = createStructure.append(version);
        createStructure = createStructure.append(PacketSectionCount);
        createStructure = createStructure.append(dataBlockSize);
        createStructure = createStructure.append(sequence);
        createStructure = createStructure.append(FYPCreateFrequency.ERROR_BITS);
        createStructure = createStructure.append(dataType);
        */
        if(createStructure.length() > packetMaxSize) {
            throw new FYPException("Packet size exceeds maximum size");
        }
        //createStructure = createStructure.append(createStructure.length());
        //createStructure = createStructure.append(data);

        //System.out.println("Packet data length is : " + dataBlockSize);
        //System.out.println("Whole Packet  length is : " + createStructure);
        //String test;
//        test = createStructure.substring(13,dataBlockSize);
        //System.out.println("Packet data is  : " + Util.toHex(data));
        //System.out.println("Packet data is  : " + Arrays.toString(createStructure.toString().getBytes()));
        //String finaldata = data;
        //return createStructure.toString().getBytes();
        return data;
    }

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



    public static void readPackets() throws FYPException, IOException, EncoderDecoder.DataTooLargeException, ReedSolomonException, ReedSolomonException, LineUnavailableException, InterruptedException {
        final AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        try (SourceDataLine Data = AudioSystem.getSourceDataLine(audioFormat)) {


            FYPCreateFrequency firsttest = new   FYPCreateFrequency();

            Data.open(audioFormat, SAMPLE_RATE);
            Data.start();
            byte[] signalaccumulator;

            signalaccumulator = firsttest.calculateSinWave(9600, 300);
            Data.write(signalaccumulator, 0, signalaccumulator.length);
            sleep(5);

            signalaccumulator = firsttest.calculateSinWave(9600, 300);
            Data.write(signalaccumulator, 0, signalaccumulator.length);
            sleep(5);

            signalaccumulator = firsttest.calculateSinWave(9000, 300);
            Data.write(signalaccumulator, 0, signalaccumulator.length);
            sleep(5);

            Data.drain();
        }

    }



    public static void assembleData(byte[] data) throws FYPException, EncoderDecoder.DataTooLargeException, ReedSolomonException, ReedSolomonException, LineUnavailableException, InterruptedException, UnsupportedEncodingException {
        byte[][] chunks = chunkArray(data, dataSize);

        System.out.println("Data size is " + data.length);
        for (byte[] chunk : chunks) {
            numberOfChunks++;
            System.out.println("Chunk " + numberOfChunks + " : " + Util.toHex(chunk));
        }
        System.out.println("Number of Chunks : " + numberOfChunks + " With the last chunk being " + chunks[numberOfChunks-1].length + " size");
        numberOfChunks =0;
        PacketStructure makeNewStructure = new PacketStructure();
        FYPCreateFrequency makeNewFrequency = new   FYPCreateFrequency();
        byte[] packet;
        for (byte[] chunk : chunks) {
            numberOfChunks++;
            packet = makeNewStructure.createPacketStructure(chunk, chunk.length, numberOfChunks, 0);
            System.out.println("Chunk length is : " + chunk.length );
            makeNewFrequency.generateFrequency(makeNewFrequency.encodeData(packet));
        }

        final AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        try (SourceDataLine Data = AudioSystem.getSourceDataLine(audioFormat)) {


            FYPCreateFrequency firsttest = new   FYPCreateFrequency();

            Data.open(audioFormat, SAMPLE_RATE);
            Data.start();
            byte[] signalaccumulator;

            signalaccumulator = firsttest.calculateSinWave(9390, 300);
            Data.write(signalaccumulator, 0, signalaccumulator.length);
            sleep(5);


            Data.drain();
        }








    }


    public static void main(String args[]) throws FYPException, EncoderDecoder.DataTooLargeException, ReedSolomonException, UnsupportedEncodingException, LineUnavailableException, InterruptedException, IOException {

        // byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
        //      0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
        //     0x30, 0x30, (byte)0x9d,0x4f, (byte)0xd0,(byte)0xe0, 0x4f, (byte)0xd0,
        //     0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
        //  };
        byte[] data = "123".getBytes();

        assembleData(data);


    }


}




