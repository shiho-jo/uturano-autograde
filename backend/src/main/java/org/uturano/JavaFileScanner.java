package org.uturano;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class JavaFileScanner {
    private String topDir; // absolute path to string dir
    private List<String> javaFiles = null; // list of java files found in the directory
    private String errorMessages = null; // String to store error messages
    private StringBuilder errorMessageBuilder = new StringBuilder(); // builder object for error messages

    public JavaFileScanner(String topDir) {
        this.topDir = topDir;
        this.javaFiles = new ArrayList<>();
        this.errorMessages = ""; // Error message string initialization
    }

    private void findJavaFiles() throws IOException {
        Path dir = Paths.get(topDir); // path object for NIO operations
        if (!Files.isDirectory(dir)) {
            throw new IOException(this.topDir + " is not a directory");
        }
        findJavaFiles(dir); // Call the recursive helper method
        this.errorMessages = errorMessageBuilder.toString();
    }

    /** helper method to recursively iterate through sub-paths */
    private void findJavaFiles(Path dir) throws IOException {
        DirectoryStream<Path> stream = null; // use stream to optimise performance
        try {
            stream = Files.newDirectoryStream(dir);
            for (Path path : stream) {
                // Skip hidden files (e.g., macOS Finder metadata, etc.)
                if (path.getFileName().toString().startsWith(".")) {
                    continue;
                }

                if (Files.isDirectory(path)) {
                    // Sub-dir, recursively find its files
                    findJavaFiles(path);
                } else if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java")) {
                    // Add .java file path to the list
                    javaFiles.add(path.toAbsolutePath().toString());
                }
            }
        } catch (IOException e) {
            // Append error message if directory reading fails
            errorMessageBuilder.append("Oops! Error occurs when reading directory ")
                    .append(dir).append(": ").append(e.getMessage()).append("\n");
        } finally {
            if (stream != null) {
                try {
                    stream.close(); // Manually close the stream
                } catch (IOException e) {
                    errorMessageBuilder.append("Oops! Closing stream failed with error: ").append(e.getMessage()).append("\n");
                }
            }
        }
    }

    /** Method to return the list of paths to all .java files */
    public List<String> listSkeletonFiles() throws IOException {
        if (javaFiles.isEmpty() && errorMessages == null) {
           this.findJavaFiles();
        }
        return javaFiles;
    }

    /** Method to get error messages during file scanning */
    public String getErrorMessages() throws IOException {
        if (this.errorMessages == null) {
            this.findJavaFiles();
        }
        return errorMessages;
    }
}

