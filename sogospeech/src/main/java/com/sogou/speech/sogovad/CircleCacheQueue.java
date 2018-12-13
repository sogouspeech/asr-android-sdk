// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.sogovad;

/**
 * 缓存队列 缓存区满了之后 丢弃前面先放进来的数据 FIFO
 */
class CircleCacheQueue {
    private short[] cache;
    private int cacheSize;
    private int curLen;

    public CircleCacheQueue(int cacheSize) {
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("cacheSize 必须大于0");
        }

        this.cacheSize = cacheSize;
        cache = new short[cacheSize];
        this.curLen = 0;
    }

    /**
     * 往缓存中添加数据  当缓存满了之后 丢弃数据 规则：FIFO
     *
     * @return put操作之后 缓存中当前的数据量
     */
    public synchronized int put(short[] data) {
        if (null != data && data.length > 0) {
            int curLenAndDataLength = curLen + data.length;
            if (curLenAndDataLength <= cacheSize) {// 不用移除数据
                System.arraycopy(data, 0, cache, curLen, data.length);
                curLen = curLenAndDataLength;
            } else {// 需要移除数据
                int del_count = curLenAndDataLength - cacheSize;
                if (del_count < curLen) {
                    // 当前缓存中的数据需要移除部分
                    System.arraycopy(cache, del_count, cache, 0, curLen - del_count);
                    System.arraycopy(data, 0, cache, curLen - del_count, data.length);
                } else {
                    // 当前缓存中的数据需要全部移除
                    int index = data.length - cacheSize;
                    System.arraycopy(data, index, cache, 0, cacheSize);
                }

                curLen = cacheSize;
            }
        }

        return curLen;
    }

    /**
     * 获取缓存中的数据 同时移除获取的数据
     *
     * @return
     */
    public synchronized short[] get() {
        short[] ret = null;
        if (curLen == cacheSize) {
            ret = cache;
        } else if (curLen > 0) {
            ret = new short[curLen];
            System.arraycopy(cache, 0, ret, 0, curLen);
        }

        curLen = 0;

        return ret;
    }

    public synchronized int getCacheDatalength() {
        return curLen;
    }

    public synchronized void reset() {
        curLen = 0;
    }

    public synchronized void destroy() {
        cache = null;
        cacheSize = 0;
        curLen = 0;
    }
}