package org.uturano.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JavaFileService {

    private static final String SOURCE_DIR = System.getProperty("user.dir") + "/outputDir";

    public void saveFile(MultipartFile file,Integer type,String md5Folder) throws IOException {
        String dir = SOURCE_DIR + File.separator + md5Folder;
        if (type == 1){
            dir += File.separator + "code";
        }else if (type == 2){
            dir += File.separator + "skeletons";
        }else{
            dir += File.separator + "tests";
        }
        System.out.println("folder:"+dir);
        Path dirPath = Paths.get(dir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        String filename = file.getOriginalFilename();
        String[] split = filename.split("/");
        Path path = Paths.get(dir, split[split.length-1]);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


        // 方便编译通过
//        if (type != 3){
//            String tempDir = SOURCE_DIR + File.separator + md5Folder + File.separator + "tests";
//            Path tempDirPath = Paths.get(tempDir);
//            if (!Files.exists(tempDirPath)) {
//                Files.createDirectories(tempDirPath);
//            }
//            String tempFileName = file.getOriginalFilename();
//            String[] splits = tempFileName.split("/");
//            Path tempPath = Paths.get(tempDir, splits[splits.length-1]);
//            Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);
//
//            // tests目录编译
//            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//            List<String> sourceFiles = new ArrayList<>();
//            sourceFiles.add(tempPath.toString());
//
//            // Create compilation arguments
//            String[] compilationArgs = new String[sourceFiles.size() + 2];
//            compilationArgs[0] = "-d";
//            compilationArgs[1] = tempDir.toString();
//            for (int i = 2; i < compilationArgs.length; i++) {
//                compilationArgs[i] = sourceFiles.get(i - 2);
//            }
//            // Compile files
//            int result = compiler.run(null, null, null, compilationArgs);
//            if (result != 0) {
//                throw new IOException("Compilation failed with exit code: " + result);
//            }
//        }
    }

    public void compileJavaFiles(Integer type,String folder) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No JavaCompiler found. Ensure you are running this with a JDK, not a JRE.");
        }
        String dir = SOURCE_DIR + File.separator + folder;
        if (type == 1){
            dir += File.separator + "code";
        }else if (type == 2){
            dir += File.separator + "skeletons";
        }else{
            dir += File.separator + "tests";
        }

        // Ensure output dir exists
        Path outputDir = Paths.get(dir);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Collect all Java files needed
        List<String> sourceFiles = Files.walk(Paths.get(dir))
                .filter(path -> path.toString().endsWith(".java"))
                .map(path -> path.toString())
                .collect(Collectors.toList());

        // Create compilation arguments
        String[] compilationArgs = new String[sourceFiles.size() + 2];
        compilationArgs[0] = "-d";
        compilationArgs[1] = outputDir.toString();
        for (int i = 2; i < compilationArgs.length; i++) {
            compilationArgs[i] = sourceFiles.get(i - 2);
        }

        // Compile files
        int result = compiler.run(null, null, null, compilationArgs);

        if (result != 0) {
            throw new IOException("Compilation failed with exit code: " + result);
        }
    }
}
