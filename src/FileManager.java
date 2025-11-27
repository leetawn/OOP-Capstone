import java.io.*;
import java.util.*;
import java.lang.*;
public class FileManager {
    private String rootPath;
    private List<String> filePaths;
    private String[] valid_extensions;

    public FileManager(String rootPath) {
        this.rootPath = rootPath;
        this.filePaths = new ArrayList<>();
        valid_extensions = new String[]{".java", ".cpp", ".c", ".py"};
    }

    String getRootPath() {
        return this.rootPath;
    }
    CCFile create(String filename) {
        CCFile out;
        String act_path = getRootPath() + "/" + filename;
        System.out.println("Actual: " + act_path);
        out = new CCFile(act_path);
        if (out != null) {
            System.out.println("File successfully created!");
        } else {
            System.out.println("File already exists!");
        }
        return out;
    }


    void load(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                // do something later
                System.out.println(file.getName());
            }
        }
    }

    String getContent(String path) {
        StringBuilder res = new StringBuilder("");
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String s;
            while ((s = br.readLine()) != null) {
                res.append(s);
                res.append('\n');
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        return res.toString();
    }

    void overwriteTemp(String something) {

    }

    void printFilePaths() {
        for (String f : filePaths) {
            System.out.println(f);
        }
    }

    public static void main(String[] args) {
        FileManager fm = new FileManager("Z:/L12Y08W19");

//        fm.load("C:\\Users\\L12Y08W19\\Desktop\\CSIT227-LA6");
//        fm.printFilePaths();
        CCFile diddy = fm.create("diddy.txt");
        diddy.overwrite();
    }
}
