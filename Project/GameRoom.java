package Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameRoom {
    private String roomName;
    private List<Question> questionList;
    private List<ClientData> clients;
    private int currentRound;
    private Question currentQuestion;
    private Timer roundTimer;

    // Constructor
    public GameRoom(String roomName) {
        this.roomName = roomName;
        this.clients = new ArrayList<>();
        this.questionList = new ArrayList<>();
        this.currentRound = 0;
        this.roundTimer = new Timer();
        // Load questions with hardcoded path
        loadQuestions("C:\\Users\\Mohamad\\Desktop\\ma2633-114-003\\Project\\questions.txt");
    }

    // Method to load questions from a file
    public void loadQuestions(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 4);
                if (parts.length == 4) {
                    String questionText = parts[0];
                    String category = parts[1];
                    String[] answers = parts[2].split(",");
                    String correctAnswer = parts[3];
                    Question question = new Question(questionText, category, Arrays.asList(answers), correctAnswer);
                    questionList.add(question);
                    System.out.println("Loaded question: " + questionText); // Debug message
                } else {
                    System.out.println("Invalid question format: " + line); // Debug message for incorrect format
                }
            }
            System.out.println("Total questions loaded: " + questionList.size()); // Debug message
        } catch (IOException e) {
            System.err.println("Error loading questions: " + e.getMessage());
        }
    }

    // Method to start the first round
    public void startFirstRound() {
        if (!questionList.isEmpty()) {
            currentRound++;
            System.out.println("Starting round " + currentRound); // Debug message
            startRound();
        } else {
            System.out.println("No questions available to start the game.");
        }
    }

    // Method to start a round
    private void startRound() {
        if (!questionList.isEmpty()) {
            currentQuestion = questionList.remove((int) (Math.random() * questionList.size()));
            broadcastQuestionToClients(currentQuestion);
            startRoundTimer();
        } else {
            System.out.println("No more questions available.");
            endSession();
        }
    }

    // Method to broadcast the current question to all clients
    private void broadcastQuestionToClients(Question question) {
        QAPayload payload = new QAPayload("Server", "New Question", PayloadType.QUESTION, question.getQuestionText(), question.getAnswerOptions().toArray(new String[0]));
        for (ClientData client : clients) {
            client.getServerThread().sendPayload(payload);
        }
        System.out.println("Broadcasting question to clients: " + question.getQuestionText()); // Debug message
    }

    // Method to start the round timer with periodic updates to clients
    private void startRoundTimer() {
        long roundDuration = 30000; // 30 seconds per round
        long updateInterval = 5000; // Update every 5 seconds

        roundTimer.scheduleAtFixedRate(new TimerTask() {
            long timeRemaining = roundDuration;

            @Override
            public void run() {
                timeRemaining -= updateInterval;
                if (timeRemaining > 0) {
                    broadcastTimeUpdate(timeRemaining);
                } else {
                    System.out.println("Round timer expired.");
                    endRound();
                    cancel();
                }
            }
        }, 0, updateInterval);
    }

    // Method to broadcast time updates to all clients
    private void broadcastTimeUpdate(long timeRemaining) {
        for (ClientData client : clients) {
            TimePayload timePayload = new TimePayload("Server", "Time Update", PayloadType.TIME, timeRemaining);
            client.getServerThread().sendPayload(timePayload);
        }
        System.out.println("Broadcasting time update to clients: " + timeRemaining / 1000 + " seconds remaining");
    }
    // ma2633 || 11/12

    // Method to process a player's answer
    public void processAnswer(ClientData client, String answer) {
        if (currentQuestion != null && currentQuestion.getAnswerOptions().contains(answer)) {
            if (answer.equalsIgnoreCase(currentQuestion.getCorrectAnswer())) {
                client.addPoints(10); // Award points to the player
                System.out.println(client.getName() + " answered correctly and earned 10 points.");
            } else {
                System.out.println(client.getName() + " answered incorrectly.");
            }
            checkAllPlayersAnswered();
        } else {
            System.out.println("Invalid answer provided by " + client.getName());
        }
    }

    // Method to check if all players have answered
    private void checkAllPlayersAnswered() {
        boolean allAnswered = true;
        for (ClientData client : clients) {
            // In a real implementation, we would track if each player answered
            // For simplicity, assuming all players answer in sequence for now
        }
        if (allAnswered) {
            endRound();
        }
    }

    // Method to end the round
    private void endRound() {
        roundTimer.cancel();
        roundTimer = new Timer(); // Reset the timer for the next round
        syncPointsToClients();
        if (currentRound < 5) { // Example condition for session end after 5 rounds
            startRound();
        } else {
            endSession();
        }
    }

    // Method to sync points to all clients
    private void syncPointsToClients() {
        for (ClientData client : clients) {
            PointsPayload pointsPayload = new PointsPayload("Server", "Points Update", PayloadType.POINTS, client.getPoints());
            client.getServerThread().sendPayload(pointsPayload);
        }
    }

    // Method to end the session
    private void endSession() {
        System.out.println("Game session ended.");
        // Send final scoreboard to all clients
        for (ClientData client : clients) {
            PointsPayload pointsPayload = new PointsPayload("Server", "Final Score", PayloadType.POINTS, client.getPoints());
            client.getServerThread().sendPayload(pointsPayload);
        }
        resetGame();
    }

    // Method to reset the game
    private void resetGame() {
        currentRound = 0;
        questionList.clear();
        System.out.println("Game reset. Ready for a new session.");
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
}
