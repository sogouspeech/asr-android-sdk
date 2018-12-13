// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.speex.speech.sogou.sogocompression.speex;

import android.util.Log;

/**
 * Created by zhouqilin on 16/9/30.
 */

public class SpeexDecoder extends SpeexCodec {
    public SpeexDecoder(){
        setSpeexNative(createDecoder(SPEEX_DEFAULT_BAND_MODE, SPEEX_DEFAULT_QUALITY));
    }

    public int decode(byte[] input, short[] output){
        if (mSpeexNative == 0){
            return -1;
        }
        return decode(mSpeexNative, input, output);
    }

    public short[] decode(byte[] input){
        int outputLen = decodedSizeInSamples(mSpeexNative, input.length);
        if(outputLen <= 0){
            return null;
        }
        short[] result = new short[outputLen];
        decode(input, result);
        return result;
    }

    public void destroy(){
        if (mSpeexNative != 0){
            destroyDecoder(mSpeexNative);
        }
        mSpeexNative = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        if (mSpeexNative != 0){
            Log.i("SpeexDecoder", "!!! SpeexDecoder finalize. Forget to call destroy !!!");
        }
        destroy();
        super.finalize();
    }
}