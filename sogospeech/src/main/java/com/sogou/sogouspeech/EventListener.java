// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.sogouspeech;

/**
 * Created by marxma on 2018/6/1.
 */

public interface  EventListener{
    /**
     * 消息回调
     * @param eventName name是输出事件名
     * @param param params该事件的参数
     * @param data (data,offset, length) 三者一起组成额外数据,如回调的音频数据，从data[offset] 开始至data[offset + length] 结束，长度为length
     * @param offset
     * @param length
     */
    public void onEvent(String eventName, String param, byte[] data, int offset, int length, Object extra);


    /**
     * 错误回调
     * @param errorDomain 错误域
     * @param errorCode 错误码 (详见ErrorIndex)
     * @param errorDescription 错误简要描述
     * @param extra 扩展参数
     */
    public void onError(String errorDomain, int errorCode, String errorDescription, Object extra);
}