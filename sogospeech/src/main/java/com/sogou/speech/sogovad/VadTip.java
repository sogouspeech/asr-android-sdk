// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.sogovad;

/**
 * 回调信息，包含错误信息和状态信息
 * vad 和 agc 相关预处理
 * 错误信息以3000开始，agc以3100区分，vad以3200区分，3300以后为预留
 * 提示/状态信息以30000开始 32000为vad。
 */
public enum VadTip {
    /**
     * 回调信息
     **/
    // agc 处理过程中出错
    ERROR_AGC_PROCESS(3000, "AGC process error"),
    // agc 初始化失败
    ERROR_AGC_INIT_FAILED(3001, "AGC init failed"),

    //输入数据不符合要求
    ERROR_VAD_WRONG_PARAMETER(3200,"wrong parameter"),
    //超过了最大等待时间，未检测到有效声音
    ERROR_VAD_SPEECH_TIMEOUT(3201,"over time, speech not found"),
    //语音数据太长
    ERROR_VAD_SPEECH_TOO_LONG(3202, "speech too long"),
    //音频长度太短，且未检测到有效声音。
    ERROR_AUDIO_TOO_SHORT(3203, "audio too short"),
    //超过了可传入音频的最大时长
    ERROR_AUDIO_TOO_LONG(3204, "audio too long"),


    //检测到说话开始
    MSG_SPEECH_START(32001,"speech start"),
    //检测到说话结束
    MSG_SPEECH_END(32002,"speech end");  //结束态



    public int code;
    public String msg;

    VadTip(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}