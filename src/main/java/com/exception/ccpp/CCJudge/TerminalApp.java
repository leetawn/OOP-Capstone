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
                TERMINAL_START_COMMAND = new String[]{"powershell.exe"}; // "-NoExit"
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
            inputField.addActionListener(new CommandListener());

            setLayout(new BorderLayout());
            add(scrollPane, BorderLayout.CENTER);
            add(inputField, BorderLayout.SOUTH);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                String[] cmd = Helpers.concatStringArrays(TERMINAL_START_COMMAND,execCmd);

                Map<String, String> env = new HashMap<>(System.getenv());
                if (!env.containsKey("TERM")) env.put("TERM", "xterm");

                terminalProcess = new PtyProcessBuilder()
                        .setCommand(cmd)
                        .setEnvironment(env)
                        .setRedirectErrorStream(true)
                        .setDirectory(fm.getRootdir().toString())
                        .setConsole(true)
                        .start();

                // TERMINAL WILL EXIT HERE
                terminalProcess.onExit().thenApply( p -> {
                    System.out.println("Process exited with: " + p.exitValue());
                    String[] inputs_arr = inputs.toArray(new String[0]);
                    SubmissionRecord verdict = Judge.judgeInteractively(fm, inputs_arr, null);
                    outputArea.append("-------- MAIN OUTPUT --------\n");
                    outputArea.append(verdict.output());
                    outputArea.append("\n-------- MAIN END --------\n");
                    Judge.cleanup(fm);

                    if (exitCallback != null) {
                        // Ensure the callback runs on the EDT if it manipulates Swing components
                        SwingUtilities.invokeLater(() -> exitCallback.onTerminalExit(inputs_arr, verdict.output()));
                    }

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

        /**
         * Listener for the input field. Sends the command to the external process.
         */
        private class CommandListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = inputField.getText();
                inputField.setText(""); // Clear the input field

                System.out.printf("Entered: [%s]\n",command);
                inputs.add(command);
                outputArea.append(command);

                // SEND TO TERMINAL
                try {
                    processWriter.write(command + "\n");
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

        /**
         * Runnable class to continuously read the output (stdout/stderr) of the external process
         * and append it to the Swing JTextArea.
         */
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
                                final String output = new String(buffer, 0, readChars);
                                SwingUtilities.invokeLater(() -> {
                                    outputArea.append(output);
                                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                                });
                            }
                        }
                        // Wait briefly to prevent the thread from consuming excessive CPU
                        Thread.sleep(50);
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("\n--- External process terminated or connection lost (IOException). ---\n");
                        System.out.println("Error: " + e.getMessage() + "\n");
                    });
                } catch (InterruptedException e) {
                    // The thread was interrupted while sleeping
                    Thread.currentThread().interrupt();
                    SwingUtilities.invokeLater(() -> System.out.println("\n--- Console output reader interrupted. ---\n"));
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
            // Start the GUI on the Event Dispatch Thread (EDT)
            String directoryPath = "W:\\sysdev\\OOP-Capstone\\COMPILER_TEST\\C";
            TestcaseFile tf = new TestcaseFile("datafile.ccpp");
            FileManager fm = null;
            try {
                fm = FileManager.getInstance().setAll(directoryPath, "c");
                fm.setCurrentFile(fm.getFiles().getLast());
            } catch (NotDirException e) {
                System.err.println("ERR: fm.setAll()");
            }


            FileManager finalFm = fm;
            SwingUtilities.invokeLater(() -> {
                    new TerminalApp(finalFm, tf); // adds testcases to tf
                    // new TerminalApp(finalFm, null); // equivalent to run code, doesnt make testcases

            });

            // sleep main thread for 10 seconds to mimic workload on main
            // the testfile will be updated after the terminal is finished
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }

            // this is how you call judge
            SubmissionRecord[] sr_arr= Judge.judge(fm,tf);

            // print test cases
            int i = 0;
            for (SubmissionRecord sr : sr_arr) {
                System.out.printf("[[[------------- ACTUAL OUT Testcase %d---------------\n", ++i);
                System.out.println(sr.output()); // actual output
                System.out.printf("-------------- EXPECTED OUT Testcase %d---------------\n", ++i);
                System.out.println(sr.expected_output()); // expected output
                System.out.printf("------------- END Testcase %d---------------]]]\n\n", i);
            }
            tf.writeOut(); // saves testcase


        }
    }