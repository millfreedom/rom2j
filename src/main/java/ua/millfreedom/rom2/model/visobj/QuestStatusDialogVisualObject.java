package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.QuestsStorage;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.res.Resources;
import ua.millfreedom.rom2.text.BuildingText;
import ua.millfreedom.rom2.text.NpcNamesText;
import ua.millfreedom.rom2.text.UnitNameText;

import java.util.Locale;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.res.Constants.INTERFACE;
import static ua.millfreedom.rom2.res.Constants.SUBOBJ_256;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.MainText.NO_QUESTS_343;
import static ua.millfreedom.rom2.text.MainText.QUESTS_342;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_N_W_317;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_THIRD_HERO_DIES_IN_THE_WORST_POSSIBLE_WAY_284;
import static ua.millfreedom.rom2.text.TextTableId.BUILDING;
import static ua.millfreedom.rom2.text.TextTableId.NPCNAMES;
import static ua.millfreedom.rom2.text.TextTableId.UNITNAME;

/**
 * Native class: QuestStatusDialogVisualObject.
 * Purpose: quest-status dialog with a quest-state icon sprite sheet.
 */
public class QuestStatusDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x6C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int QUEST_ROW_HEIGHT = 0x20;
    private static final int QUEST_TEXT_LINE_SPACING = 10;
    private static final int QUEST_HOVER_SHADE = 4;
    private static final int MONSTER_TYPE_FIRST = 0x52;
    private static final int MONSTER_TYPE_LAST = 0x66;
    private static final int UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK = 0x11;
    private static final int QUEST_ID_TOKEN_TARGET = 1;
    private static final int QUEST_ID_PACKED_UNIT_TARGET = 2;
    private static final int QUEST_ID_FLAGGED_UNIT_TARGET = 3;
    private static final int QUEST_ID_NPC_TARGET = 11;
    private static final int QUEST_ID_FLAGGED_UNIT_TARGET_NO_ARGS = 12;
    private static final int QUEST_STATE_PENDING = 0;
    private static final int QUEST_STATE_ACCEPTED = 1;
    private static final int QUEST_STATE_COMPLETED = 2;

    //0x68
    public CSprite256 questStateIconSprite;

    /**
     * Native: QuestStatusDialogVisualObject::QuestStatusDialogVisualObject @004DF3D6.
     * Fully ported.
     */
    public QuestStatusDialogVisualObject() {
        super();
        this.questStateIconSprite = createSubObjectSprite();
    }

    /**
     * Native: QuestStatusDialogVisualObject::QuestStatusDialogVisualObject @004DF475.
     * Fully ported.
     */
    public QuestStatusDialogVisualObject(int id, CRect rect) {
        super(id, rect.left, rect.top, rect.right, rect.bottom, null);
        this.questStateIconSprite = createSubObjectSprite();
    }

    /**
     * Native: QuestStatusDialogVisualObject::QuestStatusDialogVisualObject @004DF520.
     * Fully ported.
     */
    public QuestStatusDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.questStateIconSprite = createSubObjectSprite();
    }

    /**
     * vtbl +0x2C: QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    @Override
    public void update() {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        QuestsStorage questStorage = mapVisualObject.questStorage;
        int questCount = questStorage.questsByKey.size();
        cRect.bottom = cRect.top + 0x48 + questCount * 0x20;

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        screenRect.right -= 8;
        screenRect.bottom -= 8;
        int contentTop = cRect.top + 0x28;

        Globals.renderer.lockSurface();
        try {
            drawQuestFrame(screenRect);
            drawQuestHeader(questCount, contentTop);
            drawQuestRows(mapVisualObject, questStorage, cRect.left + 0x20, contentTop);
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x48: QuestStatusDialogVisualObject::OnMessage @004E0EA0.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        return 0;
    }

    /**
     * Native constructor allocation + palette-init path @004DF3D6, @004DF475, and @004DF520.
     * Fully ported.
     */
    private static CSprite256 createSubObjectSprite() {
        try {
            CSprite256 sprite = new CSprite256(Resources.path(GRAPHICS, INTERFACE, SUBOBJ_256));
            sprite.initPalette(1, 1, 0);
            return sprite;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create quest state icon sprite", e);
        }
    }

    /**
     * Native quest-frame draw shell inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private static void drawQuestFrame(CRect screenRect) {
        GUI.uiFrameSprite.drawAlpha(screenRect.right - 0x18, screenRect.top + 8, 0x0C, 6, false);
        GUI.uiFrameSprite.drawAlpha(screenRect.left + 8, screenRect.bottom - 0x18, 0x0F, 6, false);
        GUI.uiFrameSprite.drawAlpha(screenRect.right - 0x18, screenRect.bottom - 0x18, 0x11, 6, false);

        for (int i = 0; i < (screenRect.width() - 0x40) / 0x30; i++) {
            GUI.uiFrameSprite.drawAlpha(screenRect.left + 0x28 + i * 0x30, screenRect.bottom - 0x18, 0x10, 6, false);
        }
        for (int i = 0; i < ((screenRect.height() - 0x40) >> 5); i++) {
            GUI.uiFrameSprite.drawAlpha(screenRect.right - 0x18, screenRect.top + 0x28 + i * 0x20, 0x0E, 6, false);
        }

        GUI.uiFrameSprite.draw(screenRect.left, screenRect.top, 0x0A, 0, false);
        GUI.uiFrameSprite.draw(screenRect.right - 0x20, screenRect.top, 0x0C, 0, false);
        GUI.uiFrameSprite.draw(screenRect.left, screenRect.bottom - 0x20, 0x0F, 0, false);
        GUI.uiFrameSprite.draw(screenRect.right - 0x20, screenRect.bottom - 0x20, 0x11, 0, false);

        for (int i = 0; i < (screenRect.width() - 0x40) / 0x30; i++) {
            int x = screenRect.left + 0x20 + i * 0x30;
            GUI.uiFrameSprite.draw(x, screenRect.top, 0x0B, 0, false);
            GUI.uiFrameSprite.draw(x, screenRect.bottom - 0x20, 0x10, 0, false);
        }
        for (int i = 0; i < ((screenRect.height() - 0x40) >> 5); i++) {
            int y = screenRect.top + 0x20 + i * 0x20;
            GUI.uiFrameSprite.draw(screenRect.left, y, 0x0D, 0, false);
            GUI.uiFrameSprite.draw(screenRect.right - 0x20, y, 0x0E, 0, false);
        }
        for (int x = 0; x < (screenRect.width() - 0x40) / 0x30; x++) {
            for (int y = 0; y < ((screenRect.height() - 0x40) >> 5); y++) {
                GUI.uiFrameSprite.draw(screenRect.left + 0x20 + x * 0x30, screenRect.top + 0x20 + y * 0x20, 9, 0, false);
            }
        }
    }

    /**
     * Native title branch inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private void drawQuestHeader(int questCount, int contentTop) {
        int centerX = cRect.left + cRect.width() / 2;
        int headerY = contentTop - 0x0E;
        String headerText = questCount == 0 ? get(NO_QUESTS_343) : get(QUESTS_342);
        drawTextShadowed(Globals.fonts.font2, centerX, headerY, headerText, TextAlign.CENTER.mask, Palettes.grayDim, 1);
    }

    /**
     * Native quest-row loop inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private void drawQuestRows(MapVisualObject mapVisualObject, QuestsStorage questStorage, int rowLeft, int rowTop) {
        int currentRowTop = rowTop;
        for (Quest quest : questStorage.questsByKey.values()) {
            if (mapVisualObject.matchesHoveredQuestKey(quest.questKey)) {
                Globals.renderer.applyShadeAdditiveToRect(rowLeft + 0x16, currentRowTop - 2,
                        rowLeft - 0x46 + cRect.width(), currentRowTop + 0x18, QUEST_HOVER_SHADE);
            }

            String questText = formatQuestStatusText(mapVisualObject, quest);
            CRect textRect = new CRect(rowLeft + 0x18, currentRowTop,
                    rowLeft + 0x18 + cRect.width() - 0x60, currentRowTop + QUEST_ROW_HEIGHT);
            Palette16 palette = resolveQuestStatePalette(quest.state);
            Globals.fonts.font2.drawWrappedJustifiedTextShadowed(textRect, questText, palette, QUEST_TEXT_LINE_SPACING);

            questStateIconSprite.draw(rowLeft - 6, currentRowTop - 2, resolveQuestStateIconFrame(quest.state), 0,
                    false);
            currentRowTop += QUEST_ROW_HEIGHT;
        }
    }

    /**
     * Native CString::Format switch inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private static String formatQuestStatusText(MapVisualObject mapVisualObject, Quest quest) {
        int questId = quest.getId();
        if (questId <= 0) {
            return "";
        }

        String format = get(MAIN_THIRD_HERO_DIES_IN_THE_WORST_POSSIBLE_WAY_284 + questId);
        CGameObject primaryTarget = resolveQuestPrimaryTarget(mapVisualObject, quest, questId);
        String targetName = resolveQuestTargetName(quest, questId, primaryTarget);
        String regionName = resolveQuestRegionName(mapVisualObject, quest);
        String landmarkName = resolveQuestLandmarkName(mapVisualObject, quest);
        return switch (questId) {
            case QUEST_ID_TOKEN_TARGET -> String.format(Locale.ROOT, format, targetName, regionName, landmarkName);
            case QUEST_ID_PACKED_UNIT_TARGET -> String.format(Locale.ROOT, format, quest.secondaryArgument, targetName);
            case QUEST_ID_FLAGGED_UNIT_TARGET -> String.format(Locale.ROOT, format, regionName, landmarkName);
            case 4, 5, 13 -> String.format(Locale.ROOT, format, regionName);
            case 6 -> String.format(Locale.ROOT, format, Integer.divideUnsigned(quest.secondaryArgument, 0x3C0),
                    (quest.secondaryArgument >>> 4) % 0x3C, regionName);
            case 8, 9, 10 -> String.format(Locale.ROOT, format, quest.secondaryArgument);
            case QUEST_ID_NPC_TARGET -> String.format(Locale.ROOT, format, targetName);
            case QUEST_ID_FLAGGED_UNIT_TARGET_NO_ARGS -> String.format(Locale.ROOT, format);
            default -> "";
        };
    }

    /**
     * Native primary object lookup branch inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private static CGameObject resolveQuestPrimaryTarget(MapVisualObject mapVisualObject, Quest quest, int questId) {
        return switch (questId) {
            case QUEST_ID_TOKEN_TARGET, 4, QUEST_ID_NPC_TARGET ->
                    mapVisualObject.getObjectByToken((short) quest.primaryArgument);
            default -> null;
        };
    }

    /**
     * Native target-name branches inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported for modeled CGameObject/CUnit targets.
     */
    private static String resolveQuestTargetName(Quest quest, int questId, CGameObject targetObject) {
        if (questId == QUEST_ID_PACKED_UNIT_TARGET) {
            return resolvePackedQuestTargetName(quest.primaryArgument);
        }
        return targetObject == null ? "" : resolveQuestTargetName(targetObject);
    }

    /**
     * Native packed unit-name branch for quest id `2` inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private static String resolvePackedQuestTargetName(int packedUnitAndPortrait) {
        int unitType = packedUnitAndPortrait & 0xFF;
        int portraitVariant = packedUnitAndPortrait >>> 8;
        String unitName = get(UNITNAME, UnitNameText.byIndex(unitType));
        if (isMonsterType(unitType)) {
            return unitName;
        }
        return String.format(Locale.ROOT, "%s (%d)", unitName, portraitVariant);
    }

    /**
     * Native unit/NPC target-name branches inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported for modeled CGameObject/CUnit targets.
     */
    private static String resolveQuestTargetName(CGameObject targetObject) {
        if (targetObject instanceof CUnit targetUnit
                && (targetUnit.unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) != 0) {
            int npcNameIndex = Short.toUnsignedInt(targetUnit.serverID) - 1;
            return get(NPCNAMES, NpcNamesText.byIndex(npcNameIndex));
        }

        String unitName = get(UNITNAME, UnitNameText.byIndex(targetObject.type));
        if (isMonsterType(targetObject.type)) {
            return unitName;
        }
        return String.format(Locale.ROOT, "%s (%d)", unitName, targetObject.field8_0x28);
    }

    /**
     * Native monster type-name branch inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private static boolean isMonsterType(int unitType) {
        return unitType >= MONSTER_TYPE_FIRST && unitType <= MONSTER_TYPE_LAST;
    }

    /**
     * Native secondary object region lookup inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported for modeled secondary-object lookup.
     */
    private static String resolveQuestRegionName(MapVisualObject mapVisualObject, Quest quest) {
        CGameObject landmarkObject = resolveQuestSecondaryObject(mapVisualObject, quest);
        if (landmarkObject == null) {
            return "";
        }

        int regionX = ((landmarkObject.tileX - 8) * 5) / (mapVisualObject.cachedMapWidth - 0x10);
        int regionY = ((landmarkObject.tileY - 8) * 5) / (mapVisualObject.cachedMapHeight - 0x10);
        return get(MAIN_N_W_317 + regionX + regionY * 5);
    }

    /**
     * Native secondary object building-name lookup inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported for modeled secondary-object lookup.
     */
    private static String resolveQuestLandmarkName(MapVisualObject mapVisualObject, Quest quest) {
        CGameObject landmarkObject = resolveQuestSecondaryObject(mapVisualObject, quest);
        if (landmarkObject == null) {
            return "";
        }
        return get(BUILDING, BuildingText.byIndex(landmarkObject.type - 1));
    }

    /**
     * Native MapVisualObject::m_ObjectMap lookup by Quest::getSecondaryIndexKey inside
     * QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported at the MapVisualObject object-map boundary.
     */
    private static CGameObject resolveQuestSecondaryObject(MapVisualObject mapVisualObject, Quest quest) {
        return mapVisualObject.getObjectByToken((short) quest.secondaryIndexKey);
    }

    /**
     * Native quest-state palette branch inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private static Palette16 resolveQuestStatePalette(int state) {
        return switch (state) {
            case QUEST_STATE_ACCEPTED -> Palettes.gray;
            case QUEST_STATE_COMPLETED -> Palettes.redish;
            case QUEST_STATE_PENDING -> Palettes.grayDim;
            default -> Palettes.grayDim;
        };
    }

    /**
     * Native quest-state icon branch inside QuestStatusDialogVisualObject::Update @004DF659.
     * Fully ported.
     */
    private static int resolveQuestStateIconFrame(int state) {
        return switch (state) {
            case QUEST_STATE_PENDING -> 0;
            case QUEST_STATE_ACCEPTED -> 1;
            case QUEST_STATE_COMPLETED -> 2;
            default -> 10;
        };
    }

    /**
     * Native support boundary for `CBitmapFont::DrawTextShadowed` calls in QuestStatusDialogVisualObject::Update
     * @004DF659.
     * Fully ported.
     */
    private static void drawTextShadowed(
            CBitmapFont bitmapFont,
            int x,
            int y,
            String text,
            int textAlignFlags,
            Palette16 palette,
            int shadowOffset
    ) {
        bitmapFont.drawTextShadowed(x, y, text, textAlignFlags, palette, shadowOffset);
    }
}
