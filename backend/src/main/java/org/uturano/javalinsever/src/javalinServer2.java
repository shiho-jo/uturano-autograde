import io.javalin.Javalin;

public class JavalinServer {
    public static void main(String[] args) {
        // 初始化 Javalin 应用
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json"; // 默认返回 JSON
            config.enableDevLogging(); // 开启开发日志
        }).start(8080); // 启动服务，监听 8080 端口

        // 添加路由示例
        app.get("/", ctx -> ctx.result("Hello, Javalin!")); // 首页
        app.post("/api/save-settings", ctx -> {
            String assignmentName = ctx.formParam("assignmentName");
            ctx.json(Map.of("status", "success", "message", "Assignment Name: " + assignmentName));
        });

        System.out.println("Server started on http://localhost:8080");
    }
}
