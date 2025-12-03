package CCJudge;

import CustomExceptions.NotDirException;
import FileManagement.FileManager;
import FileManagement.SFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Judge {

    private static final long TIME_LIMIT_MS = 2000;
    private static final long INPUT_WAIT_MS = 128; // shits slow but still faster than codecum
    private static final String[] TEST_INPUTS = {"Alice", "Bob"};

    public static void main(String[] args) {
        String[] s = {"Ethan"};
        try {
            // JAVA JUDGE DEMO
            FileManager fm = new FileManager("src", "java");
            // SET Current file with fm.setCurrentFile(SFile);
            /* Ignore this line this is just so FileManager has a current file since by default it has no CurrentFile you have to set it */ for (SFile f : fm.getFiles()) { if (f.getPath().getFileName().toString().contains("TestMain")) { fm.setCurrentFile(f); break; } }
            judge(fm, null);

            // CPP, C, PYTHON JUDGE DEMO
            judge(new FileManager("COMPILER_TEST/CPP", "cpp"), s);
            judge(new FileManager("COMPILER_TEST/PYTHON", "python"), s);
            judge(new FileManager("COMPILER_TEST/C", "c"), s);

        } catch (NotDirException e) {}
    }

    public static void judge(FileManager fm, String[] test_inputs) {

        String verdict = "Unknown Error";

        try {
            verdict = compile(fm);
            if (verdict.startsWith("CE")) {
                System.out.println("Result: **" + verdict + "**");
                return;
            }
            verdict = judgeInteractively(fm, test_inputs);

        } catch (Exception e) {
            System.err.println("Judge System Failure: " + e.getMessage());
            verdict = "System Error";
        } finally {
            // --- 5. Cleanup ---
            System.out.println("\n--- Cleanup ---");
            cleanup(fm);
            System.out.printf("Final Result: **%s**\n\n", verdict);
        }
    }

    private static String judgeInteractively(FileManager fm, String[] testInputs) {
        Process process = null;
        try {
            String[] executeCommand = ExecutionConfig.getExecuteCommand(fm);
            ProcessBuilder pb = new ProcessBuilder(executeCommand);

            // Set working directory to '.' (current directory) for execution
            pb.directory(fm.getRootdir().toFile());

            System.out.println("-> Executing: " + String.join(" ", executeCommand));
            process = pb.start();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            StringBuilder transcript = new StringBuilder();

            // Start the asynchronous output reader
            executor.submit(new OutputReader(process.getInputStream(), transcript));
            BufferedWriter processInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            int inputIndex = 0;
            long startTime = System.currentTimeMillis();

            // Main interaction loop
            while (testInputs != null && inputIndex < testInputs.length) {
                if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS) {
                    process.destroyForcibly();
                    return "TLE (Time Limit Exceeded)";
                }

                // Polling Heuristic: Wait for output to complete
                Thread.sleep(INPUT_WAIT_MS);

                // Send the next input line
                String inputLine = testInputs[inputIndex];
                transcript.append(inputLine).append("\n");

                System.out.println("-> Judge providing input: " + inputLine);
                processInputWriter.write(inputLine);
                processInputWriter.newLine();
                processInputWriter.flush();

                inputIndex++;
            }

            processInputWriter.close();

            // Wait for termination using remaining time
            long remainingTime = TIME_LIMIT_MS - (System.currentTimeMillis() - startTime);
            process.waitFor(remainingTime, TimeUnit.MILLISECONDS);

            executor.shutdownNow();

            if (process.isAlive()) {
                process.destroyForcibly();
                return "TLE (Time Limit Exceeded)";
            } else if (process.exitValue() != 0) {
                String errorOutput = readStream(process.getErrorStream());
                System.err.println("Runtime Error Details:\n" + errorOutput);
                process.destroy();
                return "RTE (Runtime Error) - Exit Code: " + process.exitValue();
            } else {
                System.out.println("\n--- Captured Interactive Transcript ---");
                System.out.println(transcript.toString().trim());
                System.out.println("---------------------------------------");
                process.destroy();
                return "AC (Execution Complete)";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Execution System Failure";
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static String compile(FileManager fm) throws Exception {
        String[] compileCommand = ExecutionConfig.getCompileCommand(fm);

        if (compileCommand == null) {
            System.out.println("-> No compilation required for " + fm.getLanguage());
            return "No Compilation";
        }

        System.out.println("-> Compiling: " + String.join(" ", compileCommand));
        ProcessBuilder pb = new ProcessBuilder(compileCommand);
        pb.directory(fm.getRootdir().toFile());

        return startCompilation(pb);
    }

    private static String startCompilation(ProcessBuilder pb) throws IOException, InterruptedException {
        Process compileProcess = pb.start();

        String errorOutput = readStream(compileProcess.getErrorStream());

        if (compileProcess.waitFor(10, TimeUnit.SECONDS) && compileProcess.exitValue() != 0) {
            System.err.println("Compiler Errors:\n" + errorOutput);
            return "CE (Compilation Error)";
        }
        return "Compilation Success";
    }

    private static String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static void cleanup(FileManager fm) {
        String language = fm.getLanguage();
        try {
            if (language.equals("java")) {
                Files.deleteIfExists(Paths.get(fm.getCurrentFile().getPath().getParent().toString(),"SubmissionFile.class"));
                for (SFile file : fm.getFiles()) {
                    String className = file.getPath().toAbsolutePath().toString().replace(".java", ".class");
                    Files.deleteIfExists(Paths.get(className));
                }
            } else if (language.equals("cpp") || language.equals("c")) {
                Path p = Paths.get(fm.getRootdir().toAbsolutePath().toString(),"Submission.exe");
                System.out.println("Deleting " + p);
                if (Files.exists(p)) { Files.delete(p); }
                System.out.println("Deleted " + p);
            }
        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
        }
    }
}