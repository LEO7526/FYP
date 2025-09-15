# Preserve Firebase classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Preserve FirebaseUI classes
-keep class com.firebase.ui.** { *; }
-dontwarn com.firebase.ui.**

# Preserve Glide classes and generated modules
-keep class com.bumptech.glide.** { *; }
-keep class com.bumptech.glide.GeneratedAppGlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule
-dontwarn com.bumptech.glide.**

# Preserve Kotlin metadata
-keep class kotlin.Metadata
-dontwarn kotlin.**

# Preserve multidex support
-keep class android.support.multidex.** { *; }

# Preserve WebView JavaScript interface (replace with your actual class name if used)
#-keepclassmembers class com.app.android.june.easyorder4u.MyWebInterface {
#   public *;
#}

# Preserve ViewBinding and DataBinding classes
-keep class **Binding { *; }

# Preserve CircleImageView
-keep class de.hdodenhof.circleimageview.** { *; }

# Preserve Picasso
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**

# Preserve OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Preserve Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Preserve Dexter
-keep class com.karumi.dexter.** { *; }
-dontwarn com.karumi.dexter.**

# Preserve Compressor
-keep class id.zelory.compressor.** { *; }
-dontwarn id.zelory.compressor.**

# Preserve NineOldAndroids
-keep class com.nineoldandroids.** { *; }
-dontwarn com.nineoldandroids.**

# Preserve Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Preserve line number info for crash reports
-keepattributes SourceFile,LineNumberTable

# Optional: hide original source file names
-renamesourcefileattribute SourceFile