import java.lang.*;
import java.io.*;
import java.util.*;

/*
    Some kind of File Wrapper
    Initially creates a temporary file for users to edit.
    Temporary files are deleted on save.
    On save, a final version of the file is made.
 */

public class CCFile {
    private String path;
    private String filename;
    private File tempFile;
    private File finalFile;


    public CCFile(String path, String filename) throws IOException {
        this.path = path;
        this.filename = filename;
        tempFile = new File(path, filename);

        if (tempFile.exists()) {
            System.out.println("Existing file loaded: " + tempFile.getAbsolutePath());
        } else if (tempFile.createNewFile()) {
            System.out.println("Temporary file created: " + tempFile.getAbsolutePath());
        } else {
            throw new IOException("Failed to create file for unknown reason!");
        }
    }

    void overwrite() {
        String[] filename_split = this.filename.split("\\.");
        System.out.println(path);
        finalFile = new File(path + "/" + filename_split[0] + "-f." + filename_split[1]);
        try (BufferedReader br = new BufferedReader(new FileReader(tempFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(finalFile))) {
            String s;
            while ((s = br.readLine()) != null) {
                bw.write(s);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        if (tempFile.delete()) System.out.println("Temporary file has been deleted.");
        System.out.println("Successfully overwritten!");
    }
}
