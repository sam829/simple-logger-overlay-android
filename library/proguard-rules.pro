# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified in the
# Android SDK tools proguard-rules.pro file.

# Keep public library API
-keep public class in.sammacwan.logger.LoggerOverlay { *; }
-keep public class in.sammacwan.logger.config.** { *; }
-keep public class in.sammacwan.logger.core.** { *; }
-keep public class in.sammacwan.logger.network.** { *; }
-keep public class in.sammacwan.logger.telemetry.** { *; }
-keep public class in.sammacwan.logger.framework.** { *; }
