package org.uturano.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.uturano.api.common.R;
import org.uturano.api.service.JavaFileService;
import org.uturano.api.utils.FileChecker;
import org.uturano.api.utils.JUnitTestRunner;
import org.uturano.api.utils.JavaParsing;
import org.uturano.api.utils.MD5Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequestMapping("/api")
@ResponseBody
@Controller
public class FileUploadAndScoreController {
    private static final String SOURCE_DIR = System.getProperty("user.dir") + "/outputDir";

    @Autowired
    private JavaFileService javaFileService;

    /**
     * .java File Upload And Compile(D:/sources)
     *
     * @param files
     * @return
     */
    @PostMapping("/upload")
    public R uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("type") Integer type, String folder) throws IOException {
        String md5Folder;
        if (type == 1) {
            // save map relation
            StringBuffer stringBuffer = new StringBuffer();
            files.forEach(item -> {
                stringBuffer.append(item.getOriginalFilename());
            });
            String bufferString = stringBuffer.toString();
            md5Folder = MD5Util.toMD5(bufferString);
            // Ensure output dir exists
            Path outputDir = Paths.get(SOURCE_DIR + File.separator + md5Folder);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
        } else {
            md5Folder = folder;
        }


        try {
            for (MultipartFile file : files) {
                javaFileService.saveFile(file, type, md5Folder);
            }
//            javaFileService.compileJavaFiles(type, md5Folder);
//            return ResponseEntity.ok("Files uploaded and compiled successfully.");
            R r = R.builder().status(true).message(md5Folder).build();
            return r;
        } catch (Exception e) {
            e.printStackTrace();
//            return ResponseEntity.badRequest().body("Error during file upload or compilation: " + e.getMessage());
            R r = R.builder().status(false).message(md5Folder).build();
            return r;
        }
    }

    /**
     * calc scores
     * Usage: java Main <codeDir> <skeletonDir> <junitFile> [outputDir]
     *
     * @return
     */
    @PostMapping("/score")
    public R calcScores(String folder) throws IOException, ClassNotFoundException {
        R r = new R();
        // Get directories and JUnit file from command-line arguments
        String codeDir = SOURCE_DIR + File.separator + folder + File.separator + "code";
        String skeletonDir = SOURCE_DIR + File.separator + folder + File.separator + "skeleton";
        String junitDirPath = SOURCE_DIR + File.separator + folder + File.separator + "tests";
        StringBuilder junitFilePath = new StringBuilder(junitDirPath);
        File temp = new File(junitDirPath);
        if (temp.exists() && temp.isDirectory()) {
            File[] files = temp.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".java")) {
                        junitFilePath.append(File.separator).append(file.getName());
                    }
                }
            }
        }

        String outputDir = System.getProperty("user.dir") + "/outputDir";

        // Ensure the JUnit file is valid
        File junitFile = new File(junitFilePath.toString());
        if (!junitFile.exists() || !junitFile.isFile() || !junitFilePath.toString().endsWith(".java")) {
            System.out.println("=== File Validation ===");
            System.out.println("Error: Invalid JUnit file provided: " + junitFilePath);
            System.out.println("Skipping further processing due to invalid file.");
            r.setMessage("Error: Invalid JUnit file provided.");
            return r;
        }

        // Ensure output directory exists
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        Map<String, Object> result1 = r.getResult();
        // Perform file checking
        System.out.println("=== File Check ===");
        System.out.println("Checking code and skeleton directories...");
        FileChecker fileChecker = new FileChecker(codeDir, skeletonDir);
        String fileCheckErrors;
        try {
            fileCheckErrors = fileChecker.getErrorMessages();
        } catch (IOException e) {
            System.out.println("Error occurred during file checking: " + e.getMessage());
            return R.builder().status(false).message("Error occurred during file checking.").build();
        }

        if (fileCheckErrors != null && !fileCheckErrors.trim().isEmpty()) {
            System.out.println("=== File Check Errors ===");
            System.out.println(fileCheckErrors);
            System.out.println("Skipping JUnit tests because of file check errors.");
            return R.builder().status(false).message("Skipping JUnit tests because of file check errors.").build();
        }

        System.out.println("File check passed. Proceeding to JUnit test execution...");
        result1.put("File Check","File check passed. Proceeding to JUnit test execution...");

        // Parse annotations from the JUnit file to build a score map
        System.out.println("=== JUnit Test File Parsing ===");
        System.out.println("Parsing JUnit test file for methods with @Test annotation...");
        result1.put("JUnit Test File Parsing","Parsing JUnit test file for methods with @Test annotation...");
        Map<String, Integer> scoreMap = getScoreMapFromAnnotations(junitFilePath.toString());

        if (scoreMap.isEmpty()) {
            System.out.println("No methods with @Test annotation found in the JUnit file.");
            System.out.println("Score: 0");
        } else {
            System.out.println("=== Test Score Allocation ===");
            for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
                System.out.println("Test: " + entry.getKey() + " - Score: " + entry.getValue());
                Map<String, Object> result = r.getResult();
                result.put("Test: " + entry.getKey(), entry.getValue());
                Object totalScore = result.get("TotalScore");
                if (totalScore != null) {
                    result.put("TotalScore", (int) totalScore + entry.getValue());
                } else {
                    result.put("TotalScore", entry.getValue());
                }
            }
        }

        // Compile and run JUnit tests
