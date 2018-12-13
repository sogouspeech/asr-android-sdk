// Copyright 2018 Sogou Inc. All rights reserved.
// Use of this source code is governed by the Apache 2.0
// license that can be found in the LICENSE file.
package com.sogou.sogouspeech.utils;

import com.sogou.speech.asr.v1.SpeechRecognitionAlternative;
import com.sogou.speech.asr.v1.StreamingRecognitionResult;
import com.sogou.speech.asr.v1.WordInfo;

import java.util.List;

public class RecognizeResultUtils {

    public static List<WordInfo> getWordInfo(StreamingRecognitionResult result){
        if (result.getAlternativesCount() > 0){
            SpeechRecognitionAlternative alternative = result.getAlternatives(0);
            return alternative.getWordsList();
        }

        return null;
    }

    public static float getConfidence(StreamingRecognitionResult result){
        if (result.getAlternativesCount() > 0){
            SpeechRecognitionAlternative alternative = result.getAlternatives(0);
            return alternative.getConfidence();
        }

        return 0;

    }
}
