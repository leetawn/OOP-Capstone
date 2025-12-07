package com.exception.ccpp.CCJudge;

import com.exception.ccpp.Common.Helpers;
import com.exception.ccpp.Debug.CCLogger;
import com.exception.ccpp.Debug.DebugLog;
import com.exception.ccpp.FileManagement.FileManager;
import com.exception.ccpp.FileManagement.SFile;
import com.pty4j.PtyProcessBuilder;

import javax.tools.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.exception.ccpp.CCJudge.ExecutionConfig.NO_C_COMPILER_ERROR;
import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;

public class Judge {
    private static Map<String, String> env;
    private static Map<JudgeSlave, Boolean> activeSlaves = new ConcurrentHashMap<>();
    private static DebugLog judge_logger = DebugLog.getInstance();
    static {
        env = new HashMap<>(System.getenv());
        if (!env.containsKey("TERM")) env.put("TERM", "xterm");
    }

    static final long TIME_LIMIT_MS = 2000; // TLE
    static final long COMPILE_TIME_LIMIT_SEC = 10;



    /******************** SINGLE-THREADED *************************/
    // TODO: this will be the real judge
    // will output an Array of SubmissionRecord, check the Submission Record definition
    // for TERMINAL USE ONLY
    static void judgeInteractively(String[] cmd, FileManager fm, String[] testInputs, Consumer<SubmissionRecord[]> callback, CCLogger logger) {
        slaveWorkers.submit(() -> {
            Future<SubmissionRecord> f = slaveWorkers.submit(new JudgeSlave(cmd, fm, testInputs, null, 1, logger));
            SubmissionRecord verdict = new SubmissionRecord(JudgeVerdict.RE, "Runtime Error Buddy", null);
            try {
                verdict = f.get();
            } catch (InterruptedException e) {}
            catch (ExecutionException e) { /*Normal shit*/
                logger.errln("[Judge.judge]: Execution Error!");
            }
            callback.accept(new SubmissionRecord[]{verdict});
        });

    }

    /******************** MULTI-THREADED *************************/
    // PARALLEL JUDGE
    public static void judge(FileManager fm, TestcaseFile tf, Consumer<SubmissionRecord[]> callback)  {
        slaveWorkers.submit(() -> {
            SubmissionRecord judge_res;
            String rootdir = fm.getRootdir().toString();
            String language = fm.getLanguage();

            Map<Testcase, String> testcases = tf.getTestcases();
            int testcases_size = testcases.size();
            int i = 0;
            SubmissionRecord[] verdicts = new SubmissionRecord[testcases_size];
            for (Testcase tc : testcases.keySet()) {
                verdicts[i++] = new SubmissionRecord(JudgeVerdict.UE, "Unknown Error", tc.getExpectedOutput());
            }
            // COMPILE
            judge_logger.logln("[Judge.judge]: Compiling...");
            try {
                judge_res = compile(fm, judge_logger); // WAITS TO FINISH
                if (judge_res.verdict() == JudgeVerdict.CE) {
                    judge_logger.errln("[Judge.judge]: Compiler Error!");
                    recordCopy(verdicts, judge_res, testcases_size);
                    parallelCleanup(fm);
                    callback.accept(verdicts);
                    return;
                }
            } catch (Exception e) {
                String fmsg = "Judge System Failure: " + e.getMessage();
                judge_logger.errln(fmsg);
                e.printStackTrace();
                judge_res = new SubmissionRecord(JudgeVerdict.JSF, fmsg, null);
                recordCopy(verdicts, judge_res, testcases_size);
                parallelCleanup(fm);
                callback.accept(verdicts);
                return;
            }
            judge_logger.logln("[Judge.judge]: Compilation Done.");

            // judgeInteractively (Parallel)
            String[] cmd = ExecutionConfig.getExecuteCommand(fm);
            ArrayList<Future<SubmissionRecord>> futures = new ArrayList<>();
            i =1;
            for (Testcase tc : testcases.keySet()) {

                judge_logger.logf("[Judge.judge]: Running Testcase %d...\n", i);
                futures.add(slaveWorkers.submit(
                    new JudgeSlave(cmd, rootdir, language, tc.getInputs(), tc.getExpectedOutput(), i++, judge_logger)
                ));
            }

            i = 0;
            for  (Future<SubmissionRecord> f : futures) {
                try {
                    judge_logger.logf("[Judge.judge]: Fetching Testcase %d Results...\n", i+1);
                    SubmissionRecord sr = f.get(); // blocks
                    verdicts[i++]
                            .setVerdict(sr.verdict())
                            .setOutput(sr.output());
                }
                catch (InterruptedException e) { /*TODO HANDLE INTERRUPT*/}
                catch (ExecutionException e) { /*Normal shit*/
//                    Throwable cause = e.getCause();
//                    cause.printStackTrace();  // see what actually went wrong
                    judge_logger.errln("[Judge.judge]: Execution Error!");
                }
            }

            if (DebugLog.DEBUG_ENABLED)
                for (int j = 0; j < verdicts.length; j++) {
                    judge_logger.logf("[Judge.judge] Testcase %d exit code: %s\n", j+1, verdicts[j].verdict().name());
                }


            parallelCleanup(fm);
            judge_logger.logln("[Judge.judge]: Returning Results...\n");
            callback.accept(verdicts);
        });
    }

