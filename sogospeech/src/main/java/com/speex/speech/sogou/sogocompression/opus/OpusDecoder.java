// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.speex.speech.sogou.sogocompression.opus;

public class OpusDecoder {
    static {
        System.loadLibrary("opus");
    }

    public native boolean nativeInitDecoder(int samplingRate, int numberOfChannels, int frameSize);

    public native int nativeDecodeBytes(byte[] in, short[] out);

    public native boolean nativeReleaseDecoder();


    /**
     * @param sampleRate 采样率
     * @param channels   声道数，单声道为1，双声道为2
     * @param frameSize  每帧音频的采样数，frameSize为固定的某个值
     */
    public void init(int sampleRate, int channels, int frameSize) {
        this.nativeInitDecoder(sampleRate, channels, frameSize);
    }

    /**
     * @param encodedBuffer 被解码的数据
     * @param buffer        解码后输出的数据
     * @return 解码完成的short数据个数
     */
    public int decode(byte[] encodedBuffer, short[] buffer) {
        int decoded = this.nativeDecodeBytes(encodedBuffer, buffer);
        return decoded;
    }

    public void close() {
        this.nativeReleaseDecoder();
    }

}