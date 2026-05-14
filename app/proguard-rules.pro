# Add project specific ProGuard rules here.
-keep class com.clawpet.domain.** { *; }
-keep class com.clawpet.data.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn dagger.hilt.**