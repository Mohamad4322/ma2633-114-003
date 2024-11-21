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
    private List<ClientData> readyClients; // Track clients who are ready
    private int currentRound;
    private Question currentQuestion;
    private Timer roundTimer;
    private List<ClientData> answeredClients; // Track clients who answered
    private long questionStartTime; // Track when the current question was broadcasted

    // Constructor
    public GameRoom(String roomName) {
        this.roomName = roomName;
        this.clients = new ArrayList<>();
        this.readyClients = new ArrayList<>();
        this.questionList = new ArrayList<>();
        this.answeredClients = new ArrayList<>();
        this.currentRound = 0;
        this.roundTimer = new Timer();
        // Load questions with hardcoded path
        loadQuestions("Project/questions.txt");
    }

    // Method to load questions from a file
    public void loadQuestions(String filePath) {
        questionList.clear(); // Clear any previous questions
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

    // Method to notify all players when a player locks in an answer
    public void notifyPlayersAnswerLocked(ClientData player) {
        String message = player.getName() + " has locked in an answer.";
        Payload payload = new Payload("Server", message, PayloadType.NOTIFICATION);
        for (ClientData client : clients) {
            client.getServerThread().sendPayload(payload);
        }
    }

    // Method to mark a client as ready
    public void markClientReady(ClientData client) {
        if (!readyClients.contains(client)) {
            readyClients.add(client);
            System.out.println(client.getName() + " is marked as ready.");

            // If all clients in the room are ready, start the game
            if (readyClients.size() == clients.size()) {
                System.out.println("All players are ready. Starting the game.");
                startFirstRound();
            }
        }
    }

    // Method to start the first round
    public void startFirstRound() {
        // Reload questions if the question list is empty
        if (questionList.isEmpty()) {
            loadQuestions("Project/questions.txt");
        }
        
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
            answeredClients.clear(); // Reset the list of clients who have answered
            questionStartTime = System.currentTimeMillis(); // Set the start time for this question
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
        long roundDuration = 50000; // 50 seconds per round
        long updateInterval = 5000; // Update every 5 seconds

        roundTimer.scheduleAtFixedRate(new TimerTask() {
            long timeRemaining = roundDuration;

            @Override
            public void run() {
                timeRemaining -= updateInterval;
                if (timeRemaining > 0) {
                    // Commenting out time update broadcast for now
                    // broadcastTimeUpdate(timeRemaining);
                } else {
                    System.out.println("Round timer expired.");
                    endRound();
                    cancel();
                }
            }
        }, 0, updateInterval);
    }

    // Method to process a player's answer
    public void processAnswer(ClientData client, String answer) {
        if (currentQuestion != null) {
            // Get the answer options
            List<String> options = currentQuestion.getAnswerOptions();

            // Determine the index based on the provided letter (A, B, C, D)
            int answerIndex = -1;
            if (answer.length() == 1) {
                char answerChar = answer.toUpperCase().charAt(0);
                if (answerChar >= 'A' && answerChar < 'A' + options.size()) {
                    answerIndex = answerChar - 'A';
                }
            }

            if (answerIndex >= 0 && answerIndex < options.size()) {
                String selectedAnswer = options.get(answerIndex);
                long responseTime = System.currentTimeMillis() - questionStartTime; // Calculate response time

                if (selectedAnswer.equalsIgnoreCase(currentQuestion.getCorrectAnswer())) {
                    int pointsAwarded = calculatePoints(responseTime); // Calculate points based on response time
                    client.addPoints(pointsAwarded);
                    System.out.println(client.getName() + " answered correctly and earned " + pointsAwarded + " points.");
                } else {
                    System.out.println(client.getName() + " answered incorrectly.");
                }
                notifyPlayersAnswerLocked(client); // Notify all players that this player has locked in an answer

                // Add the client to the list of those who have answered
                if (!answeredClients.contains(client)) {
                    answeredClients.add(client);
                }

                // Check if all players have answered
                checkAllPlayersAnswered();
            } else {
                System.out.println("Invalid answer provided by " + client.getName());
            }
        } else {
            System.out.println("No current question available for " + client.getName());
        }
    }

    // Method to calculate points based on response time
    private int calculatePoints(long responseTime) {
        if (responseTime <= 10000) { // If answered within 10 seconds
            return 20; // Fast response, higher points
        } else if (responseTime <= 30000) { // If answered within 30 seconds
            return 10; // Medium response, standard points
        } else {
            return 5; // Slow response, fewer points
        }
    }

    // Method to check if all players have answered
    private void checkAllPlayersAnswered() {
        if (answeredClients.size() == clients.size()) {
            System.out.println("All players have answered. Ending the round.");
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
        shiftToReadyPhase();
    }

    // Method to reset the game
    private void resetGame() {
        currentRound = 0;
        questionList.clear(); // Clear previous questions
        loadQuestions("Project/questions.txt"); // Reload questions for the next session
        System.out.println("Game reset. Ready for a new session.");
    }

    // Method to shift all players back to ready phase
    private void shiftToReadyPhase() {
        readyClients.clear();
        for (ClientData client : clients) {
            client.setPoints(0); // Reset player points
            Payload readyPayload = new Payload("Server", "Game is ready for a new session", PayloadType.NOTIFICATION);
            client.getServerThread().sendPayload(readyPayload);
        }
        System.out.println("Players are shifted back to the ready phase.");
    }

    // Method to add a client to the room
    public void addClient(ClientData client) {
        clients.add(client);
        System.out.println(client.getName() + " joined the room " + roomName);

        // Sync the current game state if a game is in progress
        if (currentRound > 0 && currentQuestion != null) {
            // Send the current question to the newly joined client
            QAPayload questionPayload = new QAPayload("Server", "Current Question", PayloadType.QUESTION, currentQuestion.getQuestionText(), currentQuestion.getAnswerOptions().toArray(new String[0]));
            client.getServerThread().sendPayload(questionPayload);

            // Send the remaining time for the current round
            long timeRemaining = calculateRemainingTime();
            TimePayload timePayload = new TimePayload("Server", "Time Update", PayloadType.TIME, timeRemaining);
            client.getServerThread().sendPayload(timePayload);

            // Sync the points for all clients to the newly joined client
            syncPointsToClient(client);
        }
    }

    // Method to remove a client from the room
    public void removeClient(ClientData client) {
        clients.remove(client);
        readyClients.remove(client);
        System.out.println(client.getName() + " left the room " + roomName);
    }

    // Method to sync points to a specific client
    private void syncPointsToClient(ClientData client) {
        for (ClientData existingClient : clients) {
            PointsPayload pointsPayload = new PointsPayload("Server", "Points Update", PayloadType.POINTS, existingClient.getPoints());
            client.getServerThread().sendPayload(pointsPayload);
        }
    }

    // Placeholder method to calculate remaining time for the current round
    private long calculateRemainingTime() {
        return 20000; // Example: 20 seconds remaining
    }
}
