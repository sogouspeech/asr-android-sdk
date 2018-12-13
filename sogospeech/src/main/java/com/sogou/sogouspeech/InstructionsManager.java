// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech;

/**
 * Created by marxma on 2018/6/1.
 */

public interface InstructionsManager {

    /**
     * 操作命令的控制方法，控制启停、执行等操作
     * @param command 操作指令
     * @param paramString 相关参数
     * @param paramArrayOfByte 输入的音频数据
     * @param paramInt1 音频数据起点 index offset
     * @param paramInt2 音频数据长度
     */
    public void send(String command, String paramString, short[] paramArrayOfByte, int paramInt1, int paramInt2);

    /**
     * 注册回调监听
     * @param paramEventListener 监听
     */
    public void registerListener(EventListener paramEventListener);

    /**
     * 取消监听
     * @param paramEventListener 监听实例
     */
    public void unregisterListener(EventListener paramEventListener);

}