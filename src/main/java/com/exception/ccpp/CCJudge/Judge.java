package com.exception.ccpp.CCJudge;

import com.exception.ccpp.Common.Helpers;
import com.exception.ccpp.Debug.DebugLog;
import com.exception.ccpp.FileManagement.FileManager;
import com.exception.ccpp.FileManagement.SFile;
import com.pty4j.PtyProcessBuilder;

import javax.tools.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.exception.ccpp.CCJudge.ExecutionConfig.NO_C_COMPILER_ERROR;

public class Judge {

    private static final long TIME_LIMIT_MS = 2000; // TLE

    public static void main(String[] args) {

    }

    // TODO: this will be the real judge
    // will output an Array of SubmissionRecord, check the Submission Record definition
    public static SubmissionRecord[] judge(FileManager fm, TestcaseFile tf) {
        DebugLog logger = DebugLog.getInstance();
        SubmissionRecord judge_res;

        List<String[]> inputs = tf.getInputs();
        List<String> ex_out = tf.getExpectedOutputs();
        SubmissionRecord[] verdicts = new SubmissionRecord[ex_out.size()];
        for (int i = 0; i < verdicts.length; i++) {
            verdicts[i] = new SubmissionRecord(JudgeVerdict.UE, "Unknown Error", ex_out.get(i));
        }
        try {
            judge_res = compile(fm);
            if (judge_res.verdict() == JudgeVerdict.CE) {
                recordCopy(verdicts, judge_res, ex_out.size());
                return verdicts;
            };

            for (int i = 0; i < ex_out.size(); i++) {
                judge_res = judgeInteractively(fm, inputs.get(i), ex_out.get(i));
                verdicts[i]
                        .setVerdict(judge_res.verdict())
                        .setOutput(judge_res.output());
            }

        } catch (Exception e) {
            String fmsg = "Judge System Failure: " + e.getMessage();
            logger.logf("%s:\n%s",fmsg);
            e.printStackTrace();
            judge_res = new SubmissionRecord(JudgeVerdict.JSF, fmsg, null);
            recordCopy(verdicts, judge_res, ex_out.size());
        } finally {
            logger.logln("\n--- FINISHED RUNNING TESTCASES ---");
            if (DebugLog.DEBUG_ENABLED)
            {
                for (int i = 0; i < verdicts.length; i++) {
                    logger.logf("Testcase %d exit code: %s\n", i, verdicts[i].verdict().name());
                }
            }
            logger.logln("\n--- Cleanup ---\n");
            cleanup(fm);
        }

        return verdicts;
    }

    static void cleanup(FileManager fm) {
        String language = fm.getLanguage();
        DebugLog logger = DebugLog.getInstance();
        try {
            if (language.equals("java")) {
                for (SFile file : fm.getLanguageFiles()) {
                    String className = file.getPath().toAbsolutePath().toString().replace(".java", ".class");
                    logger.logln("Judge.cleanup: Deleting " + className);
                    Files.deleteIfExists(Paths.get(className));
                }
            } else if (language.equals("cpp") || language.equals("c")) {
                Path p = Paths.get(fm.getRootdir().toAbsolutePath().toString(),"Submission.exe");
                logger.logln("Judge.cleanup: Deleting " + p);
                if (Files.exists(p)) { Files.delete(p); }
                logger.logln("Judge.cleanup: Deleted " + p);
            }
        } catch (IOException ignored) {}
    }

