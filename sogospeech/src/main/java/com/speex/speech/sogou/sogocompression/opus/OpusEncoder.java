// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.speex.speech.sogou.sogocompression.opus;

public class OpusEncoder {
    static {
        System.loadLibrary("opus");
    }

    public native boolean nativeInitEncoder(int samplingRate, int numberOfChannels, int frameSize);

    public native int nativeEncodeBytes(short[] in, byte[] out);

    public native boolean nativeReleaseEncoder();

    /**
     * @param sampleRate 采样率
     * @param channels   声道数，单声道为1，双声道为2
     * @param frameSize  每帧音频的采样数，frameSize为固定的某个值
     */
    public void init(int sampleRate, int channels, int frameSize) {
        this.nativeInitEncoder(sampleRate, channels, frameSize);
    }

    /**
     * @param buffer 被编码的数据
     * @param out    编码完成后输出的数据
     * @return 编码完成的数据字节个数
     */
    public int encode(short[] buffer, byte[] out) {
        int encoded = this.nativeEncodeBytes(buffer, out);

        return encoded;
    }

    public void close() {
        this.nativeReleaseEncoder();
    }

}