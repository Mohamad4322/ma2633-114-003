package Project;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private String clientId;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private BufferedReader console;

    // Constructor
    public Client(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            console = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connected to server at " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("Unable to connect to server: " + e.getMessage());
            System.exit(1);
        }
    }

    // Method to set client name
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    // Method to start the client
    public void start() {
        new Thread(new ServerListener()).start();
        try {
            String input;
            while ((input = console.readLine()) != null) {
                if (input.startsWith("/name")) {
                    String[] parts = input.split(" ", 2);
                    if (parts.length == 2) {
                        setClientId(parts[1]);
                        System.out.println("Client name set to: " + clientId);
                    } else {
                        System.out.println("Usage: /name [clientName]");
                    }
                } else if (input.startsWith("/connect")) {
                    if (clientId == null) {
                        System.out.println("Please set your name first using /name [clientName]");
                    } else {
                        Payload payload = new Payload(clientId, "Connecting", PayloadType.CONNECT);
                        sendPayload(payload);
                    }
                } else if (input.startsWith("/answer")) {
                    String[] parts = input.split(" ", 2);
                    if (parts.length == 2) {
                        String answer = parts[1];
                        Payload payload = new Payload(clientId, answer, PayloadType.ANSWER);
                        sendPayload(payload);
                    } else {
                        System.out.println("Usage: /answer [choice]");
                    }
                } else if (input.startsWith("/start")) {
                    if (clientId == null) {
                        System.out.println("Please set your name first using /name [clientName]");
                    } else {
                        Payload payload = new Payload(clientId, "Start Game", PayloadType.START_GAME);
                        sendPayload(payload);
                    }
                } else {
                    System.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from console: " + e.getMessage());
        }
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
                        System.out.println("Question: " + qaPayload.getQuestion());
                        String[] options = qaPayload.getAnswerOptions();
                        for (int i = 0; i < options.length; i++) {
                            System.out.println((char) ('A' + i) + ". " + options[i]);
                        }
                    } else if (response instanceof PointsPayload) {
                        PointsPayload pointsPayload = (PointsPayload) response;
                        System.out.println("Player " + pointsPayload.getClientId() + " has " + pointsPayload.getPoints() + " points.");
                    } else if (response instanceof Payload) {
                        Payload payload = (Payload) response;
                        System.out.println(payload.getMessage());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Connection to server lost: " + e.getMessage());
            }
        }
    }

    // Main method to start the client
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345; // Default port
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + port);
            }
        }
        Client client = new Client(host, port);
        client.start();
    }
}