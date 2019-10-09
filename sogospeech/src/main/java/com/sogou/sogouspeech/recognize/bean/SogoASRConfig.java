// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech.recognize.bean;

import com.sogou.speech.asr.v1.RecognitionConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SogoASRConfig {

    public static class SogoSettings{

    }
    public static class ASRSettings extends SogoSettings{
        private String token;
        private String appid;
        private String uuid;
        private String languageCode;
        private RecognitionConfig.AudioEncoding audioEncoding = RecognitionConfig.AudioEncoding.LINEAR16;
        private String model;
//        private int asrMode;

        private ArrayList<ArrayList<String>> speechContext = null;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getLanguageCode() {
            return languageCode;
        }

        public void setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
        }

//        public int getAsrMode() {
//            return asrMode;
//        }
//
//        public void setAsrMode(int asrMode) {
//            this.asrMode = asrMode;
//        }

        public String getModel() {
            return model;
        }

        public void setModel(String _model) {
            this.model = _model;
        }

        public RecognitionConfig.AudioEncoding getAudioEncoding() {
            return audioEncoding;
        }

        public void setAudioEncoding(RecognitionConfig.AudioEncoding audioEncoding) {
            this.audioEncoding = audioEncoding;
        }

        public void setSpeechContext(ArrayList<ArrayList<String>> context){
            this.speechContext = null;
            this.speechContext = context;
        }
        public ArrayList<ArrayList<String>> getSpeechContext(){
            return speechContext;
        }


        @Override
        public String toString() {
//            return super.toString();
            return "appid:("+getAppid()+"); uuid:(" + getUuid() + "); Language:(" + getLanguageCode() + "); model:(" + getModel() + "); \ntoken:" + getToken();
        }
    }

    public static class WakeupSettings extends SogoSettings{
        private String wakeupConfPath;
        private String wakeupConfFilename;
        private String keywordPath;
        private boolean isForceInit = false;

        public String getWakeupConfPath() {
            return wakeupConfPath;
        }

        public void setWakeupConfPath(String wakeupConfPath) {
            this.wakeupConfPath = wakeupConfPath;
        }

        public String getWakeupConfFilename() {
            return wakeupConfFilename;
        }

        public void setWakeupConfFilename(String wakeupConfFilename) {
            this.wakeupConfFilename = wakeupConfFilename;
        }

        public String getKeywordPath() {
            return keywordPath;
        }

        public void setKeywordPath(String keywordPath) {
            this.keywordPath = keywordPath;
        }

        public boolean isForceInit() {
            return isForceInit;
        }

        public void setForceInit(boolean forceInit) {
            isForceInit = forceInit;
        }

        @Override
        public String toString() {
            return "wakeupConfPath:("+getWakeupConfPath()+"); wakeupConfFilename:(" + getWakeupConfFilename() + "); keywordPath:(" + getKeywordPath() + "); ";
        }
    }




}