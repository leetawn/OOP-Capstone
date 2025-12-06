module com.exception.ccpp {
    requires java.base;
    requires java.compiler;
    requires java.datatransfer;
    requires java.desktop;
    requires pty4j;
    requires jdk.compiler;
    requires jdk.incubator.vector;
    requires java.sql;
    exports com.exception.ccpp;
    exports com.exception.ccpp.FileManagement;
}