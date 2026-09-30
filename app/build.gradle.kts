plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
val vp=java.util.Properties().apply { rootProject.file("app/version.properties").inputStream().use { load(it) } }
android {
 namespace="kr.ledoa.cut2"; compileSdk=35
 defaultConfig { applicationId="kr.ledoa.cut2"; minSdk=26; targetSdk=35; versionCode=vp.getProperty("VERSION_CODE").toInt(); versionName=vp.getProperty("VERSION_NAME") }
 buildFeatures { viewBinding=true }
}
dependencies {
 implementation("androidx.core:core-ktx:1.15.0")
 implementation("androidx.appcompat:appcompat:1.7.0")
 implementation("com.google.android.material:material:1.12.0")
 implementation("androidx.media3:media3-exoplayer:1.5.1")
 implementation("androidx.media3:media3-ui:1.5.1")
}