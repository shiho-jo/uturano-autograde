package org.uturano;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class JavaFileScanner {
    private String topDir; // Absolute path to directory
    private List<String> javaFiles = null; // List of .java files
    private StringBuilder errorMessageBuilder = new StringBuilder(); // Error messages builder

    public JavaFileScanner(String topDir) {
        this.topDir = topDir;
        this.javaFiles = new ArrayList<>(); // Initialize file list
    }

    // Main method to find .java files
    private void findJavaFiles() throws IOException {
        Path dir = Paths.get(topDir); // Get path to top directory
        if (!Files.isDirectory(dir)) {
            throw new IOException(this.topDir + " is not a directory");
        }
        findJavaFiles(dir); // Call helper method to scan recursively
    }

    // Recursive method to scan for .java files
    private void findJavaFiles(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) { // Use try-with-resources for safe closing
            for (Path path : stream) {
                // Skip hidden files or directories (those starting with ".")
                if (path.getFileName().toString().startsWith(".")) {
                    continue;
                }

                if (Files.isDirectory(path)) {
                    // Recursively scan sub-directories
                    findJavaFiles(path);
                } else if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java")) {
                    // Add .java file path to the list
                    javaFiles.add(path.toAbsolutePath().toString());
                }
            }
        } catch (IOException e) {
            // Log errors during directory traversal
            errorMessageBuilder.append("Error reading directory ")
                    .append(dir).append(": ").append(e.getMessage()).append("\n");
        }
    }

    // Public method to get list of .java files
    public List<String> listJavaFiles() throws IOException {
        if (javaFiles.isEmpty()) { // Only scan if list is empty
            this.findJavaFiles();
        }
        return javaFiles;
    }

    // Public method to get error messages during scanning
    public String getErrorMessages() {
        return errorMessageBuilder.toString(); // Return all collected errors
    }
}
