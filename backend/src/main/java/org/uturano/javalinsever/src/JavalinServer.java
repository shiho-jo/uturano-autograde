package org.uturano.javalinsever.src;

import io.javalin.Javalin;
import io.javalin.http.UploadedFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

public class JavalinServer {

    public static void main(String[] args) {
        // 创建 Javalin 应用
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost())); // 跨域
            config.http.defaultContentType = "application/json"; // 默认 Content-Type
        }).start(8080);

        // 注册 API 路由
        app.post("/api/save-settings", JavalinServer::saveSettings);

        System.out.println("Server started on http://localhost:8080");
    }

    private static void saveSettings(io.javalin.http.Context ctx) {
        try {
            // 获取表单数据
            String assignmentName = ctx.formParam("assignmentName");
            String deadline = ctx.formParam("deadline");
            UploadedFile junitFile = ctx.uploadedFile("junitFile");
            List<UploadedFile> codeFiles = ctx.uploadedFiles("codeFiles[]");

            if (assignmentName == null || assignmentName.isBlank()) {
                ctx.status(400).json(Map.of("status", "error", "message", "Assignment Name is required."));
                return;
            }

            // 创建动态路径
            Path dynamicPath = Paths.get("uploads", assignmentName);
            if (!Files.exists(dynamicPath)) {
                Files.createDirectories(dynamicPath);
                System.out.println("Created directory: " + dynamicPath);
            }

            // 保存 JUnit 文件
            if (junitFile != null) {
                Path junitPath = dynamicPath.resolve(junitFile.filename());
                Files.copy(junitFile.content(), junitPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Saved JUnit file to: " + junitPath);
            }

            // 保存代码文件
            for (UploadedFile file : codeFiles) {
                Path codePath = dynamicPath.resolve(file.filename());
                Files.copy(file.content(), codePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Saved code file to: " + codePath);
            }

            // 返回成功响应
            ctx.json(Map.of(
                    "status", "success",
                    "message", "Settings saved successfully!",
                    "path", dynamicPath.toAbsolutePath().toString(),
                    "deadline", deadline
            ));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "status", "error",
                    "message", "Failed to save settings. Error: " + e.getMessage()
            ));
        }
    }
}
