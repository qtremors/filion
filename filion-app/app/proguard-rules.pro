# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers and source file names for stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*, InnerClasses

# Preserve all native methods across the application
-keepclasseswithmembernames class * {
    native <methods>;
}

# Google Filament & gltfio native bridge & bindings
-keep class com.google.android.filament.** { *; }
-dontwarn com.google.android.filament.**

# SceneView 3D rendering library
-keep class io.github.sceneview.** { *; }
-dontwarn io.github.sceneview.**

# Filion application domain and UI models
-keep class dev.qtremors.filion.** { *; }
