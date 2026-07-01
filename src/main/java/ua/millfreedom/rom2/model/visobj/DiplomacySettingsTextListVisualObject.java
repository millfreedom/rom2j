package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.Diplomacy;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

/**
 * Native class: DiplomacySettingsTextListVisualObject.
 * Purpose: diplomacy settings text-list specialization with per-column child control groups.
 */
public class DiplomacySettingsTextListVisualObject extends TextListVisualObject {
    private static final int COLUMN_TOP_OFFSET = 0;
    private static final int COLUMN_HEIGHT = 0x18;
    private static final int COLUMN_1_LEFT = 200;
    private static final int COLUMN_1_RIGHT = 0xFA;
    private static final int COLUMN_2_LEFT = 0x10E;
    private static final int COLUMN_2_RIGHT = 0x140;
    private static final int COLUMN_3_LEFT = 0x154;
    private static final int COLUMN_3_RIGHT = 0x186;
    private static final int COLUMN_4_LEFT = 0x19A;
    private static final int COLUMN_4_RIGHT = 0x1CC;
    private static final String CHOICE_LABEL = " ";

    public static final int NATIVE_SIZE = 0xA8; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x94
    public List<Diplomacy> diplomacyArray;
    //0x98
    public final List<StringListVariantAVisualObject> enemyControls = new ArrayList<>();
    //0x9C
    public final List<StringListVariantAVisualObject> allianceControls = new ArrayList<>();
    //0xA0
    public final List<StringListVariantAVisualObject> visibleControls = new ArrayList<>();
    //0xA4
    public final List<StringListVariantAVisualObject> silentControls = new ArrayList<>();

    /**
     * Native: DiplomacySettingsTextListVisualObject::DiplomacySettingsTextListVisualObject @004D90E0.
     * Fully ported.
     */
    public DiplomacySettingsTextListVisualObject(
            int id,
            CRect rect,
            List<Diplomacy> diplomacyArray,
            Object bitmapFont,
            Palette16 field0x7c,
            Palette16 field0x80,
            int rowHeightMinimum
    ) {
        super(id, rect, bitmapFont, field0x7c, field0x80, -1, null);
        this.diplomacyArray = diplomacyArray;
        int rowHeightBase = rowHeightMinimum > 0 ? rowHeightMinimum : rowHeight;
        this.rowPitch = Math.max(rowHeightBase, 0x18) + 8;
        this.visibleRowCount = cRect.height() / rowPitch;
        initializeChoiceControls();
    }

    /**
     * vtbl +0x48: DiplomacySettingsTextListVisualObject::OnMessage @004D9ED8.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int rowCount = diplomacyArray.size();
        if (msg == MessageCodes.TEXT_LIST_SELECTION_CHANGED) {
            if (w >= rowCount * 2) {
                return 0;
            }
            StringListVariantAVisualObject sourceControl = requireChoiceControlById(w);
            StringListVariantAVisualObject pairedControl =
                    requireChoiceControlById(Integer.remainderUnsigned(w + rowCount, rowCount << 1));
            if (sourceControl.getSelectionValue() != 0 && pairedControl.getSelectionValue() != 0) {
                pairedControl.setSelectionValue(0);
                pairedControl.draw();
            }
            return 1;
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x54: DiplomacySettingsTextListVisualObject::OnLButtonDown @004D9B2F.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        int rowOffset = getRowOffsetAtScreenY(y);
        int rowCount = diplomacyArray.size();
        if (rowOffset < visibleRowCount) {
            int nextSelectedRow = firstVisibleRow + rowOffset;
            selectedRow = nextSelectedRow < rowCount - 1 ? nextSelectedRow : rowCount - 1;
        }

        draw();
        if (linkedChildId > 0) {
            syncParentScrollbarSelection(selectedRow, rowCount);
        }
        m_pParent.onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, selectedRow);
        return 1;
    }

    /**
     * vtbl +0x6C: DiplomacySettingsTextListVisualObject::OnKeyDown @004D9FAA.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        return 0;
    }

    /**
     * vtbl +0x78: DiplomacySettingsTextListVisualObject::IsRowIndexValid @004D977D.
     * Fully ported.
     */
    @Override
    public boolean isRowIndexValid(int rowIndex) {
        return rowIndex < enemyControls.size();
    }

