package org.uturano;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Main <codeDir> <skeletonDir> <junitFile> [outputDir]");
            return;
        }

        // Get directories and JUnit file from command-line arguments
        String codeDir = args[0];
        String skeletonDir = args[1];
        String junitFilePath = args[2];

        String outputDir;
        if (args.length > 3) {
            outputDir = args[3];
        } else {
            outputDir = System.getProperty("user.dir") + "/outputDir";
        }

        // Ensure the JUnit file is valid
        File junitFile = new File(junitFilePath);
        if (!junitFile.exists() || !junitFile.isFile() || !junitFilePath.endsWith(".java")) {
            System.out.println("=== File Validation ===");
            System.out.println("Error: Invalid JUnit file provided: " + junitFilePath);
            System.out.println("Skipping further processing due to invalid file.");
            return;
        }

        // Ensure output directory exists
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // Perform file checking
        System.out.println("=== File Check ===");
        System.out.println("Checking code and skeleton directories...");
        FileChecker fileChecker = new FileChecker(codeDir, skeletonDir);
        String fileCheckErrors;
        try {
            fileCheckErrors = fileChecker.getErrorMessages();
        } catch (IOException e) {
            System.out.println("Error occurred during file checking: " + e.getMessage());
            return;
        }

        if (fileCheckErrors != null && !fileCheckErrors.trim().isEmpty()) {
            System.out.println("=== File Check Errors ===");
            System.out.println(fileCheckErrors);
            System.out.println("Skipping JUnit tests because of file check errors.");
            return;
        }

        System.out.println("File check passed. Proceeding to JUnit test execution...");

        // Parse annotations from the JUnit file to build a score map
        System.out.println("=== JUnit Test File Parsing ===");
        System.out.println("Parsing JUnit test file for methods with @Test annotation...");
        Map<String, Integer> scoreMap = getScoreMapFromAnnotations(junitFilePath);

        if (scoreMap.isEmpty()) {
            System.out.println("No methods with @Test annotation found in the JUnit file.");
            System.out.println("Score: 0");
        } else {
            System.out.println("=== Test Score Allocation ===");
            for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
                System.out.println("Test: " + entry.getKey() + " - Score: " + entry.getValue());
            }
        }

        // Compile and run JUnit tests
        try {
            Path codePath = Paths.get(codeDir);
            Path junitPath = Paths.get(junitFilePath);
            Path outputPath = Paths.get(outputDir);

            JUnitTestRunner testRunner = new JUnitTestRunner(codePath, junitPath, outputPath);
            testRunner.runTests();

            // Output results
            System.out.println("=== JUnit Test Results ===");
            System.out.println(testRunner.getOutputs());
        } catch (Exception e) {
            System.out.println("Error during JUnit execution: " + e.getMessage());
        }
    }

    /**
     * Parses the JUnit test file to extract methods annotated with @Test and assigns default scores.
     *
     * @param junitFilePath Path to the JUnit file.
     * @return A map of test method names to their allocated scores.
     */
    private static Map<String, Integer> getScoreMapFromAnnotations(String junitFilePath) {
        Map<String, Integer> scoreMap = new TreeMap<>();
        JavaParsing parser = new JavaParsing(junitFilePath);

        try {
            TreeMap<String, List<String>> annotations = parser.getAnnotations(); // Get method annotations
            for (Map.Entry<String, List<String>> entry : annotations.entrySet()) {
                String methodName = entry.getKey();
                List<String> annotationNames = entry.getValue();

                // Check for @Test annotation
                if (annotationNames.contains("Test")) {
                    int defaultScore = 10; // Assign a default score for each test method
                    scoreMap.put(methodName, defaultScore);
                }
            }
        } catch (IOException e) {
            System.out.println("Error parsing JUnit file: " + e.getMessage());
        }

        return scoreMap;
    }
}
