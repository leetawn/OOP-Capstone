package com.exception.ccpp.CCJudge;


import com.exception.ccpp.Common.Helpers;
import com.exception.ccpp.FileManagement.FileManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ExecutionConfig {
    public static final ConcurrentMap<String, Boolean> COMMAND_CACHE = new ConcurrentHashMap<>();
    public static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public static String python_exec = null;
    public static String cpp_exec    = null;
    public static String c_exec      = null;
    public static String java_exec   = "java";
    static {
        if ((python_exec = getPythonPath()) != null);
        else if (isCommandAvailable("python3")) python_exec = "python3";
        else python_exec = "python";
        python_exec = "C:/Program Files/CC++/app/redist/python/python.exe";

        if ((cpp_exec = getCPPPath()) != null);
        else if (isCommandAvailable("clang++")) cpp_exec = "clang++";
        else if (isCommandAvailable("g++")) cpp_exec = "g++";
        else cpp_exec = null;

        if ((c_exec = getCPath()) != null);
        else if (isCommandAvailable("clang")) c_exec = "clang";
        else if (isCommandAvailable("gcc")) c_exec = "gcc";
        else cpp_exec = null;
    }

    public static String[] getCompileCommand(FileManager fm) {
        String[] sourceFilenames = fm.getLanguageFiles().stream().map(fm::getRelativePath).map(Path::toString).toArray(String[]::new);
        return getCompileCommand(fm.getLanguage(), sourceFilenames);
    }

    private static String[] getCompileCommand(String language, String[] sourceFilenames) {


        String[] command;
        switch (language) {
            case "java":
                command = new String[1 + sourceFilenames.length];
                command[0] = "javac";
                System.arraycopy(sourceFilenames, 0, command, 1, sourceFilenames.length);
                return command;
            case "cpp", "c++":
                if (cpp_exec == null) return null;
                command = new String[3 + sourceFilenames.length];
                command[0] = cpp_exec;
                System.arraycopy(sourceFilenames, 0, command, 1, sourceFilenames.length);
                command[1 + sourceFilenames.length] = "-o";
                command[2 + sourceFilenames.length] = IS_WINDOWS ? "Submission.exe" : "Submission";
                return command;
            case "c":
                if (c_exec == null) return null;
                command = new String[3 + sourceFilenames.length];
                command[0] = c_exec;
                System.arraycopy(sourceFilenames, 0, command, 1, sourceFilenames.length);
                command[1 + sourceFilenames.length] = "-o";
                command[2 + sourceFilenames.length] = IS_WINDOWS ? "Submission.exe" : "Submission";
                return command;
            default:
                return null;
        }
    }
    // make sure to set the dir of process to root dir
    public static String[] getExecuteCommand(FileManager fm) {
        String[] cmd = switch (fm.getLanguage()) {
            case "java" -> new String[]{(java_exec == null) ? "java" : java_exec, fm.getCurrentFileStringPath().replace(".java","").replaceAll("[\\\\/]",".")}; // idk if com.exception.ccpp.Main is in all program
            case "cpp", "c++", "c" -> new String[]{(fm != null) ? (fm.getRootdir().toString() + "/Submission") : (Paths.get(".").toAbsolutePath().normalize().toString() + "/Submission")};
            case "python", "py" -> new String[]{ python_exec, fm.getCurrentFileStringPath()};
            default -> throw new IllegalArgumentException("Unsupported language.");
        };
        return cmd;
    }

    public static boolean isCommandAvailable(String command) {
        // 1. Check the cache first (O(1))
        if (command == null) return false;
        System.out.println("Checking command: " + command);
        if (COMMAND_CACHE.containsKey(command)) {
            boolean val = COMMAND_CACHE.get(command);
            System.out.println("Command " + command + " exists: " + val);
            return val;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String checkCmd = os.contains("win") ? "where" : "which";

        boolean isAvailable = false;
        try {
            Process process = new ProcessBuilder(checkCmd, command)
                    .redirectErrorStream(true)
                    .start();

            // IMPORTANT: Limit the wait time to avoid indefinite hangs
            // We expect the check to be fast, so 5 seconds is very generous.
            if (process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                isAvailable = process.exitValue() == 0;
            } else {
                // Process timed out (very rare for 'which'/'where')
                process.destroyForcibly();
            }
        } catch (Exception e) {
            // Error starting or waiting for the process
            isAvailable = false;
        }

        // 3. Store the result in the cache
        COMMAND_CACHE.put(command, isAvailable);
        System.out.println("Command " + command+ " exists: " + isAvailable);
        return isAvailable;
    }

    public static String getPythonPath() {
        return getInternalPath("python/python.exe", null);
    }

    public static String getCPath() {
        return getInternalPath("w64devkit/bin/gcc.exe", null);
    }
    public static String getCPPPath() {
        return getInternalPath("w64devkit/bin/g++.exe", null);
    }

    public static String getJavaExecutablePath() {
        String userDir = System.getProperty("user.dir");

        // Check Choice 2 location (Standard jpackage)
        File jpackageRuntime = new File(userDir, "runtime/bin/java.exe");
        if (jpackageRuntime.exists()) return jpackageRuntime.getAbsolutePath();

        // Check Choice 1 location (Your manual redist)
        File manualRuntime = new File(userDir, "app/redist/java-runtime/bin/java.exe");
        if (manualRuntime.exists()) return manualRuntime.getAbsolutePath();

        return "java"; // Fallback to system path
    }

    private static String getInternalPath(String relativePath, String fallback) {
        // user.dir points to the folder containing the .exe launcher
        String installPath = System.getProperty("user.dir");

        // In a jpackage MSI/APP_IMAGE, resources usually end up in 'app'
        File bundledFile = new File(installPath, "app/redist/" + relativePath);

        if (bundledFile.exists()) {
            System.err.println("[bundled_compiler]: "+bundledFile.getAbsolutePath());
            return bundledFile.getAbsolutePath();
        }

        // Debug: Print where we looked if it fails
        System.err.println("[bundled_compiler] Bundled tool not found at: " + bundledFile.getAbsolutePath());
        return fallback; // Fallback to system PATH
    }



    static String NO_JDK_ERROR = "Java Compiler not found.\nEnsure you are running on a JDK.\n";
    static String NO_C_COMPILER_ERROR =
"""
No C/C++ compiler found.
Required: gcc or clang.

Windows:
  gcc (MSYS2):   https://www.msys2.org/
  clang/LLVM:    https://github.com/llvm/llvm-project/releases
  
macOS:
  Command Line Tools: xcode-select --install
""";
}