// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.sogovad;


import android.text.TextUtils;
import android.util.Log;

abstract class BaseVadDetector implements IVadDetector {
    protected volatile long instance = -1;

    protected VadDetectorCallback callback;

    /**
     * 是否是长时VAD模式
     */
    protected boolean mIsLongMode = false;
    /**
     * BOS
     */
    protected float mBos = 3;
    protected long mBosLength;

    /**
     * EOS
     */
    protected float mEos = 1;
    protected long mEosLength;
    protected volatile long mCurEndLength;

    /**
     * 最大有效语音时长
     */
    protected int mMaxSpeechTime = 30;
    protected long mMaxSpeechLength;

    /**
     * 最大录音时长
     */
    protected int mMaxAudioLength = 60;


    /**
     * vad侦测过的数据总长度
     **/
    protected volatile long mCurTotalLength;

    /**
     * vad已经检测到的有效数据总长度
     **/
    protected volatile long mCurSpeechDataLength;

    /**
     *vad的输入长度规范
     */
    protected long mValidDataLength;

    /**
     *是否检测到有效声音
     */
    protected boolean mIsFoundedValidVoice = false;

    protected int mChannelConfig = 1;
    protected SAMPLE_RATE mSampleRate = SAMPLE_RATE.SAMPLE_RATE_16k;
    protected VAD_ENCODE_FORMAT mEncodeFormat = VAD_ENCODE_FORMAT.ENCODING_PCM_16BIT;

    protected boolean mEnableLog = true;

    @Override
    public String getVersion(){
        return null;
    }

    @Override
    public String getDescription(){
        return null;
    }

    @Override
    public IVadDetector registCallback(VadDetectorCallback callback) {
        if (callback != null) {
            this.callback = callback;
        }
        return this;
    }

    @Override
    public IVadDetector setParameter(String parameterName, Object parameterValue) {
        if (!TextUtils.isEmpty(parameterName)) {
            switch (parameterName) {
                case ConfigurableParameterName.ASR_VAD_LONGMODE_BOOLEAN:
                    mIsLongMode = (boolean) parameterValue;
                    break;
                case ConfigurableParameterName.ASR_VAD_EOS_FLOAT:
                    mEos = (float) parameterValue;
                    mEosLength = (long) (mEos * mSampleRate.value * mChannelConfig * 2);
                    break;
                case ConfigurableParameterName.ASR_VAD_BOS_FLOAT:
                    mBos = (float) parameterValue;
                    mBosLength = (long) (mBos * mSampleRate.value * mChannelConfig * 2);
                    break;
                case ConfigurableParameterName.ASR_VAD_MAX_INTERVAL_INT:
                    mMaxSpeechTime = (int) parameterValue;
                    mMaxSpeechLength = mMaxSpeechTime * mSampleRate.value * mChannelConfig * 2;
                    break;
                case ConfigurableParameterName.ASR_VAD_MAX_AUDIO_LENGTH_INT:
                    mMaxAudioLength = (int) parameterValue;
                    break;
                case ConfigurableParameterName.ASR_VAD_ENABLE_DEBUG_LOG_BOOLEAN: {
                    mEnableLog = (boolean) parameterValue;
                    enableLog(mEnableLog);
                }
                    break;
                default:
                    break;
            }
        }
        return this;
    }

    protected boolean checkValidDataLength(short[] voice) {
        return (voice != null && voice.length == mValidDataLength);
    }

    @Override
    public synchronized void reset(Object extra) {
        if (instance > 0) {
            mIsFoundedValidVoice = false;
            mCurEndLength = 0;
            mCurTotalLength = 0;
            mCurSpeechDataLength = 0;
        }
    }

    @Override
    abstract public int init(Object extra);

    @Override
    abstract public void detect(short[]voice, int sn, Object extra);

    @Override
    abstract public void release(Object extra);

    abstract public void enableLog(boolean enable);


    protected static enum SAMPLE_RATE {
        /**
         * 采样率，目前只支持16000
         */
        SAMPLE_RATE_8k(8000), SAMPLE_RATE_16k(16000), SAMPLE_RATE_32k(32000), SAMPLE_RATE_48k(48000);
        public int value;

        SAMPLE_RATE(int value) {
            this.value = value;
        }
    }


    protected static enum VAD_ENCODE_FORMAT {
        /**
         * 位宽，目前只支持16bit
         */
        ENCODING_PCM_16BIT(2), ENCODING_PCM_8BIT(1);
        public int value;

        VAD_ENCODE_FORMAT(int value) {
            this.value = value;
        }
    }


    protected void vadLog(String msg){
        if (mEnableLog){
            Log.d("SogoVad Log",msg);
        }
    }

}