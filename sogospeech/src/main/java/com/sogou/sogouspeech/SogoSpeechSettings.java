// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech;

import android.text.TextUtils;
import android.util.Log;

import com.sogou.sogouspeech.paramconstants.LanguageCode;
import com.sogou.sogouspeech.paramconstants.SpeechConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by marxma on 2018/6/1.
 */

public class SogoSpeechSettings {
    private final static String TAG = "SogoSpeechSettings";
    // 鉴权
    public String appid = null;
    public String token = null;
    public String uuid = null;
    // vad 相关参数
    public boolean enableVad = true;
    public boolean isAutoStop = true;
    public float bos = 3.0f;
    public float eos = 1.0f;
    public int maxSpeechInterval = 60; //最大有效语音时长
    public int maxRecordInterval = 60; //最大录音时间，与maxSpeechInterval先到者生效。
    public boolean isVadLongMode = false;
    // 识别服务相关参数
    public int audioCoding = 1; //音频格式，1.pcm格式。2.flac格式。100.speex格式
    public String asrlanguage = LanguageCode.ASRLanguageCode.CHINESE; //语种，目前支持普通话
    public int packageSize = SpeechConstants.LENGTH_200MS_SHORT;
    public String model = "default";

    //唤醒相关参数
    //是否需要唤醒功能
    public volatile boolean needWakeup = false;
    //是否需要oneshot功能，是要在needWakeup == true的基础上才会生效的
    public volatile boolean needOneshot = false;
    //是否需要强行重新建模（一般是不需要的）
    public volatile boolean needForceInit = false;
    //传入本地的keywordpath，一般也不用，可以直接在assets里面的keywords文件里修改好
    public String keywordsPath = "";

    // Debug相关参数
    public boolean enableLog = true;
    public String vadDataPath = null;
    public String speexDataPath = null;
    public String requestDataPath = null; //保存上传到语音服务的音频数据

    //在线翻译相关参数
    private boolean isEnableOnlineTranslate = false;


    private static volatile SogoSpeechSettings ourInstance = null;

    public ArrayList<ArrayList<String>> asrSpeechContexts = null;

    public boolean asrProfanityFilter = true;
    public boolean asrDisableAutomaticPunctuation = false;
    public boolean asrEnableWordTimeOffsets = true;
    public int asrMaxAlternatives = 1;

    public static SogoSpeechSettings shareInstance() {
        if (ourInstance == null) {
            synchronized (SogoSpeechSettings.class) {
                if (ourInstance == null) {
                    ourInstance = new SogoSpeechSettings();
                }
            }
        }
        return ourInstance;
    }

    private SogoSpeechSettings() {
    }


    public SogoSpeechSettings setProperty(String parameterName, Object parameterValue){
        try {
            if (!TextUtils.isEmpty(parameterName)) {
                switch (parameterName) {
                    case SpeechConstants.Parameter.APPID:
                        appid = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.UUID:
                        uuid = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_AUDIO_CODING_INT:
                        audioCoding = (int) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_AUTH_TOKEN_STRING:
                        token = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_VAD_BOS_FLOAT:
                        bos = (float) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_VAD_EOS_FLOAT:
                        eos = (float) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_VAD_ENABLE_BOOLEAN:
                        enableVad = (boolean) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_VAD_LONGMODE_BOOLEAN:
                        isVadLongMode = (boolean) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_VAD_MAX_AUDIO_LENGTH_INT:
                        maxRecordInterval = (int) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_VAD_MAX_INTERVAL_INT:
                        maxSpeechInterval = (int) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_LANGUAGE_STRING:
                        asrlanguage = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_ENABLE_DEBUG_LOG_BOOLEAN:
                        enableLog = (boolean) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_DEBUG_SAVE_SPEEX_PATH:
                        speexDataPath = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_DEBUG_SAVE_VAD_PATH:
                        vadDataPath = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_DEBUG_SAVE_REQUEST_DATA_PATH:
                        requestDataPath = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_SEND_PACK_LEN_INT:
                        packageSize = (int) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ONLINE_MODEL:
                        model = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.WAKEUP_IS_NEEDED:
                        needWakeup = (boolean) parameterValue;
                        break;
                    case SpeechConstants.Parameter.WAKEUP_ONESHOT_IS_NEEDED:
                        needOneshot = (boolean) parameterValue;
                        break;
                    case SpeechConstants.Parameter.WAKEUP_NEED_FORCE_INIT:
                        needForceInit = (boolean) parameterValue;
                        break;
                    case SpeechConstants.Parameter.WAKEUP_KEYWORD_PATH:
                        keywordsPath = (String) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_SPEECH_CONTEXTS:
                        asrSpeechContexts = (ArrayList<ArrayList<String>>) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_PROFANITY_FILTER_BOOLEAN:
                        asrProfanityFilter = (boolean>) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_DISABLE_AUTOMATIC_PUNCTUATION_BOOLEAN:
                        asrDisableAutomaticPunctuation = (boolean>) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_ENABLE_WORD_TIMEOFFSETS_BOOLEAN:
                        asrEnableWordTimeOffsets = (boolean>) parameterValue;
                        break;
                    case SpeechConstants.Parameter.ASR_MAX_ALTERNATIVES_INT:
                        asrMaxAlternatives = (int) parameterValue;
                        break;
                    default:
                        Log.e("SogoSpeech", "Parameter set error, there is no parameter name：" + parameterName);
                        break;
                }
            }
        }catch (ClassCastException e){
            Log.e(TAG,""+e.getMessage());
            e.printStackTrace();
        }

        return ourInstance;
    }

    public String paramToString (){
                String result = "enableVad:"+enableVad+" "
                +"bos:"+bos+" "
                +"eos:"+eos+" "
                +"maxSpeechInterval:"+maxSpeechInterval+" "
                +"maxRecordInterval:"+maxRecordInterval+" "
                +"isVadLongMode:"+isVadLongMode+" "
                +"needWakeup:"+needWakeup;
        return result;
    }
    public SogoSpeechSettings setProperty(HashMap map) {
        if (map != null) {
            for (Object key : map.keySet()) { //todo 现在是最慢的遍历
                String param = (String) key;
                setProperty(param, map.get(key));
            }
        }
        return ourInstance;
    }

//
//    @Override
//    public String toString() {
//        String result = "enableVad:"+enableVad+" "
//                +"bos:"+bos+" "
//                +"eos:"+eos+" "
//                +"maxSpeechInterval:"+maxSpeechInterval+" "
//                +"maxRecordInterval:"+maxRecordInterval+" "
//                +"isVadLongMode:"+isVadLongMode+" "
//                +"needWakeup:"+needWakeup;
//        return result;
//    }
}