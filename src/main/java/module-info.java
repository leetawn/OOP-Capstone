module com.exception.ccpp {
    requires java.base;
    requires java.datatransfer;
    requires java.desktop;
    requires pty4j;
    requires jdk.compiler;
    requires org.fife.RSyntaxTextArea;
    requires kotlin.stdlib;
    requires com.formdev.flatlaf;
    exports com.exception.ccpp;
    exports com.exception.ccpp.FileManagement;
}