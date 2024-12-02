package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerUI {
    private Server server;
    private JFrame frame;
    private DefaultListModel<String> clientListModel;
    private JLabel statsLabel;

    public ServerUI(Server server) {
        this.server = server;
        createUI();
        startServerInBackground(); // Start the server when the UI is created
    }

    private void createUI() {
        frame = new JFrame("Server UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Client list panel
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBorder(BorderFactory.createTitledBorder("Connected Clients"));
        clientListModel = new DefaultListModel<>();
        JList<String> clientList = new JList<>(clientListModel);
        clientPanel.add(new JScrollPane(clientList), BorderLayout.CENTER);

        // Stats panel
        statsLabel = new JLabel();
        updateStats(); // Initial stats update
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Server Stats"));
        statsPanel.add(statsLabel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel();
        JButton stopServerButton = new JButton("Stop Server");
        stopServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });
        controlPanel.add(stopServerButton);

        // Add panels to frame
        frame.add(clientPanel, BorderLayout.CENTER);
        frame.add(statsPanel, BorderLayout.SOUTH);
        frame.add(controlPanel, BorderLayout.NORTH);

        // Periodically update client list and stats
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClientList();
                updateStats();
            }
        });
        timer.start();

        frame.setVisible(true);
    }

    private void updateClientList() {
        clientListModel.clear();
        for (ServerThread client : server.getServerThreads()) {
            clientListModel.addElement(client.getClientData().getName());
        }
    }

    private void updateStats() {
        long uptime = System.currentTimeMillis() - server.getStartTime();
        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        statsLabel.setText(String.format("<html>Uptime: %d ms<br>Memory Usage: %d KB</html>", uptime, memoryUsage / 1024));
    }

    private void stopServer() {
        server.stop();
        frame.dispose();
    }
    private void startServerInBackground() {
        Thread serverThread = new Thread(() -> server.start());
        serverThread.setDaemon(true); // Ensures the server thread stops when the UI is closed
        serverThread.start();
    }
    public static void launch(Server server) {
        // server.start();
        SwingUtilities.invokeLater(() -> new ServerUI(server));

    }
}
