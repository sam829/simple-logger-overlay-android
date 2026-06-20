# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified in the
# Android SDK tools proguard-rules.pro file.

# Keep public library API
-keep public class com.debugtools.logger.LoggerOverlay { *; }
-keep public class com.debugtools.logger.config.** { *; }
-keep public class com.debugtools.logger.core.** { *; }
-keep public class com.debugtools.logger.network.** { *; }
-keep public class com.debugtools.logger.telemetry.** { *; }
-keep public class com.debugtools.logger.framework.** { *; }
