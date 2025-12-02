package CustomExceptions;

public class NotDirException extends Exception {
    public NotDirException() {
        super("The specified path is not a directory.");
    }

    public NotDirException(String message) {
        super(message);
    }
}
