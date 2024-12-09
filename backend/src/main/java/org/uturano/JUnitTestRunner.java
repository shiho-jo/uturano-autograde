package org.uturano;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import javax.tools.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class JUnitTestRunner {
    private Path junitFilePath;        // path to JUnit test java file
    private Path outputDir;           // output dir for compiled classes
    private String junitClassName;    // class file name parsed from JUnit test class name
    private List<String> testMethodNames = new ArrayList<>(); // method names parsed from JUnit file
    private StringBuilder outputBuilder = new StringBuilder(); // to collect outputs
    private String outputMessages = null; // output messages to return
    Map<String, String> testResults = null; // test results for each method

    public JUnitTestRunner(Path junitFilePath, Path outputDir) {
        this.junitFilePath = junitFilePath;
        this.outputDir = outputDir;
    }

    /** dynamically parse class name and test methods using JavaParsing */
    private void extractClassAndTestMethods() throws IOException {
        JavaParsing javaParsing = new JavaParsing(junitFilePath.toString());
        javaParsing.parsing();

        List<String> classNames = javaParsing.getClasses();
        if (classNames.isEmpty()) {
            throw new IOException("No classes found in file: " + junitFilePath);
        }
        this.junitClassName = classNames.get(0);

        Map<String, List<String>> methods = javaParsing.getMethods();
        for (String methodName : methods.keySet()) {
            List<String> annotations = methods.get(methodName);
            if (annotations.contains("Test")) {
                testMethodNames.add(methodName);
            }
        }
    }

    /** Compile and run JUnit tests. */
    public void runTests() {
        try {
            if (!Files.exists(junitFilePath)) {
                throw new IOException("JUnit test file not found: " + junitFilePath);
            }

            extractClassAndTestMethods();
            compileJavaFile();

            Class<?> junitClass = Class.forName(junitClassName);
            Result result = JUnitCore.runClasses(junitClass);

            int runCount = result.getRunCount();
            int failCount = result.getFailureCount();
            int ignoreCount = result.getIgnoreCount();

            Map<String, String> testResultsMap = new HashMap<>();
            for (String testName : testMethodNames) {
                testResultsMap.put(testName, "Passed");
            }

            if (!result.wasSuccessful()) {
                for (Failure failure : result.getFailures()) {
                    String testName = failure.getTestHeader().split("\\(")[0];
                    String detailedMessage = failure.getMessage() != null ? failure.getMessage() : "No detailed message";
                    String trace = failure.getTrace();
                    testResultsMap.put(testName, "Failed: " + detailedMessage + "\nStack Trace: " + trace);
                }
            }

            this.testResults = testResultsMap;

            outputBuilder.append("JUnit Test Results:\n");
            for (Map.Entry<String, String> entry : testResultsMap.entrySet()) {
                outputBuilder.append("Test: ").append(entry.getKey()).append(" - ").append(entry.getValue()).append("\n");
            }

            outputBuilder.append("\nSummary:\n");
            outputBuilder.append("Total Tests: ").append(runCount).append("\n");
            outputBuilder.append("Passed: ").append(runCount - failCount - ignoreCount).append("\n");
            outputBuilder.append("Failed: ").append(failCount).append("\n");
            outputBuilder.append("Ignored: ").append(ignoreCount).append("\n");

        } catch (IOException e) {
            outputBuilder.append("Oops! An IO error occurred: ").append(e.getMessage()).append("\n");
        } catch (ClassNotFoundException e) {
            outputBuilder.append("Oops! Could not load class: ").append(e.getMessage()).append("\n");
        } catch (Exception e) {
            outputBuilder.append("Oops! An unexpected error occurred: ").append(e.getMessage()).append("\n");
        } finally {
            outputMessages = outputBuilder.toString();
        }
    }

    /** dynamically compile the Java file */
    private void compileJavaFile() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No JavaCompiler found. Ensure you are running this with a JDK, not a JRE.");
        }

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        int result = compiler.run(
                null, null, null,
                "-d", outputDir.toString(),
                junitFilePath.toString()
        );

        if (result != 0) {
            throw new IOException("Compilation failed with exit code: " + result);
        }
    }

    /** get results or error messages */
    public String getOutputs() {
        if (outputMessages == null) {
            runTests();
        }
        return outputMessages;
    }

    public Map<String, String> getTestResults() {
        if (testResults == null) {
            runTests();
        }
        return testResults;
    }
}