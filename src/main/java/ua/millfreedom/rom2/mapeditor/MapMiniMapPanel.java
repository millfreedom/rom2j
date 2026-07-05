package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.ScenarioDescriptor;

import javax.swing.JPanel;
import javax.swing.JViewport;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * Standalone editor minimap for navigating the scrollable map viewport.
 * not ported.
 */
final class MapMiniMapPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int PREFERRED_WIDTH = 180;
    private static final int PREFERRED_HEIGHT = 196;
    private static final int HEADER_HEIGHT = 20;
    private static final int INSET = 8;

    private final MapPreviewPanel previewPanel;
    private final MapEditorMiniMapOverviewRenderer overviewRenderer = new MapEditorMiniMapOverviewRenderer();
    private MapMiniMapScale scale = MapMiniMapScale.DOUBLE;
    private MapEditorDocument document;
    private MapEditorEntitySelection selectedEntity;
    private MapEditorAreaSelection areaSelection;
    private Point currentCell;
    private BufferedImage overviewImage;
    private int overviewWidth = -1;
    private int overviewHeight = -1;
    private int overviewAnimationPhase = Integer.MIN_VALUE;

    /**
     * Java support constructor binding the minimap to the scrollable map canvas.
     * not ported.
     */
    MapMiniMapPanel(MapPreviewPanel previewPanel) {
        this.previewPanel = previewPanel;
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        setOpaque(true);
        MouseAdapter mouseAdapter = new MouseAdapter() {
            /**
             * Java support minimap press navigation.
             * not ported.
             */
            @Override
            public void mousePressed(MouseEvent event) {
                centerViewportAt(event.getX(), event.getY());
            }

            /**
             * Java support minimap drag navigation.
             * not ported.
             */
            @Override
            public void mouseDragged(MouseEvent event) {
                centerViewportAt(event.getX(), event.getY());
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    /**
     * Java support document binding for minimap overview repaint.
     * not ported.
     */
    void setDocument(MapEditorDocument document) {
        this.document = document;
        invalidateOverview();
        revalidate();
    }

    /**
     * Java support selected saved-entity binding for minimap highlight painting.
     * not ported.
     */
    void setSelectedEntity(MapEditorEntitySelection selectedEntity) {
        this.selectedEntity = selectedEntity;
        repaint();
    }

    /**
     * Java support selected map-area binding for minimap highlight painting.
     * not ported.
     */
    void setAreaSelection(MapEditorAreaSelection areaSelection) {
        this.areaSelection = areaSelection;
        repaint();
    }

    /**
     * Java support current-cell binding for minimap marker painting.
     * not ported.
     */
    void setCurrentCell(Point currentCell) {
        this.currentCell = currentCell == null ? null : new Point(currentCell);
        repaint();
    }

    /**
     * Java support cache invalidation after map-data mutations that affect minimap colors.
     * not ported.
     */
    void invalidateOverview() {
        overviewImage = null;
        overviewWidth = -1;
        overviewHeight = -1;
        overviewAnimationPhase = Integer.MIN_VALUE;
        repaint();
    }

    /**
     * Java support accessor for the active editor minimap scale.
     * not ported.
     */
    MapMiniMapScale scale() {
        return scale;
    }

    /**
     * Java support setter for native Help-aligned editor minimap scale commands.
     * not ported.
     */
    void setScale(MapMiniMapScale scale) {
        this.scale = scale;
        invalidateOverview();
        revalidate();
    }

    /**
     * Java support preferred minimap size derived from active map dimensions and scale.
     * not ported.
     */
    @Override
    public Dimension getPreferredSize() {
        if (document == null) {
            return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        }
        ScenarioDescriptor scenario = document.scenario();
        return new Dimension(
                scale.scaledSpan(scenario.mapWidth) + INSET * 2,
                scale.scaledSpan(scenario.mapHeight) + HEADER_HEIGHT + INSET * 2
        );
    }

    /**
     * Java support minimap overview and viewport rectangle painting.
     * not ported.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            drawFrame(g);
            if (document == null) {
                drawEmpty(g);
                return;
            }
            ScenarioDescriptor scenario = document.scenario();
            int[] bounds = minimapBounds(scenario);
            drawOverview(g, scenario, bounds);
            MapEditorEntityOverlayRenderer.drawMinimapOverlay(g, scenario, bounds);
            MapEditorEntityOverlayRenderer.drawMinimapSelection(
                    g,
                    scenario,
                    bounds,
                    selectedEntity,
                    previewPanel.currentEffectSelectionAnimationTick()
            );
            drawAreaSelectionMarker(g, scenario, bounds);
            drawCurrentCellMarker(g, scenario, bounds);
            drawViewportRectangle(g, scenario, bounds);
        } finally {
            g.dispose();
        }
    }

    /**
     * Java support minimap frame drawing matching the compact native editor utility window.
     * not ported.
     */
    private void drawFrame(Graphics2D g) {
        g.setColor(new Color(198, 198, 198));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(226, 226, 226));
        g.fillRect(1, 1, getWidth() - 2, HEADER_HEIGHT - 1);
        g.setColor(new Color(104, 104, 104));
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        g.setColor(new Color(88, 88, 88));
        g.drawString("MiniMap " + scale.label(), 6, 15);
        g.setColor(new Color(12, 12, 16));
        g.fillRect(INSET, HEADER_HEIGHT + INSET, getWidth() - INSET * 2, getHeight() - HEADER_HEIGHT - INSET * 2);
    }

    /**
     * Java support empty minimap state for no loaded document.
     * not ported.
     */
    private void drawEmpty(Graphics2D g) {
        g.setColor(new Color(118, 124, 132));
        g.drawString("No map", INSET + 4, HEADER_HEIGHT + INSET + 18);
    }

    /**
     * Java support map-area bounds inside the minimap component.
     * not ported.
     */
    private int[] minimapBounds(ScenarioDescriptor scenario) {
        int availableWidth = Math.max(1, getWidth() - INSET * 2);
        int availableHeight = Math.max(1, getHeight() - HEADER_HEIGHT - INSET * 2);
        int drawWidth = Math.min(availableWidth, scale.scaledSpan(scenario.mapWidth));
        int drawHeight = Math.min(availableHeight, scale.scaledSpan(scenario.mapHeight));
        int left = (getWidth() - drawWidth) / 2;
        int top = HEADER_HEIGHT + INSET + Math.max(0, (availableHeight - drawHeight) / 2);
        return new int[]{left, top, drawWidth, drawHeight};
    }

    /**
     * Java support minimap overview drawing from current terrain/object arrays.
     * not ported.
     */
    private void drawOverview(Graphics2D g, ScenarioDescriptor scenario, int[] bounds) {
        int left = bounds[0];
        int top = bounds[1];
        int width = bounds[2];
        int height = bounds[3];
        g.drawImage(overviewImage(scenario, width, height, previewPanel.currentPreviewAnimationTick()), left, top, null);
        g.setColor(new Color(235, 235, 235));
        g.drawRect(left, top, width, height);
    }

    /**
     * Java support cached overview image for the editor minimap paint path.
     * not ported.
     */
    private BufferedImage overviewImage(ScenarioDescriptor scenario, int width, int height, int animationTick) {
        int animationPhase = MapEditorMiniMapOverviewRenderer.terrainAnimationPhase(animationTick);
        if (overviewImage == null
                || overviewWidth != width
                || overviewHeight != height
                || overviewAnimationPhase != animationPhase) {
            overviewImage = overviewRenderer.buildOverviewImage(scenario, width, height, animationTick);
            overviewWidth = width;
            overviewHeight = height;
            overviewAnimationPhase = animationPhase;
        }
        return overviewImage;
    }

    /**
     * Java support viewport rectangle drawing over the minimap overview.
     * not ported.
     */
    private void drawViewportRectangle(Graphics2D g, ScenarioDescriptor scenario, int[] bounds) {
        Rectangle visibleMapBounds = previewPanel.visibleMapPixelBounds(scenario);
        int mapPixelWidth = Math.max(1, scenario.mapWidth * previewPanel.baseTilePixelSize());
        int mapPixelHeight = Math.max(1, scenario.mapHeight * previewPanel.baseTilePixelSize());
        int rectX = bounds[0] + (int) ((long) visibleMapBounds.x * bounds[2] / mapPixelWidth);
        int rectY = bounds[1] + (int) ((long) visibleMapBounds.y * bounds[3] / mapPixelHeight);
        int rectWidth = Math.max(3, (int) ((long) visibleMapBounds.width * bounds[2] / mapPixelWidth));
        int rectHeight = Math.max(3, (int) ((long) visibleMapBounds.height * bounds[3] / mapPixelHeight));
        rectX = clamp(rectX, bounds[0], bounds[0] + bounds[2] - 1);
        rectY = clamp(rectY, bounds[1], bounds[1] + bounds[3] - 1);
        rectWidth = Math.min(rectWidth, bounds[0] + bounds[2] - rectX);
        rectHeight = Math.min(rectHeight, bounds[1] + bounds[3] - rectY);
        g.setColor(new Color(255, 255, 255));
        g.drawRect(rectX, rectY, rectWidth, rectHeight);
        g.setColor(new Color(35, 80, 210));
        g.drawRect(rectX - 1, rectY - 1, rectWidth + 2, rectHeight + 2);
    }

    /**
     * Java support current-cell marker drawing over the minimap overview.
     * not ported.
     */
    private void drawCurrentCellMarker(Graphics2D g, ScenarioDescriptor scenario, int[] bounds) {
        if (currentCell == null
                || currentCell.x < 0
                || currentCell.y < 0
                || currentCell.x >= scenario.mapWidth
                || currentCell.y >= scenario.mapHeight) {
            return;
        }
        int x = bounds[0] + (int) ((long) currentCell.x * bounds[2] / scenario.mapWidth);
        int y = bounds[1] + (int) ((long) currentCell.y * bounds[3] / scenario.mapHeight);
        g.setColor(new Color(0, 0, 0, 230));
        g.drawRect(x - 3, y - 3, 6, 6);
        g.setColor(new Color(85, 235, 255, 245));
        g.drawRect(x - 2, y - 2, 4, 4);
    }

    /**
     * Java support selected map-area marker drawing over the minimap overview.
     * not ported.
     */
    private void drawAreaSelectionMarker(Graphics2D g, ScenarioDescriptor scenario, int[] bounds) {
        if (areaSelection == null) {
            return;
        }
        int left = bounds[0] + (int) ((long) areaSelection.left() * bounds[2] / scenario.mapWidth);
        int top = bounds[1] + (int) ((long) areaSelection.top() * bounds[3] / scenario.mapHeight);
        int right = bounds[0] + (int) ((long) (areaSelection.right() + 1) * bounds[2] / scenario.mapWidth);
        int bottom = bounds[1] + (int) ((long) (areaSelection.bottom() + 1) * bounds[3] / scenario.mapHeight);
        int width = Math.max(2, right - left);
        int height = Math.max(2, bottom - top);
        g.setColor(new Color(50, 150, 255, 72));
        g.fillRect(left, top, width, height);
        g.setColor(new Color(0, 0, 0, 230));
        g.drawRect(left - 1, top - 1, width + 1, height + 1);
        g.setColor(new Color(90, 205, 255, 245));
        g.drawRect(left, top, width, height);
    }

    /**
     * Java support minimap mouse navigation into the scrollable map viewport.
     * not ported.
     */
    private void centerViewportAt(int mouseX, int mouseY) {
        if (document == null) {
            return;
        }
        ScenarioDescriptor scenario = document.scenario();
        int[] bounds = minimapBounds(scenario);
        int clampedMouseX = clamp(mouseX, bounds[0], bounds[0] + bounds[2]);
        int clampedMouseY = clamp(mouseY, bounds[1], bounds[1] + bounds[3]);
        int targetTileX = (int) ((long) (clampedMouseX - bounds[0]) * scenario.mapWidth / Math.max(1, bounds[2]));
        int targetTileY = (int) ((long) (clampedMouseY - bounds[1]) * scenario.mapHeight / Math.max(1, bounds[3]));
        previewPanel.centerViewportOnMapCell(targetTileX, targetTileY);
        repaint();
    }

    /**
     * Java support integer clamp helper for minimap coordinate conversion.
     * not ported.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
