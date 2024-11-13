package Project;

public class TimePayload extends Payload {
    private long timeRemaining;

    public TimePayload(String sender, String message, PayloadType type, long timeRemaining) {
        super(sender, message, type);
        this.timeRemaining = timeRemaining;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}