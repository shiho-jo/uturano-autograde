package org.uturano;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
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
        this.codeDir = codeDir;
        this.junitFilePath = junitFilePath;
        this.outputDir = outputDir;
    }

    /** Compile codeDir and JUnit test file */
    private void compileJavaFiles() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No JavaCompiler found. Ensure you are running this with a JDK, not a JRE.");
        }

        // Ensure output dir exists
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Collect all Java files needed
        List<String> sourceFiles = new ArrayList<>();
        Files.walk(codeDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> sourceFiles.add(path.toString()));

        // Add JUnit test file
        sourceFiles.add(junitFilePath.toString());

        // Create compilation arguments
        List<String> args = new ArrayList<>();
        args.add("-d");
        args.add(outputDir.toString());
        args.addAll(sourceFiles);

        // Compile files
        int result = compiler.run(null, null, null, args.toArray(new String[0]));
        if (result != 0) {
            throw new IOException("Compilation failed with exit code: " + result);
        }
    }

    /** Run JUnit tests */
    public void runTests() {
        try {
            compileJavaFiles(); // Compile Java files before running tests

            // Load compiled classes
            ClassLoader loader = URLClassLoader.newInstance(new URL[]{outputDir.toUri().toURL()});
            String junitClassName = junitFilePath.getFileName().toString().replace(".java", "");
            Class<?> junitClass = loader.loadClass(junitClassName);

            // Run JUnit tests
            System.out.println("Running JUnit tests for class: " + junitClassName);
            Result result = JUnitCore.runClasses(junitClass);

            // Collect results
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
