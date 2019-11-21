// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech.recognize.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.sogou.sogocommon.ErrorIndex;
import com.sogou.sogocommon.utils.CommonSharedPreference;
import com.sogou.sogocommon.utils.HttpsUtil;
import com.sogou.sogocommon.utils.LogUtil;
import com.sogou.sogocommon.utils.ShortByteUtil;
import com.sogou.sogouspeech.EventListener;
import com.sogou.sogouspeech.SogoSpeech;
import com.sogou.sogouspeech.paramconstants.SpeechConstants;
import com.sogou.sogouspeech.recognize.IAudioRecognizer;
import com.sogou.sogouspeech.recognize.bean.SogoASRConfig;
import com.sogou.speech.asr.v1.RecognitionConfig;
import com.sogou.speech.asr.v1.SpeechContext;
import com.sogou.speech.asr.v1.SpeechRecognitionAlternative;
import com.sogou.speech.asr.v1.StreamingRecognitionConfig;
import com.sogou.speech.asr.v1.StreamingRecognitionResult;
import com.sogou.speech.asr.v1.StreamingRecognizeRequest;
import com.sogou.speech.asr.v1.StreamingRecognizeResponse;
import com.sogou.speech.asr.v1.asrGrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.NegotiationType;
import io.grpc.okhttp.OkHttpChannelProvider;
import io.grpc.stub.StreamObserver;

public class OnlineRecognizer extends IAudioRecognizer {
    private static final String TAG = OnlineRecognizer.class.getSimpleName();

    private SogoASRConfig.ASRSettings mAsrSettings = null;
    private EventListener mListener = null;
    private asrGrpc.asrStub client;
    private StreamObserver<StreamingRecognizeRequest> mRequestObserver;
    private ManagedChannel channel = null;
    private Context mContext = null;


    private volatile boolean isCompleted = false;

    public OnlineRecognizer(Context context, EventListener mListener) {
        this.mListener = mListener;
        mContext = context;
    }

    @Override
    public void initListening(SogoASRConfig.SogoSettings asrSettings) {
        if (null == asrSettings) {
            Log.e(SpeechConstants.CommonTag, "asrSettings is null");
            return;
        }

        mAsrSettings = (SogoASRConfig.ASRSettings) asrSettings;
        createGrpcClient();
    }

    @Override
    public void startListening(String languageCode) {
        if(!TextUtils.isEmpty(languageCode)){
            mAsrSettings.setLanguageCode(languageCode);
        }
        isCompleted = false;
        buildGrpcConnection();
    }

    @Override
    public void stopListening() {
//        LogUtil.d(TAG, "recognize.onCompleted");
        if(isCompleted){
            return;
        }
        if(mRequestObserver == null){
            return;
        }
        mRequestObserver.onCompleted();
        isCompleted = true;
    }

    @Override
    public void cancelListening() {
        finishRecognizing();
    }

    @Override
    public void destroy() {
        finishRecognizing();
//        mResponseObserver = null;
        client = null;
    }

    @Override
    public void feedShortData(int sn, short[] data) {
//        LogUtil.d(TAG, "feed short data(short) length : " + data.length);
        byte[] dateBytes = ShortByteUtil.shortArray2ByteArray(data);
        recognize(dateBytes,data.length,sn);
        dateBytes = null;
    }

    @Override
    public void feedByteData(int sn, byte[] data) {
//        LogUtil.d(TAG, "feed short data(short) length : " + data.length);
        recognize(data,data.length,sn);
    }

    private void createGrpcClient() {
        HashMap<String, String> headerParams = new HashMap<>();
        headerParams.put("Authorization", "Bearer " + CommonSharedPreference.getInstance(mContext).getString(CommonSharedPreference.TOKEN,""));
        headerParams.put("appid",  CommonSharedPreference.getInstance(mContext).getString("appid",""));
        headerParams.put("uuid", CommonSharedPreference.getInstance(mContext).getString("uuid",""));

        Log.d(TAG, "create rpc client : " + mAsrSettings);
        channel = null;
        channel = new OkHttpChannelProvider()
                .builderForAddress(SogoSpeech.sBaseUrl,
                        443)
                .overrideAuthority(SogoSpeech.sBaseUrl
                        + ":443")
                .negotiationType(NegotiationType.TLS)
                .sslSocketFactory(HttpsUtil.getSSLSocketFactory(null, null, null))
                .intercept(new HeaderClientInterceptor(headerParams))
                .build();
        client = null;
        client = asrGrpc.newStub(channel);

//        headerParams.clear();
//        headerParams = null;
    }