//        try {
        Path codePath = Paths.get(codeDir);
        Path junitPath = Paths.get(junitFilePath.toString());
        Path outputPath = Paths.get(outputDir);

        JUnitTestRunner testRunner = new JUnitTestRunner(codePath, junitPath, outputPath);
        testRunner.runTests(r,folder);

        // Output results
        System.out.println("=== JUnit Test Results ===");
        System.out.println(testRunner.getOutputs());
        result1.put("JUnit Test Results", testRunner.getOutputs());
//        } catch (Exception e) {
//            System.out.println("Error during JUnit execution: " + e.getMessage());
//        }
        r.setStatus(true);
        return r;
    }

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

//    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        // Get directories and JUnit file from command-line arguments
//        String codeDir = "D:\\sources\\Mainstudent111.java";
//        String skeletonDir = "D:\\sources\\Mainstudent111framework.java";
//        String junitFilePath = "D:\\sources\\student111Maintest.java";
//
//        String outputDir = System.getProperty("user.dir") + "/outputDir";
//
//        // Ensure the JUnit file is valid
//        File junitFile = new File(junitFilePath);
//        if (!junitFile.exists() || !junitFile.isFile() || !junitFilePath.endsWith(".java")) {
//            System.out.println("=== File Validation ===");
//            System.out.println("Error: Invalid JUnit file provided: " + junitFilePath);
//            System.out.println("Skipping further processing due to invalid file.");
//        }
//
//        // Ensure output directory exists
//        File outputDirectory = new File(outputDir);
//        if (!outputDirectory.exists()) {
//            outputDirectory.mkdirs();
//        }
//
//        // Perform file checking
//        System.out.println("=== File Check ===");
//        System.out.println("Checking code and skeleton directories...");
//        FileChecker fileChecker = new FileChecker(codeDir, skeletonDir);
//        String fileCheckErrors = "";
//        try {
//            fileCheckErrors = fileChecker.getErrorMessages();
//        } catch (IOException e) {
//            System.out.println("Error occurred during file checking: " + e.getMessage());
//        }
//
//        if (fileCheckErrors != null && !fileCheckErrors.trim().isEmpty()) {
//            System.out.println("=== File Check Errors ===");
//            System.out.println(fileCheckErrors);
//            System.out.println("Skipping JUnit tests because of file check errors.");
//        }
//
//        System.out.println("File check passed. Proceeding to JUnit test execution...");
//
//        // Parse annotations from the JUnit file to build a score map
//        System.out.println("=== JUnit Test File Parsing ===");
//        System.out.println("Parsing JUnit test file for methods with @Test annotation...");
//        Map<String, Integer> scoreMap = getScoreMapFromAnnotations(junitFilePath);
//
//        if (scoreMap.isEmpty()) {
//            System.out.println("No methods with @Test annotation found in the JUnit file.");
//            System.out.println("Score: 0");
//        } else {
//            System.out.println("=== Test Score Allocation ===");
//            for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
//                System.out.println("Test: " + entry.getKey() + " - Score: " + entry.getValue());
//            }
//        }
//
//        // Compile and run JUnit tests
////        try {
//        Path codePath = Paths.get(codeDir);
//        Path junitPath = Paths.get(junitFilePath);
//        Path outputPath = Paths.get(outputDir);
//
//        JUnitTestRunner testRunner = new JUnitTestRunner(codePath, junitPath, outputPath);
//        testRunner.runTests();
//
//        // Output results
//        System.out.println("=== JUnit Test Results ===");
//        System.out.println(testRunner.getOutputs());
////        } catch (Exception e) {
////            System.out.println("Error during JUnit execution: " + e.getMessage());
////        }
//    }
}
