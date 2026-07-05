package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.scenario.EffectDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectOrTrapMod;

import javax.swing.DefaultListModel;
import javax.swing.BoxLayout;
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
import java.awt.Point;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Swing effects/traps editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorEffectsPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int SCENARIO_EFFECT_BYTE_MAX = 0xFF;
    private static final int TRANSIENT_SPELL_CELL_MODE_MAX = 3;

    private final DefaultListModel<String> effectListModel = new DefaultListModel<>();
    private final JList<String> effectList = new JList<>(effectListModel);
    private final DefaultListModel<String> modifierListModel = new DefaultListModel<>();
    private final JList<String> modifierList = new JList<>(modifierListModel);
    private final JSpinner itemIdSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner effectModeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner spreadSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner spellIdSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner spellStrengthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner modifierTypeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner modifierValueSpinner = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private final JSpinner spellCellXSpinner = byteSpinner(0);
    private final JSpinner spellCellYSpinner = byteSpinner(0);
    private final JSpinner spellCellModeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, TRANSIENT_SPELL_CELL_MODE_MAX, 1));
    private final JSpinner spellCellSpellSpinner = byteSpinner(0);
    private final JSpinner spellCellStrengthSpinner = byteSpinner(0);
    private final JSpinner spellCellSourceXSpinner = byteSpinner(0);
    private final JSpinner spellCellSourceYSpinner = byteSpinner(0);
    private final JSpinner spellCellTargetXSpinner = byteSpinner(0);
    private final JSpinner spellCellTargetYSpinner = byteSpinner(0);
    private final JSpinner structureBuildingIdSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    private final JSpinner structureSourceXSpinner = byteSpinner(0);
    private final JSpinner structureSourceYSpinner = byteSpinner(0);
    private final JSpinner structureRadiusSpinner = byteSpinner(0);
    private final JSpinner structureSpellSpinner = byteSpinner(0);
    private final JSpinner structureStrengthSpinner = byteSpinner(0);
    private final JButton addSpellCellButton = button("Add Spell Cell", this::addTransientSpellCell);
    private final JButton addStructureCastingButton = button("Add Structure Casting", this::addStructureCasting);
    private final JButton addEffectButton = button("Add Effect", this::addEffect);
    private final JButton deleteEffectButton = button("Delete Effect", this::deleteEffect);
    private final JButton applyEffectButton = button("Apply Effect", this::applyEffect);
    private final JButton addModifierButton = button("Add Modifier", this::addModifier);
    private final JButton deleteModifierButton = button("Delete Modifier", this::deleteModifier);
    private final JButton applyModifierButton = button("Apply Modifier", this::applyModifier);

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor effects/traps panel.
     * not ported.
     */
    public MapEditorEffectsPanel() {
        super(new BorderLayout(6, 6));
        effectList.setVisibleRowCount(7);
        modifierList.setVisibleRowCount(7);
        effectList.setPrototypeCellValue("#00000 item 000000 @ 000,000 mode 000 spell 000 strength 000");
        modifierList.setPrototypeCellValue("#00 type 000000 value 000000");
        effectList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncEffectSelectionToFields();
                notifyEffectSelection();
            }
        });
        modifierList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncModifierSelectionToFields();
            }
        });

        add(createListPanel(), BorderLayout.CENTER);
        add(createEditorControls(), BorderLayout.SOUTH);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the effects/traps panel.
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
        int previousEffectSelection = effectList.getSelectedIndex();
        int previousModifierSelection = modifierList.getSelectedIndex();
        List<String> labels = new ArrayList<>();
        refreshing = true;
        if (document != null) {
            for (int i = 0; i < document.effectCount(); i++) {
                labels.add(effectLabel(i, document.effectAt(i)));
            }
        }
        effectListModel.clear();
        effectListModel.addAll(labels);
        if (effectListModel.isEmpty()) {
            effectList.clearSelection();
        } else {
            int effectIndex = Math.min(Math.max(previousEffectSelection, 0), effectListModel.size() - 1);
            effectList.setSelectedIndex(effectIndex);
        }
        refreshModifierList(previousModifierSelection);
        refreshing = false;

        syncEffectFieldsOnly();
        syncModifierSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selection hook used by viewport entity hit-testing.
     * not ported.
     */
    void selectEffectIndex(int effectIndex) {
        if (effectIndex < 0 || effectIndex >= effectListModel.size()) {
            return;
        }
        effectList.setSelectedIndex(effectIndex);
        effectList.ensureIndexIsVisible(effectIndex);
    }

    /**
     * Java support map-click coordinate binding for native-editor traps/structure-casting helpers.
     * not ported.
     */
    void setLogicHelperCell(Point cell) {
        if (cell == null) {
            return;
        }
        spellCellXSpinner.setValue(cell.x);
        spellCellYSpinner.setValue(cell.y);
        structureSourceXSpinner.setValue(cell.x);
        structureSourceYSpinner.setValue(cell.y);
    }

    /**
     * Java support split list construction for effects and their modifiers.
     * not ported.
     */
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 6, 0));
        panel.add(new JScrollPane(effectList));
        panel.add(new JScrollPane(modifierList));
        return panel;
    }

    /**
     * Java support aggregate editor controls construction.
     * not ported.
     */
    private JPanel createEditorControls() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 6, 0));
        panel.add(createEffectEditorControls());
        panel.add(createModifierEditorControls());
        return panel;
    }

    /**
     * Java support effect editor controls construction.
     * not ported.
     */
    private JPanel createEffectEditorControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel forms = new JPanel();
        forms.setLayout(new BoxLayout(forms, BoxLayout.Y_AXIS));
        forms.add(createTransientSpellCellPanel());
        forms.add(createStructureCastingPanel());
        forms.add(createEffectAttributesPanel());
        panel.add(forms, BorderLayout.CENTER);
        panel.add(createEffectButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support effect modifier editor controls construction.
     * not ported.
     */
    private JPanel createModifierEditorControls() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(createModifierAttributesPanel(), BorderLayout.CENTER);
        panel.add(createModifierButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support native-style transient spell/trap cell helper construction.
     * not ported.
     */
    private JPanel createTransientSpellCellPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "Cell X", spellCellXSpinner);
        addField(panel, 0, 1, "Cell Y", spellCellYSpinner);
        addField(panel, 0, 2, "Mode", spellCellModeSpinner);
        addField(panel, 1, 0, "Spell", spellCellSpellSpinner);
        addField(panel, 1, 1, "Strength", spellCellStrengthSpinner);
        addField(panel, 2, 0, "Source X", spellCellSourceXSpinner);
        addField(panel, 2, 1, "Source Y", spellCellSourceYSpinner);
        addField(panel, 3, 0, "Target X", spellCellTargetXSpinner);
        addField(panel, 3, 1, "Target Y", spellCellTargetYSpinner);
        addWideButton(panel, 4, addSpellCellButton);
        return MapEditorCollapsiblePanel.collapsed("Spell Cell / Trap", panel);
    }

    /**
     * Java support native-style building virtual-caster helper construction.
     * not ported.
     */
    private JPanel createStructureCastingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "Building", structureBuildingIdSpinner);
        addField(panel, 0, 1, "Source X", structureSourceXSpinner);
        addField(panel, 0, 2, "Source Y", structureSourceYSpinner);
        addField(panel, 1, 0, "Radius", structureRadiusSpinner);
        addField(panel, 1, 1, "Spell", structureSpellSpinner);
        addField(panel, 1, 2, "Strength", structureStrengthSpinner);
        addWideButton(panel, 2, addStructureCastingButton);
        return MapEditorCollapsiblePanel.collapsed("Structure Casting", panel);
    }

    /**
     * Java support effect attribute form construction.
     * not ported.
     */
    private JPanel createEffectAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "Item", itemIdSpinner);
        addField(panel, 1, 0, "X", xSpinner);
        addField(panel, 2, 0, "Y", ySpinner);
        addField(panel, 3, 0, "Mode", effectModeSpinner);
        addField(panel, 0, 1, "Min", minSpinner);
        addField(panel, 1, 1, "Spread", spreadSpinner);
        addField(panel, 2, 1, "Spell", spellIdSpinner);
        addField(panel, 3, 1, "Strength", spellStrengthSpinner);
        return MapEditorCollapsiblePanel.collapsed("Effect / Trap", panel);
    }

    /**
     * Java support modifier attribute form construction.
     * not ported.
     */
    private JPanel createModifierAttributesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, 0, "Type", modifierTypeSpinner);
        addField(panel, 1, 0, "Value", modifierValueSpinner);
        return MapEditorCollapsiblePanel.collapsed("Modifier", panel);
    }

    /**
     * Java support helper-command button placement across a helper form.
     * not ported.
     */
    private static void addWideButton(JPanel panel, int row, JButton button) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 6;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(3, 0, 0, 0);
        panel.add(button, constraints);
    }

    /**
     * Java support button-row construction for effect commands.
     * not ported.
     */
    private JPanel createEffectButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = commandButtonConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(addEffectButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteEffectButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyEffectButton, constraints);
        return panel;
    }

    /**
     * Java support button-row construction for modifier commands.
     * not ported.
     */
    private JPanel createModifierButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = commandButtonConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(addModifierButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteModifierButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyModifierButton, constraints);
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
     * Java support button construction helper for effect commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support modifier-list refresh for the currently selected effect.
     * not ported.
     */
    private void refreshModifierList(int selectedModifierIndex) {
        List<String> labels = new ArrayList<>();
        int effectIndex = effectList.getSelectedIndex();
        if (document != null && effectIndex >= 0) {
            EffectDTO effect = document.effectAt(effectIndex);
            for (int i = 0; i < document.effectModifierCount(effectIndex); i++) {
                labels.add(modifierLabel(i, effect.carr.get(i)));
            }
        }
        modifierListModel.clear();
        modifierListModel.addAll(labels);
        if (modifierListModel.isEmpty()) {
            modifierList.clearSelection();
        } else {
            int modifierIndex = Math.min(Math.max(selectedModifierIndex, 0), modifierListModel.size() - 1);
            modifierList.setSelectedIndex(modifierIndex);
        }
    }

    /**
     * Java support selection transfer from effect list row to editor controls.
     * not ported.
     */
    private void syncEffectSelectionToFields() {
        if (refreshing) {
            return;
        }
        syncEffectFieldsOnly();
        refreshModifierList(0);
        syncModifierSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selected-effect field refresh without changing modifier rows.
     * not ported.
     */
    private void syncEffectFieldsOnly() {
        int selectedIndex = effectList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            resetEffectFieldsForNewEffect();
            return;
        }

        EffectDTO effect = document.effectAt(selectedIndex);
        itemIdSpinner.setValue(effect.itemID);
        xSpinner.setValue(effect.x);
        ySpinner.setValue(effect.y);
        effectModeSpinner.setValue(effect.effectMode);
        minSpinner.setValue(effect.min);
        spreadSpinner.setValue(effect.spread);
        spellIdSpinner.setValue(effect.spellId);
        spellStrengthSpinner.setValue(effect.spellStrength);
    }

    /**
     * Java support selection transfer from modifier list row to editor controls.
     * not ported.
     */
    private void syncModifierSelectionToFields() {
        if (refreshing) {
            return;
        }
        int effectIndex = effectList.getSelectedIndex();
        int modifierIndex = modifierList.getSelectedIndex();
        if (document == null || effectIndex < 0 || modifierIndex < 0) {
            resetModifierFieldsForNewModifier();
            updateControlsEnabled();
            return;
        }

        EffectOrTrapMod modifier = document.effectAt(effectIndex).carr.get(modifierIndex);
        modifierTypeSpinner.setValue(modifier.type);
        modifierValueSpinner.setValue(modifier.value);
        updateControlsEnabled();
    }

    /**
     * Java support default field setup for adding a new effect record.
     * not ported.
     */
    private void resetEffectFieldsForNewEffect() {
        itemIdSpinner.setValue(0);
        xSpinner.setValue(0);
        ySpinner.setValue(0);
        effectModeSpinner.setValue(0);
        minSpinner.setValue(0);
        spreadSpinner.setValue(0);
        spellIdSpinner.setValue(0);
        spellStrengthSpinner.setValue(0);
    }

    /**
     * Java support default field setup for adding a new effect modifier.
     * not ported.
     */
    private void resetModifierFieldsForNewModifier() {
        modifierTypeSpinner.setValue(0);
        modifierValueSpinner.setValue(0);
    }

    /**
     * Java support command for appending one native-style transient spell/trap cell effect.
     * not ported.
     */
    private void addTransientSpellCell() {
        if (document == null) {
            return;
        }
        try {
            document.addTransientSpellCell(
                    spinnerInt(spellCellXSpinner),
                    spinnerInt(spellCellYSpinner),
                    spinnerInt(spellCellModeSpinner),
                    spinnerInt(spellCellSpellSpinner),
                    spinnerInt(spellCellStrengthSpinner),
                    spinnerInt(spellCellSourceXSpinner),
                    spinnerInt(spellCellSourceYSpinner),
                    spinnerInt(spellCellTargetXSpinner),
                    spinnerInt(spellCellTargetYSpinner)
            );
            refreshFromDocument();
            effectList.setSelectedIndex(document.effectCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add spell cell failed", exception);
        }
    }

    /**
     * Java support command for appending one native-style structure-casting effect.
     * not ported.
     */
    private void addStructureCasting() {
        if (document == null) {
            return;
        }
        try {
            document.addStructureCasting(
                    spinnerInt(structureBuildingIdSpinner),
                    spinnerInt(structureSourceXSpinner),
                    spinnerInt(structureSourceYSpinner),
                    spinnerInt(structureRadiusSpinner),
                    spinnerInt(structureSpellSpinner),
                    spinnerInt(structureStrengthSpinner)
            );
            refreshFromDocument();
            effectList.setSelectedIndex(document.effectCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add structure casting failed", exception);
        }
    }

    /**
     * Java support command for appending one scenario effect.
     * not ported.
     */
    private void addEffect() {
        if (document == null) {
            return;
        }
        try {
            document.addEffect(
                    spinnerInt(itemIdSpinner),
                    spinnerInt(xSpinner),
                    spinnerInt(ySpinner),
                    spinnerInt(effectModeSpinner),
                    spinnerInt(minSpinner),
                    spinnerInt(spreadSpinner),
                    spinnerInt(spellIdSpinner),
                    spinnerInt(spellStrengthSpinner)
            );
            refreshFromDocument();
            effectList.setSelectedIndex(document.effectCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add effect failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected scenario effect.
     * not ported.
     */
    private void deleteEffect() {
        int selectedIndex = effectList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteEffect(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete effect failed", exception);
        }
    }

    /**
     * Java support command for applying selected effect attributes.
     * not ported.
     */
    private void applyEffect() {
        int selectedIndex = effectList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateEffect(
                    selectedIndex,
                    spinnerInt(itemIdSpinner),
                    spinnerInt(xSpinner),
                    spinnerInt(ySpinner),
                    spinnerInt(effectModeSpinner),
                    spinnerInt(minSpinner),
                    spinnerInt(spreadSpinner),
                    spinnerInt(spellIdSpinner),
                    spinnerInt(spellStrengthSpinner)
            );
            refreshFromDocument();
            effectList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply effect failed", exception);
        }
    }

    /**
     * Java support command for appending one selected-effect modifier.
     * not ported.
     */
    private void addModifier() {
        int effectIndex = effectList.getSelectedIndex();
        if (document == null || effectIndex < 0) {
            return;
        }
        try {
            document.addEffectModifier(
                    effectIndex,
                    spinnerInt(modifierTypeSpinner),
                    spinnerInt(modifierValueSpinner)
            );
            refreshFromDocument();
            effectList.setSelectedIndex(effectIndex);
            modifierList.setSelectedIndex(document.effectModifierCount(effectIndex) - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add modifier failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected effect modifier.
     * not ported.
     */
    private void deleteModifier() {
        int effectIndex = effectList.getSelectedIndex();
        int modifierIndex = modifierList.getSelectedIndex();
        if (document == null || effectIndex < 0 || modifierIndex < 0) {
            return;
        }
        try {
            document.deleteEffectModifier(effectIndex, modifierIndex);
            refreshFromDocument();
            effectList.setSelectedIndex(effectIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete modifier failed", exception);
        }
    }

    /**
     * Java support command for applying selected effect modifier attributes.
     * not ported.
     */
    private void applyModifier() {
        int effectIndex = effectList.getSelectedIndex();
        int modifierIndex = modifierList.getSelectedIndex();
        if (document == null || effectIndex < 0 || modifierIndex < 0) {
            return;
        }
        try {
            document.updateEffectModifier(
                    effectIndex,
                    modifierIndex,
                    spinnerInt(modifierTypeSpinner),
                    spinnerInt(modifierValueSpinner)
            );
            refreshFromDocument();
            effectList.setSelectedIndex(effectIndex);
            modifierList.setSelectedIndex(modifierIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply modifier failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to selected effect and modifier records.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasEffectSelection = hasDocument && effectList.getSelectedIndex() >= 0;
        boolean hasModifierSelection = hasEffectSelection && modifierList.getSelectedIndex() >= 0;
        setHelperControlsEnabled(hasDocument);
        setEffectControlsEnabled(hasDocument);
        setModifierControlsEnabled(hasEffectSelection);
        addSpellCellButton.setEnabled(hasDocument);
        addStructureCastingButton.setEnabled(hasDocument);
        addEffectButton.setEnabled(hasDocument);
        deleteEffectButton.setEnabled(hasEffectSelection);
        applyEffectButton.setEnabled(hasEffectSelection);
        addModifierButton.setEnabled(hasEffectSelection);
        deleteModifierButton.setEnabled(hasModifierSelection);
        applyModifierButton.setEnabled(hasModifierSelection);
    }

    /**
     * Java support bulk enabled-state update for effect form controls.
     * not ported.
     */
    private void setEffectControlsEnabled(boolean enabled) {
        itemIdSpinner.setEnabled(enabled);
        xSpinner.setEnabled(enabled);
        ySpinner.setEnabled(enabled);
        effectModeSpinner.setEnabled(enabled);
        minSpinner.setEnabled(enabled);
        spreadSpinner.setEnabled(enabled);
        spellIdSpinner.setEnabled(enabled);
        spellStrengthSpinner.setEnabled(enabled);
    }

    /**
     * Java support bulk enabled-state update for native-style helper controls.
     * not ported.
     */
    private void setHelperControlsEnabled(boolean enabled) {
        spellCellXSpinner.setEnabled(enabled);
        spellCellYSpinner.setEnabled(enabled);
        spellCellModeSpinner.setEnabled(enabled);
        spellCellSpellSpinner.setEnabled(enabled);
        spellCellStrengthSpinner.setEnabled(enabled);
        spellCellSourceXSpinner.setEnabled(enabled);
        spellCellSourceYSpinner.setEnabled(enabled);
        spellCellTargetXSpinner.setEnabled(enabled);
        spellCellTargetYSpinner.setEnabled(enabled);
        structureBuildingIdSpinner.setEnabled(enabled);
        structureSourceXSpinner.setEnabled(enabled);
        structureSourceYSpinner.setEnabled(enabled);
        structureRadiusSpinner.setEnabled(enabled);
        structureSpellSpinner.setEnabled(enabled);
        structureStrengthSpinner.setEnabled(enabled);
    }

    /**
     * Java support bulk enabled-state update for modifier form controls.
     * not ported.
     */
    private void setModifierControlsEnabled(boolean enabled) {
        modifierTypeSpinner.setEnabled(enabled);
        modifierValueSpinner.setEnabled(enabled);
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support selected-effect notification back to the owning frame.
     * not ported.
     */
    private void notifyEffectSelection() {
        int selectedIndex = effectList.getSelectedIndex();
        if (refreshing || selectedIndex < 0) {
            return;
        }
        selectionListener.accept(new MapEditorEntitySelection(MapEditorEntitySelection.Kind.EFFECT, selectedIndex));
    }

    /**
     * Java support list label for scenario effects.
     * not ported.
     */
    private static String effectLabel(int effectIndex, EffectDTO effect) {
        return "#" + (effectIndex + 1)
                + " @ " + effect.x + "," + effect.y
                + " mode " + effect.effectMode
                + " spell " + effect.spellId + "/" + effect.spellStrength
                + " item " + effect.itemID
                + " mods " + effect.carr.size();
    }

    /**
     * Java support list label for scenario effect modifiers.
     * not ported.
     */
    private static String modifierLabel(int modifierIndex, EffectOrTrapMod modifier) {
        return "#" + modifierIndex + " type " + modifier.type + " value " + modifier.value;
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support byte-domain spinner factory for EFFECTS helper fields consumed through `& 0xFF`.
     * not ported.
     */
    private static JSpinner byteSpinner(int value) {
        return new JSpinner(new SpinnerNumberModel(value, 0, SCENARIO_EFFECT_BYTE_MAX, 1));
    }

    /**
     * Java support error dialog for effects/traps editor operations.
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
