# Keep kotlinx.serialization metadata for serializable models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.nunoapps.cartelli.data.off.** {
    kotlinx.serialization.KSerializer serializer(...);
}
