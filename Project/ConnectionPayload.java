package Project;

// ConnectionPayload.java - Handles connection-specific payloads
public class ConnectionPayload extends Payload {
    private String roomName;

    // Constructor
    public ConnectionPayload(String clientId, String message, PayloadType type, String roomName) {
        super(clientId, message, type);
        this.roomName = roomName;
    }

    // Getter for room name
    public String getRoomName() {
        return roomName;
    }

    // Setter for room name
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public String toString() {
        return "ConnectionPayload [clientId=" + getClientId() + ", message=" + getMessage() + ", type=" + getType() + ", roomName=" + roomName + "]";
    }
}

