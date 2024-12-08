package org.uturano;
import java.io.IOException;
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
        // 使用JavaFileScanner掃描 skeleton 及 code 目錄
        JavaFileScanner skeletonScanner = new JavaFileScanner(skeletonDir);
        JavaFileScanner codeScanner = new JavaFileScanner(codeDir);

        List<String> skeletonFiles = skeletonScanner.listSkeletonFiles();
        List<String> codeFiles = codeScanner.listSkeletonFiles();

        // 若掃描過程中有錯誤訊息，合併至 errorMessageBuilder
        String skeletonScanErrors = skeletonScanner.getErrorMessages();
        if (skeletonScanErrors != null && !skeletonScanErrors.isEmpty()) {
            errorMessageBuilder.append("Error scanning skeleton directory:\n");
            errorMessageBuilder.append(skeletonScanErrors).append("\n");
        }

        String codeScanErrors = codeScanner.getErrorMessages();
        if (codeScanErrors != null && !codeScanErrors.isEmpty()) {
            errorMessageBuilder.append("Error scanning code directory:\n");
            errorMessageBuilder.append(codeScanErrors).append("\n");
        }

        // 直接使用完整路徑來比較
        // 找出 skeleton 中有但 code 中沒有的檔案
        List<String> missingFromCode = new ArrayList<>();
        for (String skeletonPath : skeletonFiles) {
            if (!codeFiles.contains(skeletonPath)) {
                missingFromCode.add(skeletonPath);
            }
        }

        // 找出 code 中有但 skeleton 沒有的檔案
        List<String> extraInCode = new ArrayList<>();
        for (String codePath : codeFiles) {
            if (!skeletonFiles.contains(codePath)) {
                extraInCode.add(codePath);
            }
        }

        // 輸出遺漏檔案的訊息
        if (!missingFromCode.isEmpty()) {
            errorMessageBuilder.append("Missing corresponding code files for skeleton files:\n");
            for (String mf : missingFromCode) {
                errorMessageBuilder.append("  - ").append(mf).append("\n");
            }
        }

        if (!extraInCode.isEmpty()) {
            errorMessageBuilder.append("Code directory has extra files not present in skeleton:\n");
            for (String ef : extraInCode) {
                errorMessageBuilder.append("  - ").append(ef).append("\n");
            }
        }

        // 對於同時存在 skeleton 與 code 的檔案進行比對
        for (String skeletonPath : skeletonFiles) {
            if (codeFiles.contains(skeletonPath)) {
                // skeletonPath 與 codePath 相同，表示完整路徑比對
                // 若需要對應不同目錄結構，需在此對 skeletonPath 做路徑轉換以取得對應的 codePath
                String codePath = skeletonPath; // 由於已是同路徑，此處直接使用

                ParseComparator comparator = new ParseComparator(codePath, skeletonPath);
                String result = comparator.processComparison();

                // 若 result 有錯誤訊息，加入errorMessageBuilder
                if (result != null && !result.trim().isEmpty()) {
                    errorMessageBuilder.append("Differences found in file: ")
                            .append(skeletonPath)
                            .append("\n");
                    errorMessageBuilder.append(result).append("\n");
                }
            }
        }

        this.errorMessages = errorMessageBuilder.toString();

        public String getErrorMessages() throws IOException {
            if (this.errorMessages == null) {
                this.compareDirectories();
            }
            return this.errorMessages;
        }
}
