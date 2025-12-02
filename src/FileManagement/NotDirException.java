package FileManagement;

public class NotDirException extends Exception {
    public NotDirException(String message) {
        super(message);
    }
    public NotDirException() {
        super("This not a directory, Stupid!");
    }
}
