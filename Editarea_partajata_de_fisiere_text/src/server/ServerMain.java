package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerMain {

    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Server started on port 5000...");
            
            new Thread(new FileWatcher("server_files")).start();

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);

                clients.add(client);

                new Thread(client).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler c : clients) {
            if (c != sender) {
                c.send(message);
            }
        }
    }
}
