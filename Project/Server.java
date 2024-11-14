package Project;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ServerThread> serverThreads;
    private GameRoom lobby;

    // Constructor
    public Server(int port) {
        this.port = port;
        this.serverThreads = new ArrayList<>();
        this.lobby = new GameRoom("Lobby");
    }

    // Method to start the server
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ServerThread serverThread = new ServerThread(clientSocket, this);
                serverThreads.add(serverThread);
                serverThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    // Method to broadcast messages to all clients in the same room
    public void broadcastToRoom(GameRoom room, Payload payload) {
        for (ServerThread serverThread : serverThreads) {
            if (serverThread.getCurrentRoom() == room) {
                serverThread.sendPayload(payload);
            }
        }
    }

    // Method to remove a client from the server
    public void removeClient(ServerThread serverThread) {
        serverThreads.remove(serverThread);
        GameRoom currentRoom = serverThread.getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.removeClient(serverThread.getClientData());
        }
        System.out.println("Client disconnected: " + serverThread.getClientData().getName());
    }

    // Getter for the lobby
    public GameRoom getLobby() {
        return lobby;
    }

    // Main method to start the server
    public static void main(String[] args) {
        int port = 12345; // Default port
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + port);
            }
        }
        Server server = new Server(port);
        server.start();
    }
}
