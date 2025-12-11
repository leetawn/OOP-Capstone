package com.exception.ccpp.CCJudge;

import com.exception.ccpp.Common.Helpers;
import com.exception.ccpp.CCJudge.Judge.OutputReader;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.exception.ccpp.CCJudge.ExecutionConfig.NO_C_COMPILER_ERROR;
import static com.exception.ccpp.Gang.SlaveManager.romanArmy;
import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;

public class Judge {
    private static Map<String, String> env;
    private static Map<JudgeSlave, Boolean> activeSlaves = new ConcurrentHashMap<>();
    private static DebugLog judge_logger = DebugLog.getInstance();
    static {
        env = new HashMap<>(System.getenv());
        if (!env.containsKey("TERM")) env.put("TERM", "dumb");
    }

    static final long TIME_LIMIT_MS = 2000; // TLE
    static final long COMPILE_TIME_LIMIT_SEC = 10;
//    static boolean is_running = false;



    /******************** SINGLE-THREADED *************************/
    // TODO: this will be the real judge
    // will output an Array of SubmissionRecord, check the Submission Record definition
    // for TERMINAL USE ONLY
    static void judgeInteractively(
            String[] cmd,
            FileManager fm,
            String[] testInputs,
            Consumer<SubmissionRecord[]> callback,
            CCLogger logger,
            ConcurrentLinkedDeque<Object> runningThreads
    ) {
        if (logger == null) logger = judge_logger;

        final CCLogger f_logger = logger;

        runningThreads.add(slaveWorkers.submit(() -> {
            Future<SubmissionRecord> f = slaveWorkers.submit(new JudgeSlave(cmd, fm, testInputs, null, 1, f_logger, runningThreads));
            runningThreads.add(f);
            SubmissionRecord verdict = new SubmissionRecord(JudgeVerdict.RE, "Runtime Error Buddy", null);
            try {
                verdict = f.get();
            } catch (InterruptedException e) {}
            catch (ExecutionException e) { /*Normal shit*/
                f_logger.errln("[Judge.judge]: Execution Error while waiting for submission!");
            }
            callback.accept(new SubmissionRecord[]{verdict});
        }));
    }

    /******************** MULTI-THREADED *************************/
    // PARALLEL JUDGE

