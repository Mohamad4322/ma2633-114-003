package Project;
import java.util.List;

public class Question {
    private String questionText;
    private String category;
    private List<String> answerOptions;
    private String correctAnswer;

    // Constructor
    public Question(String questionText, String category, List<String> answerOptions, String correctAnswer) {
        this.questionText = questionText;
        this.category = category;
        this.answerOptions = answerOptions;
        this.correctAnswer = correctAnswer;
    }

    // Getters
    public String getQuestionText() {
        return questionText;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    @Override
    public String toString() {
        return "Question [questionText=" + questionText + ", category=" + category + ", answerOptions=" + answerOptions + ", correctAnswer=" + correctAnswer + "]";
    }
}
// ma2633 || 11/12