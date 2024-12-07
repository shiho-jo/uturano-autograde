package org.uturano;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SkeletonJavaScanner {
    private String skeletonDir;
    private List<String> filePathes;

    public SkeletonJavaScanner(String skeletonDir) {
        this.skeletonDir = skeletonDir;
        this.filePathes = new ArrayList<>();
    }

    public static void findSkeletonJavaFiles() throws IOException {
    }

    public List<String> listJavaFiles() {
        return null;
    }
}
