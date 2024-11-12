package Project;

import java.util.List;
// ma2633 || 11/11/24
/**
 * QAPayload is used for sending questions and answer options to players.
 */
public class QAPayload extends Payload {
    private String question;
    private String category;
    private List<String> answers;

    /**
     * The correct answer option, like "A", "B", "C", or "D".
     */
    private String correctAnswer;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    @Override
    public String toString() {
        return String.format("QAPayload[Question: %s, Category: %s, Answers: %s, Correct Answer: %s]", 
                question, category, answers, correctAnswer);
    }
}
// ma2633 || 11/11/24