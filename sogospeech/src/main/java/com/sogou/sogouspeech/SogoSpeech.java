// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sogou.sogocommon.utils.CommonSharedPreference;
import com.sogou.sogocommon.utils.FileUtils;
import com.sogou.sogocommon.utils.LogUtil;
import com.sogou.sogocommon.utils.RingBufferFlip;
import com.sogou.sogocommon.utils.ShortByteUtil;
import com.sogou.sogouspeech.auth.TokenFetchTask;
import com.sogou.sogouspeech.paramconstants.SpeechConstants;
import com.sogou.sogouspeech.recognize.IAudioRecognizer;
import com.sogou.sogouspeech.recognize.bean.SogoASRConfig;
import com.sogou.sogouspeech.recognize.impl.OnlineRecognizer;
import com.sogou.sogouspeech.recognize.impl.WakeupRecognizer;
import com.sogou.speech.asr.v1.RecognitionConfig;
import com.sogou.speech.sogovad.ConfigurableParameterName;
import com.sogou.speech.sogovad.IVadDetector;
import com.sogou.speech.sogovad.SogouVadDetector;
import com.sogou.speech.sogovad.VadDetectorCallback;
import com.sogou.speech.sogovad.VadTip;
import com.speex.speech.sogou.sogocompression.speex.SpeexEncoder;

import org.conscrypt.Conscrypt;

import java.lang.ref.WeakReference;
import java.security.Security;
import java.util.concurrent.atomic.AtomicReference;


public class SogoSpeech implements InstructionsManager , VadDetectorCallback, EventListener {
    private static final String TAG = SogoSpeech.class.getSimpleName();
    private IVadDetector mVadDetector = null;
    private SpeexEncoder mSpeexEncoder = null;
    private IAudioRecognizer mRecognizer = null;
    private IAudioRecognizer mWakeupRecognizer = null;
    private SogoASRConfig.ASRSettings mAsrSettings = new SogoASRConfig.ASRSettings();
    private final AtomicReference<EventListener> mListener = new AtomicReference<>(null);

    private static final int RINGBUFFER_SIZE = 16000;
    private RingBufferFlip resizeDataCache = null;
    private volatile int mPackageSize = SpeechConstants.LENGTH_200MS_SHORT;

    private HandlerThread mWorkThread = null;
    private volatile SogoSpeechHandler mSogoSpeechHandler = null;
    private ASR_ONLINE_ENGINE_STATUS mEngineStatus = ASR_ONLINE_ENGINE_STATUS.TO_INIT;

    private static final int MSG_ON_ASR_CREATE = 100;
    private static final int MSG_ON_ASR_START = 101;
    private static final int MSG_ON_ASR_RECOGNIZE = 102;
    private static final int MSG_ON_ASR_STOP = 103;
    private static final int MSG_ON_ASR_CANCEL = 104;
    private static final int MSG_ON_ASR_DESTROY = 105;


    private static final int MSG_ON_WAKEUP_START = 200;
    private static final int MSG_ON_WAKEUP_RECGONIZE = 201;

    private static final int MSG_TRANS_TEXT = 300;
    private static final int MSG_TRANS_INIT = 301;
    private static final int MSG_TRANS_DESTROY = 302;

    private static final int MSG_SPECIAL_USE = 999;

    //TODO 后面下面两个变量要改成用户可设置的
    //是否需要唤醒功能
//    private boolean needWakeup = false;
//    在需要唤醒的基础上，是否需要oneshot功能
//    private boolean needOneshot = false;

