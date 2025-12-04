package com.exception.ccpp.CCTerminal;

import com.exception.ccpp.CCJudge.ExecutionConfig;
import com.exception.ccpp.Common.Helpers;
import com.exception.ccpp.CustomExceptions.NotDirException;
import com.exception.ccpp.FileManagement.FileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Arrays;

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
            TERMINAL_START_COMMAND = new String[]{"cmd.exe", "/k"};;
        }
    }

    public TerminalApp(FileManager fm) {
        super("Java Swing Command Console");

        // Setup GUI Components
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        inputField = new JTextField();
        inputField.addActionListener(new CommandListener());

        // Layout Setup
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        // Final Frame Setup
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // Center the window
        setVisible(true);

        // Start the external process
        startTerminalProcess(fm);
    }

    private void startTerminalProcess(FileManager fm) {
        try {
            // Use ProcessBuilder for better control over the process environment
            String[] cmd = Helpers.concatStringArrays(TERMINAL_START_COMMAND, new String[]{ExecutionConfig.getRunCodeCommand(fm)});
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.directory(fm.getRootdir().toFile());

            // Redirect stderr to stdout so we only need one thread for reading output.
            builder.redirectErrorStream(true);

            terminalProcess = builder.start();

            // Setup the writer for sending commands to the process's input (stdin)
            processWriter = new BufferedWriter(new OutputStreamWriter(terminalProcess.getOutputStream()));

            // Start a thread to continuously read the process's output (stdout and stderr)
            new Thread(new ConsoleOutputReader(terminalProcess.getInputStream())).start();

            // Print the initial message
            outputArea.append("--- Starting External Process: " + Arrays.toString(cmd) + " ---\n");
            outputArea.append("Type commands below and press Enter.\n\n");

        } catch (IOException e) {
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

            String commandString = TERMINAL_START_COMMAND[0].toLowerCase();
            boolean isWindowsShell = commandString.contains("cmd") || commandString.contains("powershell");

            // Display the command entered by the user in the output area with a recognizable prompt
            outputArea.append(isWindowsShell ? command + "\n" : "\n$ " + command + "\n");

            try {
                // IMPORTANT FIX: Write the command using \r\n (Carriage Return + Newline)
                // This is generally more reliable for interactive shell input over pipes.
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
            // Use InputStreamReader and a character buffer for real-time output reading.
            // This is better for catching interactive prompts that might not end in a newline.
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {

                char[] buffer = new char[1024];
                int readChars;

                // Loop as long as the process is alive or we can still read from the stream
                while (terminalProcess.isAlive() || inputStream.available() > 0) {
                    if (reader.ready()) {
                        readChars = reader.read(buffer);
                        if (readChars > 0) {
                            final String output = new String(buffer, 0, readChars);

                            // Update the JTextArea on the Event Dispatch Thread (EDT)
                            SwingUtilities.invokeLater(() -> {
                                // IMPORTANT: Append the raw output directly (including any partial lines or prompts)
                                outputArea.append(output);
                                // Auto-scroll to the bottom
                                outputArea.setCaretPosition(outputArea.getDocument().getLength());
                            });
                        }
                    }
                    // Wait briefly to prevent the thread from consuming excessive CPU
                    Thread.sleep(50);
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("\n--- External process terminated or connection lost (IOException). ---\n");
                    outputArea.append("Error: " + e.getMessage() + "\n");
                });
            } catch (InterruptedException e) {
                // The thread was interrupted while sleeping
                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> outputArea.append("\n--- Console output reader interrupted. ---\n"));
            } finally {
                // Ensure resources are cleaned up
                if (terminalProcess != null) {
                    terminalProcess.destroy();
                }
            }
        }
    }

    public static void main(String[] args) {
        // Start the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                FileManager fm = FileManager.getInstance().setAll("COMPILER_TEST/JAVA","java");
                fm.setCurrentFile(fm.getFiles().getLast());
                new TerminalApp(fm);
            } catch (NotDirException e) {
                System.err.println("ERR: fm.setAll()");
            }
        });
    }
}