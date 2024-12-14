# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile



-keep class com.google.firebase.** { *; }
-keep class * extends com.google.firebase.**
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Application

-keepclassmembers class * {
    public void onClick(android.view.View);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclassmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclassmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * {
    private static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepattributes Signature
-keepattributes *Annotation*


#-keep public class com.android.vending.licensing.ILicensingService

-keepclassmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keep class mobilesecurity.sit.project.userauth.** { *; }
-keep class mobilesecurity.sit.project.mainpage.MainActivity

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}


-keepclassmembers class * {
    native <methods>;
    public static final java.lang.String TAG;
}



-keepclassmembers class * {
    public void *(android.view.View);
}

-keepclassmembers class * {
    public static final java.lang.String TAG;
}


-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.okhttp.**


 -keepclasseswithmembers class * {
     public <init>(android.content.Context, android.util.AttributeSet, int);
 }

 -keepclassmembers class * {
     public void *(android.view.View);
 }

 -keepclassmembers enum * {
     public static **[] values();
     public static ** valueOf(java.lang.String);
 }

 -keep class * implements android.os.Parcelable {
     public static final android.os.Parcelable$Creator *;
 }

 -keep class mobilesecurity.sit.project.parents.DataClass { *; }
 -keep class mobilesecurity.sit.project.parents.TeacherInfoDataClass { *; }

-keep class mobilesecurity.sit.project.parents.ParentKidsActivity { *; }
-keep class mobilesecurity.sit.project.parents.TeachersActivity { *; }
-keep class mobilesecurity.sit.project.parents.TeacherDetailActivity { *; }
-keep class mobilesecurity.sit.project.parents.MyAdapter { *; }
-keep class mobilesecurity.sit.project.parents.TeacherAdapter { *; }


-keepclassmembers class * {
    public void *(android.view.View);
}

-keep public class * extends androidx.recyclerview.widget.RecyclerView$Adapter { *; }
-keep public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(android.view.View);
}


-keepclassmembers class * {
    @com.google.firebase.firestore.* <fields>;
    @com.google.firebase.firestore.* <methods>;
}

-keep class mobilesecurity.sit.project.chats.conversations.Conversation { *; }
-keep class mobilesecurity.sit.project.chats.messages.Message { *; }
-keep class mobilesecurity.sit.project.chats.conversations.Participant { *; }

-keep class mobilesecurity.sit.project.chats.messages.MessageAdapter { *; }
-keep class mobilesecurity.sit.project.chats.conversations.ConversationAdapter { *; }
-keep class mobilesecurity.sit.project.chats.conversations.NewGroupConversationAdapter { *; }
-keep class mobilesecurity.sit.project.chats.conversations.*ViewHolder { *; }

-keep class mobilesecurity.sit.project.chats.conversations.ConversationActivity { *; }
-keep class mobilesecurity.sit.project.chats.conversations.NewConversationActivity { *; }
-keep class mobilesecurity.sit.project.chats.conversations.NewGroupConversationActivity { *; }


-keepclassmembers class * {
    public void *(android.view.View);
}

-keep class mobilesecurity.sit.project.invoices.CreditCard { *; }
-keep class mobilesecurity.sit.project.invoices.Invoice { *; }

-keep class mobilesecurity.sit.project.invoices.InvoiceDetailActivity { *; }
-keep class mobilesecurity.sit.project.invoices.InvoicesActivity { *; }
-keep class mobilesecurity.sit.project.invoices.CreditCardAdapter { *; }
-keep class mobilesecurity.sit.project.invoices.InvoiceAdapter { *; }

-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