    static SubmissionRecord judgeInteractively(FileManager fm, String[] testInputs, String expected_output) {
        Process process = null;
        DebugLog logger = DebugLog.getInstance();
        try {
            String[] executeCommand = ExecutionConfig.getExecuteCommand(fm);
            logger.logln("-> Executing: " + String.join(" ", executeCommand));
            Map<String, String> env = new HashMap<>(System.getenv());
            if (!env.containsKey("TERM")) env.put("TERM", "xterm");
            process = new PtyProcessBuilder(executeCommand)
                    .setEnvironment(env)
                    .setRedirectErrorStream(true) // Redirects stderr to stdout stream
                    .setDirectory(fm.getRootdir().toString())
                    .setConsole(false)
                    .start();


            ExecutorService executor = Executors.newFixedThreadPool(2);
            StringBuilder transcript = new StringBuilder();

            executor.submit(new OutputReader(process.getInputStream(), transcript));
            BufferedWriter processInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            int inputIndex = 0;
            long startTime = System.currentTimeMillis();
            String cmd_newline = "\r\n";
            if (fm.getLanguage().equals("python")) cmd_newline = "\n";

            System.out.printf("input size: %d\n",testInputs.length);
            do {
                if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS) {
                    process.destroyForcibly();
                    return new SubmissionRecord(JudgeVerdict.TLE, Helpers.stripAnsiCRLines(transcript.toString()).trim(), expected_output);
                }

                Thread.sleep(50);

                // Send the next input line

                if (inputIndex < testInputs.length) {
                    String inputLine = testInputs[inputIndex++];
                    logger.logln("-> Judge providing input: " + inputLine);
                    processInputWriter.write(inputLine + cmd_newline);
//                    processInputWriter.newLine();
                }

                processInputWriter.flush();

            } while (testInputs != null && inputIndex < testInputs.length);

            processInputWriter.close();

            // Wait for termination using remaining time
            long remainingTime = TIME_LIMIT_MS - (System.currentTimeMillis() - startTime);
            process.waitFor(remainingTime, TimeUnit.MILLISECONDS);

            executor.shutdownNow();

            if (process.isAlive()) {
                process.destroyForcibly();
                return new SubmissionRecord(
                        JudgeVerdict.RE,
                        Helpers.stripAnsiCRLines(
                            transcript
                                .append("\nTLE (Time Limit Exceeded)\n")
                                .toString()
                        ).trim(),
                        expected_output
                );
            } else if (process.exitValue() != 0) {
                String errorOutput = readStream(process.getErrorStream());
                System.err.println("Runtime Error Details:\n" + errorOutput);
                System.err.println("Program Details:\n" + transcript.toString().trim());
                process.destroy();
                return new SubmissionRecord(
                        JudgeVerdict.RE,
                        Helpers.stripAnsiCRLines(
                            transcript
                                .append("RTE (Runtime Error) - Exit Code: ")
                                .append(process.exitValue())
                                .append("\n")
                                .toString()
                            ).trim(),
                        expected_output
                );
            } else {
                process.destroy();
                return new SubmissionRecord(JudgeVerdict.NONE, Helpers.stripAnsiCRLines(transcript.toString()).trim(), expected_output);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new SubmissionRecord(JudgeVerdict.ESF, "Execution System Failure\n", expected_output);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    static SubmissionRecord compile(FileManager fm) throws IOException, InterruptedException {
        DebugLog logger = DebugLog.getInstance();

        String[] compileCommand = null;
        switch (fm.getLanguage().toLowerCase()) {
            case "java": {
                logger.logln("-> Compiling Java using javax.tools.JavaCompiler");
                return startJavaCompilation(fm);
            }
            case "c","cpp", "c++": {
                compileCommand = ExecutionConfig.getCompileCommand(fm);
                if (compileCommand == null) return new SubmissionRecord(JudgeVerdict.CE, NO_C_COMPILER_ERROR, null);
                break;
            }
            default: {
                logger.logln("-> No compilation required for " + fm.getLanguage());
                return new SubmissionRecord(JudgeVerdict.NONE, "No compilation required for " + fm.getLanguage(), null);
            }
        }
        logger.logln("-> Compiling: " + String.join(" ", compileCommand));
        ProcessBuilder pb = new ProcessBuilder(compileCommand);
        pb.directory(fm.getRootdir().toFile());

        return startCompilation(pb);
    }

    private static SubmissionRecord startJavaCompilation(FileManager fm) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return new SubmissionRecord(JudgeVerdict.CE, ExecutionConfig.NO_JDK_ERROR, null);
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        // Use try-with-resources to ensure fileManager is closed
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {

            // 1. Get all Java source files to be compiled
            Iterable<Path> sourcePaths = fm.getFiles().stream()
                    .filter(f -> f.getPath().toString().endsWith(".java"))
                    .map(SFile::getPath)
                    .collect(Collectors.toList());

            if (!sourcePaths.iterator().hasNext()) {
                return new SubmissionRecord(JudgeVerdict.CE, "No Java source files (.java) found for compilation.", null);
            }

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(sourcePaths);

            // 2. Set compilation options: output directory (-d)
            Path outputDir = fm.getRootdir();
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // Options: -d <output directory>
            Iterable<String> options = Arrays.asList(
                    "-d", outputDir.toAbsolutePath().toString()
            );

            // 3. Create and run the compilation task
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, // Writer for compilation output (null uses System.err/out)
                    fileManager,
                    diagnostics,
                    options,
                    null, // Classes for annotation processing
                    compilationUnits
            );

            boolean success = task.call();

            // 4. Handle results
            if (!success) {
                StringWriter output = new StringWriter();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    // Format the error message to be submission record friendly
                    output.write(String.format("Error [%s] on line %d in %s:\n%s\n",
                            diagnostic.getKind().toString(),
                            diagnostic.getLineNumber(),
                            diagnostic.getSource() != null ? diagnostic.getSource().getName() : "Unknown File",
                            diagnostic.getMessage(null)));
                }
                String ceMsg = "Compilation Errors (javax.tools):\n" + output.toString();
                System.err.println(ceMsg);
                return new SubmissionRecord(JudgeVerdict.CE, ceMsg, null);
            }

            return new SubmissionRecord(JudgeVerdict.NONE, "Compilation Successful (javax.tools)", null);
        }
    }

    private static SubmissionRecord startCompilation(ProcessBuilder pb) throws IOException, InterruptedException {
        Process compileProcess = pb.start();

        String errorOutput = readStream(compileProcess.getErrorStream());

        if (compileProcess.waitFor(10, TimeUnit.SECONDS) && compileProcess.exitValue() != 0) {
            String ceMsg = "Compiler Errors:\n" + errorOutput;
            System.err.println(ceMsg);
            return new SubmissionRecord(JudgeVerdict.CE, ceMsg, null);
        }
        return new SubmissionRecord(JudgeVerdict.NONE, "Compilation Successful", null);
    }

    private static String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static void recordCopy(SubmissionRecord[] dest, SubmissionRecord src, int ex_out_size)
    {
        for (int i = 0; i < ex_out_size; i++) {
            dest[i]
                    .setVerdict(src.verdict())
                    .setOutput(src.output());
        }
    }

    /*********************** STATIC CLASSES, ENUMS OR WHATEVER **********************/

    private static class OutputReader implements Callable<Void> {
        private final BufferedReader reader;
        private final StringBuilder transcript;

        public OutputReader(InputStream is, StringBuilder transcript) {
            this.reader = new BufferedReader(new InputStreamReader(is));
            this.transcript = transcript;
        }

        @Override
        public Void call() {
            try {
                int data;
                while ((data = reader.read()) != -1) {
                    transcript.append((char) data);
                }
            } catch (IOException e) {}
            return null;
        }
    }

    public enum JudgeVerdict {
        AC, // Accepted
        WA, // Wrong Answer
        CE, // Compile Error
        RE, // Runtime Error
        TLE, // Time Limit Exceeded
        MLE, // Memory Limit Exceeded

        UE, // Unknown Error. We the devs, idk and idc what the fuck you are facing rn
        ESF, // Execution System Failure, Not the Users Fault!
        JSF, // Judge System Failure
        NONE // USED FOR CREATION ONLY
    }

}