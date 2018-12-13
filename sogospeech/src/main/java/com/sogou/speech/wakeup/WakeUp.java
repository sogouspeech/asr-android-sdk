// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.wakeup;

import java.util.*;
import java.io.*;

/**
 * WakeUp.java: SOGOU-INC.com
 *
 * the only header file for android JNI libwakeup.so
 */

/**
 * [[NEW FEATURES in v2.1.0 or above]]
 * 
 * wakeup process now support multi instances,
 * they can be distinguished through different handlers
 */

/**
 * [[CONFIGURE FILE FORMAT]]
 * 
 * configure file format instruction used in v2.4.0 or above:
 *
 * # this indicates a comment line
 * # configure name and value are seperated by ':'
 * [CONF_NAME_1] : [CONF_VALUE_1] # comment in line accepted, this is a comment
 * [conf_name_1] : [conf_value_1] # case sensitive, [CONF_NAME_1] and [conf_name_1] are different
 *
 * # empty line(s) accepted
 *
 * [CONF_NAME_2]:[CONF_VALUE_2] # another configure, optional space and tab aroung ':'
 * [CONF_NAME_1]:[CONF_VALUE_3]
 * # duplicate key will override previous value, finally [CONF_NAME_1]=[CONF_VALUE_3]
 *
 */

/**
 * [[CUSTOM WAKEUP]]
 * 
 * keywords can be customized by users
 * include build net and wakeup process functions
 */

public class WakeUp {

    // get execution time of paticular part in wakeup process
    // time unit: ms
    public static class WakeUp_time {

        // time of each part
        public float vad_time;
        public float agc_time;
        public float feature_time;
        public float dnn_time;
        public float search_time;
        public float post_time;

        // default constructor, all values default set to 0
        public WakeUp_time() {
            vad_time = 0;
            agc_time = 0;
            feature_time = 0;
            dnn_time = 0;
            search_time = 0;
            post_time = 0;
        }

    }

    static {
        System.loadLibrary("wakeup");
    }

    /**
     * [[application interfaces (APIs) in wakeup library]]
     * 
     * There are two different implementions in wakeup library, snet and e2e.
     * They share some same interfaces, and other interfaces are unique.
     * So it is important to remind that:
     * If you are provided with [snet] wakeup library, then you should use interfaces
     * marked with [shared] and [snet].
     * If you are provided with [e2e] wakeup library, then you should use interfaces
     * marked with [shared] and [e2e].
     */

    /**
     * 1. Basic APIs
     */

    /// 1.1 [shared] APIs

    //// get lib version number
    public native static String wakeup_get_version();

    /// 1.2 [snet] APIs

    //// none

    /// 1.3 [e2e] APIs

    //// none

    // end of 1. Basic APIs

    /**
     * 2. APIs for custom wakeup build net
     */

    /// 2.1 [shared] APIs

    //// set data path in build net
    public native static int wakeup_set_bn_data_path(String data_path);

    public native static int wakeup_set_data_path(String data_path);

    //// set output model path in build net
    public native static int wakeup_set_bn_model_path(String model_path);

    public native static int wakeup_set_model_path(String model_path);

    //// set output model file name
    public native static int wakeup_set_bn_model_file(String model_file);

    public native static int wakeup_set_model_file(String model_file);

    /**
     * build net process
     * 
     * build net process yet only support one instance, so no handler needed
     */
    public native static int wakeup_build_net(String keyword_file);

    //// print build net parameters into file
    public native static int wakeup_bn_print_parameter_file(String conf_file);

    //// print build net parameters into stdout
    public native static int wakeup_bn_print_parameter();

    //// set parameters in build net using "conf_file" as configure file
    public native static int wakeup_set_bn_parameter(String conf_file);

    //// (re)set all parameters in build net to default value
    public native static int wakeup_set_bn_parameter_default();

    /// 2.2 [snet] APIs

    //// set garbage number used
    public native static int wakeup_set_bn_garbage_num(int garbage_num);

    //// add single state filler or not
    public native static int wakeup_bn_add_filler(boolean add_filler);

    //// add multi state filler or not
    public native static int wakeup_set_bn_multi_filler(int multi_filler);

    //// set other path or file in build net, use them if you know their meanings
    public native static int wakeup_set_bn_dnn_file(String dnn_file);

    public native static int wakeup_set_bn_stat_file(String stat_file);

    public native static int wakeup_set_bn_cms_file(String cms_file);

    public native static int wakeup_set_bn_net_model(String net_model);

    public native static int wakeup_set_bn_patch_file(String patch_file);

    /// 2.3 [e2e] APIs

    //// set other path or file in build net, use them if you know their meanings
    public native static int wakeup_set_bl_dnn_file(String dnn_file);

    public native static int wakeup_set_bl_label_file(String label_file);

    public native static int wakeup_set_bl_cms_file(String cms_file);

    public native static int wakeup_set_bl_net_model(String net_model);

    // end of 2. APIs for custom wakeup build net

    /**
     * 3. APIs for custom wakeup process
     */

    /// 3.1 [shared] APIs

    /**
     * get dnn model version number (md5sum)
     * 
     * [[NOTICE]]
     * this method must be called after wakeup init!
     */
    public native static String wakeup_get_model_version(long wakeup);

    /**
     * init wakeup instance
     * 
     * if "wakeup.conf" exists in the same directory of model file, then it's used as configure file
     * 
     * return a handler of a wakeup instance
     * eg. long my_wakeup = wakeup_init("full/path/to/model_file");
     */
    public native static long wakeup_init(String model_file);

    /**
     * init wakeup instance and use "confFile" as configure file to set parameters
     * 
     * configure file format:
     * [CONF_NAME_1] : [CONF_VALUE_1]
     * [CONF_NAME_2] : [CONF_VALUE_2]
     */
    public native static long wakeup_conf_init(String model_file, String conf_file);

