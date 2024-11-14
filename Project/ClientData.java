package Project;

public class ClientData {
    private String name;
    private ServerThread serverThread;
    private int points;

    // Constructor
    public ClientData(String name, ServerThread serverThread) {
        this.name = name;
        this.serverThread = serverThread;
        this.points = 0; // Initialize points to zero
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    // Getter for serverThread
    public ServerThread getServerThread() {
        return serverThread;
    }

    // Setter for serverThread
    public void setServerThread(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    // Getter for points
    public int getPoints() {
        return points;
    }

    // Setter for points
    public void setPoints(int points) {
        this.points = points;
    }

    // Method to add points
    public void addPoints(int additionalPoints) {
        this.points += additionalPoints;
    }
}
