# Jetpack Glance ActionCallback zero-argument constructors must be preserved for reflection
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}
