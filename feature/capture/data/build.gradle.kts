plugins {
    id("joycon.android.library")
}

android {
    namespace = "com.joegec.joycon2android.capture.data"
}

dependencies {
    implementation(project(":feature:capture:domain"))
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
}
