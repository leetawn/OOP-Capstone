import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String compilerVersion = System.getProperty("java.specification.version");
        System.out.println("Compiler: " + compilerVersion);


        Process p = null;
        try {
            p = new ProcessBuilder("java", "-version")
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException e) {
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String runtimeLine = null; // first line
        try {
            runtimeLine = br.readLine();
        } catch (IOException e) {}

        String runtimeVersion = extractVersion(runtimeLine);
        System.out.println("Runtime: " + runtimeVersion);

        int comp = Integer.parseInt(compilerVersion);
        int run = Integer.parseInt(runtimeVersion);

        if (run < comp) {
            System.out.println("Java runtime is too old! Runtime=" + run + " Compiler=" + comp);
        }
    }
    private static String extractVersion(String line) {
        // matches: "java version "17.0.5"" OR "openjdk version "1.8.0_392""
        Matcher m = Pattern.compile("\"([^\"]+)\"").matcher(line);
        if (!m.find()) return null;
        String full = m.group(1);

        // Extract major version (17, 21, 8, etc)
        if (full.startsWith("1.")) {
            return full.substring(2, 3); // 1.8 → 8
        }
        int dot = full.indexOf('.');
        return dot > 0 ? full.substring(0, dot) : full;
    }

}
