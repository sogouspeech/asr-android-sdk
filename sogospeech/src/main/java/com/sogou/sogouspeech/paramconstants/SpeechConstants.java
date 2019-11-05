// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech.paramconstants;

public class SpeechConstants {

    public static final String CommonTag = "SogoSpeech";
    public static final int LENGTH_200MS_SHORT = 3200;
    public static final int LENGTH_300MS_SHORT = 4800;
    public static final int LENGTH_500MS_SHORT = 8000;

    public static final String ASR_MODEL_SEARCH = "search";
    public static final String ASR_MODEL_DEFAULT = "default";

    /**
     * 错误回调的错误域，错误域将所有错误做分类，精确到功能模块。
     * 命名规则，ERR + 主功能 + 模块 。
     * 由 void onError(String errorDomain, int errorCode, String errorDescription, Object extra) 返回
     */
    public interface ErrorDomain{
        /**
         * 在线语音识别
         *
         * ERR_ASR_ONLINE_PREPROCESS：预处理包括vad,agc等出错
         * ERR_ASR_ONLINE_NETWORK：网络请求相关错误
         * ERR_ASR_ONLINE_SERVER：语音识别服务器出错
         * ERR_ASR_ONLINE_AUDIO_ENCODE：音频编码错误
         * ERR_ASR_ONLINE_ENGINE_LOGIC: 识别业务逻辑错误
         *
         */
        String ERR_ASR_ONLINE_AUTHENTICATION = "error.asr.online.auth";
        String ERR_ASR_ONLINE_PREPROCESS = "error.asr.online.preprocess";
        String ERR_ASR_ONLINE_NETWORK = "error.asr.online.network";
        String ERR_ASR_ONLINE_SERVER = "error.asr.online.server";
        String ERR_ASR_ONLINE_AUDIO_CODING = "error.asr.online.audio.encode";
        String ERR_ASR_ONLINE_ENGINE_LOGIC = "error.asr.online.engine.logic";


        /**
         * 唤醒功能错误码
         * ERR_WAKEUP_COPY_CONFIG:拷贝配置文件出错
         * ERR_WAKEUP_INVALID_KEYWORD_PATH：唤醒词路径不对
         * ERR_WAKEUP_COPY_KEYWORD：拷贝唤醒词文件出错
         * ERR_WAKEUP_SET_CONFIG_PATH：设置唤醒配置文件路径失败
         * ERR_WAKEUP_SET_MODEL_PATH：设置唤醒模型生成路径失败
         * ERR_WAKEUP_BUILD_MODEL：唤醒建模失败
         * ERR_WAKEUP_INIT_KWS：唤醒初始化失败
         */
        String ERR_WAKEUP_COPY_CONFIG = "error.wakeup.copyconfig";
        String ERR_WAKEUP_INVALID_KEYWORD_PATH = "error.wakeup.invalid.keywordpath";
        String ERR_WAKEUP_COPY_KEYWORD = "error.wakeup.copy.keyword";
        String ERR_WAKEUP_SET_CONFIG_PATH = "error.wakeup.setconfigpath";
        String ERR_WAKEUP_SET_MODEL_PATH = "error.wakeup.setmodelpath";
        String ERR_WAKEUP_BUILD_MODEL = "error.wakeup.build.model";
        String ERR_WAKEUP_INIT_KWS = "error.wakeup.init.kws";
        String ERR_WAKEUP_NOT_INIT = "error.wakeup.init.not";


        /**
         * 其他功能
         * ...
         */
    }

    /**
     * 回调信息
     * 命名规则，MSG + 主功能 + 具体信息
     * 由 void onEvent(String eventName, String param, byte[] data, int offset, int length) 返回
     */
    public interface Message{
        /**
         * 在线语音识别
         *
         * MSG_ASR_ONLINE_PART_RESULT: 语音识别中间结果
         * MSG_ASR_ONLINE_LAST_RESULT: 语音识别最终结果
         * MSG_ASR_ONLINE_SPEECH_START: 检测到语音开始(说话声)
         * MSG_ASR_ONLINE_SPEECH_END: 检测到语音结束(说话声)
//         * MSG_ASR_ONLINE_SPEECH_NOT_FOUND: 未检测到有效语音(说话声)
         * MSG_ASR_ONLINE_READ: 本次识别服务就绪,等待传入数据
         * MSG_ASR_ONLINE_READ: 识别中
         * MSG_ASR_ONLINE_COMPLETED: 本次语音识别结束
         * MSG_ASR_ONLINE_TERMINATION: 语音识别终止
         * MSG_ASR_ONLINE_AUDIO_DATA: 识别的音频数据回调
         *
         */
        String MSG_ASR_ONLINE_PART_RESULT = "asr.online.part.result";
        String MSG_ASR_ONLINE_LAST_RESULT = "asr.online.last.result";
        String MSG_ASR_ONLINE_SPEECH_END = "asr.online.speech.end";//(说话声)
        String MSG_ASR_ONLINE_SPEECH_START = "asr.online.speech.start";//(说话声)
//        String MSG_ASR_ONLINE_SPEECH_NOT_FOUND = "asr.online.none.speech";//(说话声)
        String MSG_ASR_ONLINE_WORKING = "asr.online.working";
        String MSG_ASR_ONLINE_READY = "asr.online.ready";
        String MSG_ASR_ONLINE_COMPLETED = "asr.online.completed";
        String MSG_ASR_ONLINE_TERMINATION = "asr.online.terminate";
        String MSG_ASR_ONLINE_AUDIO_DATA = "asr.online.audio.data";

