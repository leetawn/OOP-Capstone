package Debug;

// temporarily just prints to sout
// soon it will write to a single log file
public class DebugLog {
    public static final boolean DEBUG_ENABLED = true;
    private static DebugLog instance;
    private DebugLog() {}

    public static DebugLog getInstance() {
        if (instance == null) {
            instance = new DebugLog();
        }
        return instance;
    }

    // INSTANCE
    public void logln(String msg) {
        if (DEBUG_ENABLED) {
            System.out.println(msg);
        }
    }
    public void logf(String format, Object ... args) {
        if (DEBUG_ENABLED) {
            System.out.printf(format, args);
        }
    }
}