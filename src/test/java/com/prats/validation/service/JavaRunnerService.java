package com.prats.validation.service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JavaRunnerService {
    public void runStageJar(String csvPath, String jsonPath) throws Exception {
        List<String> command = getJavaCommand(csvPath, jsonPath);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        logReader(process);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Process exited with code: " + exitCode);
        }
    }

    public List<String> getJavaCommand(String csvPath, String jsonPath) {
        List<String> command = new ArrayList<>();
        command.add("java");
        //command.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
        command.add("-jar");

        command.add("target/validation-app-0.0.1-SNAPSHOT.jar"); // Replace with your actual JAR
        command.add("com.prats.validation.ValidationAppApplication");
        command.add(csvPath);
        command.add(jsonPath);
        return command;
    }

    public void logReader(Process process) throws IOException {
        try (BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            System.out.println("[Output]");
            while ((line = stdOut.readLine()) != null) {
                System.out.println(line);
            }

            System.out.println("[Error]");
            while ((line = stdErr.readLine()) != null) {
                System.err.println(line);
            }
        }
    }
}
