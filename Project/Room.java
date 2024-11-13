package Project;
import java.util.ArrayList;
import java.util.List;

public class Room {
    protected String roomName;
    protected List<ClientData> clients;

    // Constructor
    public Room(String roomName) {
        this.roomName = roomName;
        this.clients = new ArrayList<>();
    }

    // Method to add a client to the room
    public void addClient(ClientData client) {
        clients.add(client);
        System.out.println(client.getName() + " joined the room " + roomName);
    }

    // Method to remove a client from the room
    public void removeClient(ClientData client) {
        clients.remove(client);
        System.out.println(client.getName() + " left the room " + roomName);
    }

    // Method to broadcast a message to all clients in the room
    public void broadcastMessage(Payload payload) {
        for (ClientData client : clients) {
            client.getServerThread().sendPayload(payload);
        }
    }

    // Get the room name
    public String getRoomName() {
        return roomName;
    }

    // Set the room name
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
