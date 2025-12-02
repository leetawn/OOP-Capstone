package FileManagement;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FileManager {
    private static final Set<String> IGNORED_FOLDERS = Set.of(".git", ".svn", ".vscode", "target", "out", "bin");
    private static final Set<String> ALL_ALLOWED_EXTENSIONS = Set.of(".c", ".cpp", ".h", ".hpp",".java",".py");
    private static final Set<String> JAVA_ALLOWED_EXTENSIONS = Set.of(".java");
    private static final Set<String> CPP_ALLOWED_EXTENSIONS  = Set.of(".cpp", ".h", ".hpp");
    private static final Set<String> C_ALLOWED_EXTENSIONS    = Set.of(".c", ".h");
    private static final Set<String> PY_ALLOWED_EXTENSIONS   = Set.of(".py");
    private Path rootdir;
    private ArrayList<SFile> s_files;
    private String language;

    // Opens a folder and make set it as rootdir, throws error when path is not dir
    public FileManager(String rootpath, String language) throws NotDirException {
        rootdir = Paths.get(rootpath);
        this.language = language;
        if (!Files.isDirectory(rootdir)) throw new NotDirException();

        s_files = new ArrayList<>();

        try {
            listAllContents(rootdir, getAllowedExtensions(language));
        } catch (IOException e) {}
    }

    private Set<String> getAllowedExtensions(String lang) {
        if (lang == null) return ALL_ALLOWED_EXTENSIONS;

        return switch (lang.toLowerCase()) {
            case "java" -> JAVA_ALLOWED_EXTENSIONS;
            case "cpp" -> CPP_ALLOWED_EXTENSIONS;
            case "c" -> C_ALLOWED_EXTENSIONS;
            case "python", "py" -> PY_ALLOWED_EXTENSIONS;
            default -> ALL_ALLOWED_EXTENSIONS;
        };
    }

    /****************** GETTERS ******************/
    public Path getRootdir() { return rootdir; }
    public String getLanguage() { return language; }

    // @ TODO ETHAN, USE THIS FOR FILE TREE
    public ArrayList<SFile> getFiles() { return s_files; }


    /****************** SETTERS ******************/
    public void setLanguage(String language)
    {
        this.language = language;
        s_files.clear();
        try {
            listAllContents(rootdir, getAllowedExtensions(language));
        } catch (IOException e) {}
    }

    /****************** INPUT/OUTPUT ******************/
    public void saveAll() { for (SFile sfile : s_files) sfile.writeOut(); }
    public Path getRelativePath(SFile sfile) {
        return rootdir.relativize(sfile.getPath());
    }

    // @ TODO: REMOVE sout WHEN DEBUGGING IS DONE
    private void listAllContents(Path rootDir, Set<String> allowed_extensions) throws IOException {
        Path absoluteRootDir = rootDir.toAbsolutePath().normalize();

         System.out.println("--- Listing only: " + allowed_extensions + " inside: " + absoluteRootDir + " ---");

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
                boolean isAllowed = allowed_extensions.stream()
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
            FileManager fm = new FileManager(".", "java");
            SFile s = fm.getFiles().get(0);
            Path p = fm.getRelativePath(s);
            System.out.println("RELATIVE PATH: " + p);
        } catch (Exception ignored) {}
    }

}
