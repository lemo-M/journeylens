# ====================================================
# JourneyLens ProGuard Rules
# ====================================================

# --- 基础设置 ---
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# --- Kotlin ---
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# --- Kotlinx Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}
-keep,includedescriptorclasses class com.lm.journeylens.**$$serializer { *; }
-keepclassmembers class com.lm.journeylens.** {
    *** Companion;
}
-keepclasseswithmembers class com.lm.journeylens.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- Room ---
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# --- Koin (依赖注入，重度使用反射) ---
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.dsl.** { *; }
-keepclassmembers class * {
    public <init>(...);
}

# --- Voyager 导航 ---
-keep class cafe.adriel.voyager.** { *; }
-keep class * extends cafe.adriel.voyager.core.screen.Screen { *; }
-keep class * extends cafe.adriel.voyager.core.model.ScreenModel { *; }

# --- Coil 图片加载 ---
-keep class coil3.** { *; }
-dontwarn coil3.**

# --- 高德地图 SDK (关键：防止 JNI 找不到类) ---
-keep class com.amap.api.** { *; }
-keep class com.autonavi.** { *; }
-keep class com.loc.** { *; }
-keep class com.amap.** { *; }
-dontwarn com.amap.api.**
-dontwarn com.autonavi.**
-dontwarn com.loc.**

# 高德地图内部混淆类 (col = core obfuscated layer)
-keep class com.amap.api.col.** { *; }
-keep class com.amap.api.maps.** { *; }
-keep class com.amap.api.trace.** { *; }
-keep class com.amap.api.navi.** { *; }
-keep class com.amap.api.location.** { *; }
-keep class com.amap.api.services.** { *; }

# 保留所有 JNI 本地方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# --- 应用数据类 (防止序列化/Room 查询失败) ---
-keep class com.lm.journeylens.core.domain.model.** { *; }
-keep class com.lm.journeylens.core.database.entity.** { *; }
-keep class com.lm.journeylens.feature.memory.AddMemoryUiState { *; }

# --- 所有 ScreenModel 和 Screen 类 (Voyager 通过反射查找) ---
-keep class * extends cafe.adriel.voyager.core.screen.Screen { *; }
-keep class * extends cafe.adriel.voyager.core.model.ScreenModel { *; }
-keep class com.lm.journeylens.feature.** { *; }

# --- 保留枚举类 ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
