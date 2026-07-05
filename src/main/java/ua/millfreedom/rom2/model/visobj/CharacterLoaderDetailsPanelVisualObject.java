package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CGameSession;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.*;

/**
 * Native class: CharacterLoaderDetailsPanelVisualObject (vtbl @0x005CB730).
 * Purpose: character-loader details/stats pane used as child `id=0x462` under dialog `0x460`.
 */
public class CharacterLoaderDetailsPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0xD0; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int DETAILS_X_OFFSET = 0x0C;
    private static final int DETAILS_Y_OFFSET = 0xEE;
    private static final int SUMMARY_TEXT_CENTER_OFFSET = 0x0C;
    private static final int SUMMARY_TEXT_ALIGN = TextAlign.combine(TextAlign.CENTER, TextAlign.VERTICAL_CENTER);
    private static final int SUMMARY_TEXT_SHADOW_OFFSET = 1;
    private static final String FULL_STATSL_BMP = "graphics/interface/chrgen/fullstatsl.bmp";
    private static final String LOADER_LEFTUP_BMP = "graphics/interface/chrgen/loader/leftup.bmp";
    private static final String CUBE_SPRITES_16A = "graphics/interface/chrgen/cube/sprites.16a";

    //0x5c
    public CharacterLoaderDialogVisualObject ownerDialog;

    //0x60
    public CBmp64k bottomPanelBitmap;

    //0x64
    public CBmp64k topPanelBitmap;

    //0x68
    public final String[] primaryAttributeLabels = new String[4];

    //0x7c
    public int bodyValue;

    //0x80
    public int reactionValue;

    //0x84
    public int mindValue;

    //0x88
    public int spiritValue;

    //0x8c
    public CA16 statCubeSpriteSheet;

    //0x90
    public final CRect[] primaryAttributeRowRects = {new CRect(), new CRect(), new CRect(), new CRect()};

    /**
     * Native: CharacterLoaderDetailsPanelVisualObject::CharacterLoaderDetailsPanelVisualObject @0042E7AE.
     * Fully ported.
     */
    public CharacterLoaderDetailsPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CharacterLoaderDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeDetailsPanel();
    }

    /**
     * Native: CharacterLoaderDetailsPanelVisualObject::InitializeDetailsPanel @0042E8AA.
     * Fully ported.
     */
    private void initializeDetailsPanel() {
        bottomPanelBitmap = null;
        topPanelBitmap = null;
        statCubeSpriteSheet = null;

        primaryAttributeLabels[0] = get(MAIN_BODY_15);
        primaryAttributeLabels[1] = get(MAIN_AGILITY_16);
        primaryAttributeLabels[2] = get(MAIN_MIND_17);
        primaryAttributeLabels[3] = get(MAIN_SPIRIT_18);

        primaryAttributeRowRects[0].set(0x70, 0x31, 0x90, 0x51);
        primaryAttributeRowRects[1].set(0x70, 0x52, 0x90, 0x72);
        primaryAttributeRowRects[2].set(0x70, 0x73, 0x90, 0x93);
        primaryAttributeRowRects[3].set(0x70, 0x94, 0x90, 0xB4);

        bodyValue = 0x1E;
        reactionValue = 0x1F;
        mindValue = 0x20;
        spiritValue = 0x21;
    }

    /**
     * vtbl +0x2C: CharacterLoaderDetailsPanelVisualObject::Update @0042EE73.
     * Fully ported.
     */
    @Override
    public void update() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            drawPanelGraphic(topPanelBitmap, screenRect.left, screenRect.top);
            drawPanelGraphic(
                    bottomPanelBitmap,
                    screenRect.left,
                    screenRect.top + DETAILS_Y_OFFSET
            );
            if (ownerDialog.rosterListPanel.selectedEntryIndex
                    == ownerDialog.gameSession.getCharacterRosterEntryCount() - 1) {
                return;
            }
            drawSummaryRows(screenRect);
            ownerDialog.renderSelectedUnitDetails(getDetailsScreenRect(screenRect));
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x14: CharacterLoaderDetailsPanelVisualObject::GetText @0042F31B.
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
        return ownerDialog.getSelectedUnitDetailsTooltip(localX, localY);
    }

    /**
     * Native support extracted from CharacterLoaderDetailsPanelVisualObject::Update @0042EE73.
     */
    private static CRect getDetailsScreenRect(CRect screenRect) {
        return new CRect(
                screenRect.left + DETAILS_X_OFFSET,
                screenRect.top + DETAILS_Y_OFFSET,
                screenRect.right + DETAILS_X_OFFSET,
                screenRect.bottom
        );
    }

    /**
     * Native support extracted from CharacterLoaderDetailsPanelVisualObject::Update @0042EE73.
     */
    private void drawSummaryRows(CRect screenRect) {
        CGameSession gameSession = ownerDialog.gameSession;
        drawSummaryRow(screenRect, 0x35, gameSession.characterGold, get(MAIN_GOLD_89));
        drawSummaryRow(screenRect, 0x57, gameSession.monstersKilled, get(MAIN_MONSTERS_KILLED_244));
        drawSummaryRow(screenRect, 0x79, gameSession.playersKilled, get(MAIN_PLAYERS_KILLED_245));
        drawSummaryRow(screenRect, 0x9B, gameSession.deathCount, get(MAIN_DIED_246));
    }

    /**
     * Native support extracted from CharacterLoaderDetailsPanelVisualObject::Update @0042EE73.
     */
    private void drawSummaryRow(CRect screenRect, int rowY, int value, String label) {
        int centerX = screenRect.left + (cRect.width() + SUMMARY_TEXT_CENTER_OFFSET) / 2;
        int labelY = screenRect.top + rowY;
        int valueY = labelY + Globals.fonts.font2.getHeight() + Globals.fonts.font4.getFrameHeight() / 2;
        Globals.fonts.font4.drawTextInternal(
                centerX,
                valueY,
                Integer.toString(value),
                SUMMARY_TEXT_ALIGN,
                Palettes.p1.paletteData[0]
        );
        Globals.fonts.font2.drawTextShadowed(
                centerX,
                labelY,
                label,
                SUMMARY_TEXT_ALIGN,
                Palettes.yellowish,
                SUMMARY_TEXT_SHADOW_OFFSET
        );
    }

    /**
     * Native: CharacterLoaderDetailsPanelVisualObject::RefreshSelectedUnitPrimaryAttributes @0042EDB5.
     * Fully ported. Native caches the selected unit's primary attributes for the details pane, or clears them when no unit is selected.
     */
    void refreshSelectedUnitPrimaryAttributes() {
        CUnit selectedUnit = ownerDialog.getSelectedUnit();
        if (selectedUnit == null) {
            bodyValue = 0;
            reactionValue = 0;
            mindValue = 0;
            spiritValue = 0;
            return;
        }

        bodyValue = Byte.toUnsignedInt(selectedUnit.body);
        reactionValue = Byte.toUnsignedInt(selectedUnit.reaction);
        mindValue = Byte.toUnsignedInt(selectedUnit.mind);
        spiritValue = Byte.toUnsignedInt(selectedUnit.spirit);
    }

    /**
     * Native: CharacterLoaderDetailsPanelVisualObject::LoadDetailsResources @0042EBA1.
     * Fully ported.
     */
    void loadDetailsResources() {
        releaseDetailsResources();
        bottomPanelBitmap = new CBmp64k(FULL_STATSL_BMP);
        topPanelBitmap = new CBmp64k(LOADER_LEFTUP_BMP);
        statCubeSpriteSheet = new CA16(CUBE_SPRITES_16A);
        statCubeSpriteSheet.initPalette(0x10, 4, 0);
    }

    /**
     * Native: CharacterLoaderDetailsPanelVisualObject::ReleaseDetailsResources @0042ECDC.
     * Fully ported. Java clears retained bitmap references instead of emulating native delete semantics.
     */
    void releaseDetailsResources() {
        topPanelBitmap = null;
        bottomPanelBitmap = null;
        statCubeSpriteSheet = null;
    }

    /**
     * Native support boundary for the `CGameBitmap::Draw` calls in CharacterLoaderDetailsPanelVisualObject::Update @0042EE73.
     * Fully ported.
     */
    private static void drawPanelGraphic(CBmp64k graphic, int x, int y) {
        graphic.draw(x, y, 0, null, false);
    }

}
