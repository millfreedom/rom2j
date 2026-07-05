package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.CPlayer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Swing players and diplomacy editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorPlayersPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DefaultListModel<String> playerListModel = new DefaultListModel<>();
    private final JList<String> playerList = new JList<>(playerListModel);
    private final JTextField nameField = new JTextField();
    private final JSpinner colorSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));
    private final JSpinner goldSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 100));
    private final JSpinner flagsSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JComboBox<String> targetPlayerCombo = new JComboBox<>();
    private final JComboBox<MapEditorDiplomacyRelation> relationCombo =
            new JComboBox<>(MapEditorDiplomacyRelation.values());
    private final JCheckBox visionCheckBox = new JCheckBox("Vision");
    private final JButton addPlayerButton = button("Add Player", this::addPlayer);
    private final JButton deletePlayerButton = button("Delete Player", this::deletePlayer);
    private final JButton applyPlayerButton = button("Apply Player", this::applyPlayer);
    private final JButton applyDiplomacyButton = button("Apply Diplomacy", this::applyDiplomacy);
    private final List<Integer> targetPlayerIndexes = new ArrayList<>();

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor players panel.
     * not ported.
     */
    public MapEditorPlayersPanel() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        playerList.setVisibleRowCount(6);
        playerList.setPrototypeCellValue("#00 Player_Name flags 000000 gold 000000");
        playerList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncSelectionToFields();
            }
        });
        targetPlayerCombo.addActionListener(event -> syncDiplomacySelection());

        add(new JScrollPane(playerList), BorderLayout.CENTER);
        add(createEditorControls(), BorderLayout.SOUTH);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the players panel.
     * not ported.
     */
    public void setDocument(MapEditorDocument document) {
        this.document = document;
        refreshFromDocument();
    }

    /**
     * Java support refresh callback binding used by the owning frame.
     * not ported.
     */
    public void setRefreshListener(Runnable refreshListener) {
        this.refreshListener = refreshListener == null ? () -> {
        } : refreshListener;
    }

    /**
     * Java support UI refresh from the active editor document.
     * not ported.
     */
    public void refreshFromDocument() {
        int previousSelection = playerList.getSelectedIndex();
        List<String> labels = new ArrayList<>();
        refreshing = true;
        if (document != null) {
            for (int i = 0; i < document.playerCount(); i++) {
                labels.add(playerLabel(document.playerAt(i)));
            }
        }
        playerListModel.clear();
        playerListModel.addAll(labels);
        refreshing = false;

        if (playerListModel.isEmpty()) {
            playerList.clearSelection();
        } else {
            int selectedIndex = Math.min(Math.max(previousSelection, 0), playerListModel.size() - 1);
            playerList.setSelectedIndex(selectedIndex);
        }
        syncSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support aggregate editor controls construction.
     * not ported.
     */
    private JPanel createEditorControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(createPlayerAttributesPanel(), BorderLayout.NORTH);
        panel.add(createDiplomacyPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support player-attribute form construction.
     * not ported.
     */
    private JPanel createPlayerAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, "Name", nameField);
        addField(panel, 1, "Color", colorSpinner);
        addField(panel, 2, "Gold", goldSpinner);
        addField(panel, 3, "Flags", flagsSpinner);
        return MapEditorCollapsiblePanel.collapsed("Player", panel);
    }

    /**
     * Java support diplomacy form construction.
     * not ported.
     */
    private JPanel createDiplomacyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, "Target", targetPlayerCombo);
        addField(panel, 1, "Relation", relationCombo);
        addField(panel, 2, "", visionCheckBox);
        return MapEditorCollapsiblePanel.collapsed("Diplomacy", panel);
    }

    /**
     * Java support button-row construction for player commands.
     * not ported.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 2, 0, 2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(addPlayerButton, constraints);
        constraints.gridx = 1;
        panel.add(deletePlayerButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(applyPlayerButton, constraints);
        constraints.gridx = 1;
        panel.add(applyDiplomacyButton, constraints);
        return panel;
    }

    /**
     * Java support button construction helper for panel commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support helper for adding one form row.
     * not ported.
     */
    private static void addField(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(3, 0, 3, 8);
        panel.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(3, 0, 3, 0);
        panel.add(component, fieldConstraints);
    }

    /**
     * Java support selection transfer from list row to editor controls.
     * not ported.
     */
    private void syncSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = playerList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            clearFields();
            updateControlsEnabled();
            return;
        }

        CPlayer player = document.playerAt(selectedIndex);
        nameField.setText(player.name.toString());
        colorSpinner.setValue(player.color);
        goldSpinner.setValue(player.gold);
        flagsSpinner.setValue(player.flags);
        rebuildTargetPlayers(selectedIndex);
        syncDiplomacySelection();
        updateControlsEnabled();
    }

    /**
     * Java support field clearing when no document or player row is selected.
     * not ported.
     */
    private void clearFields() {
        nameField.setText("");
        colorSpinner.setValue(0);
        goldSpinner.setValue(0);
        flagsSpinner.setValue(0);
        targetPlayerIndexes.clear();
        targetPlayerCombo.removeAllItems();
        relationCombo.setSelectedItem(MapEditorDiplomacyRelation.NEUTRAL);
        visionCheckBox.setSelected(false);
    }

    /**
     * Java support target-player combo rebuild for the selected source player.
     * not ported.
     */
    private void rebuildTargetPlayers(int sourcePlayerIndex) {
        int previousTargetIndex = selectedTargetPlayerIndex();
        refreshing = true;
        targetPlayerIndexes.clear();
        targetPlayerCombo.removeAllItems();
        for (int i = 0; i < document.playerCount(); i++) {
            if (i != sourcePlayerIndex) {
                targetPlayerIndexes.add(i);
                targetPlayerCombo.addItem(playerLabel(document.playerAt(i)));
            }
        }
        int selectedTargetSlot = targetPlayerIndexes.indexOf(previousTargetIndex);
        if (selectedTargetSlot < 0 && !targetPlayerIndexes.isEmpty()) {
            selectedTargetSlot = 0;
        }
        if (selectedTargetSlot >= 0) {
            targetPlayerCombo.setSelectedIndex(selectedTargetSlot);
        }
        refreshing = false;
    }

    /**
     * Java support diplomacy control refresh for the selected source/target pair.
     * not ported.
     */
    private void syncDiplomacySelection() {
        if (refreshing || document == null) {
            return;
        }
        int sourcePlayerIndex = playerList.getSelectedIndex();
        int targetPlayerIndex = selectedTargetPlayerIndex();
        if (sourcePlayerIndex < 0 || targetPlayerIndex < 0) {
            relationCombo.setSelectedItem(MapEditorDiplomacyRelation.NEUTRAL);
            visionCheckBox.setSelected(false);
            updateControlsEnabled();
            return;
        }
        int flags = document.diplomacyFlags(sourcePlayerIndex, targetPlayerIndex);
        relationCombo.setSelectedItem(MapEditorDiplomacyRelation.fromFlags(flags));
        visionCheckBox.setSelected((flags & CPlayer.DIPLOMACY_VISIBLE_MASK) != 0);
        updateControlsEnabled();
    }

    /**
     * Java support selected target-player lookup.
     * not ported.
     */
    private int selectedTargetPlayerIndex() {
        int selectedTargetSlot = targetPlayerCombo.getSelectedIndex();
        if (selectedTargetSlot < 0 || selectedTargetSlot >= targetPlayerIndexes.size()) {
            return -1;
        }
        return targetPlayerIndexes.get(selectedTargetSlot);
    }

    /**
     * Java support command for appending one scenario player.
     * not ported.
     */
    private void addPlayer() {
        if (document == null) {
            return;
        }
        try {
            document.addPlayer();
            refreshFromDocument();
            playerList.setSelectedIndex(document.playerCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add player failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario player.
     * not ported.
     */
    private void deletePlayer() {
        int selectedIndex = playerList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deletePlayer(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete player failed", exception);
        }
    }

    /**
     * Java support command for applying selected player attributes.
     * not ported.
     */
    private void applyPlayer() {
        int selectedIndex = playerList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updatePlayer(
                    selectedIndex,
                    nameField.getText(),
                    spinnerInt(colorSpinner),
                    spinnerInt(goldSpinner),
                    spinnerInt(flagsSpinner)
            );
            refreshFromDocument();
            playerList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply player failed", exception);
        }
    }

    /**
     * Java support command for applying selected diplomacy relation settings.
     * not ported.
     */
    private void applyDiplomacy() {
        int sourcePlayerIndex = playerList.getSelectedIndex();
        int targetPlayerIndex = selectedTargetPlayerIndex();
        if (document == null || sourcePlayerIndex < 0 || targetPlayerIndex < 0) {
            return;
        }
        try {
            document.setDiplomacy(
                    sourcePlayerIndex,
                    targetPlayerIndex,
                    (MapEditorDiplomacyRelation) relationCombo.getSelectedItem(),
                    visionCheckBox.isSelected()
            );
            syncDiplomacySelection();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply diplomacy failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to selected players.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasPlayerSelection = hasDocument && playerList.getSelectedIndex() >= 0;
        boolean hasTargetSelection = hasPlayerSelection && selectedTargetPlayerIndex() >= 0;
        addPlayerButton.setEnabled(hasDocument && document.playerCount() < MapEditorDocument.MAX_PLAYERS);
        deletePlayerButton.setEnabled(hasDocument && document.playerCount() > 1 && hasPlayerSelection);
        applyPlayerButton.setEnabled(hasPlayerSelection);
        applyDiplomacyButton.setEnabled(hasTargetSelection);
        nameField.setEnabled(hasPlayerSelection);
        colorSpinner.setEnabled(hasPlayerSelection);
        goldSpinner.setEnabled(hasPlayerSelection);
        flagsSpinner.setEnabled(hasPlayerSelection);
        targetPlayerCombo.setEnabled(hasTargetSelection);
        relationCombo.setEnabled(hasTargetSelection);
        visionCheckBox.setEnabled(hasTargetSelection);
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support list label for scenario-player rows.
     * not ported.
     */
    private static String playerLabel(CPlayer player) {
        return "P" + player.playerId + "  " + player.name + "  color " + player.color;
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support error dialog for player-panel commands.
     * not ported.
     */
    private void showError(String title, Exception exception) {
        JOptionPane.showMessageDialog(
                this,
                exception.getMessage(),
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
}
