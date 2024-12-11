
plugins {
    application
    java
}

dependencies {
    implementation("io.javalin:javalin:5.6.0") // 替换为 Javalin 的最新版本
}
group = "org.uturano"
version = "1.0-Ihumke-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.uturano.Main") // Specify the main class
}

dependencies {
    // Use JavaParser and JavaSymbolSolver for code parsing and comparison
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.26.2")

    // implementation("com.google.code.gson:gson:2.10.1")

    // Use JUnit 4 and JUnit 5 for testing
    implementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.register<JavaExec>("runWithArgs") {
    group = "application"
    description = "Run the program with arguments"

    mainClass.set("org.uturano.Main")
    classpath = sourceSets["main"].runtimeClasspath

    // Dynamically set arguments using project properties
    val codeDir = project.findProperty("codeDir") as? String ?: "default/codeDir"
    val skeletonDir = project.findProperty("skeletonDir") as? String ?: "default/skeletonDir"
    val junitFile = project.findProperty("junitFile") as? String ?: "default/JUnitFile.java"
    val outputDir = project.findProperty("outputDir") as? String ?: "default/outputDir"

    // Validate that junitFile ends with .java
    if (!junitFile.endsWith(".java")) {
        throw GradleException("The provided junitFile argument must be a .java file: $junitFile")
    }

    args = listOf(codeDir, skeletonDir, junitFile, outputDir)

    doFirst {
        println("Running with arguments:")
        println("Code Directory: $codeDir")
        println("Skeleton Directory: $skeletonDir")
        println("JUnit File: $junitFile")
        println("Output Directory: $outputDir")
    }
}
