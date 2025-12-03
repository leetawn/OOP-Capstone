package FileManagement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SFile {
    // testing
    public static void main(String[] args) {
        SFile s = new SFile("TODO");
        System.out.println(s.getContent());
        s.writeOut();
    }

    private Path path = null;
    private String content = "";

    /****************** LOADS ******************/
    public SFile(String filepath) {
        path = Paths.get(filepath);
        if (Files.exists(path)) {
            try {
                content = Files.readString(path);
            } catch (Exception ignored) {}
        }
    }
    public SFile(Path path) {
        this.path = path;
        if (Files.exists(this.path)) {
            try {
                content = Files.readString(this.path);
            } catch (Exception ignored) {}
        }
    }

    /****************** GETTERS ******************/

    // TODO: @GODSPRO magamit ni nimo nga function para sa Ctrl + S
    public String getContent() { return content; }
    public Path getPath() { return path; }
    public String getStringPath() { return path.toString(); }

    /****************** SETTERS ******************/

    // when editing in JTextArea in GUI use this to set the content to write */
    // TODO: @GODSPRO magamit ni nimo nga function para sa Ctrl + S
    public void setContent(String content) { this.content = content; }

    /****************** INPUT/OUTPUT ******************/

    // save/write the file to specified file
    // if you need a function to load a file just create a new instance of SFile
    public void writeOut() {
        try {
            Files.writeString(path, content);
        } catch (Exception ignored) {}
    }
}
