    package com.exception.ccpp.CCJudge;

    import com.exception.ccpp.CCJudge.Judge.JudgeVerdict;
    import com.exception.ccpp.Common.Helpers;
    import com.exception.ccpp.CustomExceptions.NotDirException;
    import com.exception.ccpp.FileManagement.FileManager;
    import com.pty4j.PtyProcessBuilder;
    import com.sun.source.doctree.SummaryTree;

    import javax.swing.*;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.awt.event.WindowAdapter;
    import java.awt.event.WindowEvent;
    import java.io.*;
    import java.nio.MappedByteBuffer;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.Map;

    /**
     * A Java Swing application that acts as a console for an external command-line process (e.g., cmd.exe or bash).
     * The application sends user input from a JTextField to the external process's stdin
     * and displays the process's stdout and stderr in a JTextArea.
     */
    public class TerminalApp extends JFrame {

        private JTextArea outputArea;
        private JTextField inputField;
        private Process terminalProcess;
        private BufferedWriter processWriter;
        private final FileManager fm;
        private ArrayList<String> inputs;
        private TerminalCallback exitCallback;
        private String[] terminal_command;
        private boolean prompt_again;
        private boolean terminal_loop;
        private Thread output_thread;

        private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
        private static final String[] TERMINAL_START_COMMAND;

        static {
            if (OS_NAME.contains("nix") || OS_NAME.contains("mac")) {
                // Linux/macOS-specific path
                TERMINAL_START_COMMAND = new String[]{"bash", "-c"};
            } else {
                // Fallback (WIN)
                TERMINAL_START_COMMAND = new String[]{"powershell.exe", "-c"}; //
            }
        }

        public TerminalApp(FileManager fm, TerminalCallback exitCallback) {
            super("Java Swing Command Console");
            this.fm = fm;
            this.exitCallback = exitCallback;
            terminal_loop = false;
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
                    if (output_thread.isAlive()) output_thread.interrupt();
                    if (terminalProcess.isAlive()) terminalProcess.destroyForcibly();
                    SwingUtilities.invokeLater(() -> Judge.cleanup(fm));
                }
            });
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();

            setLocationRelativeTo(null);
            setVisible(true);

            if (initTerminalProcess())
            {
                startTerminalProcess();
            }
        }

        private boolean initTerminalProcess()
        {
            SubmissionRecord sr = null;
            try {
                sr = Judge.compile(fm);
            } catch (Exception e) {
                System.err.println("Compilation error:\n" + e.getMessage());
            }

            if (sr.verdict() == JudgeVerdict.CE)  {
                outputArea.append(sr.output());
                return false;
            }

            String[] execCmd = ExecutionConfig.getExecuteCommand(fm);
            System.out.println(String.join(" ", execCmd));
            terminal_command = execCmd;
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
                    SubmissionRecord verdict = Judge.judgeInteractively(fm, inputs_arr, null);

                    SwingUtilities.invokeLater(() -> {
                        // Only prompt if a callback handler is present (for testcase generation)
                        if (exitCallback != null) {
                            exitCallback.onTerminalExit(inputs_arr, verdict.output());
                            outputArea.append("\nContinue adding testcases [y/n]?\n");
                        }
                        else{
                            outputArea.append("\nEnter any key to continue...\n");
                        }
                        prompt_again = true; // Set flag to divert the *next* input
                    });

                    return p.exitValue();
                });

                // Setup the writer for sending commands to the process's input (stdin)
                processWriter = new BufferedWriter(new OutputStreamWriter(terminalProcess.getOutputStream()));

                // Start a thread to continuously read the process's output (stdout and stderr)
                output_thread = new Thread(new ConsoleOutputReader(terminalProcess.getInputStream()));
                output_thread.start();
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

                                SwingUtilities.invokeLater(() -> {
                                    outputArea.append(Helpers.stripAnsi(output));
                                    outputArea.setText(Helpers.stripCRLines(outputArea.getText()));
                                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                                });
                            }
                        }

                        Thread.sleep(50);
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        System.err.println("TerminalApp Error: " + e.getMessage());
                        // e.printStackTrace();
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
            String[] files = {"datafile3.ccpp", "datafile2.ccpp"};

            // TEST VARIABLES
            final String TEST_LANG = "CPP";
            final String TEST_FILE = files[0];
            final int TEST_TYPE = 2;

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

            if (APPEND_TESTCASES || RUN_TESTCASES) tf = new TestcaseFile(TEST_FILE);
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
                TerminalApp ta  = null;
                if (APPEND_TESTCASES) ta = new TerminalApp(finalFm, tf);
                else ta = new TerminalApp(finalFm, null);

                // block main thread to mimic workload on main
                // the testfile will be updated after the terminal is finished
                try {
                    Thread.sleep(10000);
                    while (ta.isDisplayable()) Thread.sleep(100);
                } catch (InterruptedException e) {}
                System.out.printf("CONTINUING AFTER TerminalApp DISPOSAL.\n");
                if (APPEND_TESTCASES) tf.writeOut();
            }

            if (RUN_TESTCASES)
            {
                // this is how you call judge
                SubmissionRecord[] sr_arr= Judge.judge(fm,tf);

                // print test cases
                int i = 0;
                for (SubmissionRecord sr : sr_arr) {
                    System.out.printf("[[[------------- ACTUAL OUT Testcase %d---------------\n", ++i);
                    System.out.println(sr.output()); // actual output
                    System.out.printf("-------------- EXPECTED OUT Testcase %d---------------\n", i);
                    System.out.println(sr.expected_output()); // expected output
                    System.out.printf("------------- END Testcase %d---------------]]]\n\n", i);
                }
            }



        }
    }