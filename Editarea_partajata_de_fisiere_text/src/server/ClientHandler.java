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
                send("ERROR: trebuie CONNECT username");
                return;
            }

            username = line.split(" ", 2)[1];
            System.out.println("Conectat: " + username);

            send("SUCCESS: conectare reusita");
            sendFileList();

            while ((line = in.readLine()) != null) {

                String[] parts = line.split(" ", 2);
                String cmd = parts[0];

                if (!cmd.equals("FILES") && parts.length < 2) {
                    send("ERROR: comanda invalida");
                    continue;
                }

                switch (cmd) {

                    case "FILES":
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

                    case "RENUNTA":
                        handleRenunta(parts[1]);
                        break;

                    default:
                        send("ERROR: comanda necunoscuta");
                }
            }

        } catch (Exception e) {
            System.out.println("Deconectat: " + username);
        } 
        
        finally {

            String unlockedFile = editingFile;

            FileManager.unlockAllForUser(username);
            ServerMain.clients.remove(this);

            ServerMain.broadcast("INFO: " + username + " s-a deconectat", this);

            if (unlockedFile != null) {
                ServerMain.broadcast("INFO: " + unlockedFile + " a fost deblocat", this);
            }
        }
    }

    // ================= FILE LIST =================

    private void sendFileList() {
        send("FILES");

        for (String f : FileManager.getFiles()) {
            if (FileManager.isLocked(f))
                send(f + " IN_EDITARE " + FileManager.lockedBy(f));
            else
                send(f + " LIBER");
        }

        send("END");
    }

    // ================= VIEW =================

    private void sendFile(String name) throws IOException {
    	
    	System.out.println(username + " a cerut VIEW pentru " + name);

        File file = FileManager.getFile(name);

        if (!file.exists()) {
            send("ERROR: fisier inexistent");
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
    	
    	System.out.println(username + " a cerut EDIT pentru " + file);

        synchronized (FileManager.class) {

            if (FileManager.isLocked(file)) {
                send("ERROR: fisierul este deja editat de " + FileManager.lockedBy(file));
                System.out.println(file + " blocat de " + username);
                return;
            }

            FileManager.lock(file, username);
            editingFile = file;
        }

        send("SUCCESS: editare inceputa");
        sendFile(file);

        ServerMain.broadcast("INFO: " + file + " a fost blocat de " + username, this);
    }

    // ================= SAVE =================

    private void handleSave(String file) throws IOException {

        System.out.println(username + " a cerut SAVE pentru " + file);

        if (!FileManager.isLocked(file)) {
            send("ERROR: fisierul nu este in editare");
            return;
        }

        if (!FileManager.lockedBy(file).equals(username)) {
            send("ERROR: nu ai dreptul sa salvezi acest fisier");
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

        System.out.println(username + " a salvat fisierul " + file);

        FileManager.unlock(file);
        editingFile = null;

        send("SUCCESS: fisier salvat");

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

        ServerMain.broadcast("INFO: " + file + " a fost actualizat", this);
        ServerMain.broadcast("INFO: " + file + " a fost deblocat", this);
    }

    // ================= RENUNTA =================

    private void handleRenunta(String file) {
    	
    	System.out.println(username + " a renuntat la editarea fisierului " + file);
    	System.out.println(file + " deblocat");

        if (!FileManager.lockedBy(file).equals(username)) {
            send("ERROR: nu ai dreptul sa renunti la acest fisier");
            return;
        }

        FileManager.unlock(file);
        editingFile = null;

        send("SUCCESS: editare anulata");
        
        

        ServerMain.broadcast("INFO: " + file + " a fost eliberat", this);
    }
}