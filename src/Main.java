import java.io.BufferedReader;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) {
        String out_file = "file.txt";
        String file_test = "Test";
        CmdRunner.runCommand( String.format("cd src && javac %s.java && java %s > %s", file_test, file_test, out_file));

        try (
            BufferedReader br = new BufferedReader(new FileReader(out_file));
        )
        {
            String s;
            while ((s = br.readLine()) != null)
            {
                System.out.println(s);
            }
        } catch (Exception e) {}
    }
}
