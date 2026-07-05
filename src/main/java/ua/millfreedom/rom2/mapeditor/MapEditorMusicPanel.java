package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.scenario.MusicDTO;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Swing interactive/default music editor panel for standalone MapEditor documents.
 * not ported.
 */
public final class MapEditorMusicPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final MapEditorMusicRecordControls defaultControls = new MapEditorMusicRecordControls("Default Music");
    private final MapEditorMusicRecordControls zoneControls = new MapEditorMusicRecordControls("Music Area");
    private final DefaultListModel<String> musicZoneListModel = new DefaultListModel<>();
    private final JList<String> musicZoneList = new JList<>(musicZoneListModel);
    private final JButton applyDefaultButton = button("Apply Default", this::applyDefaultMusic);
    private final JButton addZoneButton = button("Add Area", this::addMusicZone);
    private final JButton deleteZoneButton = button("Delete Area", this::deleteMusicZone);
    private final JButton applyZoneButton = button("Apply Area", this::applyMusicZone);

    private MapEditorDocument document;
    private Runnable refreshListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private boolean refreshing;

    /**
     * Java support constructor for the editor music panel.
     * not ported.
     */
    public MapEditorMusicPanel() {
        super(new BorderLayout(6, 6));
        musicZoneList.setVisibleRowCount(6);
        musicZoneList.setPrototypeCellValue("#000 @ 000,000 000x000 song 000 mode 000");
        musicZoneList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                syncSelectionToFields();
                notifyMusicZoneSelection();
            }
        });

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createDefaultPanel(),
                createZonePanel()
        );
        splitPane.setResizeWeight(0.35d);
        add(splitPane, BorderLayout.CENTER);
        updateControlsEnabled();
    }

    /**
     * Java support document binding for the music panel.
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
        int previousSelection = musicZoneList.getSelectedIndex();
        List<String> labels = new ArrayList<>();
        refreshing = true;
        if (document != null) {
            defaultControls.setValues(document.defaultMusic());
            for (int i = 0; i < document.musicZoneCount(); i++) {
                labels.add(musicLabel(document.musicZoneAt(i)));
            }
        } else {
            defaultControls.clear();
        }
        musicZoneListModel.clear();
        musicZoneListModel.addAll(labels);

        if (musicZoneListModel.isEmpty()) {
            musicZoneList.clearSelection();
            zoneControls.clear();
        } else {
            int selectedIndex = Math.min(Math.max(previousSelection, 0), musicZoneListModel.size() - 1);
            musicZoneList.setSelectedIndex(selectedIndex);
        }
        refreshing = false;
        syncSelectionToFields();
        updateControlsEnabled();
    }

    /**
     * Java support selection hook used by viewport entity hit-testing.
     * not ported.
     */
    void selectMusicZoneIndex(int musicZoneIndex) {
        if (musicZoneIndex < 0 || musicZoneIndex >= musicZoneListModel.size()) {
            return;
        }
        musicZoneList.setSelectedIndex(musicZoneIndex);
        musicZoneList.ensureIndexIsVisible(musicZoneIndex);
    }

    /**
     * Java support default music control panel construction.
     * not ported.
     */
    private JPanel createDefaultPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(defaultControls, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(3, 2, 0, 2);
        buttons.add(applyDefaultButton, constraints);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Java support interactive music-area control panel construction.
     * not ported.
     */
    private JPanel createZonePanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(musicZoneList), BorderLayout.CENTER);
        panel.add(zoneControls, BorderLayout.NORTH);
        panel.add(createZoneButtonPanel(), BorderLayout.SOUTH);
        return MapEditorCollapsiblePanel.collapsed("Interactive Areas", panel);
    }

    /**
     * Java support button-row construction for music-area commands.
     * not ported.
     */
    private JPanel createZoneButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 2, 0, 2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(addZoneButton, constraints);
        constraints.gridx = 1;
        panel.add(deleteZoneButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(applyZoneButton, constraints);
        return panel;
    }

    /**
     * Java support selection transfer from list row to music-area controls.
     * not ported.
     */
    private void syncSelectionToFields() {
        if (refreshing) {
            return;
        }
        int selectedIndex = musicZoneList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            if (document != null) {
                zoneControls.setValues(0, 0, 1, -1, -1, -1, -1);
            } else {
                zoneControls.clear();
            }
            updateControlsEnabled();
            return;
        }
        zoneControls.setValues(document.musicZoneAt(selectedIndex));
        updateControlsEnabled();
    }

    /**
     * Java support command for applying default map music.
     * not ported.
     */
    private void applyDefaultMusic() {
        if (document == null) {
            return;
        }
        try {
            document.updateDefaultMusic(
                    defaultControls.x(),
                    defaultControls.y(),
                    defaultControls.radius(),
                    defaultControls.m1(),
                    defaultControls.m2(),
                    defaultControls.m3(),
                    defaultControls.m4()
            );
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply default music failed", exception);
        }
    }

    /**
     * Java support command for appending one interactive music area.
     * not ported.
     */
    private void addMusicZone() {
        if (document == null) {
            return;
        }
        try {
            document.addMusicZone(
                    zoneControls.x(),
                    zoneControls.y(),
                    zoneControls.radius(),
                    zoneControls.m1(),
                    zoneControls.m2(),
                    zoneControls.m3(),
                    zoneControls.m4()
            );
            refreshFromDocument();
            musicZoneList.setSelectedIndex(document.musicZoneCount() - 1);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Add music area failed", exception);
        }
    }

    /**
     * Java support command for deleting the selected interactive music area.
     * not ported.
     */
    private void deleteMusicZone() {
        int selectedIndex = musicZoneList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.deleteMusicZone(selectedIndex);
            refreshFromDocument();
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Delete music area failed", exception);
        }
    }

    /**
     * Java support command for applying selected interactive music area fields.
     * not ported.
     */
    private void applyMusicZone() {
        int selectedIndex = musicZoneList.getSelectedIndex();
        if (document == null || selectedIndex < 0) {
            return;
        }
        try {
            document.updateMusicZone(
                    selectedIndex,
                    zoneControls.x(),
                    zoneControls.y(),
                    zoneControls.radius(),
                    zoneControls.m1(),
                    zoneControls.m2(),
                    zoneControls.m3(),
                    zoneControls.m4()
            );
            refreshFromDocument();
            musicZoneList.setSelectedIndex(selectedIndex);
            notifyDocumentChanged();
        } catch (RuntimeException exception) {
            showError("Apply music area failed", exception);
        }
    }

    /**
     * Java support enabled-state refresh for controls bound to selected music records.
     * not ported.
     */
    private void updateControlsEnabled() {
        boolean hasDocument = document != null;
        boolean hasZoneSelection = hasDocument && musicZoneList.getSelectedIndex() >= 0;
        defaultControls.setControlsEnabled(hasDocument);
        zoneControls.setControlsEnabled(hasDocument);
        applyDefaultButton.setEnabled(hasDocument);
        addZoneButton.setEnabled(hasDocument);
        deleteZoneButton.setEnabled(hasZoneSelection);
        applyZoneButton.setEnabled(hasZoneSelection);
    }

    /**
     * Java support dirty-document notification back to the owning frame.
     * not ported.
     */
    private void notifyDocumentChanged() {
        refreshListener.run();
    }

    /**
     * Java support selected-music-area notification back to the owning frame.
     * not ported.
     */
    private void notifyMusicZoneSelection() {
        int selectedIndex = musicZoneList.getSelectedIndex();
        if (refreshing || selectedIndex < 0) {
            return;
        }
        selectionListener.accept(new MapEditorEntitySelection(MapEditorEntitySelection.Kind.MUSIC, selectedIndex));
    }

    /**
     * Java support button construction helper for music commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support list label for interactive music areas.
     * not ported.
     */
    private static String musicLabel(MusicDTO music) {
        return "x " + music.x
                + " y " + music.y
                + " r " + music.radius
                + " tracks " + music.m1 + "," + music.m2 + "," + music.m3 + "," + music.m4;
    }

    /**
     * Java support error dialog for music-panel commands.
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
