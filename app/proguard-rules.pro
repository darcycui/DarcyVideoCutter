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

## dylancaicoding.viewbinding 反射保护
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
  public static * inflate(android.view.LayoutInflater);
  public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
  public static * bind(android.view.View);
}

# 显式保留 Parent 类及其子类的泛型信息
-keep class com.darcy.videocutter.ui.base.BaseBindingActivity { *; }
#-keep class * extends com.darcy.videocutter.ui.base.BaseBindingActivity { *; }
-keep class com.darcy.videocutter.ui.base.BaseBindingFragment { *; }
-keep class com.darcy.videocutter.ui.base.BaseBindingDialog { *; }

