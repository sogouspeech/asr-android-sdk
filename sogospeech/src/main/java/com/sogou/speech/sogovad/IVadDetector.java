// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.sogovad;

public interface IVadDetector {
    /**
     *
     * @param parameterName 参数名称 可配置参数请参考ConfigurableParameterName
     * @param parameterValue 参数值
     *
     *  注意必须在init(Object extra)之前调用，如果不调用 则所有参数采用默认值
     */
    IVadDetector setParameter(String parameterName, Object parameterValue);

    /**
     * 初始化
     * @param extra  方便以后扩展 暂时不需要
     */
    int init(Object extra);

    /**
     * 重置
     * @param extra 方便以后扩展 暂时不需要
     */
    void reset(Object extra);

    /**
     * 注册回调
     */
    IVadDetector registCallback(VadDetectorCallback callback);

    /**
     * vad 探测
     * @param voice 语音数据
     * @param sn 语音包序列号
     * @param extra 方便以后扩展 暂时不需要
     */
    void detect(short[] voice, int sn, Object extra);

    /**
     * 停止 vad 释放资源
     * @param extra 方便以后扩展 暂时不需要
     */
    void release(Object extra);

    /**
    * 当前算法的版本号、简单描述
    * */

    String getVersion();
    String getDescription();

}