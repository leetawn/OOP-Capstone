import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.lang.*;
public class FileManager {
    private String rootPath;
    private List<String> filePaths;
    public String[] valid_extensions;

    public FileManager(String rootPath) {
        this.rootPath = rootPath;
        this.filePaths = new ArrayList<>();
        valid_extensions = new String[]{".java", ".cpp", ".c", ".py"};
    }

    String getRootPath() {
        return this.rootPath;
    }


    CCFile create(String filename)  {
        CCFile out = null;
        String[] filename_split = filename.split("\\.");
        if (filename_split.length < 2 || !Arrays.asList(valid_extensions).contains("." + filename_split[1])) {
            System.out.println("This file extension is not supported! Please try another one.");
            return null;
        }
        try {
            out = new CCFile(this.rootPath, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (out != null) {
            System.out.println("File successfully created!");
        } else {
            System.out.println("File creation failed!");
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

    void printFilePaths() {
        for (String f : filePaths) {
            System.out.println(f);
        }
    }

    public static void main(String[] args) {
        FileManager fm = new FileManager("src/");
        // fm.load("src/");
//        fm.printFilePaths();
        CCFile tc1 = fm.create("a.mp3");
        //tc1.overwrite();
    }
}
