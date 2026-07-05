package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.scenario.WorldSack;

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
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Swing sacks/items editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorSacksPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DefaultListModel<String> sackListModel = new DefaultListModel<>();
    private final JList<String> sackList = new JList<>(sackListModel);
    private final DefaultListModel<String> itemListModel = new DefaultListModel<>();
    private final JList<String> itemList = new JList<>(itemListModel);
    private final JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner unitIdSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner goldSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner packedHashSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner incomingFlagsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner effectIndexSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JButton addSackButton = button("Add Sack", this::addSack);
    private final JButton deleteSackButton = button("Delete Sack", this::deleteSack);
    private final JButton applySackButton = button("Apply Sack", this::applySack);
    private final JButton addItemButton = button("Add Item", this::addItem);
    private final JButton deleteItemButton = button("Delete Item", this::deleteItem);
    private final JButton applyItemButton = button("Apply Item", this::applyItem);

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor sacks/items panel.
     * not ported.
     */
    public MapEditorSacksPanel() {
        super(new BorderLayout(6, 6));
        sackList.setVisibleRowCount(7);
        itemList.setVisibleRowCount(7);
        sackList.setPrototypeCellValue("#00000 @ 000,000 unit 000000 gold 000000 hash 65535 flags 65535 effect 000000");
        itemList.setPrototypeCellValue("#00 Some_Long_Item_Name flags 000000 count 000000");
        sackList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncSackSelectionToFields();
                notifySackSelection();
            }
        });
        itemList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncItemSelectionToFields();
            }
        });

        add(createListPanel(), BorderLayout.CENTER);
        add(createEditorControls(), BorderLayout.SOUTH);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the sacks/items panel.
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
        int previousSackSelection = sackList.getSelectedIndex();
        int previousItemSelection = itemList.getSelectedIndex();
        List<String> labels = new ArrayList<>();
        refreshing = true;
        if (document != null) {
            for (int i = 0; i < document.sackCount(); i++) {
                labels.add(sackLabel(document.sackAt(i)));
            }
        }
        sackListModel.clear();
        sackListModel.addAll(labels);
        if (sackListModel.isEmpty()) {
            sackList.clearSelection();
        } else {
            int selectedSackIndex = Math.min(Math.max(previousSackSelection, 0), sackListModel.size() - 1);
            sackList.setSelectedIndex(selectedSackIndex);
        }
        refreshItemList(previousItemSelection);
        refreshing = false;

        syncSackFieldsOnly();
        syncItemSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selection hook used by viewport entity hit-testing.
     * not ported.
     */
    void selectSackIndex(int sackIndex) {
        if (sackIndex < 0 || sackIndex >= sackListModel.size()) {
            return;
        }
        sackList.setSelectedIndex(sackIndex);
        sackList.ensureIndexIsVisible(sackIndex);
    }

    /**
     * Java support split list construction for sacks and their item records.
     * not ported.
     */
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 6, 0));
        panel.add(new JScrollPane(sackList));
        panel.add(new JScrollPane(itemList));
        return panel;
    }

    /**
     * Java support aggregate editor controls construction.
     * not ported.
     */
    private JPanel createEditorControls() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 6, 0));
        panel.add(createSackEditorControls());
        panel.add(createItemEditorControls());
        return panel;
    }

    /**
     * Java support sack editor controls construction.
     * not ported.
     */
    private JPanel createSackEditorControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(createSackAttributesPanel(), BorderLayout.CENTER);
        panel.add(createSackButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support item editor controls construction.
     * not ported.
     */
    private JPanel createItemEditorControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(createItemAttributesPanel(), BorderLayout.CENTER);
        panel.add(createItemButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support sack attribute form construction.
     * not ported.
     */
    private JPanel createSackAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "X", xSpinner);
        addField(panel, 1, 0, "Y", ySpinner);
        addField(panel, 0, 1, "Unit", unitIdSpinner);
        addField(panel, 1, 1, "Gold", goldSpinner);
        return MapEditorCollapsiblePanel.collapsed("Sack", panel);
    }

    /**
     * Java support sack item attribute form construction.
     * not ported.
     */
    private JPanel createItemAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "Hash", packedHashSpinner);
        addField(panel, 1, 0, "Flags", incomingFlagsSpinner);
        addField(panel, 0, 1, "Effect", effectIndexSpinner);
        return MapEditorCollapsiblePanel.collapsed("Sack Item", panel);
    }

    /**
     * Java support button-row construction for sack commands.
     * not ported.
     */
    private JPanel createSackButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = commandButtonConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(addSackButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteSackButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applySackButton, constraints);
        return panel;
    }

    /**
     * Java support button-row construction for sack item commands.
     * not ported.
     */
    private JPanel createItemButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = commandButtonConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(addItemButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteItemButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyItemButton, constraints);
        return panel;
    }

    /**
     * Java support command-button layout constraints.
     * not ported.
     */
    private static GridBagConstraints commandButtonConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 2, 0, 2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        return constraints;
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
     * Java support button construction helper for sack/item commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support item-list refresh for the currently selected sack.
     * not ported.
     */
    private void refreshItemList(int selectedItemIndex) {
        List<String> labels = new ArrayList<>();
        int sackIndex = sackList.getSelectedIndex();
        if (document != null && sackIndex >= 0) {
            WorldSack sack = document.sackAt(sackIndex);
            for (int i = 0; i < document.sackItemCount(sackIndex); i++) {
                labels.add(itemLabel(sack, i));
            }
        }
        itemListModel.clear();
        itemListModel.addAll(labels);
        if (itemListModel.isEmpty()) {
            itemList.clearSelection();
        } else {
            int itemIndex = Math.min(Math.max(selectedItemIndex, 0), itemListModel.size() - 1);
            itemList.setSelectedIndex(itemIndex);
        }
    }

    /**
     * Java support selection transfer from sack list row to editor controls.
     * not ported.
     */
    private void syncSackSelectionToFields() {
        if (refreshing) {
            return;
        }
        syncSackFieldsOnly();
        refreshItemList(0);
        syncItemSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selected-sack field refresh without changing item rows.
     * not ported.
     */
    private void syncSackFieldsOnly() {
        int selectedIndex = sackList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetSackFieldsForNewSack();
            return;
        }
        WorldSack sack = document.sackAt(selectedIndex);
        xSpinner.setValue(sackTileX(sack));
        ySpinner.setValue(sackTileY(sack));
        unitIdSpinner.setValue(sack.unitID);
        goldSpinner.setValue(sack.gold);
    }

    /**
     * Java support selection transfer from sack item list row to editor controls.
     * not ported.
     */
    private void syncItemSelectionToFields() {
        if (refreshing) {
            return;
        }
        int sackIndex = sackList.getSelectedIndex();
        int itemIndex = itemList.getSelectedIndex();
        if (document == null || sackIndex < 0 || itemIndex < 0) {
            resetItemFieldsForNewItem();
            updateControlsEnabled();
            return;
        }

        WorldSack sack = document.sackAt(sackIndex);
        packedHashSpinner.setValue(sack.itemPackedHashes.get(itemIndex) & 0xFFFF);
        incomingFlagsSpinner.setValue(sack.incomingItemFlags.get(itemIndex) & 0xFFFF);
        effectIndexSpinner.setValue(sack.effectIndices.get(itemIndex));
        updateControlsEnabled();
    }

    /**
     * Java support default field setup for adding a new sack record.
     * not ported.
     */
    private void resetSackFieldsForNewSack() {
        xSpinner.setValue(0);
        ySpinner.setValue(0);
        unitIdSpinner.setValue(0);
        goldSpinner.setValue(0);
    }

    /**
     * Java support default field setup for adding a new sack item record.
     * not ported.
     */
    private void resetItemFieldsForNewItem() {
        packedHashSpinner.setValue(0);
        incomingFlagsSpinner.setValue(0);
        effectIndexSpinner.setValue(0);
    }

    /**
     * Java support command for appending one scenario sack.
     * not ported.
     */
    private void addSack() {
        if (document == null) {
            return;
        }
        try {
            document.addSack(
                    spinnerInt(xSpinner),
                    spinnerInt(ySpinner),
                    spinnerInt(unitIdSpinner),
                    spinnerInt(goldSpinner)
            );
            refreshFromDocument();
            sackList.setSelectedIndex(document.sackCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add sack failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario sack.
     * not ported.
     */
    private void deleteSack() {
        int selectedIndex = sackList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteSack(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete sack failed", exception);
        }
    }

    /**
     * Java support command for applying selected sack attributes.
     * not ported.
     */
    private void applySack() {
        int selectedIndex = sackList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateSack(
                    selectedIndex,
                    spinnerInt(xSpinner),
                    spinnerInt(ySpinner),
                    spinnerInt(unitIdSpinner),
                    spinnerInt(goldSpinner)
            );
            refreshFromDocument();
            sackList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply sack failed", exception);
        }
    }

    /**
     * Java support command for appending one selected-sack item record.
     * not ported.
     */
    private void addItem() {
        int sackIndex = sackList.getSelectedIndex();
        if (document == null || sackIndex < 0) {
            return;
        }
        try {
            document.addSackItem(
                    sackIndex,
                    spinnerInt(packedHashSpinner),
                    spinnerInt(incomingFlagsSpinner),
                    spinnerInt(effectIndexSpinner)
            );
            refreshFromDocument();
            sackList.setSelectedIndex(sackIndex);
            itemList.setSelectedIndex(document.sackItemCount(sackIndex) - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add sack item failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario-sack item.
     * not ported.
     */
    private void deleteItem() {
        int sackIndex = sackList.getSelectedIndex();
        int itemIndex = itemList.getSelectedIndex();
        if (document == null || sackIndex < 0 || itemIndex < 0) {
            return;
        }
        try {
            document.deleteSackItem(sackIndex, itemIndex);
            refreshFromDocument();
            sackList.setSelectedIndex(sackIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete sack item failed", exception);
        }
    }

    /**
     * Java support command for applying selected scenario-sack item attributes.
     * not ported.
     */
    private void applyItem() {
        int sackIndex = sackList.getSelectedIndex();
        int itemIndex = itemList.getSelectedIndex();
        if (document == null || sackIndex < 0 || itemIndex < 0) {
            return;
        }
        try {
            document.updateSackItem(
                    sackIndex,
                    itemIndex,
                    spinnerInt(packedHashSpinner),
                    spinnerInt(incomingFlagsSpinner),
                    spinnerInt(effectIndexSpinner)
            );
            refreshFromDocument();
            sackList.setSelectedIndex(sackIndex);
            itemList.setSelectedIndex(itemIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply sack item failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to selected sack and item records.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasSackSelection = hasDocument && sackList.getSelectedIndex() >= 0;
        boolean hasItemSelection = hasSackSelection && itemList.getSelectedIndex() >= 0;
        setSackControlsEnabled(hasDocument);
        setItemControlsEnabled(hasSackSelection);
        addSackButton.setEnabled(hasDocument);
        deleteSackButton.setEnabled(hasSackSelection);
        applySackButton.setEnabled(hasSackSelection);
        addItemButton.setEnabled(hasSackSelection);
        deleteItemButton.setEnabled(hasItemSelection);
        applyItemButton.setEnabled(hasItemSelection);
    }

    /**
     * Java support bulk enabled-state update for sack form controls.
     * not ported.
     */
    private void setSackControlsEnabled(boolean enabled) {
        xSpinner.setEnabled(enabled);
        ySpinner.setEnabled(enabled);
        unitIdSpinner.setEnabled(enabled);
        goldSpinner.setEnabled(enabled);
    }

    /**
     * Java support bulk enabled-state update for sack item form controls.
     * not ported.
     */
    private void setItemControlsEnabled(boolean enabled) {
        packedHashSpinner.setEnabled(enabled);
        incomingFlagsSpinner.setEnabled(enabled);
        effectIndexSpinner.setEnabled(enabled);
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support selected-sack notification back to the owning frame.
     * not ported.
     */
    private void notifySackSelection() {
        int selectedIndex = sackList.getSelectedIndex();
        if (refreshing || selectedIndex < 0) {
            return;
        }
        selectionListener.accept(new MapEditorEntitySelection(MapEditorEntitySelection.Kind.SACK, selectedIndex));
    }

    /**
     * Java support list label for scenario sacks.
     * not ported.
     */
    private static String sackLabel(WorldSack sack) {
        return (sack.unitID == 0 ? "bag" : "unit " + sack.unitID)
                + " @ " + sackTileX(sack) + "," + sackTileY(sack)
                + " gold " + sack.gold
                + " items " + sack.itemPackedHashes.size();
    }

    /**
     * Java support list label for scenario sack items.
     * not ported.
     */
    private static String itemLabel(WorldSack sack, int itemIndex) {
        return "#" + itemIndex
                + " hash 0x" + Integer.toHexString(sack.itemPackedHashes.get(itemIndex) & 0xFFFF)
                + " flags " + (sack.incomingItemFlags.get(itemIndex) & 0xFFFF)
                + " effect " + sack.effectIndices.get(itemIndex);
    }

    /**
     * Java support conversion from raw scenario-sack X coordinate to tile coordinate.
     * not ported.
     */
    private static int sackTileX(WorldSack sack) {
        return (sack.x >>> 8) & 0xFFFF;
    }

    /**
     * Java support conversion from raw scenario-sack Y coordinate to tile coordinate.
     * not ported.
     */
    private static int sackTileY(WorldSack sack) {
        return (sack.y >>> 8) & 0xFFFF;
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support error dialog for sacks/items editor operations.
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
