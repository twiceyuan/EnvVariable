import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import org.gradle.internal.impldep.aQute.bnd.osgi.Constants.options
import org.gradle.internal.impldep.com.amazonaws.PredefinedClientConfigurations.defaultConfig

plugins {
    id("com.android.library")
    id("com.github.dcendents.android-maven")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(Versions.target_sdk_version)
    defaultConfig {
        minSdkVersion(Versions.min_sdk_version)
        targetSdkVersion(Versions.target_sdk_version)
        versionCode = 1
        versionName = "1.0"
        resourcePrefix("env_var_")
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin_version}")
    implementation("com.android.support:appcompat-v7:${Versions.support_version}")
    implementation("com.android.support.constraint:constraint-layout:1.1.3")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}

// build a jar with source files
// 指定编码
tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF-8"
}

task<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    classifier = "sources"
}

task<Javadoc>("javadoc") {
    isFailOnError = false
    source = android.sourceSets["main"].java.sourceFiles
    classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
    classpath += configurations.compile
}

// build a jar with javadoc
task<Jar>("javadocJar") {
    dependsOn("javadoc")
    classifier = "javadoc"
    from("javadoc.destinationDir")
}

artifacts {
    add("archives", tasks.getByName("sourcesJar"))
    add("archives", tasks.getByName("javadocJar"))
}