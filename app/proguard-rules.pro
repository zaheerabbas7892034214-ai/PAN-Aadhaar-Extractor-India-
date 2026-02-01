# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep data classes used for Room and JSON serialization
-keepclassmembers class com.panaadhaar.extractor.india.data.model.** { *; }
-keepclassmembers class com.panaadhaar.extractor.india.data.database.** { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep Billing classes
-keep class com.android.billingclient.** { *; }
