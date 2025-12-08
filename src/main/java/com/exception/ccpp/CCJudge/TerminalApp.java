    package com.exception.ccpp.CCJudge;

    import com.exception.ccpp.CCJudge.Judge.JudgeVerdict;
    import com.exception.ccpp.Common.Helpers;
    import com.exception.ccpp.CustomExceptions.NotDirException;
    import com.exception.ccpp.Debug.JTextLogger;
    import com.exception.ccpp.FileManagement.FileManager;
    import com.exception.ccpp.GUI.UpdateGUICallback;
    import com.pty4j.PtyProcessBuilder;

    import javax.swing.*;
    import javax.swing.text.*;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.awt.event.WindowAdapter;
    import java.awt.event.WindowEvent;
    import java.io.*;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.Map;

    import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;

    /**
     * A Java Swing application that acts as a console for an external command-line process (e.g., cmd.exe or bash).
     * The application sends user input from a JTextField to the external process's stdin
     * and displays the process's stdout and stderr in a JTextArea.
     */
    public class TerminalApp extends JFrame {

        private boolean is_processing;
        private JTextArea outputArea;
        private JTextField inputField;
        private Process terminalProcess;
        private BufferedWriter processWriter;
        private FileManager fm;
        private ArrayList<String> inputs;
        private String[] terminal_command;
        private boolean prompt_again;
        private TerminalCallback exitCallback;
        private UpdateGUICallback guiCallback;
        private String[] execCmd;
        private JTextLogger output_logger;
        static TerminalApp instance;
        static boolean bypassFilter;
        private TerminalDocumentFilter outputAreaFilter;

        static final String OS_NAME = System.getProperty("os.name").toLowerCase();
        static final String[] TERMINAL_START_COMMAND;
        static {
            if (OS_NAME.contains("nix") || OS_NAME.contains("mac")) {
                // Linux/macOS-specific path
                TERMINAL_START_COMMAND = new String[]{"bash", "-c"};
            } else {
                // Fallback (WIN)
                TERMINAL_START_COMMAND = new String[]{"powershell.exe", "-c"}; //
            }
        }


        public static TerminalApp getInstance() {
            if (instance == null) {
                instance = new TerminalApp();
            }
            return instance;
        }

        public TerminalApp setFileManager(FileManager fm)
        {
            if (is_processing) return this;
            this.fm = fm;
            return this;
        }
        public TerminalApp setExitCallback(TerminalCallback exitCallback)
        {
            if (is_processing) return this;
            this.exitCallback = exitCallback;
            return this;
        }
        public TerminalApp setUpdateCallback(UpdateGUICallback guiCallback)
        {
            if (is_processing) return this;
            this.guiCallback = guiCallback;
            return this;
        }
        public TerminalApp setAll(FileManager fm, TerminalCallback exitCallback,  UpdateGUICallback guiCallback)
        {
            if (is_processing) return this;
            this.fm = fm;
            this.exitCallback = exitCallback;
            this.guiCallback = guiCallback;
            return this;
        }

        public void start()
        {
            if (is_processing) return;;
            is_processing = true;
            setTitle("Java Swing Command Console");
            inputs = new ArrayList<>();

            outputArea = new JTextArea();
            outputArea.setEditable(false);
            outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(outputArea);
            scrollPane.setPreferredSize(new Dimension(800, 600));

            inputField = new JTextField();
            inputField.addActionListener(
                    new CommandListener(fm.getLanguage())
            );

            setLayout(new BorderLayout());
            add(scrollPane, BorderLayout.CENTER);
            add(inputField, BorderLayout.SOUTH);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("windowClosing");
                    if (terminalProcess != null && terminalProcess.isAlive()) terminalProcess.destroyForcibly();
                    slaveWorkers.submit(()->Judge.cleanup(fm));
                    instance = null;
                    is_processing = false;
                }
            });

            output_logger = new JTextLogger(outputArea);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();

            setLocationRelativeTo(null);
            setVisible(true);

            AbstractDocument doc = (AbstractDocument) outputArea.getDocument();
            outputAreaFilter = new TerminalDocumentFilter();
            doc.setDocumentFilter(outputAreaFilter);


            slaveWorkers.submit(() -> {
                if (initTerminalProcess())
                    startTerminalProcess();
                else {
                    Judge.cleanup(fm);
                }
            });
        }

        private TerminalApp() {
            is_processing = false;
        }


        void setBypassFilter(boolean shouldBypass) {
            bypassFilter = shouldBypass;
//            outputAreaFilter.lastInsertionStart = outputArea.getDocument().getLength();
        }

        static class TerminalDocumentFilter extends DocumentFilter {
            int lastInsertionStart = 0;

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (bypassFilter) {
                    lastInsertionStart = offset;
//                    return;
                }

                int start = Math.min(lastInsertionStart, offset);
                int length = offset - start;

                String prevText = "";
                if (length > 0) {
                    prevText = fb.getDocument().getText(start, length);
                }

                String combined = prevText + string;
                String filtered = Helpers.stripCRLines(combined);
                fb.replace(start, length, filtered, attr);
                lastInsertionStart = start;

            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                fb.replace(offset, length, text, attrs);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
//                lastInsertionEnd = offset;
//                fb.remove(offset, length);
                fb.remove(offset, length);
            }
        };

        private boolean initTerminalProcess() {
            setBypassFilter(true);
            SubmissionRecord sr = null;
            try {
                sr = Judge.compile(fm, output_logger);
            } catch (Exception e) {
                System.err.println("Compilation error:\n" + e.getMessage());
                return false;
            }

            if (sr.verdict() == JudgeVerdict.CE)  {
                outputArea.append(sr.output());
                return false;
            }

            outputArea.setText("");
            execCmd = ExecutionConfig.getExecuteCommand(fm);
            System.out.println(String.join(" ", execCmd));
            terminal_command = execCmd;
            setBypassFilter(false);
            return true;
        }

        private void startTerminalProcess() {
            try {

                Map<String, String> env = new HashMap<>(System.getenv());
                if (!env.containsKey("TERM")) env.put("TERM", "xterm");
                terminalProcess = new PtyProcessBuilder()
                        .setCommand(terminal_command)
                        .setEnvironment(env)
                        .setRedirectErrorStream(true)
                        .setDirectory(fm.getRootdir().toString())
                        .setConsole(false)
                        .start();


                // TERMINAL WILL EXIT HERE
                terminalProcess.onExit().thenApply( p -> {

                    System.out.println("Process exited with: " + p.exitValue());
                    if (p.exitValue() != 0) return p.exitValue();

                    String[] inputs_arr = inputs.toArray(new String[0]);
                    inputs.clear();
                    System.out.printf("Inputs[%d]: %s\n", inputs_arr.length, String.join(", ", inputs_arr));

                    Judge.judgeInteractively(execCmd,fm,inputs_arr,res -> {
                        if (exitCallback != null) {
                            System.out.println(res[0].expected_output());
                            exitCallback.onTerminalExit(inputs_arr, res[0].output());
                            SwingUtilities.invokeLater(() -> {
                                setBypassFilter(true);
                                outputArea.append("\nContinue adding testcases [y/n]?\n");
                                setBypassFilter(false);
                            });
                            prompt_again = true;
                            if (guiCallback != null) { guiCallback.updateGUI(); }
                        }
                    }, null);

                    outputArea.append("");

                    return p.exitValue();
                });

                // Setup the writer for sending commands to the process's input (stdin)
                processWriter = new BufferedWriter(new OutputStreamWriter(terminalProcess.getOutputStream()));

                slaveWorkers.submit(new ConsoleOutputReader(terminalProcess.getInputStream()));

            } catch (Exception e) {
                outputArea.append("Error starting external process:\n" + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }

        private class CommandListener implements ActionListener {
            String language;
            public CommandListener(String language) {
                this.language = language;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                String command = inputField.getText();
                inputField.setText(""); // Clear the input field

                if (prompt_again) {
                    prompt_again = false;
                    if (command.length() > 0 && Character.toLowerCase(command.charAt(0)) == 'y') {
                        outputArea.append(command + "\n");
                        if (exitCallback != null)
                        {
                            startTerminalProcess();
                            outputArea.setText("");
                            return;
                        }
                    }
                    SwingUtilities.invokeLater(TerminalApp.this::dispose);
                    return;
                }

                System.out.printf("Entered: [%s]\n", command);
                inputs.add(command);

                try {
                    processWriter.write(command + "\r\n");
                    processWriter.flush();
                } catch (IOException ex) {
                    outputArea.append("Error sending command to process: " + ex.getMessage() + "\n");
                    // Attempt to close the connection if the write fails
                    try {
                        terminalProcess.destroy();
                    } catch (Exception destroyEx) { /* ignored */ }
                }
            }
        }

        private class ConsoleOutputReader implements Runnable {
            private final InputStream inputStream;
            private int prev;
            private int curr;
            public ConsoleOutputReader(InputStream inputStream) {
                this.inputStream = inputStream;
            }

            @Override
            public void run() {

                try (InputStreamReader reader = new InputStreamReader(inputStream)) {

                    char[] buffer = new char[1024];
                    int readChars;

                    while (terminalProcess.isAlive() || inputStream.available() > 0) {
                        if (reader.ready()) {
                            readChars = reader.read(buffer);
                            if (readChars > 0) {
                                final String output = (new String(buffer, 0, readChars));

                                prev = curr;
                                curr = outputArea.getCaretPosition();

                                SwingUtilities.invokeLater(() -> {
                                    outputArea.append(Helpers.stripAnsi(output));
                                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                                });
                            }
                        }

                        Thread.sleep(50);
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        System.err.println("TerminalApp Error: " + e.getMessage());
                    });
                } catch (InterruptedException e) {
                    SwingUtilities.invokeLater(() -> System.out.println("Console output reader interrupted."));
                    Thread.currentThread().interrupt();
                } finally {
                    outputArea.append("\n");
                    // Ensure resources are cleaned up
                    if (terminalProcess != null) {
                        terminalProcess.destroy();
                    }
                }
            }
        }

        public static void main(String[] args) {
            boolean OPEN_TERMINAL, APPEND_TESTCASES, RUN_TESTCASES;
            String[] files = {"datafile3.ccpp", "datafile3.ccpp"};

            // TEST VARIABLES
            final String TEST_LANG = "CPP";
            final String TEST_FILE = files[0];
            final int TEST_TYPE = 1;

            switch (TEST_TYPE) {
                case 1: // TEST SUBMIT CODE
                {
                    OPEN_TERMINAL = false;
                    APPEND_TESTCASES = false;
                    RUN_TESTCASES = true;
                    break;
                }
                case 2: // TEST GENERATE TESTCASES
                {
                    OPEN_TERMINAL = true;
                    APPEND_TESTCASES = true;
                    RUN_TESTCASES = true;
                    break;
                }
                default: // TEST RUN CODE
                {
                    OPEN_TERMINAL = true;
                    APPEND_TESTCASES = false;
                    RUN_TESTCASES = false;
                    break;
                }
            }

            // Start the GUI on the Event Dispatch Thread (EDT)
            System.out.println(System.getenv("PROJECT_DIR"));
            String directoryPath =  System.getenv("PROJECT_DIR") + "/COMPILER_TEST/"+TEST_LANG.toUpperCase();
            TestcaseFile tf;
            FileManager fm = null;

            if (APPEND_TESTCASES || RUN_TESTCASES) tf = TestcaseFile.open(TEST_FILE);
            else tf = null;

            try {
                fm = FileManager.getInstance().setAll(directoryPath, TEST_LANG.toLowerCase());
                fm.setCurrentFile(fm.getFiles().getLast());
            } catch (NotDirException e) {
                System.err.println("ERR: fm.setAll()");
            }


            FileManager finalFm = fm;
            if (OPEN_TERMINAL)
            {
                TerminalApp ta  = TerminalApp.getInstance();
                if (APPEND_TESTCASES) ta.setAll(finalFm, tf, null).start();
                else ta.setAll(finalFm, null, null).start();

                // block main thread to mimic workload on main
                // the testfile will be updated after the terminal is finished
                try {
                    while (ta.isDisplayable()) Thread.sleep(100);
                } catch (InterruptedException e) {}
                System.out.println("No longer displayable");

                if (APPEND_TESTCASES) tf.write();
            }

            if (RUN_TESTCASES)
            {
                // this is how you call multithreaded judge
                Judge.judge(fm,tf, results -> {
                    int i = 0;
                    for (SubmissionRecord sr : results) {
                        System.out.printf("[[[------------- ACTUAL OUT Testcase %d---------------\n", ++i);
                        System.out.println(sr.output()); // actual output
                        System.out.printf("-------------- EXPECTED OUT Testcase %d---------------\n", i);
                        System.out.println(sr.expected_output()); // expected output
                        System.out.printf("------------- END Testcase %d---------------]]]\n\n", i);
                    }

                    slaveWorkers.shutdown();
                });
            }


        }
    }