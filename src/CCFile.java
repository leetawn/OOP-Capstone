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
        File original = new File(path);
        File temp = new File(path + ".tmp");
        // Extract name + extension
        String name = original.getName();
        int dotIndex = name.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? name : name.substring(0, dotIndex);
        String ext = (dotIndex == -1) ? "" : name.substring(dotIndex);

        // Create new filename: "filename (overwritten).ext"
        File overwrittenFile = new File(original.getParent(), baseName + " (overwritten)" + ext);
        try (BufferedReader br = new BufferedReader(new FileReader(original));
            BufferedWriter bw = new BufferedWriter(new FileWriter(overwrittenFile))) {
            String s;
            while ((s = br.readLine()) != null) {
                bw.write(s);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
//        if (original.delete()) {
//            temp.renameTo(original);
//        }
    }
}
