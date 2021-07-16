import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
}

group = "com.nltv"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    api("net.mamoe", "mirai-core", "2.6.2")
    implementation("com.beust:klaxon:5.5")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.2")
    implementation("io.ktor:ktor-client-core:1.6.1")
    implementation("io.ktor:ktor-client-cio:1.6.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.20")
    implementation( "org.jsoup:jsoup:1.14.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "14"
}

/*val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "NLBot2021"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "MainKt"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}*/

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>(){
    archiveBaseName.set("NLBot2021")
    archiveClassifier.set("")
    archiveVersion.set("1.0")
    manifest{
        attributes["Implementation-Title"] = "NLBot2021"
        attributes["Implementation-Version"] = archiveVersion.get()
        attributes["Main-Class"] = "MainKt"
    }
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}


application{
    mainClass.set("MainKt")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
