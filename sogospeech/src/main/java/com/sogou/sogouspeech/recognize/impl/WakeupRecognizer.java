// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech.recognize.impl;

import android.content.Context;
import android.util.Log;

import com.sogou.sogocommon.utils.LogUtil;
import com.sogou.sogocommon.utils.ShortByteUtil;
import com.sogou.sogouspeech.EventListener;
import com.sogou.sogouspeech.paramconstants.SpeechConstants;
import com.sogou.sogouspeech.recognize.IAudioRecognizer;
import com.sogou.sogouspeech.recognize.bean.SogoASRConfig;
import com.sogou.sogouspeech.wakeup.WakeupManager;

public class WakeupRecognizer extends IAudioRecognizer {
    private final static String TAG  ="WakeupRecognizer";

    private EventListener mListener = null;
    private SogoASRConfig.WakeupSettings wakeupSettings = null;
    private Context mContext = null;
//    private WakeupManager mWakeupManager = null;
    private boolean isWorking = false;
    private boolean initKwsRet = false;
    public WakeupRecognizer(Context context, EventListener listener) {
        mListener = listener;
        mContext = context;
//        mWakeupManager = WakeupManager.getInstance(context);
    }

    @Override
    public void initListening(SogoASRConfig.SogoSettings settings) {
        if(null == settings){
            Log.e(SpeechConstants.CommonTag, "wakeupSettings is null");
            return;
        }
        wakeupSettings = (SogoASRConfig.WakeupSettings)settings;
        //异步构建唤醒引擎，只需在低性能设备上第一次使用唤醒功能时调用
        WakeupManager.getInstance(mContext).setWakeupConfPath(wakeupSettings.getWakeupConfPath(), wakeupSettings.getWakeupConfFilename());
        WakeupManager.getInstance(mContext).asyncInitWakeUpEngine(wakeupSettings.isForceInit(), wakeupSettings.getKeywordPath(), new EventListener() {
            @Override
            public void onEvent(String eventName, String param, byte[] data, int offset, int length,Object extra) {
                LogUtil.v("xq","@asyncInitWakeUpEngine onEvent "+eventName+"  null != mListener"+(null != mListener));
                if(null != mListener){
                    mListener.onEvent(eventName,param,data,offset,length,null);
                }
                initKws();
            }

            @Override
            public void onError(String errorDomain, int errorCode, String errorDescription, Object extra) {
                if(null != mListener){
                    mListener.onError(errorDomain,errorCode,errorDescription,extra);
                }
            }
        });
        settings = null;
    }

    private void initKws(){
        initKwsRet =  WakeupManager.getInstance(mContext).initKWS();
        if(!initKwsRet){
            LogUtil.d("init kws failed");
            if(null != mListener){
                mListener.onError(SpeechConstants.ErrorDomain.ERR_WAKEUP_INIT_KWS,1100,"WakeUpManager init failed",null);
            }
            isWorking = false;
        }else{
            isWorking = true;
        }
    }

    @Override
    public void startListening(String languageCode) {
//        if (mWakeupManager == null) {
//            Log.e(SpeechConstants.CommonTag, "mWakeupManager is null");
//            return;
//        }
//        LogUtil.d(TAG,"do nothing");
//        boolean ret =  WakeupManager.getInstance(mContext).initKWS();
//        if(!ret){
//            LogUtil.d("init kws failed");
//            if(null != mListener){
//                mListener.onError(SpeechConstants.ErrorDomain.ERR_WAKEUP_INIT_KWS,1100,"WakeUpManager init failed",null);
//            }
//            isWorking = false;
//        }else{
//            isWorking = true;
//        }
        isWorking = true;
    }

    @Override
    public void stopListening() {
        isWorking = false;
        WakeupManager.getInstance(mContext).resetKWS();
    }

    @Override
    public void cancelListening() {
        isWorking = false;
        WakeupManager.getInstance(mContext).resetKWS();
    }

    @Override
    public void destroy() {
        try {
            isWorking = false;
            WakeupManager.getInstance(mContext).releaseKWS();
            initKwsRet = false;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void feedShortData(int sn, short[] data) {
//        LogUtil.d("xq","feedShortData");
        if(!isWorking){
            return;
        }
        boolean isWakeup = false;
        if (getIsAwake()) {
            return;
        }
        isWakeup = WakeupManager.getInstance(mContext).startRecognize(sn, data);

        if (isWakeup) {
            wakeupSucess(sn, data);
        }
    }

    @Override
    public void feedByteData(int sn, byte[] data) {

    }

    public boolean getIsAwake(){
        return WakeupManager.getInstance(mContext).isAwake();
    }

    private void wakeupSucess(int sn, short[] data){
        WakeupManager.getInstance(mContext).setIsAwake(true);
//           sn = 0;
        data = WakeupManager.getInstance(mContext).getPreVoiceData();

        LogUtil.d(TAG,"wake up success!");
        if(null != mListener){
            byte[] temp = ShortByteUtil.shortArray2ByteArray(data);
            String keywords = WakeupManager.getInstance(mContext).getKeyWord();
            mListener.onEvent(SpeechConstants.Message.MSG_WAKEUP_SUCC,keywords, temp,sn,0, null);
            temp = null;
            keywords = null;
        }
        data = null;
    }
}