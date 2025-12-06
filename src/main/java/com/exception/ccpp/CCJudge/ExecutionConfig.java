package com.exception.ccpp.CCJudge;


import com.exception.ccpp.Common.Helpers;
import com.exception.ccpp.FileManagement.FileManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class ExecutionConfig {
    public final static boolean GCC_AVAILABLE;
    public final static boolean GPP_AVAILABLE;
    public final static boolean CLANG_AVAILABLE;
    public final static boolean CLANGPP_AVAILABLE;
    public final static boolean IS_WINDOWS;
    public final static boolean PYTHON_AVAILABLE;
    public final static boolean PYTHON3_AVAILABLE;
    static {
        IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
        GCC_AVAILABLE = isCommandAvailable("gcc");
        GPP_AVAILABLE = isCommandAvailable("g++");
        CLANG_AVAILABLE = isCommandAvailable("clang");
        CLANGPP_AVAILABLE = isCommandAvailable("clang++");
        PYTHON_AVAILABLE = isCommandAvailable("python");
        PYTHON3_AVAILABLE = isCommandAvailable("python3");
    }

    public static String[] getCompileCommand(FileManager fm) {
        String[] sourceFilenames = fm.getLanguageFiles().stream().map(fm::getRelativePath).map(Path::toString).toArray(String[]::new);
        return getCompileCommand(fm.getLanguage(), sourceFilenames);
    }

    private static String[] getCompileCommand(String language, String[] sourceFilenames) {

        String[] command;
        switch (language) {
            case "cpp":
                String[] cppSourceFiles = Arrays.stream(sourceFilenames)
                        .filter(f -> f.endsWith(".cpp"))
                        .toArray(String[]::new);

                command = new String[3 + cppSourceFiles.length];
                if (CLANGPP_AVAILABLE) command[0] = "clang++";
                else if (GPP_AVAILABLE) command[0] = "g++";
                else return null;
                System.arraycopy(cppSourceFiles, 0, command, 1, cppSourceFiles.length);
                command[1 + cppSourceFiles.length] = "-o";
                command[2 + cppSourceFiles.length] = IS_WINDOWS ? "Submission.exe" : "Submission";
                return command;
            case "c":
                String[] cSourceFiles = Arrays.stream(sourceFilenames)
                        .filter(f -> f.endsWith(".c"))
                        .toArray(String[]::new);
                command = new String[3 + cSourceFiles.length];
                if (CLANG_AVAILABLE) command[0] = "clang";
                else if (GCC_AVAILABLE) command[0] = "gcc";
                else return null;

                System.arraycopy(cSourceFiles, 0, command, 1, cSourceFiles.length);
                command[1 + cSourceFiles.length] = "-o";
                command[2 + cSourceFiles.length] = IS_WINDOWS ? "Submission.exe" : "Submission";
                return command;
            default:
                return null;
        }
    }
    // make sure to set the dir of process to root dir
    public static String[] getExecuteCommand(FileManager fm) {
        return switch (fm.getLanguage()) {
            case "java" -> new String[]{"java", fm.getCurrentFileStringPath().replace(".java","").replaceAll("[\\\\/]",".")}; // idk if com.exception.ccpp.Main is in all program
            case "cpp", "c" -> new String[]{(fm != null) ? (fm.getRootdir().toString() + "/Submission") : (Paths.get(".").toAbsolutePath().normalize().toString() + "/Submission")};
            case "python" -> new String[]{ (PYTHON3_AVAILABLE) ? "python3" : "python", fm.getCurrentFileStringPath()};
            default -> throw new IllegalArgumentException("Unsupported language.");
        };
    }

    // for running code
    public static String getRunCodeCommand(FileManager fm) {
        return Helpers.joinStringArrays(getCompileCommand(fm), getExecuteCommand(fm), " ", "&&");
    }


    public static boolean isCommandAvailable(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        String checkCmd = os.contains("win") ? "where" : "which";

        try {
            Process process = new ProcessBuilder(checkCmd, command)
                    .redirectErrorStream(true)
                    .start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
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