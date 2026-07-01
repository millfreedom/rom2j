package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.text.MainText;

import java.awt.Point;
import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.MainText.*;
import static ua.millfreedom.rom2.text.TextTableId.MAIN;

/**
 * Native class: StatsAllocationPanelVisualObject (vtbl @0x005CB4C8).
 * Purpose: character-generator primary stat allocation panel with plus/minus controls and free-pool display.
 */
public class StatsAllocationPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x1F4; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STAT_COUNT = 4;
    private static final int REGION_NONE = -1;
    private static final int REGION_STAT_VALUE = 0;
    private static final int REGION_SPEND_BUTTON = 1;
    private static final int REGION_REFUND_BUTTON = 2;
    private static final double STAT_COST_BASE = 1.15;
    private static final double STAT_COST_SCALE = 0.349;
    private static final int MIN_STAT_VALUE = 0x10;
    private static final int MAX_STAT_VALUE = 0x2D;
    private static final String STATS_BACKGROUND_BMP = "main/graphics/chrgen/leftup.bmp";
    private static final String BUTTONS_DIR = "graphics/interface/chrgen/buttons";
    private static final String PLUS_PRESSED_BMP = BUTTONS_DIR + "/plon.bmp";
    private static final String PLUS_HOVER_BMP = BUTTONS_DIR + "/ploff.bmp";
    private static final String PLUS_IDLE_ON_BMP = BUTTONS_DIR + "/pnlon.bmp";
    private static final String PLUS_IDLE_BMP = BUTTONS_DIR + "/pnloff.bmp";
    private static final String PLUS_DISABLED_BMP = BUTTONS_DIR + "/pdisable.bmp";
    private static final String MINUS_PRESSED_BMP = BUTTONS_DIR + "/mlon.bmp";
    private static final String MINUS_HOVER_BMP = BUTTONS_DIR + "/mloff.bmp";
    private static final String MINUS_IDLE_ON_BMP = BUTTONS_DIR + "/mnlon.bmp";
    private static final String MINUS_IDLE_BMP = BUTTONS_DIR + "/mnloff.bmp";
    private static final String MINUS_DISABLED_BMP = BUTTONS_DIR + "/mdisable.bmp";

    //0x5c
    public CharacterGeneratorDialogVisualObject ownerDialog;
    //0x60
    public CBmp64k backgroundGraphic;
    //0x64
    public final CRect[] statDescriptionRects = new CRect[STAT_COUNT];
    //0xa4
    public final CRect freePoolRect = new CRect();
    //0xb4
    public final CRect[] statControlRects = new CRect[STAT_COUNT * 3];
    //0x174 / 0x17c / 0x184 / 0x18c
    public final CBmp64k[] currentPlusButtonGraphics = new CBmp64k[STAT_COUNT];
    //0x178 / 0x180 / 0x188 / 0x190
    public final CBmp64k[] currentMinusButtonGraphics = new CBmp64k[STAT_COUNT];
    //0x194
    public CBmp64k plusPressedGraphic;
    //0x198
    public CBmp64k plusHoverGraphic;
    //0x19c
    public CBmp64k plusIdleOnGraphic;
    //0x1a0
    public CBmp64k plusIdleGraphic;
    //0x1a4
    public CBmp64k plusDisabledGraphic;
    //0x1a8
    public CBmp64k minusPressedGraphic;
    //0x1ac
    public CBmp64k minusHoverGraphic;
    //0x1b0
    public CBmp64k minusIdleOnGraphic;
    //0x1b4
    public CBmp64k minusIdleGraphic;
    //0x1b8
    public CBmp64k minusDisabledGraphic;
    //0x1bc
    public final String[] statNames = new String[STAT_COUNT];
    //0x1d0
    public int bodyValue;
    //0x1d4
    public int agilityValue;
    //0x1d8
    public int mindValue;
    //0x1dc
    public int spiritValue;
    //0x1e0
    public int snapshotBodyValue;
    //0x1e4
    public int snapshotAgilityValue;
    //0x1e8
    public int snapshotMindValue;
    //0x1ec
    public int snapshotSpiritValue;
    //0x1f0
    public int freePoolPoints;

    /**
     * Native: StatsAllocationPanelVisualObject::StatsAllocationPanelVisualObject @00428521.
     * Fully ported.
     */
    public StatsAllocationPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CharacterGeneratorDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initializeStatsAllocationPanel();
        this.ownerDialog = ownerDialog;
    }

    /**
     * vtbl +0x14: StatsAllocationPanelVisualObject::GetText @00429B01.
     * Fully ported.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return null;
        }

        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        Point screenOrigin = getScreenOrigin();
        for (int i = 0; i < STAT_COUNT; i++) {
            if (containsTranslatedPoint(statDescriptionRects[i], screenOrigin, mouseX, mouseY)) {
                return get(MAIN, MainText.byIndex(BODY_TOOLTIP_155.index() + i));
            }
        }
        if (containsTranslatedPoint(freePoolRect, screenOrigin, mouseX, mouseY)) {
            return get(MAIN, FREE_POOL_273);
        }
        for (int i = 0; i < STAT_COUNT; i++) {
            if (containsTranslatedPoint(statControlRects[i * 3], screenOrigin, mouseX, mouseY)) {
                return String.format(Locale.US, "%s: %d", statNames[i], getStatValueByIndex(i));
            }
            if (containsTranslatedPoint(statControlRects[i * 3 + 1], screenOrigin, mouseX, mouseY)) {
                return formatSignedStatDelta(-getSpendCost(getStatValueByIndex(i)));
            }
            if (containsTranslatedPoint(statControlRects[i * 3 + 2], screenOrigin, mouseX, mouseY)) {
                return formatSignedStatDelta(getRefundGain(getStatValueByIndex(i)));
            }
        }
        return null;
    }

    /**
     * vtbl +0x2C: StatsAllocationPanelVisualObject::Update @004293CB.
     * Full port. Native background, stat text, button art, and free-pool text rendering are native-aligned.
     */
    @Override
    public void update() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (ownerDialog.dialogActiveFlag == 0) {
            return;
        }

        Globals.renderer.lockSurface();
        try {
            drawBackgroundGraphic(backgroundGraphic, screenRect.left, screenRect.top);
            for (int i = 0; i < STAT_COUNT; i++) {
                drawGFont4TextShadowed(
                        screenRect.left + 2 + statControlRects[i * 3].left,
                        screenRect.top + 4 + statControlRects[i * 3].top,
                        Integer.toString(getStatValueByIndex(i))
                );
                drawSetupButtonGraphic(
                        currentPlusButtonGraphics[i],
                        screenRect.left + statControlRects[i * 3 + 1].left,
                        screenRect.top + statControlRects[i * 3 + 1].top
                );
                drawSetupButtonGraphic(
                        currentMinusButtonGraphics[i],
                        screenRect.left + statControlRects[i * 3 + 2].left,
                        screenRect.top + statControlRects[i * 3 + 2].top
                );
            }
            drawGFont4CenteredTextShadowed(
                    screenRect.left + freePoolRect.left + (freePoolRect.width() / 2),
                    screenRect.top + freePoolRect.top + 2 + (freePoolRect.height() / 2),
                    formatSignedStatDelta(freePoolPoints)
            );
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x4C: StatsAllocationPanelVisualObject::OnMouseMove @004299D8.
     * Full port. Native interaction-state recomputation is delegated through RefreshButtonGraphics.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        refreshButtonGraphics(nFlags, x, y);
        return 0;
    }

    /**
     * vtbl +0x50: StatsAllocationPanelVisualObject::OnUserMsg @00429AB8.
     * Fully ported.
     */
    @Override
    public int onUserMsg(int nFlags, int x, int y) {
        if ((nFlags & 1) == 0) {
            return super.onUserMsg(nFlags, x, y);
        }
        onLButtonDown(nFlags, x, y);
        return 1;
    }

    /**
     * vtbl +0x54: StatsAllocationPanelVisualObject::OnLButtonDown @004299FB.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        int packedHit = refreshButtonGraphics(nFlags, x, y);
        if (packedHit != REGION_NONE) {
            int hitRegion = packedHit & 0xFF;
            int statIndex = packedHit >>> 8;
            if (hitRegion == REGION_SPEND_BUTTON) {
                increaseStatValue(statIndex);
            } else if (hitRegion == REGION_REFUND_BUTTON) {
                decreaseStatValue(statIndex);
            }
        }
        return 1;
    }

    /**
     * vtbl +0x58: StatsAllocationPanelVisualObject::OnLButtonUp @00429A6E.
     * Full port. Native interaction-state recomputation is delegated through RefreshButtonGraphics.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        refreshButtonGraphics(nFlags, x, y);
        return 1;
    }

    /**
     * vtbl +0x5C: StatsAllocationPanelVisualObject::OnLButtonDblClk @00429A94.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return onLButtonDown(nFlags, x, y);
    }

    /**
     * Native: StatsAllocationPanelVisualObject::InitializeStatsAllocationPanel @0042864F.
     * Full port. Geometry, native gFont4 label metrics, and default stat/free-pool state are native-aligned.
     */
    private void initializeStatsAllocationPanel() {
        ownerDialog = null;
        backgroundGraphic = null;
        for (int i = 0; i < STAT_COUNT; i++) {
            statDescriptionRects[i] = new CRect();
            statControlRects[i * 3] = new CRect();
            statControlRects[i * 3 + 1] = new CRect();
            statControlRects[i * 3 + 2] = new CRect();
            currentPlusButtonGraphics[i] = null;
            currentMinusButtonGraphics[i] = null;
        }
        plusPressedGraphic = null;
        plusHoverGraphic = null;
        plusIdleOnGraphic = null;
        plusIdleGraphic = null;
        plusDisabledGraphic = null;
        minusPressedGraphic = null;
        minusHoverGraphic = null;
        minusIdleOnGraphic = null;
        minusIdleGraphic = null;
        minusDisabledGraphic = null;

        bodyValue = 0x1F;
        agilityValue = 0x20;
        mindValue = 0x21;
        spiritValue = 0x22;
        statNames[0] = get(MAIN, BODY_15);
        statNames[1] = get(MAIN, AGILITY_16);
        statNames[2] = get(MAIN, MIND_17);
        statNames[3] = get(MAIN, SPIRIT_18);

        for (int i = 0; i < STAT_COUNT; i++) {
            statControlRects[i * 3].set(0x52, i * 0x20 + 0x36, 0x52 + 0x14, i * 0x20 + 0x36 + 0x14);
            statControlRects[i * 3 + 1].set(0x6B, i * 0x20 + 0x36, 0x6B + 0x14, i * 0x20 + 0x36 + 0x14);
            statControlRects[i * 3 + 2].set(0x84, i * 0x20 + 0x36, 0x84 + 0x14, i * 0x20 + 0x36 + 0x14);

            int labelWidth = estimateLabelWidth(statNames[i]);
            int labelHeight = estimateLabelHeight();
            int labelTop = i * 0x21 + 0x39;
            statDescriptionRects[i].set(0x10, labelTop, 0x10 + labelWidth, labelTop + labelHeight);
        }
        freePoolRect.set(0x2E, 0xB5, 0x2E + 0x4D, 0xB5 + 0x16);
        freePoolPoints = 0;
    }

    /**
     * Native: StatsAllocationPanelVisualObject::LoadCharacterGeneratorStatsResources @00428B45 from CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     * Fully ported.
     */
    void loadCharacterGeneratorStatsResources() {
        releaseCharacterGeneratorStatsResources();
        backgroundGraphic = loadStatsGraphic(STATS_BACKGROUND_BMP);
        plusPressedGraphic = loadStatsGraphic(PLUS_PRESSED_BMP);
        plusHoverGraphic = loadStatsGraphic(PLUS_HOVER_BMP);
        plusIdleOnGraphic = loadStatsGraphic(PLUS_IDLE_ON_BMP);
        plusIdleGraphic = loadStatsGraphic(PLUS_IDLE_BMP);
        plusDisabledGraphic = loadStatsGraphic(PLUS_DISABLED_BMP);
        minusPressedGraphic = loadStatsGraphic(MINUS_PRESSED_BMP);
        minusHoverGraphic = loadStatsGraphic(MINUS_HOVER_BMP);
        minusIdleOnGraphic = loadStatsGraphic(MINUS_IDLE_ON_BMP);
        minusIdleGraphic = loadStatsGraphic(MINUS_IDLE_BMP);
        minusDisabledGraphic = loadStatsGraphic(MINUS_DISABLED_BMP);
        for (int i = 0; i < STAT_COUNT; i++) {
            currentPlusButtonGraphics[i] = plusDisabledGraphic;
            currentMinusButtonGraphics[i] = minusIdleGraphic;
        }
    }

    /**
     * Native: StatsAllocationPanelVisualObject::ReleaseCharacterGeneratorStatsResources @00428F86.
     * Fully ported.
     */
    void releaseCharacterGeneratorStatsResources() {
        backgroundGraphic = releaseStatsGraphic(backgroundGraphic);
        plusPressedGraphic = releaseStatsGraphic(plusPressedGraphic);
        plusHoverGraphic = releaseStatsGraphic(plusHoverGraphic);
        plusIdleOnGraphic = releaseStatsGraphic(plusIdleOnGraphic);
        plusIdleGraphic = releaseStatsGraphic(plusIdleGraphic);
        plusDisabledGraphic = releaseStatsGraphic(plusDisabledGraphic);
        minusPressedGraphic = releaseStatsGraphic(minusPressedGraphic);
        minusHoverGraphic = releaseStatsGraphic(minusHoverGraphic);
        minusIdleOnGraphic = releaseStatsGraphic(minusIdleOnGraphic);
        minusIdleGraphic = releaseStatsGraphic(minusIdleGraphic);
        minusDisabledGraphic = releaseStatsGraphic(minusDisabledGraphic);
        for (int i = 0; i < STAT_COUNT; i++) {
            currentPlusButtonGraphics[i] = null;
            currentMinusButtonGraphics[i] = null;
        }
    }

    /**
     * Native: StatsAllocationPanelVisualObject::LoadCurrentCharacterProfileStats @00429309.
     * Fully ported.
     */
    void loadCurrentCharacterProfileStats() {
        CUnit profile = ownerDialog.currentCharacterProfile;
        bodyValue = Byte.toUnsignedInt(profile.body);
        agilityValue = Byte.toUnsignedInt(profile.reaction);
        mindValue = Byte.toUnsignedInt(profile.mind);
        spiritValue = Byte.toUnsignedInt(profile.spirit);
    }

    /**
     * Native: StatsAllocationPanelVisualObject::SnapshotCurrentStatValues @00429388.
     * Fully ported.
     */
    private void snapshotCurrentStatValues() {
        snapshotBodyValue = bodyValue;
        snapshotAgilityValue = agilityValue;
        snapshotMindValue = mindValue;
        snapshotSpiritValue = spiritValue;
    }

    /**
     * Native helper: StatsAllocationPanelVisualObject::GetHoveredRegion @004296F2.
     * Fully ported.
     */
    private int getHoveredRegion(int x, int y) {
        Point screenOrigin = getScreenOrigin();
        for (int i = 0; i < STAT_COUNT; i++) {
            if (containsTranslatedPoint(statControlRects[i * 3], screenOrigin, x, y)) {
                return (i << 8) | REGION_STAT_VALUE;
            }
            if (containsTranslatedPoint(statControlRects[i * 3 + 1], screenOrigin, x, y)) {
                return (i << 8) | REGION_SPEND_BUTTON;
            }
            if (containsTranslatedPoint(statControlRects[i * 3 + 2], screenOrigin, x, y)) {
                return (i << 8) | REGION_REFUND_BUTTON;
            }
        }
        return REGION_NONE;
    }

    /**
     * Native helper: StatsAllocationPanelVisualObject::RefreshButtonGraphics @00429827.
     * Fully ported.
     */
    int refreshButtonGraphics(int mouseFlags, int x, int y) {
        int packedHit = getHoveredRegion(x, y);
        int hoveredRegion = packedHit == REGION_NONE ? REGION_NONE : packedHit & 0xFF;
        int hoveredStatIndex = packedHit == REGION_NONE ? -1 : packedHit >>> 8;
        for (int i = 0; i < STAT_COUNT; i++) {
            int statValue = getStatValueByIndex(i);
            int spendCost = getSpendCost(statValue);
            currentPlusButtonGraphics[i] = plusIdleGraphic;
            if (freePoolPoints < spendCost || statValue > MAX_STAT_VALUE - 1) {
                currentPlusButtonGraphics[i] = plusDisabledGraphic;
            } else if (i == hoveredStatIndex && hoveredRegion == REGION_SPEND_BUTTON) {
                currentPlusButtonGraphics[i] = (mouseFlags & 1) == 0
                        ? plusHoverGraphic
                        : plusPressedGraphic;
            }

            currentMinusButtonGraphics[i] = minusIdleGraphic;
            if (statValue < MIN_STAT_VALUE) {
                currentMinusButtonGraphics[i] = minusDisabledGraphic;
            } else if (i == hoveredStatIndex && hoveredRegion == REGION_REFUND_BUTTON) {
                currentMinusButtonGraphics[i] = (mouseFlags & 1) == 0
                        ? minusHoverGraphic
                        : minusPressedGraphic;
            }
        }
        return packedHit;
    }

    /**
     * Native helper: StatsAllocationPanelVisualObject::IncreaseStatValue @00429E3E.
     * Fully ported.
     */
    private boolean increaseStatValue(int statIndex) {
        int statValue = getStatValueByIndex(statIndex);
        int spendCost = getSpendCost(statValue);
        if (freePoolPoints < spendCost || statValue > MAX_STAT_VALUE - 1) {
            return false;
        }

        setStatValueByIndex(statIndex, statValue + 1);
        freePoolPoints -= spendCost;
        ownerDialog.onStatsAllocationChanged(
                bodyValue,
                agilityValue,
                mindValue,
                spiritValue,
                ownerDialog.getSelectedSetupIndex() + 1
        );
        ownerDialog.refreshSetupAudio();
        return true;
    }

    /**
     * Native helper: StatsAllocationPanelVisualObject::DecreaseStatValue @00429F41.
     * Fully ported.
     */
    private boolean decreaseStatValue(int statIndex) {
        int statValue = getStatValueByIndex(statIndex);
        if (statValue < MIN_STAT_VALUE) {
            return false;
        }

        freePoolPoints += getRefundGain(statValue);
        setStatValueByIndex(statIndex, statValue - 1);
        ownerDialog.onStatsAllocationChanged(
                bodyValue,
                agilityValue,
                mindValue,
                spiritValue,
                ownerDialog.getSelectedSetupIndex() + 1
        );
        ownerDialog.refreshSetupAudio();
        return true;
    }

    /**
     * Native helper: StatsAllocationPanelVisualObject::GetSpendCost @0042A033.
     * Fully ported.
     */
    private int getSpendCost(int statValue) {
        return getScaledStatCost(statValue + 1) - getScaledStatCost(statValue);
    }

    /**
     * Native helper: StatsAllocationPanelVisualObject::GetRefundGain @0042A063.
     * Fully ported.
     */
    private int getRefundGain(int statValue) {
        return getScaledStatCost(statValue) - getScaledStatCost(statValue - 1);
    }

    /**
     * Native: StatsAllocationPanelVisualObject::GetScaledStatCost @004F250E.
     * Fully ported.
     */
    public static int getScaledStatCost(int statValue) {
        return (int) (Math.pow(STAT_COST_BASE, statValue - 1) * STAT_COST_SCALE + 0.5);
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::GetText @00429B01
     * and StatsAllocationPanelVisualObject::GetHoveredRegion @004296F2.
     */
    private Point getScreenOrigin() {
        Point origin = new Point(cRect.left, cRect.top);
        clientToScreen(origin, origin);
        return origin;
    }

    /**
     * Native helper path shared by stats allocation tooltip and mouse handlers.
     * not ported.
     */
    private static boolean containsTranslatedPoint(CRect localRect, Point screenOrigin, int x, int y) {
        return x >= screenOrigin.x + localRect.left
                && x < screenOrigin.x + localRect.right
                && y >= screenOrigin.y + localRect.top
                && y < screenOrigin.y + localRect.bottom;
    }

    /**
     * Native owner: stats allocation panel stat array access.
     * Native support extracted from StatsAllocationPanelVisualObject::RefreshButtonGraphics @00429827,
     * IncreaseStatValue @00429E3E, and DecreaseStatValue @00429F41.
     */
    private int getStatValueByIndex(int statIndex) {
        return switch (statIndex) {
            case 0 -> bodyValue;
            case 1 -> agilityValue;
            case 2 -> mindValue;
            case 3 -> spiritValue;
            default -> throw new IndexOutOfBoundsException("statIndex: " + statIndex);
        };
    }

    /**
     * Native owner: stats allocation panel stat array writeback.
     * Native support extracted from StatsAllocationPanelVisualObject::IncreaseStatValue @00429E3E
     * and DecreaseStatValue @00429F41.
     */
    private void setStatValueByIndex(int statIndex, int value) {
        switch (statIndex) {
            case 0 -> bodyValue = value;
            case 1 -> agilityValue = value;
            case 2 -> mindValue = value;
            case 3 -> spiritValue = value;
            default -> throw new IndexOutOfBoundsException("statIndex: " + statIndex);
        }
    }

    /**
     * Native owner: signed stat-delta formatting in stats allocation tooltip/update paths.
     * Native support extracted from StatsAllocationPanelVisualObject::GetText @00429B01 and Update @004293CB.
     */
    private static String formatSignedStatDelta(int value) {
        return String.format(Locale.US, "%+,d", value);
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::Update @004293CB.
     */
    private static void drawBackgroundGraphic(CBmp64k backgroundGraphic, int x, int y) {
        backgroundGraphic.draw(x, y, 0, null, false);
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::Update @004293CB.
     */
    private static void drawGFont4TextShadowed(int x, int y, String text) {
        Globals.fonts.font4.drawTextShadowed(x, y, text, 0, defaultFontPalette(), 1);
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::Update @004293CB.
     */
    private static void drawGFont4CenteredTextShadowed(int x, int y, String text) {
        Globals.fonts.font4.drawTextShadowed(
                x,
                y,
                text,
                TextAlign.combine(TextAlign.CENTER, TextAlign.VERTICAL_CENTER),
                defaultFontPalette(),
                1
        );
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::Update @004293CB.
     */
    private static void drawSetupButtonGraphic(CBmp64k graphic, int x, int y) {
        graphic.drawRectMasked(x, y);
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::InitializeStatsAllocationPanel @0042864F.
     */
    private static int estimateLabelWidth(String text) {
        return Globals.fonts.font4.getTextWidth(text);
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::InitializeStatsAllocationPanel @0042864F.
     */
    private static int estimateLabelHeight() {
        return Globals.fonts.font4.getFrameHeight();
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::LoadCharacterGeneratorStatsResources @00428B45.
     */
    private static CBmp64k loadStatsGraphic(String resourcePath) {
        CBmp64k graphic = new CBmp64k(resourcePath);
        Globals.mousePointer.update();
        return graphic;
    }

    /**
     * Native support extracted from CVisualObject::FreeOwned(panel457)
     * in CharacterGeneratorDialogVisualObject::HideDialog @0042DE80.
     */
    private static CBmp64k releaseStatsGraphic(CBmp64k graphic) {
        return null;
    }

    /**
     * Native support extracted from g_pPalette1[0] reads in StatsAllocationPanelVisualObject::Update @004293CB.
     */
    private static Palette16 defaultFontPalette() {
        return Palettes.p1.paletteData[0];
    }

}
