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

#-keep class *.R -keepclasseswithmembers class *.R$* { public static <fields>;}
## 不混淆 KuaihuoCountManager方法
#-keep class com.kuaihuo.data.count.KuaihuoCountManager{
#    <fields>;
#    <methods>;
#}
#-keep class com.kuaihuo.data.count.AbsModelRunHelper{
#    <fields>;
#    <methods>;
#}
#-keep class com.kuaihuo.data.count.AbsModelRealRunHelper{
#    <fields>;
#    <methods>;
#}
#-keep class com.kuaihuo.data.count.beans.*
#-keep class com.kuaihuo.data.count.configs.*
#-keep class com.kuaihuo.data.count.utils.*
#-keep class com.kuaihuo.data.count.enums.*
#
# #不要混淆MySuperBean所有子类的属性与方法
##-keepclasseswithmembers class * extends MySuperBean{
##    <fields>;
##    <methods>;
##}
#-keep class com.kuaihuo.data.count.enums.* {
#    <fields>;
#    <methods>;
#}
#-keep class com.kuaihuo.data.count.ext.ActivityExtKt{
#    <methods>;
#}
#-keep class com.kuaihuo.data.count.ext.ObservableExtKt{
#    <methods>;
#}
#-keep class com.kuaihuo.data.count.ext.FragmentExtKt{
#    <methods>;
#}

-optimizationpasses 9
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-ignorewarnings
-keep public class * {
  public protected *;
}