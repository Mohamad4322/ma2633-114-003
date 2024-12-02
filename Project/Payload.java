package Project;

import java.io.Serializable;

public class Payload implements Serializable {
    private static final long serialVersionUID = 1L;
    private String clientId;
    private String message;
    private PayloadType type;

    // Constructor
    public Payload(String clientId, String message, PayloadType type) {
        this.clientId = clientId;
        this.message = message;
        this.type = type;
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PayloadType getType() {
        return type;
    }

    public void setType(PayloadType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Payload [clientId=" + clientId + ", message=" + message + ", type=" + type + "]";
    }
}
