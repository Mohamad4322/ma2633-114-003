package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameUI {

    private static GameUI instance; // Static reference for accessing GameUI instance

    private JFrame frame;
    private JPanel connectionPanel;
    private JPanel roomPanel; // New panel for room creation/joining
    private JPanel readyCheckPanel;
    private JPanel gameAreaPanel;
    private JPanel userListPanel;
    private JPanel gameEventPanel;

    private JTextField usernameField;
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;

    private JTextField roomNameField; // Room name input field
    private JButton createRoomButton;
    private JButton joinRoomButton;

    private JButton readyButton;
    private JLabel readyStatusLabel; // Label to show waiting status or countdown

    private JLabel questionLabel;
    private JButton[] answerButtons;
    private JLabel timerLabel;

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

        frame.add(connectionPanel, "Connection");
        frame.add(roomPanel, "Room"); // Add room panel
        frame.add(readyCheckPanel, "ReadyCheck");
        frame.add(gameAreaPanel, "GameArea");

        frame.setVisible(true);
        showPanel("Connection");

        // Initialize Client and pass GameUI reference
        client = new Client(host, port, this);
    }

    // Method to create the connection panel
    private void createConnectionPanel() {
        connectionPanel = new JPanel();
        connectionPanel.setLayout(new GridLayout(4, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel hostLabel = new JLabel("Host:");
        hostField = new JTextField("localhost");

        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField("12345");

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());

                client.setClientId(username);
                client.connect();
                showPanel("Room"); // Show the room panel after connecting
            }
        });

        connectionPanel.add(usernameLabel);
        connectionPanel.add(usernameField);
        connectionPanel.add(hostLabel);
        connectionPanel.add(hostField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portField);
        connectionPanel.add(connectButton);
    }

    // Method to create the room panel for creating or joining rooms
    private void createRoomPanel() {
        roomPanel = new JPanel();
        roomPanel.setLayout(new GridLayout(3, 2));

        JLabel roomNameLabel = new JLabel("Room Name:");
        roomNameField = new JTextField();

        createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomNameField.getText();
                if (!roomName.isEmpty()) {
                    client.createRoom(roomName);
                    showPanel("ReadyCheck");
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a room name.");
                }
            }
        });

        joinRoomButton = new JButton("Join Room");
        joinRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomNameField.getText();
                if (!roomName.isEmpty()) {
                    client.joinRoom(roomName);
                    showPanel("ReadyCheck");
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a room name.");
                }
            }
        });

        roomPanel.add(roomNameLabel);
        roomPanel.add(roomNameField);
        roomPanel.add(createRoomButton);
        roomPanel.add(joinRoomButton);
    }

    // Method to create the ready check panel
    private void createReadyCheckPanel() {
        readyCheckPanel = new JPanel();
        readyCheckPanel.setLayout(new FlowLayout());

        readyButton = new JButton("Ready");
        readyStatusLabel = new JLabel("Waiting for other players...");

        readyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.markReady();
                readyButton.setEnabled(false); // Disable the button after marking as ready
                readyStatusLabel.setText("Waiting for all players to be ready...");
            }
        });

        readyCheckPanel.add(new JLabel("Press Ready to begin the game"));
        readyCheckPanel.add(readyButton);
        readyCheckPanel.add(readyStatusLabel);
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

        // Game Events Panel
        gameEventPanel = new JPanel();
        gameEventPanel.setLayout(new BoxLayout(gameEventPanel, BoxLayout.Y_AXIS));
        gameEventPanel.setBorder(BorderFactory.createTitledBorder("Game Events"));

        // Question Area
        JPanel questionArea = new JPanel();
        questionArea.setLayout(new GridLayout(3, 1));
        questionLabel = new JLabel("Current Question: ");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Make the question more prominent
        questionArea.add(questionLabel);

        // Answer Buttons
        JPanel answerPanel = new JPanel();
        answerPanel.setLayout(new GridLayout(2, 2, 10, 10)); // Make it look better with spacing
        answerButtons = new JButton[4];
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i] = new JButton("Answer " + (char) ('A' + i));
            int answerIndex = i;  // Required for lambda expression to access the correct button index
            answerButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    lockInAnswer(answerIndex);
                }
            });
            answerPanel.add(answerButtons[i]);
        }
        questionArea.add(answerPanel);

        // Timer label
        timerLabel = new JLabel("Time Remaining: ");
        questionArea.add(timerLabel);

        gameAreaPanel.add(userListPanel, BorderLayout.WEST);
        gameAreaPanel.add(gameEventPanel, BorderLayout.EAST);
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

    public static void updatePlayerPoints(String clientId, int points) {
        SwingUtilities.invokeLater(() -> {
            GameUI uiInstance = getUIInstance();
            if (uiInstance != null) {
                JLabel playerLabel = new JLabel(clientId + ": " + points + " points");
                uiInstance.userListPanel.add(playerLabel);
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
