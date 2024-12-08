package org.uturano;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AutoGradingSystem {

    static final int TOTAL_SCORE = 100;

    public static void main(String[] args) {
        // Root directory containing student code
        File studentRootDir = new File("src");

        // Retrieve all student folders
        File[] studentDirs = studentRootDir.listFiles(file -> file.isDirectory() && file.getName().matches("student\\d+"));
        System.out.println("Current working directory: " + new File(".").getAbsolutePath());
        if (studentDirs == null || studentDirs.length == 0) {
            System.out.println("No student code folders found. Check the path or folder names.");
            return;
        }

        // Iterate through each student folder
        for (File studentDir : studentDirs) {
            String studentId = studentDir.getName();
            try {
                // Create a sandbox environment
                File sandboxDir = createSandbox(studentDir);

                // Compile student code
                boolean compileSuccess = compileStudentCode(sandboxDir, studentId);

                if (compileSuccess) {
                    // Run test cases
                    TestResult testResult = runTests(sandboxDir, studentId);

                    int passedTests = testResult.passedTests;
                    int totalTests = testResult.totalTests;

                    // Calculate the score
                    double score = ((double) passedTests / totalTests) * TOTAL_SCORE;

                    // Output results
                    System.out.println("Student ID: " + studentId);
                    System.out.println("Passed Tests: " + passedTests);
                    System.out.println("Total Tests: " + totalTests);
                    System.out.println("Score: " + String.format("%.3f", score));
                    System.out.println("-------------------------");
                } else {
                    System.out.println("Student ID: " + studentId);
                    System.out.println("Compilation failed. Unable to grade.");
                    System.out.println("-------------------------");
                }

                // Delete sandbox
                deleteDirectory(sandboxDir);

            } catch (Exception e) {
                System.out.println("Error occurred while testing student ID: " + studentId);
                e.printStackTrace();
            }
        }
    }

    // Create a sandbox environment and copy student code into it
    private static File createSandbox(File studentDir) throws IOException {
        File sandboxDir = new File("sandbox/" + studentDir.getName());
        if (!sandboxDir.exists()) {
            sandboxDir.mkdirs();
        }
        // Copy student code to sandbox
        Files.walk(studentDir.toPath())
                .forEach(source -> {
                    try {
                        Path destination = sandboxDir.toPath().resolve(studentDir.toPath().relativize(source));
                        if (Files.isDirectory(source)) {
                            if (!Files.exists(destination)) {
                                Files.createDirectories(destination);
                            }
                        } else {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        return sandboxDir;
    }

    // Compile student code
    private static boolean compileStudentCode(File sandboxDir, String studentId) throws IOException, InterruptedException {
        // Dynamically adapt filenames
        File mainFile = new File(sandboxDir, studentId + "Main.java");
        if (!mainFile.exists()) {
            mainFile = new File(sandboxDir, "Main" + studentId + ".java");
        }
        File testFile = new File(sandboxDir, studentId + "Maintest.java");
        if (!testFile.exists()) {
            testFile = new File(sandboxDir, "Maintest" + studentId + ".java");
        }

        // Check if files exist
        if (!mainFile.exists() || !testFile.exists()) {
            System.err.println("Compilation failed: Missing files - " + mainFile.getName() + " or " + testFile.getName());
            return false;
        }

        // Get JUnit library paths
        String junitPath = "/Users/mohanpei/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar";
        String hamcrestPath = "/Users/mohanpei/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar";

        // Execute the compilation command, set classpath, and specify output directory
        ProcessBuilder pb = new ProcessBuilder(
                "javac",
                "-d", sandboxDir.getAbsolutePath(),
                "-cp", junitPath + File.pathSeparator + hamcrestPath,
                mainFile.getAbsolutePath(),
                testFile.getAbsolutePath()
        );
        pb.directory(sandboxDir);
        Process process = pb.start();

        // Read compilation output and error messages
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = outputReader.readLine()) != null) {
            System.out.println(line);
        }
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }

        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    // Run test cases
    private static TestResult runTests(File sandboxDir, String studentId) throws IOException, InterruptedException {
        // Get JUnit library paths
        String junitPath = "/Users/mohanpei/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar";
        String hamcrestPath = "/Users/mohanpei/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar";

        // Build classpath
        String classpath = sandboxDir.getAbsolutePath() + File.pathSeparator + junitPath + File.pathSeparator + hamcrestPath;

        // Use fully qualified class name (package + class name)
        String testClassName = studentId + "." + studentId + "Maintest";

        // Use JUnitCore to run tests
        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-cp", classpath,
                "org.junit.runner.JUnitCore",
                testClassName
        );
        pb.directory(sandboxDir);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Read program output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder outputBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            outputBuilder.append(line).append("\n");
        }
        reader.close();

        int exitCode = process.waitFor();

        // Output test results (optional)
        System.out.println("Test Output:\n" + outputBuilder.toString());

        // Parse test results
        return parseTestResults(outputBuilder.toString());
    }

    // Parse test results
    private static class TestResult {
        int passedTests;
        int totalTests;

        TestResult(int passedTests, int totalTests) {
            this.passedTests = passedTests;
            this.totalTests = totalTests;
        }
    }

    private static TestResult parseTestResults(String output) {
        int passedTests = 0;
        int totalTests = 0;
        try {
            for (String line : output.split("\n")) {
                line = line.trim();
                if (line.startsWith("OK (")) {
                    // Parse the number of passed tests, e.g., "OK (2 tests)"
                    String testsStr = line.substring(4, line.length() - 1).trim(); // Extract "2 tests"
                    String[] parts = testsStr.split(" ");
                    totalTests = Integer.parseInt(parts[0]);
                    passedTests = totalTests;
                } else if (line.startsWith("Tests run:")) {
                    // Parse test run summary, e.g., "Tests run: 2, Failures: 1"
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        part = part.trim();
                        if (part.startsWith("Tests run:")) {
                            totalTests = Integer.parseInt(part.split(":")[1].trim());
                        } else if (part.startsWith("Failures:")) {
                            int failures = Integer.parseInt(part.split(":")[1].trim());
                            passedTests = totalTests - failures;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing test results: " + e.getMessage());
        }
        return new TestResult(passedTests, totalTests);
    }

    // Delete sandbox directory
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}




