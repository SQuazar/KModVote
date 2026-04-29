plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

group = "net.nullpointer.kmodvote"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.jda) {
        exclude("opus-java")
        exclude("tink")
    }
    implementation(libs.quartz)
    implementation(libs.caffeine)
    implementation(libs.mongo)
    implementation(libs.bson)
    implementation(libs.kotlinx.datetime)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.serialization)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveBaseName = "KModVote"
    archiveClassifier = ""
    version = "${project.version}"

    manifest {
        attributes["Main-Class"] = "net.nullpointer.kmodvote.MainKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
}