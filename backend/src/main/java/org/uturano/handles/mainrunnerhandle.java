package org.uturano.handlers;

import io.javalin.http.Context;
import org.uturano.MainRunner;

import java.util.Map;

public class MainRunnerHandler {
    public static void handle(Context ctx) {
        String codeDir = ctx.formParam("codeDir");
        String skeletonDir = ctx.formParam("skeletonDir");
        String junitFile = ctx.formParam("junitFile");

        if (codeDir == null || skeletonDir == null || junitFile == null) {
            ctx.status(400).json(Map.of("error", "Missing required parameters: codeDir, skeletonDir, junitFile"));
            return;
        }

        Map<String, Object> result = MainRunner.run(codeDir, skeletonDir, junitFile);

        if ("error".equals(result.get("status"))) {
            ctx.status(500).json(result);
        } else {
            ctx.json(result);
        }
    }
}

