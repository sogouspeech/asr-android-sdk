package com.sogou.sogouspeech.translate;

import android.content.Context;

import com.sogou.sogocommon.utils.CommonSharedPreference;
import com.sogou.sogocommon.utils.HttpsUtil;
import com.sogou.sogocommon.utils.LogUtil;
import com.sogou.sogouspeech.SogoSpeech;
import com.sogou.sogouspeech.recognize.impl.HeaderClientInterceptor;
import com.sogou.speech.mt.v1.TranslateConfig;
import com.sogou.speech.mt.v1.TranslateTextRequest;
import com.sogou.speech.mt.v1.TranslateTextResponse;
import com.sogou.speech.mt.v1.mtGrpc;

import java.util.HashMap;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.NegotiationType;
import io.grpc.okhttp.OkHttpChannelProvider;
import io.grpc.stub.StreamObserver;

public class OnlineTranslator implements ITranslatorInterface {

    private mtGrpc.mtStub mtClient;
    private StreamObserver<TranslateTextResponse> mTranslateResponse = null;
    private TransaltionCallback callback;
    private volatile ManagedChannel channel;
    private Context mContext = null;

    public OnlineTranslator(Context context, TransaltionCallback callback) {
        this.callback = callback;
        mContext = context;
    }

    @Override
    public void initTranslation(String fromLanguage,String toLanguage) {
        HashMap<String, String > headerParams = new HashMap<>(8);
        headerParams.put("Authorization", "Bearer " + CommonSharedPreference.getInstance(mContext).getString(CommonSharedPreference.TOKEN,""));
        headerParams.put("appid",  CommonSharedPreference.getInstance(mContext).getString("appid",""));
        headerParams.put("uuid", CommonSharedPreference.getInstance(mContext).getString("uuid",""));

        channel = new OkHttpChannelProvider()
                .builderForAddress(SogoSpeech.sBaseUrl,
                        443)
                .overrideAuthority(SogoSpeech.sBaseUrl
                        +":443")
                .sslSocketFactory(HttpsUtil.getSSLSocketFactory(null, null, null))
                .negotiationType(NegotiationType.TLS)
                .intercept(new HeaderClientInterceptor(headerParams))
                .build();
        mtClient = mtGrpc.newStub(channel);
        callback.onTranslationInit();
    }

    @Override
    public int startTranslation(String text,String fromLanguage, String toLanguage) {
        int ret = -1;
        if(mtClient == null){
            callback.onTranslationErrorCb("mt client is null",10086);
            return ret;
        }

        mTranslateResponse = new StreamObserver<TranslateTextResponse>() {
            @Override
            public void onNext(TranslateTextResponse value) {
//                LogUtil.loge("xq","TranslateTextResponse "+value.toString());
//                LogUtil.loge("xq","getSourceText "+value.getSourceText());
//                LogUtil.loge("xq","getTranslatedText "+value.getTranslatedText());
                if(value==null){
                    callback.onTranslationErrorCb("translation response is null",this.hashCode());
                    return;
                }
                callback.onTranslationResultCb(true,value.getSourceText(),value.getTranslatedText(),0,this.hashCode());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                callback.onTranslationErrorCb("onError:"+t.getMessage(),this.hashCode());
                LogUtil.e("OnlineTranslator","onError "+t.getMessage());
            }

            @Override
            public void onCompleted() {
                LogUtil.e("OnlineTranslator","translate onCompleted："+this.hashCode());
            }
        };

        ret = mTranslateResponse.hashCode();

        TranslateTextRequest mTranslateRequest = TranslateTextRequest.newBuilder()
                .setConfig(TranslateConfig.newBuilder()
                        .setSourceLanguageCode(fromLanguage)
                        .setTargetLanguageCode(toLanguage)
                        .build())
                .setText(text)
                .build();
        mtClient.translateText(mTranslateRequest,mTranslateResponse);
        return ret;
    }

    @Override
    public void releaseTranslation() {
        if (channel != null){
            channel.shutdownNow();
        }

        channel = null;
        mtClient = null;
    }


}
