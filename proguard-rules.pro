-keepattributes Annotation, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

# Keep Serializers

-keep,includedescriptorclasses class com.jerryjeon.logjerry.**$$serializer { *; }
-keepclassmembers class com.jerryjeon.logjerry.** {
    *** Companion;
}
-keepclasseswithmembers class com.jerryjeon.logjerry.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# When kotlinx.serialization.json.JsonObjectSerializer occurs

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}