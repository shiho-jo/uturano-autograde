package org.uturano;

import io.javalin.Javalin;

public class JavalinServer {

    public static void main(String[] args) {
        // 初始化 Javalin
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
        }).start(8080);

        // 注册路由
        app.post("/api/run-mainrunner", MainRunnerHandler::handle);

        System.out.println("Javalin server started on http://localhost:8080");
    }
}
