package server;

import java.nio.file.*;

public class FileWatcher implements Runnable {

    private final Path folder;

    public FileWatcher(String dir) {
    	this.folder = Paths.get(System.getProperty("user.dir"), "server_files");
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();

            folder.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE
            );

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {

                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();

                    String file = fileName.toString();

                    if (!file.endsWith(".txt")) continue;

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("File added: " + file);
                        ServerMain.broadcast("INFO: FILE_ADDED " + file, null);
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("File deleted: " + file);
                        ServerMain.broadcast("INFO: FILE_REMOVED " + file, null);
                    }
                }

                key.reset();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