    private void buildGrpcConnection() {
        if (client == null) {
            if (mListener != null) {
                mListener.onError(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_ENGINE_LOGIC, ErrorIndex.ERROR_NETWORK_OTHER, "client == null",null);
            }
            return;
        }
        if(mResponseObserver == null){
            if (mListener != null) {
                mListener.onError(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_ENGINE_LOGIC, ErrorIndex.ERROR_NETWORK_OTHER, "mResponseObserver == null",null);
            }
            return;
        }

        SpeechContext.Builder context = SpeechContext.newBuilder();
        ArrayList<ArrayList<String>> speechContext = mAsrSettings.getSpeechContext();
        if(speechContext!=null){
            for (int i = 0; i < speechContext.size(); i++) {
                if(speechContext.get(i) != null && speechContext.get(i).size()>0) {
                    context.addAllPhrases(speechContext.get(i));
                }
            }
        }

        ArrayList<SpeechContext> speechContexts = new ArrayList<>();
        speechContexts.add(context.build());

        mRequestObserver = client.streamingRecognize(mResponseObserver);
        mRequestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(StreamingRecognitionConfig.newBuilder()
                        .setConfig(RecognitionConfig.newBuilder()
                                .setLanguageCode(mAsrSettings.getLanguageCode())
                                .setEncoding(mAsrSettings.getAudioEncoding())
                                .setSampleRateHertz(16000)
                                .setMaxAlternatives(mAsrSettings.getMaxAlternatives())
                                .setProfanityFilter(mAsrSettings.getProfanityFilter())
                                .setDisableAutomaticPunctuation(mAsrSettings.getDisableAutomaticPunctuation())
                                .setModel(mAsrSettings.getModel())
                                .addAllSpeechContexts(speechContexts)
                                .setEnableWordTimeOffsets(mAsrSettings.getEnableWordTimeOffsets())
                                .build())
                        .setInterimResults(true)
                        .build())
                .build());
        Log.d(TAG, "build rpc connection : " + mAsrSettings);
    }

    private StreamObserver<StreamingRecognizeResponse> mResponseObserver = new StreamObserver<StreamingRecognizeResponse>() {
        @Override
        public void onNext(StreamingRecognizeResponse response) {
            String text = null;
            boolean isFinal = false;
            StreamingRecognitionResult result = null;
//            LogUtil.d(TAG, "response = " + response + "\nresults count = " + response.getResultsCount());
            if (response.getResultsCount() > 0) {
                result = response.getResults(0);
                isFinal = result.getIsFinal();
                if (result.getAlternativesCount() > 0) {
                    final SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                    text = alternative.getTranscript();
//                    LogUtil.d(TAG, "callback text " + text);
                }
            }
            if (response.getError() != null) {
//                LogUtil.d(TAG, "response status is " + response.getError().getCode() + "  DetailsCount is " + response.getError().getDetailsCount());
                if (response.getError().getDetailsCount() > 0) {
//                    LogUtil.d(TAG, "response value is " + response.getError().getDetailsList().get(0).getValue().toString());
                }
//                LogUtil.d(TAG, "response errmsg is " + response.getError().getMessage());
                if (0 != response.getError().getCode() && 200 != response.getError().getCode()) {
                    if (mListener != null) {
//                        mListener.onEvent(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_SERVER, "error code:" + response.getError().getCode() + response.getError().getMessage().toString(), null, 0, 0);
                        mListener.onError(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_SERVER, response.getError().getCode(),response.getError().getMessage().toString(), null);
                    }
                }
            }
            if (text != null && mListener != null) {
                if (isFinal) {
                    mListener.onEvent(SpeechConstants.Message.MSG_ASR_ONLINE_LAST_RESULT, text, null, 0, 0, result);
                } else {
                    mListener.onEvent(SpeechConstants.Message.MSG_ASR_ONLINE_PART_RESULT, text, null, 0, 0, result);
                }
            }

        }

        @Override
        public void onError(Throwable t) {
            LogUtil.e(TAG, "Error calling the API." + t.getMessage());
            if (mListener != null) {
//                mListener.onEvent(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_NETWORK, "error code:-2 " + t.getMessage(), null, 0, 0);
                mListener.onError(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_NETWORK, ErrorIndex.ERROR_GRPC_SERVER, t.getMessage(),null);
            }
            t.printStackTrace();
        }

        @Override
        public void onCompleted() {
            LogUtil.d(TAG, "API completed.");
            if (mListener != null) {
                mListener.onEvent(SpeechConstants.Message.MSG_ASR_ONLINE_COMPLETED, "", null, 0, 0, null);
            } else {
                LogUtil.e(TAG, "err callback is null");
            }

        }
    };

    private void recognize(byte[] data, int size, long pkgID) {
        if(isCompleted){
            return;
        }
        if (mRequestObserver == null) {
            LogUtil.e(TAG,"mRequestObserver == null");
            return;
        }
        // Call the streaming recognition API
        ByteString tempData = null;
        tempData = ByteString.copyFrom(data);
        try {
            if (data != null && data.length > 0) {
                mRequestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(tempData)
                        .build());
                LogUtil.d(TAG , "recognize data(byte) length:" + data.length + " sn:" + pkgID);
            }

//            LogUtil.d(TAG,"packageReceivedID "+pkgID+" is dealed");
            if (pkgID < 0) {
                if(isCompleted){
//                    LogUtil.d(TAG, "recognize.onCompleted called already");
                    return;
                }
                LogUtil.d(TAG, "recognize.onCompleted");
                mRequestObserver.onCompleted();
                isCompleted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onError(SpeechConstants.ErrorDomain.ERR_ASR_ONLINE_ENGINE_LOGIC, ErrorIndex.ERROR_NETWORK_UNAVAILABLE, e.getMessage(),null);
            }
            LogUtil.e(TAG, "Exception! :" + e.getMessage());
        }

    }

    private void finishRecognizing() {
        if (!isCompleted && mRequestObserver != null) {
            mRequestObserver.onCompleted();
        }
        mRequestObserver = null;
        if (client == null) {
            return;
        }
        final ManagedChannel channel = (ManagedChannel) client.getChannel();
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdownNow();
//                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                LogUtil.e(TAG, "Error shutting down the gRPC channel. " + e.getMessage());
            }
        }


        LogUtil.d(TAG, "finishRecognizing");
        if(mListener != null){
            mListener.onEvent(SpeechConstants.Message.MSG_ASR_ONLINE_TERMINATION,"",null,0,0, null);
        }
    }

}