package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClientMain {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in));

            System.out.print("Username: ");
            String name = console.readLine();

            out.println("CONNECT " + name);

            List<String> buffer = new ArrayList<>();

            boolean[] editAllowed = new boolean[1];
            boolean[] editResponseReceived = new boolean[1];
            
            Object editLock = new Object();
            boolean[] fileLoaded = new boolean[1];

            new Thread(new ServerListener(
                    in, out, buffer,
                    editAllowed, editResponseReceived,
                    fileLoaded,   
                    editLock
            )).start();

            boolean isEditing = false;
            String editingFile = null;

            while (true) {

                if (isEditing) {
                    System.out.print("(edit)> ");
                    String line = console.readLine();

                    if (line == null) break;

                    if (line.equalsIgnoreCase("SAVE")) {
                        out.println("SAVE " + editingFile);

                        for (String l : buffer) {
                            out.println(l);
                        }

                        out.println("CONTENT_END");

                        buffer.clear();
                        isEditing = false;
                        editingFile = null;
                        continue;
                    }

                    if (line.equalsIgnoreCase("CANCEL")) {
                        out.println("CANCEL " + editingFile);

                        buffer.clear();
                        isEditing = false;
                        editingFile = null;
                        continue;
                    }

                    if (line.startsWith("APPENDLINE ")) {
                        String[] parts = line.split(" ", 3);

                        if (parts.length < 3) {
                            System.out.println("[ERROR] Usage: APPENDLINE line_number text");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Line does not exist");
                                continue;
                            }

                            buffer.set(index, buffer.get(index) + " " + parts[2]);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Invalid number");
                        }

                        continue;
                    }

                    if (line.startsWith("APPEND ")) {
                        buffer.add(line.substring(7));
                        showBuffer(buffer);
                        continue;
                    }

                    if (line.startsWith("REPLACEWORD ")) {
                        String[] parts = line.split(" ", 4);

                        if (parts.length < 4) {
                            System.out.println("[ERROR] Usage: REPLACEWORD line_number old_word new_word");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Line does not exist");
                                continue;
                            }

                            String updated = buffer.get(index).replaceFirst(
                                    "\\b" + Pattern.quote(parts[2]) + "\\b",
                                    parts[3]
                            );

                            buffer.set(index, updated);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Invalid number");
                        }

                        continue;
                    }

                    if (line.startsWith("REPLACE ")) {
                        String[] parts = line.split(" ", 3);

                        if (parts.length < 3) {
                            System.out.println("[ERROR] Usage: REPLACEWORD line_number old_word new_word");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Line does not exist");
                                continue;
                            }

                            buffer.set(index, parts[2]);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Invalid number");
                        }

                        continue;
                    }

                    if (line.startsWith("DELETEWORD ")) {
                        String[] parts = line.split(" ", 3);

                        if (parts.length < 3) {
                            System.out.println("[ERROR] Usage: DELETEWORD line_number word");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Line does not exist");
                                continue;
                            }

                            String updated = buffer.get(index)
                                    .replaceFirst("\\b" + Pattern.quote(parts[2]) + "\\b", "")
                                    .replaceAll("\\s+", " ")
                                    .trim();

                            buffer.set(index, updated);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Invalid number");
                        }

                        continue;
                    }

                    if (line.startsWith("DELETE ")) {
                        try {
                            int index = Integer.parseInt(line.split(" ")[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Line does not exist");
                                continue;
                            }

                            buffer.remove(index);
                            showBuffer(buffer);

                        } catch (Exception e) {
                            System.out.println("[ERROR] Invalid number");
                        }

                        continue;
                    }

                    if (line.startsWith("INSERT ")) {
                        String[] parts = line.split(" ", 3);

                        if (parts.length < 3) {
                            System.out.println("[ERROR] Usage: INSERT line_number text");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index > buffer.size()) {
                                System.out.println("[ERROR] Invalid position");
                                continue;
                            }

                            buffer.add(index, parts[2]);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Invalid number");
                        }

                        continue;
                    }

                    System.out.println("[ERROR] Invalid command");
                    continue;
                }

                System.out.print("> ");
                String cmd = console.readLine();

                if (cmd == null) break;

                if (cmd.startsWith("EDIT ")) {

                    synchronized (editLock) {
                        editAllowed[0] = false;
                        editResponseReceived[0] = false;
                        fileLoaded[0] = false;

                        out.println(cmd);

                        while (!editResponseReceived[0]) {
                            editLock.wait();
                        }

                        if (!editAllowed[0]) {
                            continue;
                        }
                        while (!fileLoaded[0]) {
                            editLock.wait();
                        }
                    }

                    editingFile = cmd.split(" ", 2)[1];
                    isEditing = true;

                    System.out.println("[SERVER] Editing mode for " + editingFile);
                    showBuffer(buffer);

                    continue;
                }

                out.println(cmd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showBuffer(List<String> buffer) {
        if (buffer.isEmpty()) {
            System.out.println("[SERVER] File is empty");
            return;
        }

        for (int i = 0; i < buffer.size(); i++) {
            System.out.println((i + 1) + ": " + buffer.get(i));
        }
    }
}
