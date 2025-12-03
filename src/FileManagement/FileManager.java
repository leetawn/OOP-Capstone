package FileManagement;

import CustomExceptions.NotDirException;

import java.io.IOException;
import java.io.UncheckedIOException;
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
    private final Path rootdir;
    private SFile currentFile;
    private ArrayList<SFile> s_files;
    private String language;

    // Opens a folder and make set it as rootdir, throws error when path is not dir
    public FileManager(String rootpath, String language) throws NotDirException {
        rootdir = Paths.get(rootpath);
        currentFile = null;
        this.language = language;

        if (!Files.isDirectory(rootdir)) throw new NotDirException();
        s_files = new ArrayList<>();

        try {
            listAllContents(rootdir, ALL_ALLOWED_EXTENSIONS);
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

    // Check if file is allowed
    public boolean isAllowedFile(String filename) {
        if (filename == null) return false;
        return ALL_ALLOWED_EXTENSIONS.stream().anyMatch(filename::endsWith);
    }

    // file renaming and deleting forgot to fking add these xD
    public boolean renameFile(SFile sfile, String newName) {
        Path oldPath = sfile.getPath();
        Path newPath = oldPath.getParent().resolve(newName);

        if (Files.exists(newPath)) {
            System.err.println("Error: Destination path already exists.");
            return false;
        }

        try {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

            sfile.setPath(newPath);

            if (Files.isDirectory(newPath)) {

                for (SFile currentFile : s_files) {
                    Path currentOldPath = currentFile.getPath();

                    if (currentOldPath.startsWith(oldPath)) {
                        // Calculate the new path
                        Path relativePath = oldPath.relativize(currentOldPath);
                        Path updatedPath = newPath.resolve(relativePath);

                        currentFile.setPath(updatedPath);
                    }
                }
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error renaming path or updating references: " + e.getMessage());
            return false;
        }
    }
    public boolean deleteFile(SFile sfile) {
        if (sfile == null) return false;

        try {
            Path filePath = sfile.getPath();

            if (!Files.exists(filePath)) {
                System.err.println("File not found: " + filePath);
                return false;
            }

            Files.delete(filePath);

            s_files.remove(sfile);

            if (currentFile != null && currentFile.equals(sfile)) {
                currentFile = null;
            }

            System.out.println("[DELETED] " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
            return false;
        }
    }



    /****************** GETTERS ******************/
    public Path getRootdir() { return rootdir; }
    public String getLanguage() { return language; }
    public ArrayList<SFile> getFiles() { return s_files; }
    public SFile getCurrentFile() { return currentFile; }
    public String getCurrentFileStringPath() {
        if (currentFile == null) return null;
        return getRelativePath(currentFile).toString();
    }

    /****************** SETTERS ******************/
    public void setLanguage(String language)
    {
        this.language = language;
    }

    public void setCurrentFile(SFile currentFile) {
        this.currentFile = currentFile;
    }
    public boolean createFolder(Path targetDir, String folderName) {
        if (targetDir == null || folderName == null || folderName.isBlank()) {
            return false;
        }
        Path newDirPath = targetDir.resolve(folderName);

        try {
            if (Files.exists(newDirPath)) {
                System.out.println("Error: Directory already exists at " + newDirPath);
                return false;
            }
            Files.createDirectory(newDirPath);
            System.out.println("Folder created successfully: " + newDirPath);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to create directory: " + newDirPath + ". Error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFolder(Path dirPath) {
        if (dirPath == null || !Files.exists(dirPath)) {
            return true;
        }
        if (!Files.isDirectory(dirPath)) {
            return deleteFile(new SFile(dirPath));
        }

        try {
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            if (!Files.isDirectory(path)) {
                                s_files.removeIf(sfile -> sfile.getPath().equals(path));
                            }
                        } catch (IOException e) {
                            System.err.println("Failed to delete path: " + path + ". Error: " + e.getMessage());
                            throw new UncheckedIOException(e);
                        }
                    });

            return !Files.exists(dirPath);

        } catch (IOException | UncheckedIOException e) {
            System.err.println("Error during recursive folder deletion of " + dirPath);
            return false;
        }
    }

    /****************** INPUT/OUTPUT ******************/
    public Path getRelativePath(SFile sfile) {
        return rootdir.relativize(sfile.getPath());
    }
    public void saveAll() { for (SFile sfile : s_files) sfile.writeOut(); }
    public void delete(SFile sfile) {
        if (s_files.remove(sfile)) {
            sfile.delete();
        };
    }

    private void listAllContents(Path rootDir, Set<String> allowed_extensions) throws IOException {
        Path absoluteRootDir = rootDir.toAbsolutePath().normalize();

        System.out.println("--- Listing all files inside: " + absoluteRootDir + " ---");

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

                System.out.println("[DIR]  " + dir);
                return FileVisitResult.CONTINUE;
            }

            // FILE FILTER
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                s_files.add(new SFile(file));
                System.out.println("[FILE] " + file);

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
