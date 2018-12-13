# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# -keep关键字
# keep：包留类和类中的成员，防止他们被混淆
# keepnames:保留类和类中的成员防止被混淆，但成员如果没有被引用将被删除
# keepclassmembers :只保留类中的成员，防止被混淆和移除。
# keepclassmembernames:只保留类中的成员，但如果成员没有被引用将被删除。
# keepclasseswithmembers:如果当前类中包含指定的方法，则保留类和类成员，否则将被混淆。
# keepclasseswithmembernames:如果当前类中包含指定的方法，则保留类和类成员，如果类成员没有被引用，则会被移除。
-optimizationpasses 5
-dontoptimize
-dontusemixedcaseclassnames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod
-dontpreverify
-verbose

# Preserve all annotations.
-keepattributes *Annotation*

# 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.sogou.sogouspeech.SogoSpeech{
    public <methods>;
}

-keep class com.sogou.sogouspeech.SogoSpeechSettings{
    public <methods>;
}

-keep class com.sogou.sogouspeech.paramconstants.SpeechConstants$ErrorDomain{
  public <fields>;
   public <methods>;
}
-keep class com.sogou.sogouspeech.paramconstants.SpeechConstants$Message{
  public <fields>;
   public <methods>;
}

-keep class com.sogou.sogouspeech.paramconstants.SpeechConstants$Command{
  public <fields>;
   public <methods>;
}

-keep class com.sogou.sogouspeech.paramconstants.SpeechConstants$Parameter{
  public <fields>;
   public <methods>;
}

-keepnames class com.sogou.sogouspeech.paramconstants.LanguageCode$*{
   public <fields>;
      public <methods>;
}
-keep class com.sogou.sogouspeech.paramconstants.LanguageCode$ASRLanguageCode{
   public *;
}

-keep class com.sogou.sogouspeech.EventListener {
    *;
}

-keep class com.sogou.sogouspeech.auth.TokenFetchTask {
    public *;
}

-keep interface com.sogou.sogouspeech.auth.TokenFetchTask$TokenFetchListener{
    *;
}
-keep class com.sogou.sogouspeech.paramconstants.SpeechConstants{
    *;
}

-keep class com.sogou.sogocommon.utils.CommonUtils{
    public *;
    public <methods>;
}

-keep class com.sogou.sogocommon.utils.ShortByteUtil{
    public *;
    public <methods>;
}

-keep class com.sogou.sogocommon.utils.FileUtils{
    public *;
    public <methods>;
}

-keep class com.sogou.sogocommon.vad.VadTask{
    public *;
    public <methods>;
}

-keep class com.sogou.sogocommon.utils.SogoConstants{
    public *;
     public <methods>;
}


-ignorewarnings