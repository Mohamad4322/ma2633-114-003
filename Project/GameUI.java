package Project;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class GameUI {

    private static GameUI instance; // Static reference for accessing GameUI instance

    private JFrame frame;
    private JPanel connectionPanel;
    private JPanel roomPanel; // New panel for room creation/joining
    private JPanel readyCheckPanel;
    private JPanel gameAreaPanel;
    private JPanel userListPanel;
    private JPanel gameEventPanel;
    private JPanel questionManagementPanel;

    private JTextField usernameField;
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;

    private JTextField roomNameField; // Room name input field
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private JButton joinAsSpectatorButton;

    private JButton readyButton;
    private JLabel readyStatusLabel; // Label to show waiting status or countdown

    private JLabel questionLabel;
    private JButton[] answerButtons;
    private JLabel timerLabel;
    private JList<String> categoryList; // List to hold category options

    private Client client;

    public GameUI(String host, int port) {
        instance = this; // Set the static reference to the current instance

        frame = new JFrame("Trivia Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new CardLayout());

        createConnectionPanel();
        createRoomPanel(); // Create room panel
        createReadyCheckPanel();
        createGameAreaPanel();
        createQuestionManagementPanel();

        frame.add(connectionPanel, "Connection");
        frame.add(roomPanel, "Room"); // Add room panel
        frame.add(readyCheckPanel, "ReadyCheck");
        frame.add(gameAreaPanel, "GameArea");
        frame.add(questionManagementPanel, "questionManagementPanel");

        frame.setVisible(true);
        showPanel("Connection");

        // Initialize Client and pass GameUI reference
        client = new Client(this);
    }

    // Method to create the connection panel
    private void createConnectionPanel() {
        connectionPanel = new JPanel();
        connectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add spacing between components
    
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15); // Set a preferred column size
    
        JLabel hostLabel = new JLabel("Host:");
        hostField = new JTextField("localhost", 15); // Set a preferred column size
    
        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField("12345", 15); // Set a preferred column size
    
        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());

                client.setClientId(username);
                client.connect(host, port);
                showPanel("Room"); // Show the room panel after connecting
            }
        });
    
        // Add components to the panel with constraints
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        connectionPanel.add(usernameLabel, gbc);
    
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        connectionPanel.add(usernameField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        connectionPanel.add(hostLabel, gbc);
    
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        connectionPanel.add(hostField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        connectionPanel.add(portLabel, gbc);
    
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        connectionPanel.add(portField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        connectionPanel.add(connectButton, gbc);
    }

    // Method to create the room panel for creating or joining rooms
    private void createRoomPanel() {
        roomPanel = new JPanel();
        roomPanel.setLayout(new GridBagLayout());
        roomPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            "Create or Join a Room",
            TitledBorder.LEADING,
            TitledBorder.TOP
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing around components

        JLabel roomNameLabel = new JLabel("Room Name:");
        roomNameField = new JTextField(20); // Set a larger column size for convenience

        createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomNameField.getText().trim();
                if (!roomName.isEmpty()) {
                    client.createRoom(roomName);
                    showPanel("ReadyCheck");
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a room name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        joinRoomButton = new JButton("Join Room");
        joinRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomNameField.getText().trim();
                if (!roomName.isEmpty()) {
                    client.joinRoom(roomName);
                    showPanel("ReadyCheck");
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a room name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        joinAsSpectatorButton = new JButton("Join as Spectator");

        joinAsSpectatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomNameField.getText().trim();
                if (!roomName.isEmpty()) {
                    client.joinRoomAsSpectator(roomName);
                    showPanel("GameArea");
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a room name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });



        // Add components to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        roomPanel.add(roomNameLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        roomPanel.add(roomNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        roomPanel.add(createRoomButton, gbc);

        gbc.gridy = 2;
        roomPanel.add(joinRoomButton, gbc);

        gbc.gridy = 3;
        roomPanel.add(joinAsSpectatorButton, gbc);
    }
    

    // Method to create the ready check panel
    private void createReadyCheckPanel() {
        readyCheckPanel = new JPanel();
        readyCheckPanel.setLayout(new GridBagLayout());
        readyCheckPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
            "Ready Check",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            Color.BLUE
        ));
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
    
        // Instructions Label
        JLabel instructionsLabel = new JLabel("Select categories and press Ready.");
        instructionsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        readyCheckPanel.add(instructionsLabel, gbc);
    
        // Category Selection Panel
        JLabel categoryLabel = new JLabel("Select Categories:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        readyCheckPanel.add(categoryLabel, gbc);
    
        categoryList = new JList<>(new String[]{"Science", "History", "Geography", "Sports", "Movies"});
        categoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        categoryList.setVisibleRowCount(5);
        categoryList.setPreferredSize(new Dimension(200, 100));
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        gbc.gridx = 1;
        readyCheckPanel.add(categoryScrollPane, gbc);
    
        // Ready Button
        readyButton = new JButton("Ready");
        readyButton.setFont(new Font("Arial", Font.BOLD, 14));
        readyButton.setBackground(new Color(50, 205, 50));
        readyButton.setForeground(Color.WHITE);
        readyButton.setFocusPainted(false);
        readyButton.setPreferredSize(new Dimension(100, 40));
        readyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.setSelectedCategories(categoryList.getSelectedValuesList());
                client.markReady();
                readyButton.setEnabled(false);
                readyStatusLabel.setText("Waiting for all players to be ready...");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        readyCheckPanel.add(readyButton, gbc);
    
        // Status Label
        readyStatusLabel = new JLabel("Waiting for other players...");
        readyStatusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        readyStatusLabel.setForeground(Color.GRAY);
        gbc.gridy = 3;

        readyCheckPanel.add(readyStatusLabel, gbc);
        //add button to add question
        JButton addQuestionButton = new JButton("Add Question");
        addQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("questionManagementPanel");
            }
        });
        gbc.gridy = 4;
        readyCheckPanel.add(addQuestionButton, gbc);
    }
    // Method to create the Question Management Panel
    private void createQuestionManagementPanel() {
        questionManagementPanel = new JPanel();
        questionManagementPanel.setLayout(new GridBagLayout());
        questionManagementPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            "Add New Question",
            TitledBorder.LEADING,
            TitledBorder.TOP
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Components for Question Input
        JLabel questionLabel = new JLabel("Question:");
        JTextField questionField = new JTextField(20);

        JLabel categoryLabel = new JLabel("Category:");
        JComboBox<String> categoryDropdown = new JComboBox<>(new String[]{"Science", "History", "Geography", "Sports", "Movies"});

        JLabel answersLabel = new JLabel("Answers:");
        JTextField[] answerFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            answerFields[i] = new JTextField(20);
        }

        JLabel correctAnswerLabel = new JLabel("Correct Answer (1-4):");
        JSpinner correctAnswerSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));

        JButton saveQuestionButton = new JButton("Save Question");
        saveQuestionButton.addActionListener(e -> {
            String questionText = questionField.getText().trim();
            String category = (String) categoryDropdown.getSelectedItem();
            String[] answers = new String[4];
            boolean valid = true;

            for (int i = 0; i < 4; i++) {
                answers[i] = answerFields[i].getText().trim();
                if (answers[i].isEmpty() && i < 2) {
                    valid = false;
                }
            }

            if (questionText.isEmpty() || !valid) {
                JOptionPane.showMessageDialog(frame, "Please fill out the question and at least two answers.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int correctAnswerIndex = (int) correctAnswerSpinner.getValue() - 1;

            // client.addQuestion(questionText, category, answers, correctAnswerIndex);
            questionField.setText("");
            for (JTextField answerField : answerFields) {
                answerField.setText("");
            }
            correctAnswerSpinner.setValue(1);

            JOptionPane.showMessageDialog(frame, "Question added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            //show ready check panel after adding question
            showPanel("ReadyCheck");
        });

        // Add components to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        questionManagementPanel.add(questionLabel, gbc);

        gbc.gridx = 1;
        questionManagementPanel.add(questionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        questionManagementPanel.add(categoryLabel, gbc);

        gbc.gridx = 1;
        questionManagementPanel.add(categoryDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        questionManagementPanel.add(answersLabel, gbc);

        for (int i = 0; i < 4; i++) {
            gbc.gridy = 3 + i;
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            questionManagementPanel.add(new JLabel("Answer " + (i + 1) + ":"), gbc);

            gbc.gridx = 1;
            questionManagementPanel.add(answerFields[i], gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = 7;
        questionManagementPanel.add(correctAnswerLabel, gbc);

        gbc.gridx = 1;
        questionManagementPanel.add(correctAnswerSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        questionManagementPanel.add(saveQuestionButton, gbc);
    }

    // Method to trigger countdown after all players are ready
    public void startCountdown() {
        SwingUtilities.invokeLater(() -> {
            Timer countdownTimer = new Timer(1000, new ActionListener() {
                int countdown = 3;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (countdown > 0) {
                        readyStatusLabel.setText("Game starts in: " + countdown + "...");
                        countdown--;
                    } else {
                        ((Timer) e.getSource()).stop();
                        // Once countdown finishes, switch to the game area
                        showPanel("GameArea");
                        readyStatusLabel.setText("Game Started!");
                    }
                }
            });
            countdownTimer.start();
        });
    }

    // Method to create the game area panel
    private void createGameAreaPanel() {
        gameAreaPanel = new JPanel();
        gameAreaPanel.setLayout(new BorderLayout());
    
        // User List Panel
        userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.setBorder(BorderFactory.createTitledBorder("Users"));
        JScrollPane userListScroll = new JScrollPane(userListPanel);
        userListScroll.setPreferredSize(new Dimension(200, 400));
    
        // Game Events Panel
        gameEventPanel = new JPanel();
        gameEventPanel.setLayout(new BoxLayout(gameEventPanel, BoxLayout.Y_AXIS));
        gameEventPanel.setBorder(BorderFactory.createTitledBorder("Game Events"));
        JScrollPane gameEventScroll = new JScrollPane(gameEventPanel);
        gameEventScroll.setPreferredSize(new Dimension(200, 400));
    
        // Question Area
        JPanel questionArea = new JPanel();
        questionArea.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //button to away from the game
        JButton awayButton = new JButton("Away");
        awayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 if (awayButton.getText().equals("Away")) {
                    awayButton.setText("Back");
                    client.sendAwayStatus(true);
                } else {
                    awayButton.setText("Away");
                    //send away status to server
                    client.sendAwayStatus(false);
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        questionArea.add(awayButton, gbc);

        // Question Category Label
        JLabel questionCategoryLabel = new JLabel("Category: ");
        questionCategoryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        questionArea.add(questionCategoryLabel, gbc);
    
        // Current Question Label
        questionLabel = new JLabel("Current Question: ");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = 1;
        questionArea.add(questionLabel, gbc);
    
        // Answer Buttons Panel
        JPanel answerPanel = new JPanel();
        answerPanel.setLayout(new GridLayout(2, 2, 10, 10));
        answerButtons = new JButton[4];
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i] = new JButton("Answer " + (char) ('A' + i));
            int answerIndex = i;
            answerButtons[i].addActionListener(e -> lockInAnswer(answerIndex));
            answerPanel.add(answerButtons[i]);
        }
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        questionArea.add(answerPanel, gbc);
    
        // Timer Label
        timerLabel = new JLabel("Time Remaining: ");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 3;
        questionArea.add(timerLabel, gbc);
    
        // Add Panels to Game Area
        gameAreaPanel.add(userListScroll, BorderLayout.WEST);
        gameAreaPanel.add(gameEventScroll, BorderLayout.EAST);
        gameAreaPanel.add(questionArea, BorderLayout.CENTER);
    }

    // Method to lock in an answer
    private void lockInAnswer(int answerIndex) {
        client.sendAnswer(String.valueOf((char) ('A' + answerIndex)));
        for (JButton button : answerButtons) {
            button.setEnabled(false); // Disable buttons after selection
        }
        answerButtons[answerIndex].setBackground(Color.GREEN); // Indicate selected answer
    }

    // Method to switch between different panels
    public void showPanel(String panelName) {
        CardLayout cl = (CardLayout) (frame.getContentPane().getLayout());
        cl.show(frame.getContentPane(), panelName);
    }
    public void updateToReadyCheckPanel() {
        //clear the user list panel before updating and show the ready check panel and reset the ready button
        SwingUtilities.invokeLater(() -> {
            userListPanel.removeAll();
            readyButton.setEnabled(true);
            readyStatusLabel.setText("Waiting for other players...");
            showPanel("ReadyCheck");
        });
    }

    public static void showFinalScore(Map<String, Integer> playerPoints) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder finalScore = new StringBuilder("Final Scores:\n");
            for (Map.Entry<String, Integer> entry : playerPoints.entrySet()) {
                finalScore.append(entry.getKey()).append(": ").append(entry.getValue()).append(" points\n");
            }
            JOptionPane.showMessageDialog(getUIInstance().frame, finalScore.toString(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    // Static methods to update the UI from the client
    public static void updateQuestion(String question, String[] options) {
        SwingUtilities.invokeLater(() -> {
            GameUI uiInstance = getUIInstance();
            if (uiInstance != null) {
                uiInstance.questionLabel.setText(question);
                for (int i = 0; i < options.length; i++) {
                    uiInstance.answerButtons[i].setText(options[i]);
                    uiInstance.answerButtons[i].setEnabled(true);
                    uiInstance.answerButtons[i].setBackground(null); // Reset button background
                }
            }
        });
    }

    public static void updatePlayerPoints(Map<String, Integer> playerPoints) {
        SwingUtilities.invokeLater(() -> {
            GameUI uiInstance = getUIInstance();
            if (uiInstance != null) {
                // Clear the user list panel before updating
                uiInstance.userListPanel.removeAll();
                for (Map.Entry<String, Integer> entry : playerPoints.entrySet()) {
                    JLabel userLabel = new JLabel(entry.getKey() + ": " + entry.getValue() + " points");
                    uiInstance.userListPanel.add(userLabel);
                }
                uiInstance.userListPanel.revalidate();
                uiInstance.userListPanel.repaint();

            }
        });
    }

    // Method to update the timer in the UI
    public static void updateTimer(int timeRemaining) {
        SwingUtilities.invokeLater(() -> {
            GameUI uiInstance = getUIInstance();
            if (uiInstance != null) {
                uiInstance.timerLabel.setText("Time Remaining: " + timeRemaining + " seconds");
            }
        });
    }

    public static void displayNotification(String message) {
        SwingUtilities.invokeLater(() -> {
            GameUI uiInstance = getUIInstance();
            if (uiInstance != null) {
                JLabel notificationLabel = new JLabel(message);
                uiInstance.gameEventPanel.add(notificationLabel);
                uiInstance.gameEventPanel.revalidate();
                uiInstance.gameEventPanel.repaint();
            }
        });
    }

    // Method to reset player points display
    public static void resetPlayerPoints() {
        SwingUtilities.invokeLater(() -> {
            GameUI uiInstance = getUIInstance();
            if (uiInstance != null) {
                uiInstance.userListPanel.removeAll();
                uiInstance.userListPanel.revalidate();
                uiInstance.userListPanel.repaint();
            }
        });
    }
    // Method to get the static GameUI instance
    private static GameUI getUIInstance() {
        return instance;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameUI("localhost", 12345));
    }
}
