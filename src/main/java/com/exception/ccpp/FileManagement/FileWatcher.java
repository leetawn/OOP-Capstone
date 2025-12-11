package com.exception.ccpp.FileManagement;
import com.exception.ccpp.CustomExceptions.NotDirException;
import com.exception.ccpp.GUI.FileExplorer;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;

public class FileWatcher implements Runnable {

    private final FileManager fmInstance;
    private final FileExplorer feInstance;
    private final List<Path> filePaths;
    private final Path rootDir;
    private final WatchService watcher;
    private final ConcurrentHashMap<WatchKey, Path> keys;

    public FileWatcher(FileManager fm, FileExplorer fe) throws IOException {
        this.fmInstance = fm;
        this.rootDir = fmInstance.getRootdir().toAbsolutePath().normalize();
        this.filePaths = Collections.synchronizedList(fmInstance.getFiles().stream().map(SFile::getPath).collect(Collectors.toList()));
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new ConcurrentHashMap<>();
        this.feInstance = fe;
        registerAll(this.rootDir);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (shouldIgnore(dir)) {
                    System.out.println("Skipping registration: " + dir.getFileName());
                    return FileVisitResult.SKIP_SUBTREE;
                }

                WatchKey key = dir.register(watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE);
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean shouldIgnore(Path dir) {
        String name = dir.getFileName() != null ? dir.getFileName().toString() : "";
        return FileManager.IGNORED_FOLDERS.contains(name.toLowerCase()) || name.startsWith(".");
    }

    // MAGIC CODE
    @Override
    public void run() {
        System.out.println("File Watcher started for: " + rootDir);

        while (Thread.currentThread().isAlive()) {
            WatchKey key;
            try {
                key = watcher.take(); // Blocks until an event occurs
            } catch (InterruptedException | ClosedWatchServiceException x) {
                return; // Exit thread if interrupted or service is closed
            }

            Path dir = keys.get(key);
            if (dir == null) continue; // Key no longer valid

            for (WatchEvent<?> event : key.pollEvents()) {
                Path relativePath = (Path) event.context();
                Path fullPath = dir.resolve(relativePath); // Reconstruct the full path

                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    handleCreate(fullPath);
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    handleDelete(fullPath);
                }
            }

            // CRITICAL: Reset the key for future events. If it's invalid, the directory was deleted.
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key); // Remove it from our tracking map
                if (keys.isEmpty()) break; // No more directories to watch
            }
        }
        System.out.println("File Watcher stopped.");
    }

    /*********** EVENT CALLBACK OR WHATEVER *************/
    private void handleCreate(Path path) {
        if (Files.isRegularFile(path)) {
            String fileName = path.getFileName().toString().toLowerCase();
            boolean isAllowed = FileManager.ALL_ALLOWED_EXTENSIONS.stream()
                    .anyMatch(fileName::endsWith);

            if (isAllowed) {
                filePaths.add(path);
                FileManager.addFile(fmInstance,path);
                // TODO@Ethan feInstance.addFile, NOTE feInstance is FileExplorer
                System.out.println("FileWatcher.handleCreate: File : " + path.getFileName());
            }
        } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            if (!shouldIgnore(path)) {
                try {
                    registerAll(path);
                    // TODO@Ethan feInstance.addFolder, NOTE feInstance is FileExplorer
                    feInstance.addFileNodeToTree(path);
                    System.out.println("FileWatcher.handleCreate: Dir: " + path.getFileName());
                } catch (IOException e) {
                    System.err.println("FileWatcher.handleCreate:: Dir Err " + path);
                }
            }
        }
    }
    private void handleDelete(Path deletedPath) {
        if (Files.isDirectory(deletedPath, LinkOption.NOFOLLOW_LINKS)) {
            synchronized (filePaths) {
                filePaths.removeIf(p -> p.startsWith(deletedPath));
                fmInstance.removeDir(fmInstance,deletedPath);
            }
            // TODO@ETHAN feInstance.deleteFolder (RecursiveDelete), NOTE feInstance is FileExplorer
            feInstance.removeFileNodeFromTree(deletedPath);
            keys.entrySet().removeIf(entry -> entry.getValue().startsWith(deletedPath));
            System.out.println("FileWatcher.handleDelete: DirRM: " + deletedPath.getFileName());

        } else {
            // TODO@ETHAN feInstance.deleteFile, NOTE feInstance is FileExplorer
            fmInstance.removeFile(fmInstance,deletedPath);
            filePaths.remove(deletedPath);
            feInstance.removeFileNodeFromTree(deletedPath);
            System.out.println("FileWatcher.handleDelete: FileRM: " + deletedPath.getFileName());
        }
    }

    public void closeAndCleanup() throws IOException {
        watcher.close();
    }

    // DEMO
    public static void main(String[] args) throws IOException {
        Path root = Paths.get("./COMPILER_TEST"); // set root dir

        try {
            FileManager.getInstance().setRootdir(root).update();
            System.out.println("com.exception.ccpp.Main application running... Create/Delete files in " + root.toAbsolutePath().normalize());
        } catch (NotDirException e) {

        }
        slaveWorkers.shutdown();
        while (true) {} // FOR TESTING, TRY RENAMING, ADDING, AND DELETING FILES IN COMPILER_TEST FOLDER
    }
}