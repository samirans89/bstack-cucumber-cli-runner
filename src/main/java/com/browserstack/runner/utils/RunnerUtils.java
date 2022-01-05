package com.browserstack.runner.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunnerUtils {

    public static void createDirectories(String dirPath) throws IOException {

        final File reportDir = new File(dirPath);

        if (!reportDir.exists()) {
            if (!reportDir.mkdirs()) {
                throw new IOException(String.format("Unable to create the %s directory", reportDir));
            }
        }
    }

    public static void writeToFile(String filePath, String content, boolean isAppend) {

        try {
            File file = new File(filePath);
            if(!file.exists()) {
                file.createNewFile();
            }

            if(isAppend) {
                Files.write(
                        Paths.get(filePath),
                        content.getBytes(),
                        StandardOpenOption.APPEND);
            } else {
                Files.write(
                        Paths.get(filePath),
                        content.getBytes(),
                        StandardOpenOption.WRITE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<String> findFiles(Path path, String fileExtension)
            throws IOException {

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .collect(Collectors.toList());
        }

        return result;
    }
}
