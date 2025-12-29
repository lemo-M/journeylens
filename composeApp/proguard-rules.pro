# Banner
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Compose
-keep class androidx.compose.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keep,allowobfuscation,allowshrinking class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Voyager
-keep class cafe.adriel.voyager.** { *; }

# Koin
-keep class org.koin.** { *; }

# Coil
-keep class coil3.** { *; }

# Data Classes (Optional, but safe for serialization/room)
-keep class com.lm.journeylens.core.domain.model.** { *; }
-keep class com.lm.journeylens.core.database.entity.** { *; }
