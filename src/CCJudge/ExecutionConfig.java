package CCJudge;


import Common.Helpers;
import FileManagement.FileManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


public class ExecutionConfig {

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

            case "cpp":
                String[] cppSourceFiles = Arrays.stream(sourceFilenames)
                        .filter(f -> f.endsWith(".cpp"))
                        .toArray(String[]::new);

                command = new String[3 + cppSourceFiles.length];
                command[0] = "g++";
                System.arraycopy(cppSourceFiles, 0, command, 1, cppSourceFiles.length);
                command[1 + cppSourceFiles.length] = "-o";
                command[2 + cppSourceFiles.length] = "Submission";
                return command;
            case "c":
                String[] cSourceFiles = Arrays.stream(sourceFilenames)
                        .filter(f -> f.endsWith(".c"))
                        .toArray(String[]::new);

                command = new String[3 + cSourceFiles.length];
                command[0] = "gcc";
                System.arraycopy(cSourceFiles, 0, command, 1, cSourceFiles.length);
                command[1 + cSourceFiles.length] = "-o";
                command[2 + cSourceFiles.length] = "Submission";
                return command;

            case "python":
                return null;

            default:
                return null;
        }
    }

    public static String[] getExecuteCommand(FileManager fm) {
        return switch (fm.getLanguage()) {
            case "java" -> new String[]{"java", fm.getCurrentFileStringPath().replace(".java","").replaceAll("\\\\",".")}; // idk if Main is in all program
            case "cpp", "c" -> new String[]{(fm != null) ? (fm.getRootdir().toString() + "/Submission") : (Paths.get(".").toAbsolutePath().normalize().toString() + "/Submission")};
            case "python" -> new String[]{"python3", "main.py"};
            default -> throw new IllegalArgumentException("Unsupported language.");
        };
    }

    // for running code
    public static String getRunCodeCommand(FileManager fm) {
        return Helpers.joinArrays(getCompileCommand(fm), getExecuteCommand(fm), " ", "&&");
    }
}