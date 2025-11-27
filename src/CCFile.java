import java.lang.*;
import java.io.*;
import java.util.*;
public class CCFile {
    private String path;
    private File tempFile;
    private String finalFilename;


    public CCFile(String path) {
        this.path = path;
        tempFile = new File(path);
        finalFilename = tempFile.getName();
        //System.out.println("APAPAPAPPA: " + path);
        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    void deleteFile(String path) {
        File delFile = new File(path);
        if (delFile.delete()) {
            System.out.println("Temporary file deleted!");
        } else {
            System.out.println("Error in deleting temporary file.");
        }
    }

    void overwrite() {
        System.out.println("Path: " + tempFile.getAbsolutePath());
        try (BufferedReader br = new BufferedReader(new FileReader(path + ".tmp"));
            BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            String s;
            while ((s = br.readLine()) != null) {
                bw.write(s);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        deleteFile(tempFile.getPath() + ".tmp");
    }
}
