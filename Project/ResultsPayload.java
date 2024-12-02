package Project;

import java.util.Map;

public class ResultsPayload extends Payload {
    private Map<String, Integer> results;

    public ResultsPayload(String sender, String message, PayloadType type, Map<String, Integer> results) {
        super(sender, message, type);
        this.results = results;
    }

    public Map<String, Integer> getResults() {
        return results;
    }

    public void setResults(Map<String, Integer> results) {
        this.results = results;
    }
}