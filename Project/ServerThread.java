package Project;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ClientData clientData;
    private GameRoom currentRoom;

    // Constructor
    public ServerThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error initializing I/O streams: " + e.getMessage());
        }
    }

    // Get the current room of the client
    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    // Get the client data
    public ClientData getClientData() {
        return clientData;
    }

    // Run method to handle incoming messages from the client
    @Override
    public void run() {
        try {
            Object request;
            while ((request = in.readObject()) != null) {
                if (request instanceof Payload) {
                    Payload payload = (Payload) request;
                    handlePayload(payload);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client disconnected: " + e.getMessage());
            server.removeClient(this);
        }
    }

    // Method to handle different types of payloads
    private void handlePayload(Payload payload) {
        switch (payload.getType()) {
            case CONNECT:
                handleConnectPayload(payload);
                break;
            case CREATE_ROOM:
                handleCreateRoomPayload(payload);
                break;
            case JOIN_ROOM:
                handleJoinRoomPayload(payload);
                break;
            case JOIN_ROOM_AS_SPECTATOR:
                handleJoinRoomAsSpectatorPayload(payload);
                break;
            case ANSWER:
                handleAnswerPayload(payload);
                break;
            case START_GAME:
                handleStartGamePayload(payload);
                break;
            case READY:
                handleReadyPayload(payload);
                break;
            case AWAY_STATUS:
                handleAwayStatusPayload(payload);
                break;
            case NOTIFICATION:
                handleNotificationPayload(payload);
                break;
            default:
                System.err.println("Unknown payload type: " + payload.getType());
                break;
        }
    }

    // Handle connect payload
    private void handleConnectPayload(Payload payload) {
        clientData = new ClientData(payload.getClientId(), this);
        currentRoom = server.getLobby();
        currentRoom.addClient(clientData);
        System.out.println(clientData.getName() + " connected and joined the Lobby");
    }

    // Handle create room payload
    private void handleCreateRoomPayload(Payload payload) {
        String roomName = payload.getMessage();
        boolean roomCreated = server.createRoom(roomName, this);
        if (roomCreated) {
            joinRoom(roomName);
            System.out.println(clientData.getName() + " created and joined the room: " + roomName);
        } else {
            sendPayload(new Payload("Server", "Room creation failed. Room already exists.", PayloadType.NOTIFICATION));
        }
    }

    // Handle join room payload
    private void handleJoinRoomPayload(Payload payload) {
        String roomName = payload.getMessage();
        GameRoom room = server.getRoom(roomName);
        if (room != null) {
            joinRoom(roomName);
            System.out.println(clientData.getName() + " joined the room: " + roomName);
        } else {
            sendPayload(new Payload("Server", "Room not found. Please try again.", PayloadType.NOTIFICATION));
        }
    }

    // Handle join room as spectator payload
    private void handleJoinRoomAsSpectatorPayload(Payload payload) {
        String roomName = payload.getMessage();
        GameRoom room = server.getRoom(roomName);
        if (room != null) {
            room.addSpectator(clientData);
            System.out.println(clientData.getName() + " joined the room as a spectator: " + roomName);
        } else {
            sendPayload(new Payload("Server", "Room not found. Please try again.", PayloadType.NOTIFICATION));
        }
    }
    private void handleAwayStatusPayload(Payload payload) {
        System.out.println("Away status received: " + payload.getMessage());
        boolean isAway = Boolean.parseBoolean(payload.getMessage());
        if (currentRoom != null && currentRoom instanceof GameRoom) {
            ((GameRoom) currentRoom).markClientAway(clientData, isAway);
        }
    }
    // Helper method to join a room
    private void joinRoom(String roomName) {
        if (currentRoom != null) {
            currentRoom.removeClient(clientData);
        }
        currentRoom = server.getRoom(roomName);
        if (currentRoom != null) {
            currentRoom.addClient(clientData);
        }
    }

    // Handle answer payload
    private void handleAnswerPayload(Payload payload) {
        if (currentRoom != null && currentRoom instanceof GameRoom) {
            ((GameRoom) currentRoom).processAnswer(clientData, payload.getMessage());
        }
    }

    // Handle start game payload
    private void handleStartGamePayload(Payload payload) {
        if (currentRoom != null && currentRoom instanceof GameRoom) {
            System.out.println(clientData.getName() + " initiated the game.");
            ((GameRoom) currentRoom).startFirstRound();
        } else {
            System.err.println("Client is not in a GameRoom or the room is invalid.");
        }
    }

    // Handle ready payload
    private void handleReadyPayload(Payload payload) {
        if (currentRoom != null && currentRoom instanceof GameRoom) {
            ((GameRoom) currentRoom).markClientReady(clientData);
        }
    }

    // Handle notification payload
    private void handleNotificationPayload(Payload payload) {
        // For now, simply print out the notification
        System.out.println("Notification: " + payload.getMessage());
    }

    // Handle the `START_GAME` payload to notify the clients to start the game
    private void handleStartGamePayloadFromCountdown() {
        Payload startGamePayload = new Payload("Server", "Game is starting now!", PayloadType.START_GAME);
        sendPayload(startGamePayload);
    }

    // Method to send a payload to the client
    public void sendPayload(Payload payload) {
        try {
            out.writeObject(payload);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending payload to client: " + e.getMessage());
        }
    }

    // Method to disconnect the client
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error disconnecting client: " + e.getMessage());
        }
    }
}
