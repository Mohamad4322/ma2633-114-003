package Project;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public class Client {
    private String clientId;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Map<String, Integer> playerPoints; // Map to store player points
    private GameUI gameUI; // Reference to GameUI

    // Constructor
    public Client(GameUI gameUI) {
        // try {
        //     this.gameUI = gameUI; // Assign the GameUI reference
            
        // } catch (IOException e) {
        //     System.err.println("Unable to connect to server: " + e.getMessage());
        //     System.exit(1);
        // }
        this.gameUI = gameUI;
    }

    // Method to set client name
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    // Method to connect to the server (called from UI)
    public void connect(String host, int port) {
        if (clientId == null) {
            System.out.println("Please set your name first.");
        } else {
            try {
                socket = new Socket(host, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                playerPoints = new HashMap<>(); // Initialize the player points map
                System.out.println("Connected to server at " + host + ":" + port);
                new Thread(new ServerListener()).start(); // Start listening to server messages
                Payload payload = new Payload(clientId, "Connecting", PayloadType.CONNECT);
                sendPayload(payload);
            } catch (IOException e) {
                System.err.println("Unable to connect to server: " + e.getMessage());
            }
        }
    }

    // Method to create a room
    public void createRoom(String roomName) {
        if (roomName == null || roomName.isEmpty()) {
            System.out.println("Room name cannot be empty.");
        } else {
            Payload payload = new Payload(clientId, roomName, PayloadType.CREATE_ROOM);
            sendPayload(payload);
            System.out.println("Room '" + roomName + "' created. Waiting for others to join...");
        }
    }

    // Method to join a room
    public void joinRoom(String roomName) {
        if (roomName == null || roomName.isEmpty()) {
            System.out.println("Room name cannot be empty.");
        } else {
            Payload payload = new Payload(clientId, roomName, PayloadType.JOIN_ROOM);
            sendPayload(payload);
            System.out.println("Joined room '" + roomName + "'.");
        }
    }
    //method to join room as spectator
    public void joinRoomAsSpectator(String roomName){
        if (roomName == null || roomName.isEmpty()) {
            System.out.println("Room name cannot be empty.");
        } else {
            Payload payload = new Payload(clientId, roomName, PayloadType.JOIN_ROOM_AS_SPECTATOR);
            sendPayload(payload);
            System.out.println("Joined room '" + roomName + "'.");
        }
    }
    // Method to mark the client as ready
    public void markReady() {
        if (clientId == null) {
            System.out.println("Please set your name first.");
        } else {
            Payload payload = new Payload(clientId, "Ready", PayloadType.READY);
            sendPayload(payload);
            System.out.println("You are marked as ready.");
        }
    }

    // Method to send an answer (called from UI)
    public void sendAnswer(String answer) {
        Payload payload = new Payload(clientId, answer, PayloadType.ANSWER);
        sendPayload(payload);
    }
    
    // Method to send away status
    public void sendAwayStatus(boolean isAway) {
        Payload payload = new Payload(clientId, String.valueOf(isAway), PayloadType.AWAY_STATUS);
        sendPayload(payload);
    }

    public void setSelectedCategories(List <String> selectedCategories){
        Payload payload = new Payload(clientId, selectedCategories.toString(), PayloadType.SELECTED_CATEGORIES);
        sendPayload(payload);
    }
    // Method to send a payload to the server
    private void sendPayload(Payload payload) {
        try {
            out.writeObject(payload);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending payload: " + e.getMessage());
        }
    }

    // Inner class to listen to server messages
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                Object response;
                while ((response = in.readObject()) != null) {
                    if (response instanceof QAPayload) {
                        QAPayload qaPayload = (QAPayload) response;
                        GameUI.updateQuestion(qaPayload.getQuestion(), qaPayload.getAnswerOptions());
                    } else if (response instanceof PointsPayload) {
                        PointsPayload pointsPayload = (PointsPayload) response;
                        playerPoints.put(pointsPayload.getClientId(), pointsPayload.getPoints());
                        GameUI.updatePlayerPoints(playerPoints);
                    }
                    else if (response instanceof TimePayload) {
                            System.out.println("Received time payload");
                            TimePayload timePayload = (TimePayload) response;
                            GameUI.updateTimer((int) (timePayload.getTimeRemaining() / 1000));
                    
                    } else if (response instanceof Payload) {
                        Payload payload = (Payload) response;
                        switch (payload.getType()) {
                            case RESET_POINTS:
                                resetPlayerPoints();
                                GameUI.resetPlayerPoints();
                                break;
                            case NOTIFICATION:
                                handleNotification(payload);
                                GameUI.displayNotification(payload.getMessage());
                                break;
                            case CREATE_ROOM:
                                System.out.println("Room created successfully: " + payload.getMessage());
                                break;
                            case JOIN_ROOM:
                                System.out.println("Successfully joined room: " + payload.getMessage());
                                break;
                            case START_GAME:
                                SwingUtilities.invokeLater(() -> gameUI.startCountdown()); // Start the countdown on the ready page
                                break;
                            case QUESTION_TIMER:
                                handleQuestionTimer(payload); // Handle question timer countdown
                                break;
                            case READY:
                                System.out.println("Player " + payload.getClientId() + " is ready.");
                                SwingUtilities.invokeLater(() -> gameUI.updateToReadyCheckPanel());
                                GameUI.showFinalScore(playerPoints);
                                break;
                            default:
                                System.out.println(payload);
                                break;
                        }
                    }   
                    else {
                        System.out.println("Unknown response from server: " + response);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Connection to server lost: " + e.getMessage());
            }
        }
    }

    // Method to handle the question timer countdown received from the server
    private void handleQuestionTimer(Payload payload) {
        SwingUtilities.invokeLater(() -> {
            try {
                int timeRemaining = Integer.parseInt(payload.getMessage());
                GameUI.updateTimer(timeRemaining);
            } catch (NumberFormatException e) {
                System.err.println("Invalid timer format: " + e.getMessage());
            }
        });
    }

    // Method to reset player points locally
    private void resetPlayerPoints() {
        playerPoints.clear();
        System.out.println("All player points have been reset.");
    }

    // Method to handle notification payloads
    private void handleNotification(Payload payload) {
        System.out.println("Notification: " + payload.getMessage());
    }

    // Main method to start the client
    public static void main(String[] args) {
        final String host = (args.length > 0) ? args[0] : "localhost";
        final int port;
        if (args.length > 1) {
            int parsedPort;
            try {
                parsedPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port 12345");
                parsedPort = 12345;
            }
            port = parsedPort;
        } else {
            port = 12345; // Default port
        }

        SwingUtilities.invokeLater(() -> new GameUI(host, port));
    }
}
