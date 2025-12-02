package CCJudge;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Judge {

    private static final long TIME_LIMIT_MS = 2000;
    private static final long INPUT_WAIT_MS = 180; // shits slow but still faster than codecum
    private static final String[] TEST_INPUTS = {"Alice", "Bob"};

    public static void main(String[] args) {
        test_judge();
    }

    public static void judge(String language, SubmissionFile[] files, String[] test_inputs) {

        String verdict = "Unknown Error";

        try {
            verdict = compile(files, language);
            if (verdict.startsWith("CE")) {
                System.out.println("Result: **" + verdict + "**");
                return;
            }
            verdict = judgeInteractively(language, test_inputs);

        } catch (Exception e) {
            System.err.println("Judge System Failure: " + e.getMessage());
            verdict = "System Error";
        } finally {
            // --- 5. Cleanup ---
            System.out.println("\n--- Cleanup ---");
            cleanup(files, language);
            System.out.println("Final Result: **" + verdict + "**");
        }
    }

    public static void test_judge() {
        String language = "cpp";

        String verdict = "Unknown Error";
        SubmissionFile[] files = ExecutionConfig.getTestFiles(language);

        try {
            setupSubmission(files);

            verdict = compile(files, language);
            if (verdict.startsWith("CE")) {
                System.out.println("Result: **" + verdict + "**");
                return;
            }

            verdict = judgeInteractively(language, TEST_INPUTS);

        } catch (Exception e) {
            System.err.println("Judge System Failure: " + e.getMessage());
            verdict = "System Error";
        } finally {
            System.out.println("\n--- Cleanup ---");
            test_cleanup(files, language);
            System.out.println("Final Result: **" + verdict + "**");
        }
    }
    // PRIVATE FUNCTIONS ------------------

    private static String judgeInteractively(String language, String[] testInputs) {
        Process process = null;
        try {
            String[] executeCommand = ExecutionConfig.getExecuteCommand(language);
            ProcessBuilder pb = new ProcessBuilder(executeCommand);
            // Set working directory to '.' (current directory) for execution
            pb.directory(new File("."));

            System.out.println("-> Executing: " + String.join(" ", executeCommand));
            process = pb.start();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            StringBuilder transcript = new StringBuilder();

            // Start the asynchronous output reader
            Future<Void> outputReader = executor.submit(new OutputReader(process.getInputStream(), transcript));
            BufferedWriter processInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            int inputIndex = 0;
            long startTime = System.currentTimeMillis();

            // Main interaction loop
            while (inputIndex < testInputs.length) {
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
                return "RTE (Runtime Error) - Exit Code: " + process.exitValue();
            } else {
                System.out.println("\n--- Captured Interactive Transcript ---");
                System.out.println(transcript.toString().trim());
                System.out.println("---------------------------------------");
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

    private static String compile(SubmissionFile[] files, String language) throws Exception {
        String[] compileCommand = ExecutionConfig.getCompileCommand(language, files);

        if (compileCommand == null) {
            System.out.println("-> No compilation required for " + language);
            return "No Compilation";
        }

        System.out.println("-> Compiling: " + String.join(" ", compileCommand));
        ProcessBuilder pb = new ProcessBuilder(compileCommand);
        pb.directory(new File("."));

        Process compileProcess = pb.start();

        String errorOutput = readStream(compileProcess.getErrorStream());

        if (compileProcess.waitFor(10, TimeUnit.SECONDS) && compileProcess.exitValue() != 0) {
            System.err.println("Compiler Errors:\n" + errorOutput);
            return "CE (Compilation Error)";
        }
        return "Compilation Success";
    }

    private static void setupSubmission(SubmissionFile[] files) throws IOException {
        System.out.println("-> Setting up " + files.length + " source files...");
        for (SubmissionFile file : files) {
            Files.writeString(Paths.get(file.filename()), file.content());
        }
    }

    private static String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static void cleanup(SubmissionFile[] files, String language) {
        try {
            if (language.equals("java")) {
                for (SubmissionFile file : files) {
                    String className = file.filename().replace(".java", ".class");
                    Files.deleteIfExists(Paths.get(className));
                }
            } else if (language.equals("cpp") || language.equals("c")) {
                Files.deleteIfExists(Paths.get("Submission.exe"));
            }
        } catch (IOException e) { }
    }

    // DONT USE THIS!! IT WILL DELETE USERS FILES
    private static void test_cleanup(SubmissionFile[] files, String language) {
        for (SubmissionFile file : files) {
            try {
                Files.deleteIfExists(Paths.get(file.filename()));
            } catch (IOException e) {}
        }

        try {
            if (language.equals("java")) {
                // del nig ahh java
                for (SubmissionFile file : files) {
                    String className = file.filename().replace(".java", ".class");
                    Files.deleteIfExists(Paths.get(className));
                }
            } else if (language.equals("cpp") || language.equals("c")) {
                // del exe
                Files.deleteIfExists(Paths.get("Submission.exe"));
                System.out.println("Deleted Submission.exe");
            }
        } catch (IOException e) { /* Ignore */ }
    }
}