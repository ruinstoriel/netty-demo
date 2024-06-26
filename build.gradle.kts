plugins {
    kotlin("jvm") version "1.9.22"
    id("org.graalvm.buildtools.native") version "0.10.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
// https://mvnrepository.com/artifact/io.netty.incubator/netty-incubator-codec-native-quic
    implementation("io.netty.incubator:netty-incubator-codec-native-quic:0.0.64.Final")

    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:4.1.111.Final")
// https://mvnrepository.com/artifact/ch.qos.logback/logback-core
    implementation("ch.qos.logback:logback-core:1.5.6")
// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.6")
// https://mvnrepository.com/artifact/org.bouncycastle/bcpkix-jdk18on
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set("Main.kt")
        }
    }
}