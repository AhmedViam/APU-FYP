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





public class ReadAcousticData {

 TargetDataLine microphone;
 static String h;
 final int audioFrames = 8192; //power ^ 2  
 public float freq;
 final float sampleRate = 200000.0f;
 final int bitsPerRecord = 16;
 final int channels = 1;
 final boolean bigEndian = true;
 final boolean signed = true;
 static double tracefreq;
 public static final int DATA_SHARDS = 4;
 public static final int PARITY_SHARDS = 2;
 public static final int TOTAL_SHARDS = 6;

 public static final int BYTES_IN_INT = 4;
 byte byteData[]; // length=audioFrames * 2  
 double doubleData[]; // length=audioFrames only reals needed for apache lib.  
 AudioFormat format;
 FastFourierTransformer transformer;
 int st = 0;
 public ReadAcousticData() {


















  byteData = new byte[audioFrames * 1]; //two bytes per audio frame, 16 bits  

  //doubleData= new double[audioFrames * 2];  // real & imaginary  
  doubleData = new double[audioFrames]; // only real for apache  

  transformer = new FastFourierTransformer(DftNormalization.STANDARD);

  System.out.print("Acoustic data capture active;\n");
  format = new AudioFormat(sampleRate, bitsPerRecord, channels, signed, bigEndian);
  DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object  

  if (!AudioSystem.isLineSupported(info)) {
   System.err.print("isLineSupported failed");
   System.exit(1);
  }

  try {
   microphone = (TargetDataLine) AudioSystem.getLine(info);
   microphone.open(format);
   System.out.print("ViamFYP Sound CMProtocol version 1.1.7: " + format.toString() + "\n");
   microphone.start();
  } catch (Exception ex) {
   System.out.println("Microphone failed: " + ex.getMessage());
   System.exit(1);
  }

 }




 public int readPcm() {
  int numBytesRead =
   microphone.read(byteData, 0, byteData.length);
  if (numBytesRead != byteData.length) {
   System.out.println("Warning: read less bytes than buffer size");
   System.exit(1);
  }
  return numBytesRead;
 }


 public void byteToDouble() {
  ByteBuffer buf = ByteBuffer.wrap(byteData);
  buf.order(ByteOrder.BIG_ENDIAN);
  int i = 0;

  while (buf.remaining() > 2) {
   short s = buf.getShort();
   doubleData[i] = (new Short(s)).doubleValue();
   ++i;
  }
  //System.out.println("Parsed "+i+" doubles from "+byteData.length+" bytes");  
 }
 int count = 0;
 double prevfrequency = 0;
 boolean found = false;
 int f;
 double newfreq = 0;
 double finalfreq;
 int d = 0;
 int dats = 0;
 int k = 0;
 static boolean start = false;
 double datafreq = 0;
 static StringBuilder builder = new StringBuilder();
 int charCode;
 private final String setBoldText = "\033[0;1m";
 int aj;
 double above;
 double below;
 boolean reed = false;
 static int cccount = 0;
 static int
 let = 0;
 boolean uh = true;
 String input = "0100100001100101011011000110110001101111001000000111011101101111011100100110110001100100111000001010011000110110111000101110011101110101";



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



















 boolean build = false;

 String j;

 public static String binaryToHex(String bin) {
  return String.format("%21X", Long.parseLong(bin, 2));
 }

 public String toHex(String arg) {
  return String.format("%040x", new BigInteger(1, arg.getBytes( /*YOUR_CHARSET?*/ )));
 }


