// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.sogovad;

/**
 * vad 回调信息,(1)数据回调
 *             (2)消息状态错误等回调
 */
public interface VadDetectorCallback {
    /**
     * @param code  标识回调类型
     * @param msg   回调所携带的提示信息
     * @param extra
     */
    void onCallback(int code, String msg, Object extra);

    /**
     * @param isValid 数据是否有效
     * @param voice   数据
     * @param sn      序号
     * @param start   相对于传递进来的voice的位置
     * @param end     相对于传递进来的voice的位置
     */
    void onVadProcessed(boolean isValid, short[] voice, int sn, long start, long end, Object extra);
}