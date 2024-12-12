package org.uturano;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileChecker {
    private String codeDir;
    private String skeletonDir;
    private StringBuilder errorMessageBuilder = new StringBuilder();
    private String errorMessages = null;

    public FileChecker(String codeDir, String skeletonDir) {
        this.codeDir = codeDir;
        this.skeletonDir = skeletonDir;
    }

    public void compareDirectories() throws IOException {
        // scan skeleton and code directory
        JavaFileScanner skeletonScanner = new JavaFileScanner(skeletonDir);
        JavaFileScanner codeScanner = new JavaFileScanner(codeDir);

        List<String> skeletonFiles = skeletonScanner.listJavaFiles();
        List<String> codeFiles = codeScanner.listJavaFiles();

        // append error messages
        String skeletonScanErrors = skeletonScanner.getErrorMessages();
        String codeScanErrors = codeScanner.getErrorMessages();

        // scan errors
        if (skeletonScanErrors != null && !skeletonScanErrors.isEmpty()) {
            errorMessageBuilder.append("Error scanning skeleton directory:\n");
            errorMessageBuilder.append(skeletonScanErrors).append("\n");
        }

        if (codeScanErrors != null && !codeScanErrors.isEmpty()) {
            errorMessageBuilder.append("Error scanning code directory:\n");
            errorMessageBuilder.append(codeScanErrors).append("\n");
        }

        // find missing files
        List<String> missingFiles = new ArrayList<>();
        for (String skeletonPath : skeletonFiles) {
            boolean found = false;
            for (String codePath : codeFiles) {
                // Compare relative paths
                if (getRelativePath(skeletonDir, skeletonPath).equals(getRelativePath(codeDir, codePath))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingFiles.add(skeletonPath);
            }
        }

        // output error message for missing files
        if (!missingFiles.isEmpty()) {
            errorMessageBuilder.append("Missing corresponding code files for skeleton files:\n");
            for (String mf : missingFiles) {
                errorMessageBuilder.append("  - ").append(mf).append("\n");
            }
        }

        // only compare files that both exists
        for (String skeletonPath : skeletonFiles) {
            for (String codePath : codeFiles) {
                if (getRelativePath(skeletonDir, skeletonPath).equals(getRelativePath(codeDir, codePath))) {
                    ParseComparator comparator = new ParseComparator(codePath, skeletonPath);
                    String result = comparator.processComparison();

                    if (result != null && !result.trim().isEmpty()) {
                        errorMessageBuilder.append("Differences found in file: ")
                                .append(skeletonPath)
                                .append("\n");
                        errorMessageBuilder.append(result).append("\n");
                    }
                }
            }
        }

        this.errorMessages = errorMessageBuilder.toString();
    }

    public String getErrorMessages() throws IOException {
        if (this.errorMessages == null) {
            this.compareDirectories();
        }
        return this.errorMessages;
    }

    private String getRelativePath(String baseDir, String fullPath) {
        Path basePath = Paths.get(baseDir).toAbsolutePath();
        Path fullPathObj = Paths.get(fullPath).toAbsolutePath();
        return basePath.relativize(fullPathObj).toString();
    }
}
