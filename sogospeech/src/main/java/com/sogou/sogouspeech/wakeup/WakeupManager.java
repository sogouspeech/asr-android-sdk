// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech.wakeup;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.sogou.sogocommon.ErrorIndex;
import com.sogou.sogocommon.utils.CircleCacheQueue;
import com.sogou.sogocommon.utils.CommonSharedPreference;
import com.sogou.sogocommon.utils.FileUtils;
import com.sogou.sogocommon.utils.LogUtil;
import com.sogou.sogouspeech.EventListener;
import com.sogou.sogouspeech.paramconstants.SpeechConstants;
import com.sogou.speech.wakeup.WakeUp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author lijunchen
 * @edited by xuq
 */
public class WakeupManager {

    private static final String LIB_VERSION = "libs_v1.5.2b_0518";

    //对应so 3.2.0
    private int API_VERSION = 1005;


    private static final int ERROR_INVALID_PARAM = -1;
    private static final int ERROR_COPY_CONFIG = -2;
    private static final int ERROR_SET_CONFIG_PATH = -3;
    private static final int ERROR_SET_MODEL_PATH = -4;
    private static final int ERROR_BUILD_MODEL = -5;
    private static final int ERROR_INVALID_KEYWORD_PATH = -6;
    private static final int ERROR_COPY_KEYWORD = -7;

    private Context mContext;

    private static final String KWS_ASSET_FOLDER = "config";

    private static final String SP_HAS_CONFIGS_KEY = "has_configs";

    private static final String SP_HAS_BUILD_NET = "has_build_net";

    private static final String SP_WAKEUP_VERSION = "api_version";

    //缓冲区大小 = 声道 * 采样率 * 时间
    private static final int CACHE_SIZE = 1 * 16000 * 1;

    private int GARBAGE_SCORE = -40;

    private int cacheSize = 2000;

    private boolean useFrame = false;

    private void setGarbageScore(int score) {
        GARBAGE_SCORE = score;
    }

    private void setCacheSize(int size) {
        cacheSize = size;
    }

    private void setUseFrame(boolean flag) {
        useFrame = flag;
    }

    private boolean useAGC = false;

    private void setOpenAGC(boolean flag) {
        useAGC = flag;
    }

    private volatile static WakeupManager wakeUpManager = null;

    private boolean isWorking = false; //是否在工作状态

    private volatile boolean isAwake = false; //是否已经唤醒

    private CircleCacheQueue mCircleCacheQueue; //用来缓存唤醒前数据的循环队列

//    private CacheQueue mCacheQueue;//用来拼包的缓冲队列

    private String keyWord = "";

    long mWakeupHandle; // 唤醒句柄

    private int level = 1; //敏感度等级

    String wakeupConfFile = "";

    public static synchronized WakeupManager getInstance(Context context) {
        if (wakeUpManager == null) {
            synchronized (WakeupManager.class) {
                if (wakeUpManager == null) {
                    wakeUpManager = new WakeupManager(context.getApplicationContext());
                }
            }
        }
        return wakeUpManager;
    }

    private WakeupManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private void setLevel(int level) {
        this.level = level;
    }

    public void forceInitWakeUpEngine(final EventListener listener) {
        forceInitWakeUpEngine("", listener);
    }

    public void forceInitWakeUpEngine(String keyWordPath, final EventListener listener) {
        boolean res = copyAssetFolder(mContext.getAssets(), KWS_ASSET_FOLDER, FileUtils.getDataFileDir(mContext));
        if (res) {
            boolean ret = CommonSharedPreference.getInstance(mContext).setBoolean(SP_HAS_CONFIGS_KEY, true);
            if (ret) {
                CommonSharedPreference.getInstance(mContext).setInt(SP_WAKEUP_VERSION, API_VERSION);
            }
        } else {

        }
    }

    public void asyncInitWakeUpEngine(final EventListener listener) {
        asyncInitWakeUpEngine(false, "", listener);
    }

    public void asyncInitWakeUpEngine(boolean forceInit, EventListener listener) {
        asyncInitWakeUpEngine(forceInit, "", listener);
    }

    public void asyncInitWakeUpEngine(String keywordPath, EventListener listener) {
        asyncInitWakeUpEngine(false, keywordPath, listener);
    }

