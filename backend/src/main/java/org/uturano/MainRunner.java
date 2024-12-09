package org.uturano;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MainRunner {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: MainRunner <codeDir> <skeletonDir> <junitFile>");
            return;
        }

        String codeDir = args[0];
        String skeletonDir = args[1];
        String junitFile = args[2];

        Map<String, Object> result = run(codeDir, skeletonDir, junitFile);

        // 输出结果到控制台
        System.out.println(result.get("output"));
    }

    public static Map<String, Object> run(String codeDir, String skeletonDir, String junitFile) {
        Map<String, Object> response = new HashMap<>();
        StringBuilder output = new StringBuilder();

        // 文件结构检查
        FileChecker fileChecker = new FileChecker(codeDir, skeletonDir);
        String fileCheckErrors;
        try {
            fileCheckErrors = fileChecker.getErrorMessages();
        } catch (IOException e) {
            fileCheckErrors = "Error during directory check: " + e.getMessage();
        }

        if (fileCheckErrors != null && !fileCheckErrors.trim().isEmpty()) {
            output.append("=== File Check Errors ===\n");
            output.append(fileCheckErrors).append("\n");
            output.append("Skipping JUnit tests because of file check errors.\n");

            response.put("output", output.toString());
            response.put("status", "error");
            return response;
        }

        // JUnit 测试
        Path javaFile = Paths.get(junitFile);
        Path outputDir = Paths.get("/path/to/tests/out");

        JUnitTestRunner testRunner = new JUnitTestRunner(javaFile, outputDir);

        Map<String, String> testResults;
        try {
            testResults = testRunner.getTestResults();
        } catch (Exception e) {
            output.append("Error during test run: ").append(e.getMessage()).append("\n");
            response.put("output", output.toString());
            response.put("status", "error");
            return response;
        }

        output.append("=== JUnit Test Results ===\n");
        for (Map.Entry<String, String> entry : testResults.entrySet()) {
            output.append("Test: ").append(entry.getKey()).append(" - ").append(entry.getValue()).append("\n");
        }

        // 分数计算
        Map<String, Integer> testWeights = Map.of(
                "testAddition", 30,
                "testSubtraction", 40,
                "testMultiplication", 20,
                "testDivision", 10
        );

        int totalScore = 0;
        int maxPossibleScore = 0;

        for (Map.Entry<String, Integer> entry : testWeights.entrySet()) {
            String testName = entry.getKey();
            int weight = entry.getValue();
            maxPossibleScore += weight;

            String result = testResults.get(testName);
            if (result != null && result.startsWith("Passed")) {
                totalScore += weight;
            }
        }

        output.append("=== Scoring ===\n");
        output.append("Your score is: ").append(totalScore).append("/").append(maxPossibleScore).append("\n");

        response.put("output", output.toString());
        response.put("status", "success");
        response.put("score", Map.of("total", totalScore, "max", maxPossibleScore));
        return response;
    }
}
