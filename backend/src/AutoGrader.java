import java.io.File;

public class AutoGrader {
    public static void main(String[] args) {
        // Define the upload directory
        File uploadDir = new File(System.getProperty("user.dir") + "/src");

        // Check whether the directory exists
        if (!uploadDir.exists()) {
            System.out.println("Upload directory does not exist: " + uploadDir.getAbsolutePath());
            return;
        }

        File[] files = uploadDir.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in directory: " + uploadDir.getAbsolutePath());
            return;
        }

        System.out.println("Processing upload directory: " + uploadDir.getAbsolutePath());

        // Iterate through subdirectories
        for (File studentDir : files) {
            if (studentDir.isDirectory()) {
                String studentId = studentDir.getName();
                System.out.println("Found student directory: " + studentId);

                // Automatically find test classes
                File[] studentFiles = studentDir.listFiles();
                if (studentFiles == null) continue;

                for (File file : studentFiles) {
                    if (file.getName().endsWith("Test.java")) {
                        String testClassName = file.getName().replace(".java", "");
                        System.out.println("Running sandbox for student: " + studentId + " with test class: " + testClassName);

                        // Run the sandbox logic
                        SandboxManager.runSandbox(studentId, studentDir, testClassName);
                        break;
                    }
                }
            } else {
                System.out.println("Skipping non-directory: " + studentDir.getName());
            }
        }

        System.out.println("Grading process completed.");
    }
}




