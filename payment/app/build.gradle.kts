plugins { id("com.android.application") }
android {
  namespace = "kr.ledoa.indopayment"
  compileSdk = 35
  defaultConfig {
    applicationId = "kr.ledoa.indopayment"
    minSdk = 29
    targetSdk = 35
    versionCode = 1
    versionName = "1.0.0"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
