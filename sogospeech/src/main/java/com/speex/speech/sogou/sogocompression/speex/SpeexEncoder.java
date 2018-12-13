// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.speex.speech.sogou.sogocompression.speex;

import android.util.Log;

/**
 * Created by zhouqilin on 16/9/30.
 */

public class SpeexEncoder extends SpeexCodec {
    public SpeexEncoder(){
        setSpeexNative(createEncoder(SPEEX_DEFAULT_BAND_MODE, SPEEX_DEFAULT_QUALITY));
    }

    public int encode(short[] input, byte[] output){
        if (mSpeexNative == 0){
            return -1;
        }
        return encode(mSpeexNative, input, output);
    }

    public byte[] encode(short[] input){
        int outputLen = encodedSizeInBytes(mSpeexNative, input.length);
        if(outputLen <= 0){
            return null;
        }
        byte[] result = new byte[outputLen];
        encode(input, result);
        return result;
    }

    public void destroy(){
        if (mSpeexNative != 0){
            destroyEncoder(mSpeexNative);
        }
        mSpeexNative = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        if (mSpeexNative != 0){
            Log.i("SpeexEncoder", "!!! SpeexEncoder finalize. Forget to call destroy !!!");
        }
        destroy();
        super.finalize();
    }
}