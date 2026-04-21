package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String editingFile = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void send(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String line = in.readLine();
            if (line == null || !line.startsWith("CONNECT")) {
                send("ERROR: expected CONNECT <username>");
                return;
            }

            username = line.split(" ", 2)[1];
            System.out.println("Connected: " + username);

            send("SUCCESS: connected");
            sendFileList();

            while ((line = in.readLine()) != null) {

                String[] parts = line.split(" ", 2);
                String cmd = parts[0];

                if (!cmd.equals("LIST"") && parts.length < 2) {
                    send("ERROR: invalid command");
                    continue;
                }

                switch (cmd) {

                    case "LIST":
                        sendFileList();
                        break;

                    case "VIEW":
                        sendFile(parts[1]);
                        break;

                    case "EDIT":
                        handleEdit(parts[1]);
                        break;

                    case "SAVE":
                        handleSave(parts[1]);
                        break;

                    case "CANCEL":
                        handleRenunta(parts[1]);
                        break;

                    default:
                        send("ERROR: unknown command");
                }
            }

        } catch (Exception e) {
            System.out.println("Deconnected: " + username);
        } 
        
        finally {

            String unlockedFile = editingFile;

            FileManager.unlockAllForUser(username);
            ServerMain.clients.remove(this);

            ServerMain.broadcast("INFO: " + username + " deconnected", this);

            if (unlockedFile != null) {
                ServerMain.broadcast("INFO: " + unlockedFile + " has been unlocked", this);
            }
        }
    }

    // ================= FILE LIST =================

    private void sendFileList() {
        send("LIST");

        for (String f : FileManager.getFiles()) {
            if (FileManager.isLocked(f))
                send(f + " LOCKED " + FileManager.lockedBy(f));
            else
                send(f + " AVAILABLE");
        }

        send("END");
    }

    // ================= VIEW =================

    private void sendFile(String name) throws IOException {
    	
    	System.out.println(username + " requested VIEW for " + name);

        File file = FileManager.getFile(name);

        if (!file.exists()) {
            send("ERROR: file not found");
            return;
        }

        send("CONTENT_BEGIN");

        BufferedReader r = new BufferedReader(new FileReader(file));
        String l;

        while ((l = r.readLine()) != null) {
            send(l);
        }

        send("CONTENT_END");
    }

    // ================= EDIT =================

    private void handleEdit(String file) throws IOException {
    	
    	System.out.println(username + " requested EDIT for " + file);

        synchronized (FileManager.class) {

            if (FileManager.isLocked(file)) {
                send("ERROR: file is already being edited by " + FileManager.lockedBy(file));
                System.out.println(file + " locked by " + username);
                return;
            }

            FileManager.lock(file, username);
            editingFile = file;
        }

        send("SUCCESS: editing started");
        sendFile(file);

        ServerMain.broadcast("INFO: " + file + " was locked by " + username, this);
    }

    // ================= SAVE =================

    private void handleSave(String file) throws IOException {

        System.out.println(username + " requested SAVE for " + file);

        if (!FileManager.isLocked(file)) {
            send("ERROR: file is not in edit mode");
            return;
        }

        if (!FileManager.lockedBy(file).equals(username)) {
            send("ERROR: you are not allowed to save this file");
            return;
        }

        File f = FileManager.getFile(file);

        BufferedWriter w = new BufferedWriter(new FileWriter(f));

        String line;
        while (!(line = in.readLine()).equals("CONTENT_END")) {
            w.write(line);
            w.newLine();
        }

        w.close();

        System.out.println(username + "  saved file " + file);

        FileManager.unlock(file);
        editingFile = null;

        send("SUCCESS: file saved");

        for (ClientHandler c : ServerMain.clients) {
            if (c != this) {
                c.send("CONTENT_BEGIN");

                try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                    String l;
                    while ((l = r.readLine()) != null) {
                        c.send(l);
                    }
                }

                c.send("CONTENT_END");
            }
        }

        ServerMain.broadcast("INFO: " + file + " has been updated", this);
        ServerMain.broadcast("INFO: " + file + "  has been unlocked", this);
    }

    // ================= RENUNTA =================

    private void handleRenunta(String file) {
    	
    	System.out.println(username + " a renuntat la editarea fisierului " + file);
    	System.out.println(file + " unlocked");

        if (!FileManager.lockedBy(file).equals(username)) {
            send("ERROR: you are not allowed to cancel this file");
            return;
        }

        FileManager.unlock(file);
        editingFile = null;

        send("SUCCESS: editare anulata");
        
        

        ServerMain.broadcast("INFO: " + file + " a fost eliberat", this);
    }
}
