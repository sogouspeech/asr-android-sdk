package com.sogou.sogouspeech.translate;

public interface ITranslatorInterface {

    public void initTranslation(String fromLanguage, String toLanguage);

    public int startTranslation(String text, String fromLanguage, String toLanguage);

    public void releaseTranslation();

    public interface TransaltionCallback {
        void onTranslationResultCb(boolean sentenceFinal, String origintext, String resultText, int sessionIndex, int sessionID);
        void onTranslationErrorCb(String errInfo, int sessionID);
        void onTranslationInit();
    }
}
