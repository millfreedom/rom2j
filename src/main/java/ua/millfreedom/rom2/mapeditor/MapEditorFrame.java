package ua.millfreedom.rom2.mapeditor;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;

/**
 * Standalone Swing frame for MapEditor document operations.
 * not ported.
 */
public final class MapEditorFrame extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    private final MapPreviewPanel previewPanel = new MapPreviewPanel();
    private final JScrollPane mapScrollPane = new JScrollPane(previewPanel);
    private final MapMiniMapPanel miniMapPanel = new MapMiniMapPanel(previewPanel);
    private final JTextArea summaryText = new JTextArea(8, 34);
    private final JLabel cellStatusLabel = new JLabel();
    private final JTextField mapNameField = new JTextField();
    private final JTextField authorsField = new JTextField();
    private final JSpinner recommendedPlayersSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 16, 1));
    private final JSpinner mapLevelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final MapEditorEnvironmentPanel environmentPanel = new MapEditorEnvironmentPanel();
    private final MapEditorPlayersPanel playersPanel = new MapEditorPlayersPanel();
    private final MapEditorObjectsPanel objectsPanel = new MapEditorObjectsPanel();
    private final MapEditorBuildingsPanel buildingsPanel = new MapEditorBuildingsPanel();
    private final MapEditorUnitsPanel unitsPanel = new MapEditorUnitsPanel();
    private final MapEditorSacksPanel sacksPanel = new MapEditorSacksPanel();
    private final MapEditorEffectsPanel effectsPanel = new MapEditorEffectsPanel();
    private final MapEditorDescriptorsPanel descriptorsPanel = new MapEditorDescriptorsPanel();
    private final MapEditorGroupsPanel groupsPanel = new MapEditorGroupsPanel();
    private final MapEditorLogicPanel logicPanel = new MapEditorLogicPanel();
    private final MapEditorMusicPanel musicPanel = new MapEditorMusicPanel();
    private final JTabbedPane inspectorTabs = new JTabbedPane();
    private final JComboBox<MapEditorToolMode> toolModeCombo = new JComboBox<>(MapEditorToolMode.values());
    private final JSpinner tileSpinner = new JSpinner(new SpinnerNumberModel(0x11, 0, 0xFFFF, 1));
    private final JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(0x3F, 0, 0xFF, 1));
    private final JSpinner altitudeSpreadSpinner = new JSpinner(new SpinnerNumberModel(8, 0, 0xFF, 1));
    private final JSpinner objectSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFF, 1));
    private final JButton newButton = button("New", this::showNewMapDialog);
    private final JButton openButton = button("Open", this::loadMap);
    private final JButton closeButton = button("Close", this::closeMap);
    private final JButton saveAsButton = button("Save As", this::saveMapAs);
    private final JButton applyMetadataButton = button("Apply Metadata", this::applyMetadata);
    private final JButton undoButton = button("Undo", this::undoEdit);
    private final JButton redoButton = button("Redo", this::redoEdit);
    private final JButton cutAreaButton = button("Cut", this::cutSelectedArea);
    private final JButton copyAreaButton = button("Copy", this::copySelectedArea);
    private final JButton pasteAreaButton = button("Paste", this::pasteAreaAtCurrentCell);
    private final JButton randomTerrainButton = button("Random Tile", this::randomizeTerrain);
    private final JButton randomAltitudeButton = button("Random Height", this::randomizeAltitude);
    private final JButton randomMapButton = button("Random Map", this::randomizeMap);
    private final JButton raiseMapAltitudeButton = button("Map Up", this::raiseMapAltitude);
    private final JButton lowerMapAltitudeButton = button("Map Down", this::lowerMapAltitude);
    private final JMenuItem closeMenuItem = menuItem("Close", this::closeMap);
    private final JMenuItem saveAsMenuItem = menuItem("Save As", this::saveMapAs);
    private final JMenuItem applyMetadataMenuItem = menuItem("Apply Metadata", this::applyMetadata);
    private final JMenuItem undoMenuItem = menuItem("Undo", this::undoEdit);
    private final JMenuItem redoMenuItem = menuItem("Redo", this::redoEdit);
    private final JMenuItem cutAreaMenuItem = menuItem("Cut", this::cutSelectedArea);
    private final JMenuItem copyAreaMenuItem = menuItem("Copy", this::copySelectedArea);
    private final JMenuItem pasteAreaMenuItem = menuItem("Paste", this::pasteAreaAtCurrentCell);
    private final JMenuItem randomTerrainMenuItem = menuItem("Randomize Tiles", this::randomizeTerrain);
    private final JMenuItem randomAltitudeMenuItem = menuItem("Randomize Altitude", this::randomizeAltitude);
    private final JMenuItem randomMapMenuItem = menuItem("Randomize Entire Map", this::randomizeMap);
    private final JMenuItem raiseMapAltitudeMenuItem = menuItem("Entire Map UP", this::raiseMapAltitude);
    private final JMenuItem lowerMapAltitudeMenuItem = menuItem("Entire Map DOWN", this::lowerMapAltitude);
    private final JToggleButton gridToggleButton = new JToggleButton("Grid", true);
    private final JToggleButton miniMapToggleButton = new JToggleButton("MiniMap", true);
    private final JCheckBoxMenuItem gridMenuItem = new JCheckBoxMenuItem("Grid", true);
    private final JCheckBoxMenuItem miniMapMenuItem = new JCheckBoxMenuItem("MiniMap", true);
    private final ButtonGroup miniMapScaleButtonGroup = new ButtonGroup();
    private final ButtonGroup miniMapScaleMenuItemGroup = new ButtonGroup();
    private final JToggleButton miniMapHalfScaleButton = new JToggleButton(MapMiniMapScale.HALF.label());
    private final JToggleButton miniMapNormalScaleButton = new JToggleButton(MapMiniMapScale.NORMAL.label());
    private final JToggleButton miniMapDoubleScaleButton = new JToggleButton(MapMiniMapScale.DOUBLE.label());
    private final JRadioButtonMenuItem miniMapHalfScaleMenuItem = new JRadioButtonMenuItem(MapMiniMapScale.HALF.label());
    private final JRadioButtonMenuItem miniMapNormalScaleMenuItem = new JRadioButtonMenuItem(MapMiniMapScale.NORMAL.label());
    private final JRadioButtonMenuItem miniMapDoubleScaleMenuItem = new JRadioButtonMenuItem(MapMiniMapScale.DOUBLE.label());
    private MapEditorDocument document;
    private MapEditorEntitySelection selectedEntity;
    private MapEditorAreaSelection areaSelection;
    private MapEditorAreaClipboard areaClipboard;
    private Point currentCell;
    private boolean routingEntitySelection;

    /**
     * Java support constructor for the standalone MapEditor Swing frame.
     * not ported.
     */
    public MapEditorFrame() {
        super("Rage of Mages 2 MapEditor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        previewPanel.setEditListener(this::refreshForm);
        previewPanel.setSelectionListener(this::selectEntityFromPreview);
        previewPanel.setCellSelectionListener(this::selectMapCell);
        previewPanel.setObjectPickListener(this::pickObjectFromPreview);
        previewPanel.setLogicCellListener(this::selectLogicCell);
        previewPanel.setLogicContextCellListener(this::selectLogicContextCell);
        previewPanel.setAreaSelectionListener(this::selectMapArea);
        previewPanel.setAreaPasteListener(this::pasteAreaFromPreview);
        previewPanel.setViewportGeometryListener(miniMapPanel::repaint);
        previewPanel.setAnimationTickListener(miniMapPanel::repaint);
        environmentPanel.setRefreshListener(this::refreshForm);
        playersPanel.setRefreshListener(this::refreshForm);
        objectsPanel.setRefreshListener(this::refreshForm);
        buildingsPanel.setRefreshListener(this::refreshForm);
        unitsPanel.setRefreshListener(this::refreshForm);
        sacksPanel.setRefreshListener(this::refreshForm);
        effectsPanel.setRefreshListener(this::refreshForm);
        descriptorsPanel.setRefreshListener(this::refreshForm);
        groupsPanel.setRefreshListener(this::refreshForm);
        logicPanel.setRefreshListener(this::refreshForm);
        musicPanel.setRefreshListener(this::refreshForm);
        objectsPanel.setSelectionListener(this::selectEntityFromInspector);
        buildingsPanel.setSelectionListener(this::selectEntityFromInspector);
        unitsPanel.setSelectionListener(this::selectEntityFromInspector);
        sacksPanel.setSelectionListener(this::selectEntityFromInspector);
        effectsPanel.setSelectionListener(this::selectEntityFromInspector);
        descriptorsPanel.setSelectionListener(this::selectEntityFromInspector);
        logicPanel.setSelectionListener(this::selectEntityFromInspector);
        musicPanel.setSelectionListener(this::selectEntityFromInspector);
        setJMenuBar(createMenuBar());
        add(createToolbar(), BorderLayout.NORTH);
        add(createMapWorkspacePanel(), BorderLayout.CENTER);
        add(createInspectorPanel(), BorderLayout.EAST);
        add(createStatusPanel(), BorderLayout.SOUTH);
        installEditorShortcuts();
        setSize(1120, 720);
        refreshForm();
    }

    /**
     * Java support factory for the standalone MapEditor window.
     * not ported.
     */
    public static MapEditorFrame createAndShow() {
        MapEditorFrame frame = new MapEditorFrame();
        frame.showFrame();
        return frame;
    }

    /**
     * Java support cleanup boundary for the standalone MapEditor Swing frame.
     * not ported.
     */
    @Override
    public void dispose() {
        previewPanel.disposeEditorResources();
        super.dispose();
    }

    /**
     * Java support callback from the editor starter after native resources are stable for terrain preview painting.
     * not ported.
     */
    public void setTerrainPreviewResourcesReady(boolean terrainPreviewResourcesReady) {
        previewPanel.setTerrainPreviewResourcesReady(terrainPreviewResourcesReady);
    }

    /**
     * Java support callback from the editor starter after object/tree sprite resources are stable for preview painting.
     * not ported.
     */
    public void setObjectPreviewResourcesReady(boolean objectPreviewResourcesReady) {
        previewPanel.setObjectPreviewResourcesReady(objectPreviewResourcesReady);
    }

    /**
     * Java support callback from the editor starter after structure sprite resources are stable for preview painting.
     * not ported.
     */
    public void setStructurePreviewResourcesReady(boolean structurePreviewResourcesReady) {
        previewPanel.setStructurePreviewResourcesReady(structurePreviewResourcesReady);
    }

    /**
     * Java support callback from the editor starter after unit sprite resources are stable for preview painting.
     * not ported.
     */
    public void setUnitPreviewResourcesReady(boolean unitPreviewResourcesReady) {
        previewPanel.setUnitPreviewResourcesReady(unitPreviewResourcesReady);
    }

    /**
     * Java support callback from the editor starter after sack sprite resources are stable for preview painting.
     * not ported.
     */
    public void setSackPreviewResourcesReady(boolean sackPreviewResourcesReady) {
        previewPanel.setSackPreviewResourcesReady(sackPreviewResourcesReady);
    }

    /**
     * Java support callback from the editor starter after projectile/effect sprite resources are stable for preview painting.
     * not ported.
     */
    public void setEffectPreviewResourcesReady(boolean effectPreviewResourcesReady) {
        previewPanel.setEffectPreviewResourcesReady(effectPreviewResourcesReady);
    }

    /**
     * Java support frame display boundary for the standalone MapEditor UI.
     * not ported.
     */
    private void showFrame() {
        if (SwingUtilities.isEventDispatchThread()) {
            showFrameOnEventDispatchThread();
        } else {
            SwingUtilities.invokeLater(this::showFrameOnEventDispatchThread);
        }
    }

    /**
     * Java support EDT-side display boundary for the standalone MapEditor UI.
     * not ported.
     */
    private void showFrameOnEventDispatchThread() {
        setLocationByPlatform(true);
        setVisible(true);
    }

    /**
     * Java support toolbar construction for editor file actions.
     * not ported.
     */
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(newButton);
        toolbar.add(openButton);
        toolbar.add(closeButton);
        toolbar.add(saveAsButton);
        toolbar.add(applyMetadataButton);
        toolbar.addSeparator();
        toolbar.add(undoButton);
        toolbar.add(redoButton);
        toolbar.add(cutAreaButton);
        toolbar.add(copyAreaButton);
        toolbar.add(pasteAreaButton);
        toolbar.addSeparator();
        toolbar.add(randomTerrainButton);
        toolbar.add(randomAltitudeButton);
        toolbar.add(randomMapButton);
        toolbar.add(raiseMapAltitudeButton);
        toolbar.add(lowerMapAltitudeButton);
        toolbar.addSeparator();
        gridToggleButton.addActionListener(event -> toggleGrid());
        toolbar.add(gridToggleButton);
        miniMapToggleButton.addActionListener(event -> toggleMiniMap());
        toolbar.add(miniMapToggleButton);
        configureMiniMapScaleButton(miniMapHalfScaleButton, MapMiniMapScale.HALF);
        configureMiniMapScaleButton(miniMapNormalScaleButton, MapMiniMapScale.NORMAL);
        configureMiniMapScaleButton(miniMapDoubleScaleButton, MapMiniMapScale.DOUBLE);
        toolbar.add(miniMapHalfScaleButton);
        toolbar.add(miniMapNormalScaleButton);
        toolbar.add(miniMapDoubleScaleButton);
        toolbar.addSeparator();
        toolbar.add(new JLabel("Tool "));
        toolModeCombo.addActionListener(event -> toolModeChangedFromCombo());
        toolbar.add(toolModeCombo);
        return toolbar;
    }

    /**
     * Java support menu construction for the native Help-aligned editor command surface.
     * not ported.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createInstrumentsMenu());
        menuBar.add(createPlayersMenu());
        menuBar.add(createSettingsMenu());
        menuBar.add(createWindowMenu());
        return menuBar;
    }

    /**
     * Java support File menu construction for existing standalone editor file commands.
     * not ported.
     */
    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        menu.add(menuItem("New", "control N", this::showNewMapDialog));
        menu.add(menuItem("Open", "control O", this::loadMap));
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke("control F4"));
        menu.add(closeMenuItem);
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke("control A"));
        menu.add(saveAsMenuItem);
        menu.addSeparator();
        menu.add(menuItem("Exit", KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK), this::dispose));
        return menu;
    }

    /**
     * Java support Edit menu construction for selected-area and undo commands.
     * not ported.
     */
    private JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        menu.add(undoMenuItem);
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        menu.add(redoMenuItem);
        menu.addSeparator();
        menu.add(menuItem("Select", "control B", () -> selectToolMode(MapEditorToolMode.SELECT)));
        menu.add(menuItem("Entity Select", "shift E", () -> selectToolMode(MapEditorToolMode.ENTITY_SELECT)));
        cutAreaMenuItem.setAccelerator(KeyStroke.getKeyStroke("control X"));
        menu.add(cutAreaMenuItem);
        copyAreaMenuItem.setAccelerator(KeyStroke.getKeyStroke("control C"));
        menu.add(copyAreaMenuItem);
        pasteAreaMenuItem.setAccelerator(KeyStroke.getKeyStroke("control V"));
        menu.add(pasteAreaMenuItem);
        return menu;
    }

    /**
     * Java support Instruments menu construction for existing editor tool-mode commands.
     * not ported.
     */
    private JMenu createInstrumentsMenu() {
        JMenu menu = new JMenu("Instruments");
        menu.add(menuItem("Draw", "SPACE", () -> selectToolMode(MapEditorToolMode.TERRAIN)));
        menu.add(menuItem("Altitude", "shift A", () -> selectToolMode(MapEditorToolMode.ALTITUDE)));
        menu.add(menuItem("Objects", "shift O", () -> selectToolMode(MapEditorToolMode.OBJECTS)));
        menu.add(menuItem("Structures", "shift S", () -> selectToolMode(MapEditorToolMode.BUILDINGS)));
        menu.add(menuItem("Bridges", "shift B", () -> selectToolMode(MapEditorToolMode.BRIDGES)));
        menu.add(menuItem("Units", "shift U", () -> selectToolMode(MapEditorToolMode.UNITS)));
        menu.add(menuItem("Items", "shift I", () -> selectToolMode(MapEditorToolMode.ITEMS)));
        menu.add(menuItem("Logic", "shift G", () -> selectToolMode(MapEditorToolMode.LOGIC)));
        menu.add(menuItem("Music", "shift M", () -> selectToolMode(MapEditorToolMode.MUSIC)));
        menu.add(menuItem("Default Music", "control M", this::showDefaultMusicSettings));
        menu.addSeparator();
        menu.add(randomTerrainMenuItem);
        randomMapMenuItem.setAccelerator(KeyStroke.getKeyStroke("control E"));
        menu.add(randomMapMenuItem);
        randomAltitudeMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift control R"));
        menu.add(randomAltitudeMenuItem);
        return menu;
    }

    /**
     * Java support Players menu construction for the existing player/diplomacy panel.
     * not ported.
     */
    private JMenu createPlayersMenu() {
        JMenu menu = new JMenu("Players");
        menu.add(menuItem("Players", () -> inspectorTabs.setSelectedComponent(playersPanel)));
        return menu;
    }

    /**
     * Java support Settings menu construction for grid, light, randomization, and map settings commands.
     * not ported.
     */
    private JMenu createSettingsMenu() {
        JMenu menu = new JMenu("Settings");
        gridMenuItem.setAccelerator(KeyStroke.getKeyStroke("control G"));
        gridMenuItem.addActionListener(event -> toggleGrid());
        menu.add(gridMenuItem);
        menu.add(menuItem("Light", "control L", this::showLightSettings));
        menu.add(menuItem("Randomize", "control R", this::showRandomizationSettings));
        menu.addSeparator();
        raiseMapAltitudeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
        menu.add(raiseMapAltitudeMenuItem);
        lowerMapAltitudeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
        menu.add(lowerMapAltitudeMenuItem);
        menu.addSeparator();
        menu.add(menuItem("Global Description & Settings", "shift control G", this::showGlobalSettings));
        menu.add(applyMetadataMenuItem);
        return menu;
    }

    /**
     * Java support Window menu construction for the editor minimap command surface.
     * not ported.
     */
    private JMenu createWindowMenu() {
        JMenu menu = new JMenu("Window");
        miniMapMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift control M"));
        miniMapMenuItem.addActionListener(event -> toggleMiniMap());
        menu.add(miniMapMenuItem);
        menu.addSeparator();
        configureMiniMapScaleMenuItem(miniMapHalfScaleMenuItem, MapMiniMapScale.HALF, "control 1");
        configureMiniMapScaleMenuItem(miniMapNormalScaleMenuItem, MapMiniMapScale.NORMAL, "control 2");
        configureMiniMapScaleMenuItem(miniMapDoubleScaleMenuItem, MapMiniMapScale.DOUBLE, "control 3");
        menu.add(miniMapHalfScaleMenuItem);
        menu.add(miniMapNormalScaleMenuItem);
        menu.add(miniMapDoubleScaleMenuItem);
        menu.addSeparator();
        menu.add(menuItem("Zoom...", this::showZoomSettings));
        return menu;
    }

    /**
     * Java support scrollable map workspace with minimap overlay.
     * not ported.
     */
    private JLayeredPane createMapWorkspacePanel() {
        JLayeredPane workspace = new JLayeredPane() {
            @Serial
            private static final long serialVersionUID = 1L;

            /**
             * Java support layout hook for the scroll pane plus minimap overlay.
             * not ported.
             */
            @Override
            public void doLayout() {
                layoutMapWorkspace(this);
            }
        };
        workspace.setOpaque(true);
        workspace.setBackground(new Color(22, 24, 27));
        mapScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mapScrollPane.getViewport().setBackground(new Color(22, 24, 27));
        mapScrollPane.getViewport().addChangeListener(event -> miniMapPanel.repaint());
        workspace.add(mapScrollPane, JLayeredPane.DEFAULT_LAYER);
        workspace.add(miniMapPanel, JLayeredPane.PALETTE_LAYER);
        layoutMapWorkspace(workspace);
        return workspace;
    }

    /**
     * Java support absolute layout for the scroll pane plus minimap overlay.
     * not ported.
     */
    private void layoutMapWorkspace(JLayeredPane workspace) {
        mapScrollPane.setBounds(0, 0, workspace.getWidth(), workspace.getHeight());
        Dimension miniMapSize = miniMapPanel.getPreferredSize();
        int inset = 18;
        int width = Math.min(miniMapSize.width, Math.max(0, workspace.getWidth() - inset * 2));
        int height = Math.min(miniMapSize.height, Math.max(0, workspace.getHeight() - inset * 2));
        int x = Math.max(0, workspace.getWidth() - width - inset);
        int y = Math.max(0, workspace.getHeight() - height - inset);
        miniMapPanel.setBounds(x, y, width, height);
    }

    /**
     * Java support status bar construction for current map-cell feedback.
     * not ported.
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        panel.add(cellStatusLabel, BorderLayout.WEST);
        updateCellStatus();
        return panel;
    }

    /**
     * Java support button construction helper for editor commands.
     * not ported.
     */
    private static JButton button(String text, Runnable command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> command.run());
        return button;
    }

    /**
     * Java support menu item construction helper for editor commands.
     * not ported.
     */
    private static JMenuItem menuItem(String text, Runnable command) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(event -> command.run());
        return menuItem;
    }

    /**
     * Java support menu item construction helper with Swing accelerator text.
     * not ported.
     */
    private static JMenuItem menuItem(String text, String keyStroke, Runnable command) {
        return menuItem(text, KeyStroke.getKeyStroke(keyStroke), command);
    }

    /**
     * Java support menu item construction helper with an explicit Swing accelerator.
     * not ported.
     */
    private static JMenuItem menuItem(String text, KeyStroke keyStroke, Runnable command) {
        JMenuItem menuItem = menuItem(text, command);
        menuItem.setAccelerator(keyStroke);
        return menuItem;
    }

    /**
     * Java support minimap scale button binding for editor toolbar commands.
     * not ported.
     */
    private void configureMiniMapScaleButton(JToggleButton button, MapMiniMapScale scale) {
        miniMapScaleButtonGroup.add(button);
        button.addActionListener(event -> setMiniMapScale(scale));
    }

    /**
     * Java support minimap scale menu binding for editor Window menu commands.
     * not ported.
     */
    private void configureMiniMapScaleMenuItem(JRadioButtonMenuItem menuItem, MapMiniMapScale scale, String keyStroke) {
        miniMapScaleMenuItemGroup.add(menuItem);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyStroke));
        menuItem.addActionListener(event -> setMiniMapScale(scale));
    }

    /**
     * Java support inspector panel construction for current map metadata.
     * not ported.
     */
    private JPanel createInspectorPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setPreferredSize(new Dimension(420, 1));
        panel.setMinimumSize(new Dimension(320, 1));
        panel.add(createMetadataPanel(), BorderLayout.NORTH);
        panel.add(createPaintPanel(), BorderLayout.SOUTH);
        summaryText.setEditable(false);
        summaryText.setLineWrap(true);
        summaryText.setWrapStyleWord(true);
        inspectorTabs.addTab("Summary", new JScrollPane(summaryText));
        inspectorTabs.addTab("Environment", environmentPanel);
        inspectorTabs.addTab("Players", playersPanel);
        inspectorTabs.addTab("Objects", objectsPanel);
        inspectorTabs.addTab("Buildings", buildingsPanel);
        inspectorTabs.addTab("Units", unitsPanel);
        inspectorTabs.addTab("Sacks", sacksPanel);
        inspectorTabs.addTab("Effects", effectsPanel);
        inspectorTabs.addTab("Descriptors", descriptorsPanel);
        inspectorTabs.addTab("Groups", groupsPanel);
        inspectorTabs.addTab("Logic", logicPanel);
        inspectorTabs.addTab("Music", musicPanel);
        panel.add(inspectorTabs, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Java support metadata form construction for currently exposed editable fields.
     * not ported.
     */
    private JPanel createMetadataPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, "Name", mapNameField);
        addField(panel, 1, "Authors", authorsField);
        addField(panel, 2, "Players", recommendedPlayersSpinner);
        addField(panel, 3, "Level", mapLevelSpinner);
        return MapEditorCollapsiblePanel.collapsed("Map", panel);
    }

    /**
     * Java support paint-value controls for the currently implemented cell-editing modes.
     * not ported.
     */
    private JPanel createPaintPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, "Tile", tileSpinner);
        addField(panel, 1, "Height", heightSpinner);
        addField(panel, 2, "Height Spread", altitudeSpreadSpinner);
        addField(panel, 3, "Object", objectSpinner);
        tileSpinner.addChangeListener(event -> updateToolState());
        heightSpinner.addChangeListener(event -> updateToolState());
        altitudeSpreadSpinner.addChangeListener(event -> updateToolState());
        objectSpinner.addChangeListener(event -> updateToolState());
        return MapEditorCollapsiblePanel.collapsed("Cell Edit", panel);
    }

    /**
     * Java support helper for adding one metadata form row.
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
     * Java support dialog for creating a blank map document.
     * not ported.
     */
    private void showNewMapDialog() {
        JSpinner width = new JSpinner(new SpinnerNumberModel(64, 1, 512, 1));
        JSpinner height = new JSpinner(new SpinnerNumberModel(64, 1, 512, 1));
        JSpinner tile = new JSpinner(new SpinnerNumberModel(0x11, 0, 0xFFFF, 1));
        JComboBox<MapEditorLandscapeType> landscape = new JComboBox<>(MapEditorLandscapeType.values());
        JSpinner altitude = new JSpinner(new SpinnerNumberModel(0x3F, 0, 0xFF, 1));
        JSpinner altitudeSpread = new JSpinner(new SpinnerNumberModel(
                spinnerInt(altitudeSpreadSpinner),
                0,
                0xFF,
                1
        ));
        JCheckBox randomTiles = new JCheckBox("Randomize tiles");
        JCheckBox randomAltitude = new JCheckBox("Randomize altitude");
        landscape.setSelectedItem(MapEditorLandscapeType.fromTileWord(spinnerInt(tile)));
        landscape.addActionListener(event -> {
            MapEditorLandscapeType selected = (MapEditorLandscapeType) landscape.getSelectedItem();
            if (selected != null) {
                tile.setValue(selected.defaultTileWord());
            }
        });
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, "Width", width);
        addField(panel, 1, "Height", height);
        addField(panel, 2, "Ground", landscape);
        addField(panel, 3, "Tile Word", tile);
        addField(panel, 4, "Height", altitude);
        addField(panel, 5, "Height Spread", altitudeSpread);
        addField(panel, 6, "Tiles", randomTiles);
        addField(panel, 7, "Altitude", randomAltitude);
        int answer = JOptionPane.showConfirmDialog(this, panel, "New Map", JOptionPane.OK_CANCEL_OPTION);
        if (answer == JOptionPane.OK_OPTION) {
            MapEditorDocument newDocument = MapEditorFileService.createNewMap(
                    spinnerInt(width),
                    spinnerInt(height),
                    spinnerInt(tile),
                    spinnerInt(altitude)
            );
            applyNewMapRandomization(
                    newDocument,
                    spinnerInt(altitude),
                    spinnerInt(altitudeSpread),
                    randomTiles.isSelected(),
                    randomAltitude.isSelected()
            );
            setDocument(newDocument);
        }
    }

    /**
     * Java support Help-aligned new-map randomization checkboxes using existing editor randomization commands.
     * not ported.
     */
    private static void applyNewMapRandomization(
            MapEditorDocument newDocument,
            int altitude,
            int altitudeSpread,
            boolean randomTiles,
            boolean randomAltitude
    ) {
        if (randomTiles && randomAltitude) {
            newDocument.randomizeTerrainAndAltitude(altitude, altitudeSpread);
        } else if (randomTiles) {
            newDocument.randomizeTerrainVariants();
        } else if (randomAltitude) {
            newDocument.randomizeAltitude(altitude, altitudeSpread);
        }
    }

    /**
     * Java support open-file command for loading an existing ALM map.
     * not ported.
     */
    private void loadMap() {
        JFileChooser chooser = new JFileChooser(Path.of(".").toFile());
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            setDocument(MapEditorFileService.load(chooser.getSelectedFile().toPath()));
        } catch (RuntimeException exception) {
            showError("Open failed", exception);
        }
    }

    /**
     * Java support save-as command for writing the current document to a new ALM file.
     * not ported.
     */
    private void saveMapAs() {
        if (document == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser(Path.of(".").toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            MapEditorFileService.saveAs(document, chooser.getSelectedFile().toPath());
            refreshForm();
        } catch (IOException | RuntimeException exception) {
            showError("Save failed", exception);
        }
    }

    /**
     * Java support Close command for unloading the current standalone editor document.
     * not ported.
     */
    private void closeMap() {
        if (document == null) {
            return;
        }
        setDocument(null);
    }

    /**
     * Java support metadata apply command for currently exposed editor fields.
     * not ported.
     */
    private void applyMetadata() {
        if (document == null) {
            return;
        }
        document.applyMetadata(
                mapNameField.getText(),
                authorsField.getText(),
                spinnerInt(recommendedPlayersSpinner),
                spinnerInt(mapLevelSpinner)
        );
        refreshForm();
    }

    /**
     * Java support undo command for editor-owned direct cell edits.
     * not ported.
     */
    private void undoEdit() {
        if (document == null || !document.canUndo()) {
            return;
        }
        document.undo();
        refreshForm();
    }

    /**
     * Java support redo command for editor-owned direct cell edits.
     * not ported.
     */
    private void redoEdit() {
        if (document == null || !document.canRedo()) {
            return;
        }
        document.redo();
        refreshForm();
    }

    /**
     * Java support native-editor selected-area copy command for currently editable cell layers.
     * not ported.
     */
    private void copySelectedArea() {
        if (document == null || areaSelection == null) {
            return;
        }
        try {
            areaClipboard = document.copyArea(areaSelection);
            updateCommandState();
            updateCellStatus();
        } catch (RuntimeException exception) {
            showError("Copy failed", exception);
        }
    }

    /**
     * Native editor behavior mirrored from ROM2 Map Editor.exe Cut handler @0041FFA0.
     * Java support selected-area cut command for currently editable cell layers.
     * not ported.
     */
    private void cutSelectedArea() {
        if (document == null || areaSelection == null) {
            return;
        }
        try {
            areaClipboard = document.cutArea(areaSelection);
            refreshForm();
        } catch (RuntimeException exception) {
            showError("Cut failed", exception);
        }
    }

    /**
     * Java support native-editor selected-area paste command at the current map cell.
     * not ported.
     */
    private void pasteAreaAtCurrentCell() {
        if (currentCell == null) {
            return;
        }
        pasteAreaAt(currentCell);
    }

    /**
     * Java support native-editor selected-area paste command from viewport right-clicks.
     * not ported.
     */
    private boolean pasteAreaFromPreview(Point targetCell) {
        if (areaClipboard == null) {
            return false;
        }
        pasteAreaAt(targetCell);
        return true;
    }

    /**
     * Java support selected-area paste implementation at an explicit upper-left map cell.
     * not ported.
     */
    private void pasteAreaAt(Point targetCell) {
        if (document == null || areaClipboard == null || targetCell == null) {
            return;
        }
        try {
            document.pasteArea(areaClipboard, targetCell.x, targetCell.y);
            MapEditorAreaSelection pastedSelection = MapEditorAreaSelection.fromCells(
                    targetCell.x,
                    targetCell.y,
                    targetCell.x + areaClipboard.width() - 1,
                    targetCell.y + areaClipboard.height() - 1
            );
            previewPanel.setAreaSelection(pastedSelection);
            previewPanel.setCurrentCell(targetCell);
            refreshForm();
        } catch (RuntimeException exception) {
            showError("Paste failed", exception);
        }
    }

    /**
     * Java support full-map terrain randomization command for the standalone editor.
     * not ported.
     */
    private void randomizeTerrain() {
        if (document == null) {
            return;
        }
        document.randomizeTerrainVariants();
        refreshForm();
    }

    /**
     * Java support full-map altitude randomization command for the standalone editor.
     * not ported.
     */
    private void randomizeAltitude() {
        if (document == null) {
            return;
        }
        document.randomizeAltitude(spinnerInt(heightSpinner), spinnerInt(altitudeSpreadSpinner));
        refreshForm();
    }

    /**
     * Java support Help-aligned full-map randomization command for the standalone editor.
     * not ported.
     */
    private void randomizeMap() {
        if (document == null) {
            return;
        }
        document.randomizeTerrainAndAltitude(spinnerInt(heightSpinner), spinnerInt(altitudeSpreadSpinner));
        refreshForm();
    }

    /**
     * Java support native Help-aligned full-map altitude raise command.
     * not ported.
     */
    private void raiseMapAltitude() {
        adjustMapAltitude(10);
    }

    /**
     * Java support native Help-aligned full-map altitude lower command.
     * not ported.
     */
    private void lowerMapAltitude() {
        adjustMapAltitude(-10);
    }

    /**
     * Java support full-map altitude delta command for the standalone editor.
     * not ported.
     */
    private void adjustMapAltitude(int delta) {
        if (document == null) {
            return;
        }
        document.adjustAltitude(delta);
        refreshForm();
    }

    /**
     * Java support native Help-aligned grid visibility toggle command.
     * not ported.
     */
    private void toggleGrid() {
        previewPanel.setGridVisible(!previewPanel.isGridVisible());
        selectToolMode(MapEditorToolMode.GRID);
        updateGridCommandState();
        previewPanel.repaint();
    }

    /**
     * Java support clockwise virtual map-perspective rotation command.
     * not ported.
     */
    private void rotatePerspectiveClockwise() {
        previewPanel.rotatePerspectiveClockwise();
        miniMapPanel.repaint();
    }

    /**
     * Java support counter-clockwise virtual map-perspective rotation command.
     * not ported.
     */
    private void rotatePerspectiveCounterClockwise() {
        previewPanel.rotatePerspectiveCounterClockwise();
        miniMapPanel.repaint();
    }

    /**
     * Java support native Help-aligned minimap visibility toggle command.
     * not ported.
     */
    private void toggleMiniMap() {
        miniMapPanel.setVisible(!miniMapPanel.isVisible());
        updateMiniMapCommandState();
        java.awt.Container parent = miniMapPanel.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    /**
     * Java support native Help-aligned minimap scale command.
     * not ported.
     */
    private void setMiniMapScale(MapMiniMapScale scale) {
        miniMapPanel.setScale(scale);
        updateMiniMapCommandState();
        java.awt.Container parent = miniMapPanel.getParent();
        if (parent != null) {
            parent.doLayout();
            parent.repaint();
        }
    }

    /**
     * Java support tool-combo change routing for the standalone editor command surface.
     * not ported.
     */
    private void toolModeChangedFromCombo() {
        routeToolModeToInspector(selectedToolMode());
        updateToolState();
        if (document != null) {
            summaryText.setText(summary());
        }
    }

    /**
     * Java support native Help-aligned tool-mode command routing.
     * not ported.
     */
    private void selectToolMode(MapEditorToolMode toolMode) {
        toolModeCombo.setSelectedItem(toolMode);
        routeToolModeToInspector(toolMode);
        updateToolState();
        if (document != null) {
            summaryText.setText(summary());
        }
    }

    /**
     * Java support native Help-aligned light-settings command routing.
     * not ported.
     */
    private void showLightSettings() {
        selectToolMode(MapEditorToolMode.LIGHT);
        inspectorTabs.setSelectedComponent(environmentPanel);
    }

    /**
     * Java support native Help-aligned randomization-settings command routing.
     * not ported.
     */
    private void showRandomizationSettings() {
        selectToolMode(MapEditorToolMode.RANDOMIZATION);
    }

    /**
     * Java support native Help-aligned global description/settings command routing.
     * not ported.
     */
    private void showGlobalSettings() {
        inspectorTabs.setSelectedComponent(environmentPanel);
        mapNameField.requestFocusInWindow();
    }

    /**
     * Java support native Help-aligned default-music command routing.
     * not ported.
     */
    private void showDefaultMusicSettings() {
        selectToolMode(MapEditorToolMode.MUSIC);
        inspectorTabs.setSelectedComponent(musicPanel);
    }

    /**
     * Java support editor viewport zoom-level configuration command.
     * not ported.
     */
    private void showZoomSettings() {
        JTextField zoomLevelsField = new JTextField(previewPanel.zoomLevelsText(), 24);
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, 0, "Levels", zoomLevelsField);
        while (true) {
            int answer = JOptionPane.showConfirmDialog(this, panel, "Viewport Zoom", JOptionPane.OK_CANCEL_OPTION);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                previewPanel.setZoomLevelsText(zoomLevelsField.getText());
                miniMapPanel.repaint();
                updateCellStatus();
                return;
            } catch (IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(), "Viewport Zoom", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Java support inspector tab selection for already implemented native editor tool modes.
     * not ported.
     */
    private void routeToolModeToInspector(MapEditorToolMode toolMode) {
        switch (toolMode) {
            case TERRAIN, SELECT, ENTITY_SELECT, ALTITUDE, BRIDGES, GRID, RANDOMIZATION ->
                    inspectorTabs.setSelectedIndex(0);
            case OBJECTS -> inspectorTabs.setSelectedComponent(objectsPanel);
            case BUILDINGS -> inspectorTabs.setSelectedComponent(buildingsPanel);
            case ITEMS -> inspectorTabs.setSelectedComponent(sacksPanel);
            case UNITS -> inspectorTabs.setSelectedComponent(unitsPanel);
            case MUSIC -> inspectorTabs.setSelectedComponent(musicPanel);
            case LOGIC -> inspectorTabs.setSelectedComponent(logicPanel);
            case LIGHT -> inspectorTabs.setSelectedComponent(environmentPanel);
        }
    }

    /**
     * Java support routing from viewport entity hit-testing to existing inspector panels.
     * not ported.
     */
    private void selectEntityFromPreview(MapEditorEntitySelection selection) {
        applyEntitySelection(selection, true, false);
    }

    /**
     * Java support native editor right-click object picker from the map viewport.
     * not ported.
     */
    private void pickObjectFromPreview(Point cell) {
        if (document == null || cell == null) {
            return;
        }
        int cellIndex = cell.y * document.scenario().mapWidth + cell.x;
        applyEntitySelection(new MapEditorEntitySelection(MapEditorEntitySelection.Kind.OBJECT, cellIndex), true, false);
    }

    /**
     * Java support native-editor Logic left-click routing into the Drop Location helper coordinates.
     * not ported.
     */
    private void selectLogicCell(Point cell) {
        if (document == null || cell == null) {
            return;
        }
        toolModeCombo.setSelectedItem(MapEditorToolMode.LOGIC);
        inspectorTabs.setSelectedComponent(logicPanel);
        logicPanel.setDropLocationCell(cell);
        applyEntitySelection(null, false, false);
        updateCellStatus();
    }

    /**
     * Java support native-editor Logic right-click routing into traps/structure-casting helper coordinates.
     * not ported.
     */
    private void selectLogicContextCell(Point cell) {
        if (document == null || cell == null) {
            return;
        }
        toolModeCombo.setSelectedItem(MapEditorToolMode.LOGIC);
        inspectorTabs.setSelectedComponent(effectsPanel);
        effectsPanel.setLogicHelperCell(cell);
        applyEntitySelection(null, false, false);
        updateCellStatus();
    }

    /**
     * Java support current map-cell selection from the viewport into status and minimap feedback.
     * not ported.
     */
    private void selectMapCell(Point cell) {
        currentCell = cell == null ? null : new Point(cell);
        miniMapPanel.setCurrentCell(currentCell);
        updateCommandState();
        updateCellStatus();
    }

    /**
     * Java support current map-area selection from the viewport into status and minimap feedback.
     * not ported.
     */
    private void selectMapArea(MapEditorAreaSelection selection) {
        areaSelection = selection;
        miniMapPanel.setAreaSelection(selection);
        if (selection != null) {
            applyEntitySelection(null, false, false);
        }
        updateCommandState();
        updateCellStatus();
    }

    /**
     * Java support routing from inspector row selection to viewport and minimap highlights.
     * not ported.
     */
    private void selectEntityFromInspector(MapEditorEntitySelection selection) {
        if (routingEntitySelection) {
            return;
        }
        applyEntitySelection(selection, false, true);
    }

    /**
     * Java support selected saved-entity state shared by viewport, minimap, and inspector panels.
     * not ported.
     */
    private void applyEntitySelection(
            MapEditorEntitySelection selection,
            boolean routeInspector,
            boolean centerViewport
    ) {
        selectedEntity = selection;
        previewPanel.setSelectedEntity(selection);
        miniMapPanel.setSelectedEntity(selection);
        syncObjectPaintValueFromSelection(selection);
        if (routeInspector) {
            routeEntitySelectionToInspector(selection, selectedToolMode() != MapEditorToolMode.ENTITY_SELECT);
        }
        if (centerViewport) {
            scrollEntitySelectionIntoView(selection);
        }
    }

    /**
     * Java support selected saved-entity routing into existing inspector panels.
     * not ported.
     */
    private void routeEntitySelectionToInspector(MapEditorEntitySelection selection, boolean routeToolMode) {
        routingEntitySelection = true;
        try {
            switch (selection.kind) {
                case OBJECT -> {
                    if (routeToolMode) {
                        toolModeCombo.setSelectedItem(MapEditorToolMode.OBJECTS);
                    }
                    inspectorTabs.setSelectedComponent(objectsPanel);
                    objectsPanel.selectObjectCellIndex(selection.index);
                }
                case BUILDING -> {
                    if (routeToolMode) {
                        toolModeCombo.setSelectedItem(MapEditorToolMode.BUILDINGS);
                    }
                    inspectorTabs.setSelectedComponent(buildingsPanel);
                    buildingsPanel.selectBuildingIndex(selection.index);
                }
                case SHOP_DESCRIPTOR -> {
                    inspectorTabs.setSelectedComponent(descriptorsPanel);
                    descriptorsPanel.selectShopDescriptorIndex(selection.index);
                }
                case INN_DESCRIPTOR -> {
                    inspectorTabs.setSelectedComponent(descriptorsPanel);
                    descriptorsPanel.selectInnDescriptorIndex(selection.index);
                }
                case POST_DESCRIPTOR -> {
                    inspectorTabs.setSelectedComponent(descriptorsPanel);
                    descriptorsPanel.selectPostDescriptorIndex(selection.index);
                }
                case DROP_LOCATION_INSTANT -> {
                    if (routeToolMode) {
                        toolModeCombo.setSelectedItem(MapEditorToolMode.LOGIC);
                    }
                    inspectorTabs.setSelectedComponent(logicPanel);
                    logicPanel.selectInstantIndex(selection.index);
                }
                case UNIT -> {
                    if (routeToolMode) {
                        toolModeCombo.setSelectedItem(MapEditorToolMode.UNITS);
                    }
                    inspectorTabs.setSelectedComponent(unitsPanel);
                    unitsPanel.selectUnitIndex(selection.index);
                }
                case SACK -> {
                    if (routeToolMode) {
                        toolModeCombo.setSelectedItem(MapEditorToolMode.ITEMS);
                    }
                    inspectorTabs.setSelectedComponent(sacksPanel);
                    sacksPanel.selectSackIndex(selection.index);
                }
                case EFFECT -> {
                    if (routeToolMode) {
                        toolModeCombo.setSelectedItem(MapEditorToolMode.LOGIC);
                    }
                    inspectorTabs.setSelectedComponent(effectsPanel);
                    effectsPanel.selectEffectIndex(selection.index);
                }
                case MUSIC -> {
                    if (routeToolMode) {
                        toolModeCombo.setSelectedItem(MapEditorToolMode.MUSIC);
                    }
                    inspectorTabs.setSelectedComponent(musicPanel);
                    musicPanel.selectMusicZoneIndex(selection.index);
                }
            }
        } finally {
            routingEntitySelection = false;
        }
        updateToolState();
    }

    /**
     * Java support toolbar object-value synchronization from selected object cells.
     * not ported.
     */
    private void syncObjectPaintValueFromSelection(MapEditorEntitySelection selection) {
        if (document == null || selection == null || selection.kind != MapEditorEntitySelection.Kind.OBJECT) {
            return;
        }
        int cellCount = document.scenario().mapWidth * document.scenario().mapHeight;
        if (selection.index < 0 || selection.index >= cellCount) {
            return;
        }
        int objectValue = Byte.toUnsignedInt(document.scenario().sec3Objects[selection.index]);
        if (objectValue == 0) {
            return;
        }
        objectSpinner.setValue(objectValue);
        updateToolState();
    }

    /**
     * Java support viewport centering around selected saved entities.
     * not ported.
     */
    private void scrollEntitySelectionIntoView(MapEditorEntitySelection selection) {
        if (document == null || selection == null) {
            return;
        }
        Rectangle bounds = previewPanel.selectedEntityBaseBounds(selection);
        if (bounds == null) {
            return;
        }
        previewPanel.centerViewportOnBaseBounds(bounds);
        previewPanel.repaint();
        miniMapPanel.repaint();
    }

    /**
     * Java support document swap for editor UI state.
     * not ported.
     */
    private void setDocument(MapEditorDocument document) {
        this.document = document;
        selectedEntity = null;
        areaSelection = null;
        currentCell = null;
        previewPanel.setSelectedEntity(null);
        previewPanel.setAreaSelection(null);
        previewPanel.setCurrentCell(null);
        miniMapPanel.setSelectedEntity(null);
        miniMapPanel.setAreaSelection(null);
        miniMapPanel.setCurrentCell(null);
        previewPanel.setDocument(document);
        miniMapPanel.setDocument(document);
        mapScrollPane.getViewport().setViewPosition(new Point(0, 0));
        environmentPanel.setDocument(document);
        playersPanel.setDocument(document);
        objectsPanel.setDocument(document);
        buildingsPanel.setDocument(document);
        unitsPanel.setDocument(document);
        sacksPanel.setDocument(document);
        effectsPanel.setDocument(document);
        descriptorsPanel.setDocument(document);
        groupsPanel.setDocument(document);
        logicPanel.setDocument(document);
        musicPanel.setDocument(document);
        refreshForm();
    }

    /**
     * Java support form refresh from the active document.
     * not ported.
     */
    private void refreshForm() {
        if (document == null) {
            mapNameField.setText("");
            authorsField.setText("");
            summaryText.setText("No map loaded.");
            environmentPanel.setDocument(null);
            playersPanel.setDocument(null);
            objectsPanel.setDocument(null);
            buildingsPanel.setDocument(null);
            unitsPanel.setDocument(null);
            sacksPanel.setDocument(null);
            effectsPanel.setDocument(null);
            descriptorsPanel.setDocument(null);
            groupsPanel.setDocument(null);
            logicPanel.setDocument(null);
            musicPanel.setDocument(null);
            updateCommandState();
            miniMapPanel.invalidateOverview();
            updateCellStatus();
            return;
        }
        mapNameField.setText(document.scenario().mapName.toString());
        authorsField.setText(document.scenario().authors.toString());
        recommendedPlayersSpinner.setValue(document.scenario().recommendedPlayers);
        mapLevelSpinner.setValue(document.scenario().mapLevel);
        summaryText.setText(summary());
        environmentPanel.refreshFromDocument();
        playersPanel.refreshFromDocument();
        objectsPanel.refreshFromDocument();
        buildingsPanel.refreshFromDocument();
        unitsPanel.refreshFromDocument();
        sacksPanel.refreshFromDocument();
        effectsPanel.refreshFromDocument();
        descriptorsPanel.refreshFromDocument();
        groupsPanel.refreshFromDocument();
        logicPanel.refreshFromDocument();
        musicPanel.refreshFromDocument();
        updateToolState();
        updateCommandState();
        previewPanel.repaint();
        miniMapPanel.invalidateOverview();
        updateCellStatus();
    }

    /**
     * Java support summary text for the currently loaded descriptor.
     * not ported.
     */
    private String summary() {
        return "Source: " + (document.sourcePath() == null ? "<new map>" : document.sourcePath()) + "\n"
                + "Dirty: " + document.dirty() + "\n"
                + "Tool: " + selectedToolMode().label() + " - " + selectedToolMode().description() + "\n"
                + "Size: " + document.scenario().mapWidth + " x " + document.scenario().mapHeight + "\n"
                + "Time: " + document.scenario().time + "\n"
                + "Brightness: " + document.scenario().darkness + "\n"
                + "Contrast: " + document.scenario().contrast + "\n"
                + "Solar angle: " + Math.toDegrees(document.scenario().sunAngle) + "\n"
                + "Tile mask: 0x" + Integer.toHexString(document.scenario().useTiles) + "\n"
                + "Object cells: " + objectCellCount() + "\n"
                + "Players: " + document.scenario().sec5Players.size() + "\n"
                + "Units: " + document.scenario().sec6Units.size() + "\n"
                + "Buildings: " + document.scenario().sec4Buildings.size() + "\n"
                + "Instants: " + document.scenario().sect7Instants.size() + "\n"
                + "Checks: " + document.scenario().sect7Checks.size() + "\n"
                + "Triggers: " + document.scenario().sect7Triggers.size() + "\n"
                + "Sacks: " + document.scenario().sect8Sacks.size() + "\n"
                + "Effects: " + document.scenario().sect9Effects.size() + "\n"
                + "Descriptors: inns " + document.scenario().sect11InnDescriptors.size()
                + ", shops " + document.scenario().sect11ShopDescriptors.size()
                + ", posts " + document.scenario().sect11PostDescriptors.size() + "\n"
                + "Music areas: " + document.scenario().sect12Music.size() + "\n"
                + "Groups: " + document.scenario().sect10Groups.size();
    }

    /**
     * Java support nonzero OBJECTS-section cell count for editor summary text.
     * not ported.
     */
    private int objectCellCount() {
        int objectCells = 0;
        for (byte objectValue : document.scenario().sec3Objects) {
            if (Byte.toUnsignedInt(objectValue) != 0) {
                objectCells++;
            }
        }
        return objectCells;
    }

    /**
     * Java support synchronization from Swing controls to the preview editing surface.
     * not ported.
     */
    private void updateToolState() {
        previewPanel.setToolMode(selectedToolMode());
        previewPanel.setPaintTileId(spinnerInt(tileSpinner));
        previewPanel.setPaintHeight(spinnerInt(heightSpinner));
        previewPanel.setPaintObject(spinnerInt(objectSpinner));
    }

    /**
     * Java support undo/redo button-state refresh for editor-owned direct cell edits.
     * not ported.
     */
    private void updateCommandState() {
        closeButton.setEnabled(document != null);
        closeMenuItem.setEnabled(document != null);
        saveAsButton.setEnabled(document != null);
        saveAsMenuItem.setEnabled(document != null);
        applyMetadataButton.setEnabled(document != null);
        applyMetadataMenuItem.setEnabled(document != null);
        undoButton.setEnabled(document != null && document.canUndo());
        undoMenuItem.setEnabled(document != null && document.canUndo());
        redoButton.setEnabled(document != null && document.canRedo());
        redoMenuItem.setEnabled(document != null && document.canRedo());
        cutAreaButton.setEnabled(document != null && areaSelection != null);
        cutAreaMenuItem.setEnabled(document != null && areaSelection != null);
        copyAreaButton.setEnabled(document != null && areaSelection != null);
        copyAreaMenuItem.setEnabled(document != null && areaSelection != null);
        pasteAreaButton.setEnabled(document != null && areaClipboard != null && currentCell != null);
        pasteAreaMenuItem.setEnabled(document != null && areaClipboard != null && currentCell != null);
        randomTerrainButton.setEnabled(document != null);
        randomTerrainMenuItem.setEnabled(document != null);
        randomAltitudeButton.setEnabled(document != null);
        randomAltitudeMenuItem.setEnabled(document != null);
        randomMapButton.setEnabled(document != null);
        randomMapMenuItem.setEnabled(document != null);
        raiseMapAltitudeButton.setEnabled(document != null);
        raiseMapAltitudeMenuItem.setEnabled(document != null);
        lowerMapAltitudeButton.setEnabled(document != null);
        lowerMapAltitudeMenuItem.setEnabled(document != null);
        updateGridCommandState();
        updateMiniMapCommandState();
    }

    /**
     * Java support current-cell status text from the active map arrays.
     * not ported.
     */
    private void updateCellStatus() {
        if (document == null) {
            cellStatusLabel.setText("Cell: no map");
            return;
        }
        if (currentCell == null) {
            cellStatusLabel.setText("Cell: -" + areaSelectionStatus());
            return;
        }
        if (currentCell.x < 0
                || currentCell.y < 0
                || currentCell.x >= document.scenario().mapWidth
                || currentCell.y >= document.scenario().mapHeight) {
            cellStatusLabel.setText("Cell: outside map");
            return;
        }
        int index = currentCell.y * document.scenario().mapWidth + currentCell.x;
        int tile = document.scenario().sec1Tiles[index] & 0xFFFF;
        int height = Byte.toUnsignedInt(document.scenario().sec2Heights[index]);
        int object = Byte.toUnsignedInt(document.scenario().sec3Objects[index]);
        cellStatusLabel.setText(
                "x: " + currentCell.x
                        + "  y: " + currentCell.y
                        + "  tile: " + tile
                        + "  height: " + height
                        + "  object: " + object
                        + areaSelectionStatus()
        );
    }

    /**
     * Java support selected map-area status text for the status bar.
     * not ported.
     */
    private String areaSelectionStatus() {
        if (areaSelection == null) {
            return "";
        }
        return "  selection: "
                + areaSelection.left() + "," + areaSelection.top()
                + " - " + areaSelection.right() + "," + areaSelection.bottom()
                + " (" + areaSelection.width() + "x" + areaSelection.height()
                + ", " + areaSelection.cellCount() + " cells)";
    }

    /**
     * Java support minimap toolbar button-state refresh.
     * not ported.
     */
    private void updateMiniMapCommandState() {
        miniMapToggleButton.setSelected(miniMapPanel.isVisible());
        miniMapMenuItem.setSelected(miniMapPanel.isVisible());
        miniMapHalfScaleButton.setSelected(miniMapPanel.scale() == MapMiniMapScale.HALF);
        miniMapNormalScaleButton.setSelected(miniMapPanel.scale() == MapMiniMapScale.NORMAL);
        miniMapDoubleScaleButton.setSelected(miniMapPanel.scale() == MapMiniMapScale.DOUBLE);
        miniMapHalfScaleMenuItem.setSelected(miniMapPanel.scale() == MapMiniMapScale.HALF);
        miniMapNormalScaleMenuItem.setSelected(miniMapPanel.scale() == MapMiniMapScale.NORMAL);
        miniMapDoubleScaleMenuItem.setSelected(miniMapPanel.scale() == MapMiniMapScale.DOUBLE);
    }

    /**
     * Java support native Help-aligned grid toolbar state refresh.
     * not ported.
     */
    private void updateGridCommandState() {
        gridToggleButton.setEnabled(document != null);
        gridToggleButton.setSelected(previewPanel.isGridVisible());
        gridMenuItem.setEnabled(document != null);
        gridMenuItem.setSelected(previewPanel.isGridVisible());
    }

    /**
     * Java support keyboard shortcuts for editor commands.
     * not ported.
     */
    private void installEditorShortcuts() {
        JComponent rootPane = getRootPane();
        installShortcut(rootPane, "control N", "mapeditor.new", this::showNewMapDialog);
        installShortcut(rootPane, "control O", "mapeditor.open", this::loadMap);
        installShortcut(rootPane, "control A", "mapeditor.save.as", this::saveMapAs);
        installShortcut(rootPane, "control F4", "mapeditor.close", this::closeMap);
        installShortcut(rootPane, "control Z", "mapeditor.undo", this::undoEdit);
        installShortcut(rootPane, "control Y", "mapeditor.redo", this::redoEdit);
        installShortcut(rootPane, "shift control Z", "mapeditor.redo", this::redoEdit);
        installShortcut(rootPane, "shift control M", "mapeditor.minimap.toggle", this::toggleMiniMap);
        installShortcut(rootPane, "control B", "mapeditor.select.area", () -> selectToolMode(MapEditorToolMode.SELECT));
        installShortcut(rootPane, "shift E", "mapeditor.select.entity", () -> selectToolMode(MapEditorToolMode.ENTITY_SELECT));
        installShortcut(rootPane, "control C", "mapeditor.copy.area", this::copySelectedArea);
        installShortcut(rootPane, "control V", "mapeditor.paste.area", this::pasteAreaAtCurrentCell);
        installShortcut(rootPane, "control 1", "mapeditor.minimap.half", () -> setMiniMapScale(MapMiniMapScale.HALF));
        installShortcut(rootPane, "control 2", "mapeditor.minimap.normal", () -> setMiniMapScale(MapMiniMapScale.NORMAL));
        installShortcut(rootPane, "control 3", "mapeditor.minimap.double", () -> setMiniMapScale(MapMiniMapScale.DOUBLE));
        installShortcut(rootPane, "control G", "mapeditor.grid.toggle", this::toggleGrid);
        installShortcut(rootPane, "control L", "mapeditor.light", this::showLightSettings);
        installShortcut(rootPane, "control R", "mapeditor.randomization", this::showRandomizationSettings);
        installShortcut(rootPane, "shift control R", "mapeditor.randomize.altitude", this::randomizeAltitude);
        installShortcut(rootPane, "shift control G", "mapeditor.global.settings", this::showGlobalSettings);
        installShortcut(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0),
                "mapeditor.view.rotate.counter", this::rotatePerspectiveCounterClockwise);
        installShortcut(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_E, 0),
                "mapeditor.view.rotate.clockwise", this::rotatePerspectiveClockwise);
        installShortcut(rootPane, "SPACE", "mapeditor.tool.terrain", () -> selectToolMode(MapEditorToolMode.TERRAIN));
        installShortcut(rootPane, "shift A", "mapeditor.tool.altitude", () -> selectToolMode(MapEditorToolMode.ALTITUDE));
        installShortcut(rootPane, "shift O", "mapeditor.tool.objects", () -> selectToolMode(MapEditorToolMode.OBJECTS));
        installShortcut(rootPane, "shift S", "mapeditor.tool.buildings", () -> selectToolMode(MapEditorToolMode.BUILDINGS));
        installShortcut(rootPane, "shift B", "mapeditor.tool.bridges", () -> selectToolMode(MapEditorToolMode.BRIDGES));
        installShortcut(rootPane, "shift U", "mapeditor.tool.units", () -> selectToolMode(MapEditorToolMode.UNITS));
        installShortcut(rootPane, "shift I", "mapeditor.tool.items", () -> selectToolMode(MapEditorToolMode.ITEMS));
        installShortcut(rootPane, "shift G", "mapeditor.tool.logic", () -> selectToolMode(MapEditorToolMode.LOGIC));
        installShortcut(rootPane, "shift M", "mapeditor.tool.music", () -> selectToolMode(MapEditorToolMode.MUSIC));
        installShortcut(rootPane, "control M", "mapeditor.default.music", this::showDefaultMusicSettings);
        installShortcut(rootPane, "control E", "mapeditor.randomize.map", this::randomizeMap);
        installShortcut(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "mapeditor.altitude.raise", this::raiseMapAltitude);
        installShortcut(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "mapeditor.altitude.lower", this::lowerMapAltitude);
    }

    /**
     * Java support helper for focused-window keyboard shortcut binding.
     * not ported.
     */
    private static void installShortcut(JComponent rootPane, String keyStroke, String actionKey, Runnable command) {
        installShortcut(rootPane, KeyStroke.getKeyStroke(keyStroke), actionKey, command);
    }

    /**
     * Java support helper for focused-window keyboard shortcut binding with explicit key strokes.
     * not ported.
     */
    private static void installShortcut(JComponent rootPane, KeyStroke keyStroke, String actionKey, Runnable command) {
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionKey);
        rootPane.getActionMap().put(actionKey, new AbstractAction() {
            /**
             * Java support keyboard action dispatch for editor commands.
             * not ported.
             */
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                command.run();
            }
        });
    }

    /**
     * Java support accessor for the currently selected editor tool mode.
     * not ported.
     */
    private MapEditorToolMode selectedToolMode() {
        return (MapEditorToolMode) toolModeCombo.getSelectedItem();
    }

    /**
     * Java support helper for reading integer spinner values.
     * not ported.
     */
    private static int spinnerInt(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    /**
     * Java support integer clamp helper for viewport centering.
     * not ported.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Java support error dialog for editor operations.
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
