// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech.recognize;

import com.sogou.sogouspeech.recognize.bean.SogoASRConfig;


/**
 * 1.初始化：initListening
 * 2.启动引擎：startListening，建立连接
 * 3.输入音频数据：feedShortData
 *   当sn < 0，表示不再输入音频数据。然后即可重复2步骤继续流程。
 * 4.stopListening(或者feedShortData(sn<0))，执行完毕可重复2步骤继续流程。
 * 5.cancelListening 取消识别，可重复步骤2继续。
 * 6.destroy 销毁引擎。
 *
 *
 * subclasses:{@com.sogou.sogouspeech.recognize.impl.OnlineRecognizer,
 *             @com.sogou.sogouspeech.recognize.impl.WakeupRecognizer}
 */
public abstract class IAudioRecognizer {

    public abstract void initListening(SogoASRConfig.SogoSettings settings);

    public abstract void startListening(String languageCode);

    public abstract void stopListening();

    public abstract void cancelListening();

    public abstract void destroy();

    public abstract void feedShortData(int sn, short[] data);

    public abstract void feedByteData(int sn, byte[] data);


}