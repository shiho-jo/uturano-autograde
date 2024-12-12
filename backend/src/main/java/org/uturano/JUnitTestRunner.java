package org.uturano;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JUnitTestRunner {
    private Path codeDir;             // Directory containing Java code
    private Path junitFilePath;       // Path to JUnit test Java file
    private Path outputDir;           // Output directory for compiled classes
    private StringBuilder outputMessages = new StringBuilder(); // Collect output messages
    private Map<String, String> testResults = new HashMap<>();  // Test results

    public JUnitTestRunner(Path codeDir, Path junitFilePath, Path outputDir) {
        this.codeDir = codeDir.toAbsolutePath();
        this.junitFilePath = junitFilePath.toAbsolutePath();
        this.outputDir = outputDir.toAbsolutePath();
    }

    /** Compile codeDir and JUnit test file */
    private void compileJavaFiles() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No JavaCompiler found. Ensure you are running this with a JDK, not a JRE.");
        }

        // Ensure output directory exists
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Collect all Java files in codeDir using JavaFileScanner
        JavaFileScanner scanner = new JavaFileScanner(codeDir.toString());
        List<String> sourceFiles = scanner.listJavaFiles();

        // Add JUnit test file to the list
        if (Files.exists(junitFilePath)) {
            sourceFiles.add(junitFilePath.toString());
        } else {
            throw new IOException("JUnit test file not found: " + junitFilePath);
        }

        // Create compilation arguments
        List<String> args = new ArrayList<>();
        args.add("-d"); // Specify output directory
        args.add(outputDir.toString());
        args.addAll(sourceFiles);

        // Compile files
        System.out.println("Compiling the following files:");
        for (String file : sourceFiles) {
            System.out.println(" - " + file);
        }
        int result = compiler.run(null, null, null, args.toArray(new String[0]));
        if (result != 0) {
            throw new IOException("Compilation failed with exit code: " + result);
        }

        System.out.println("Compilation successful. Output directory: " + outputDir);
    }

    public void runTests() {
        try {
            compileJavaFiles(); // Compile Java files before running tests

            // Ensure the test class is compiled
            String junitClassName = junitFilePath.getFileName().toString().replace(".java", "");
            Path compiledClassPath = outputDir.resolve(junitClassName + ".class");
            if (!Files.exists(compiledClassPath)) {
                throw new IOException("Compiled class not found: " + compiledClassPath);
            }

            // Use a custom ClassLoader to load the compiled class
            ClassLoader loader = new java.net.URLClassLoader(new java.net.URL[]{outputDir.toUri().toURL()});
            Class<?> junitClass;
            try {
                junitClass = loader.loadClass(junitClassName); // Load class assuming no package
            } catch (ClassNotFoundException e) {
                throw new IOException("Failed to load the test class. Ensure it is compiled and in the correct output directory.", e);
            }

            // Run JUnit tests
            System.out.println("Running JUnit tests for class: " + junitClassName);
            Result result = JUnitCore.runClasses(junitClass);

            // Collect test results
            for (Failure failure : result.getFailures()) {
                String testName = failure.getTestHeader();
                String reason = failure.getMessage();
                testResults.put(testName, "Failed: " + reason);
            }

            int passedCount = result.getRunCount() - result.getFailureCount();
            int failedCount = result.getFailureCount();

            outputMessages.append("Run count: ").append(result.getRunCount()).append("\n");
            outputMessages.append("Passed count: ").append(passedCount).append("\n");
            outputMessages.append("Failed count: ").append(failedCount).append("\n");
        } catch (Exception e) {
            outputMessages.append("Error running tests: ").append(e.getMessage()).append("\n");
        }
    }


    /** Returns the collected output messages */
    public String getOutputs() {
        return outputMessages.toString();
    }

    /** Returns the test results as a map of test names to results */
    public Map<String, String> getTestResults() {
        return testResults;
    }
}
