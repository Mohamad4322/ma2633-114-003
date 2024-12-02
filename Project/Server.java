package Project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private int port;
    private List<ServerThread> serverThreads; // List of all active client threads
    private Map<String, GameRoom> gameRooms; // Map to store all game rooms by room name
    private GameRoom lobby;
    private long startTime; // Server start time

    // Constructor
    public Server(int port) {
        this.port = port;
        this.serverThreads = Collections.synchronizedList(new ArrayList<>()); // Thread-safe list
        this.gameRooms = new ConcurrentHashMap<>(); // Concurrent map to store rooms
        this.lobby = new GameRoom("Lobby");
        gameRooms.put("Lobby", lobby); // Initialize the Lobby
        this.startTime = System.currentTimeMillis(); // Record the start time
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

    // Method to create a new room
    public boolean createRoom(String roomName, ServerThread creatorThread) {
        if (!gameRooms.containsKey(roomName)) {
            GameRoom newRoom = new GameRoom(roomName);
            gameRooms.put(roomName, newRoom);
            System.out.println("Room created: " + roomName);
            return true;
        } else {
            System.out.println("Room with name " + roomName + " already exists.");
            return false;
        }
    }

    // Method to get a room by name
    public GameRoom getRoom(String roomName) {
        return gameRooms.get(roomName);
    }

    // Method to broadcast messages to all clients in the same room
    public void broadcastToRoom(GameRoom room, Payload payload) {
        synchronized (serverThreads) {
            for (ServerThread serverThread : serverThreads) {
                if (serverThread.getCurrentRoom() == room) {
                    serverThread.sendPayload(payload);
                }
            }
        }
    }

    // Method to remove a client from the server
    public void removeClient(ServerThread serverThread) {
        synchronized (serverThreads) {
            serverThreads.remove(serverThread);
        }
        GameRoom currentRoom = serverThread.getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.removeClient(serverThread.getClientData());
        }
        System.out.println("Client disconnected: " + serverThread.getClientData().getName());
    }

    public void stop() {
        synchronized (serverThreads) {
            for (ServerThread serverThread : serverThreads) {
                serverThread.disconnect();
            }
        }
    }

    // Getter for the lobby
    public GameRoom getLobby() {
        return lobby;
    }

    // Getter for server threads
    public List<ServerThread> getServerThreads() {
        return serverThreads;
    }

    // Getter for start time
    public long getStartTime() {
        return startTime;
    }

    // Main method to start the server
    public static void main(String[] args) {
        int port = 12345; // Default port
        boolean useUI = true; // Default to UI mode
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.equalsIgnoreCase("--headless")) {
                    useUI = false;
                } else if (arg.startsWith("--port=")) {
                    try {
                        port = Integer.parseInt(arg.split("=")[1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port number. Using default port " + port);
                    }
                }
            }
        }
        Server server = new Server(port);
        if (useUI) {
            System.out.println("Starting server with UI...");
            ServerUI.launch(server); // Start Swing UI
        } else {
            server.start(); // Start headless mode
        }
    }

    // Method to start game in a room (handle countdown and game start)
    public void startGameInRoom(String roomName) {
        GameRoom room = getRoom(roomName);
        if (room != null && !room.isGameStarted()) {
            room.startCountdown();  // Start countdown for this room
        }
    }
}
