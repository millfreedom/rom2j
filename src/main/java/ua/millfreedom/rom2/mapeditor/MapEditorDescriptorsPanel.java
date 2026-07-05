package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.scenario.InnDescriptor;
import ua.millfreedom.rom2.model.world.scenario.PostDescriptor;
import ua.millfreedom.rom2.model.world.scenario.ShopDescriptor;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Swing descriptor-section editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorDescriptorsPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int SHOP_SHELF_COUNT = 4;

    private final DefaultListModel<String> innListModel = new DefaultListModel<>();
    private final JList<String> innList = new JList<>(innListModel);
    private final JSpinner innIdSpinner = integerSpinner(1);
    private final JSpinner innFlagsSpinner = integerSpinner(0);
    private final JSpinner innItemIdSpinner = integerSpinner(0);
    private final JButton addInnButton = button("Add Inn", this::addInnDescriptor);
    private final JButton deleteInnButton = button("Delete Inn", this::deleteInnDescriptor);
    private final JButton applyInnButton = button("Apply Inn", this::applyInnDescriptor);

    private final DefaultListModel<String> shopListModel = new DefaultListModel<>();
    private final JList<String> shopList = new JList<>(shopListModel);
    private final JSpinner shopIdSpinner = integerSpinner(1);
    private final JSpinner[] shopShelfFlagsSpinners = integerSpinners();
    private final JSpinner[] shopMinPricesSpinners = integerSpinners();
    private final JSpinner[] shopMaxPricesSpinners = integerSpinners();
    private final JSpinner[] shopMaxItemsSpinners = integerSpinners();
    private final JSpinner[] shopMaxSameTypeItemsSpinners = integerSpinners();
    private final JButton addShopButton = button("Add Shop", this::addShopDescriptor);
    private final JButton deleteShopButton = button("Delete Shop", this::deleteShopDescriptor);
    private final JButton applyShopButton = button("Apply Shop", this::applyShopDescriptor);

    private final DefaultListModel<String> postListModel = new DefaultListModel<>();
    private final JList<String> postList = new JList<>(postListModel);
    private final JTabbedPane descriptorTabs = new JTabbedPane();
    private final JSpinner postIdSpinner = integerSpinner(1);
    private final JSpinner postInstanceOnSpinner = integerSpinner(0);
    private final JSpinner postInstanceIdSpinner = integerSpinner(0);
    private final JButton addPostButton = button("Add Post", this::addPostDescriptor);
    private final JButton deletePostButton = button("Delete Post", this::deletePostDescriptor);
    private final JButton applyPostButton = button("Apply Post", this::applyPostDescriptor);

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the descriptor-section editor panel.
     * not ported.
     */
    public MapEditorDescriptorsPanel() {
        super(new BorderLayout(6, 6));
        innList.setVisibleRowCount(7);
        shopList.setVisibleRowCount(7);
        postList.setVisibleRowCount(7);
        innList.setPrototypeCellValue("#00000 flags 000000 item 000000");
        shopList.setPrototypeCellValue("#00000 shelves [000000000, 000000000, 000000000, 000000000]");
        postList.setPrototypeCellValue("#00000 on 000000 instant 000000");
        innList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncInnSelectionToFields();
                notifyInnSelection();
            }
        });
        shopList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncShopSelectionToFields();
                notifyShopSelection();
            }
        });
        postList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncPostSelectionToFields();
                notifyPostSelection();
            }
        });

        descriptorTabs.addTab("Inn", createInnPanel());
        descriptorTabs.addTab("Shop", createShopPanel());
        descriptorTabs.addTab("Post", createPostPanel());
        add(descriptorTabs, BorderLayout.CENTER);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the descriptors panel.
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
     * Java support descriptor-row selection from viewport entity picking.
     * not ported.
     */
    void selectInnDescriptorIndex(int descriptorIndex) {
        selectDescriptorIndex(0, innList, descriptorIndex);
    }

    /**
     * Java support descriptor-row selection from viewport entity picking.
     * not ported.
     */
    void selectShopDescriptorIndex(int descriptorIndex) {
        selectDescriptorIndex(1, shopList, descriptorIndex);
    }

    /**
     * Java support descriptor-row selection from viewport entity picking.
     * not ported.
     */
    void selectPostDescriptorIndex(int descriptorIndex) {
        selectDescriptorIndex(2, postList, descriptorIndex);
    }

    /**
     * Java support UI refresh from the active editor document.
     * not ported.
     */
    public void refreshFromDocument() {
        int previousInnSelection = innList.getSelectedIndex();
        int previousShopSelection = shopList.getSelectedIndex();
        int previousPostSelection = postList.getSelectedIndex();
        refreshing = true;
        refreshInnList(previousInnSelection);
        refreshShopList(previousShopSelection);
        refreshPostList(previousPostSelection);
        refreshing = false;
        syncInnSelectionToFields();
        syncShopSelectionToFields();
        syncPostSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support inn-descriptor tab construction.
     * not ported.
     */
    private JPanel createInnPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(innList), BorderLayout.CENTER);
        panel.add(createInnControls(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support shop-descriptor tab construction.
     * not ported.
     */
    private JPanel createShopPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(shopList), BorderLayout.CENTER);
        panel.add(createShopControls(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support post-descriptor tab construction.
     * not ported.
     */
    private JPanel createPostPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(postList), BorderLayout.CENTER);
        panel.add(createPostControls(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support inn-descriptor controls construction.
     * not ported.
     */
    private JPanel createInnControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel fields = new JPanel(new GridBagLayout());
        addField(fields, 0, 0, "Id", innIdSpinner);
        addField(fields, 1, 0, "Flags", innFlagsSpinner);
        addField(fields, 2, 0, "Item", innItemIdSpinner);
        panel.add(MapEditorCollapsiblePanel.collapsed("Inn", fields), BorderLayout.CENTER);
        panel.add(createButtonPanel(addInnButton, deleteInnButton, applyInnButton), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support shop-descriptor controls construction.
     * not ported.
     */
    private JPanel createShopControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel idFields = new JPanel(new GridBagLayout());
        addField(idFields, 0, 0, "Id", shopIdSpinner);
        panel.add(MapEditorCollapsiblePanel.collapsed("Shop", idFields), BorderLayout.NORTH);
        panel.add(createShopShelfPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(addShopButton, deleteShopButton, applyShopButton), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support post-descriptor controls construction.
     * not ported.
     */
    private JPanel createPostControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel fields = new JPanel(new GridBagLayout());
        addField(fields, 0, 0, "Id", postIdSpinner);
        addField(fields, 1, 0, "On", postInstanceOnSpinner);
        addField(fields, 2, 0, "Instant", postInstanceIdSpinner);
        panel.add(MapEditorCollapsiblePanel.collapsed("Post / Pointer", fields), BorderLayout.CENTER);
        panel.add(createButtonPanel(addPostButton, deletePostButton, applyPostButton), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support shop shelf-array controls construction.
     * not ported.
     */
    private JPanel createShopShelfPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        for (int shelf = 0; shelf < SHOP_SHELF_COUNT; shelf++) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = shelf + 1;
            constraints.gridy = 0;
            constraints.insets = new Insets(2, shelf == 0 ? 0 : 4, 2, 0);
            panel.add(new JLabel("Shelf " + shelf), constraints);
        }
        addShelfRow(panel, 1, "Flags", shopShelfFlagsSpinners);
        addShelfRow(panel, 2, "Min", shopMinPricesSpinners);
        addShelfRow(panel, 3, "Max", shopMaxPricesSpinners);
        addShelfRow(panel, 4, "Items", shopMaxItemsSpinners);
        addShelfRow(panel, 5, "Same", shopMaxSameTypeItemsSpinners);
        return MapEditorCollapsiblePanel.collapsed("Shelves", panel);
    }

    /**
     * Java support helper for adding one shop shelf array row.
     * not ported.
     */
    private static void addShelfRow(JPanel panel, int row, String label, JSpinner[] spinners) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(2, 0, 2, 8);
        panel.add(new JLabel(label), labelConstraints);

        for (int shelf = 0; shelf < spinners.length; shelf++) {
            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = shelf + 1;
            fieldConstraints.gridy = row;
            fieldConstraints.weightx = 1.0;
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.insets = new Insets(2, shelf == 0 ? 0 : 4, 2, 0);
            panel.add(spinners[shelf], fieldConstraints);
        }
    }

    /**
     * Java support button-row construction for descriptor commands.
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
     * Java support helper for adding one descriptor form row.
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
     * Java support button construction helper for descriptor commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support integer spinner construction for descriptor fields.
     * not ported.
     */
    private static JSpinner integerSpinner(int value) {
        return new JSpinner(new SpinnerNumberModel(value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    }

    /**
     * Java support four-slot spinner-array construction for shop shelf fields.
     * not ported.
     */
    private static JSpinner[] integerSpinners() {
        JSpinner[] spinners = new JSpinner[SHOP_SHELF_COUNT];
        for (int i = 0; i < spinners.length; i++) {
            spinners[i] = integerSpinner(0);
        }
        return spinners;
    }

    /**
     * Java support inn-descriptor list refresh from the active document.
     * not ported.
     */
    private void refreshInnList(int previousSelection) {
        List<String> labels = new ArrayList<>();
        if (document != null) {
            for (int i = 0; i < document.innDescriptorCount(); i++) {
                labels.add(innLabel(document.innDescriptorAt(i)));
            }
        }
        innListModel.clear();
        innListModel.addAll(labels);
        selectListIndex(innList, innListModel, previousSelection);
    }

    /**
     * Java support shop-descriptor list refresh from the active document.
     * not ported.
     */
    private void refreshShopList(int previousSelection) {
        List<String> labels = new ArrayList<>();
        if (document != null) {
            for (int i = 0; i < document.shopDescriptorCount(); i++) {
                labels.add(shopLabel(document.shopDescriptorAt(i)));
            }
        }
        shopListModel.clear();
        shopListModel.addAll(labels);
        selectListIndex(shopList, shopListModel, previousSelection);
    }

    /**
     * Java support post-descriptor list refresh from the active document.
     * not ported.
     */
    private void refreshPostList(int previousSelection) {
        List<String> labels = new ArrayList<>();
        if (document != null) {
            for (int i = 0; i < document.postDescriptorCount(); i++) {
                labels.add(postLabel(document.postDescriptorAt(i)));
            }
        }
        postListModel.clear();
        postListModel.addAll(labels);
        selectListIndex(postList, postListModel, previousSelection);
    }

    /**
     * Java support list selection restoration after descriptor-list refresh.
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
     * Java support descriptor-subtab and row selection from shared map entity state.
     * not ported.
     */
    private void selectDescriptorIndex(int tabIndex, JList<String> list, int descriptorIndex) {
        descriptorTabs.setSelectedIndex(tabIndex);
        if (descriptorIndex < 0 || descriptorIndex >= list.getModel().getSize()) {
            list.clearSelection();
            return;
        }
        list.setSelectedIndex(descriptorIndex);
        list.ensureIndexIsVisible(descriptorIndex);
    }

    /**
     * Java support selection transfer from inn list row to editor controls.
     * not ported.
     */
    private void syncInnSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = innList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetInnFieldsForNewDescriptor();
            updateControlsEnabled();
            return;
        }

        InnDescriptor descriptor = document.innDescriptorAt(selectedIndex);
        innIdSpinner.setValue(descriptor.id);
        innFlagsSpinner.setValue(descriptor.flags);
        innItemIdSpinner.setValue(descriptor.itemID);
        updateControlsEnabled();
    }

    /**
     * Java support selection transfer from shop list row to editor controls.
     * not ported.
     */
    private void syncShopSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = shopList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetShopFieldsForNewDescriptor();
            updateControlsEnabled();
            return;
        }

        ShopDescriptor descriptor = document.shopDescriptorAt(selectedIndex);
        shopIdSpinner.setValue(descriptor.id);
        setSpinnerArrayValues(shopShelfFlagsSpinners, descriptor.shelfFlags);
        setSpinnerArrayValues(shopMinPricesSpinners, descriptor.minPrices);
        setSpinnerArrayValues(shopMaxPricesSpinners, descriptor.maxPrices);
        setSpinnerArrayValues(shopMaxItemsSpinners, descriptor.maxItems);
        setSpinnerArrayValues(shopMaxSameTypeItemsSpinners, descriptor.maxSameTypeItems);
        updateControlsEnabled();
    }

    /**
     * Java support selection transfer from post list row to editor controls.
     * not ported.
     */
    private void syncPostSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = postList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetPostFieldsForNewDescriptor();
            updateControlsEnabled();
            return;
        }

        PostDescriptor descriptor = document.postDescriptorAt(selectedIndex);
        postIdSpinner.setValue(descriptor.id);
        postInstanceOnSpinner.setValue(descriptor.instanceOn);
        postInstanceIdSpinner.setValue(descriptor.instanceID);
        updateControlsEnabled();
    }

    /**
     * Java support default field setup for adding a new inn descriptor.
     * not ported.
     */
    private void resetInnFieldsForNewDescriptor() {
        innIdSpinner.setValue(1);
        innFlagsSpinner.setValue(0);
        innItemIdSpinner.setValue(0);
    }

    /**
     * Java support default field setup for adding a new shop descriptor.
     * not ported.
     */
    private void resetShopFieldsForNewDescriptor() {
        shopIdSpinner.setValue(1);
        setSpinnerArrayValues(shopShelfFlagsSpinners, new int[SHOP_SHELF_COUNT]);
        setSpinnerArrayValues(shopMinPricesSpinners, new int[SHOP_SHELF_COUNT]);
        setSpinnerArrayValues(shopMaxPricesSpinners, new int[SHOP_SHELF_COUNT]);
        setSpinnerArrayValues(shopMaxItemsSpinners, new int[SHOP_SHELF_COUNT]);
        setSpinnerArrayValues(shopMaxSameTypeItemsSpinners, new int[SHOP_SHELF_COUNT]);
    }

    /**
     * Java support default field setup for adding a new post descriptor.
     * not ported.
     */
    private void resetPostFieldsForNewDescriptor() {
        postIdSpinner.setValue(1);
        postInstanceOnSpinner.setValue(0);
        postInstanceIdSpinner.setValue(0);
    }

    /**
     * Java support command for appending one scenario inn descriptor.
     * not ported.
     */
    private void addInnDescriptor() {
        if (document == null) {
            return;
        }
        try {
            int descriptorIndex = document.addInnDescriptor(
                    spinnerInt(innIdSpinner),
                    spinnerInt(innFlagsSpinner),
                    spinnerInt(innItemIdSpinner)
            );
            refreshFromDocument();
            innList.setSelectedIndex(descriptorIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add inn descriptor failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario inn descriptor.
     * not ported.
     */
    private void deleteInnDescriptor() {
        int selectedIndex = innList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteInnDescriptor(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete inn descriptor failed", exception);
        }
    }

    /**
     * Java support command for applying selected inn descriptor attributes.
     * not ported.
     */
    private void applyInnDescriptor() {
        int selectedIndex = innList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            int descriptorIndex = document.updateInnDescriptor(
                    selectedIndex,
                    spinnerInt(innIdSpinner),
                    spinnerInt(innFlagsSpinner),
                    spinnerInt(innItemIdSpinner)
            );
            refreshFromDocument();
            innList.setSelectedIndex(descriptorIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply inn descriptor failed", exception);
        }
    }

    /**
     * Java support command for appending one scenario shop descriptor.
     * not ported.
     */
    private void addShopDescriptor() {
        if (document == null) {
            return;
        }
        try {
            int descriptorIndex = document.addShopDescriptor(
                    spinnerInt(shopIdSpinner),
                    spinnerArrayValues(shopShelfFlagsSpinners),
                    spinnerArrayValues(shopMinPricesSpinners),
                    spinnerArrayValues(shopMaxPricesSpinners),
                    spinnerArrayValues(shopMaxItemsSpinners),
                    spinnerArrayValues(shopMaxSameTypeItemsSpinners)
            );
            refreshFromDocument();
            shopList.setSelectedIndex(descriptorIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add shop descriptor failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario shop descriptor.
     * not ported.
     */
    private void deleteShopDescriptor() {
        int selectedIndex = shopList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteShopDescriptor(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete shop descriptor failed", exception);
        }
    }

    /**
     * Java support command for applying selected shop descriptor attributes.
     * not ported.
     */
    private void applyShopDescriptor() {
        int selectedIndex = shopList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            int descriptorIndex = document.updateShopDescriptor(
                    selectedIndex,
                    spinnerInt(shopIdSpinner),
                    spinnerArrayValues(shopShelfFlagsSpinners),
                    spinnerArrayValues(shopMinPricesSpinners),
                    spinnerArrayValues(shopMaxPricesSpinners),
                    spinnerArrayValues(shopMaxItemsSpinners),
                    spinnerArrayValues(shopMaxSameTypeItemsSpinners)
            );
            refreshFromDocument();
            shopList.setSelectedIndex(descriptorIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply shop descriptor failed", exception);
        }
    }

    /**
     * Java support command for appending one scenario post descriptor.
     * not ported.
     */
    private void addPostDescriptor() {
        if (document == null) {
            return;
        }
        try {
            document.addPostDescriptor(
                    spinnerInt(postIdSpinner),
                    spinnerInt(postInstanceOnSpinner),
                    spinnerInt(postInstanceIdSpinner)
            );
            refreshFromDocument();
            postList.setSelectedIndex(document.postDescriptorCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add post descriptor failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario post descriptor.
     * not ported.
     */
    private void deletePostDescriptor() {
        int selectedIndex = postList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deletePostDescriptor(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete post descriptor failed", exception);
        }
    }

    /**
     * Java support command for applying selected post descriptor attributes.
     * not ported.
     */
    private void applyPostDescriptor() {
        int selectedIndex = postList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updatePostDescriptor(
                    selectedIndex,
                    spinnerInt(postIdSpinner),
                    spinnerInt(postInstanceOnSpinner),
                    spinnerInt(postInstanceIdSpinner)
            );
            refreshFromDocument();
            postList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply post descriptor failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to descriptor records.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasInnSelection = hasDocument && innList.getSelectedIndex() >= 0;
        boolean hasShopSelection = hasDocument && shopList.getSelectedIndex() >= 0;
        boolean hasPostSelection = hasDocument && postList.getSelectedIndex() >= 0;
        setInnControlsEnabled(hasDocument);
        setShopControlsEnabled(hasDocument);
        setPostControlsEnabled(hasDocument);
        addInnButton.setEnabled(hasDocument);
        deleteInnButton.setEnabled(hasInnSelection);
        applyInnButton.setEnabled(hasInnSelection);
        addShopButton.setEnabled(hasDocument);
        deleteShopButton.setEnabled(hasShopSelection);
        applyShopButton.setEnabled(hasShopSelection);
        addPostButton.setEnabled(hasDocument);
        deletePostButton.setEnabled(hasPostSelection);
        applyPostButton.setEnabled(hasPostSelection);
    }

    /**
     * Java support bulk enabled-state update for inn descriptor controls.
     * not ported.
     */
    private void setInnControlsEnabled(boolean enabled) {
        innIdSpinner.setEnabled(enabled);
        innFlagsSpinner.setEnabled(enabled);
        innItemIdSpinner.setEnabled(enabled);
    }

    /**
     * Java support bulk enabled-state update for shop descriptor controls.
     * not ported.
     */
    private void setShopControlsEnabled(boolean enabled) {
        shopIdSpinner.setEnabled(enabled);
        setSpinnerArrayEnabled(shopShelfFlagsSpinners, enabled);
        setSpinnerArrayEnabled(shopMinPricesSpinners, enabled);
        setSpinnerArrayEnabled(shopMaxPricesSpinners, enabled);
        setSpinnerArrayEnabled(shopMaxItemsSpinners, enabled);
        setSpinnerArrayEnabled(shopMaxSameTypeItemsSpinners, enabled);
    }

    /**
     * Java support bulk enabled-state update for post descriptor controls.
     * not ported.
     */
    private void setPostControlsEnabled(boolean enabled) {
        postIdSpinner.setEnabled(enabled);
        postInstanceOnSpinner.setEnabled(enabled);
        postInstanceIdSpinner.setEnabled(enabled);
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
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support selected-inn-descriptor notification back to the owning frame.
     * not ported.
     */
    private void notifyInnSelection() {
        notifyDescriptorSelection(MapEditorEntitySelection.Kind.INN_DESCRIPTOR, innList.getSelectedIndex());
    }

    /**
     * Java support selected-shop-descriptor notification back to the owning frame.
     * not ported.
     */
    private void notifyShopSelection() {
        notifyDescriptorSelection(MapEditorEntitySelection.Kind.SHOP_DESCRIPTOR, shopList.getSelectedIndex());
    }

    /**
     * Java support selected-pointer/post-descriptor notification back to the owning frame.
     * not ported.
     */
    private void notifyPostSelection() {
        notifyDescriptorSelection(MapEditorEntitySelection.Kind.POST_DESCRIPTOR, postList.getSelectedIndex());
    }

    /**
     * Java support selected-descriptor notification back to the owning frame.
     * not ported.
     */
    private void notifyDescriptorSelection(MapEditorEntitySelection.Kind kind, int selectedIndex) {
        if (refreshing || selectedIndex < 0) {
            return;
        }
        selectionListener.accept(new MapEditorEntitySelection(kind, selectedIndex));
    }

    /**
     * Java support list label for inn descriptors.
     * not ported.
     */
    private static String innLabel(InnDescriptor descriptor) {
        return "#" + descriptor.id + " flags " + descriptor.flags + " item " + descriptor.itemID;
    }

    /**
     * Java support list label for shop descriptors.
     * not ported.
     */
    private static String shopLabel(ShopDescriptor descriptor) {
        return "#" + descriptor.id + " shelves " + Arrays.toString(descriptor.shelfFlags);
    }

    /**
     * Java support list label for post descriptors.
     * not ported.
     */
    private static String postLabel(PostDescriptor descriptor) {
        return "#" + descriptor.id + " on " + descriptor.instanceOn + " instant " + descriptor.instanceID;
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support helper for reading a four-slot shop shelf array from controls.
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
     * Java support helper for copying descriptor array values into four spinner controls.
     * not ported.
     */
    private static void setSpinnerArrayValues(JSpinner[] spinners, int[] values) {
        for (int i = 0; i < spinners.length; i++) {
            spinners[i].setValue(values[i]);
        }
    }

    /**
     * Java support error dialog for descriptor-editor operations.
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
