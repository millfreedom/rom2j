package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.scenario.MusicDTO;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;

/**
 * Compact Swing form for one MusicDTO-shaped editor record.
 * not ported.
 */
final class MapEditorMusicRecordControls extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final JSpinner xSpinner = intSpinner(0, 0, Integer.MAX_VALUE, 1);
    private final JSpinner ySpinner = intSpinner(0, 0, Integer.MAX_VALUE, 1);
    private final JSpinner radiusSpinner = intSpinner(0, 0, Integer.MAX_VALUE, 1);
    private final JSpinner m1Spinner = intSpinner(-1, -1, Integer.MAX_VALUE, 1);
    private final JSpinner m2Spinner = intSpinner(-1, -1, Integer.MAX_VALUE, 1);
    private final JSpinner m3Spinner = intSpinner(-1, -1, Integer.MAX_VALUE, 1);
    private final JSpinner m4Spinner = intSpinner(-1, -1, Integer.MAX_VALUE, 1);

    /**
     * Java support constructor for one music record control group.
     * not ported.
     */
    MapEditorMusicRecordControls(String title) {
        super(new BorderLayout());
        JPanel fields = new JPanel(new GridBagLayout());
        addField(fields, 0, "X", xSpinner);
        addField(fields, 1, "Y", ySpinner);
        addField(fields, 2, "Radius", radiusSpinner);
        addField(fields, 3, "Track 1", m1Spinner);
        addField(fields, 4, "Track 2", m2Spinner);
        addField(fields, 5, "Track 3", m3Spinner);
        addField(fields, 6, "Track 4", m4Spinner);
        add(MapEditorCollapsiblePanel.collapsed(title, fields), BorderLayout.CENTER);
    }

    /**
     * Java support value transfer from one MusicDTO record.
     * not ported.
     */
    void setValues(MusicDTO music) {
        setValues(music.x, music.y, music.radius, music.m1, music.m2, music.m3, music.m4);
    }

    /**
     * Java support direct value assignment for one music record control group.
     * not ported.
     */
    void setValues(int x, int y, int radius, int m1, int m2, int m3, int m4) {
        xSpinner.setValue(x);
        ySpinner.setValue(y);
        radiusSpinner.setValue(radius);
        m1Spinner.setValue(m1);
        m2Spinner.setValue(m2);
        m3Spinner.setValue(m3);
        m4Spinner.setValue(m4);
    }

    /**
     * Java support reset for an unbound music record control group.
     * not ported.
     */
    void clear() {
        setValues(0, 0, 0, -1, -1, -1, -1);
    }

    /**
     * Java support enabled-state update for all music record controls.
     * not ported.
     */
    void setControlsEnabled(boolean enabled) {
        xSpinner.setEnabled(enabled);
        ySpinner.setEnabled(enabled);
        radiusSpinner.setEnabled(enabled);
        m1Spinner.setEnabled(enabled);
        m2Spinner.setEnabled(enabled);
        m3Spinner.setEnabled(enabled);
        m4Spinner.setEnabled(enabled);
    }

    /**
     * Java support accessor for the x coordinate spinner.
     * not ported.
     */
    int x() {
        return spinnerInt(xSpinner);
    }

    /**
     * Java support accessor for the y coordinate spinner.
     * not ported.
     */
    int y() {
        return spinnerInt(ySpinner);
    }

    /**
     * Java support accessor for the radius spinner.
     * not ported.
     */
    int radius() {
        return spinnerInt(radiusSpinner);
    }

    /**
     * Java support accessor for the first track spinner.
     * not ported.
     */
    int m1() {
        return spinnerInt(m1Spinner);
    }

    /**
     * Java support accessor for the second track spinner.
     * not ported.
     */
    int m2() {
        return spinnerInt(m2Spinner);
    }

    /**
     * Java support accessor for the third track spinner.
     * not ported.
     */
    int m3() {
        return spinnerInt(m3Spinner);
    }

    /**
     * Java support accessor for the fourth track spinner.
     * not ported.
     */
    int m4() {
        return spinnerInt(m4Spinner);
    }

    /**
     * Java support helper for adding one music form row.
     * not ported.
     */
    private static void addField(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(2, 0, 2, 8);
        panel.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(2, 0, 2, 0);
        panel.add(component, fieldConstraints);
    }

    /**
     * Java support integer spinner construction helper.
     * not ported.
     */
    private static JSpinner intSpinner(int value, int minimum, int maximum, int step) {
        return new JSpinner(new SpinnerNumberModel(value, minimum, maximum, step));
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }
}