        /**
         * 唤醒功能回调相关参数
         * MSG_WAKEUP_INIT_SUCC：唤醒初始化成功，可以开始往里面送入音频
         * MSG_WAKEUP_SUCC：唤醒成功
         */
        String MSG_WAKEUP_INIT_SUCC = "wakeup.init.success";
        String MSG_WAKEUP_SUCC = "wakeup.success";


        /**
         * 其他功能
         * ...
         */
    }

    /**
     * 各种操作的控制命令
     * void send(String command, String paramString, short[] paramArrayOfByte,
     *           int paramInt1, int paramInt2)  的第一个参数输入
     */
    public interface Command{
        /**
         * 在线识别控制命令
         * ----------------------------------------------
         * --> 创建ASR引擎；
         * --> ASR引擎开启；
         * --> 识别；
         * --> 停止；
         * --> 取消；
         * --> 销毁ASR资源。
         */
        String ASR_ONLINE_CREATE = "asr.online.create";
        String ASR_ONLINE_START = "asr.online.start";
        String ASR_ONLINE_RECOGIZE = "asr.online.recognize";
        String ASR_ONLINE_STOP = "asr.online.stop";
        String ASR_ONLINE_CANCEL = "asr.online.cancel";
        String ASR_ONLINE_DESTROY = "asr.online.destroy";

        /**
         * 唤醒控制指令
         */
        String WAKEUP_START = "wakeup.start";

        /**
         * 其他功能
         * ...
         */
    }


    public interface Parameter{

        /**
         * APPID app在搜狗的唯一标识
         * UUID 设备唯一标识
         */
        String APPID = "sogo.APPID";
        String UUID = "sogo.UUID";
        /**
         * 在线识别参数设置
         * ----------------------------------------------
         *
         *
         * ASR_ONLINE_VAD_ENABLE_BOOLEAN：
         *      VAD 开关
         * ASR_ONLINE_VAD_BOS_FLOAT:
         *      语音后端超时检测，如果已经开始说话后，间隔超过 ASR_ONLINE_VAD_BOS_FLOAT 时间不说话，则返回 OUT_OF_INTERVAL_TIME 消息，此后将进入不可再输入状态。
         * ASR_ONLINE_VAD_BOS_FLOAT:
         *      语音后端超时检测，如果已经开始说话后，间隔超过 ASR_VAD_EOS_FLOAT 时间不说话，则返回 MSG_SPEECH_END 消息，此后将进入不可再输入状态。
         * ASR_ONLINE_VAD_MAX_INTERVAL_INT：
         *      VAD检测到有效声音的最大时长，超过该时长，vad将返回 ERROR_VAD_SPEECH_TIMEOUT 消息，此后将进入不可再输入状态。
         * ASR_ONLINE_VAD_MAX_AUDIO_LENGTH_INT：
         *      VAD可检测的最长音频长度，如果音频长度超过该阈值，则返回ERROR_AUDIO_TOO_LONG消息，此后将进入不可再输入状态。
         * ASR_ONLINE_VAD_LONGMODE_BOOLEAN:
         *      VAD长时模式，开启后可以一直进行输入，vad将过滤掉无效声音/静音，返回有效声音。(暂时等效于enable_vad = false)
         *      此模式下，ASR_VAD_BOS_FLOAT，ASR_VAD_EOS_FLOAT，ASR_VAD_MAX_INTERVAL_FLOAT 失效。
         *
         * ASR_ONLINE_AUDIO_CODING_INT:
         *      音频编码格式:
         *      value = 1 Uncompressed 16-bit signed little-endian samples (Linear PCM).
         *      value = 2.[`FLAC`](https://xiph.org/flac/documentation.html) (Free Lossless Audio Codec)
         *      value = 100. speex压缩格式
         * ASR_ONLINE_LANGUAGE_STRING:
         *      识别的语种。
         *
         * ASR_ONLINE_AUTH_TOKEN_STRING:
         *      语音识别token，鉴权用。
         *
         * ASR_ONLINE_SEND_PACK_LEN_INT:
         *      发送请求时语音包长，取值范围（3200，8000），单位Short。必须设置200ms的整数倍，200ms的数据长度为3200Short，最长不得超过500ms。
         *
         * ASR_ONLINE_MODEL:
         *      分环境设置：包括通用、地图导航类、搜索(search)等。
         *
         * ASR_SPEECH_CONTEXTS:
         *      自定义热词，能够提升这些词在本次识别中的得分权重，更容易识别出这些词。
         *
         * ASR_PROFANITY_FILTER_BOOLEAN:
         *      敏感词过滤开关。
         *
         * ASR_DISABLE_AUTOMATIC_PUNCTUATION_BOOLEAN:
         *      禁用自动标点开关。
         *
         * ASR_ENABLE_WORD_TIMEOFFSETS_BOOLEAN:
         *      识别内容位置信息开关。
         *
         * ASR_MAX_ALTERNATIVES_INT:
         *      候选项最大数目。
         *
         */

