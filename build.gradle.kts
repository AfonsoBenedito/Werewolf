plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    application
}

group = "com.afonsobenedito"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    config.setFrom("detekt.yml")
    autoCorrect = project.hasProperty("detektAutoCorrect")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.afonsobenedito.werewolf.web.WerewolfApplicationKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

springBoot {
    mainClass.set("com.afonsobenedito.werewolf.web.WerewolfApplicationKt")
}

tasks.register<JavaExec>("runConsole") {
    group = "application"
    description = "Runs the console application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.afonsobenedito.werewolf.console.ConsoleApplicationKt")
    standardInput = System.`in`
}