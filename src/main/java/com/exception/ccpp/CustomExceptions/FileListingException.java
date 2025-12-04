package CustomExceptions;

public class FileListingException extends Exception {
    public FileListingException(String message, Throwable cause) {
        super(message, cause);
    }
}
