package FileManagement;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FileManager {
    private static final Set<String> IGNORED_FOLDERS = Set.of(".git", ".svn", ".vscode", "target", "out", "bin");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".c", ".cpp", ".h", ".hpp", ".java", ".py");
    private Path rootdir;
    private ArrayList<SFile> s_files;

    // Opens a folder and make set it as rootdir, throws error when path is not dir
    public FileManager(String rootpath) throws NotDirException {
        rootdir = Paths.get(rootpath);
        if (!Files.isDirectory(rootdir)) throw new NotDirException();

        s_files = new ArrayList<>();

        try {
            listAllContents(rootdir);
        } catch (IOException e) {}
    }

    /****************** GETTERS ******************/
    public Path getRootdir() {
        return rootdir;
    }

    // @ TODO ETHAN, USE THIS FOR FILE TREE
    public ArrayList<SFile> getFiles() {
        return s_files;
    }

    // @ TODO: REMOVE sout WHEN DEBUGGING IS DONE
    private void listAllContents(Path rootDir) throws IOException {
        Path absoluteRootDir = rootDir.toAbsolutePath().normalize();

         System.out.println("--- Listing only: " + ALLOWED_EXTENSIONS + " inside: " + absoluteRootDir + " ---");

        Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {

            // DIR FILTER
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                // ROOT DIR KEEP ON GETTING SKIPPED SO THIS CODE EXIST
                if (dir.toAbsolutePath().normalize().equals(absoluteRootDir)) {
                    // System.out.println("[ROOT DIR] " + dir);
                    return FileVisitResult.CONTINUE;
                }

                String name = dir.getFileName() != null ? dir.getFileName().toString() : "";

                // SKIP HIDDEN FOLDERS
                if (IGNORED_FOLDERS.contains(name.toLowerCase()) || name.startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                // System.out.println("[DIR]  " + dir);
                return FileVisitResult.CONTINUE;
            }

            // FILE FILTER
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                String fileName = file.getFileName().toString().toLowerCase();

                // NO JEWS ALLOWED
                boolean isAllowed = ALLOWED_EXTENSIONS.stream()
                        .anyMatch(fileName::endsWith);

                if (isAllowed) {
                    s_files.add(new SFile(file));
                     System.out.println("[FILE] " + file);
                }

                return FileVisitResult.CONTINUE;
            }

            // MAGIC CODE THAT PREVENTS CRASH WHEN ACCESS DENIED
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.println("Failed to access: " + file + " because " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void main(String[] args) {
        try {
            new FileManager(".");
        } catch (NotDirException ignored) {}
    }

}
