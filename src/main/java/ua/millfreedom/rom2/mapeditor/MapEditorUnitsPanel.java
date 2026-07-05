package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.enums.UnitId;
import ua.millfreedom.rom2.model.world.scenario.UnitDTO;

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
 * Swing units editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorUnitsPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DefaultListModel<String> unitListModel = new DefaultListModel<>();
    private final JList<String> unitList = new JList<>(unitListModel);
    private final JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner typeSpinner = new JSpinner(new SpinnerNumberModel(UnitId.HUMAN.id, 0, 0xFFFF, 1));
    private final JSpinner faceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner serverIdSpinner = new JSpinner(new SpinnerNumberModel(UnitId.HUMAN.id, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner playerSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 0xFFFF, 1));
    private final JSpinner sackIndexSpinner = new JSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
    private final JSpinner rotationSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner hpSpinner = new JSpinner(new SpinnerNumberModel(100, Short.MIN_VALUE, Short.MAX_VALUE, 1));
    private final JSpinner maxHpSpinner = new JSpinner(new SpinnerNumberModel(100, Short.MIN_VALUE, Short.MAX_VALUE, 1));
    private final JSpinner unitIdSpinner = new JSpinner(new SpinnerNumberModel(1, Short.MIN_VALUE, Short.MAX_VALUE, 1));
    private final JSpinner flagsSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner questFlagsSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner groupIdSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JButton addUnitButton = button("Add Unit", this::addUnit);
    private final JButton deleteUnitButton = button("Delete Unit", this::deleteUnit);
    private final JButton applyUnitButton = button("Apply Unit", this::applyUnit);

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor units panel.
     * not ported.
     */
    public MapEditorUnitsPanel() {
        super(new BorderLayout(6, 6));
        unitList.setVisibleRowCount(8);
        unitList.setPrototypeCellValue("#00000 Very_Long_Unit_Name.99 @ 000,000 p00 HP 00000/00000");
        unitList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncSelectionToFields();
                notifyUnitSelection();
            }
        });

        add(new JScrollPane(unitList), BorderLayout.CENTER);
        add(createEditorControls(), BorderLayout.SOUTH);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the units panel.
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
        int previousSelection = unitList.getSelectedIndex();
        List<String> labels = new ArrayList<>();
        refreshing = true;
        if (document != null) {
            for (int i = 0; i < document.unitCount(); i++) {
                labels.add(unitLabel(document.unitAt(i)));
            }
        }
        unitListModel.clear();
        unitListModel.addAll(labels);

        if (unitListModel.isEmpty()) {
            unitList.clearSelection();
        } else {
            int selectedIndex = Math.min(Math.max(previousSelection, 0), unitListModel.size() - 1);
            unitList.setSelectedIndex(selectedIndex);
        }
        refreshing = false;
        syncSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selection hook used by viewport entity hit-testing.
     * not ported.
     */
    void selectUnitIndex(int unitIndex) {
        if (unitIndex < 0 || unitIndex >= unitListModel.size()) {
            return;
        }
        unitList.setSelectedIndex(unitIndex);
        unitList.ensureIndexIsVisible(unitIndex);
    }

    /**
     * Java support aggregate editor controls construction.
     * not ported.
     */
    private JPanel createEditorControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(createUnitAttributesPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support unit-attribute form construction.
     * not ported.
     */
    private JPanel createUnitAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "X", xSpinner);
        addField(panel, 1, 0, "Y", ySpinner);
        addField(panel, 2, 0, "Type", typeSpinner);
        addField(panel, 3, 0, "Face", faceSpinner);
        addField(panel, 4, 0, "Server", serverIdSpinner);
        addField(panel, 5, 0, "Player", playerSpinner);
        addField(panel, 6, 0, "Sack", sackIndexSpinner);
        addField(panel, 0, 1, "Rot", rotationSpinner);
        addField(panel, 1, 1, "HP", hpSpinner);
        addField(panel, 2, 1, "Max HP", maxHpSpinner);
        addField(panel, 3, 1, "Id", unitIdSpinner);
        addField(panel, 4, 1, "Flags", flagsSpinner);
        addField(panel, 5, 1, "Quest", questFlagsSpinner);
        addField(panel, 6, 1, "Group", groupIdSpinner);
        return MapEditorCollapsiblePanel.collapsed("Unit", panel);
    }

    /**
     * Java support button-row construction for unit commands.
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
        panel.add(addUnitButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteUnitButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyUnitButton, constraints);
        return panel;
    }

    /**
     * Java support helper for adding one form row.
     * not ported.
     */
    private static void addField(JPanel panel, int row, int column, String label, Component component) {
        int labelColumn = column * 2;
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = labelColumn;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(3, column == 0 ? 0 : 12, 3, 8);
        panel.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = labelColumn + 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(3, 0, 3, 0);
        panel.add(component, fieldConstraints);
    }

    /**
     * Java support button construction helper for unit commands.
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
        int selectedIndex = unitList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetFieldsForNewUnit();
            updateControlsEnabled();
            return;
        }

        UnitDTO unit = document.unitAt(selectedIndex);
        xSpinner.setValue(unitTileX(unit));
        ySpinner.setValue(unitTileY(unit));
        typeSpinner.setValue(unit.typeID);
        faceSpinner.setValue(unit.face);
        serverIdSpinner.setValue(unit.serverID);
        playerSpinner.setValue(unit.playerID);
        sackIndexSpinner.setValue(unit.sackIDX);
        rotationSpinner.setValue(unit.rotation);
        hpSpinner.setValue(MapEditorUnitDisplay.displayHp(unit));
        maxHpSpinner.setValue(MapEditorUnitDisplay.displayMaxHp(unit));
        unitIdSpinner.setValue(MapEditorUnitDisplay.nativeUnitId(unit));
        flagsSpinner.setValue(unit.unitFlags1);
        questFlagsSpinner.setValue(unit.questFlags);
        groupIdSpinner.setValue(unit.groupID);
        updateControlsEnabled();
    }

    /**
     * Java support default field setup for adding a new unit record.
     * not ported.
     */
    private void resetFieldsForNewUnit() {
        xSpinner.setValue(0);
        ySpinner.setValue(0);
        typeSpinner.setValue(UnitId.HUMAN.id);
        faceSpinner.setValue(0);
        serverIdSpinner.setValue(UnitId.HUMAN.id);
        playerSpinner.setValue(defaultPlayerId());
        sackIndexSpinner.setValue(-1);
        rotationSpinner.setValue(0);
        hpSpinner.setValue(100);
        maxHpSpinner.setValue(100);
        unitIdSpinner.setValue(1);
        flagsSpinner.setValue(0);
        questFlagsSpinner.setValue(0);
        groupIdSpinner.setValue(0);
    }

    /**
     * Java support command for appending one scenario unit.
     * not ported.
     */
    private void addUnit() {
        if (document == null) {
            return;
        }
        try {
            document.addUnit(
                    spinnerInt(xSpinner),
                    spinnerInt(ySpinner),
                    spinnerInt(typeSpinner),
                    spinnerInt(faceSpinner),
                    spinnerInt(serverIdSpinner),
                    spinnerInt(playerSpinner),
                    spinnerInt(sackIndexSpinner),
                    spinnerInt(rotationSpinner),
                    spinnerInt(hpSpinner),
                    spinnerInt(maxHpSpinner),
                    spinnerInt(flagsSpinner),
                    spinnerInt(questFlagsSpinner),
                    spinnerInt(groupIdSpinner)
            );
            refreshFromDocument();
            unitList.setSelectedIndex(document.unitCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add unit failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario unit.
     * not ported.
     */
    private void deleteUnit() {
        int selectedIndex = unitList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteUnit(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete unit failed", exception);
        }
    }

    /**
     * Java support command for applying selected unit attributes.
     * not ported.
     */
    private void applyUnit() {
        int selectedIndex = unitList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateUnit(
                    selectedIndex,
                    spinnerInt(xSpinner),
                    spinnerInt(ySpinner),
                    spinnerInt(typeSpinner),
                    spinnerInt(faceSpinner),
                    spinnerInt(serverIdSpinner),
                    spinnerInt(playerSpinner),
                    spinnerInt(sackIndexSpinner),
                    spinnerInt(rotationSpinner),
                    appliedHp(selectedIndex),
                    appliedMaxHp(selectedIndex),
                    appliedUnitId(selectedIndex),
                    spinnerInt(flagsSpinner),
                    spinnerInt(questFlagsSpinner),
                    spinnerInt(groupIdSpinner)
            );
            refreshFromDocument();
            unitList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply unit failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to selected unit records.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasSelection = hasDocument && unitList.getSelectedIndex() >= 0;
        setEditorControlsEnabled(hasDocument);
        addUnitButton.setEnabled(hasDocument);
        deleteUnitButton.setEnabled(hasSelection);
        applyUnitButton.setEnabled(hasSelection);
    }

    /**
     * Java support bulk enabled-state update for unit form controls.
     * not ported.
     */
    private void setEditorControlsEnabled(boolean enabled) {
        xSpinner.setEnabled(enabled);
        ySpinner.setEnabled(enabled);
        typeSpinner.setEnabled(enabled);
        faceSpinner.setEnabled(enabled);
        serverIdSpinner.setEnabled(enabled);
        playerSpinner.setEnabled(enabled);
        sackIndexSpinner.setEnabled(enabled);
        rotationSpinner.setEnabled(enabled);
        hpSpinner.setEnabled(enabled);
        maxHpSpinner.setEnabled(enabled);
        unitIdSpinner.setEnabled(enabled);
        flagsSpinner.setEnabled(enabled);
        questFlagsSpinner.setEnabled(enabled);
        groupIdSpinner.setEnabled(enabled);
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support selected-unit notification back to the owning frame.
     * not ported.
     */
    private void notifyUnitSelection() {
        int selectedIndex = unitList.getSelectedIndex();
        if (refreshing || selectedIndex < 0) {
            return;
        }
        selectionListener.accept(new MapEditorEntitySelection(MapEditorEntitySelection.Kind.UNIT, selectedIndex));
    }

    /**
     * Java support list label for scenario units.
     * not ported.
     */
    private static String unitLabel(UnitDTO unit) {
        return MapEditorUnitDisplay.unitLabel(unit);
    }

    /**
     * Java support conversion from raw scenario-unit X coordinate to tile coordinate.
     * not ported.
     */
    private static int unitTileX(UnitDTO unit) {
        return MapEditorUnitDisplay.unitTileX(unit);
    }

    /**
     * Java support conversion from raw scenario-unit Y coordinate to tile coordinate.
     * not ported.
     */
    private static int unitTileY(UnitDTO unit) {
        return MapEditorUnitDisplay.unitTileY(unit);
    }

    /**
     * Java support raw scenario id preservation unless the editor-visible id changes.
     * not ported.
     */
    private int appliedUnitId(int selectedIndex) {
        return MapEditorUnitDisplay.storedUnitId(document.unitAt(selectedIndex), spinnerInt(unitIdSpinner));
    }

    /**
     * Java support raw scenario HP preservation unless the editor-visible HP changes.
     * not ported.
     */
    private int appliedHp(int selectedIndex) {
        return MapEditorUnitDisplay.storedHp(document.unitAt(selectedIndex), spinnerInt(hpSpinner));
    }

    /**
     * Java support raw scenario max-HP preservation unless the editor-visible max-HP changes.
     * not ported.
     */
    private int appliedMaxHp(int selectedIndex) {
        return MapEditorUnitDisplay.storedMaxHp(document.unitAt(selectedIndex), spinnerInt(maxHpSpinner));
    }

    /**
     * Java support default owner id for newly added unit records.
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
     * Java support error dialog for unit-editor operations.
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