    /**
     * 异步建模
     *
     * @param forceInit   是否强制建模
     * @param keywordPath keyWord路径，如 /sdcard/test/keywords
     * @param listener    建模状态的监听器
     */
    public void asyncInitWakeUpEngine(final boolean forceInit, final String keywordPath, final EventListener listener) {
        if (!forceInit && hasBuildNet()) {
            if (listener != null) {
                listener.onEvent(SpeechConstants.Message.MSG_WAKEUP_INIT_SUCC,"",null,0,0, null);
                return;
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                preBuildNet(forceInit, keywordPath, listener);
            }
        }).start();
    }

    private void preBuildNet(boolean forceInit, final String keywordPath, final EventListener listener) {
        //先拷贝assert中的配置文件
        File file = new File(FileUtils.getDataFileDir(mContext));
        if (!file.exists()) {
            file.mkdirs();
        }
        //如果需要重新建模，则重新拷贝assets目录下的配置文件
        if (forceInit) {
            CommonSharedPreference.getInstance(mContext).setBoolean(SP_HAS_CONFIGS_KEY, false);
        }

        if (!ensureConfigFilesInDataDir()) {
            if (listener != null) {
                listener.onError(SpeechConstants.ErrorDomain.ERR_WAKEUP_COPY_CONFIG, ErrorIndex.ERROR_WAKE_UP_COPY_CONFIG,getErrorMsg(ERROR_COPY_CONFIG),null);
            }
            return;
        }

        //如果keywordPath不为空，则拷贝sdcard中的关键词
        if (!TextUtils.isEmpty(keywordPath)) {
            File keywordFile = new File(keywordPath);
            if (!keywordFile.exists()) {
                if (listener != null) {
                    listener.onError(SpeechConstants.ErrorDomain.ERR_WAKEUP_INVALID_KEYWORD_PATH,ErrorIndex.ERROR_WAKE_UP_INVALID_KEYWORD_PATH, getErrorMsg(ERROR_INVALID_KEYWORD_PATH),null);
                }
                return;
            } else {
                boolean ret = copyFile(keywordPath, FileUtils.getDataFileDir(mContext) + File.separator + "keywords");
                if (!ret) { //拷贝keyword失败
                    if (listener != null) {
                        listener.onError(SpeechConstants.ErrorDomain.ERR_WAKEUP_COPY_KEYWORD, ErrorIndex.ERROR_WAKE_UP_COPY_KEYWORD, getErrorMsg(ERROR_COPY_KEYWORD),null);
                    }
                    return;
                }
            }
        }
        //最后进行建模
//        String confPath = FileUtils.getDataFileDir(mContext);
//        File modelFile = new File(confPath + "/model.awb");
//        if (forceInit && modelFile.exists()) { //如果强制建模，则删除原模型文件
//            modelFile.delete();
//        }
//        //删除后或者第一次建模
//        if (!modelFile.exists()) {
//            if (WakeUp.wakeup_set_bn_data_path(confPath) < 0) {
//                if (listener != null) {
//                    listener.onEvent(SpeechConstants.ErrorDomain.ERR_WAKEUP_SET_CONFIG_PATH, getErrorMsg(ERROR_SET_CONFIG_PATH),null,0,0);
//                }
//                return;
//            }
//            if (WakeUp.wakeup_set_bn_model_path(confPath) < 0) {
//                if (listener != null) {
//                    listener.onEvent(SpeechConstants.ErrorDomain.ERR_WAKEUP_SET_MODEL_PATH, getErrorMsg(ERROR_SET_MODEL_PATH),null,0,0);
//                }
//                return;
//            }
//            if (WakeUp.wakeup_build_net(confPath + "/keywords") < 0) {
//                if (listener != null) {
//                    listener.onEvent(SpeechConstants.ErrorDomain.ERR_WAKEUP_BUILD_MODEL, getErrorMsg(ERROR_BUILD_MODEL),null,0,0);
//                }
//                return;
//            }
//        }
        CommonSharedPreference.getInstance(mContext).setBoolean(SP_HAS_BUILD_NET, true);
        listener.onEvent(SpeechConstants.Message.MSG_WAKEUP_INIT_SUCC,"",null,0,0, null);
    }

    private String getErrorMsg(int errCode) {
        if (errCode == -1) {
            return "无效的垃圾词得分";
        }
        if (errCode == -2) {
            return "无有效的配置文件或复制文件失败";
        }
        if (errCode == -3) {
            return "设置配置文件路径失败";
        }
        if (errCode == -4) {
            return "配置模型路径失败";
        }
        if (errCode == -5) {
            return "模型建网失败";
        }
        if (errCode == -6) {
            return "关键词文件不存在";
        }
        if (errCode == -7) {
            return "拷贝关键词文件失败";
        }
        return "";
    }

    public void setWakeupConfPath(String dir, String filename){
        if(TextUtils.isEmpty(dir) || TextUtils.isEmpty(filename)){
            throw new NullPointerException("empty kws conf file");
        }
        FileUtils.createDirectory(dir, true ,false);
        if(dir.endsWith("/")){
            this.wakeupConfFile = dir + filename;
        }else{
            this.wakeupConfFile = dir + "/" + filename;
        }
    }

    /**
     * 预建模
     */
    public int preBuildNet(int version, int garbage_score) {
        String confPath = FileUtils.getDataFileDir(mContext);
        API_VERSION = version;
        if (garbage_score < -50 || garbage_score > -10) {
            return -1;
        }
        File file = new File(FileUtils.getDataFileDir(mContext));
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!ensureConfigFilesInDataDir()) {
            return -2;
        }

        if (!new File(confPath + "/model.awb").exists()) {
            if (WakeUp.wakeup_set_bn_data_path(confPath) < 0) {
                return -3;
            }
            if (WakeUp.wakeup_set_bn_model_path(confPath) < 0) {
                return -4;
            }
            if (WakeUp.wakeup_build_net(confPath + "/keywords") < 0) {
                return -5;
            }
        }
        CommonSharedPreference.getInstance(mContext).setBoolean(SP_HAS_BUILD_NET, true);
        return 0;
    }


    public boolean initKWS() {
        boolean res = false;
        try{
            res = initKWS(API_VERSION, GARBAGE_SCORE);
        }catch(Error e){
            res = false;
            LogUtil.w("init wakeup failed:"+e.getMessage());
        }
//        int garbageScore = -10 * (1 + level);
        return res;
    }

    public boolean initKWS(int version, int garbage_score) {
        if (!hasBuildNet()) {
            int ret = preBuildNet(version, garbage_score);

            if (ret < 0) {
                return false;
            }
        }
        mWakeupHandle = WakeUp.wakeup_init(FileUtils.getDataFileDir(mContext)+"/model.awb");
        /**
         * kws 相关参数今后都通过assets/config/wakeup.conf来设置， 不单独暴露设置唤醒参数的接口给用户
         */
//        WakeUp.wakeup_set_sensitivity(mWakeupHandle, 4);
//        WakeUp.wakeup_set_thread_num(mWakeupHandle, 1);
//        WakeUp.wakeup_set_packet_len(mWakeupHandle, 2000);

        String kwsModelVersion = WakeUp.wakeup_get_model_version(mWakeupHandle);
        String kwsLibVersion = WakeUp.wakeup_get_version();
        LogUtil.v("kws model version:"+kwsModelVersion+", kws so version:"+kwsLibVersion);

        try{
            WakeUp.wakeup_print_parameter(mWakeupHandle);
            if(!TextUtils.isEmpty(wakeupConfFile)){
                WakeUp.wakeup_print_parameter_file(mWakeupHandle, wakeupConfFile);
            }
        }catch(Throwable e){
            LogUtil.w("WakeUp.wakeup_print_parameter_file failed");
        }


//        WakeUp.wakeup_set_garbage_score(garbage_score);
//        WakeUp.wakeup_use_agc(level > 3 ? true : false);
        if(this.mCircleCacheQueue == null) {
            this.mCircleCacheQueue = new CircleCacheQueue(CACHE_SIZE);
        }else{
            this.mCircleCacheQueue.reset();
        }
        /** 8000是半秒的录音数据，设置为8000是可提高唤醒的精度 */
        /** 修改为6000为了提高唤醒的速度*/
//        this.mCacheQueue = new CacheQueue(20000, cacheSize);
        keyWord = "";
        isWorking = true;
        isAwake = false;
        return true;
    }


    /**
     * 是否已经建模
     */
    public boolean hasBuildNet() {
        return  CommonSharedPreference.getInstance(mContext).getBoolean(SP_HAS_BUILD_NET, false);
    }

    /**
     * 开始识别音频中是否含有唤醒词
     *
     *
     * @param seq
     * @param recordData 音频数据
     * @return 是否唤醒
     */
    public boolean startRecognize(int seq, short[] recordData) {
        if (!isWorking) {
            LogUtil.e("Try working on an uninited WakeUpManager.");
            return false;
        }

        mCircleCacheQueue.put(recordData);
//        mCacheQueue.put(recordData);

        /** 数据不够半秒时，不进行识别*/
//        if (!mCacheQueue.available()) {
//            return false;
//        }
//
//        recordData = mCacheQueue.read();

        final String result;
        int[] ret = new int[3];
        if (recordData != null) {
            result = WakeUp.wakeup_process(mWakeupHandle, seq, recordData, recordData.length, ret);

            final boolean success = ret[0] == 0;
            final int startPackId = ret[1];
            final int endPackId = ret[2];

            if(!success){
                WakeUp.wakeup_reset(mWakeupHandle);
            }else{
                if (result != null && result.length() != 0) {
                    keyWord = result;
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * 返回唤醒引擎识别出的关键字
     */
    public String getKeyWord() {
        if (keyWord == null) {
            return "";
        }
        return keyWord;
    }

    /**
     * 获取唤醒前一段时间的音频
     *
     * @return
     */
    public short[] getPreVoiceData() {
        if (mCircleCacheQueue != null) {
            return mCircleCacheQueue.get();
        }
        return new short[0];
    }

    /**
     * 是否唤醒成功
     */
    public synchronized boolean isAwake() {
        return isAwake;
    }

    /**
     * 车联网需求：是否停止唤醒根据外部决定
     *
     * @param isAwake
     */
    public synchronized void setIsAwake(boolean isAwake) {
        this.isAwake = isAwake;
    }

    /**
     * 重置唤醒引擎资源
     */
    public void resetKWS() {
        if (isWorking) {
            isAwake = false;
            keyWord = "";
            mCircleCacheQueue.reset();
//            mCacheQueue.clear();
        }
    }

    /**
     * 释放唤醒引擎
     *
     * @return
     */
    public void releaseKWS() {
        if (isWorking) {
            WakeUp.wakeup_release(mWakeupHandle);
            mCircleCacheQueue = null;
//            mCacheQueue = null;
            isWorking = false;
            isAwake = false;
        }

    }

    private boolean ensureConfigFilesInDataDir() {
        boolean hasConfigs = CommonSharedPreference.getInstance(mContext).getBoolean(SP_HAS_CONFIGS_KEY, false);
        if (!hasConfigs) {
            boolean res = copyAssetFolder(mContext.getAssets(),
                    KWS_ASSET_FOLDER, FileUtils.getDataFileDir(mContext));
            if (res) {
                boolean ret = CommonSharedPreference.getInstance(mContext).setBoolean(SP_HAS_CONFIGS_KEY, true);
                if (ret) {
                    boolean api = CommonSharedPreference.getInstance(mContext).setInt(SP_WAKEUP_VERSION, API_VERSION);
                    return api;
                }
                return ret;
            }
        }
        int version = CommonSharedPreference.getInstance(mContext).getInt(SP_WAKEUP_VERSION, 1000);
        if (API_VERSION != version) {
            if (deleteConfFile(FileUtils.getDataFileDir(mContext))) {
                boolean res = copyAssetFolder(mContext.getAssets(),
                        KWS_ASSET_FOLDER, FileUtils.getDataFileDir(mContext));
                if (res) {
                    boolean ret = CommonSharedPreference.getInstance(mContext).setBoolean(SP_HAS_CONFIGS_KEY, true);
                    if (ret) {
                        boolean api = CommonSharedPreference.getInstance(mContext).setInt(SP_WAKEUP_VERSION, API_VERSION);
                        return api;
                    }
                    return ret;
                }
            }
        }
        return hasConfigs;
    }

    private boolean deleteConfFile(String confPath) {
        File file = new File(confPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
        if (file.listFiles().length == 0) {
            return true;
        }
        return false;
    }

    /**
     * copy files from fromPath under asset dir to toPath. note that fromPath
     * may be empty.
     *
     * @param assetManager
     * @param fromPath
     * @param toPath
     * @liukeang 2016-6-28
     */
    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromPath, String toPath) {
        try {
            String[] configFiles = assetManager.list(fromPath);
            if (configFiles == null) {
                return false;
            }

            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : configFiles) {
                String assetFromPath = fromPath + File.separator + file;
                String assetToPath = toPath + File.separator + file;
                res &= copyAsset(assetManager, assetFromPath, assetToPath);
            }

            return res;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = assetManager.open(fromAssetPath);
            File targetFile = new File(toPath);
            if (targetFile.exists()) {
                if (targetFile.isDirectory()) {
                    FileUtils.deleteDir(targetFile);
                } else {
                    FileUtils.deleteFile(targetFile);
                }
            }
            targetFile.createNewFile();
            outputStream = new FileOutputStream(toPath);
            copyFile(inputStream, outputStream);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     * @param fromPath
     * @param toPath
     * @return 是否拷贝成功
     */
    private static boolean copyFile(String fromPath, String toPath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(fromPath);
            File targetFile = new File(toPath);
            if (targetFile.exists()) {
                if (targetFile.isDirectory()) {
                    FileUtils.deleteDir(targetFile);
                } else {
                    FileUtils.deleteFile(targetFile);
                }
            }
            targetFile.createNewFile();
            outputStream = new FileOutputStream(toPath);
            copyFile(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return true;
    }

    private static void copyFile(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}