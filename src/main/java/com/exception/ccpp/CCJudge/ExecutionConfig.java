package com.exception.ccpp.CCJudge;


import com.exception.ccpp.Common.Helpers;
import com.exception.ccpp.FileManagement.FileManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ExecutionConfig {
    public static final ConcurrentMap<String, Boolean> COMMAND_CACHE = new ConcurrentHashMap<>();
    public static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public static String[] getCompileCommand(FileManager fm) {
        String[] sourceFilenames = fm.getLanguageFiles().stream().map(fm::getRelativePath).map(Path::toString).toArray(String[]::new);
        return getCompileCommand(fm.getLanguage(), sourceFilenames);
    }

    private static String[] getCompileCommand(String language, String[] sourceFilenames) {


        String[] command;
        switch (language) {
            case "java":
//                return new String[] {"where","javac"};
                command = new String[1 + sourceFilenames.length];
                command[0] = "javac";
                System.arraycopy(sourceFilenames, 0, command, 1, sourceFilenames.length);
                return command;
            case "cpp", "c++":
                command = new String[3 + sourceFilenames.length];
                if (isCommandAvailable("clang++")) command[0] = "clang++";
                else if (isCommandAvailable("g++")) command[0] = "g++";
                else return null;
                System.arraycopy(sourceFilenames, 0, command, 1, sourceFilenames.length);
                command[1 + sourceFilenames.length] = "-o";
                command[2 + sourceFilenames.length] = IS_WINDOWS ? "Submission.exe" : "Submission";
                return command;
            case "c":
                command = new String[3 + sourceFilenames.length];
                if (isCommandAvailable("clang")) command[0] = "clang";
                else if (isCommandAvailable("gcc")) command[0] = "gcc";
                else return null;

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
        return switch (fm.getLanguage()) {
            case "java" -> new String[]{"java", fm.getCurrentFileStringPath().replace(".java","").replaceAll("[\\\\/]",".")}; // idk if com.exception.ccpp.Main is in all program
            case "cpp", "c++", "c" -> new String[]{(fm != null) ? (fm.getRootdir().toString() + "/Submission") : (Paths.get(".").toAbsolutePath().normalize().toString() + "/Submission")};
            case "python" -> new String[]{ TerminalApp.TERMINAL_START_COMMAND[0], TerminalApp.TERMINAL_START_COMMAND[1], (isCommandAvailable("python3")) ? "python3" : "python", fm.getCurrentFileStringPath()};
            default -> throw new IllegalArgumentException("Unsupported language.");
        };
    }

    // for running code
    public static String getRunCodeCommand(FileManager fm) {
        return Helpers.joinStringArrays(getCompileCommand(fm), getExecuteCommand(fm), " ", "&&");
    }


    public static boolean isCommandAvailable(String command) {
        // 1. Check the cache first (O(1))
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