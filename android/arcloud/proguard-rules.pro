# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
# http://developer.android.com/guide/developing/tools/proguard.html

-printmapping out.map
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod

# Preserve all annotations.
-keepattributes *Annotation*

# Preserve selected public classes, and their public fields and methods.
-keep public class com.graffity.arcloud.ar.ARCloudFragment {
    public *;
}
-keep public class io.github.sceneview.node.Node {
    public *;
}

# For removing all System.out.println
-assumenosideeffects class java.io.PrintStream {
     public void println(%);
     public void println(**);
     public void print(%);
     public void print(**);
}