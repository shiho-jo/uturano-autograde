import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.Test;

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
                    System.out.println("Passed Methods: " + testResult.passedMethodNames);
                    System.out.println("Failed Methods: " + testResult.failedMethodNames);
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

    // Run test cases using JUnit programmatically
    private static TestResult runTests(File sandboxDir, String studentId) throws Exception {
        String testClassName = studentId + "." + studentId + "Maintest";

        // 使用URLClassLoader从sandbox中加载类
        URL[] urls = new URL[]{sandboxDir.toURI().toURL()};
        try (URLClassLoader classLoader = new URLClassLoader(urls, AutoGradingSystem.class.getClassLoader())) {
            Class<?> testClass = classLoader.loadClass(testClassName);

            // 获取所有的@Test方法
            List<String> testMethodNames = getTestMethods(testClass);

            // 使用JUnitCore运行测试
            JUnitCore junit = new JUnitCore();
            org.junit.runner.Result result = junit.run(testClass);

            int totalTests = result.getRunCount();
            int failedCount = result.getFailureCount();
            int passedCount = totalTests - failedCount;

            // 获取失败的方法名列表
            Set<String> failedMethods = new HashSet<>();
            for (Failure f : result.getFailures()) {
                // 从Description中获取方法名
                String methodName = f.getDescription().getMethodName();
                failedMethods.add(methodName);
            }

            // 确定通过的方法列表
            List<String> passedMethods = new ArrayList<>();
            for (String m : testMethodNames) {
                if (!failedMethods.contains(m)) {
                    passedMethods.add(m);
                }
            }

            // 返回结果
            return new TestResult(passedCount, totalTests, passedMethods, new ArrayList<>(failedMethods));
        }
    }

    // 通过反射获取测试类中所有标注了@Test的方法名称
    private static List<String> getTestMethods(Class<?> testClass) {
        List<String> testMethods = new ArrayList<>();
        for (Method m : testClass.getDeclaredMethods()) {
            for (Annotation a : m.getAnnotations()) {
                if (a.annotationType().equals(Test.class)) {
                    testMethods.add(m.getName());
                }
            }
        }
        return testMethods;
    }

    // Parse test results with details
    private static class TestResult {
        int passedTests;
        int totalTests;
        List<String> passedMethodNames;
        List<String> failedMethodNames;

        TestResult(int passedTests, int totalTests, List<String> passedMethodNames, List<String> failedMethodNames) {
            this.passedTests = passedTests;
            this.totalTests = totalTests;
            this.passedMethodNames = passedMethodNames;
            this.failedMethodNames = failedMethodNames;
        }
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
