package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;

/**
 * Native class: FullStatsPanelVisualObject (vtbl @0x005CB540).
 * Purpose: full stats panel that renders the current unit's detailed stats and tooltips.
 */
public class FullStatsPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0xA8; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final String FULL_STATS_L_BMP = "graphics/Interface/chrgen/FullStatsL.bmp";
    private static final int DETAILS_X_OFFSET = 0x0C;
    private static final int DETAILS_Y_OFFSET = 0x0C;

    //0x5c
    public CharacterGeneratorDialogVisualObject ownerDialog;
    //0x60
    public CBmp64k fullStatsBitmap;

    /**
     * Native: FullStatsPanelVisualObject::FullStatsPanelVisualObject @0042A0F1.
     * Fully ported.
     */
    public FullStatsPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CharacterGeneratorDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeFullStatsPanelFields();
    }

    /**
     * Native: FullStatsPanelVisualObject::InitializeFullStatsPanelFields @0042A1C1.
     * Fully ported.
     */
    private void initializeFullStatsPanelFields() {
        fullStatsBitmap = null;
    }

    /**
     * Native: FullStatsPanelVisualObject::InitializeFullStatsPanel @0042A1DE.
     * Fully ported.
     */
    void initializeFullStatsPanel() {
        releaseFullStatsBitmap();
        this.fullStatsBitmap = loadFullStatsBitmap();
    }

    /**
     * vtbl +0x2C: FullStatsPanelVisualObject::Update @0042A2B0.
     * Fully ported.
     */
    @Override
    public void update() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            fullStatsBitmap.draw(screenRect.left, screenRect.top, 0, null, false);
            ownerDialog.renderFullStatsPanel(getStatsScreenRect(screenRect));
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x14: FullStatsPanelVisualObject::GetText @0042A34B.
     * Fully ported.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return null;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int localX = Globals.mousePointer.getX() - screenRect.left - DETAILS_X_OFFSET;
        int localY = Globals.mousePointer.getY() - screenRect.top - DETAILS_Y_OFFSET;
        return ownerDialog.getFullStatsPanelTooltipText(localX, localY);
    }

    /**
     * Native support extracted from FullStatsPanelVisualObject::InitializeFullStatsPanel @0042A1DE.
     */
    private static CBmp64k loadFullStatsBitmap() {
        return new CBmp64k(FULL_STATS_L_BMP);
    }

    /**
     * Native: FullStatsPanelVisualObject::ReleaseFullStatsBitmap @0042A262.
     * Fully ported. Java clears the retained bitmap reference instead of emulating native delete semantics.
     */
    void releaseFullStatsBitmap() {
        fullStatsBitmap = null;
    }

    /**
     * Native helper path shared by FullStatsPanelVisualObject::Update @0042A2B0.
     */
    private static CRect getStatsScreenRect(CRect screenRect) {
        return new CRect(
                screenRect.left + DETAILS_X_OFFSET,
                screenRect.top,
                screenRect.right + DETAILS_X_OFFSET,
                screenRect.bottom
        );
    }
}
