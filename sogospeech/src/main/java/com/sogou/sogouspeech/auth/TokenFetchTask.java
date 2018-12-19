// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech.auth;

import android.content.Context;
import android.text.TextUtils;

import com.google.protobuf.Duration;
import com.sogou.sogocommon.utils.SogoConstants;
import com.sogou.sogocommon.utils.CommonSharedPreference;
import com.sogou.sogocommon.utils.CommonUtils;
import com.sogou.sogocommon.utils.HttpsUtil;
import com.sogou.sogocommon.utils.LogUtil;
import com.sogou.sogouspeech.SogoSpeech;
import com.sogou.speech.auth.v1.CreateTokenRequest;
import com.sogou.speech.auth.v1.CreateTokenResponse;
import com.sogou.speech.auth.v1.authGrpc;

import org.conscrypt.Conscrypt;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.NegotiationType;
import io.grpc.okhttp.OkHttpChannelProvider;
import io.grpc.stub.StreamObserver;

public class TokenFetchTask {
    private Context context;
    private TokenFetchListener tokenFetchListener;
    private static long TIME_EXP = 8 * 60 * 60;
    private String baseUrl;

    static {
        try {
            Security.insertProviderAt(Conscrypt.newProvider("GmsCore_OpenSSL"), 1);
        } catch (Throwable t){
            t.printStackTrace();
        }
    }


    public TokenFetchTask(Context context, String baseUrl, TokenFetchListener listener) {
        this.context = context;
        this.tokenFetchListener = listener;
        this.baseUrl = baseUrl;

    }

    public void execute(Object object) {
        try {
            grpcRequestToken();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void onGainTokenSuccess(String token, long tokenExp) {
        CommonSharedPreference.getInstance(context).setString(CommonSharedPreference.TOKEN, token);
        CommonSharedPreference.getInstance(context).setLong(CommonSharedPreference.TIMEOUT_STAMP, tokenExp);
        tokenFetchListener.onTokenFetchSucc(token);
    }

    private void grpcRequestToken() throws NoSuchAlgorithmException, KeyManagementException {

        LogUtil.d("TokenFetchTask","grpc request token");
        Duration duration = Duration.newBuilder().setSeconds(TIME_EXP).build();
        CreateTokenRequest request = CreateTokenRequest.newBuilder()
                .setExp(duration)
                .setAppid(CommonSharedPreference.getInstance(context).getString("appid",""))
                .setAppkey(CommonSharedPreference.getInstance(context).getString("appkey",""))
                .setUuid(CommonSharedPreference.getInstance(context).getString("uuid",""))
                .buildPartial();
        ManagedChannel channel = new OkHttpChannelProvider().builderForAddress(baseUrl, 443)
                .negotiationType(NegotiationType.TLS)
                .overrideAuthority(SogoSpeech.sBaseUrl + ":443")
                .sslSocketFactory(HttpsUtil.getSSLSocketFactory(null, null, null))
                .build();
        authGrpc.authStub client = authGrpc.newStub(channel);
        client.createToken(request, new StreamObserver<CreateTokenResponse>() {
            @Override
            public void onNext(CreateTokenResponse tokenResponse) {
                onGainTokenSuccess(tokenResponse.getToken(), tokenResponse.getEndTime().getSeconds());
//                LogUtil.d("xq", "token:" + tokenResponse.getToken() + ",timestamp:" + tokenResponse.getEndTime().getSeconds());
            }

            @Override
            public void onError(Throwable t) {
                tokenFetchListener.onTokenFetchFailed("error:" + t.getLocalizedMessage());
                LogUtil.d("xq","onError "+t.getMessage());
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    public interface TokenFetchListener {
        void onTokenFetchSucc(String result);

        void onTokenFetchFailed(String errMsg);
    }
}