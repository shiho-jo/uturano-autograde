package org.uturano;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Main {
	public static void main(String[] args) {
		// Student's code is in /path/to/code/
		// Skeleton code is in /path/to/skeleton/
		String codeDir = "/path/to/code";
		String skeletonDir = "/path/to/skeleton";

		// Check the directory structure
		FileChecker fileChecker = new FileChecker(codeDir, skeletonDir);
		String fileCheckErrors = null;
		try {
			fileCheckErrors = fileChecker.getErrorMessages();
		} catch (IOException e) {
			// Handle errors while checking directories
			fileCheckErrors = "Error during directory check: " + e.getMessage();
		}

		if (fileCheckErrors != null && !fileCheckErrors.trim().isEmpty()) {
			// If there are errors, show them and stop
			System.out.println("=== File Check Errors ===");
			System.out.println(fileCheckErrors);
			System.out.println("Skipping JUnit tests because of file check errors.");
			return;
		}

		// If no errors, move to JUnit tests
		Path javaFile = Paths.get("/path/to/tests/JUnitFile.java");
		Path outputDir = Paths.get("/path/to/tests/out");

		JUnitTestRunner testRunner = new JUnitTestRunner(javaFile, outputDir);

		// Run the tests and get results
		Map<String, String> testResults = null;
		try {
			testResults = testRunner.getTestResults();
		} catch (Exception e) {
			// Handle errors during test execution
			System.out.println("Error during test run: " + e.getMessage());
			return;
		}

		// Show the results for each test
		System.out.println("=== JUnit Test Results ===");
		for (Map.Entry<String, String> entry : testResults.entrySet()) {
			System.out.println("Test: " + entry.getKey() + " - " + entry.getValue());
		}

		// Define test weights
		Map<String, Integer> testWeights = Map.of(
				"testAddition", 30,
				"testSubtraction", 40,
				"testMultiplication", 20,
				"testDivision", 10
		);

		int totalScore = 0;
		int maxPossibleScore = 0;

		// Calculate the score
		for (Map.Entry<String, Integer> entry : testWeights.entrySet()) {
			String testName = entry.getKey();
			int weight = entry.getValue();
			maxPossibleScore += weight;

			String result = testResults.get(testName);
			if (result != null && result.startsWith("Passed")) {
				totalScore += weight;
			}
		}

		// Show the final score
		System.out.println("=== Scoring ===");
		System.out.println("Your score is: " + totalScore + "/" + maxPossibleScore);
	}
}
