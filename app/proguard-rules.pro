# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep WebView @JavascriptInterface bridges across R8 release minification
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.example.ui.home.ScreensaverVideoBridge {
    *;
}
-keep class com.example.ui.games.CustomBookmarkWebView$* {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.example.ui.games.CustomWebPortalScreenKt$* {
    @android.webkit.JavascriptInterface <methods>;
}

# Strip verbose/debug log calls in release builds for smaller DEX & zero log overhead
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Preserve source file and line numbers for readable release crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
