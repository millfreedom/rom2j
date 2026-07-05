package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.scenario.Instant;
import ua.millfreedom.rom2.model.world.scenario.Trigger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Swing instants/checks/triggers editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorLogicPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int SCRIPT_ARGUMENT_COUNT = 10;
    private static final int TRIGGER_CHECK_REFERENCE_COUNT = 6;
    private static final int TRIGGER_INSTANT_REFERENCE_COUNT = 4;

    private final DefaultListModel<String> instantListModel = new DefaultListModel<>();
    private final JList<String> instantList = new JList<>(instantListModel);
    private final JTextField instantNameField = new JTextField();
    private final JSpinner instantTypeSpinner = integerSpinner(0);
    private final JSpinner instantIndexSpinner = integerSpinner(1);
    private final JSpinner instantExecuteOnceSpinner = integerSpinner(0);
    private final JSpinner[] instantArgumentValueSpinners = integerSpinners(SCRIPT_ARGUMENT_COUNT);
    private final JSpinner[] instantArgumentTypeSpinners = integerSpinners(SCRIPT_ARGUMENT_COUNT);
    private final JTextField[] instantArgumentNameFields = textFields(SCRIPT_ARGUMENT_COUNT);
    private final JSpinner dropLocationXSpinner = integerSpinner(0);
    private final JSpinner dropLocationYSpinner = integerSpinner(0);
    private final JButton addDropLocationButton = button("Add Drop Location", this::addDropLocation);
    private final JButton addInstantButton = button("Add Instant", this::addInstant);
    private final JButton deleteInstantButton = button("Delete Instant", this::deleteInstant);
    private final JButton applyInstantButton = button("Apply Instant", this::applyInstant);

    private final DefaultListModel<String> checkListModel = new DefaultListModel<>();
    private final JList<String> checkList = new JList<>(checkListModel);
    private final JTextField checkNameField = new JTextField();
    private final JSpinner checkTypeSpinner = integerSpinner(0);
    private final JSpinner checkIndexSpinner = integerSpinner(1);
    private final JSpinner checkExecuteOnceSpinner = integerSpinner(1);
    private final JSpinner[] checkArgumentValueSpinners = integerSpinners(SCRIPT_ARGUMENT_COUNT);
    private final JSpinner[] checkArgumentTypeSpinners = integerSpinners(SCRIPT_ARGUMENT_COUNT);
    private final JTextField[] checkArgumentNameFields = textFields(SCRIPT_ARGUMENT_COUNT);
    private final JButton addCheckButton = button("Add Check", this::addCheck);
    private final JButton deleteCheckButton = button("Delete Check", this::deleteCheck);
    private final JButton applyCheckButton = button("Apply Check", this::applyCheck);

    private final DefaultListModel<String> triggerListModel = new DefaultListModel<>();
    private final JList<String> triggerList = new JList<>(triggerListModel);
    private final JTextField triggerDescriptionField = new JTextField();
    private final JSpinner[] triggerCheckIdSpinners = integerSpinners(TRIGGER_CHECK_REFERENCE_COUNT);
    private final JSpinner[] triggerInstantIdSpinners = integerSpinners(TRIGGER_INSTANT_REFERENCE_COUNT);
    private final JSpinner check12OperatorSpinner = integerSpinner(0);
    private final JSpinner check34OperatorSpinner = integerSpinner(0);
    private final JSpinner check56OperatorSpinner = integerSpinner(0);
    private final JSpinner triggerRunOnceSpinner = integerSpinner(0);
    private final JButton addTriggerButton = button("Add Trigger", this::addTrigger);
    private final JButton deleteTriggerButton = button("Delete Trigger", this::deleteTrigger);
    private final JButton applyTriggerButton = button("Apply Trigger", this::applyTrigger);
    private final JTabbedPane tabs = new JTabbedPane();

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor logic panel.
     * not ported.
     */
    public MapEditorLogicPanel() {
        super(new BorderLayout(6, 6));
        instantList.setVisibleRowCount(7);
        checkList.setVisibleRowCount(7);
        triggerList.setVisibleRowCount(7);
        instantList.setPrototypeCellValue("#00000 type 000000 index 000000 name Very_Long_Instant_Name");
        checkList.setPrototypeCellValue("#00000 type 000000 index 000000 name Very_Long_Check_Name");
        triggerList.setPrototypeCellValue("#00000 checks [000000, 000000, 000000, 000000, 000000, 000000]");
        instantList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncInstantSelectionToFields();
                notifyInstantSelection();
            }
        });
        checkList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncCheckSelectionToFields();
                notifyNonMapLogicSelection();
            }
        });
        triggerList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncTriggerSelectionToFields();
                notifyNonMapLogicSelection();
            }
        });

        tabs.addTab("Instants", createInstantPanel());
        tabs.addTab("Checks", createCheckPanel());
        tabs.addTab("Triggers", createTriggerPanel());
        add(tabs, BorderLayout.CENTER);
        updateControlsEnabled();
    }

    /**
     * Java support map-click coordinate binding for the native-editor Drop Location helper.
     * not ported.
     */
    void setDropLocationCell(Point cell) {
        if (cell == null) {
            return;
        }
        dropLocationXSpinner.setValue(cell.x);
        dropLocationYSpinner.setValue(cell.y);
    }

    /**
     * Java support document binding for the logic panel.
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
        int previousInstantSelection = instantList.getSelectedIndex();
        int previousCheckSelection = checkList.getSelectedIndex();
        int previousTriggerSelection = triggerList.getSelectedIndex();
        refreshing = true;
        refreshInstantList(previousInstantSelection);
        refreshCheckList(previousCheckSelection);
        refreshTriggerList(previousTriggerSelection);
        refreshing = false;
        syncInstantSelectionToFields();
        syncCheckSelectionToFields();
        syncTriggerSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selection hook used by viewport entity hit-testing.
     * not ported.
     */
    void selectInstantIndex(int instantIndex) {
        if (instantIndex < 0 || instantIndex >= instantListModel.size()) {
            return;
        }
        tabs.setSelectedIndex(0);
        instantList.setSelectedIndex(instantIndex);
        instantList.ensureIndexIsVisible(instantIndex);
    }

    /**
     * Java support instants tab construction.
     * not ported.
     */
    private JPanel createInstantPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(createDropLocationPanel(), BorderLayout.NORTH);
        panel.add(new JScrollPane(instantList), BorderLayout.CENTER);
        panel.add(createScriptRecordControls(
                "Instant",
                instantNameField,
                instantTypeSpinner,
                instantIndexSpinner,
                instantExecuteOnceSpinner,
                instantArgumentValueSpinners,
                instantArgumentTypeSpinners,
                instantArgumentNameFields,
                addInstantButton,
                deleteInstantButton,
                applyInstantButton
        ), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support Help-aligned Drop Location helper construction for the instants tab.
     * not ported.
     */
    private JPanel createDropLocationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "X", dropLocationXSpinner);
        addField(panel, 0, 1, "Y", dropLocationYSpinner);
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 1;
        buttonConstraints.gridwidth = 4;
        buttonConstraints.weightx = 1.0;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(3, 0, 0, 0);
        panel.add(addDropLocationButton, buttonConstraints);
        return MapEditorCollapsiblePanel.collapsed("Drop Location", panel);
    }

    /**
     * Java support checks tab construction.
     * not ported.
     */
    private JPanel createCheckPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(checkList), BorderLayout.CENTER);
        panel.add(createScriptRecordControls(
                "Check",
                checkNameField,
                checkTypeSpinner,
                checkIndexSpinner,
                checkExecuteOnceSpinner,
                checkArgumentValueSpinners,
                checkArgumentTypeSpinners,
                checkArgumentNameFields,
                addCheckButton,
                deleteCheckButton,
                applyCheckButton
        ), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support triggers tab construction.
     * not ported.
     */
    private JPanel createTriggerPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(triggerList), BorderLayout.CENTER);
        panel.add(createTriggerControls(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support shared instant/check controls construction.
     * not ported.
     */
    private static JPanel createScriptRecordControls(
            String title,
            JTextField nameField,
            JSpinner typeSpinner,
            JSpinner indexSpinner,
            JSpinner executeOnceSpinner,
            JSpinner[] argumentValueSpinners,
            JSpinner[] argumentTypeSpinners,
            JTextField[] argumentNameFields,
            JButton addButton,
            JButton deleteButton,
            JButton applyButton
    ) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel metadata = new JPanel(new GridBagLayout());
        addField(metadata, 0, 0, "Name", nameField);
        addField(metadata, 1, 0, "Type", typeSpinner);
        addField(metadata, 2, 0, "Index", indexSpinner);
        addField(metadata, 3, 0, "Once", executeOnceSpinner);
        panel.add(MapEditorCollapsiblePanel.collapsed(title, metadata), BorderLayout.NORTH);
        panel.add(createArgumentPanel(argumentValueSpinners, argumentTypeSpinners, argumentNameFields), BorderLayout.CENTER);
        panel.add(createButtonPanel(addButton, deleteButton, applyButton), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support instant/check ten-argument controls construction.
     * not ported.
     */
    private static JPanel createArgumentPanel(
            JSpinner[] valueSpinners,
            JSpinner[] typeSpinners,
            JTextField[] nameFields
    ) {
        JPanel panel = new JPanel(new GridBagLayout());
        addArgumentHeader(panel);
        for (int argument = 0; argument < SCRIPT_ARGUMENT_COUNT; argument++) {
            addArgumentRow(panel, argument + 1, argument, valueSpinners[argument], typeSpinners[argument], nameFields[argument]);
        }
        return MapEditorCollapsiblePanel.collapsed("Arguments", panel);
    }

    /**
     * Java support argument-grid header construction.
     * not ported.
     */
    private static void addArgumentHeader(JPanel panel) {
        String[] labels = {"Arg", "Value", "Type", "Name"};
        for (int column = 0; column < labels.length; column++) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = column;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(2, column == 0 ? 0 : 4, 2, 0);
            panel.add(new JLabel(labels[column]), constraints);
        }
    }

    /**
     * Java support one argument-grid row construction.
     * not ported.
     */
    private static void addArgumentRow(
            JPanel panel,
            int row,
            int argumentIndex,
            JSpinner valueSpinner,
            JSpinner typeSpinner,
            JTextField nameField
    ) {
        GridBagConstraints indexConstraints = new GridBagConstraints();
        indexConstraints.gridx = 0;
        indexConstraints.gridy = row;
        indexConstraints.anchor = GridBagConstraints.WEST;
        indexConstraints.insets = new Insets(2, 0, 2, 8);
        panel.add(new JLabel(Integer.toString(argumentIndex)), indexConstraints);
        addArgumentField(panel, row, 1, valueSpinner);
        addArgumentField(panel, row, 2, typeSpinner);
        addArgumentField(panel, row, 3, nameField);
    }

    /**
     * Java support one argument-grid field placement.
     * not ported.
     */
    private static void addArgumentField(JPanel panel, int row, int column, Component component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.weightx = column == 3 ? 1.0 : 0.35;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, column == 1 ? 0 : 4, 2, 0);
        panel.add(component, constraints);
    }

    /**
     * Java support trigger controls construction.
     * not ported.
     */
    private JPanel createTriggerControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel metadata = new JPanel(new GridBagLayout());
        addField(metadata, 0, 0, "Description", triggerDescriptionField);
        addField(metadata, 1, 0, "Op 1/2", check12OperatorSpinner);
        addField(metadata, 2, 0, "Op 3/4", check34OperatorSpinner);
        addField(metadata, 3, 0, "Op 5/6", check56OperatorSpinner);
        addField(metadata, 4, 0, "Once", triggerRunOnceSpinner);
        panel.add(MapEditorCollapsiblePanel.collapsed("Trigger", metadata), BorderLayout.NORTH);
        panel.add(createTriggerReferencesPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(addTriggerButton, deleteTriggerButton, applyTriggerButton), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support trigger check/instant reference controls construction.
     * not ported.
     */
    private JPanel createTriggerReferencesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addReferenceRow(panel, 0, "Checks", triggerCheckIdSpinners);
        addReferenceRow(panel, 1, "Instants", triggerInstantIdSpinners);
        return MapEditorCollapsiblePanel.collapsed("References", panel);
    }

    /**
     * Java support trigger reference row construction.
     * not ported.
     */
    private static void addReferenceRow(JPanel panel, int row, String label, JSpinner[] spinners) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(2, 0, 2, 8);
        panel.add(new JLabel(label), labelConstraints);

        for (int i = 0; i < spinners.length; i++) {
            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = i + 1;
            fieldConstraints.gridy = row;
            fieldConstraints.weightx = 1.0;
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.insets = new Insets(2, i == 0 ? 0 : 4, 2, 0);
            panel.add(spinners[i], fieldConstraints);
        }
    }

    /**
     * Java support button-row construction for logic commands.
     * not ported.
     */
    private static JPanel createButtonPanel(JButton addButton, JButton deleteButton, JButton applyButton) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 2, 0, 2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(addButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyButton, constraints);
        return panel;
    }

    /**
     * Java support helper for adding one metadata form row.
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
     * Java support button construction helper for logic commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support integer spinner construction for logic fields.
     * not ported.
     */
    private static JSpinner integerSpinner(int value) {
        return new JSpinner(new SpinnerNumberModel(value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    }

    /**
     * Java support fixed-length spinner-array construction.
     * not ported.
     */
    private static JSpinner[] integerSpinners(int count) {
        JSpinner[] spinners = new JSpinner[count];
        for (int i = 0; i < spinners.length; i++) {
            spinners[i] = integerSpinner(0);
        }
        return spinners;
    }

    /**
     * Java support fixed-length text-field array construction.
     * not ported.
     */
    private static JTextField[] textFields(int count) {
        JTextField[] fields = new JTextField[count];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new JTextField();
        }
        return fields;
    }

    /**
     * Java support instant list refresh from the active document.
     * not ported.
     */
    private void refreshInstantList(int previousSelection) {
        List<String> labels = new ArrayList<>();
        if (document != null) {
            for (int i = 0; i < document.instantCount(); i++) {
                labels.add(instantLabel(document.instantAt(i)));
            }
        }
        instantListModel.clear();
        instantListModel.addAll(labels);
        selectListIndex(instantList, instantListModel, previousSelection);
    }

    /**
     * Java support check list refresh from the active document.
     * not ported.
     */
    private void refreshCheckList(int previousSelection) {
        List<String> labels = new ArrayList<>();
        if (document != null) {
            for (int i = 0; i < document.checkCount(); i++) {
                labels.add(instantLabel(document.checkAt(i)));
            }
        }
        checkListModel.clear();
        checkListModel.addAll(labels);
        selectListIndex(checkList, checkListModel, previousSelection);
    }

    /**
     * Java support trigger list refresh from the active document.
     * not ported.
     */
    private void refreshTriggerList(int previousSelection) {
        List<String> labels = new ArrayList<>();
        if (document != null) {
            for (int i = 0; i < document.triggerCount(); i++) {
                labels.add(triggerLabel(document.triggerAt(i)));
            }
        }
        triggerListModel.clear();
        triggerListModel.addAll(labels);
        selectListIndex(triggerList, triggerListModel, previousSelection);
    }

    /**
     * Java support list selection restoration after logic-list refresh.
     * not ported.
     */
    private static void selectListIndex(JList<String> list, DefaultListModel<String> model, int previousSelection) {
        if (model.isEmpty()) {
            list.clearSelection();
            return;
        }
        int selectedIndex = Math.min(Math.max(previousSelection, 0), model.size() - 1);
        list.setSelectedIndex(selectedIndex);
    }

    /**
     * Java support selection transfer from instant list row to editor controls.
     * not ported.
     */
    private void syncInstantSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = instantList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetInstantFieldsForNewRecord();
            updateControlsEnabled();
            return;
        }
        setScriptRecordFields(
                document.instantAt(selectedIndex),
                instantNameField,
                instantTypeSpinner,
                instantIndexSpinner,
                instantExecuteOnceSpinner,
                instantArgumentValueSpinners,
                instantArgumentTypeSpinners,
                instantArgumentNameFields
        );
        syncDropLocationFieldsFromInstant(document.instantAt(selectedIndex));
        updateControlsEnabled();
    }

    /**
     * Java support selection transfer from check list row to editor controls.
     * not ported.
     */
    private void syncCheckSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = checkList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetCheckFieldsForNewRecord();
            updateControlsEnabled();
            return;
        }
        setScriptRecordFields(
                document.checkAt(selectedIndex),
                checkNameField,
                checkTypeSpinner,
                checkIndexSpinner,
                checkExecuteOnceSpinner,
                checkArgumentValueSpinners,
                checkArgumentTypeSpinners,
                checkArgumentNameFields
        );
        updateControlsEnabled();
    }

    /**
     * Java support selection transfer from trigger list row to editor controls.
     * not ported.
     */
    private void syncTriggerSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = triggerList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetTriggerFieldsForNewRecord();
            updateControlsEnabled();
            return;
        }
        Trigger trigger = document.triggerAt(selectedIndex);
        triggerDescriptionField.setText(trigger.description.toString());
        setSpinnerArrayValues(triggerCheckIdSpinners, trigger.checkIds);
        setSpinnerArrayValues(triggerInstantIdSpinners, trigger.instantIds);
        check12OperatorSpinner.setValue(trigger.check12Operator);
        check34OperatorSpinner.setValue(trigger.check34Operator);
        check56OperatorSpinner.setValue(trigger.check56Operator);
        triggerRunOnceSpinner.setValue(trigger.runOnce);
        updateControlsEnabled();
    }

    /**
     * Java support selected instant/check field refresh.
     * not ported.
     */
    private static void setScriptRecordFields(
            Instant instant,
            JTextField nameField,
            JSpinner typeSpinner,
            JSpinner indexSpinner,
            JSpinner executeOnceSpinner,
            JSpinner[] argumentValueSpinners,
            JSpinner[] argumentTypeSpinners,
            JTextField[] argumentNameFields
    ) {
        nameField.setText(instant.name.toString());
        typeSpinner.setValue(instant.typeId);
        indexSpinner.setValue(instant.index);
        executeOnceSpinner.setValue(instant.executeOnce);
        for (int i = 0; i < instant.arguments.length; i++) {
            argumentValueSpinners[i].setValue(instant.arguments[i].value);
            argumentTypeSpinners[i].setValue(instant.arguments[i].type);
            argumentNameFields[i].setText(instant.arguments[i].name.toString());
        }
    }

    /**
     * Java support default field setup for adding a new instant.
     * not ported.
     */
    private void resetInstantFieldsForNewRecord() {
        resetScriptRecordFields(
                instantNameField,
                instantTypeSpinner,
                instantIndexSpinner,
                instantExecuteOnceSpinner,
                instantArgumentValueSpinners,
                instantArgumentTypeSpinners,
                instantArgumentNameFields,
                0
        );
    }

    /**
     * Java support default field setup for adding a new check.
     * not ported.
     */
    private void resetCheckFieldsForNewRecord() {
        resetScriptRecordFields(
                checkNameField,
                checkTypeSpinner,
                checkIndexSpinner,
                checkExecuteOnceSpinner,
                checkArgumentValueSpinners,
                checkArgumentTypeSpinners,
                checkArgumentNameFields,
                1
        );
    }

    /**
     * Java support shared default field setup for adding a new instant/check record.
     * not ported.
     */
    private static void resetScriptRecordFields(
            JTextField nameField,
            JSpinner typeSpinner,
            JSpinner indexSpinner,
            JSpinner executeOnceSpinner,
            JSpinner[] argumentValueSpinners,
            JSpinner[] argumentTypeSpinners,
            JTextField[] argumentNameFields,
            int executeOnce
    ) {
        nameField.setText("");
        typeSpinner.setValue(0);
        indexSpinner.setValue(1);
        executeOnceSpinner.setValue(executeOnce);
        setSpinnerArrayValues(argumentValueSpinners, new int[SCRIPT_ARGUMENT_COUNT]);
        setSpinnerArrayValues(argumentTypeSpinners, new int[SCRIPT_ARGUMENT_COUNT]);
        setTextFieldArrayValues(argumentNameFields, new String[SCRIPT_ARGUMENT_COUNT]);
    }

    /**
     * Java support default field setup for adding a new trigger.
     * not ported.
     */
    private void resetTriggerFieldsForNewRecord() {
        triggerDescriptionField.setText("");
        setSpinnerArrayValues(triggerCheckIdSpinners, new int[TRIGGER_CHECK_REFERENCE_COUNT]);
        setSpinnerArrayValues(triggerInstantIdSpinners, new int[TRIGGER_INSTANT_REFERENCE_COUNT]);
        check12OperatorSpinner.setValue(0);
        check34OperatorSpinner.setValue(0);
        check56OperatorSpinner.setValue(0);
        triggerRunOnceSpinner.setValue(0);
    }

    /**
     * Java support command for appending one scenario instant.
     * not ported.
     */
    private void addInstant() {
        if (document == null) {
            return;
        }
        try {
            document.addInstant(
                    instantNameField.getText(),
                    spinnerInt(instantTypeSpinner),
                    spinnerInt(instantExecuteOnceSpinner),
                    spinnerArrayValues(instantArgumentValueSpinners),
                    spinnerArrayValues(instantArgumentTypeSpinners),
                    textFieldArrayValues(instantArgumentNameFields)
            );
            refreshFromDocument();
            instantList.setSelectedIndex(document.instantCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add instant failed", exception);
        }
    }

    /**
     * Java support command for adding the native editor's special Drop Location instant.
     * not ported.
     */
    private void addDropLocation() {
        if (document == null) {
            return;
        }
        try {
            document.addDropLocation(spinnerInt(dropLocationXSpinner), spinnerInt(dropLocationYSpinner));
            refreshFromDocument();
            instantList.setSelectedIndex(document.instantCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add drop location failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario instant.
     * not ported.
     */
    private void deleteInstant() {
        int selectedIndex = instantList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteInstant(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete instant failed", exception);
        }
    }

    /**
     * Java support command for applying selected instant attributes.
     * not ported.
     */
    private void applyInstant() {
        int selectedIndex = instantList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateInstant(
                    selectedIndex,
                    instantNameField.getText(),
                    spinnerInt(instantTypeSpinner),
                    spinnerInt(instantIndexSpinner),
                    spinnerInt(instantExecuteOnceSpinner),
                    spinnerArrayValues(instantArgumentValueSpinners),
                    spinnerArrayValues(instantArgumentTypeSpinners),
                    textFieldArrayValues(instantArgumentNameFields)
            );
            refreshFromDocument();
            instantList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply instant failed", exception);
        }
    }

    /**
     * Java support command for appending one scenario check.
     * not ported.
     */
    private void addCheck() {
        if (document == null) {
            return;
        }
        try {
            document.addCheck(
                    checkNameField.getText(),
                    spinnerInt(checkTypeSpinner),
                    spinnerInt(checkExecuteOnceSpinner),
                    spinnerArrayValues(checkArgumentValueSpinners),
                    spinnerArrayValues(checkArgumentTypeSpinners),
                    textFieldArrayValues(checkArgumentNameFields)
            );
            refreshFromDocument();
            checkList.setSelectedIndex(document.checkCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add check failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario check.
     * not ported.
     */
    private void deleteCheck() {
        int selectedIndex = checkList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteCheck(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete check failed", exception);
        }
    }

    /**
     * Java support command for applying selected check attributes.
     * not ported.
     */
    private void applyCheck() {
        int selectedIndex = checkList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateCheck(
                    selectedIndex,
                    checkNameField.getText(),
                    spinnerInt(checkTypeSpinner),
                    spinnerInt(checkIndexSpinner),
                    spinnerInt(checkExecuteOnceSpinner),
                    spinnerArrayValues(checkArgumentValueSpinners),
                    spinnerArrayValues(checkArgumentTypeSpinners),
                    textFieldArrayValues(checkArgumentNameFields)
            );
            refreshFromDocument();
            checkList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply check failed", exception);
        }
    }

    /**
     * Java support command for appending one scenario trigger.
     * not ported.
     */
    private void addTrigger() {
        if (document == null) {
            return;
        }
        try {
            document.addTrigger(
                    triggerDescriptionField.getText(),
                    spinnerArrayValues(triggerCheckIdSpinners),
                    spinnerArrayValues(triggerInstantIdSpinners),
                    spinnerInt(check12OperatorSpinner),
                    spinnerInt(check34OperatorSpinner),
                    spinnerInt(check56OperatorSpinner),
                    spinnerInt(triggerRunOnceSpinner)
            );
            refreshFromDocument();
            triggerList.setSelectedIndex(document.triggerCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add trigger failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario trigger.
     * not ported.
     */
    private void deleteTrigger() {
        int selectedIndex = triggerList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteTrigger(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete trigger failed", exception);
        }
    }

    /**
     * Java support command for applying selected trigger attributes.
     * not ported.
     */
    private void applyTrigger() {
        int selectedIndex = triggerList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateTrigger(
                    selectedIndex,
                    triggerDescriptionField.getText(),
                    spinnerArrayValues(triggerCheckIdSpinners),
                    spinnerArrayValues(triggerInstantIdSpinners),
                    spinnerInt(check12OperatorSpinner),
                    spinnerInt(check34OperatorSpinner),
                    spinnerInt(check56OperatorSpinner),
                    spinnerInt(triggerRunOnceSpinner)
            );
            refreshFromDocument();
            triggerList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply trigger failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to logic records.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasInstantSelection = hasDocument && instantList.getSelectedIndex() >= 0;
        boolean hasCheckSelection = hasDocument && checkList.getSelectedIndex() >= 0;
        boolean hasTriggerSelection = hasDocument && triggerList.getSelectedIndex() >= 0;
        setScriptRecordControlsEnabled(
                instantNameField,
                instantTypeSpinner,
                instantIndexSpinner,
                instantExecuteOnceSpinner,
                instantArgumentValueSpinners,
                instantArgumentTypeSpinners,
                instantArgumentNameFields,
                hasDocument
        );
        setScriptRecordControlsEnabled(
                checkNameField,
                checkTypeSpinner,
                checkIndexSpinner,
                checkExecuteOnceSpinner,
                checkArgumentValueSpinners,
                checkArgumentTypeSpinners,
                checkArgumentNameFields,
                hasDocument
        );
        setTriggerControlsEnabled(hasDocument);
        dropLocationXSpinner.setEnabled(hasDocument);
        dropLocationYSpinner.setEnabled(hasDocument);
        addDropLocationButton.setEnabled(hasDocument);
        addInstantButton.setEnabled(hasDocument);
        deleteInstantButton.setEnabled(hasInstantSelection);
        applyInstantButton.setEnabled(hasInstantSelection);
        addCheckButton.setEnabled(hasDocument);
        deleteCheckButton.setEnabled(hasCheckSelection);
        applyCheckButton.setEnabled(hasCheckSelection);
        addTriggerButton.setEnabled(hasDocument);
        deleteTriggerButton.setEnabled(hasTriggerSelection);
        applyTriggerButton.setEnabled(hasTriggerSelection);
    }

    /**
     * Java support bulk enabled-state update for instant/check controls.
     * not ported.
     */
    private static void setScriptRecordControlsEnabled(
            JTextField nameField,
            JSpinner typeSpinner,
            JSpinner indexSpinner,
            JSpinner executeOnceSpinner,
            JSpinner[] argumentValueSpinners,
            JSpinner[] argumentTypeSpinners,
            JTextField[] argumentNameFields,
            boolean enabled
    ) {
        nameField.setEnabled(enabled);
        typeSpinner.setEnabled(enabled);
        indexSpinner.setEnabled(enabled);
        executeOnceSpinner.setEnabled(enabled);
        setSpinnerArrayEnabled(argumentValueSpinners, enabled);
        setSpinnerArrayEnabled(argumentTypeSpinners, enabled);
        setTextFieldArrayEnabled(argumentNameFields, enabled);
    }

    /**
     * Java support bulk enabled-state update for trigger controls.
     * not ported.
     */
    private void setTriggerControlsEnabled(boolean enabled) {
        triggerDescriptionField.setEnabled(enabled);
        setSpinnerArrayEnabled(triggerCheckIdSpinners, enabled);
        setSpinnerArrayEnabled(triggerInstantIdSpinners, enabled);
        check12OperatorSpinner.setEnabled(enabled);
        check34OperatorSpinner.setEnabled(enabled);
        check56OperatorSpinner.setEnabled(enabled);
        triggerRunOnceSpinner.setEnabled(enabled);
    }

    /**
     * Java support bulk enabled-state update for an array of spinner controls.
     * not ported.
     */
    private static void setSpinnerArrayEnabled(JSpinner[] spinners, boolean enabled) {
        for (JSpinner spinner : spinners) {
            spinner.setEnabled(enabled);
        }
    }

    /**
     * Java support bulk enabled-state update for an array of text controls.
     * not ported.
     */
    private static void setTextFieldArrayEnabled(JTextField[] fields, boolean enabled) {
        for (JTextField field : fields) {
            field.setEnabled(enabled);
        }
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support selected Drop Location instant notification back to the owning frame.
     * not ported.
     */
    private void notifyInstantSelection() {
        int selectedIndex = instantList.getSelectedIndex();
        if (refreshing || document == null || selectedIndex < 0) {
            return;
        }
        Instant instant = document.instantAt(selectedIndex);
        if (isDropLocationInstant(instant)) {
            selectionListener.accept(new MapEditorEntitySelection(
                    MapEditorEntitySelection.Kind.DROP_LOCATION_INSTANT,
                    selectedIndex
            ));
        } else {
            selectionListener.accept(null);
        }
    }

    /**
     * Java support clearing map selection for logic rows that do not own map-visible markers.
     * not ported.
     */
    private void notifyNonMapLogicSelection() {
        if (!refreshing) {
            selectionListener.accept(null);
        }
    }

    /**
     * Java support Drop Location helper fields refresh from its serialized instant arguments.
     * not ported.
     */
    private void syncDropLocationFieldsFromInstant(Instant instant) {
        if (!isDropLocationInstant(instant)) {
            return;
        }
        dropLocationXSpinner.setValue(instant.arguments[0].value & 0xFF);
        dropLocationYSpinner.setValue(instant.arguments[1].value & 0xFF);
    }

    /**
     * Java support Drop Location instant type check matching ScenarioMapLoader's special instant branch.
     * not ported.
     */
    private static boolean isDropLocationInstant(Instant instant) {
        return instant.typeId == MapEditorDocument.MISSION_ENTRY_DROP_INSTANT_TYPE;
    }

    /**
     * Java support list label for scenario instants and checks.
     * not ported.
     */
    private static String instantLabel(Instant instant) {
        return "#" + instant.index
                + " type " + instant.typeId
                + " once " + instant.executeOnce
                + " " + instant.name;
    }

    /**
     * Java support list label for scenario triggers.
     * not ported.
     */
    private static String triggerLabel(Trigger trigger) {
        return trigger.description
                + " checks " + Arrays.toString(trigger.checkIds)
                + " instants " + Arrays.toString(trigger.instantIds);
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support helper for reading fixed-length int arrays from controls.
     * not ported.
     */
    private static int[] spinnerArrayValues(JSpinner[] spinners) {
        int[] values = new int[spinners.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = spinnerInt(spinners[i]);
        }
        return values;
    }

    /**
     * Java support helper for copying fixed-length int arrays into spinner controls.
     * not ported.
     */
    private static void setSpinnerArrayValues(JSpinner[] spinners, int[] values) {
        for (int i = 0; i < spinners.length; i++) {
            spinners[i].setValue(values[i]);
        }
    }

    /**
     * Java support helper for reading fixed-length text arrays from controls.
     * not ported.
     */
    private static String[] textFieldArrayValues(JTextField[] fields) {
        String[] values = new String[fields.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = fields[i].getText();
        }
        return values;
    }

    /**
     * Java support helper for copying fixed-length text arrays into controls.
     * not ported.
     */
    private static void setTextFieldArrayValues(JTextField[] fields, String[] values) {
        for (int i = 0; i < fields.length; i++) {
            fields[i].setText(values[i]);
        }
    }

    /**
     * Java support error dialog for logic editor operations.
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
