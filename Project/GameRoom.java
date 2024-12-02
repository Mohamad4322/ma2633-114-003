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
    private boolean gameStarted; // Track if the game has started

    // Constructor
    public GameRoom(String roomName) {
        this.roomName = roomName;
        this.clients = new ArrayList<>();
        this.readyClients = new ArrayList<>();
        this.questionList = new ArrayList<>();
        this.answeredClients = new ArrayList<>();
        this.currentRound = 0;
        this.roundTimer = new Timer();
        this.gameStarted = false; // Initialize gameStarted to false
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

    // Method to mark a client as ready
    public void markClientReady(ClientData client) {
        if (!readyClients.contains(client)) {
            readyClients.add(client);
            System.out.println(client.getName() + " is marked as ready.");

            // If all clients in the room are ready and the game hasn't started, start the countdown
            if (readyClients.size() == clients.size() && !gameStarted) {
                System.out.println("All players are ready. Starting the countdown.");
                startCountdown();
            }
        }
    }

    // Method to start the countdown before the game starts
    public void startCountdown() {
        Timer countdownTimer = new Timer();
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            int countdown = 3;

            @Override
            public void run() {
                if (countdown > 0) {
                    String message = "Game starts in: " + countdown + "...";
                    Payload payload = new Payload("Server", message, PayloadType.NOTIFICATION);
                    for (ClientData client : clients) {
                        client.getServerThread().sendPayload(payload);
                    }
                    countdown--;
                } else {
                    System.out.println("Countdown complete. Starting the game.");
                    countdownTimer.cancel();
                    gameStarted = true; // Set game status to started

                    // Notify all clients that the game has started
                    Payload startGamePayload = new Payload("Server", "Game has started!", PayloadType.START_GAME);
                    for (ClientData client : clients) {
                        client.getServerThread().sendPayload(startGamePayload);
                    }

                    startFirstRound();
                }
            }
        }, 0, 1000); // 1 second interval
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
            if (client.isAway()) {
                // Skip their turn and notify others
                String message = client.getName() + " is away and their turn is skipped.";
                Payload skipTurnPayload = new Payload("Server", message, PayloadType.NOTIFICATION);
                client.getServerThread().sendPayload(skipTurnPayload);
            }
            else {
                client.getServerThread().sendPayload(payload);
            }
        }
        System.out.println("Broadcasting question to clients: " + question.getQuestionText()); // Debug message
    }

    // Method to start the round timer for each question
    private void startRoundTimer() {
        long roundDuration = 30000; // 30 seconds per question
        long updateInterval = 1000; // Update every second

        roundTimer.scheduleAtFixedRate(new TimerTask() {
            long timeRemaining = roundDuration;

            @Override
            public void run() {
                if (timeRemaining > 0) {
                    // Notify clients about time remaining
                    TimePayload timePayload = new TimePayload("Server", "Time Update", PayloadType.TIME, timeRemaining);
                    for (ClientData client : clients) {
                        client.getServerThread().sendPayload(timePayload);
                    }
                    timeRemaining -= updateInterval;
                } else {
                    System.out.println("Round timer expired.");
                    endRound();
                    cancel();
                }
            }
        }, 0, updateInterval);
    }

    // Method to notify all players when a player locks in an answer
    private void notifyPlayersAnswerLocked(ClientData player) {
        String message = player.getName() + " has locked in an answer.";
        Payload payload = new Payload("Server", message, PayloadType.NOTIFICATION);
        for (ClientData client : clients) {
            client.getServerThread().sendPayload(payload);
        }
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
                    //sendnotification to all clients
                   
                    for (ClientData c : clients) {
                        String notificationMessage = client.getName() + " answered correctly and earned " + pointsAwarded + " points.";
                        Payload notification = new Payload("Server", notificationMessage, PayloadType.NOTIFICATION);
                        c.getServerThread().sendPayload(notification);
                    }
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
        if (responseTime <= 5000) { // If answered within 5 seconds
            return 20; // Fast response, higher points
        } else if (responseTime <= 15000) { // If answered within 15 seconds
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
            PointsPayload pointsPayload = new PointsPayload(client.getName(), "Points Update", PayloadType.POINTS, client.getPoints());
            for (ClientData c : clients) {
                c.getServerThread().sendPayload(pointsPayload);
            }
        }
    }

    // Method to end the session
    private void endSession() {
        System.out.println("Game session ended.");
        // Send final scoreboard to all clients
        for (ClientData client : clients) {
            PointsPayload pointsPayload = new PointsPayload(client.getName(), "Final Score", PayloadType.POINTS, client.getPoints());
            client.getServerThread().sendPayload(pointsPayload);
        }
        resetGame();
        shiftToReadyPhase();
        //send payload to all clients to come back to ready phase
        Payload readyPayload = new Payload("Server", "Game is ready for a new session", PayloadType.READY);
        for (ClientData client : clients) {
            client.getServerThread().sendPayload(readyPayload);
        }


    }

    // Method to reset the game
    private void resetGame() {
        currentRound = 0;
        questionList.clear(); // Clear previous questions
        loadQuestions("Project/questions.txt"); // Reload questions for the next session
        System.out.println("Game reset. Ready for a new session.");
        //make payload to send to all clients to reset the clients points
        for (ClientData client : clients) {
            Payload pointsPayload = new Payload(client.getName(), "Reset Points", PayloadType.RESET_POINTS);
            client.getServerThread().sendPayload(pointsPayload);
        }
        gameStarted = false; // Reset game status
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

    // Method to check if the game has started
    public boolean isGameStarted() {
        return gameStarted;
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

    //method to add spector to the roo
    public void addSpectator(ClientData client) {
        //send notification to all clients that a spectator has joined
        Payload notification = new Payload("Server", client.getName() + " joined the room as a spectator: " + roomName, PayloadType.NOTIFICATION);
        for (ClientData c : clients) {
            c.getServerThread().sendPayload(notification);
        }
        System.out.println(client.getName() + " joined the room as a spectator: " + roomName);
        addClient(client);
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
            PointsPayload pointsPayload = new PointsPayload(client.getName(), "Points Update", PayloadType.POINTS, client.getPoints());
            client.getServerThread().sendPayload(pointsPayload);
        }
    }
    public void markClientAway(ClientData client, boolean isAway) {
        client.setAway(isAway);
        String message = client.getName() + " is " + (isAway ? "away" : "no longer away");
        Payload awayStatusPayload = new Payload("Server", message, PayloadType.NOTIFICATION);
        
        // Send notification to all clients
        for (ClientData c : clients) {
            c.getServerThread().sendPayload(awayStatusPayload);
        }
    }

    // Placeholder method to calculate remaining time for the current round
    private long calculateRemainingTime() {
        return 20000; // Example: 20 seconds remaining
    }
}
