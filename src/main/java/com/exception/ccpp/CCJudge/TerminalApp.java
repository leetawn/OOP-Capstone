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

        // --- Configuration ---
        // Switched to powershell.exe as the default for better modern Windows compatibility.
        // Uncomment the other options to switch shells (remember to recompile).
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
            inputs = new ArrayList<>();

            outputArea = new JTextArea();
            outputArea.setEditable(false);
            outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(outputArea);
            scrollPane.setPreferredSize(new Dimension(800, 600));

            inputField = new JTextField();
            inputField.addActionListener(new CommandListener(fm.getLanguage()));

            setLayout(new BorderLayout());
            add(scrollPane, BorderLayout.CENTER);
            add(inputField, BorderLayout.SOUTH);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
//
//                    String output = outputArea.getText();
//                    System.out.print("Full Received [");
//                    int j=1;
//                    for (int c : output.getBytes()) {
//                        System.out.printf("%d, ", c);
//                        if (j%10==0) System.out.println();
//                        j++;
//                    }
//                    if (output.getBytes().length > 0) System.out.print("\b\b");
//                    System.out.println("]");

                    System.out.println("windowClosing");
                    Judge.cleanup(fm);
                }
            });
            ;
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);

            startTerminalProcess();
        }

        private void startTerminalProcess() {
            try {

                SubmissionRecord sr = Judge.compile(fm);
                if (sr.verdict() == JudgeVerdict.CE)  {
                    outputArea.append(sr.output());
                    return;
                }

                String[] execCmd = ExecutionConfig.getExecuteCommand(fm);
                System.out.println(String.join(" ", execCmd));
                String[] cmd = Helpers.concatStringArrays(execCmd);

                Map<String, String> env = new HashMap<>(System.getenv());
                if (!env.containsKey("TERM")) env.put("TERM", "xterm");

                terminalProcess = new PtyProcessBuilder()
                        .setCommand(cmd)
                        .setEnvironment(env)
                        .setRedirectErrorStream(true)
                        .setDirectory(fm.getRootdir().toString())
                        .setConsole(false)
                        .start();

                // TERMINAL WILL EXIT HERE
                terminalProcess.onExit().thenApply( p -> {

                    System.out.println("Process exited with: " + p.exitValue());

                    String[] inputs_arr = inputs.toArray(new String[0]);
                    System.out.printf("Inputs[%d]: %s\n", inputs_arr.length, String.join(", ", inputs_arr));
                    SubmissionRecord verdict = Judge.judgeInteractively(fm, inputs_arr, null);

                    if (exitCallback != null) {
                        SwingUtilities.invokeLater(() -> exitCallback.onTerminalExit(inputs_arr, verdict.output()));
                    }

                    // keeps the terminal open
                    try {
                        while (true) Thread.sleep(100);
                    } catch (InterruptedException f) {}

                    SwingUtilities.invokeLater(this::dispose);

                    return p.exitValue();
                });

                // Setup the writer for sending commands to the process's input (stdin)
                processWriter = new BufferedWriter(new OutputStreamWriter(terminalProcess.getOutputStream()));

                // Start a thread to continuously read the process's output (stdout and stderr)
                new Thread(new ConsoleOutputReader(terminalProcess.getInputStream())).start();
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

                // SEND TO TERMINAL
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

                    // Loop as long as the process is alive or we can still read from the stream
                    while (terminalProcess.isAlive() || inputStream.available() > 0) {
                        if (reader.ready()) {
                            readChars = reader.read(buffer);
                            if (readChars > 0) {
                                final String output = (new String(buffer, 0, readChars));

//                                System.out.print("Recieved [");
//                                int j = 1;
//                                for (int c : output.getBytes())
//                                {
//                                    System.out.printf("%d, ", c);
//                                    if (j%10==0) System.out.println();
//                                    j++;
//                                }
//                                if (output.getBytes().length > 0) System.out.print("\b\b");
//                                System.out.println("]");

                                SwingUtilities.invokeLater(() -> {
                                    outputArea.append(Helpers.stripAnsi(output));
                                    outputArea.setText(Helpers.stripCRLines(outputArea.getText()));
                                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                                });
                            }
                        }

                        // Wait briefly to prevent the thread from consuming excessive CPU
                        Thread.sleep(50);
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("External process terminated or connection lost (IOException).");
                        System.out.println("Error: " + e.getMessage());
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
            String[] files = {"datafile3.ccpp", "datafile4.ccpp"};

            // TEST VARIABLES
            final String TEST_LANG = "CPP";
            final String TEST_FILE = files[1];
            final int TEST_TYPE = 0;

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


            if (OPEN_TERMINAL)
            {
                FileManager finalFm = fm;
                SwingUtilities.invokeLater(() -> {
                    if (APPEND_TESTCASES) new TerminalApp(finalFm, tf); // adds testcases to tf
                    else new TerminalApp(finalFm, null); // equivalent to run code, doesnt make testcases
                });

                // sleep main thread for 10 seconds to mimic workload on main
                // the testfile will be updated after the terminal is finished
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
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
                tf.writeOut(); // saves testcase
            }


        }
    }