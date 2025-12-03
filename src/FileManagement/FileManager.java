package FileManagement;

import CustomExceptions.NotDirException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FileManager {
    public static final Set<String> IGNORED_FOLDERS = Set.of(".git", ".svn", ".vscode", "target", "out", "bin");
    public static final Set<String> ALL_ALLOWED_EXTENSIONS = Set.of(".c", ".cpp", ".h", ".hpp",".java",".py");
    public static final Set<String> JAVA_ALLOWED_EXTENSIONS = Set.of(".java");
    public static final Set<String> CPP_ALLOWED_EXTENSIONS  = Set.of(".cpp", ".h", ".hpp");
    public static final Set<String> C_ALLOWED_EXTENSIONS    = Set.of(".c", ".h");
    public static final Set<String> PY_ALLOWED_EXTENSIONS   = Set.of(".py");
    private static FileManager instance;

    private Path rootdir;
    private SFile currentFile;
    private ArrayList<SFile> all_files;
    private ArrayList<SFile> s_files;
    private String language;
    private FileWatcher fileWatcher;
    private Thread watcherThread;

    // Opens a folder and make set it as rootdir, throws error when path is not dir
    private FileManager() {
        rootdir = null;
        currentFile = null;
        this.language = null;
        s_files = new ArrayList<>();
        all_files = new ArrayList<>();
    }



    // FIXME: WARNING I WILL BE REMOVING THIS (deleteFile, renameFile), Please use addFile, removeFile, removeDir
    // NOTE: for RENAMING files just use Files.move(), the fileWatcher will do its job
    // file renaming and deleting forgot to fking add these xD
    public boolean renameFile(SFile sfile, String newName) {
        try {
            Path oldPath = sfile.getPath();
            Path newPath = oldPath.resolveSibling(newName);

            if (Files.exists(newPath)) {
                System.err.println("File already exists: " + newPath);
                return false;
            }

            Files.move(oldPath, newPath);
            if (currentFile != null && currentFile.equals(sfile)) {
                currentFile = all_files.getLast();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rename file: " + e.getMessage());
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
    public boolean isAllowedFile(String filename) {
        if (filename == null) return false;
        return ALL_ALLOWED_EXTENSIONS.stream().anyMatch(filename::endsWith);
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

    /****************** File Manager ******************/
    public void addFile(Path file) {
        all_files.add(new SFile(file));
    }
    public void removeFile(Path file) {
        for (SFile sfile : all_files) {
            if (sfile.getPath().equals(file)) {
                all_files.remove(sfile);
                return;
            }
        }
    }
    public void removeDir(Path dir) {
        all_files.removeIf(p -> p.getPath().startsWith(dir));
    }

    /****************** INITIALIZERS ******************/
    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }
    public FileManager setLanguage(String language) {
        this.language = language;
        return this;
    }
    public FileManager setRootdir(Path rootPath) throws NotDirException {
        if (!Files.isDirectory(rootPath)) throw new NotDirException();
        this.rootdir = rootPath;
        return this;
    }
    public FileManager setRootdir(String rootDir) throws NotDirException {
        return setRootdir(this.rootdir = Paths.get(rootDir));
    }
    public FileManager update() {
        if (rootdir == null) return this;
        all_files.clear();
        s_files.clear();
        try {
            listAllContents(rootdir, getAllowedExtensions(language));
            openNewFolder();
        } catch (Exception e) {
            System.err.println("Failed to open new folder: " + e.getMessage());
        }
        return this;
    }
    public FileManager setAll(Path rootPath, String language) throws NotDirException {
        setLanguage(language);
        setRootdir(rootPath);
        return update();
    }
    public FileManager setAll(String rootDir, String language) throws NotDirException {
        setLanguage(language);
        setRootdir(rootDir);
        return update();
    }


    /****************** GETTERS ******************/
    public Path getRootdir() { return rootdir; }
    public String getLanguage() { return language; }
    public ArrayList<SFile> getFiles() { return all_files; }
    public ArrayList<SFile> getLanguageFiles() { return s_files; }
    public SFile getCurrentFile() { return currentFile; }
    public String getCurrentFileStringPath() {
        if (currentFile == null) return null;
        return getRelativePath(currentFile).toString();
    }

    /****************** HELPERS ******************/
    public void setCurrentFile(SFile currentFile) { this.currentFile = currentFile; }
    public Path getRelativePath(SFile sfile) { return rootdir.relativize(sfile.getPath()); }
    private Set<String> getAllowedExtensions(String lang) {
        if (lang == null) return ALL_ALLOWED_EXTENSIONS;

        return switch (lang.toLowerCase()) {
            case "java" -> JAVA_ALLOWED_EXTENSIONS;
            case "cpp", "c++" -> CPP_ALLOWED_EXTENSIONS;
            case "c" -> C_ALLOWED_EXTENSIONS;
            case "python", "py" -> PY_ALLOWED_EXTENSIONS;
            default -> ALL_ALLOWED_EXTENSIONS;
        };
    }

    /****************** INPUT/OUTPUT ******************/
    // @ TODO: REMOVE sout WHEN DEBUGGING IS DONE
    private void listAllContents(Path rootDir, Set<String> allowed_extensions) throws IOException {
        Path absoluteRootDir = rootDir.toAbsolutePath().normalize();

         System.out.println("--- Listing files: " + ALL_ALLOWED_EXTENSIONS + " inside: " + absoluteRootDir + " ---");

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
                boolean isAllowed = ALL_ALLOWED_EXTENSIONS.stream()
                        .anyMatch(fileName::endsWith);

                if (isAllowed) {
                    SFile sfile = new SFile(file);
                    all_files.add(sfile);
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
    public void saveAll() { for (SFile sfile : s_files) sfile.writeOut(); }

    /****************** File Watcher ******************/
    public synchronized void openNewFolder() throws Exception {
        if (fileWatcher != null) {
            System.out.println("Stopping old watcher...");

            fileWatcher.closeAndCleanup();

            if (watcherThread != null && watcherThread.isAlive()) {
                watcherThread.interrupt();
                try {
                    // Wait briefly for the thread to exit gracefully
                    watcherThread.join(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Instantiate a brand new FileWatcher object
        this.fileWatcher = new FileWatcher(this);

        // Start the new thread
        this.watcherThread = new Thread(this.fileWatcher);
        this.watcherThread.setDaemon(true);
        this.watcherThread.start();
    }
    // 3. --- START THE NEW WATCHER ---

    public static void main(String[] args) {

        try {
            FileManager fm = FileManager.getInstance().setAll(".", "java").update();
            SFile s = fm.getFiles().get(0);
            Path p = fm.getRelativePath(s);
            System.out.println("RELATIVE PATH: " + p);
        } catch (Exception ignored) {}
    }

}