    static {
        try {
            Security.insertProviderAt(Conscrypt.newProvider("GmsCore_OpenSSL"), 1);
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    private SogoSpeechSettings mSettings = null;
    private Context mContext = null;

    /**
     *保存debug音频，用于音频命名。
     */
    public long mTimeStamp = 0;

    /**
     * vad 生效的模式下
     * 是否已经检测到有效声音，如果未检测到有效声音，则stop后，不需要向服务端发送识别结束标识。
     */
    private boolean mSpeechFound = false;


    private enum ASR_ONLINE_ENGINE_STATUS {
        /**
         * 尚未初始化
         */
        TO_INIT,
        /**
         * 初始化完成
         */
        INITED,

        /**
         * 开始工作，传入数据
         */
        WORKING,
        /**
         * 停止
         */
        STOP
    }

    public SogoSpeech(Context context) {
        mContext = context;
        mSettings = SogoSpeechSettings.shareInstance();
        mWorkThread = new HandlerThread(TAG);
        mWorkThread.start();
        mSogoSpeechHandler = new SogoSpeechHandler(this,mWorkThread.getLooper());
        if (TextUtils.isEmpty(sBaseUrl)){
            throw new IllegalArgumentException("no base url");
        }
    }

    @Override
    public void send(String command, String paramString, short[] paramArrayOfByte, int paramInt1, int paramInt2) {
        switch (command){
            case SpeechConstants.Command.ASR_ONLINE_CREATE:// alloc init
                mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_CREATE).sendToTarget();
                break;
            case SpeechConstants.Command.ASR_ONLINE_START:
                mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_START).sendToTarget();
                break;
            case SpeechConstants.Command.ASR_ONLINE_RECOGIZE:
                if(mSettings.needWakeup){
                    if(mWakeupRecognizer!=null && !((WakeupRecognizer)mWakeupRecognizer).getIsAwake()) {
                        mSogoSpeechHandler.obtainMessage(MSG_ON_WAKEUP_RECGONIZE, paramInt1, 0, paramArrayOfByte).sendToTarget();
                    }else {
                    }
                }else{
                    mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_RECOGNIZE,paramInt1,0, paramArrayOfByte).sendToTarget();
                }
                break;
            case SpeechConstants.Command.ASR_ONLINE_STOP:
                mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_STOP).sendToTarget();
                break;
            case SpeechConstants.Command.ASR_ONLINE_CANCEL:
                mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_CANCEL).sendToTarget();
                break;
            case SpeechConstants.Command.ASR_ONLINE_DESTROY:
                mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_DESTROY).sendToTarget();
                break;

            case SpeechConstants.Command.WAKEUP_START:
                if(!mSettings.needWakeup){
                    return;
                }
                mSogoSpeechHandler.obtainMessage(MSG_ON_WAKEUP_START).sendToTarget();
                break;
            default:
                break;
        }

    }

    @Override
    public void registerListener(EventListener paramEventListener) {
        mListener.set(paramEventListener);
    }

    @Override
    public void unregisterListener(EventListener paramEventListener) {
        mListener.compareAndSet(paramEventListener, null);
    }


    private void createAsrOnline(Message msg){ // !wroking
        if (mEngineStatus == ASR_ONLINE_ENGINE_STATUS.WORKING){
            LogUtil.e("create asr failed, status = " + mEngineStatus);
            return;
        }

        if (mSettings.audioCoding == RecognitionConfig.AudioEncoding.SOGOU_SPEEX_VALUE){
            if (mSpeexEncoder == null){
                mSpeexEncoder = new SpeexEncoder();
            }
        }

        if(resizeDataCache == null){
            resizeDataCache = new RingBufferFlip(RINGBUFFER_SIZE*2);
        }
        resizeDataCache.reset();


        initOnlineAsrRecognizer();
        if(mSettings.needWakeup) {
            initWakeupRecognizer();
        }


        mEngineStatus = ASR_ONLINE_ENGINE_STATUS.INITED;
        onEvent(SpeechConstants.Message.MSG_ASR_ONLINE_READY,"create success.",null,0,0, null);

        LogUtil.i("OnlineRecognizer", "recognizer.initListening ");

    }

    private void initWakeupRecognizer(){
        mWakeupRecognizer = new WakeupRecognizer(mContext,this);
        SogoASRConfig.WakeupSettings wakeupSettings = new SogoASRConfig.WakeupSettings();
        wakeupSettings.setForceInit(false);
        wakeupSettings.setWakeupConfPath(Environment.getExternalStorageDirectory()+"/sogou/audio/kws");
        wakeupSettings.setWakeupConfFilename("kws_conf");
        wakeupSettings.setKeywordPath("");
        mWakeupRecognizer.initListening(wakeupSettings);
    }

    private void initOnlineAsrRecognizer(){
        mRecognizer = new OnlineRecognizer(mContext,this);
        mAsrSettings.setAppid(mSettings.appid);
        mAsrSettings.setToken(mSettings.token);
        mAsrSettings.setUuid(mSettings.uuid);
        mAsrSettings.setLanguageCode(mSettings.asrlanguage);
        mAsrSettings.setModel(mSettings.model);
        if (mSettings.audioCoding == RecognitionConfig.AudioEncoding.SOGOU_SPEEX_VALUE){
            mAsrSettings.setAudioEncoding(RecognitionConfig.AudioEncoding.SOGOU_SPEEX);
        }else if (mSettings.audioCoding == RecognitionConfig.AudioEncoding.LINEAR16_VALUE){
            mAsrSettings.setAudioEncoding(RecognitionConfig.AudioEncoding.LINEAR16);
        }else {
            LogUtil.e("audio code wrong");
        }
        mRecognizer.initListening(mAsrSettings);
    }

    private void startASROnline(Message msg){
        if (mEngineStatus != ASR_ONLINE_ENGINE_STATUS.INITED && mEngineStatus!= ASR_ONLINE_ENGINE_STATUS.STOP){
            LogUtil.e("start asr failed, status = " + mEngineStatus);
            return;
        }
        if (resizeDataCache != null){
            resizeDataCache = null;
        }
        if (mSettings.enableVad){
            if (mVadDetector != null){
                mVadDetector = null;
            }
            mVadDetector = new SogouVadDetector();
            mVadDetector.init(null);
            mVadDetector.setParameter(ConfigurableParameterName.ASR_VAD_BOS_FLOAT,mSettings.bos);
            mVadDetector.setParameter(ConfigurableParameterName.ASR_VAD_EOS_FLOAT,mSettings.eos);
            mVadDetector.setParameter(ConfigurableParameterName.ASR_VAD_ENABLE_DEBUG_LOG_BOOLEAN,mSettings.enableLog);
            mVadDetector.setParameter(ConfigurableParameterName.ASR_VAD_LONGMODE_BOOLEAN,mSettings.isVadLongMode);
            mVadDetector.setParameter(ConfigurableParameterName.ASR_VAD_MAX_INTERVAL_INT,mSettings.maxSpeechInterval);
            mVadDetector.setParameter(ConfigurableParameterName.ASR_VAD_MAX_AUDIO_LENGTH_INT,mSettings.maxRecordInterval);
            mVadDetector.registCallback(this);
            mVadDetector.reset(null);
        }

        mPackageSize = mSettings.packageSize;

        mSpeechFound = false;
        mTimeStamp = System.currentTimeMillis();
        resizeDataCache = new RingBufferFlip(RINGBUFFER_SIZE*2);
        mEngineStatus = ASR_ONLINE_ENGINE_STATUS.WORKING;
        onEvent(SpeechConstants.Message.MSG_ASR_ONLINE_WORKING,"asr engine is ready.",null,0,0, null);
        if (mSettings.enableVad && !mSettings.isVadLongMode){

        } else {
            mRecognizer.startListening(mSettings.asrlanguage);
        }

        LogUtil.i("OnlineRecognizer", "recognizer.startListening ");
    }

    private void startWakeup(){
        if(null == mWakeupRecognizer){
            LogUtil.e(TAG,"mWakeupRecognizer is null");
            mListener.get().onError(SpeechConstants.ErrorDomain.ERR_WAKEUP_NOT_INIT,1101,"mWakeupRecognizer is null.Please send SpeechConstants.Command.ASR_ONLINE_CREATE again ",null);
            return;
        }
        mWakeupRecognizer.startListening("");
    }

    private void recognize(Message msg){

        LogUtil.i("OnlineRecognizer", "recognizer.recognize ");

        if (mEngineStatus != ASR_ONLINE_ENGINE_STATUS.WORKING){
            LogUtil.e("asr recognize data failed, status = " + mEngineStatus);
            return;
        }

        int snBeforeVad = msg.arg1;
        short[] voiceDataBeforeVad = (short[]) msg.obj;
        LogUtil.d("OnlineRecognizer", "sogo speech recognize data:"+voiceDataBeforeVad.length);
        if (mSettings.enableVad && !mSettings.isVadLongMode) { // todo：vad long mode需要修改recognizer的逻辑
            rawAudioPreprocess(snBeforeVad,voiceDataBeforeVad);
        } else {
//            if(mSettings.audioCoding == RecognitionConfig.AudioEncoding.SOGOU_SPEEX_VALUE){
//                byte[] bytes = mSpeexEncoder.encode(voiceDataBeforeVad);
//                mRecognizer.feedByteData(snBeforeVad,bytes);
//                bytes = null;
//            }else if(mSettings.audioCoding == RecognitionConfig.AudioEncoding.LINEAR16_VALUE) {
//                mRecognizer.feedShortData(snBeforeVad,voiceDataBeforeVad);
//            }

            resizeShortAudioData(voiceDataBeforeVad,voiceDataBeforeVad.length,snBeforeVad,true);
        }
        msg = null;
        voiceDataBeforeVad = null;
    }

    private void wakeupRecgonize(Message msg){
        int snBeforeVad = msg.arg1;
        short[] voiceDataBeforeVad = (short[]) msg.obj;
        if(mWakeupRecognizer!=null){
            mWakeupRecognizer.feedShortData(snBeforeVad,voiceDataBeforeVad);
        }
        voiceDataBeforeVad = null;
    }

    private void rawAudioPreprocess(int sn , short[] rawVoiceData){
        if (sn < 0){
            mEngineStatus = ASR_ONLINE_ENGINE_STATUS.STOP;
            resizeShortAudioData(rawVoiceData,rawVoiceData.length,sn,true);
        }
        else {
            mVadDetector.detect(rawVoiceData, sn, null);
        }
    }

    /**
     * 对short[]型音频数据进行整形，由任意长度的攒包成packageSize长度的
     * @param data 音频数据
     * @param length 包长
     * @param packageID 包序号
     * @param needresize 暂时无用
     */
    private void resizeShortAudioData(short[] data, int length, int packageID, boolean needresize){
        LogUtil.d(TAG,"data to resizeShortAudioData size is "+data.length + " packageid = " + packageID + "  package size = " + mPackageSize);
        if (null != data) {
            resizeDataCache.put(data, length);
        }
        boolean isLastPackage = (packageID<0);

        do {
            final int curLen = resizeDataCache.available();
            if (curLen < mPackageSize && !isLastPackage) {
                return;
            }
            short[] part = new short[Math.min(curLen, mPackageSize)];
            resizeDataCache.take(part,  part.length);

            if (resizeDataCache.available() == 0 && isLastPackage) {
                packageID = -Math.abs(packageID);
            }else{
                packageID = Math.abs(packageID);
            }

            try {
                LogUtil.v(TAG,"bytes to recognize size is "+part.length +"  part size is "+part.length+"  package id is "+packageID);
//                onEvent(SpeechConstants.Message.MSG_ASR_ONLINE_AUDIO_DATA,"", ShortByteUtil.shortArray2ByteArray(part),-1,part.length*2);
                if(mSettings.audioCoding == RecognitionConfig.AudioEncoding.LINEAR16_VALUE) {
                    LogUtil.d(TAG, "recognizer.feedShortData ");
                    if (mSettings.requestDataPath != null){
                        byte[] temp = ShortByteUtil.shortArray2ByteArray(part);
                        FileUtils.writeByteArray2SDCard(mSettings.requestDataPath, mTimeStamp + "_data.pcm" , temp,true);
                        temp = null;
                    }
                    mRecognizer.feedShortData(packageID,part);
                    part = null;
                }else if(mSettings.audioCoding == RecognitionConfig.AudioEncoding.SOGOU_SPEEX_VALUE){
                    LogUtil.d(TAG, "recognizer.speex.feedShortData ");
                    byte[] bytes = mSpeexEncoder.encode(part);
                    if (mSettings.speexDataPath != null){
                        FileUtils.writeByteArray2SDCard(mSettings.speexDataPath, mTimeStamp + ".spx" , bytes,true);
                    }
                    mRecognizer.feedByteData(packageID,bytes);
                    bytes = null;
                }else{
                    LogUtil.e(TAG,"unsupported audio format");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (resizeDataCache.available() > 0);

        if(packageID<0){
            resizeDataCache.reset();
        }
    }

    private void stop(Message msg){
        if (mEngineStatus != ASR_ONLINE_ENGINE_STATUS.WORKING){
            LogUtil.e("stop failed, status = " + mEngineStatus);
            return;
        }

        if (mRecognizer != null) {
            if (mSettings.enableVad && !mSettings.isVadLongMode) { //开启vad后
                if (mSpeechFound) { //且发现了有效声音
                    LogUtil.d("【停止】已经存在有效声音，需要结束识别服务");
                    mRecognizer.stopListening();
                } else { // 开启了vad,但没发现有效声音，没有开启过start，所以不需要stop
                    LogUtil.d("【停止】没检测到有效声音");
                    if (mListener != null){
                        handleEvent(SpeechConstants.Message.MSG_ASR_ONLINE_COMPLETED, "", null, 0, 0, null);
                    }
                }
            } else {
                LogUtil.e("没有启用vad，或者使用了长时模式，停止识别");
                mRecognizer.stopListening();
            }
        }

        if(mWakeupRecognizer!=null){
            LogUtil.d("停止唤醒，并重置唤醒状态，可进行下次唤醒");
            mWakeupRecognizer.stopListening();
        }

        mEngineStatus = ASR_ONLINE_ENGINE_STATUS.STOP;
    }

    private void cancel(Message msg){
        if (mEngineStatus != ASR_ONLINE_ENGINE_STATUS.WORKING){
            LogUtil.e("cancel failed, status = " + mEngineStatus);
            return;
        }
        if (mRecognizer != null){
            mRecognizer.cancelListening();
        }
        if(mWakeupRecognizer!=null){
            LogUtil.d("取消唤醒，并重置唤醒状态，可进行下次唤醒");
            mWakeupRecognizer.stopListening();
        }
        mEngineStatus = ASR_ONLINE_ENGINE_STATUS.STOP;

    }

    private void destroy(Message msg){
        try {
            if (mVadDetector != null) {
                mVadDetector.release(null);
            }
            mVadDetector = null;
            if (mSpeexEncoder != null) {
                mSpeexEncoder.destroy();
            }
            mSpeexEncoder = null;
            if(mRecognizer!=null){
                mRecognizer.destroy();
            }
            mRecognizer = null;
            if (mWakeupRecognizer != null) {
                mWakeupRecognizer.destroy();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mWakeupRecognizer = null;
        mEngineStatus = ASR_ONLINE_ENGINE_STATUS.TO_INIT;
    }


    private void handleEvent(String eventName, String param, byte[] data, int offset, int length,Object extra){
        if(mSettings.needWakeup && mSettings.needOneshot && TextUtils.equals(eventName,SpeechConstants.Message.MSG_WAKEUP_SUCC)){
            mEngineStatus = ASR_ONLINE_ENGINE_STATUS.STOP;
            mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_START).sendToTarget();
            short[] tempData = ShortByteUtil.byteArray2ShortArray(data);
            mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_RECOGNIZE,offset,0, tempData).sendToTarget();
            tempData = null;
        }
        if(mListener!=null && mListener.get()!=null){
            mListener.get().onEvent(eventName,param,data,offset,length,extra);
        }else{
            Log.e(SpeechConstants.CommonTag,"callback listener is null! Illeagal!");
        }
    }

    private void handleError(String errorDomain, int errorCode, String errorDescription, Object extra){
        if(mListener!=null && mListener.get()!=null){
            mListener.get().onError(errorDomain,errorCode,errorDescription,extra);
        }else{
            Log.e(SpeechConstants.CommonTag,"callback listener is null! Illeagal!");
        }
        if(mWakeupRecognizer!=null){
            mWakeupRecognizer.stopListening();
        }
        mEngineStatus = ASR_ONLINE_ENGINE_STATUS.STOP;

//        if (errorDomain == SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_SERVER || errorDomain == SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_NETWORK ||
//                errorDomain == SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_ENGINE_LOGIC ) {
//            mEngineStatus = ASR_ONLINE_ENGINE_STATUS.STOP;
//        }
    }


    private static class SogoSpeechHandler extends Handler {
        WeakReference<SogoSpeech> mReference;
        SogoSpeechHandler(SogoSpeech sogoSpeech, Looper looper) {
            super(looper);
            mReference = new WeakReference<SogoSpeech>(sogoSpeech);
        }

        @Override
        public void handleMessage(Message msg) {
            SogoSpeech localEngineRef = mReference.get();
            if(localEngineRef != null){
                handleLocalMessage(msg, localEngineRef);
            }else{
                LogUtil.e("SogouAsrSemEngine # localEngineRef == null, cannot handle local msg:"+msg.what);
            }
        }
    }

    private static void handleLocalMessage(Message msg, SogoSpeech instance){
        switch (msg.what){
            case MSG_ON_ASR_CREATE:
                instance.createAsrOnline(msg);
                break;
            case MSG_ON_ASR_START:
                instance.startASROnline(msg);
                break;
            case MSG_ON_ASR_RECOGNIZE:
                instance.recognize(msg);
                break;
            case MSG_ON_ASR_STOP:
                instance.stop(msg);
                break;
            case MSG_ON_ASR_CANCEL:
                instance.cancel(msg);
                break;
            case MSG_ON_ASR_DESTROY:
                instance.destroy(msg);
                break;
            case MSG_ON_WAKEUP_START:
                instance.startWakeup();
                break;
            case MSG_ON_WAKEUP_RECGONIZE:
                instance.wakeupRecgonize(msg);
                break;
            case MSG_SPECIAL_USE:
                int code = (int)msg.arg1;
                String msgStr = (String)msg.obj;
                if(code == VadTip.ERROR_AUDIO_TOO_LONG.code || code == VadTip.ERROR_VAD_SPEECH_TOO_LONG.code || code == VadTip.ERROR_VAD_SPEECH_TIMEOUT.code){
                    instance.handleError(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_PREPROCESS,code,msgStr,null);
                }
                break;
            default:
                break;
        }
    }

//================================================================================
// Vad listener
    @Override
    public void onCallback(int code, String msg, Object extra) {
        LogUtil.d(TAG,"code:"+code+" msg:"+msg);
        if(code == VadTip.ERROR_AUDIO_TOO_LONG.code || code == VadTip.ERROR_VAD_SPEECH_TOO_LONG.code || code == VadTip.ERROR_VAD_SPEECH_TIMEOUT.code){ //vad报错误状态
            if(mRecognizer != null){
                mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_STOP).sendToTarget();
            }
//            handleError(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_PREPROCESS,code,msg,null);
            mSogoSpeechHandler.obtainMessage(MSG_SPECIAL_USE,code,0,msg).sendToTarget();
        }
        else if (code == VadTip.MSG_SPEECH_END.code){ //vad检测到说话结束
            if(mRecognizer != null){
                mSogoSpeechHandler.obtainMessage(MSG_ON_ASR_STOP).sendToTarget();
            }
            handleEvent(SpeechConstants.Message.MSG_ASR_ONLINE_SPEECH_END, null, null, 0,  0, null);
        }
        else if (code == VadTip.MSG_SPEECH_START.code){
            mSpeechFound = true;
            handleEvent(SpeechConstants.Message.MSG_ASR_ONLINE_SPEECH_START, null, null, 0,  0, null);
            if(mRecognizer != null && mSettings != null) {
                mRecognizer.startListening(mSettings.asrlanguage);
            }
        }
    }


    @Override
    public void onVadProcessed(boolean isValid, short[] voice, int sn, long start, long end, Object extra) {
        if(isValid){
            LogUtil.d(TAG,"isValid:"+isValid+" voice.length:"+voice.length+" sn："+sn+" start:"+start+" end:"+end);
            resizeShortAudioData(voice,voice.length,sn,true);
            if (mSettings.vadDataPath != null){
                byte[] temp = ShortByteUtil.shortArray2ByteArray(voice);
                FileUtils.writeByteArray2SDCard(mSettings.vadDataPath, mTimeStamp + "_vad.pcm" , temp,true);
                temp = null;
            }
        }
        else {
            LogUtil.d(TAG,"isValid:"+isValid+ " sn:"+sn+" start:"+start+" end:"+end);
        }
    }
// Vad listener
//================================================================================


//================================================================================
// AudioRecognizer的回调EventListener
    @Override
    public void onEvent(String eventName, String param, byte[] data, int offset, int length, Object extra) {
        handleEvent(eventName,param,data,offset,length,extra);
    }

    @Override
    public void onError(String errorDomain, int errorCode, String errorDescription, Object extra) {
        handleError(errorDomain,errorCode,errorDescription,extra);
    }

    // AudioRecognizer的回调EventListener
//================================================================================

    //onlineTranslatorCallback


    public static String sBaseUrl = "";

    public void setToken(String token){
        CommonSharedPreference.getInstance(mContext).setString(CommonSharedPreference.TOKEN, token);
    }

    public void setTokenExp(long tokenExp){
        CommonSharedPreference.getInstance(mContext).setLong(CommonSharedPreference.TIMEOUT_STAMP, tokenExp);
    }

    public static void initZhiyinInfo(Context context, ZhiyinInitInfo info) {
        initZhiyinInfo(context,info,null);
    }

    public static void initZhiyinInfo(Context context, ZhiyinInitInfo info, final TokenFetchTask.TokenFetchListener listener){
        if (TextUtils.isEmpty(info.baseUrl)){
            throw new IllegalArgumentException("no baseUrl!");

        }

        if (TextUtils.isEmpty(info.uuid)){
            throw new IllegalArgumentException("no uuid!");

        }

        if (TextUtils.isEmpty(info.appid)){
            throw new IllegalArgumentException("no appid!");

        }

        sBaseUrl = info.baseUrl;
        CommonSharedPreference.getInstance(context).setString("uuid",info.uuid);
        CommonSharedPreference.getInstance(context).setString("appid",info.appid);
        if (!TextUtils.isEmpty(info.token)) {
            CommonSharedPreference.getInstance(context).setString(CommonSharedPreference.TOKEN, info.token);
            if (info.tokenExp > 0){
                CommonSharedPreference.getInstance(context).setLong(CommonSharedPreference.TIMEOUT_STAMP, info.tokenExp);
            }
        }else if(!TextUtils.isEmpty(info.appkey)) {
            CommonSharedPreference.getInstance(context).setString("appkey", info.appkey);

            TokenFetchTask task = new TokenFetchTask(context, sBaseUrl, new TokenFetchTask.TokenFetchListener() {
                @Override
                public void onTokenFetchSucc(String result) {
                    if (listener != null){
                        listener.onTokenFetchSucc(result);
                    }
                }

                @Override
                public void onTokenFetchFailed(String errMsg) {
                    if (listener != null){
                        listener.onTokenFetchFailed(errMsg);
                    }
                }
            });
            task.execute(null);
        }else {
            throw new IllegalArgumentException("no token or appkey!");
        }
    }

}