package Project;
class QAPayload extends Payload {
    private String question;
    private String[] answerOptions;

    public QAPayload(String clientId, String message, PayloadType type, String question, String[] answerOptions) {
        super(clientId, message, type);
        this.question = question;
        this.answerOptions = answerOptions;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(String[] answerOptions) {
        this.answerOptions = answerOptions;
    }

    @Override
    public String toString() {
        return "QAPayload [clientId=" + getClientId() + ", message=" + getMessage() + ", type=" + getType() + ", question=" + question + ", answerOptions=" + String.join(", ", answerOptions) + "]";
    }
}
// ma2633 || 11/12