    // please terminate all workers
    static void parallelCleanup(FileManager fm) {
        slaveWorkers.submit(() -> {
//            if (!activeSlaves.isEmpty())
//                for (JudgeSlave s : activeSlaves.keySet()) { s.mutilate(); }
            cleanup(fm);
        });
    }

    static void cleanup(FileManager fm) {
        String language = fm.getLanguage();
        try {
            if (language.equals("java")) {
                for (SFile file : fm.getLanguageFiles()) {
                    String className = file.getPath().toAbsolutePath().toString().replace(".java", ".class");
                    judge_logger.logln("Judge.cleanup: Deleting " + className);
                    Files.deleteIfExists(Paths.get(className));
                }
            } else if (language.equals("cpp") || language.equals("c")) {
                Path p = Paths.get(fm.getRootdir().toAbsolutePath().toString(),"Submission.exe");
                judge_logger.logln("Judge.cleanup: Deleting " + p);
                if (Files.exists(p)) { Files.delete(p); }
                judge_logger.logln("Judge.cleanup: Deleted " + p);
            }
        } catch (IOException ignored) {}
    }

    static SubmissionRecord compile(FileManager fm, CCLogger logger) throws IOException, InterruptedException {
        String[] compileCommand = null;
        switch (fm.getLanguage().toLowerCase()) {
            case "java": {
                compileCommand = ExecutionConfig.getCompileCommand(fm);
                // IN-HOUSE HAVA COMPILER
//                logger.logln("-> Compiling Java using javax.tools.JavaCompiler");
//                return startJavaCompilation(fm);
                break;
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
                judge_logger.errln(ceMsg);
                return new SubmissionRecord(JudgeVerdict.CE, ceMsg, null);
            }

            return new SubmissionRecord(JudgeVerdict.NONE, "Compilation Successful (javax.tools)", null);
        }
    }

    private static SubmissionRecord startCompilation(ProcessBuilder pb) throws IOException, InterruptedException {
        Process compileProcess = pb.start();
        String errorOutput = readStream(compileProcess.getErrorStream());

        if (compileProcess.waitFor(COMPILE_TIME_LIMIT_SEC, TimeUnit.SECONDS) && compileProcess.exitValue() != 0) {
            String ceMsg = "Compiler Errors:\n" + errorOutput;
            judge_logger.errln(ceMsg);
            return new SubmissionRecord(JudgeVerdict.CE, ceMsg, null);
        }
        return new SubmissionRecord(JudgeVerdict.NONE, "Compilation Successful", null);
    }

    static String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static void recordCopy(SubmissionRecord[] dest, SubmissionRecord src, int ex_out_size) {
        for (int i = 0; i < ex_out_size; i++) {
            dest[i]
                    .setVerdict(src.verdict())
                    .setOutput(src.output());
        }
    }

    /*********************** STATIC CLASSES, ENUMS OR WHATEVER **********************/


    // TESTCASE WORKERS;
    static class JudgeSlave implements Callable<SubmissionRecord> {
        private static final long INPUT_DELAY_MS = 50;
        private final String[] executeCommand, inputs;
        private final String rootdir, language;
        private final CCLogger logger;
        private Process process = null;
        int tc_num;
        private SubmissionRecord result;

        JudgeSlave(String[] cmd, FileManager fm, String[] inputs, String expected_output, int testcase_number, CCLogger logger) {
            this(cmd, fm.getRootdir().toString(), fm.getLanguage(), inputs, expected_output, testcase_number, logger);
        }