    /**
     * release(destroy) all resources used by wakeup instance
     * 
     * pass the handler of the wakeup instance to each function
     * eg. wakeup_release(my_wakeup);
     * 
     * [[SPECIAL ATTENTION]]
     * 
     * please check whether wakeup is 0 before wakeup_release,
     * and make wakeup = 0 after wakeup_release to avoid multi release
     */
    public native static void wakeup_release(long wakeup);

    //// reset wakeup instance
    public native static int wakeup_reset(long wakeup);

    /**
     * process wakeup on an instance
     * 
     * ret_vals must have length of at least [3]!
     * ret_vals[0]: ret. ret = 0: succeed; ret < 0: failed
     * ret_vals[1]: start_packid. start_packid == -1: no start packet detected; otherwise: start packet id
     * ret_vals[2]: end_packid. end_packid == -1: no end packet detected; otherwise: end packet id
     */
    public native static String wakeup_process(long wakeup, int pack_id, short[] data, int len, int[] ret_vals);

    /**
     * process wakeup, and get process time of each part
     * 
     * ret_vals must have length of at least [3]!
     * ret_vals[0]: ret. ret = 0: succeed; ret < 0: failed
     * ret_vals[1]: start_packid. start_packid == -1: no start packet detected; otherwise: start packet id
     * ret_vals[2]: end_packid. end_packid == -1: no end packet detected; otherwise: end packet id
     * 
     * process time of each part is stored in WakeUp_time time
     */
    public native static String wakeup_process_time(long wakeup, int pack_id, short[] data, int len, int[] ret_vals,
            WakeUp_time time);

    //// set each parameter in wakeup instance respectively, use them if you know their meanings
    public native static int wakeup_set_confidence(long wakeup, float confidence);

    public native static int wakeup_set_thread_num(long wakeup, int thread_num);

    public native static int wakeup_set_packet_len(long wakeup, int packet_len);

    public native static int wakeup_use_vad(long wakeup, boolean use_vad);

    public native static int wakeup_use_agc(long wakeup, boolean use_agc);

    public native static int wakeup_accumulate_packet(long wakeup, boolean acc_pack);

    /**
     * save max 10s record when wakeup
     * 
     * record will be saved in record_dir, with record_prefix+current_time.pcm as file name
     */
    public native static int wakeup_set_record_dir(long wakeup, String record_dir, String record_prefix);

    /**
     * save max 10s record to specified dir when wakeup
     * true: start save record, false: stop save record
     * 
     * record will be automatically written to specified dir when wakeup if you turn on the switch!
     */
    public native static int wakeup_save_record_to_file(long wakeup, boolean save_to_file);

    //// save record immediately to specified dir, no matter wakeup or not
    public native static int wakeup_save_record_to_file_now(long wakeup);

    /**
     * save max 10s record to specified memory
     * true: start save record, false: stop save record
     * 
     * record will NOT be automatically written to specified mem when wakeup if you turn on the switch!
     * unless you call wakeup_save_record_to_mem_now manually
     */
    public native static int wakeup_save_record_to_mem(long wakeup, boolean save_to_mem);

    /**
     * save record immediately to specified memory, no matter wakeup or not
     * 
     * return value: bytes actually write
     */
    public native static int wakeup_save_record_to_mem_now(long wakeup, long mem, int size_in_bytes);

    /**
     * get parameters from a wakeup instance, and print the values to file
     * 
     * output file format:
     * [CONF_NAME_1] : [CONF_VALUE_1]
     * [CONF_NAME_2] : [CONF_VALUE_2]
     */
    public native static int wakeup_print_parameter_file(long wakeup, String conf_file);

    //// get parameters from a wakeup instance, and print the values to stdout
    public native static int wakeup_print_parameter(long wakeup);

    /**
     * set parameters through configure file
     * 
     * configure file format:
     * [CONF_NAME_1] : [CONF_VALUE_1]
     * [CONF_NAME_2] : [CONF_VALUE_2]
     */
    public native static int wakeup_set_parameter(long wakeup, String conf_file);

    //// (re)set all parameters in wakeup instance to default value
    public native static int wakeup_set_parameter_default(long wakeup);

    /// 3.2 [snet] APIs

    //// set each parameter in wakeup instance respectively, use them if you know their meanings
    public native static int wakeup_set_garbage_score(long wakeup, float score);

    public native static int wakeup_set_keyword_score(long wakeup, float score);

    public native static int wakeup_set_post_prob_beam(long wakeup, float post_beam);

    public native static int wakeup_set_max_frame_gap(long wakeup, int max_frame_gap);

    public native static int wakeup_set_max_result_num(long wakeup, int max_res_num);

    public native static int wakeup_set_state_weights(long wakeup, int sw_type);

    public native static int wakeup_set_filler_topn(long wakeup, int topn);

    /// 3.3 [e2e] APIs

    //// set each parameter in wakeup instance respectively, use them if you know their meanings
    public native static int wakeup_set_silence_weight(long wakeup, float log_weight);

    // end of 3. APIs for custom wakeup process

    /**
     * 4. APIs for runtime test
     */

    /// 4.1 [shared] APIs
    
    /**
     * just for runtime test
     * 
     * path: path to all wave files
     * list: a list file contains all wave file names
     * silence: position of file "silence.raw"
     * model: position of file "model.awb"
     */
    public native static void wakeup_runtime_test(String path, String list, String silence, String model,
            int thread_num, WakeUp_time total_time);

    /// 4.2 [snet] APIs

    //// none
    
    /// 4.3 [e2e] APIs
    
    //// none
    
    // end of 4. APIs for runtime test

}