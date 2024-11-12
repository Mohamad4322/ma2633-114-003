package Project;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        QAPayload qaPayload = new QAPayload();
        
        qaPayload.setQuestion("What is the capital of France?");
        qaPayload.setCategory("Geography");
        qaPayload.setAnswers(Arrays.asList("A. Berlin", "B. Madrid", "C. Paris", "D. Rome"));
        qaPayload.setCorrectAnswer("C");

        System.out.println(qaPayload);
    }
}
// ma2633 || 11/11/2024