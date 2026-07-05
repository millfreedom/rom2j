package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.scenario.GroupDTO;

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

/**
 * Swing group/repop editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorGroupsPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DefaultListModel<String> groupListModel = new DefaultListModel<>();
    private final JList<String> groupList = new JList<>(groupListModel);
    private final JSpinner idSpinner = new JSpinner(new SpinnerNumberModel(1, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner repopTimeSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner flagsSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner instantIdSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JButton addGroupButton = button("Add Group", this::addGroup);
    private final JButton deleteGroupButton = button("Delete Group", this::deleteGroup);
    private final JButton applyGroupButton = button("Apply Group", this::applyGroup);

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor groups panel.
     * not ported.
     */
    public MapEditorGroupsPanel() {
        super(new BorderLayout(6, 6));
        groupList.setVisibleRowCount(8);
        groupList.setPrototypeCellValue("#00000 repop 000000 flags 000000 instant 000000");
        groupList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncSelectionToFields();
            }
        });

        add(new JScrollPane(groupList), BorderLayout.CENTER);
        add(createEditorControls(), BorderLayout.SOUTH);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the groups panel.
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
        int previousSelection = groupList.getSelectedIndex();
        List<String> labels = new ArrayList<>();
        refreshing = true;
        if (document != null) {
            for (int i = 0; i < document.groupCount(); i++) {
                labels.add(groupLabel(document.groupAt(i)));
            }
        }
        groupListModel.clear();
        groupListModel.addAll(labels);
        refreshing = false;

        if (groupListModel.isEmpty()) {
            groupList.clearSelection();
        } else {
            int selectedIndex = Math.min(Math.max(previousSelection, 0), groupListModel.size() - 1);
            groupList.setSelectedIndex(selectedIndex);
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
        panel.add(createGroupAttributesPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support group/repop attribute form construction.
     * not ported.
     */
    private JPanel createGroupAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "Id", idSpinner);
        addField(panel, 1, 0, "Repop", repopTimeSpinner);
        addField(panel, 0, 1, "Flags", flagsSpinner);
        addField(panel, 1, 1, "Instant", instantIdSpinner);
        return MapEditorCollapsiblePanel.collapsed("Group / Repop", panel);
    }

    /**
     * Java support button-row construction for group commands.
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
        panel.add(addGroupButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteGroupButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyGroupButton, constraints);
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
     * Java support button construction helper for group commands.
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
        int selectedIndex = groupList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetFieldsForNewGroup();
            updateControlsEnabled();
            return;
        }

        GroupDTO group = document.groupAt(selectedIndex);
        idSpinner.setValue(group.id);
        repopTimeSpinner.setValue(group.repopTime);
        flagsSpinner.setValue(group.flags);
        instantIdSpinner.setValue(group.instID);
        updateControlsEnabled();
    }

    /**
     * Java support default field setup for adding a new group record.
     * not ported.
     */
    private void resetFieldsForNewGroup() {
        idSpinner.setValue(1);
        repopTimeSpinner.setValue(0);
        flagsSpinner.setValue(0);
        instantIdSpinner.setValue(0);
    }

    /**
     * Java support command for appending one scenario group.
     * not ported.
     */
    private void addGroup() {
        if (document == null) {
            return;
        }
        try {
            document.addGroup(
                    spinnerInt(repopTimeSpinner),
                    spinnerInt(flagsSpinner),
                    spinnerInt(instantIdSpinner)
            );
            refreshFromDocument();
            groupList.setSelectedIndex(document.groupCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add group failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario group.
     * not ported.
     */
    private void deleteGroup() {
        int selectedIndex = groupList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteGroup(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete group failed", exception);
        }
    }

    /**
     * Java support command for applying selected group attributes.
     * not ported.
     */
    private void applyGroup() {
        int selectedIndex = groupList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateGroup(
                    selectedIndex,
                    spinnerInt(idSpinner),
                    spinnerInt(repopTimeSpinner),
                    spinnerInt(flagsSpinner),
                    spinnerInt(instantIdSpinner)
            );
            refreshFromDocument();
            groupList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply group failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to selected group records.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasSelection = hasDocument && groupList.getSelectedIndex() >= 0;
        setEditorControlsEnabled(hasDocument);
        addGroupButton.setEnabled(hasDocument);
        deleteGroupButton.setEnabled(hasSelection);
        applyGroupButton.setEnabled(hasSelection);
    }

    /**
     * Java support bulk enabled-state update for group form controls.
     * not ported.
     */
    private void setEditorControlsEnabled(boolean enabled) {
        idSpinner.setEnabled(enabled);
        repopTimeSpinner.setEnabled(enabled);
        flagsSpinner.setEnabled(enabled);
        instantIdSpinner.setEnabled(enabled);
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support list label for scenario groups.
     * not ported.
     */
    private static String groupLabel(GroupDTO group) {
        return "#" + group.id
                + " repop " + group.repopTime
                + " flags " + group.flags
                + " inst " + group.instID;
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support error dialog for group-editor operations.
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
