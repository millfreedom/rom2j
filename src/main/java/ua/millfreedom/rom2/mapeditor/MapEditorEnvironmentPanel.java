package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.ScenarioDescriptor;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;

/**
 * Swing editor panel for INFO-section environment and light settings.
 * not ported.
 */
public final class MapEditorEnvironmentPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DAY_MINUTES = 24 * 60;

    private final JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, DAY_MINUTES - 1, 10));
    private final JSpinner brightnessSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFF, 1));
    private final JSpinner contrastSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFF, 1));
    private final JSpinner tileMaskSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
    private final JSpinner sunAngleSpinner = new JSpinner(new SpinnerNumberModel(0.0d, -360.0d, 360.0d, 1.0d));
    private final JButton applyButton = new JButton("Apply Environment");

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };

    /**
     * Java support constructor for the editor environment panel.
     * not ported.
     */
    public MapEditorEnvironmentPanel() {
        super(new BorderLayout(6, 6));
        JPanel form = new JPanel(new GridBagLayout());
        addField(form, 0, "Time", timeSpinner);
        addField(form, 1, "Brightness", brightnessSpinner);
        addField(form, 2, "Contrast", contrastSpinner);
        addField(form, 3, "Solar Angle", sunAngleSpinner);
        addField(form, 4, "Tile Mask", tileMaskSpinner);
        applyButton.addActionListener(event -> applyEnvironment());
        add(MapEditorCollapsiblePanel.collapsed("Environment", form), BorderLayout.CENTER);
        add(applyButton, BorderLayout.SOUTH);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the environment panel.
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
        if (document == null) {
            clearFields();
            updateControlsEnabled();
            return;
        }

        ScenarioDescriptor scenario = document.scenario();
        timeSpinner.setValue(Math.floorMod(scenario.time, DAY_MINUTES));
        brightnessSpinner.setValue(clampByteField(scenario.darkness));
        contrastSpinner.setValue(clampByteField(scenario.contrast));
        tileMaskSpinner.setValue(Math.max(0, Math.min(0xFFFF, scenario.useTiles)));
        sunAngleSpinner.setValue(Math.toDegrees(scenario.sunAngle));
        updateControlsEnabled();
    }

    /**
     * Java support reset for an unbound environment panel.
     * not ported.
     */
    private void clearFields() {
        timeSpinner.setValue(0);
        brightnessSpinner.setValue(0);
        contrastSpinner.setValue(0);
        tileMaskSpinner.setValue(0);
        sunAngleSpinner.setValue(0.0d);
    }

    /**
     * Java support command for applying INFO-section environment fields.
     * not ported.
     */
    private void applyEnvironment() {
        if (document == null) {
            return;
        }
        try {
            document.applyEnvironmentSettings(
                    spinnerInt(timeSpinner),
                    spinnerInt(brightnessSpinner),
                    spinnerInt(contrastSpinner),
                    spinnerInt(tileMaskSpinner),
                    spinnerDouble(sunAngleSpinner)
            );
            refreshListener.run();
        } catch (RuntimeException exception) {
            showError("Apply environment failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to the current document.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        timeSpinner.setEnabled(hasDocument);
        brightnessSpinner.setEnabled(hasDocument);
        contrastSpinner.setEnabled(hasDocument);
        tileMaskSpinner.setEnabled(hasDocument);
        sunAngleSpinner.setEnabled(hasDocument);
        applyButton.setEnabled(hasDocument);
    }

    /**
     * Java support helper for adding one environment form row.
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
     * Java support helper for displaying byte-sized loaded fields in constrained spinners.
     * not ported.
     */
    private static int clampByteField(int value) {
        return Math.max(0, Math.min(0xFF, value));
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support helper for reading decimal spinner values.
     * not ported.
     */
    private static double spinnerDouble(JSpinner spinner) {
        return ((Number) spinner.getValue()).doubleValue();
    }

    /**
     * Java support error dialog for environment-panel commands.
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
