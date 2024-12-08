import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class SandboxManager {

    public static void runSandbox(String studentId, File studentDir, String testClassName) {
        System.out.println("Running sandbox for student: " + studentId);

        // Define the output directory for compiled files
        String outputDirPath = "out/production/autograde3";
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Prepare the compilation command
        List<String> compileCommand = new ArrayList<>();
        compileCommand.add("javac");
        compileCommand.add("-d");
        compileCommand.add(outputDirPath);
        compileCommand.add("-cp");
        compileCommand.add(outputDirPath + ":" + studentDir.getAbsolutePath() + ":/Users/mohanpei/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar");

        // Get all .java files in the student's directory
        File[] javaFiles = studentDir.listFiles((dir, name) -> name.endsWith(".java"));
        if (javaFiles == null || javaFiles.length == 0) {
            System.out.println("No Java files found in student directory: " + studentDir.getAbsolutePath());
            return;
        }

        for (File javaFile : javaFiles) {
            compileCommand.add(javaFile.getAbsolutePath());
        }

        System.out.println("Compile command: " + String.join(" ", compileCommand));

        try {
            ProcessBuilder pb = new ProcessBuilder(compileCommand);
            Process compileProcess = pb.start();

            // Capture compilation output and error messages
            StreamGobbler errorGobbler = new StreamGobbler(compileProcess.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(compileProcess.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();

            int compileExitCode = compileProcess.waitFor();

            // Wait for StreamGobbler threads to finish
            errorGobbler.join();
            outputGobbler.join();

            if (compileExitCode != 0) {
                System.out.println("Compilation failed for student: " + studentId);
                return;
            }
        } catch (Exception e) {
            System.out.println("Error during compilation: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Check if the compiled class file exists
        File testClassFile = new File(outputDirPath + "/student1/" + testClassName + ".class");
        if (!testClassFile.exists()) {
            System.out.println("Compiled test class not found: " + testClassFile.getAbsolutePath());
            return;
        }

        // Execute the test code
        try {
            File classesDir = new File(outputDirPath);
            URL[] classLoaderUrls = new URL[]{
                    classesDir.toURI().toURL(),
                    new File("/Users/mohanpei/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar").toURI().toURL()
            };
            URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls, SandboxManager.class.getClassLoader());

            // Load the test class using a custom class loader
            Class<?> testClass = urlClassLoader.loadClass("student1." + testClassName);
            Result result = JUnitCore.runClasses(testClass);

            // Generate the test report
            generateReport(studentId, result);

            // Close the class loader
            urlClassLoader.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Test class not found: student1." + testClassName);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception during testing");
            e.printStackTrace();
        }
    }

    private static void generateReport(String studentId, Result result) {
        File reportFile = new File("grading_results.log");

        try (FileWriter writer = new FileWriter(reportFile, true)) {
            writer.write("Student: " + studentId + "\n");
            writer.write("Tests Run: " + result.getRunCount() + "\n");
            writer.write("Failures: " + result.getFailureCount() + "\n");

            for (Failure failure : result.getFailures()) {
                writer.write("Failure: " + failure.toString() + "\n");
            }

            writer.write("=====================================\n");
        } catch (IOException e) {
            System.out.println("Error writing report: " + e.getMessage());
        }
    }

    // Helper class for handling input streams
    private static class StreamGobbler extends Thread {
        private InputStream inputStream;
        private String streamType;

        public StreamGobbler(InputStream inputStream, String streamType) {
            this.inputStream = inputStream;
            this.streamType = streamType;
        }

        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(streamType + "> " + line);
                }
            } catch (IOException e) {
                System.out.println("Exception in StreamGobbler: " + e.getMessage());
            }
        }
    }
}



