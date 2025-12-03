package CCJudge;

import Debug.DebugLog;
import CustomExceptions.NotDirException;
import FileManagement.FileManager;
import FileManagement.SFile;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Judge {

    private static final long TIME_LIMIT_MS = 2000;
    private static final long INPUT_WAIT_MS = 80; // shits slow but still faster than codecum
    private static final String[] TEST_INPUTS = {"Alice", "Bob"};

    public static void main(String[] args) {
        String[] s = {"Ethan"};

        try {
            // JAVA JUDGE DEMO
            FileManager fm = new FileManager("src", "java");
            // SET Current file with fm.setCurrentFile(SFile);
            /* Ignore this line this is just so FileManager has a current file since by default it has no CurrentFile */ for (SFile f : fm.getFiles()) { if (f.getPath().getFileName().toString().contains("TestMain")) { fm.setCurrentFile(f); break; } }
            judge(fm, null);

            // CPP, C, PYTHON JUDGE DEMO
            judge(new FileManager("COMPILER_TEST/CPP", "cpp"), s);
            judge(new FileManager("COMPILER_TEST/PYTHON", "python"), s);
            judge(new FileManager("COMPILER_TEST/C", "c"), s);

        } catch (NotDirException e) {}
    }

    public static SubmissionRecord judge(FileManager fm, String[] test_inputs) {

        SubmissionRecord judge_res = new SubmissionRecord(JudgeVerdict.UE, "Unknown Error");
        DebugLog logger = DebugLog.getInstance();
        try {
            judge_res = compile(fm);
            if (judge_res.verdict() == JudgeVerdict.CE) return judge_res;

            judge_res = judgeInteractively(fm, test_inputs);

        } catch (Exception e) {
            String fmsg = "Judge System Failure: " + e.getMessage();
            logger.logln(fmsg);
            judge_res = new SubmissionRecord(JudgeVerdict.JSF, fmsg);
        } finally {
            logger.logln("\n--- Program Output ---");
            logger.logln(judge_res.output());
            logger.logf("\n--- Program Exit Code: **%s** ---", judge_res.verdict().name());
            logger.logln("\n--- Cleanup ---\n");
            cleanup(fm);
        }
        return judge_res;
    }

    private static SubmissionRecord judgeInteractively(FileManager fm, String[] testInputs) {
        Process process = null;
        DebugLog logger = DebugLog.getInstance();
        try {
            String[] executeCommand = ExecutionConfig.getExecuteCommand(fm);
            ProcessBuilder pb = new ProcessBuilder(executeCommand);

            // Set working directory to '.' (current directory) for execution
            pb.directory(fm.getRootdir().toFile());

            logger.logln("-> Executing: " + String.join(" ", executeCommand));
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
                    return new SubmissionRecord(JudgeVerdict.TLE, transcript.toString().trim());
                }

                // Polling Heuristic: Wait for output to complete
                Thread.sleep(INPUT_WAIT_MS);

                // Send the next input line
                String inputLine = testInputs[inputIndex];
                transcript.append(inputLine).append("\n");

                logger.logln("-> Judge providing input: " + inputLine);
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
                return new SubmissionRecord(JudgeVerdict.RE,
                        transcript
                                .append("\nTLE (Time Limit Exceeded)\n")
                                .toString().trim()
                );
            } else if (process.exitValue() != 0) {
                String errorOutput = readStream(process.getErrorStream());
                System.err.println("Runtime Error Details:\n" + errorOutput);
                process.destroy();
                return new SubmissionRecord(JudgeVerdict.RE,
                        transcript
                                .append("RTE (Runtime Error) - Exit Code: ")
                                .append(process.exitValue())
                                .append("\n")
                                .toString().trim()
                );
            } else {
                process.destroy();
                return new SubmissionRecord(JudgeVerdict.NONE, transcript.toString().trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new SubmissionRecord(JudgeVerdict.ESF, "Execution System Failure\n");
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static SubmissionRecord compile(FileManager fm) throws Exception {
        String[] compileCommand = ExecutionConfig.getCompileCommand(fm);
        DebugLog logger = DebugLog.getInstance();
        if (compileCommand == null) {
            logger.logln("-> No compilation required for " + fm.getLanguage());
            return new SubmissionRecord(JudgeVerdict.NONE, "No compilation required for " + fm.getLanguage());
        }

        logger.logln("-> Compiling: " + String.join(" ", compileCommand));
        ProcessBuilder pb = new ProcessBuilder(compileCommand);
        pb.directory(fm.getRootdir().toFile());

        return startCompilation(pb);
    }

    private static SubmissionRecord startCompilation(ProcessBuilder pb) throws IOException, InterruptedException {
        Process compileProcess = pb.start();

        String errorOutput = readStream(compileProcess.getErrorStream());

        if (compileProcess.waitFor(10, TimeUnit.SECONDS) && compileProcess.exitValue() != 0) {
            String ceMsg = "Compiler Errors:\n" + errorOutput;
            System.err.println(ceMsg);
            return new SubmissionRecord(JudgeVerdict.CE, ceMsg);
        }
        return new SubmissionRecord(JudgeVerdict.NONE, "Compilation Successful");
    }

    private static String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static void cleanup(FileManager fm) {
        String language = fm.getLanguage();
        DebugLog logger = DebugLog.getInstance();
        try {
            if (language.equals("java")) {
                for (SFile file : fm.getFiles()) {
                    String className = file.getPath().toAbsolutePath().toString().replace(".java", ".class");
                    Files.deleteIfExists(Paths.get(className));
                }
            } else if (language.equals("cpp") || language.equals("c")) {
                Path p = Paths.get(fm.getRootdir().toAbsolutePath().toString(),"Submission.exe");
                logger.logln("Deleting " + p);
                if (Files.exists(p)) { Files.delete(p); }
                logger.logln("Deleted " + p);
            }
        } catch (IOException ignored) {}
    }
}