package ua.millfreedom.rom2.dserver;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DedicatedServerControlDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Java Swing operator console for the no-GL dedicated-server launcher.
 * not ported.
 */
public final class DedicatedServerSwingConsole implements AutoCloseable {
    private final CMainWindow mainWindow;
    private final AutoCloseable logSinkHandle;
    private final JFrame frame;
    private final JLabel statusLabel = new JLabel("Starting dedicated server");
    private final DefaultTableModel playersModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "State", "IP", "Online", "B/s", "Avg", "Max", "Kills", "PK", "Frags", "Deaths"},
            0
    ) {
        /**
         * Java support table-cell edit guard for the Swing dedicated-server UI.
         * not ported.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable playersTable = new JTable(playersModel);
    private final JTextArea detailsText = new JTextArea();
    private final JTextArea logText = new JTextArea();
    private final JTextField commandField = new JTextField();
    private final JCheckBox keepSavedCharacters = new JCheckBox("Keep saved characters on server");
    private final Map<Integer, Integer> tableRowToPlayerId = new HashMap<>();
    private volatile int selectedPlayerId;

    /**
     * Java support constructor for the Swing dedicated-server operator console.
     * not ported.
     */
    public DedicatedServerSwingConsole(CMainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.logSinkHandle = DedicatedServerConsoleSink.addSink(this::appendLogLine);
        this.frame = buildFrame();
    }

    /**
     * Java support factory for the Swing dedicated-server operator window.
     * not ported.
     */
    public static DedicatedServerSwingConsole createAndShow(CMainWindow mainWindow) {
        DedicatedServerSwingConsole console = new DedicatedServerSwingConsole(mainWindow);
        SwingUtilities.invokeLater(console::show);
        return console;
    }

    /**
     * Java support frame display boundary for the Swing dedicated-server operator UI.
     * not ported.
     */
    private void show() {
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    /**
     * Java support status updater called by the dedicated server loop after it snapshots game state.
     * not ported.
     */
    public void update(DedicatedServerStatusSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> applySnapshot(snapshot));
    }

    /**
     * Java support selected-player accessor for game-thread status snapshotting.
     * not ported.
     */
    public int selectedPlayerId() {
        return selectedPlayerId;
    }

    /**
     * Java support close boundary for the Swing dedicated-server operator UI.
     * not ported.
     */
    @Override
    public void close() {
        try {
            logSinkHandle.close();
        } catch (Exception ignored) {
            // AutoCloseable forces a checked exception; sink removal itself is in-memory only.
        }
        SwingUtilities.invokeLater(frame::dispose);
    }

    /**
     * Java support frame construction for the Swing dedicated-server operator UI.
     * not ported.
     */
    private JFrame buildFrame() {
        JFrame builtFrame = new JFrame("Rage of Mages 2 Dedicated Server");
        builtFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        builtFrame.addWindowListener(new WindowAdapter() {
            /**
             * Java support window-close command mapping to CMainWindow::OnClose @00492235.
             */
            @Override
            public void windowClosing(WindowEvent event) {
                mainWindow.postCloseCommand();
            }
        });

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(statusLabel, BorderLayout.NORTH);
        root.add(buildMainSplitPane(), BorderLayout.CENTER);
        root.add(buildCommandPanel(), BorderLayout.SOUTH);
        builtFrame.setContentPane(root);
        builtFrame.setMinimumSize(new Dimension(920, 620));
        builtFrame.pack();
        return builtFrame;
    }

    /**
     * Java support layout construction for the Swing dedicated-server operator UI.
     * not ported.
     */
    private JSplitPane buildMainSplitPane() {
        playersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playersTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int row = playersTable.getSelectedRow();
                if (row >= 0) {
                    Integer playerId = tableRowToPlayerId.get(playersTable.convertRowIndexToModel(row));
                    if (playerId != null) {
                        selectedPlayerId = playerId;
                    }
                }
            }
        });

        detailsText.setEditable(false);
        detailsText.setLineWrap(true);
        detailsText.setWrapStyleWord(true);
        logText.setEditable(false);

        JPanel playerPanel = new JPanel(new BorderLayout(6, 6));
        playerPanel.add(new JScrollPane(playersTable), BorderLayout.CENTER);
        playerPanel.add(new JScrollPane(detailsText), BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, playerPanel, new JScrollPane(logText));
        splitPane.setResizeWeight(0.58);
        splitPane.setBorder(null);
        return splitPane;
    }

    /**
     * Java support command/control construction for the Swing dedicated-server operator UI.
     * not ported.
     */
    private JPanel buildCommandPanel() {
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(event -> submitCommand());

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(event -> mainWindow.postCloseCommand());

        JButton changeMapButton = new JButton("Change Map");
        changeMapButton.addActionListener(event -> mainWindow.postMessage(MessageCodes.EXIT_MAP, 0, 0));

        JButton kickButton = new JButton("Kick");
        kickButton.addActionListener(event -> {
            if (selectedPlayerId != 0) {
                DedicatedServerControlDialog.kickPlayerById(selectedPlayerId);
            }
        });

        JButton slowerButton = new JButton("Slower");
        slowerButton.addActionListener(event -> DedicatedServerControlDialog.decreaseGameSpeedCommand());

        JButton fasterButton = new JButton("Faster");
        fasterButton.addActionListener(event -> DedicatedServerControlDialog.increaseGameSpeedCommand());

        keepSavedCharacters.addActionListener(event -> {
            if (Globals.gameServer != null) {
                Globals.gameServer.keepSavedCharactersOnServer = keepSavedCharacters.isSelected() ? 1 : 0;
            }
        });

        commandField.addActionListener(event -> submitCommand());

        JPanel commandRow = new JPanel(new BorderLayout(6, 6));
        commandRow.add(commandField, BorderLayout.CENTER);
        commandRow.add(sendButton, BorderLayout.EAST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        controls.add(stopButton);
        controls.add(changeMapButton);
        controls.add(kickButton);
        controls.add(slowerButton);
        controls.add(fasterButton);
        controls.add(keepSavedCharacters);

        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 6));
        panel.add(commandRow);
        panel.add(controls);
        return panel;
    }

    /**
     * Java support command submission for the Swing dedicated-server operator UI.
     * not ported.
     */
    private void submitCommand() {
        String text = commandField.getText();
        commandField.setText("");
        DedicatedServerCommandGateway.submitConsoleText(text);
    }

    /**
     * Java support log append callback for the Swing dedicated-server operator UI.
     * not ported.
     */
    private void appendLogLine(String line) {
        SwingUtilities.invokeLater(() -> {
            logText.append(line);
            logText.append(System.lineSeparator());
            logText.setCaretPosition(logText.getDocument().getLength());
        });
    }

    /**
     * Java support EDT-side application of one dedicated-server status snapshot.
     * not ported.
     */
    private void applySnapshot(DedicatedServerStatusSnapshot snapshot) {
        statusLabel.setText(snapshot.summaryText.isBlank()
                ? (snapshot.serverRunning ? "Server running" : "Server starting")
                : snapshot.summaryText);
        keepSavedCharacters.setSelected(snapshot.keepSavedCharactersOnServer);
        detailsText.setText(snapshot.selectedPlayerDetails);
        selectedPlayerId = snapshot.selectedPlayerId;
        tableRowToPlayerId.clear();
        playersModel.setRowCount(0);
        int selectedRow = -1;
        for (DedicatedServerPlayerStatus player : snapshot.players) {
            int row = playersModel.getRowCount();
            tableRowToPlayerId.put(row, player.playerId);
            playersModel.addRow(new Object[]{
                    player.playerId,
                    player.name,
                    player.connected ? "connected" : "disconnected",
                    player.ipAddress,
                    player.onlineDuration,
                    player.lastIntervalBytes,
                    player.averageBytes,
                    player.peakIntervalBytes,
                    player.creatureKills,
                    player.playerKills,
                    player.frags,
                    player.deaths
            });
            if (player.playerId == selectedPlayerId) {
                selectedRow = row;
            }
        }
        if (selectedRow >= 0) {
            playersTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }
}
