plugins {
    application
    java
}

group = "org.uturano"
version = "1.0-Ihumke-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.uturano.Main")
}

dependencies {
    // Using JavaParser and JavaSymbolSolver to do code comparison
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.26.2")
    implementation("junit:junit:4.13.2")

    // Javalin
    implementation ("io.javalin:javalin:5.3.0")

    // log
    implementation ("org.slf4j:slf4j-simple:2.0.7")

    // JSON processing
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.14.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}
