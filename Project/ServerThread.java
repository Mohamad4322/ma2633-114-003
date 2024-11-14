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
            case ANSWER:
                handleAnswerPayload(payload);
                break;
            case START_GAME:
                handleStartGamePayload(payload);
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

    // Handle notification payload
    private void handleNotificationPayload(Payload payload) {
        // For now, simply print out the notification
        System.out.println("Notification: " + payload.getMessage());
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
}
