package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.ScenarioDescriptor;

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
 * Swing object-cell editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorObjectsPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DefaultListModel<String> objectListModel = new DefaultListModel<>();
    private final JList<String> objectList = new JList<>(objectListModel);
    private final List<Integer> objectCellIndexes = new ArrayList<>();
    private final JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner objectSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 0xFF, 1));
    private final JButton addObjectButton = button("Add Object", this::addObject);
    private final JButton eraseObjectButton = button("Erase Object", this::eraseObject);
    private final JButton applyObjectButton = button("Apply Object", this::applyObject);

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor object-cell panel.
     * not ported.
     */
    public MapEditorObjectsPanel() {
        super(new BorderLayout(6, 6));
        objectList.setVisibleRowCount(8);
        objectList.setPrototypeCellValue("#00000 @ 000,000 object 255");
        objectList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncSelectionToFields();
                notifyObjectSelection();
            }
        });

        add(new JScrollPane(objectList), BorderLayout.CENTER);
        add(createEditorControls(), BorderLayout.SOUTH);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the object-cell panel.
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
        int previousSelection = objectList.getSelectedIndex();
        int previousCellIndex = selectedCellIndex();
        List<String> labels = new ArrayList<>();
        refreshing = true;
        objectCellIndexes.clear();
        if (document != null) {
            ScenarioDescriptor scenario = document.scenario();
            for (int cellIndex = 0; cellIndex < scenario.sec3Objects.length; cellIndex++) {
                int objectValue = Byte.toUnsignedInt(scenario.sec3Objects[cellIndex]);
                if (objectValue != 0) {
                    objectCellIndexes.add(cellIndex);
                    labels.add(objectLabel(scenario, cellIndex, objectValue));
                }
            }
        }
        objectListModel.clear();
        objectListModel.addAll(labels);

        int selectedIndex = objectCellIndexes.indexOf(previousCellIndex);
        if (objectListModel.isEmpty()) {
            objectList.clearSelection();
        } else if (selectedIndex >= 0) {
            objectList.setSelectedIndex(selectedIndex);
        } else {
            objectList.setSelectedIndex(Math.min(Math.max(previousSelection, 0), objectListModel.size() - 1));
        }
        refreshing = false;
        syncSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selection hook used by viewport entity hit-testing.
     * not ported.
     */
    void selectObjectCellIndex(int cellIndex) {
        int objectRow = objectCellIndexes.indexOf(cellIndex);
        if (objectRow < 0) {
            return;
        }
        objectList.setSelectedIndex(objectRow);
        objectList.ensureIndexIsVisible(objectRow);
    }

    /**
     * Java support aggregate editor controls construction.
     * not ported.
     */
    private JPanel createEditorControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(createObjectAttributesPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support object-cell attribute form construction.
     * not ported.
     */
    private JPanel createObjectAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, "X", xSpinner);
        addField(panel, 1, "Y", ySpinner);
        addField(panel, 2, "Object", objectSpinner);
        return MapEditorCollapsiblePanel.collapsed("Object Cell", panel);
    }

    /**
     * Java support button-row construction for object-cell commands.
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
        panel.add(addObjectButton, constraints);
        constraints.gridx = 1;
        panel.add(eraseObjectButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyObjectButton, constraints);
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
     * Java support button construction helper for object-cell commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support selection transfer from object-cell list row to editor controls.
     * not ported.
     */
    private void syncSelectionToFields() {
        if (refreshing) {
            return;
        }
        int cellIndex = selectedCellIndex();
        if (document == null || cellIndex < 0) {
            resetFieldsForNewObject();
            updateControlsEnabled();
            return;
        }

        ScenarioDescriptor scenario = document.scenario();
        xSpinner.setValue(cellIndex % scenario.mapWidth);
        ySpinner.setValue(cellIndex / scenario.mapWidth);
        objectSpinner.setValue(Byte.toUnsignedInt(scenario.sec3Objects[cellIndex]));
        updateControlsEnabled();
    }

    /**
     * Java support default field setup for placing a new object cell.
     * not ported.
     */
    private void resetFieldsForNewObject() {
        xSpinner.setValue(0);
        ySpinner.setValue(0);
        objectSpinner.setValue(1);
    }

    /**
     * Java support command for setting one object byte on the scenario map.
     * not ported.
     */
    private void addObject() {
        if (document == null) {
            return;
        }
        try {
            int tileX = spinnerInt(xSpinner);
            int tileY = spinnerInt(ySpinner);
            document.setObject(tileX, tileY, spinnerInt(objectSpinner));
            refreshFromDocument();
            selectObjectCellIndex(cellIndex(document.scenario(), tileX, tileY));
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add object failed", exception);
        }
    }

    /**
     * Java support command for clearing the selected scenario object cell.
     * not ported.
     */
    private void eraseObject() {
        int cellIndex = selectedCellIndex();
        if (document == null || cellIndex < 0) {
            return;
        }
        try {
            ScenarioDescriptor scenario = document.scenario();
            document.setObject(cellIndex % scenario.mapWidth, cellIndex / scenario.mapWidth, 0);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Erase object failed", exception);
        }
    }

    /**
     * Java support command for applying selected object-cell attributes.
     * not ported.
     */
    private void applyObject() {
        int cellIndex = selectedCellIndex();
        if (document == null || cellIndex < 0) {
            return;
        }
        try {
            ScenarioDescriptor scenario = document.scenario();
            int nextTileX = spinnerInt(xSpinner);
            int nextTileY = spinnerInt(ySpinner);
            document.updateObjectCell(
                    cellIndex % scenario.mapWidth,
                    cellIndex / scenario.mapWidth,
                    nextTileX,
                    nextTileY,
                    spinnerInt(objectSpinner)
            );
            refreshFromDocument();
            selectObjectCellIndex(cellIndex(document.scenario(), nextTileX, nextTileY));
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply object failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to selected object cells.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasSelection = hasDocument && selectedCellIndex() >= 0;
        setEditorControlsEnabled(hasDocument);
        addObjectButton.setEnabled(hasDocument);
        eraseObjectButton.setEnabled(hasSelection);
        applyObjectButton.setEnabled(hasSelection);
    }

    /**
     * Java support bulk enabled-state update for object-cell form controls.
     * not ported.
     */
    private void setEditorControlsEnabled(boolean enabled) {
        xSpinner.setEnabled(enabled);
        ySpinner.setEnabled(enabled);
        objectSpinner.setEnabled(enabled);
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support selected-object-cell notification back to the owning frame.
     * not ported.
     */
    private void notifyObjectSelection() {
        int cellIndex = selectedCellIndex();
        if (refreshing || cellIndex < 0) {
            return;
        }
        selectionListener.accept(new MapEditorEntitySelection(MapEditorEntitySelection.Kind.OBJECT, cellIndex));
    }

    /**
     * Java support selected object-cell index lookup.
     * not ported.
     */
    private int selectedCellIndex() {
        int selectedIndex = objectList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= objectCellIndexes.size()) {
            return -1;
        }
        return objectCellIndexes.get(selectedIndex);
    }

    /**
     * Java support list label for scenario object cells.
     * not ported.
     */
    private static String objectLabel(ScenarioDescriptor scenario, int cellIndex, int objectValue) {
        return "object " + objectValue
                + " (0x" + Integer.toHexString(objectValue) + ")"
                + " @ " + (cellIndex % scenario.mapWidth) + "," + (cellIndex / scenario.mapWidth);
    }

    /**
     * Java support cell-index helper for object-cell panel selections.
     * not ported.
     */
    private static int cellIndex(ScenarioDescriptor scenario, int tileX, int tileY) {
        return tileY * scenario.mapWidth + tileX;
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support error dialog for object-cell editor operations.
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