        JudgeSlave(String[] cmd, String rootdir, String language, String[] inputs, String expected_output, int testcase_number, CCLogger logger) {
            this.executeCommand = cmd;
            this.inputs = inputs;
            this.rootdir = rootdir;
            this.language = language;
            this.tc_num = testcase_number;
            this.logger = logger;
            result = new SubmissionRecord(JudgeVerdict.ESF, "Execution System Failure (IOException)\n", expected_output);
            activeSlaves.put(this, true);
        }


        /**
         * stops the Callable
         * @apiNote <b  style="color:red">PLS PLS DONT USE ON ITERATORS</b>
         * */
        public void halt() {
            if (process != null && process.isAlive()) {
                process.destroy();
                logger.logln("[JudgeSlave]: Process destroyed!");
            }
            activeSlaves.remove(this);
        }

        /**
         * forcefully stops the Callable
         * @apiNote PLS PLS DONT USE ON ITERATORS
         * */
        public void mutilate() {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                logger.logln("[JudgeSlave]: Process forcefully destroyed!");
            }
            activeSlaves.remove(this);
        }

        // Slave done with work
        private SubmissionRecord finishQuota() {
            mutilate();
            return result;
        }

        @Override
        //throws Exception
        public SubmissionRecord call()  {
            // VARS
            boolean is_python = language.equals("python");
            String cmd_newline = "\r\n";
            if (is_python) cmd_newline = "\n";

            // PROCESS
            try {
                process = new PtyProcessBuilder(executeCommand)
                        .setEnvironment(env)
                        .setRedirectErrorStream(true) // Redirects stderr to stdout stream
                        .setDirectory(rootdir)
                        .setConsole(is_python)
                        .start();
            } catch (IOException e) {
                return finishQuota();
            }

            StringBuilder transcript = new StringBuilder();
            Future<Void> output_reader_thread = slaveWorkers.submit(new Judge.OutputReader(process.getInputStream(), transcript));
            BufferedWriter processInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            int inputIndex = 0;
            long startTime = System.currentTimeMillis();

            try {
                do {
                    if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS) {
                        return finishQuota().setVerdict(JudgeVerdict.TLE).setOutput(Helpers.stripAnsiCRLines(transcript.toString()).trim());
                    }

                    synchronized (this)
                    {
                        this.wait(INPUT_DELAY_MS);
                    }

                    if (inputIndex < inputs.length) {
                        String inputLine = inputs[inputIndex++];
                        logger.logf("-> [TC_%d]Judge providing input: %s\n", tc_num, inputLine);
                        processInputWriter.write(inputLine + cmd_newline);
                    }
                    processInputWriter.flush();

                } while (inputs != null && inputIndex < inputs.length);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return finishQuota().setOutput("Execution Interrupted\n");
            } catch (IOException e) {
                return finishQuota();
            } finally {
                try {
                    processInputWriter.close();
                } catch (IOException e) {}
            }
            // Wait for termination using remaining time
            long remainingTime = TIME_LIMIT_MS - (System.currentTimeMillis() - startTime);
            try {
                process.waitFor(remainingTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) { return finishQuota().setOutput("Execution Interrupted\n"); }

            output_reader_thread.cancel(true);

            // PROGRAM TLE
            if (process.isAlive()) {
                return finishQuota()
                    .setVerdict(Judge.JudgeVerdict.TLE)
                    .setOutput(Helpers.stripAnsiCRLines(
                            transcript
                                .append("\nTLE (Time Limit Exceeded)\n")
                                .toString()
                        ).trim()
                    );
            }
            // PROGRAM RUNTIME ERROR
            else if (process.exitValue() != 0) {
                String errorOutput = "UnknownError";
                try {
                    errorOutput = readStream(process.getErrorStream());
                } catch (IOException e) {}
                logger.errf("[TC %d]Runtime Error Details:\n%s\n", tc_num, errorOutput);
                return finishQuota()
                    .setVerdict(Judge.JudgeVerdict.RE)
                    .setOutput(Helpers.stripAnsiCRLines(
                        transcript
                            .append("RTE (Runtime Error) - Exit Code: ")
                            .append(process.exitValue())
                            .append("\n")
                            .toString()
                        ).trim()
                    );
            } else {
                return finishQuota().setVerdict(JudgeVerdict.NONE).setOutput(Helpers.stripAnsiCRLines(transcript.toString()).trim());
            }
        }
    }


    static class OutputReader implements Callable<Void> {
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