    /**
     * vtbl +0x7C: DiplomacySettingsTextListVisualObject::DrawRowText @004D97A2.
     * Fully ported.
     */
    @Override
    public void drawRowText(int rowIndex, int x, int y, Palette16 textPalette) {
        if (!isRowIndexValid(rowIndex)) {
            return;
        }

        bitmapFont.drawTextShadowed(
                x,
                y + (rowPitch - rowHeight) / 2,
                diplomacyArray.get(rowIndex).name,
                0,
                textPalette,
                1
        );

        enemyControls.get(rowIndex).update();
        allianceControls.get(rowIndex).update();
        visibleControls.get(rowIndex).update();
        silentControls.get(rowIndex).update();
    }

    /**
     * vtbl +0x80: DiplomacySettingsTextListVisualObject::SetSelectedRow @004D967D.
     * Fully ported.
     */
    @Override
    public void setSelectedRow(int rowIndex) {
        int maxFirstVisibleRow = diplomacyArray.size() - visibleRowCount;
        int nextFirstVisibleRow = rowIndex < 0 ? 0 : rowIndex;
        if (maxFirstVisibleRow <= nextFirstVisibleRow) {
            nextFirstVisibleRow = maxFirstVisibleRow;
        }

        firstVisibleRow = nextFirstVisibleRow;
        selectedRow = nextFirstVisibleRow;
        layoutChoiceControls(firstVisibleRow);
        draw();
        syncParentScrollbarSelection(firstVisibleRow, maxFirstVisibleRow + 1);
        m_pParent.onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, firstVisibleRow);
    }

    /**
     * vtbl +0x88: DiplomacySettingsTextListVisualObject::DrawSelectionFrame @004E0E40.
     * Fully ported.
     */
    @Override
    public void drawSelectionFrame(int x, int y, CRect screenRect) {
    }

    /**
     * Native helper: commitSelectionControlsToDiplomacyArray @004D9897.
     * Fully ported.
     */
    public void commitSelectionControlsToDiplomacyArray() {
        int rowCount = enemyControls.size();
        while (diplomacyArray.size() > rowCount) {
            diplomacyArray.remove(diplomacyArray.size() - 1);
        }
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Diplomacy diplomacyEntry = diplomacyArray.get(rowIndex);
            diplomacyEntry.enemy = readChoiceSelection(enemyControls.get(rowIndex));
            diplomacyEntry.alliance = readChoiceSelection(allianceControls.get(rowIndex));
            diplomacyEntry.visible = readChoiceSelection(visibleControls.get(rowIndex));
            diplomacyEntry.silent = readChoiceSelection(silentControls.get(rowIndex));
        }
    }

    /**
     * Native helper: restoreFullWidthAfterScrollbar @004D9B09.
     * Fully ported.
     */
    public void restoreFullWidthAfterScrollbar() {
        if (linkedChildId >= 0) {
            cRect.right += 0x1A;
        }
    }

    /**
     * Native helper: configureVisibleRowsAndScrollbar @004D99AC.
     * Fully ported.
     */
    public void configureVisibleRowsAndScrollbar() {
        if (m_pParent == null) {
            return;
        }

        visibleRowCount = cRect.height() / rowPitch;
        int diplomacyCount = diplomacyArray.size();
        int totalHeight = rowPitch * diplomacyCount;
        if (totalHeight > cRect.height()) {
            if (linkedChildId < 0) {
                cRect.right -= 0x1A;
                linkedChildId = m_pParent.getNextChildID();
                m_pParent.addChild(new PostSetupVisualObject(
                        linkedChildId,
                        cRect.right,
                        cRect.top,
                        cRect.right + 0x18,
                        cRect.bottom,
                        null
                ));
            } else {
                PostSetupVisualObject scrollbar = (PostSetupVisualObject) getChildById(linkedChildId);
                scrollbar.syncSelectionState(selectedRow, diplomacyCount);
            }
        }
    }

    /**
     * Native support block inside DiplomacySettingsTextListVisualObject::DiplomacySettingsTextListVisualObject @004D90E0.
     * Fully ported.
     */
    private void initializeChoiceControls() {
        enemyControls.clear();
        allianceControls.clear();
        visibleControls.clear();
        silentControls.clear();
        rows.clear();

        int diplomacyCount = diplomacyArray.size();
        for (int rowIndex = 0; rowIndex < diplomacyCount; rowIndex++) {
            Diplomacy diplomacyEntry = diplomacyArray.get(rowIndex);
            rows.add(diplomacyEntry.name);
            enemyControls.add(createChoiceControl(
                    rowIndex,
                    diplomacyEntry.enemy
            ));
            allianceControls.add(createChoiceControl(
                    rowIndex + diplomacyCount,
                    diplomacyEntry.alliance
            ));
            visibleControls.add(createChoiceControl(
                    rowIndex + diplomacyCount * 2,
                    diplomacyEntry.visible
            ));
            silentControls.add(createChoiceControl(
                    rowIndex + diplomacyCount * 3,
                    diplomacyEntry.silent
            ));
        }
        layoutChoiceControls(0);
    }

    /**
     * Native helper: layoutChoiceControls @004D9C31.
     * Fully ported.
     */
    private void layoutChoiceControls(int firstVisibleRow) {
        int diplomacyCount = diplomacyArray.size();
        for (int rowIndex = 0; rowIndex < diplomacyCount; rowIndex++) {
            boolean hidden = rowIndex < this.firstVisibleRow || this.firstVisibleRow + visibleRowCount <= rowIndex;
            int rowTop = (rowIndex - firstVisibleRow) * rowPitch + COLUMN_TOP_OFFSET;
            int rowBottom = rowTop + COLUMN_HEIGHT;

            setChoiceControlBounds(enemyControls.get(rowIndex), COLUMN_1_LEFT, COLUMN_1_RIGHT, rowTop, rowBottom, hidden);
            setChoiceControlBounds(allianceControls.get(rowIndex), COLUMN_2_LEFT, COLUMN_2_RIGHT, rowTop, rowBottom, hidden);
            setChoiceControlBounds(visibleControls.get(rowIndex), COLUMN_3_LEFT, COLUMN_3_RIGHT, rowTop, rowBottom, hidden);
            setChoiceControlBounds(silentControls.get(rowIndex), COLUMN_4_LEFT, COLUMN_4_RIGHT, rowTop, rowBottom, hidden);
        }
    }

    /**
     * Native support block inside DiplomacySettingsTextListVisualObject::DiplomacySettingsTextListVisualObject @004D90E0.
     * Fully ported.
     */
    private StringListVariantAVisualObject createChoiceControl(int id, boolean selected) {
        StringListVariantAVisualObject control = new StringListVariantAVisualObject(
                id,
                0,
                0,
                0,
                0,
                bitmapFont,
                field0x7c,
                null
        );
        appendChoiceLabel(control);
        control.setSelectionValue(selected ? 1 : 0);
        addChild(control);
        return control;
    }

    /**
     * Native helper: StringListVisualObject::AddRow @0044EE50.
     * Fully ported for the diplomacy choice-label call sites.
     */
    private static void appendChoiceLabel(StringListVariantAVisualObject control) {
        control.addRow(CHOICE_LABEL);
    }

    /**
     * Native support block inside DiplomacySettingsTextListVisualObject::layoutChoiceControls @004D9C31.
     * Fully ported.
     */
    private static void setChoiceControlBounds(
            StringListVariantAVisualObject control,
            int left,
            int right,
            int top,
            int bottom,
            boolean hidden
    ) {
        control.setStateFlag(0x20, hidden ? 1 : 0);
        control.setBounds(left, top, right, bottom);
    }

    /**
     * Native support reused by DiplomacySettingsTextListVisualObject::commitSelectionControlsToDiplomacyArray @004D9897.
     * Fully ported.
     */
    private static boolean readChoiceSelection(StringListVariantAVisualObject control) {
        return control.getSelectionValue() != 0;
    }

    /**
     * Native support reused by DiplomacySettingsTextListVisualObject::OnMessage @004D9ED8.
     * Fully ported.
     */
    private StringListVariantAVisualObject requireChoiceControlById(int controlId) {
        CVisualObject child = getChildById(controlId);
        return (StringListVariantAVisualObject) child;
    }

    /**
     * Native support reused by DiplomacySettingsTextListVisualObject::OnLButtonDown @004D9B2F
     * and DiplomacySettingsTextListVisualObject::SetSelectedRow @004D967D.
     * Fully ported.
     */
    private void syncParentScrollbarSelection(int currentValue, int maxValue) {
        CVisualObject scrollbarChild = m_pParent.getChildById(linkedChildId);
        if (scrollbarChild != null) {
            ((PostSetupVisualObject) scrollbarChild).syncSelectionState(currentValue, maxValue);
        }
    }
}
