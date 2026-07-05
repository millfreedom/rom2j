package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.enums.BuildingId;
import ua.millfreedom.rom2.model.world.scenario.BuildingDTO;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Swing buildings editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorBuildingsPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DefaultListModel<String> buildingListModel = new DefaultListModel<>();
    private final JList<String> buildingList = new JList<>(buildingListModel);
    private final JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner typeSpinner = new JSpinner(new SpinnerNumberModel(BuildingId.GOBLIN_HUT.id, 0, 0xFFFF, 1));
    private final JSpinner hpSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner playerSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 0xFFFF, 1));
    private final JSpinner buildingIdSpinner = new JSpinner(new SpinnerNumberModel(1, Short.MIN_VALUE, Short.MAX_VALUE, 1));
    private final JSpinner sizeXSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner sizeYSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JButton addBuildingButton = button("Add Building", this::addBuilding);
    private final JButton deleteBuildingButton = button("Delete Building", this::deleteBuilding);
    private final JButton applyBuildingButton = button("Apply Building", this::applyBuilding);

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor buildings panel.
     * not ported.
     */
    public MapEditorBuildingsPanel() {
        super(new BorderLayout(6, 6));
        buildingList.setVisibleRowCount(8);
        buildingList.setPrototypeCellValue("#00000 Building_Name.99 @ 000,000 p00 HP 00000 size 000x000");
        buildingList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncSelectionToFields();
                notifyBuildingSelection();
            }
        });

        add(new JScrollPane(buildingList), BorderLayout.CENTER);
        add(createEditorControls(), BorderLayout.SOUTH);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the buildings panel.
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
     * Java support selection callback binding used by viewport and minimap selection state.
     * not ported.
     */
    void setSelectionListener(Consumer<MapEditorEntitySelection> selectionListener) {
        this.selectionListener = selectionListener == null ? selection -> {
        } : selectionListener;
    }

    /**
     * Java support UI refresh from the active editor document.
     * not ported.
     */
    public void refreshFromDocument() {
        int previousSelection = buildingList.getSelectedIndex();
        List<String> labels = new ArrayList<>();
        refreshing = true;
        if (document != null) {
            for (int i = 0; i < document.buildingCount(); i++) {
                labels.add(buildingLabel(document.buildingAt(i)));
            }
        }
        buildingListModel.clear();
        buildingListModel.addAll(labels);

        if (buildingListModel.isEmpty()) {
            buildingList.clearSelection();
        } else {
            int selectedIndex = Math.min(Math.max(previousSelection, 0), buildingListModel.size() - 1);
            buildingList.setSelectedIndex(selectedIndex);
        }
        refreshing = false;
        syncSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selection hook used by viewport entity hit-testing.
     * not ported.
     */
    void selectBuildingIndex(int buildingIndex) {
        if (buildingIndex < 0 || buildingIndex >= buildingListModel.size()) {
            return;
        }
        buildingList.setSelectedIndex(buildingIndex);
        buildingList.ensureIndexIsVisible(buildingIndex);
    }

    /**
     * Java support aggregate editor controls construction.
     * not ported.
     */
    private JPanel createEditorControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(createBuildingAttributesPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support building-attribute form construction.
     * not ported.
     */
    private JPanel createBuildingAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, "X", xSpinner);
        addField(panel, 1, "Y", ySpinner);
        addField(panel, 2, "Type", typeSpinner);
        addField(panel, 3, "HP", hpSpinner);
        addField(panel, 4, "Player", playerSpinner);
        addField(panel, 5, "Id", buildingIdSpinner);
        addField(panel, 6, "Size X", sizeXSpinner);
        addField(panel, 7, "Size Y", sizeYSpinner);
        return MapEditorCollapsiblePanel.collapsed("Building", panel);
    }

    /**
     * Java support button-row construction for building commands.
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
        panel.add(addBuildingButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteBuildingButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyBuildingButton, constraints);
        return panel;
    }

    /**
     * Java support helper for adding one form row.
     * not ported.
     */
    private static void addField(JPanel panel, int row, String label, Component component) {
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
     * Java support button construction helper for building commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support selection transfer from list row to editor controls.
     * not ported.
     */
    private void syncSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = buildingList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetFieldsForNewBuilding();
            updateControlsEnabled();
            return;
        }

        BuildingDTO building = document.buildingAt(selectedIndex);
        xSpinner.setValue(building.x);
        ySpinner.setValue(building.y);
        typeSpinner.setValue(building.typeID);
        hpSpinner.setValue(building.hp);
        playerSpinner.setValue(building.playerID);
        buildingIdSpinner.setValue(building.buildingID);
        sizeXSpinner.setValue(building.sizeX);
        sizeYSpinner.setValue(building.sizeY);
        updateControlsEnabled();
    }

    /**
     * Java support default field setup for adding a new building record.
     * not ported.
     */
    private void resetFieldsForNewBuilding() {
        xSpinner.setValue(0);
        ySpinner.setValue(0);
        typeSpinner.setValue(BuildingId.GOBLIN_HUT.id);
        hpSpinner.setValue(0);
        playerSpinner.setValue(defaultPlayerId());
        buildingIdSpinner.setValue(1);
        sizeXSpinner.setValue(0);
        sizeYSpinner.setValue(0);
    }

    /**
     * Java support command for appending one scenario building.
     * not ported.
     */
    private void addBuilding() {
        if (document == null) {
            return;
        }
        try {
            document.addBuilding(
                    spinnerInt(xSpinner),
                    spinnerInt(ySpinner),
                    spinnerInt(typeSpinner),
                    spinnerInt(hpSpinner),
                    spinnerInt(playerSpinner),
                    spinnerInt(sizeXSpinner),
                    spinnerInt(sizeYSpinner)
            );
            refreshFromDocument();
            buildingList.setSelectedIndex(document.buildingCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add building failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario building.
     * not ported.
     */
    private void deleteBuilding() {
        int selectedIndex = buildingList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteBuilding(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete building failed", exception);
        }
    }

    /**
     * Java support command for applying selected building attributes.
     * not ported.
     */
    private void applyBuilding() {
        int selectedIndex = buildingList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateBuilding(
                    selectedIndex,
                    spinnerInt(xSpinner),
                    spinnerInt(ySpinner),
                    spinnerInt(typeSpinner),
                    spinnerInt(hpSpinner),
                    spinnerInt(playerSpinner),
                    spinnerInt(buildingIdSpinner),
                    spinnerInt(sizeXSpinner),
                    spinnerInt(sizeYSpinner)
            );
            refreshFromDocument();
            buildingList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply building failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to selected building records.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasSelection = hasDocument && buildingList.getSelectedIndex() >= 0;
        setEditorControlsEnabled(hasDocument);
        addBuildingButton.setEnabled(hasDocument);
        deleteBuildingButton.setEnabled(hasSelection);
        applyBuildingButton.setEnabled(hasSelection);
    }

    /**
     * Java support bulk enabled-state update for building form controls.
     * not ported.
     */
    private void setEditorControlsEnabled(boolean enabled) {
        xSpinner.setEnabled(enabled);
        ySpinner.setEnabled(enabled);
        typeSpinner.setEnabled(enabled);
        hpSpinner.setEnabled(enabled);
        playerSpinner.setEnabled(enabled);
        buildingIdSpinner.setEnabled(enabled);
        sizeXSpinner.setEnabled(enabled);
        sizeYSpinner.setEnabled(enabled);
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support selected-building notification back to the owning frame.
     * not ported.
     */
    private void notifyBuildingSelection() {
        int selectedIndex = buildingList.getSelectedIndex();
        if (refreshing || selectedIndex < 0) {
            return;
        }
        selectionListener.accept(new MapEditorEntitySelection(MapEditorEntitySelection.Kind.BUILDING, selectedIndex));
    }

    /**
     * Java support list label for scenario buildings.
     * not ported.
     */
    private static String buildingLabel(BuildingDTO building) {
        return "#" + building.buildingID
                + " " + buildingTypeLabel(building.typeID)
                + " @ " + building.x + "," + building.y
                + " p" + building.playerID;
    }

    /**
     * Java support building type label for editor lists.
     * not ported.
     */
    private static String buildingTypeLabel(int typeID) {
        BuildingId buildingId = BuildingId.fromId(typeID);
        return buildingId == BuildingId.UNKNOWN ? "type " + typeID : buildingId.tableName;
    }

    /**
     * Java support default owner id for newly added building records.
     * not ported.
     */
    private int defaultPlayerId() {
        return document == null || document.playerCount() == 0 ? 1 : document.playerAt(0).playerId;
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support error dialog for building-editor operations.
     * not ported.
     */
    private void showError(String title, RuntimeException exception) {
        JOptionPane.showMessageDialog(
                this,
                exception.getMessage(),
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
}