        String ASR_ONLINE_VAD_ENABLE_BOOLEAN = "asr.online.vad.enable";
        String ASR_ONLINE_VAD_BOS_FLOAT = "asr.online.vad.bos";
        String ASR_ONLINE_VAD_EOS_FLOAT = "asr.online.vad.eos";
        String ASR_ONLINE_VAD_MAX_INTERVAL_INT = "asr.online.vad.max.interval";
        String ASR_ONLINE_VAD_MAX_AUDIO_LENGTH_INT = "asr.online.vad.max.audio.length";
        String ASR_ONLINE_VAD_LONGMODE_BOOLEAN = "asr.online.vad.long-mode.enable";

        String ASR_ONLINE_AUDIO_CODING_INT = "asr.online.audio.coding";
        String ASR_ONLINE_LANGUAGE_STRING = "asr.online.language";
        String ASR_ONLINE_AUTH_TOKEN_STRING = "asr.online.token";
        String ASR_ONLINE_SEND_PACK_LEN_INT = "asr.online.pack.length";
        String ASR_ONLINE_MODEL = "asr.online.model";

        String ASR_SPEECH_CONTEXTS = "asr.speech.contexts";

        String ASR_PROFANITY_FILTER_BOOLEAN = "asr.profanity.filter";
        String ASR_DISABLE_AUTOMATIC_PUNCTUATION_BOOLEAN = "asr.disable.automatic.punc";
        String ASR_ENABLE_WORD_TIMEOFFSETS_BOOLEAN = "asr.enable.wordtimeoffset";
        String ASR_MAX_ALTERNATIVES_INT = "asr.max.alternatives";

        /**
         * WAKEUP_IS_NEEDED:是否需要唤醒功能
         * WAKEUP_ONESHOT_IS_NEEDED：有了唤醒之后，是否需要oneshot功能
         * WAKEUP_NEED_FORCE_INIT:是否强行建模
         * WAKEUP_KEYWORD_PATH：本地唤醒词路径
         */
        String WAKEUP_IS_NEEDED = "wakeup.is.needed";
        String WAKEUP_ONESHOT_IS_NEEDED = "wakeup.oneshot.is.needed";
        String WAKEUP_NEED_FORCE_INIT = "wakeup.need.force.init";
        String WAKEUP_KEYWORD_PATH = "wakeup.keyword.path";

        /**
         * ASR_ONLINE_ENABLE_DEBUG_LOG_BOOLEAN:
         *      是否打印debug日志
         * ASR_ONLINE_DEBUG_SAVE_VAD_PATH:
         *      调试，保存vad数据至传入路径。
         * ASR_ONLINE_DEBUG_SAVE_SPEEX_PATH:
         *      保存speex数据至传入的路径。
         * ASR_ONLINE_DEBUG_SAVE_REQUEST_DATA_PATH:
         *      保存传给语音服务的数据，路径。

         */
        String ASR_ONLINE_ENABLE_DEBUG_LOG_BOOLEAN = "asr.online.enable.debuglog";
        String ASR_ONLINE_DEBUG_SAVE_VAD_PATH = "asr.online.save.vad";
        String ASR_ONLINE_DEBUG_SAVE_SPEEX_PATH = "asr.online.save.speex";
        String ASR_ONLINE_DEBUG_SAVE_REQUEST_DATA_PATH = "asr.online.save.requestdata";


    }
}