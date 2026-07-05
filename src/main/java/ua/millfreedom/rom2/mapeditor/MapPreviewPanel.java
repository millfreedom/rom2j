package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.ScenarioDescriptor;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.io.Serial;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Swing map preview surface for the standalone MapEditor.
 * not ported.
 */
public final class MapPreviewPanel extends JPanel implements Scrollable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_CELL_PIXEL_SIZE = 32;
    private static final int EMPTY_PREVIEW_WIDTH = 640;
    private static final int EMPTY_PREVIEW_HEIGHT = 520;
    private static final int GRID_MAJOR_INTERVAL = 8;
    private static final int MIN_ZOOM_PERCENT = 10;
    private static final int MAX_ZOOM_PERCENT = 800;
    private static final int DEFAULT_ZOOM_PERCENT = 100;
    private static final int[] DEFAULT_ZOOM_PERCENT_STEPS = {50, 75, 100, 150, 200, 300};
    private static final Color GRID_LINE_COLOR = new Color(120, 18, 18, 140);
    private static final Color MAP_BOUNDS_COLOR = new Color(20, 22, 25);
    private static final Color CURRENT_CELL_SHADOW = new Color(0, 0, 0, 210);
    private static final Color CURRENT_CELL_OUTLINE = new Color(85, 235, 255, 235);
    private static final int CURRENT_CELL_REPAINT_PADDING = 6;
    private static final int AREA_SELECTION_REPAINT_PADDING = 6;

    private MapEditorDocument document;
    private int[] zoomPercentSteps = DEFAULT_ZOOM_PERCENT_STEPS.clone();
    private int zoomStepIndex = zoomStepIndexForPercent(zoomPercentSteps, DEFAULT_ZOOM_PERCENT);
    private MapEditorViewRotation viewRotation = MapEditorViewRotation.NONE;
    private MapEditorToolMode toolMode = MapEditorToolMode.TERRAIN;
    private int paintTileId = 0x11;
    private int paintHeight = 0x3F;
    private int paintObject = 0;
    private boolean gridVisible = true;
    private MapEditorEntitySelection selectedEntity;
    private MapEditorAreaSelection areaSelection;
    private boolean terrainPreviewResourcesReady;
    private boolean objectPreviewResourcesReady;
    private boolean structurePreviewResourcesReady;
    private boolean unitPreviewResourcesReady;
    private boolean sackPreviewResourcesReady;
    private boolean effectPreviewResourcesReady;
    private int idleAnimationTick;
    private Point currentCell;
    private Point selectAnchorCell;
    private boolean selectDragActive;
    private final MapEditorTerrainPreviewRenderer terrainPreviewRenderer = new MapEditorTerrainPreviewRenderer();
    private final MapEditorObjectLayerSpriteRenderer objectLayerSpriteRenderer = new MapEditorObjectLayerSpriteRenderer();
    private final Timer idleAnimationTimer;
    private Runnable editListener = () -> {
    };
    private Consumer<MapEditorEntitySelection> selectionListener = selection -> {
    };
    private Consumer<Point> cellSelectionListener = cell -> {
    };
    private Consumer<Point> objectPickListener = cell -> {
    };
    private Consumer<Point> logicCellListener = cell -> {
    };
    private Consumer<Point> logicContextCellListener = cell -> {
    };
    private Consumer<MapEditorAreaSelection> areaSelectionListener = selection -> {
    };
    private Predicate<Point> areaPasteListener = cell -> false;
    private Runnable viewportGeometryListener = () -> {
    };
    private Runnable animationTickListener = () -> {
    };

    /**
     * Java support constructor for the editor preview panel.
     * not ported.
     */
    public MapPreviewPanel() {
        setBackground(new Color(22, 24, 27));
        MouseAdapter editingMouseAdapter = new MouseAdapter() {
            /**
             * Java support mouse press hook for direct map-cell edits.
             * not ported.
             */
            @Override
            public void mousePressed(MouseEvent event) {
                setCurrentCellFromEvent(event);
                if (handleAreaSelectionPress(event)) {
                    return;
                }
                if (pickObjectAt(event)) {
                    return;
                }
                if (pickLogicCellAt(event)) {
                    return;
                }
                if (selectEntityAt(event)) {
                    return;
                }
                applyCellEdit(event);
            }

            /**
             * Java support mouse drag hook for direct map-cell edits.
             * not ported.
             */
            @Override
            public void mouseDragged(MouseEvent event) {
                setCurrentCellFromEvent(event);
                if (handleAreaSelectionDrag(event)) {
                    return;
                }
                if (toolMode.paintsOnDrag()) {
                    applyCellEdit(event);
                }
            }

            /**
             * Java support mouse release hook for finalizing drag-based map-area selection.
             * not ported.
             */
            @Override
            public void mouseReleased(MouseEvent event) {
                finishAreaSelectionDrag(event);
            }

            /**
             * Java support mouse move hook for current map-cell feedback.
             * not ported.
             */
            @Override
            public void mouseMoved(MouseEvent event) {
                setCurrentCellFromEvent(event);
            }

            /**
             * Java support mouse exit hook for clearing current map-cell feedback outside the map surface.
             * not ported.
             */
            @Override
            public void mouseExited(MouseEvent event) {
                setCurrentCell(null);
            }
        };
        addMouseListener(editingMouseAdapter);
        addMouseMotionListener(editingMouseAdapter);
        addMouseWheelListener(this::handleMouseWheel);
        idleAnimationTimer = new Timer(120, event -> advanceIdleAnimationFrame());
        idleAnimationTimer.start();
    }

    /**
     * Java support document binding for preview repaint.
     * not ported.
     */
    public void setDocument(MapEditorDocument document) {
        this.document = document;
        setCurrentCell(null);
        selectAnchorCell = null;
        selectDragActive = false;
        setAreaSelection(null);
        revalidate();
        repaint();
    }

    /**
     * Java support cleanup for standalone MapEditor preview resources.
     * not ported.
     */
    void disposeEditorResources() {
        idleAnimationTimer.stop();
    }

    /**
     * Java support tool-mode binding for editor surface clicks.
     * not ported.
     */
    public void setToolMode(MapEditorToolMode toolMode) {
        this.toolMode = toolMode;
        if (toolMode != MapEditorToolMode.SELECT) {
            selectAnchorCell = null;
            selectDragActive = false;
        }
    }

    /**
     * Java support setter for the terrain tile id painted by TERRAIN mode.
     * not ported.
     */
    public void setPaintTileId(int paintTileId) {
        this.paintTileId = paintTileId;
    }

    /**
     * Java support setter for the height value painted by ALTITUDE mode.
     * not ported.
     */
    public void setPaintHeight(int paintHeight) {
        this.paintHeight = paintHeight;
    }

    /**
     * Java support setter for the object value painted by OBJECTS mode.
     * not ported.
     */
    public void setPaintObject(int paintObject) {
        this.paintObject = paintObject;
    }

    /**
     * Java support edit callback used by the frame to refresh metadata after surface edits.
     * not ported.
     */
    public void setEditListener(Runnable editListener) {
        this.editListener = editListener == null ? () -> {
        } : editListener;
    }

    /**
     * Java support entity-selection callback used by the frame to route viewport clicks to inspector panels.
     * not ported.
     */
    void setSelectionListener(Consumer<MapEditorEntitySelection> selectionListener) {
        this.selectionListener = selectionListener == null ? selection -> {
        } : selectionListener;
    }

    /**
     * Java support current-cell callback used by the frame status bar and minimap marker.
     * not ported.
     */
    void setCellSelectionListener(Consumer<Point> cellSelectionListener) {
        this.cellSelectionListener = cellSelectionListener == null ? cell -> {
        } : cellSelectionListener;
    }

    /**
     * Java support object-pick callback used by right-clicking object cells in OBJECTS mode.
     * not ported.
     */
    void setObjectPickListener(Consumer<Point> objectPickListener) {
        this.objectPickListener = objectPickListener == null ? cell -> {
        } : objectPickListener;
    }

    /**
     * Java support logic-tool map-cell callback used by native-style editor logic helpers.
     * not ported.
     */
    void setLogicCellListener(Consumer<Point> logicCellListener) {
        this.logicCellListener = logicCellListener == null ? cell -> {
        } : logicCellListener;
    }

    /**
     * Java support logic-tool map-cell callback used by native-style traps/structure-casting helpers.
     * not ported.
     */
    void setLogicContextCellListener(Consumer<Point> logicContextCellListener) {
        this.logicContextCellListener = logicContextCellListener == null ? cell -> {
        } : logicContextCellListener;
    }

    /**
     * Java support area-selection callback used by the frame status bar and minimap marker.
     * not ported.
     */
    void setAreaSelectionListener(Consumer<MapEditorAreaSelection> areaSelectionListener) {
        this.areaSelectionListener = areaSelectionListener == null ? selection -> {
        } : areaSelectionListener;
    }

    /**
     * Java support selected-area paste callback used by right-clicking the Select tool.
     * not ported.
     */
    void setAreaPasteListener(Predicate<Point> areaPasteListener) {
        this.areaPasteListener = areaPasteListener == null ? cell -> false : areaPasteListener;
    }

    /**
     * Java support viewport geometry callback used by the frame to refresh minimap state after zoom changes.
     * not ported.
     */
    void setViewportGeometryListener(Runnable viewportGeometryListener) {
        this.viewportGeometryListener = viewportGeometryListener == null ? () -> {
        } : viewportGeometryListener;
    }

    /**
     * Java support animation-tick callback used by minimap animated selection repainting.
     * not ported.
     */
    void setAnimationTickListener(Runnable animationTickListener) {
        this.animationTickListener = animationTickListener == null ? () -> {
        } : animationTickListener;
    }

    /**
     * Java support selected saved-entity binding for viewport highlight painting.
     * not ported.
     */
    void setSelectedEntity(MapEditorEntitySelection selectedEntity) {
        this.selectedEntity = selectedEntity;
        repaint();
    }

    /**
     * Java support current editor animation tick for animated EFFECTS selection projection.
     * not ported.
     */
    Integer currentEffectSelectionAnimationTick() {
        return effectPreviewResourcesReady ? idleAnimationTick : null;
    }

    /**
     * Java support current editor-owned animation tick for linked preview surfaces.
     * not ported.
     */
    int currentPreviewAnimationTick() {
        return idleAnimationTick;
    }

    /**
     * Java support selected entity bounds in unrotated, unscaled preview coordinates.
     * not ported.
     */
    Rectangle selectedEntityBaseBounds(MapEditorEntitySelection selectedEntity) {
        if (document == null || selectedEntity == null) {
            return null;
        }
        ScenarioDescriptor scenario = document.scenario();
        return MapEditorEntityOverlayRenderer.viewportSelectionBounds(
                scenario,
                selectedEntity,
                DEFAULT_CELL_PIXEL_SIZE,
                MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario),
                currentEffectSelectionAnimationTick()
        );
    }

    /**
     * Java support selected map-area binding for viewport highlight painting.
     * not ported.
     */
    void setAreaSelection(MapEditorAreaSelection areaSelection) {
        MapEditorAreaSelection oldSelection = this.areaSelection;
        this.areaSelection = areaSelection;
        areaSelectionListener.accept(areaSelection);
        repaintAreaSelection(oldSelection);
        repaintAreaSelection(areaSelection);
    }

    /**
     * Java support current map-cell binding for viewport cursor painting.
     * not ported.
     */
    void setCurrentCell(Point currentCell) {
        Point oldCell = this.currentCell;
        this.currentCell = currentCell == null ? null : new Point(currentCell);
        cellSelectionListener.accept(this.currentCell == null ? null : new Point(this.currentCell));
        repaintCell(oldCell);
        repaintCell(this.currentCell);
    }

    /**
     * Java support readiness binding for terrain-art resource usage in the standalone editor preview.
     * not ported.
     */
    void setTerrainPreviewResourcesReady(boolean terrainPreviewResourcesReady) {
        this.terrainPreviewResourcesReady = terrainPreviewResourcesReady;
        repaint();
    }

    /**
     * Java support readiness binding for object/tree sprite resources in the standalone editor preview.
     * not ported.
     */
    void setObjectPreviewResourcesReady(boolean objectPreviewResourcesReady) {
        this.objectPreviewResourcesReady = objectPreviewResourcesReady;
        repaint();
    }

    /**
     * Java support readiness binding for structure sprite resources in the standalone editor preview.
     * not ported.
     */
    void setStructurePreviewResourcesReady(boolean structurePreviewResourcesReady) {
        this.structurePreviewResourcesReady = structurePreviewResourcesReady;
        repaint();
    }

    /**
     * Java support readiness binding for unit sprite resources in the standalone editor preview.
     * not ported.
     */
    void setUnitPreviewResourcesReady(boolean unitPreviewResourcesReady) {
        this.unitPreviewResourcesReady = unitPreviewResourcesReady;
        repaint();
    }

    /**
     * Java support readiness binding for sack sprite resources in the standalone editor preview.
     * not ported.
     */
    void setSackPreviewResourcesReady(boolean sackPreviewResourcesReady) {
        this.sackPreviewResourcesReady = sackPreviewResourcesReady;
        repaint();
    }

    /**
     * Java support readiness binding for projectile/effect sprite resources in the standalone editor preview.
     * not ported.
     */
    void setEffectPreviewResourcesReady(boolean effectPreviewResourcesReady) {
        this.effectPreviewResourcesReady = effectPreviewResourcesReady;
        repaint();
    }

    /**
     * Java support grid-visibility binding for the native editor Grid command.
     * not ported.
     */
    void setGridVisible(boolean gridVisible) {
        if (this.gridVisible == gridVisible) {
            return;
        }
        this.gridVisible = gridVisible;
        repaint();
    }

    /**
     * Java support grid-visibility state for toolbar and shortcut synchronization.
     * not ported.
     */
    boolean isGridVisible() {
        return gridVisible;
    }

    /**
     * Java support cell-pixel size used by the scrollable editor viewport and minimap.
     * not ported.
     */
    int tilePixelSize() {
        return scaleBaseLength(DEFAULT_CELL_PIXEL_SIZE);
    }

    /**
     * Java support unscaled native map-cell pixel size used by minimap viewport math.
     * not ported.
     */
    int baseTilePixelSize() {
        return DEFAULT_CELL_PIXEL_SIZE;
    }

    /**
     * Java support current map viewport zoom percentage.
     * not ported.
     */
    int zoomPercent() {
        return zoomPercentSteps[zoomStepIndex];
    }

    /**
     * Java support current virtual map-view rotation used by the standalone editor only.
     * not ported.
     */
    MapEditorViewRotation viewRotation() {
        return viewRotation;
    }

    /**
     * Java support clockwise virtual map-view rotation for the editor Q/E commands.
     * not ported.
     */
    void rotatePerspectiveClockwise() {
        applyViewRotation(viewRotation.clockwise());
    }

    /**
     * Java support counter-clockwise virtual map-view rotation for the editor Q/E commands.
     * not ported.
     */
    void rotatePerspectiveCounterClockwise() {
        applyViewRotation(viewRotation.counterClockwise());
    }

    /**
     * Java support text form for the configured viewport zoom levels.
     * not ported.
     */
    String zoomLevelsText() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < zoomPercentSteps.length; i++) {
            if (i != 0) {
                text.append(", ");
            }
            text.append(zoomPercentSteps[i]);
        }
        return text.toString();
    }

    /**
     * Java support configurable viewport zoom ladder used by Alt+mouse-wheel zoom.
     * not ported.
     */
    void setZoomLevelsText(String zoomLevelsText) {
        int[] nextZoomPercentSteps = parseZoomPercentSteps(zoomLevelsText);
        int currentZoomPercent = zoomPercent();
        int nextZoomStepIndex = zoomStepIndexForPercent(nextZoomPercentSteps, currentZoomPercent);
        applyZoomState(nextZoomPercentSteps, nextZoomStepIndex, zoomAnchorPoint());
    }

    /**
     * Java support scaled terrain top padding for the scrollable viewport.
     * not ported.
     */
    int viewportTopPixelOffset(ScenarioDescriptor scenario) {
        return scaleBaseLength(MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario));
    }

    /**
     * Java support preferred size for the scrollable full-map editing canvas.
     * not ported.
     */
    @Override
    public Dimension getPreferredSize() {
        if (document == null) {
            return new Dimension(EMPTY_PREVIEW_WIDTH, EMPTY_PREVIEW_HEIGHT);
        }
        ScenarioDescriptor scenario = document.scenario();
        Dimension orientedBaseSize = viewRotation.orientedCanvasSize(mapBaseCanvasSize(scenario));
        return new Dimension(
                Math.max(1, scaleBaseLength(orientedBaseSize.width)),
                Math.max(1, scaleBaseLength(orientedBaseSize.height))
        );
    }

    /**
     * Java support preferred viewport size when the map canvas is hosted by a JScrollPane.
     * not ported.
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(EMPTY_PREVIEW_WIDTH, EMPTY_PREVIEW_HEIGHT);
    }

    /**
     * Java support small scroll increment aligned to one map cell.
     * not ported.
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return tilePixelSize();
    }

    /**
     * Java support large scroll increment aligned to a small native-editor tile group.
     * not ported.
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return tilePixelSize() * GRID_MAJOR_INTERVAL;
    }

    /**
     * Java support width tracking disabled so the map canvas remains scrollable horizontally.
     * not ported.
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Java support height tracking disabled so the map canvas remains scrollable vertically.
     * not ported.
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Java support mouse-edit dispatcher for the currently selected editor tool.
     * not ported.
     */
    private void applyCellEdit(MouseEvent event) {
        if (document == null || !toolMode.editsCellsDirectly()) {
            return;
        }
        Point cell = cellAt(event.getX(), event.getY());
        if (cell == null) {
            return;
        }
        switch (toolMode) {
            case TERRAIN -> document.setTile(cell.x, cell.y, paintTileId);
            case ALTITUDE -> document.setHeight(cell.x, cell.y, paintHeight);
            case OBJECTS -> document.setObject(cell.x, cell.y, event.isShiftDown() ? 0 : paintObject);
            default -> {
            }
        }
        editListener.run();
        repaint();
    }

    /**
     * Java support current-cell update from viewport mouse coordinates.
     * not ported.
     */
    private void setCurrentCellFromEvent(MouseEvent event) {
        if (document == null) {
            setCurrentCell(null);
            return;
        }
        setCurrentCell(cellAt(event.getX(), event.getY()));
    }

    /**
     * Java support saved-entity selection from the dedicated editor entity-select tool.
     * not ported.
     */
    private boolean selectEntityAt(MouseEvent event) {
        if (document == null || toolMode != MapEditorToolMode.ENTITY_SELECT) {
            return false;
        }
        Point cell = cellAt(event.getX(), event.getY());
        if (cell == null) {
            return false;
        }
        MapEditorEntitySelection selection = effectPreviewResourcesReady
                ? MapEditorEntityOverlayRenderer.hitTestTile(document.scenario(), cell.x, cell.y, idleAnimationTick)
                : MapEditorEntityOverlayRenderer.hitTestTile(document.scenario(), cell.x, cell.y);
        if (selection == null) {
            return false;
        }
        selectionListener.accept(selection);
        return true;
    }

    /**
     * Java support native-editor Select command press handling.
     * not ported.
     */
    private boolean handleAreaSelectionPress(MouseEvent event) {
        if (document == null || toolMode != MapEditorToolMode.SELECT) {
            return false;
        }
        if (SwingUtilities.isRightMouseButton(event)) {
            Point cell = cellAt(event.getX(), event.getY());
            if (cell != null && areaPasteListener.test(new Point(cell))) {
                return true;
            }
            selectAnchorCell = null;
            selectDragActive = false;
            setAreaSelection(null);
            return true;
        }
        if (!SwingUtilities.isLeftMouseButton(event)) {
            return true;
        }
        Point cell = cellAt(event.getX(), event.getY());
        if (cell == null) {
            return true;
        }
        if (selectAnchorCell == null) {
            selectAnchorCell = new Point(cell);
            selectDragActive = false;
            setAreaSelection(MapEditorAreaSelection.fromCorners(selectAnchorCell, cell));
        } else {
            selectDragActive = false;
            setAreaSelection(MapEditorAreaSelection.fromCorners(selectAnchorCell, cell));
            selectAnchorCell = null;
        }
        return true;
    }

    /**
     * Java support native-editor Select command drag handling.
     * not ported.
     */
    private boolean handleAreaSelectionDrag(MouseEvent event) {
        if (document == null || toolMode != MapEditorToolMode.SELECT) {
            return false;
        }
        if (selectAnchorCell == null) {
            return true;
        }
        Point cell = cellAt(event.getX(), event.getY());
        if (cell != null) {
            selectDragActive = true;
            setAreaSelection(MapEditorAreaSelection.fromCorners(selectAnchorCell, cell));
        }
        return true;
    }

    /**
     * Java support native-editor Select command drag finalization.
     * not ported.
     */
    private boolean finishAreaSelectionDrag(MouseEvent event) {
        if (document == null || toolMode != MapEditorToolMode.SELECT) {
            return false;
        }
        if (selectAnchorCell != null && selectDragActive) {
            Point cell = cellAt(event.getX(), event.getY());
            if (cell != null) {
                setAreaSelection(MapEditorAreaSelection.fromCorners(selectAnchorCell, cell));
            }
            selectAnchorCell = null;
            selectDragActive = false;
        }
        return true;
    }

    /**
     * Java support object-byte picker for the native editor right-click behavior.
     * not ported.
     */
    private boolean pickObjectAt(MouseEvent event) {
        if (document == null || toolMode != MapEditorToolMode.OBJECTS || !SwingUtilities.isRightMouseButton(event)) {
            return false;
        }
        Point cell = cellAt(event.getX(), event.getY());
        if (cell == null) {
            return true;
        }
        int index = cell.y * document.scenario().mapWidth + cell.x;
        if (Byte.toUnsignedInt(document.scenario().sec3Objects[index]) != 0) {
            objectPickListener.accept(new Point(cell));
        }
        return true;
    }

    /**
     * Java support native-editor Logic left-click routing for helper panels such as Drop Location.
     * not ported.
     */
    private boolean pickLogicCellAt(MouseEvent event) {
        if (document == null || toolMode != MapEditorToolMode.LOGIC) {
            return false;
        }
        if (!SwingUtilities.isLeftMouseButton(event) && !SwingUtilities.isRightMouseButton(event)) {
            return true;
        }
        Point cell = cellAt(event.getX(), event.getY());
        if (cell != null) {
            if (SwingUtilities.isLeftMouseButton(event)) {
                logicCellListener.accept(new Point(cell));
            } else {
                logicContextCellListener.accept(new Point(cell));
            }
        }
        return true;
    }

    /**
     * Java support preview renderer for current descriptor terrain/object arrays.
     * not ported.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            if (document == null) {
                drawEmptyPreview(g);
                return;
            }
            ScenarioDescriptor scenario = document.scenario();
            Rectangle clip = g.getClipBounds();
            if (clip == null) {
                clip = new Rectangle(0, 0, getWidth(), getHeight());
            }
            Rectangle orientedClip = viewToBaseRectangle(clip);
            Dimension baseSize = mapBaseCanvasSize(scenario);
            Rectangle baseClip = viewRotation.orientedToBaseBounds(orientedClip, baseSize.width, baseSize.height);
            Graphics2D mapGraphics = (Graphics2D) g.create();
            try {
                mapGraphics.scale(zoomScale(), zoomScale());
                viewRotation.applyBaseToOrientedTransform(mapGraphics, baseSize.width, baseSize.height);
                mapGraphics.setClip(baseClip);
                drawScenarioPreview(mapGraphics, scenario);
            } finally {
                mapGraphics.dispose();
            }
        } finally {
            g.dispose();
        }
    }

    /**
     * Java support placeholder drawing for an unopened editor document.
     * not ported.
     */
    private void drawEmptyPreview(Graphics2D g) {
        g.setColor(new Color(58, 63, 70));
        g.drawRect(16, 16, getWidth() - 33, getHeight() - 33);
        g.setColor(new Color(150, 156, 164));
        g.drawString("Create or load a map", 28, 40);
    }

    /**
     * Java support terrain/object grid preview for editor documents.
     * not ported.
     */
    private void drawScenarioPreview(Graphics2D g, ScenarioDescriptor scenario) {
        int mapWidth = scenario.mapWidth;
        int mapHeight = scenario.mapHeight;
        int cellSize = DEFAULT_CELL_PIXEL_SIZE;
        int topOffset = MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario);
        Rectangle clip = g.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(0, 0, getWidth(), getHeight());
        }
        int startX = Math.max(0, clip.x / cellSize);
        int startY = Math.max(0, Math.floorDiv(clip.y - topOffset, cellSize));
        int endX = Math.min(mapWidth, (clip.x + clip.width + cellSize - 1) / cellSize);
        int endY = Math.max(0, Math.min(mapHeight,
                (clip.y + clip.height - topOffset + cellSize - 1) / cellSize));

        if (shouldDrawCompactCellFallback(scenario)) {
            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    int index = y * mapWidth + x;
                    g.setColor(new Color(argbForCell(scenario, index), true));
                    g.fillRect(x * cellSize, topOffset + y * cellSize, cellSize, cellSize);
                }
            }
        }
        if (terrainPreviewResourcesReady) {
            terrainPreviewRenderer.drawTerrainOverlay(g, scenario, 0, topOffset, cellSize, idleAnimationTick);
        }
        objectLayerSpriteRenderer.drawObjectLayerSprites(
                g,
                scenario,
                0,
                topOffset,
                cellSize,
                idleAnimationTick,
                objectPreviewResourcesReady,
                structurePreviewResourcesReady,
                unitPreviewResourcesReady,
                sackPreviewResourcesReady,
                effectPreviewResourcesReady
        );
        if (gridVisible) {
            drawGrid(g, scenario, topOffset, clip);
        }
        MapEditorEntityOverlayRenderer.drawViewportOverlay(g, scenario, cellSize, topOffset, clip);
        MapEditorEntityOverlayRenderer.drawViewportSelection(
                g,
                scenario,
                selectedEntity,
                cellSize,
                topOffset,
                clip,
                effectPreviewResourcesReady ? idleAnimationTick : null
        );
        drawAreaSelection(g, scenario, topOffset, clip);
        drawCurrentCell(g, scenario, topOffset, clip);
        drawMapBounds(g, scenario, topOffset);
    }

    /**
     * Java support fallback gate for maps that cannot yet be painted through terrain tile resources.
     * not ported.
     */
    private boolean shouldDrawCompactCellFallback(ScenarioDescriptor scenario) {
        return !terrainPreviewResourcesReady || scenario.useTiles == 0;
    }

    /**
     * Java support red tile-grid overlay projected through the same terrain vertices as the editor terrain preview.
     * not ported.
     */
    private void drawGrid(Graphics2D g, ScenarioDescriptor scenario, int viewportTopOffset, Rectangle clip) {
        int cellSize = DEFAULT_CELL_PIXEL_SIZE;
        int startX = Math.max(0, Math.floorDiv(clip.x, cellSize) - 1);
        int endX = Math.min(scenario.mapWidth, Math.floorDiv(clip.x + clip.width + cellSize - 1, cellSize) + 1);
        int skewMargin = viewportTopOffset + MapEditorTerrainPreviewRenderer.viewportBottomPadding(scenario);
        int startY = Math.max(0, Math.floorDiv(clip.y - viewportTopOffset - skewMargin, cellSize) - 1);
        int endY = Math.min(
                scenario.mapHeight,
                Math.floorDiv(clip.y + clip.height - viewportTopOffset + skewMargin + cellSize - 1, cellSize) + 1
        );
        if (startX > endX || startY > endY) {
            return;
        }

        g.setColor(GRID_LINE_COLOR);
        for (int x = startX; x <= endX; x++) {
            drawProjectedVerticalGridLine(g, scenario, x, startY, endY, viewportTopOffset, cellSize);
        }
        for (int y = startY; y <= endY; y++) {
            drawProjectedHorizontalGridLine(g, scenario, y, startX, endX, viewportTopOffset, cellSize);
        }
    }

    /**
     * Java support projected outline for the current map surface.
     * not ported.
     */
    private void drawMapBounds(Graphics2D g, ScenarioDescriptor scenario, int viewportTopOffset) {
        int cellSize = DEFAULT_CELL_PIXEL_SIZE;
        g.setColor(MAP_BOUNDS_COLOR);
        drawProjectedHorizontalGridLine(g, scenario, 0, 0, scenario.mapWidth, viewportTopOffset, cellSize);
        drawProjectedHorizontalGridLine(g, scenario, scenario.mapHeight, 0, scenario.mapWidth, viewportTopOffset, cellSize);
        drawProjectedVerticalGridLine(g, scenario, 0, 0, scenario.mapHeight, viewportTopOffset, cellSize);
        drawProjectedVerticalGridLine(g, scenario, scenario.mapWidth, 0, scenario.mapHeight, viewportTopOffset, cellSize);
    }

    /**
     * Java support projected terrain-grid vertical line drawing.
     * not ported.
     */
    private static void drawProjectedVerticalGridLine(
            Graphics2D g,
            ScenarioDescriptor scenario,
            int vertexX,
            int startY,
            int endY,
            int viewportTopOffset,
            int cellSize
    ) {
        int screenX = vertexX * cellSize;
        int previousY = projectedTerrainVertexY(scenario, vertexX, startY, viewportTopOffset, cellSize);
        for (int y = startY + 1; y <= endY; y++) {
            int nextY = projectedTerrainVertexY(scenario, vertexX, y, viewportTopOffset, cellSize);
            g.drawLine(screenX, previousY, screenX, nextY);
            previousY = nextY;
        }
    }

    /**
     * Java support projected terrain-grid horizontal line drawing.
     * not ported.
     */
    private static void drawProjectedHorizontalGridLine(
            Graphics2D g,
            ScenarioDescriptor scenario,
            int vertexY,
            int startX,
            int endX,
            int viewportTopOffset,
            int cellSize
    ) {
        int previousX = startX * cellSize;
        int previousY = projectedTerrainVertexY(scenario, startX, vertexY, viewportTopOffset, cellSize);
        for (int x = startX + 1; x <= endX; x++) {
            int nextX = x * cellSize;
            int nextY = projectedTerrainVertexY(scenario, x, vertexY, viewportTopOffset, cellSize);
            g.drawLine(previousX, previousY, nextX, nextY);
            previousX = nextX;
            previousY = nextY;
        }
    }

    /**
     * Java support projected terrain-grid vertex Y matching MapEditorTerrainPreviewRenderer terrain-cell corners.
     * not ported.
     */
    private static int projectedTerrainVertexY(
            ScenarioDescriptor scenario,
            int vertexX,
            int vertexY,
            int viewportTopOffset,
            int cellSize
    ) {
        return MapEditorTerrainPreviewRenderer.projectedTerrainVertexY(
                scenario,
                vertexX,
                vertexY,
                viewportTopOffset,
                cellSize
        );
    }

    /**
     * Java support current-cell cursor drawing over the editable map surface.
     * not ported.
     */
    private void drawCurrentCell(Graphics2D g, ScenarioDescriptor scenario, int viewportTopOffset, Rectangle clip) {
        Path2D cursorPath = projectedCurrentCellPath(
                scenario,
                currentCell,
                viewportTopOffset,
                DEFAULT_CELL_PIXEL_SIZE
        );
        if (cursorPath == null) {
            return;
        }
        Rectangle bounds = cursorPath.getBounds();
        bounds.grow(CURRENT_CELL_REPAINT_PADDING, CURRENT_CELL_REPAINT_PADDING);
        if (!bounds.intersects(clip)) {
            return;
        }
        Stroke oldStroke = g.getStroke();
        try {
            g.setColor(CURRENT_CELL_SHADOW);
            g.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(cursorPath);
            g.setColor(CURRENT_CELL_OUTLINE);
            g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(cursorPath);
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(cursorPath);
        } finally {
            g.setStroke(oldStroke);
        }
    }

    /**
     * Java support map-area selection overlay drawing over the editable map surface.
     * not ported.
     */
    private void drawAreaSelection(Graphics2D g, ScenarioDescriptor scenario, int viewportTopOffset, Rectangle clip) {
        if (areaSelection == null) {
            return;
        }
        Path2D selectionPath = projectedAreaSelectionPath(
                scenario,
                areaSelection,
                viewportTopOffset,
                DEFAULT_CELL_PIXEL_SIZE
        );
        Rectangle bounds = selectionPath.getBounds();
        bounds.grow(AREA_SELECTION_REPAINT_PADDING, AREA_SELECTION_REPAINT_PADDING);
        if (!bounds.intersects(clip)) {
            return;
        }
        g.setColor(new Color(50, 150, 255, 54));
        g.fill(selectionPath);
        Stroke oldStroke = g.getStroke();
        try {
            g.setColor(new Color(0, 0, 0, 230));
            g.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(selectionPath);
            g.setColor(new Color(90, 205, 255, 245));
            g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(selectionPath);
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(selectionPath);
        } finally {
            g.setStroke(oldStroke);
        }
    }

    /**
     * Java support projected selected-area outline using the same terrain vertices as the editor grid overlay.
     * not ported.
     */
    private static Path2D projectedAreaSelectionPath(
            ScenarioDescriptor scenario,
            MapEditorAreaSelection selection,
            int viewportTopOffset,
            int cellSize
    ) {
        return projectedCellAreaPath(
                scenario,
                selection.left(),
                selection.top(),
                selection.right() + 1,
                selection.bottom() + 1,
                viewportTopOffset,
                cellSize
        );
    }

    /**
     * Java support projected inclusive-cell-area perimeter using terrain-grid vertices.
     * not ported.
     */
    private static Path2D projectedCellAreaPath(
            ScenarioDescriptor scenario,
            int left,
            int top,
            int right,
            int bottom,
            int viewportTopOffset,
            int cellSize
    ) {
        Path2D path = new Path2D.Double();
        path.moveTo(left * cellSize, projectedTerrainVertexY(scenario, left, top, viewportTopOffset, cellSize));
        for (int x = left + 1; x <= right; x++) {
            path.lineTo(x * cellSize, projectedTerrainVertexY(scenario, x, top, viewportTopOffset, cellSize));
        }
        for (int y = top + 1; y <= bottom; y++) {
            path.lineTo(right * cellSize, projectedTerrainVertexY(scenario, right, y, viewportTopOffset, cellSize));
        }
        for (int x = right - 1; x >= left; x--) {
            path.lineTo(x * cellSize, projectedTerrainVertexY(scenario, x, bottom, viewportTopOffset, cellSize));
        }
        for (int y = bottom - 1; y >= top; y--) {
            path.lineTo(left * cellSize, projectedTerrainVertexY(scenario, left, y, viewportTopOffset, cellSize));
        }
        path.closePath();
        return path;
    }

    /**
     * Java support coordinate conversion from preview pixels to map tile coordinates.
     * not ported.
     */
    private Point cellAt(int screenX, int screenY) {
        ScenarioDescriptor scenario = document.scenario();
        int mapWidth = scenario.mapWidth;
        int mapHeight = scenario.mapHeight;
        int cellSize = DEFAULT_CELL_PIXEL_SIZE;
        int viewportTopOffset = MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario);
        Point basePoint = viewToMapBasePoint(screenX, screenY, scenario);
        int baseX = basePoint.x;
        int baseY = basePoint.y;
        if (baseX < 0) {
            return null;
        }
        int tileX = baseX / cellSize;
        if (tileX < 0 || tileX >= mapWidth) {
            return null;
        }

        int approximateTileY = Math.floorDiv(baseY - viewportTopOffset, cellSize);
        int verticalTileRadius = projectedHitTestVerticalTileRadius(scenario, cellSize);
        int startY = Math.max(0, approximateTileY - verticalTileRadius);
        int endY = Math.min(mapHeight - 1, approximateTileY + verticalTileRadius);
        for (int tileY = endY; tileY >= startY; tileY--) {
            if (projectedCellContains(scenario, tileX, tileY, viewportTopOffset, cellSize, baseX, baseY)) {
                return new Point(tileX, tileY);
            }
        }
        return null;
    }

    /**
     * Java support projected grid hit-testing radius derived from the terrain height padding used by preview geometry.
     * not ported.
     */
    private static int projectedHitTestVerticalTileRadius(ScenarioDescriptor scenario, int cellSize) {
        int verticalSpan = MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario)
                + MapEditorTerrainPreviewRenderer.viewportBottomPadding(scenario);
        return Math.max(2, (verticalSpan + cellSize - 1) / cellSize + 1);
    }

    /**
     * Java support projected cell-shape hit test matching the current-cell and Select overlay geometry.
     * not ported.
     */
    private static boolean projectedCellContains(
            ScenarioDescriptor scenario,
            int tileX,
            int tileY,
            int viewportTopOffset,
            int cellSize,
            int baseX,
            int baseY
    ) {
        Path2D path = projectedCellAreaPath(
                scenario,
                tileX,
                tileY,
                tileX + 1,
                tileY + 1,
                viewportTopOffset,
                cellSize
        );
        return path.contains(baseX + 0.5d, baseY + 0.5d)
                || path.intersects(baseX, baseY, 1.0d, 1.0d);
    }

    /**
     * Java support repaint of one current-cell cursor location.
     * not ported.
     */
    private void repaintCell(Point cell) {
        if (document == null || cell == null) {
            return;
        }
        ScenarioDescriptor scenario = document.scenario();
        Path2D cursorPath = projectedCurrentCellPath(
                scenario,
                cell,
                MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario),
                DEFAULT_CELL_PIXEL_SIZE
        );
        if (cursorPath != null) {
            repaintBaseBounds(cursorPath.getBounds(), CURRENT_CELL_REPAINT_PADDING);
        }
    }

    /**
     * Java support repaint of one selected map-area overlay.
     * not ported.
     */
    private void repaintAreaSelection(MapEditorAreaSelection selection) {
        if (document == null || selection == null) {
            return;
        }
        ScenarioDescriptor scenario = document.scenario();
        Rectangle bounds = projectedAreaSelectionPath(
                scenario,
                selection,
                MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario),
                DEFAULT_CELL_PIXEL_SIZE
        ).getBounds();
        repaintBaseBounds(bounds, AREA_SELECTION_REPAINT_PADDING);
    }

    /**
     * Java support projected current-cell cursor outline using the same terrain vertices as the editor grid overlay.
     * not ported.
     */
    private static Path2D projectedCurrentCellPath(
            ScenarioDescriptor scenario,
            Point cell,
            int viewportTopOffset,
            int cellSize
    ) {
        if (cell == null || cell.x < 0 || cell.y < 0 || cell.x >= scenario.mapWidth || cell.y >= scenario.mapHeight) {
            return null;
        }
        return projectedCellAreaPath(
                scenario,
                cell.x,
                cell.y,
                cell.x + 1,
                cell.y + 1,
                viewportTopOffset,
                cellSize
        );
    }

    /**
     * Java support editor-owned idle animation tick without gameplay action updates.
     * not ported.
     */
    private void advanceIdleAnimationFrame() {
        if (document == null
                || (!terrainPreviewResourcesReady
                && !objectPreviewResourcesReady
                && !structurePreviewResourcesReady
                && !unitPreviewResourcesReady
                && !sackPreviewResourcesReady
                && !effectPreviewResourcesReady)
                || !isShowing()) {
            return;
        }
        idleAnimationTick++;
        repaint(getVisibleRect());
        animationTickListener.run();
    }

    /**
     * Java support Alt+mouse-wheel zoom handler while preserving ordinary scroll-pane wheel panning.
     * not ported.
     */
    private void handleMouseWheel(MouseWheelEvent event) {
        if (!event.isAltDown()) {
            if (panViewportWithMouseWheel(event)) {
                event.consume();
            }
            return;
        }
        event.consume();
        int rotation = event.getWheelRotation();
        if (rotation == 0) {
            double preciseRotation = event.getPreciseWheelRotation();
            if (preciseRotation == 0.0) {
                return;
            }
            rotation = preciseRotation < 0.0 ? -1 : 1;
        }
        int stepDelta = rotation < 0 ? 1 : -1;
        zoomAt(event.getPoint(), stepDelta);
    }

    /**
     * Java support ordinary mouse-wheel viewport panning when the preview owns wheel dispatch.
     * not ported.
     */
    private boolean panViewportWithMouseWheel(MouseWheelEvent event) {
        if (!(getParent() instanceof JViewport viewport)
                || !(viewport.getParent() instanceof JScrollPane scrollPane)) {
            return false;
        }
        JScrollBar scrollBar = event.isShiftDown()
                ? scrollPane.getHorizontalScrollBar()
                : scrollPane.getVerticalScrollBar();
        if (scrollBar == null) {
            return false;
        }
        int maxValue = scrollBar.getMaximum() - scrollBar.getVisibleAmount();
        int minValue = scrollBar.getMinimum();
        if (maxValue <= minValue) {
            return false;
        }
        int delta = mouseWheelScrollDelta(event, scrollBar);
        if (delta == 0) {
            return false;
        }
        scrollBar.setValue(Math.clamp(scrollBar.getValue() + delta, minValue, maxValue));
        return true;
    }

    /**
     * Java support conversion from a Swing mouse-wheel event to scrollbar movement.
     * not ported.
     */
    private static int mouseWheelScrollDelta(MouseWheelEvent event, JScrollBar scrollBar) {
        int rotation = event.getWheelRotation();
        if (event.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            return rotation * scrollBar.getBlockIncrement(rotation < 0 ? -1 : 1);
        }
        int units = event.getUnitsToScroll();
        if (units == 0) {
            double preciseRotation = event.getPreciseWheelRotation();
            if (preciseRotation == 0.0) {
                return 0;
            }
            units = preciseRotation < 0.0 ? -1 : 1;
        }
        return units * scrollBar.getUnitIncrement(units < 0 ? -1 : 1);
    }

    /**
     * Java support zoom-level change anchored around the mouse position.
     * not ported.
     */
    private void zoomAt(Point anchor, int stepDelta) {
        int nextIndex = Math.clamp(zoomStepIndex + stepDelta, 0, zoomPercentSteps.length - 1);
        if (nextIndex == zoomStepIndex) {
            return;
        }
        applyZoomState(zoomPercentSteps, nextIndex, anchor);
    }

    /**
     * Java support viewport zoom state application with scroll anchoring.
     * not ported.
     */
    private void applyZoomState(int[] nextZoomPercentSteps, int nextZoomStepIndex, Point anchor) {
        if (nextZoomPercentSteps == zoomPercentSteps && nextZoomStepIndex == zoomStepIndex) {
            return;
        }
        double oldScale = zoomScale();
        double anchorBaseX = anchor.x / oldScale;
        double anchorBaseY = anchor.y / oldScale;
        int anchorViewportX = anchor.x;
        int anchorViewportY = anchor.y;
        Dimension extentSize = null;
        if (getParent() instanceof JViewport viewport) {
            Point oldViewPosition = viewport.getViewPosition();
            extentSize = viewport.getExtentSize();
            anchorBaseX = anchor.x / oldScale;
            anchorBaseY = anchor.y / oldScale;
            anchorViewportX = anchor.x - oldViewPosition.x;
            anchorViewportY = anchor.y - oldViewPosition.y;
        }

        zoomPercentSteps = nextZoomPercentSteps;
        zoomStepIndex = nextZoomStepIndex;
        revalidate();
        if (getParent() instanceof JViewport viewport && extentSize != null) {
            Dimension viewSize = getPreferredSize();
            viewport.setViewSize(viewSize);
            double newScale = zoomScale();
            int newX = (int) Math.round(anchorBaseX * newScale - anchorViewportX);
            int newY = (int) Math.round(anchorBaseY * newScale - anchorViewportY);
            viewport.setViewPosition(new Point(
                    Math.clamp(newX, 0, Math.max(0, viewSize.width - extentSize.width)),
                    Math.clamp(newY, 0, Math.max(0, viewSize.height - extentSize.height))
            ));
        }
        repaint();
        viewportGeometryListener.run();
    }

    /**
     * Java support center anchor for programmatic zoom configuration changes.
     * not ported.
     */
    private Point zoomAnchorPoint() {
        if (getParent() instanceof JViewport viewport) {
            Rectangle viewRect = viewport.getViewRect();
            return new Point(viewRect.x + viewRect.width / 2, viewRect.y + viewRect.height / 2);
        }
        return new Point(Math.max(0, getWidth() / 2), Math.max(0, getHeight() / 2));
    }

    /**
     * Java support parsing for the editor-owned viewport zoom-level list.
     * not ported.
     */
    private static int[] parseZoomPercentSteps(String zoomLevelsText) {
        if (zoomLevelsText == null || zoomLevelsText.isBlank()) {
            throw new IllegalArgumentException("Enter at least two zoom percentages.");
        }
        String[] tokens = zoomLevelsText.split("[,\\s]+");
        int[] parsed = new int[tokens.length];
        int count = 0;
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            int value = parseZoomPercentToken(token);
            if (value < MIN_ZOOM_PERCENT || value > MAX_ZOOM_PERCENT) {
                throw new IllegalArgumentException(
                        "Zoom percentage must be between " + MIN_ZOOM_PERCENT + " and " + MAX_ZOOM_PERCENT + "."
                );
            }
            parsed[count++] = value;
        }
        if (count < 2) {
            throw new IllegalArgumentException("Enter at least two zoom percentages.");
        }
        int[] normalized = Arrays.copyOf(parsed, count);
        Arrays.sort(normalized);
        int uniqueCount = 0;
        boolean hasDefaultZoom = false;
        for (int value : normalized) {
            if (uniqueCount == 0 || normalized[uniqueCount - 1] != value) {
                normalized[uniqueCount++] = value;
                hasDefaultZoom |= value == DEFAULT_ZOOM_PERCENT;
            }
        }
        if (!hasDefaultZoom) {
            throw new IllegalArgumentException("Zoom percentages must include " + DEFAULT_ZOOM_PERCENT + ".");
        }
        if (uniqueCount < 2) {
            throw new IllegalArgumentException("Enter at least two different zoom percentages.");
        }
        return Arrays.copyOf(normalized, uniqueCount);
    }

    /**
     * Java support integer parsing for one editor viewport zoom token.
     * not ported.
     */
    private static int parseZoomPercentToken(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid zoom percentage: " + token, exception);
        }
    }

    /**
     * Java support nearest index lookup for a configured zoom ladder.
     * not ported.
     */
    private static int zoomStepIndexForPercent(int[] zoomPercentSteps, int zoomPercent) {
        int bestIndex = 0;
        int bestDistance = Math.abs(zoomPercentSteps[0] - zoomPercent);
        for (int i = 1; i < zoomPercentSteps.length; i++) {
            int distance = Math.abs(zoomPercentSteps[i] - zoomPercent);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /**
     * Java support base-coordinate repaint conversion after Swing presentation zoom.
     * not ported.
     */
    private void repaintBaseBounds(Rectangle baseBounds, int basePadding) {
        Rectangle paddedBaseBounds = new Rectangle(
                baseBounds.x - basePadding,
                baseBounds.y - basePadding,
                baseBounds.width + basePadding * 2,
                baseBounds.height + basePadding * 2
        );
        Dimension baseSize = mapBaseCanvasSize(document.scenario());
        Rectangle orientedBounds = viewRotation.baseToOrientedBounds(
                paddedBaseBounds,
                baseSize.width,
                baseSize.height
        );
        int left = baseToViewCoordinate(orientedBounds.x);
        int top = baseToViewCoordinate(orientedBounds.y);
        int right = baseToViewCoordinate(orientedBounds.x + orientedBounds.width);
        int bottom = baseToViewCoordinate(orientedBounds.y + orientedBounds.height);
        repaint(left, top, Math.max(1, right - left), Math.max(1, bottom - top));
    }

    /**
     * Java support virtual rotation state application without mutating loaded map data.
     * not ported.
     */
    private void applyViewRotation(MapEditorViewRotation nextRotation) {
        if (viewRotation == nextRotation) {
            return;
        }
        Point anchorBasePoint = mapBasePointAtViewCenter();
        viewRotation = nextRotation;
        revalidate();
        if (anchorBasePoint != null) {
            centerViewportOnBasePoint(anchorBasePoint);
        }
        repaint();
        viewportGeometryListener.run();
    }

    /**
     * Java support viewport centering around one unrotated base-map rectangle.
     * not ported.
     */
    void centerViewportOnBaseBounds(Rectangle baseBounds) {
        centerViewportOnBasePoint(new Point(
                baseBounds.x + baseBounds.width / 2,
                baseBounds.y + baseBounds.height / 2
        ));
    }

    /**
     * Java support viewport centering around one map cell through the active virtual rotation.
     * not ported.
     */
    void centerViewportOnMapCell(int tileX, int tileY) {
        if (document == null) {
            return;
        }
        ScenarioDescriptor scenario = document.scenario();
        int clampedTileX = Math.clamp(tileX, 0, scenario.mapWidth - 1);
        int clampedTileY = Math.clamp(tileY, 0, scenario.mapHeight - 1);
        centerViewportOnBasePoint(new Point(
                clampedTileX * DEFAULT_CELL_PIXEL_SIZE + DEFAULT_CELL_PIXEL_SIZE / 2,
                MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario)
                        + clampedTileY * DEFAULT_CELL_PIXEL_SIZE
                        + DEFAULT_CELL_PIXEL_SIZE / 2
        ));
    }

    /**
     * Java support visible map-pixel bounds in unrotated map coordinates for minimap viewport markers.
     * not ported.
     */
    Rectangle visibleMapPixelBounds(ScenarioDescriptor scenario) {
        Rectangle orientedVisible = viewToBaseRectangle(getVisibleRect());
        Dimension baseSize = mapBaseCanvasSize(scenario);
        Rectangle baseVisible = viewRotation.orientedToBaseBounds(
                orientedVisible,
                baseSize.width,
                baseSize.height
        );
        int mapPixelWidth = Math.max(1, scenario.mapWidth * DEFAULT_CELL_PIXEL_SIZE);
        int mapPixelHeight = Math.max(1, scenario.mapHeight * DEFAULT_CELL_PIXEL_SIZE);
        int mapTop = MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario);
        int left = Math.clamp(baseVisible.x, 0, mapPixelWidth);
        int top = Math.clamp(baseVisible.y - mapTop, 0, mapPixelHeight);
        int right = Math.clamp(baseVisible.x + baseVisible.width, 0, mapPixelWidth);
        int bottom = Math.clamp(baseVisible.y + baseVisible.height - mapTop, 0, mapPixelHeight);
        return new Rectangle(left, top, Math.max(0, right - left), Math.max(0, bottom - top));
    }

    /**
     * Java support viewport centering around one unrotated base-map point.
     * not ported.
     */
    private void centerViewportOnBasePoint(Point basePoint) {
        if (document == null || !(getParent() instanceof JViewport viewport)) {
            return;
        }
        ScenarioDescriptor scenario = document.scenario();
        Dimension baseSize = mapBaseCanvasSize(scenario);
        Point orientedPoint = viewRotation.baseToOrientedPoint(
                basePoint.x,
                basePoint.y,
                baseSize.width,
                baseSize.height
        );
        Dimension extent = viewport.getExtentSize();
        Dimension viewSize = getPreferredSize();
        viewport.setViewSize(viewSize);
        int viewX = baseToViewCoordinate(orientedPoint.x) - extent.width / 2;
        int viewY = baseToViewCoordinate(orientedPoint.y) - extent.height / 2;
        viewport.setViewPosition(new Point(
                Math.clamp(viewX, 0, Math.max(0, viewSize.width - extent.width)),
                Math.clamp(viewY, 0, Math.max(0, viewSize.height - extent.height))
        ));
    }

    /**
     * Java support current viewport-center conversion into unrotated base-map coordinates.
     * not ported.
     */
    private Point mapBasePointAtViewCenter() {
        if (document == null) {
            return null;
        }
        Rectangle visible = getVisibleRect();
        return viewToMapBasePoint(
                visible.x + visible.width / 2,
                visible.y + visible.height / 2,
                document.scenario()
        );
    }

    /**
     * Java support conversion from scaled/rotated Swing coordinates to unrotated base-map pixels.
     * not ported.
     */
    private Point viewToMapBasePoint(int viewX, int viewY, ScenarioDescriptor scenario) {
        Dimension baseSize = mapBaseCanvasSize(scenario);
        return viewRotation.orientedToBasePoint(
                viewToBaseCoordinate(viewX),
                viewToBaseCoordinate(viewY),
                baseSize.width,
                baseSize.height
        );
    }

    /**
     * Java support unrotated base-map canvas dimensions before Swing zoom or virtual rotation.
     * not ported.
     */
    private static Dimension mapBaseCanvasSize(ScenarioDescriptor scenario) {
        return new Dimension(
                Math.max(1, scenario.mapWidth * DEFAULT_CELL_PIXEL_SIZE),
                Math.max(1,
                        MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario)
                                + scenario.mapHeight * DEFAULT_CELL_PIXEL_SIZE
                                + MapEditorTerrainPreviewRenderer.viewportBottomPadding(scenario)
                )
        );
    }

    /**
     * Java support current Swing presentation zoom scale.
     * not ported.
     */
    private double zoomScale() {
        return zoomPercent() / 100.0;
    }

    /**
     * Java support conversion from base native map pixels to scaled view pixels.
     * not ported.
     */
    private int scaleBaseLength(int baseLength) {
        return Math.max(1, (int) Math.round(baseLength * zoomScale()));
    }

    /**
     * Java support conversion from base native coordinate to scaled view coordinate.
     * not ported.
     */
    private int baseToViewCoordinate(int baseCoordinate) {
        return (int) Math.floor(baseCoordinate * zoomScale());
    }

    /**
     * Java support conversion from scaled view coordinate to base native coordinate.
     * not ported.
     */
    private int viewToBaseCoordinate(int viewCoordinate) {
        return (int) Math.floor(viewCoordinate / zoomScale());
    }

    /**
     * Java support conversion from a scaled Swing clip rectangle to base native drawing coordinates.
     * not ported.
     */
    private Rectangle viewToBaseRectangle(Rectangle viewBounds) {
        int left = viewToBaseCoordinate(viewBounds.x);
        int top = viewToBaseCoordinate(viewBounds.y);
        int right = (int) Math.ceil((viewBounds.x + viewBounds.width) / zoomScale());
        int bottom = (int) Math.ceil((viewBounds.y + viewBounds.height) / zoomScale());
        return new Rectangle(left, top, Math.max(1, right - left), Math.max(1, bottom - top));
    }

    /**
     * Editor-only virtual view rotation for map presentation and input mapping.
     * not ported.
     */
    enum MapEditorViewRotation {
        NONE,
        CLOCKWISE_90,
        HALF_TURN,
        COUNTER_CLOCKWISE_90;

        /**
         * Java support next clockwise rotation state.
         * not ported.
         */
        private MapEditorViewRotation clockwise() {
            return switch (this) {
                case NONE -> CLOCKWISE_90;
                case CLOCKWISE_90 -> HALF_TURN;
                case HALF_TURN -> COUNTER_CLOCKWISE_90;
                case COUNTER_CLOCKWISE_90 -> NONE;
            };
        }

        /**
         * Java support next counter-clockwise rotation state.
         * not ported.
         */
        private MapEditorViewRotation counterClockwise() {
            return switch (this) {
                case NONE -> COUNTER_CLOCKWISE_90;
                case COUNTER_CLOCKWISE_90 -> HALF_TURN;
                case HALF_TURN -> CLOCKWISE_90;
                case CLOCKWISE_90 -> NONE;
            };
        }

        /**
         * Java support oriented canvas size for one unrotated base-map canvas.
         * not ported.
         */
        private Dimension orientedCanvasSize(Dimension baseSize) {
            return switch (this) {
                case NONE, HALF_TURN -> new Dimension(baseSize);
                case CLOCKWISE_90, COUNTER_CLOCKWISE_90 -> new Dimension(baseSize.height, baseSize.width);
            };
        }

        /**
         * Java support transform from unrotated base-map coordinates to the oriented view canvas.
         * not ported.
         */
        private void applyBaseToOrientedTransform(Graphics2D graphics, int baseWidth, int baseHeight) {
            switch (this) {
                case NONE -> {
                }
                case CLOCKWISE_90 -> {
                    graphics.translate(baseHeight, 0);
                    graphics.rotate(Math.PI / 2.0);
                }
                case HALF_TURN -> {
                    graphics.translate(baseWidth, baseHeight);
                    graphics.rotate(Math.PI);
                }
                case COUNTER_CLOCKWISE_90 -> {
                    graphics.translate(0, baseWidth);
                    graphics.rotate(-Math.PI / 2.0);
                }
            }
        }

        /**
         * Java support base-map point conversion into oriented canvas coordinates.
         * not ported.
         */
        private Point baseToOrientedPoint(int x, int y, int baseWidth, int baseHeight) {
            return switch (this) {
                case NONE -> new Point(x, y);
                case CLOCKWISE_90 -> new Point(baseHeight - y, x);
                case HALF_TURN -> new Point(baseWidth - x, baseHeight - y);
                case COUNTER_CLOCKWISE_90 -> new Point(y, baseWidth - x);
            };
        }

        /**
         * Java support oriented canvas point conversion back to unrotated base-map coordinates.
         * not ported.
         */
        private Point orientedToBasePoint(int x, int y, int baseWidth, int baseHeight) {
            return switch (this) {
                case NONE -> new Point(x, y);
                case CLOCKWISE_90 -> new Point(y, baseHeight - x);
                case HALF_TURN -> new Point(baseWidth - x, baseHeight - y);
                case COUNTER_CLOCKWISE_90 -> new Point(baseWidth - y, x);
            };
        }

        /**
         * Java support base-map rectangle conversion into an oriented canvas bounding rectangle.
         * not ported.
         */
        private Rectangle baseToOrientedBounds(Rectangle bounds, int baseWidth, int baseHeight) {
            return transformBounds(bounds, baseWidth, baseHeight, true);
        }

        /**
         * Java support oriented canvas rectangle conversion into an unrotated base-map bounding rectangle.
         * not ported.
         */
        private Rectangle orientedToBaseBounds(Rectangle bounds, int baseWidth, int baseHeight) {
            return transformBounds(bounds, baseWidth, baseHeight, false);
        }

        /**
         * Java support bounding rectangle transformation for the four rectangle corners.
         * not ported.
         */
        private Rectangle transformBounds(Rectangle bounds, int baseWidth, int baseHeight, boolean baseToOriented) {
            Point first = transformPoint(bounds.x, bounds.y, baseWidth, baseHeight, baseToOriented);
            Point second = transformPoint(bounds.x + bounds.width, bounds.y, baseWidth, baseHeight, baseToOriented);
            Point third = transformPoint(bounds.x, bounds.y + bounds.height, baseWidth, baseHeight, baseToOriented);
            Point fourth = transformPoint(
                    bounds.x + bounds.width,
                    bounds.y + bounds.height,
                    baseWidth,
                    baseHeight,
                    baseToOriented
            );
            int left = Math.min(Math.min(first.x, second.x), Math.min(third.x, fourth.x));
            int top = Math.min(Math.min(first.y, second.y), Math.min(third.y, fourth.y));
            int right = Math.max(Math.max(first.x, second.x), Math.max(third.x, fourth.x));
            int bottom = Math.max(Math.max(first.y, second.y), Math.max(third.y, fourth.y));
            return new Rectangle(left, top, Math.max(1, right - left), Math.max(1, bottom - top));
        }

        /**
         * Java support point transformation in the requested direction.
         * not ported.
         */
        private Point transformPoint(int x, int y, int baseWidth, int baseHeight, boolean baseToOriented) {
            if (baseToOriented) {
                return baseToOrientedPoint(x, y, baseWidth, baseHeight);
            }
            return orientedToBasePoint(x, y, baseWidth, baseHeight);
        }
    }

    /**
     * Java support ARGB color mapping for compact map-preview cells.
     * not ported.
     */
    static int argbForCell(ScenarioDescriptor scenario, int index) {
        int tile = scenario.sec1Tiles[index] & 0xFFFF;
        int height = Byte.toUnsignedInt(scenario.sec2Heights[index]);
        int object = Byte.toUnsignedInt(scenario.sec3Objects[index]);
        int red = 42 + ((tile * 11 + object * 17) & 0x3F);
        int green = 76 + ((height + tile * 3) & 0x5F);
        int blue = 48 + ((tile * 7 + height / 2) & 0x3F);
        return 0xFF000000
                | (Math.min(red, 180) << 16)
                | (Math.min(green, 190) << 8)
                | Math.min(blue, 170);
    }
}
