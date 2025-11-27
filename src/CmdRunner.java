import java.io.*;

public class CmdRunner {
    public static void runCommand(String cmd) {
        try {
            Process process = new ProcessBuilder("cmd.exe", "/c", cmd)
                    .redirectErrorStream(true)
                    .start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
file: a.ethan
{
    "input":{
        1: [3, [1,2,3], "ethan"],
    },
    "output":{
        1: "Enter n: 3\nEnter el: 1\nEnter el: 2\nEnter el: 3\nEnter name: ethan",
    }
}
*/