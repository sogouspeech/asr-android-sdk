// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.sogovad;

/**
 * vad 相关可设置的参数
 */
public class ConfigurableParameterName {
    /**
     * 语音前端超时检测，如果超过 ASR_VAD_BOS_FLOAT 时间不说话，则返回 ERROR_VAD_SPEECH_TIMEOUT 消息，此后将进入不可再输入状态。
     * 单位：秒
     */
    public static final String ASR_VAD_BOS_FLOAT = "asr.vad.bos";

    /**
     * 语音后端超时检测，如果已经开始说话后，间隔超过 ASR_VAD_EOS_FLOAT 时间不说话，则返回 MSG_SPEECH_END 消息，此后将进入不可再输入状态。
     * 单位：秒
     */
    public static final String ASR_VAD_EOS_FLOAT = "asr.vad.eos";

    /**
     * vad检测到有效声音的最大时长，超过该时长，vad将返回 ERROR_VAD_SPEECH_TIMEOUT 消息，此后将进入不可再输入状态。
     * 单位：秒
     */
    public static final String ASR_VAD_MAX_INTERVAL_INT = "asr.vad.max.voice.interval";

    /**
     * vad可检测的最长音频长度，如果音频长度超过该阈值，则返回ERROR_AUDIO_TOO_LONG消息，此后将进入不可再输入状态。
     * 单位：秒
     */
    public static final String ASR_VAD_MAX_AUDIO_LENGTH_INT = "asr.vad.max.audio.interval";

    /**
     * vad长时模式，开启后可以一直进行输入，vad将过滤掉无效声音/静音，返回有效声音。
     * 此模式下，ASR_VAD_BOS_FLOAT，ASR_VAD_EOS_FLOAT，ASR_VAD_MAX_INTERVAL_FLOAT 失效。
     */
    public static final String ASR_VAD_LONGMODE_BOOLEAN = "asr.vad.longmode";

    /**
     * 是否打印debug日志
     */
    public static final String ASR_VAD_ENABLE_DEBUG_LOG_BOOLEAN = "asr.vad.enablelog";
}