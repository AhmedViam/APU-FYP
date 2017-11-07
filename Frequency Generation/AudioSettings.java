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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import org.apache.commons.math3.transform.FastFourierTransformer;

/**
 *
 * @author Viam
 */
public class AudioSettings {
    
      TargetDataLine microphone;
      AudioFormat format;
      FastFourierTransformer transformer;
      final int numchannels;
      final int SOUND_FRAMES; 
      protected  final int BITS_PER_SECOND;
      protected  final float SAMPLE_RATE;       
      final boolean BIG_ENDIAN;
      final boolean SIGNED;
 
    public AudioSettings(TargetDataLine microphone, AudioFormat format, FastFourierTransformer transformer){    
        
        this.microphone = microphone;
        this.format =  format;
        this.transformer = transformer;
    
        this.numchannels = 1;
        this.SOUND_FRAMES = 8192; 
        this.BITS_PER_SECOND = 16;
        this.SAMPLE_RATE = 200000.0f;       
        this.BIG_ENDIAN = true;
        this.SIGNED = true;
    
    }      
}
