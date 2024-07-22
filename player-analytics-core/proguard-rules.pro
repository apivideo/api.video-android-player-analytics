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

-repackageclasses com.example.avdt

-keep class video.api.player.analytics.core.payload.Event {
    public *;
}

-keep enum video.api.player.analytics.core.payload.Event$EventType {
    public *;
}

-keep class video.api.player.analytics.core.payload.Event$Companion {
    public *;
}

-keep class video.api.player.analytics.core.payload.ErrorCode {
    public *;
}

-keep class video.api.player.analytics.core.ApiVideoPlayerAnalyticsAgent {
    public *;
}

-keep class video.api.player.analytics.core.ApiVideoPlayerAnalyticsAgent$Companion {
    public *;
}