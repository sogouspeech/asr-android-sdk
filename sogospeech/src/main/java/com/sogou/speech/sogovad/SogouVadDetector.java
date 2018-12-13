// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.sogovad;

import android.util.Log;


public class SogouVadDetector extends BaseVadDetector {
    @Override
    public synchronized int init(Object extra) {
        mValidDataLength = 2048;
        mBosLength = (long) (mBos * mSampleRate.value * mChannelConfig * 2);
        mEosLength = (long) (mEos * mSampleRate.value * mChannelConfig * 2);
        mMaxSpeechLength = mMaxSpeechTime * mSampleRate.value * mChannelConfig * 2;

        instance = init_jni(mSampleRate.value, extra);
        int res = 0;
        if (instance < 0) {
            res = -1;// 失败
        }

        Log.i("instance", "instance: " + instance);

        return res;
    }

    @Override
    public synchronized void detect(short[] voice, int sn, Object extra) {
        if (!checkValidDataLength(voice)) {
//            throw new IllegalArgumentException("valid Data Length must be " + mValidDataLength + " short");
            Log.e("vad", "valid Data Length must be " + mValidDataLength + " short");
            if (null != callback) {
                vadLog(VadTip.ERROR_VAD_WRONG_PARAMETER.msg);
                callback.onCallback(VadTip.ERROR_VAD_WRONG_PARAMETER.code, VadTip.ERROR_VAD_WRONG_PARAMETER.msg, null);
            }
            return;
        }

        if (instance == -1) {
            Log.e("vad", "no vad instance! please init vad.");
            return;
        }

        if (!mIsLongMode) {
            s_detect(voice, sn, extra);
        } else {
            l_detect(voice, sn, extra);
        }
    }

    private void s_detect(short[] voice, int sn, Object extra){
        mCurTotalLength += voice.length * 2;
        //总时间没有超时
        if (mCurTotalLength < (mMaxAudioLength * mSampleRate.value * mChannelConfig * 2)) {
            long[] start_end = new long[]{0, voice.length};//todo
            short validVoice[] = detect_jni(instance, voice, start_end, sn, extra);
            if (validVoice != null) {
                mCurEndLength = 0;
                if (!mIsFoundedValidVoice) {
                    mIsFoundedValidVoice = true;
                    if (null != callback) {
                        vadLog(VadTip.MSG_SPEECH_START.msg);
                        callback.onCallback(VadTip.MSG_SPEECH_START.code, VadTip.MSG_SPEECH_START.msg, null);
                    }
                }

                //检测是否有效声音已经超过了规定长度
                mCurSpeechDataLength += validVoice.length * 2;
                if (mCurSpeechDataLength > mMaxSpeechLength) {
                    if (null != callback) {
                        vadLog(VadTip.ERROR_VAD_SPEECH_TOO_LONG.msg);
                        callback.onCallback(VadTip.ERROR_VAD_SPEECH_TOO_LONG.code, VadTip.ERROR_VAD_SPEECH_TOO_LONG.msg, null);
                    }
                    release(null);
                    return;
                }

                if (null != callback) {
                    vadLog("持续说话中...");
                    callback.onVadProcessed(true, validVoice, sn, start_end[0], start_end[1], null);
                }
                return;
            } else {
                if (!mIsFoundedValidVoice) { //一直没有检测到有效声音
                    if (mCurTotalLength >= mBosLength) {
                        if (null != callback) {
                            vadLog(VadTip.ERROR_VAD_SPEECH_TIMEOUT.msg);
                            callback.onCallback(VadTip.ERROR_VAD_SPEECH_TIMEOUT.code, VadTip.ERROR_VAD_SPEECH_TIMEOUT.msg, null);
                        }
                        release(null);
                        return;
                    } else {
                        if (null != callback) {
                            vadLog("vad 检测中...");
                            callback.onVadProcessed(false, validVoice, sn, start_end[0], start_end[1], null);
                        }
                        return;
                    }
                } else { //已经检测到了有效声音
                    mCurEndLength += voice.length * 2;
//                    LogUtil.e("xq"," mCurEndLength:"+mCurEndLength+" mEosLength:"+mEosLength+" voice.length:"+voice.length*2);
                    if (mCurEndLength <= mEosLength) { //已经检测到了有效声音，且停顿间隔不满足EOS，认为后续还可能说话。
                        if (null != callback) {//todo 这个数据应该返回真还是假，返回voice是否会造成数据丢失
                            vadLog("持续说话中...(已经检测到语音结束，但未达到EOS长度)");
                            callback.onVadProcessed(true, voice, sn, start_end[0], start_end[1], null);
                        }
                        return;
                    } else { //已经检测到了有效声音，且eos时长后停止说话
                        if (null != callback) {
                            vadLog(VadTip.MSG_SPEECH_END.msg);
                            callback.onCallback(VadTip.MSG_SPEECH_END.code, VadTip.MSG_SPEECH_END.msg, null);
                        }
                        release(null);
                        return;
                    }
                }
            }
        } else {
            if (null != callback) {
                vadLog(VadTip.ERROR_AUDIO_TOO_LONG.msg);
                callback.onCallback(VadTip.ERROR_AUDIO_TOO_LONG.code, VadTip.ERROR_AUDIO_TOO_LONG.msg, null);
            }
            release(null);
            return;
        }
    }

    private void l_detect(short[] voice, int sn, Object extra) {

        long[] start_end = new long[]{0, voice.length};
        short[] validVoice = detect_jni(instance, voice, start_end, sn, extra);

        if (callback != null) {
            callback.onVadProcessed((validVoice != null), validVoice, sn, start_end[0], start_end[1], null);
        }
    }

    @Override
    public synchronized void release(Object extra) {
        if (instance > 0) {
            release_jni(instance, extra);
            instance = -1;
        }
    }

    @Override
    public void enableLog(boolean enable){
        mEnableLog = enable;
        enable_debug_log(enable);
    }

    private native long init_jni(long sampleRate, Object extra);

    public native short[] detect_jni(long instance, short[] voice, long[] out_start_end, int sn, Object extra);

    public native void release_jni(long instance, Object extra);

    public native void enable_debug_log(boolean enableLog);

    static {
        System.loadLibrary("vad-lib");
    }
}