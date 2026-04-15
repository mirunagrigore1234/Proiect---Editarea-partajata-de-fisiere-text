package client;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;

public class ServerListener implements Runnable {

    private BufferedReader in;
    private List<String> buffer;
    private boolean[] editAllowed;
    private boolean[] editResponseReceived;
    private Object editLock;
    private PrintWriter out;

    private boolean[] fileLoaded;

    public ServerListener(BufferedReader in, PrintWriter out,
                          List<String> buffer,
                          boolean[] editAllowed,
                          boolean[] editResponseReceived,
                          boolean[] fileLoaded,   // 👈 ADĂUGAT
                          Object editLock) {

        this.in = in;
        this.out = out;
        this.buffer = buffer;
        this.editAllowed = editAllowed;
        this.editResponseReceived = editResponseReceived;
        this.fileLoaded = fileLoaded; // 👈 IMPORTANT
        this.editLock = editLock;
    }

    @Override
    public void run() {
        try {
            String line;
            boolean readingContent = false;
            boolean editContentMode = false;

            while ((line = in.readLine()) != null) {

                // ================= CONTENT =================

            	if (line.equals("CONTENT_BEGIN")) {
            	    readingContent = true;
            	    buffer.clear();
                    continue;
            	}

                if (line.equals("CONTENT_END")) {
                    readingContent = false;
                    editContentMode = false;
                    
                    synchronized (editLock) {
                        fileLoaded[0] = true;
                        editLock.notifyAll();
                    }
                    
                    continue;
                }

                if (readingContent) {
                    buffer.add(line);

                    if (!editContentMode) {
                        System.out.println(line);
                    }

                    continue;
                }

                // ================= EDIT RESPONSE =================

                if (line.startsWith("ERROR: fisierul este deja editat")) {
                    synchronized (editLock) {
                        editAllowed[0] = false;
                        editResponseReceived[0] = true;
                        editLock.notify();
                    }

                    System.out.println("[ERROR] " + line.substring(6).trim());
                    continue;
                }

                else if (line.startsWith("SUCCESS: editare inceputa")) {
                    synchronized (editLock) {
                        editAllowed[0] = true;
                        editResponseReceived[0] = true;
                        editLock.notify();
                    }

                    editContentMode = true;
                    System.out.println("[SERVER] editare permisa");
                    continue;
                }

                else if (line.startsWith("ERROR:")) {
                    System.out.println("[ERROR] " + line.substring(6).trim());
                    continue;
                }

                else if (line.startsWith("SUCCESS:")) {
                    System.out.println("[SERVER] " + line.substring(8).trim());
                    continue;
                }

                else if (line.startsWith("INFO: FILE_ADDED ")) {
                    String file = line.substring("INFO: FILE_ADDED ".length());
                    System.out.println("[SERVER] Fișier nou: " + file);
                    out.println("FILES"); 
                    continue;
                }

                else if (line.startsWith("INFO: FILE_REMOVED ")) {
                    String file = line.substring("INFO: FILE_REMOVED ".length());
                    System.out.println("[SERVER] Fișier șters: " + file);
                    out.println("FILES"); 
                    continue;
                }
                else if (line.startsWith("INFO:")) {
                    System.out.println("[SERVER] " + line.substring(5).trim());
                    continue;
                }

                else {
                    System.out.println(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}