 public void findFrequency() throws FileNotFoundException, IOException, InterruptedException, EncoderDecoder.DataTooLargeException, ReedSolomonException {

  double frequency;
  Complex[] cmplx = transformer.transform(doubleData, TransformType.FORWARD);
  double real;
  double im;
  double mag[] = new double[cmplx.length];

  for (int i = 0; i < cmplx.length; i++) {
   real = cmplx[i].getReal();
   im = cmplx[i].getImaginary();
   mag[i] = Math.sqrt((real * real) + (im * im));
  }

  double peak = -1.0;
  int index = -1;
  for (int i = 0; i < cmplx.length; i++) {
   if (peak < mag[i]) {
    index = i;
    peak = mag[i];
   }
  }
  frequency = (sampleRate * index) / audioFrames;

  //System.out.println(frequency);
  ToneMap tm = new ToneMap();
  String bitString;

  if (frequency >= 150 && frequency <= 10000) {
   if (frequency >= 8950 && frequency <= 9050) {
    System.out.println("Received start padding: telemetry 9000 hz");

    start = true;
    build = true;
   }

   if (start == true && frequency <= 9000 && build == true) {

    datafreq = frequency;


    aj = (int) Math.round(datafreq);

    j = findNearest(tm.bitString, aj);

    System.out.println(frequency);
    System.out.println(j);


    build = false;

    builder.append(j);

   }


   if (frequency >= 9150 && frequency <= 9250) {


    //   System.out.println(frequency);
    System.out.println("Received end padding: telemetry 9200 hz");
    System.out.println(builder + "\n");
    System.out.print("ViamFYP Sound CMProtocol version 1.2.6\nRS integrity check:correct ");
    System.out.print("Decoded data chunk: ");

    Arrays.stream(builder.toString().split("(?<=\\G.{8})")).forEach(s -> System.out.print((char) Integer.parseInt(s, 2)));
    System.out.print('\n');
    printSimilarity(builder.toString(), input);
    start = false;



   }

   if (frequency >= 9400 && frequency <= 9550) {
    EncoderDecoder encoderDecoder = new EncoderDecoder();
    /*
    h = builder.toString();
     if(builder.length() > 136){
         System.out.println("String is too big");
    String str = builder.toString();
    System.out.print(builder.length()+ "\n");
         String upToNCharacters = str.substring(0, Math.min(str.length(), 136));
         System.out.print(upToNCharacters + "\n");
     }


    if(builder.length() < 136){
        System.out.println("String is too small");
        System.out.print(builder.length() + "\n");
        while(builder.length() < 136){
            builder.append("00000000");
        }
        h = builder.toString();
    }
    */

    BigInteger c = new BigInteger(builder.toString(), 2);

    String result = c.toString(16);

    //result = CharMatcher.anyOf("ff").removeFrom(b.toString(16));
    result = result.replace("ff", "");
    System.out.println(c.toString(16));


    BigInteger b = new BigInteger(result, 16);

    /*
    while(result.length() < 62){
        result = result.concat("d");
    }
    */
    System.out.println("added is " + result);
    //System.out.println(String.format("Decoded/Repaired Message: %s", Util.toHex(b.toByteArray())));

    /*
    for(int i = 0;i <17;i++) {
        if(holderh[i] ==-1){
            newholder[i] = holderh[i+1];
        }else{
            newholder[i] = holderh[i];
        }
    }*/
    /*
    for(int i = 0;i <17;i++) {
        if(holderh[i] ==(byte)0xff){
            newholder[i] = holderh[i+1];
        }else{
            newholder[i] = holderh[i];
        }
    }
    System.out.println("The new holder array is ");
    System.out.println(Arrays.toString(newholder));
    */

















    byte[] deco = result.getBytes();

    System.out.println(Arrays.toString(result.getBytes()));

    byte[] decodedData = encoderDecoder.decodeData(b.toByteArray(), 6);




    System.out.println(Arrays.toString(builder.toString().getBytes()));


    System.out.println(String.format("Decoded/Repaired Message: %s", Util.toHex(decodedData)));



    String text = new String(decodedData, 0, decodedData.length, "ASCII");

    System.out.println(text);

   }



  }







 }
 public void printFreqs() {
  for (int i = 0; i < audioFrames / 4; i++) {
   System.out.println("bin " + i + ", freq: " + (sampleRate * i) / audioFrames);
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
  /* // If you have StringUtils, you can use it to calculate the edit distance:
  return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                                                       (double) longerLength; */
  return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

 }

 // Example implementation of the Levenshtein Edit Distance
 // See http://r...content-available-to-author-only...e.org/wiki/Levenshtein_distance#Java
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
 public static void main(String[] args) throws IOException, FileNotFoundException, InterruptedException, EncoderDecoder.DataTooLargeException, ReedSolomonException {
  ReadAcousticData ai = new ReadAcousticData();


  // Do whatever you want

  while (System.in.available() == 0) {
   ai.readPcm();


   ai.byteToDouble();
   ai.findFrequency();
  }





  String yu = builder.toString();

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