    public static void judge(FileManager fm, TestcaseFile tf, BiConsumer<SubmissionRecord[], Integer> callback)  {
        romanArmy.submit(() -> {
            SubmissionRecord judge_res;
            String rootdir = fm.getRootdir().toString();
            String language = fm.getLanguage();

            Map<Testcase, String> testcases = tf.getTestcases();
            int testcases_size = testcases.size();
            int i = 0;
            SubmissionRecord[] verdicts = new SubmissionRecord[testcases_size];
            for (Testcase tc : testcases.keySet()) {
                verdicts[i++] = new SubmissionRecord(JudgeVerdict.UE, "Unknown Error", tc);
            }
            // COMPILE
            judge_logger.logln("[Judge.judge]: Compiling...");
            try {
                judge_res = compile(fm, judge_logger); // WAITS TO FINISH
                if (judge_res.verdict() == JudgeVerdict.CE) {
                    judge_logger.errln("[Judge.judge]: Compiler Error!");
                    recordCopy(verdicts, judge_res, testcases_size);
                    parallelCleanup(fm);
                    callback.accept(verdicts, JudgeVerdict.CE);
                    return;
                }
            } catch (Exception e) {
                String fmsg = "Judge System Failure: " + e.getMessage();
                judge_logger.errln(fmsg);
                e.printStackTrace();
                judge_res = new SubmissionRecord(JudgeVerdict.JSF, fmsg, null);
                recordCopy(verdicts, judge_res, testcases_size);
                parallelCleanup(fm);
                callback.accept(verdicts, JudgeVerdict.JSF);
                return;
            }
            judge_logger.logln("[Judge.judge]: Compilation Done.");

            // judgeInteractively (Parallel)
            String[] cmd = ExecutionConfig.getExecuteCommand(fm);
            List<Callable<SubmissionRecord>> tasks = new ArrayList<>();
            List<Future<SubmissionRecord>> futures = new ArrayList<>();
            i =1;
            for (Testcase tc : testcases.keySet()) {
                judge_logger.logf("[Judge.judge]: Running Testcase %d...\n", i);
                tasks.add(new JudgeSlave(cmd, rootdir, language, tc, i++, judge_logger));
            }

            try {
                futures = slaveWorkers.invokeAll(tasks);
            } catch (InterruptedException e) {}
            ArrayList<SubmissionRecord> res =  new ArrayList<>();
//            Queue<Future<SubmissionRecord>> futures = new LinkedList<>();
//            Queue<Callable<SubmissionRecord>> tasks = new LinkedList<>();
//            while(!tasks.isEmpty()) {
//                judge_logger.logf("[Judge.judge]: Fetching Testcase %d Results...\n", i+1);
//                futures.add(slaveWorkers.submit(tasks.remove()));
//                if (futures.size() > 10 || tasks.isEmpty())
//                {
//                    while (!futures.isEmpty())
//                    {
//                        Future<SubmissionRecord> f = futures.remove();
//                        try {
//                            res.add(f.get());
//                        } catch (InterruptedException | ExecutionException e) {
//                            judge_logger.errln("[Judge.judge]: Execution Error while Fetching Testcase Results.");
//                        }
//                        if (!tasks.isEmpty()) futures.add(slaveWorkers.submit(tasks.remove()));
//
//                    }
//                }
//            }

            i = 0;
            int status = 0;
            for  (Future<SubmissionRecord> r : futures) {
                SubmissionRecord sr = null;
                try {
                    sr = r.get();
                } catch (InterruptedException | ExecutionException e) {}

                verdicts[i++]
                        .setVerdict(sr.verdict())
                        .setOutput(sr.output());
                // RE/TLE
                if (status == 0 && sr.verdict() != JudgeVerdict.NONE) { status = sr.verdict(); }
                else if (sr.verdict() == JudgeVerdict.RE) { status = sr.verdict(); }

//                    Throwable cause = e.getCause();
//                    cause.printStackTrace();  // see what actually went wrong

            }

            if (DebugLog.DEBUG_ENABLED)
                for (int j = 0; j < verdicts.length; j++) {
                    judge_logger.logf("[Judge.judge] Testcase %d exit code: %s\n", j+1, verdicts[j].verdictName());
                }


            parallelCleanup(fm);
            judge_logger.logln("[Judge.judge]: Returning Results...\n");
            callback.accept(verdicts, status);
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
        if (logger == null) logger = judge_logger;
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

    static class JudgeTranscript {
        final private ConcurrentLinkedDeque<String> transcript = new ConcurrentLinkedDeque<>();

        public static String removeOverlap(String x, String y) {
            String a = Helpers.stripAnsi(x);
            String b = Helpers.stripAnsi(y);
            int max = Math.min(a.length(), b.length());
            for (int len = max; len > 0; len--) {
                if (a.regionMatches(a.length() - len, b, 0, len))
                {
                    return y;
                }
            }
            return y; // no overlap
        }

        public static String removeOverlapPrintable(String a, String b) {
            // Find printable end of A
            int aEnd = a.length();
            while (aEnd > 0) {
                char c = a.charAt(aEnd - 1);
                if ((c >= 32 && c <= 126) || c == '\r' || c == '\n' || c == '\t') break;
                aEnd--;
            }

            // Find printable start of B
            int bStart = 0;
            while (bStart < b.length()) {
                char c = b.charAt(bStart);
                if ((c >= 32 && c <= 126) || c == '\r' || c == '\n' || c == '\t') break;
                bStart++;
            }

            int max = Math.min(aEnd, b.length() - bStart);

            for (int len = max; len > 0; len--) {
                if (a.regionMatches(aEnd - len, b, bStart, len)) {
                    return b;
                }
            }

            return b; // no overlap
        }

        public static String mergeNoOverlap(String a, String b) {
            int max = Math.min(a.length(), b.length());

            for (int len = max; len > 0; len--) {
                if (a.regionMatches(a.length() - len, b, 0, len)) {
                    return a + b.substring(len);
                }
            }
            return a + b;
        }

        public JudgeTranscript() {}

        public JudgeTranscript append(String s)
        {
            transcript.addLast(s);
            return this;
        }
        public JudgeTranscript append(char c)
        {
            return append(Character.toString(c));
        }
        public JudgeTranscript append(int c)
        {
            return append(Integer.toString(c));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            transcript.forEach(sb::append);
            return sb.toString();
        }
    }


    // TESTCASE WORKERS;
    static class JudgeSlave implements Callable<SubmissionRecord> {
        private static final long INPUT_DELAY_MS = 50;
        private final String[] executeCommand;
        private final String rootdir, language;
        private final CCLogger logger;
        private Process process = null;
        private Testcase tc;
        int tc_num;
        private SubmissionRecord result;
        ConcurrentLinkedDeque<Object>  terminalThreads = null;
        StringBuffer transcript = new StringBuffer();

        JudgeSlave(
                String[] cmd,
                FileManager fm, String[] inputs,
                String expected_output,
                int testcase_number,
                CCLogger logger,
                ConcurrentLinkedDeque<Object> runningThreads
        ) {
            this(cmd, fm.getRootdir().toString(), fm.getLanguage(), new Testcase(inputs, expected_output), testcase_number, logger);
            terminalThreads = runningThreads;
        }

        JudgeSlave(String[] cmd, String rootdir, String language, Testcase tc, int testcase_number, CCLogger logger) {
            if (logger == null) logger = judge_logger;
            this.executeCommand = cmd;
            this.tc = tc;
            this.rootdir = rootdir;
            this.language = language;
            this.tc_num = testcase_number;
            this.logger = logger;
            result = new SubmissionRecord(JudgeVerdict.ESF, "Execution System Failure (IOException)\n", tc);
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

        private void addThread(Object o)
        {
            if (terminalThreads != null) terminalThreads.add(o);
        }

        @Override
        //throws Exception
        // RETURNS Verdicts: RE/TLE/NONE
        public SubmissionRecord call()  {
            // VARS
            boolean is_python = language.equals("python");
            String cmd_newline = "\r\n";
            if (is_python) cmd_newline = "\n";
            String[] inputs = tc.getInputs();


            String n = String.join(cmd_newline,inputs);
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
            addThread(process);


            BufferedWriter processInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            int inputIndex = 0;
            long startTime = System.currentTimeMillis();

            try {
                processInputWriter.write(n+cmd_newline);
                processInputWriter.flush();
            }
            catch (IOException e) {
                return finishQuota();
            } finally {
                try {
                    processInputWriter.close();
                } catch (IOException e) {}
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}

            new Judge.OutputReader(process.getInputStream(), transcript).call();

            long remainingTime = TIME_LIMIT_MS - (System.currentTimeMillis() - startTime);

            try {
                process.waitFor(remainingTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) { return finishQuota().setOutput("Execution Interrupted\n"); }

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
                final String raw = transcript.toString();
                final String res = Helpers.stripAnsiCRLines(raw);
                return finishQuota().setVerdict(JudgeVerdict.NONE).setOutput((res).trim());
            }
        }
    }


    static class OutputReader implements Callable<Void> {
        private final InputStream in;
        private final BufferedReader reader;
        private final StringBuffer transcript;
        private final StringBuilder sb = new StringBuilder();


        public OutputReader(InputStream in, StringBuffer transcript) {
            this.in = in;
            this.transcript = transcript;
            this.reader = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public Void call() {
            byte[] buffer = new byte[1000];
            int c;
            try {
                int read;

                while ((read = in.read(buffer)) != -1) {
                    String b = new String(buffer, 0, read);
                    transcript.append(b);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class JudgeVerdict {
        final public static int
                AC   = 0b000000000, // Accepted
                WA   = 0b000000001, // Wrong Answer
                WAE  = 0b000000011, // Wrong Answer Excess
                CE   = 0b000000101, // Compile Error
                RE   = 0b000001001, // Runtime Error
                TLE  = 0b000010001, // Time Limit Exceeded
                MLE  = 0b000100001, // Memory Limit Exceeded
                UE   = 0b111111111, // Unknown Error. We the devs, idk and idc what the fuck you are facing rn
                ESF  = 0b101000001, // Execution System Failure, Not the Users Fault!
                JSF  = 0b110000001, // Judge System Failure
                NONE = 0b100000001; // USED FOR CREATION ONLY

        public static String getName(int verdictCode)
        {
            return switch (verdictCode) {
                case AC -> "AC";
                case WA, WAE -> "WA";
                case CE -> "CE";
                case RE -> "RE";
                case TLE -> "TLE";
                case MLE -> "MLE";
                case UE -> "UE";
                case ESF -> "ESF";
                case JSF -> "JSF";
                default -> "NONE";
            };
        }
    }

    public static void main(String[] args) {

        System.out.println(JudgeTranscript.removeOverlap("\rOp: p\r\nSize: 5\r\n+--R: 5","+--R: 50\r\n|   +--R: 60\r\n|   |   +--R: 70\r\n|   |   |   +--R: 80\r\n|   |   |   |   +--R: 90\r\nStatus: 1\r\nOp:"));
    }

}