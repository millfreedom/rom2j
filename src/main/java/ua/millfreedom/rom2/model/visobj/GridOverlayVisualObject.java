package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.res.Resources;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Native class: GridOverlayVisualObject.
 * Purpose: generic 0x50-tile grid panel used by selection and targeting overlays.
 */
public class GridOverlayVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x20AC; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int DYNAMIC_PAYLOAD_FLAG = 0x20;
    private static final int MAGIC_TRAIL_RANDOM_OFFSET_MASK = 0x3FF;
    private static final int MAGIC_TRAIL_RED = 0xFF;
    private static final int MAGIC_TRAIL_GREEN = 0;
    private static final int MAGIC_TRAIL_BLUE = 0xFF;
    private static final String GRAPHICS_DIRECTORY = "graphics";
    private static final String INVENTORY_DIRECTORY = "inventory";
    private static final String A16_SUFFIX = ".16a";

    //0x5c
    public final List<Object> visibleCellSprites = new ArrayList<>();
    //0x70
    public final List<Integer> cellUpdateCounters = new ArrayList<>();
    //0x84
    public Object gridSource;
    //0x88
    public int visibleColumns;
    //0x8c
    public int visibleRows;
    //0x90
    public Object visibleStartRef;
    //0x94
    public final List<Integer> cellRandomOffsets1 = new ArrayList<>();
    //0x1094
    public final List<Integer> cellRandomOffsets2 = new ArrayList<>();
    //0x2094
    public final List<Integer> cellAnimationFrames = new ArrayList<>();
    //0x20a8
    public int hasAnimatedVisibleCells;

    /**
     * Native: GridOverlayVisualObject::GridOverlayVisualObject @004A1130.
     * Fully ported; native archiveObject parameter is unused.
     */
    public GridOverlayVisualObject() {
        super();
        this.visibleColumns = 0;
        this.visibleRows = 0;
        this.visibleStartRef = null;
        this.gridSource = null;
        this.hasAnimatedVisibleCells = 0;
        initializeGridArrays();
    }

    /**
     * Native: GridOverlayVisualObject::GridOverlayVisualObject @004A12B8.
     * Fully ported.
     */
    public GridOverlayVisualObject(int id, CRect rect) {
        super(id, rect, null);
        this.visibleColumns = 0;
        this.visibleRows = 0;
        this.visibleStartRef = null;
        this.gridSource = null;
        this.hasAnimatedVisibleCells = 0;
        initializeGridArrays();
    }

    /**
     * Native: GridOverlayVisualObject::GridOverlayVisualObject @004A11E8.
     * Fully ported.
     */
    public GridOverlayVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.visibleColumns = 0;
        this.visibleRows = 0;
        this.visibleStartRef = null;
        this.gridSource = null;
        this.hasAnimatedVisibleCells = 0;
        initializeGridArrays();
    }

    /**
     * Native: GridOverlayVisualObject::InitializeGridArrays @004A147B.
     * Fully ported.
     */
    public void initializeGridArrays() {
        cellRandomOffsets1.clear();
        cellRandomOffsets2.clear();
        for (int i = 0; i < 0x400; i++) {
            cellRandomOffsets1.add(Utils.randExclusive(0, 0x8000) / 0x1ff + 8);
            cellRandomOffsets2.add(Utils.randExclusive(0, 0x8000) / 0x1ff + 8);
        }
    }

    /**
     * vtbl +0x14: GridOverlayVisualObject::GetText @004A15A8.
     * Fully ported.
     */
    @Override
    public String getText() {
        return null;
    }

    /**
     * vtbl +0x48: GridOverlayVisualObject::OnMessage @004A268F.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == MessageCodes.INITIALIZE_UI) {
            advanceAnimatedCells();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x78: GridOverlayVisualObject::MergeOrInsertEntryAt @004A1DFE.
     * Fully ported.
     */
    public int mergeOrInsertEntryAt(Object entry, int insertIndex) {
        TokenEntry carriedEntry = (TokenEntry) entry;
        List<Object> source = requireMutableGridSourceList();

        for (int i = 0; i < source.size(); i++) {
            TokenEntry existingEntry = (TokenEntry) source.get(i);
            if (!carriedEntry.matchesStackIdentity(existingEntry)) {
                continue;
            }

            existingEntry.addQuantity(carriedEntry.quantity);
            return i;
        }

        if (insertIndex < 0 || insertIndex >= source.size()) {
            if (!source.isEmpty()) {
                int lastIndex = source.size() - 1;
                TokenEntry lastEntry = (TokenEntry) source.get(lastIndex);
                if (lastEntry.isMoneyEntry()) {
                    source.add(lastIndex, entry);
                    return lastIndex;
                }
            }
            source.add(entry);
            return source.size() - 1;
        }

        source.add(insertIndex, entry);
        return insertIndex;
    }

    /**
     * vtbl +0x7C: GridOverlayVisualObject::MergeOrAppendEntry @004A1F72.
     * Fully ported.
     */
    public int mergeOrAppendEntry(Object entry) {
        if (gridSource == null) {
            return -1;
        }
        TokenEntry carriedEntry = (TokenEntry) entry;
        List<Object> source = requireMutableGridSourceList();

        for (int i = 0; i < source.size(); i++) {
            TokenEntry existingEntry = (TokenEntry) source.get(i);
            if (!existingEntry.matchesStackIdentity(carriedEntry)) {
                continue;
            }

            existingEntry.addQuantity(carriedEntry.quantity);
            return i;
        }

        source.add(entry);
        return source.size() - 1;
    }

    /**
     * vtbl +0x80: GridOverlayVisualObject::DetachTokenEntryByEntryId @004A2050.
     * Fully ported.
     */
    public TokenEntry detachTokenEntryByEntryId(int entryId) {
        List<Object> source = requireMutableGridSourceList();

        for (int i = 0; i < source.size(); i++) {
            TokenEntry tokenEntry = (TokenEntry) source.get(i);
            if (tokenEntry.entryId != (entryId & 0xFFFF)) {
                continue;
            }

            if (!tokenEntry.tryRemoveQuantityLeavingRemainder(1)) {
                source.remove(i);
                tokenEntry.quantity = 1;
                return tokenEntry;
            }

            TokenEntry clone = new TokenEntry(tokenEntry);
            clone.quantity = 1;
            return clone;
        }

        return null;
    }

    /**
     * vtbl +0x84: GridOverlayVisualObject::DetachMatchingTokenEntry @004A218B.
     * Fully ported.
     */
    public TokenEntry detachMatchingTokenEntry(Object entry, int quantity) {
        TokenEntry requested = (TokenEntry) entry;
        List<Object> source = requireMutableGridSourceList();

        for (int i = 0; i < source.size(); i++) {
            TokenEntry tokenEntry = (TokenEntry) source.get(i);
            if (!requested.matchesStackIdentity(tokenEntry)) {
                continue;
            }

            if (!tokenEntry.tryRemoveQuantityLeavingRemainder(quantity)) {
                if (tokenEntry.isMoneyEntry()) {
                    TokenEntry clone = new TokenEntry(tokenEntry);
                    tokenEntry.quantity = 0;
                    return clone;
                }

                source.remove(i);
                tokenEntry.quantity = quantity;
                return tokenEntry;
            }

            TokenEntry clone = new TokenEntry(tokenEntry);
            clone.quantity = quantity;
            return clone;
        }

        return null;
    }

    /**
     * vtbl +0x88: GridOverlayVisualObject::GetGridIndexAtScreenPoint @004A4250.
     * Fully ported.
     */
    public int getGridIndexAtScreenPoint(int x, int y) {
        return -1;
    }

    /**
     * vtbl +0x8C: GridOverlayVisualObject::GetGridIndexAtPoint @004A4240.
     * Fully ported.
     */
    public int getGridIndexAtPoint(Point point) {
        return -1;
    }

    /**
     * vtbl +0x90: GridOverlayVisualObject::SetGridSource @004A1659.
     * Fully ported.
     */
    public void setGridSource(Object gridSource) {
        this.gridSource = gridSource;
        resetCellUpdateCounters();
        clampVisibleStart();
    }

    /**
     * vtbl +0x94: GridOverlayVisualObject::BindGridSourceFromContext @004A15B5.
     * Fully ported. Native binds context +0xD0 and context +0xE4.
     */
    public void bindGridSourceFromContext(Object context) {
        if (context == null) {
            gridSource = null;
            visibleStartRef = null;
        } else {
            CGameObject gameObject = (CGameObject) context;
            gridSource = gameObject.tokenEntries;
            visibleStartRef = gameObject.shopInventoryVisibleStart;
        }
        resetCellUpdateCounters();
        clampVisibleStart();
    }

    /**
     * vtbl +0x98: GridOverlayVisualObject::ClampVisibleStart @004A16D8.
     * Fully ported.
     */
    public void clampVisibleStart() {
        if (gridSource == null) {
            return;
        }

        int visibleStart = getVisibleStartValue();
        int visibleCellCount = getVisibleCellCount();
        int sourceSize = requireGridSourceList().size();
        if (sourceSize - visibleStart < visibleCellCount) {
            visibleStart = sourceSize - visibleCellCount;
        }
        if (visibleStart < 0) {
            visibleStart = 0;
        }
        setVisibleStartValue(visibleStart);
    }

    /**
     * Native helper: GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78.
     * Full port. Native shifts the visible sprite/cache window by one entry and advances the visible-start pointer.
     */
    protected void scrollForwardOneEntry() {
        if (gridSource == null) {
            return;
        }

        int visibleCellCount = getScrollVisibleCellCount();
        int visibleStart = getScrollVisibleStart();
        int sourceSize = requireGridSourceList().size();
        if (visibleStart + visibleCellCount >= sourceSize) {
            return;
        }

        rotateVisibleCellStateForward(visibleCellCount);
        setScrollVisibleStart(visibleStart + 1);
        refreshVisibleCellSpritesAfterScroll();
    }

    /**
     * Native helper: GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF.
     * Full port. Native shifts the visible sprite/cache window by one entry and rewinds the visible-start pointer.
     */
    protected void scrollBackwardOneEntry() {
        if (gridSource == null || getScrollVisibleStart() == 0) {
            return;
        }

        int visibleCellCount = getScrollVisibleCellCount();
        int visibleStart = getScrollVisibleStart();
        rotateVisibleCellStateBackward(visibleCellCount);
        setScrollVisibleStart(visibleStart - 1);
        refreshVisibleCellSpritesAfterScroll();
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78 and
     * GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF trailing RefreshVisibleCellSprites call.
     */
    protected void refreshVisibleCellSpritesAfterScroll() {
        refreshVisibleCellSprites();
    }

    /**
     * Native: GridOverlayVisualObject::RefreshVisibleCellSprites @004A176C.
     * Fully ported.
     */
    protected void refreshVisibleCellSprites() {
        if (gridSource == null) {
            return;
        }

        List<Object> source = requireGridSourceList();
        clampVisibleStartBeforeGridRead();
        int visibleSourceCount = Math.min(source.size(), getVisibleCellCount());
        int visibleStart = getVisibleStartValue();
        for (int visibleIndex = 0; visibleIndex < visibleSourceCount; visibleIndex++) {
            if (cellUpdateCounters.get(visibleIndex) != 0) {
                continue;
            }

            TokenEntry entry = (TokenEntry) source.get(visibleStart + visibleIndex);
            if (entry == null) {
                continue;
            }

            if (!entry.isMoneyEntry() && !entry.isEmptyRegularEntry()) {
                visibleCellSprites.set(visibleIndex, loadVisibleCellSprite(entry));
            }
            cellUpdateCounters.set(visibleIndex, 1);
        }
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF.
     */
    protected boolean canScrollBackwardOneEntry() {
        return gridSource != null && getScrollVisibleStart() > 0;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78.
     */
    protected boolean canScrollForwardOneEntry() {
        return gridSource != null
                && getScrollVisibleStart() + getScrollVisibleCellCount()
                < getScrollSourceSize();
    }

    /**
     * vtbl +0x9C: GridOverlayVisualObject::InitArrays @004A14DF.
     * Fully ported.
     */
    public void initArrays() {
        int visibleCellCount = getVisibleCellCount();
        resizeObjectList(visibleCellSprites, visibleCellCount);
        resizeIntegerList(cellUpdateCounters, visibleCellCount);
        resizeIntegerList(cellAnimationFrames, visibleCellCount);
        for (int i = 0; i < visibleCellCount; i++) {
            visibleCellSprites.set(i, null);
            cellUpdateCounters.set(i, 0);
        }
    }

    /**
     * vtbl +0xA0: GridOverlayVisualObject::BeginUiDrag @004A235D.
     * Native drag-start ownership checks, blocked action checks, detach, UI lock capture, source rebinding, and
     * selection-change notification are ported for TokenEntry-backed inventory grids.
     */
    public Object beginUiDrag(int sourceIndex, int quantity) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (hasUiLockPayload(mainWindow)) {
            return null;
        }
        List<Object> source = requireGridSourceList();
        if (source.size() <= sourceIndex) {
            return null;
        }
        CGameObject selectedObject = mainWindow.getGridOverlayBindingContext();
        if (selectedObject.cPlayer != mainWindow.pMapVisualObject.currentPlayer) {
            return null;
        }

        TokenEntry entry = (TokenEntry) source.get(sourceIndex);
        if (entry == null || isGridEntryBlockedForCurrentContext(selectedObject, entry)) {
            return null;
        }

        Object payload = detachMatchingTokenEntry(entry, quantity);
        mainWindow.setUiLockPayload(payload);
        mainWindow.setUiLockSourceIndex(sourceIndex);
        int packedModeCode = getGridModeCode();
        mainWindow.setUiLockPackedModeCode(packedModeCode);
        if (getGridModeCode() == 2) {
            bindGridSourceFromContext(selectedObject);
        } else {
            setGridSource(gridSource);
        }
        notifyInputControllerSelectionChanged(mainWindow);
        return mainWindow.getUiLockPayload();
    }

    /**
     * vtbl +0xA4: GridOverlayVisualObject::CompleteUiDrag @004A24E8.
     * Fully ported.
     */
    public int completeUiDrag(int insertIndex) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (!hasUiLockPayload(mainWindow)) {
            return -1;
        }

        Object payload = mainWindow.getUiLockPayload();
        if (payload instanceof TokenEntry tokenEntry && tokenEntry.isMoneyEntry()) {
            mainWindow.pMapVisualObject.currentPlayer.gold += tokenEntry.quantity;
        }
        int quantity = getGridEntryDetachedQuantity(payload);
        int packedModeCode = getGridModeCode();
        boolean notifyDropCommit = shouldNotifyGridDropCommit(mainWindow, packedModeCode);
        int result = mergeOrInsertEntryAt(payload, insertIndex);
        int targetModeCode = getGridModeCode();
        if (targetModeCode == 2) {
            bindGridSourceFromContext(mainWindow.getGridOverlayBindingContext());
        } else {
            setGridSource(gridSource);
        }
        if (notifyDropCommit) {
            mainWindow.onGridOverlayDropCommitted(
                    mainWindow.getUiLockPackedModeCode(),
                    mainWindow.getUiLockSourceIndex(),
                    getGridModeCode(),
                    result,
                    quantity
            );
        }
        mainWindow.clearUiLockState();
        notifyInputControllerSelectionChanged(mainWindow);
        return result;
    }

    /**
     * vtbl +0xA8: GridOverlayVisualObject::GetGridModeCode @004A4260.
     * Fully ported.
     */
    public int getGridModeCode() {
        return -1;
    }

    /**
     * Native: GridOverlayVisualObject::AdvanceAnimatedCells @004A26C1.
     * Fully ported.
     */
    protected void advanceAnimatedCells() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (gridSource == null) {
            return;
        }

        List<Object> source = requireGridSourceList();
        clampVisibleStartBeforeGridRead();
        int visibleCellCount = getVisibleCellCount();
        int visibleSourceCount = Math.min(source.size(), visibleCellCount);
        int visibleStart = getVisibleStartValue();
        hasAnimatedVisibleCells = 0;
        for (int i = 0; i < visibleCellCount; i++) {
            if (i >= visibleSourceCount || cellUpdateCounters.get(i) == 0) {
                continue;
            }

            Object entry = source.get(visibleStart + i);
            if (entry == null || isHeldSingleQuantityEntry(mainWindow, entry)) {
                continue;
            }
            if (!hasGridEntryAnimation(entry)) {
                continue;
            }

            cellAnimationFrames.set(i, cellAnimationFrames.get(i) + 1);
            hasAnimatedVisibleCells = 1;
        }
    }

    /**
     * Native: GridOverlayVisualObject::HasAnimatedVisibleCells @0041F330.
     * Fully ported.
     */
    public int hasAnimatedVisibleCells() {
        return hasAnimatedVisibleCells;
    }

    /**
     * Native: GridOverlayVisualObject::DrawMagicTrailDots @004A285A.
     * Fully ported.
     */
    protected void drawMagicTrailDots(int x, int y, int visibleCellIndex) {
        int frame = cellAnimationFrames.get(visibleCellIndex) >>> 1;
        drawMagicTrailDot(x, y, frame, 0x3F);
        if (frame > 0) {
            drawMagicTrailDot(x, y, frame - 1, 0x7F);
        }
        if (frame > 1) {
            drawMagicTrailDot(x, y, frame - 2, 0xBF);
        }
        if (frame > 2) {
            drawMagicTrailDot(x, y, frame - 3, 0xFF);
        }
        if (frame > 3) {
            drawMagicTrailDot(x, y, frame - 4, 0xBF);
        }
        if (frame > 4) {
            drawMagicTrailDot(x, y, frame - 5, 0x7F);
        }
        if (frame > 5) {
            drawMagicTrailDot(x, y, frame - 6, 0x3F);
        }
    }

    /**
     * Native support extracted from GridOverlayVisualObject::DrawMagicTrailDots @004A285A.
     */
    private void drawMagicTrailDot(int baseX, int baseY, int trailFrame, int alpha) {
        int randomIndex = trailFrame & MAGIC_TRAIL_RANDOM_OFFSET_MASK;
        Globals.renderer.drawSoftDot(
                baseX + cellRandomOffsets1.get(randomIndex),
                baseY + cellRandomOffsets2.get(randomIndex),
                MAGIC_TRAIL_RED,
                MAGIC_TRAIL_GREEN,
                MAGIC_TRAIL_BLUE,
                alpha
        );
    }

    /**
     * Native invariant support extracted from GridOverlayVisualObject::SetGridSource @004A1659 and
     * GridOverlayVisualObject::ClampVisibleStart @004A16D8 before visible-window CArray reads.
     * not ported as a standalone native method.
     */
    private void clampVisibleStartBeforeGridRead() {
        int visibleStart = getVisibleStartValue();
        clampVisibleStart();
        if (getVisibleStartValue() != visibleStart) {
            resetCellUpdateCounters();
        }
    }

    /**
     * Native helper branch shared by SetGridSource/BindGridSourceFromContext @004A1659 and @004A15B5.
     * Fully ported.
     */
    private void resetCellUpdateCounters() {
        int visibleCellCount = getVisibleCellCount();
        for (int i = 0; i < visibleCellCount; i++) {
            cellUpdateCounters.set(i, 0);
        }
    }

    /**
     * Native helper tail shared by BeginUiDrag/CompleteUiDrag @004A235D and @004A24E8.
     * Full port.
     */
    private void notifyInputControllerSelectionChanged(CMainWindow mainWindow) {
        mainWindow.getInputController().onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, 0);
    }

    /**
     * Native support extracted from GridOverlayVisualObject::CompleteUiDrag @004A24E8.
     */
    protected int resolveInventoryTransferSourceIndex(CMainWindow mainWindow, @SuppressWarnings("unused") Object payload) {
        return mainWindow.getUiLockSourceIndex();
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D and
     * GridOverlayVisualObject::CompleteUiDrag @004A24E8.
     */
    private static boolean hasUiLockPayload(CMainWindow mainWindow) {
        return mainWindow.getUiLockPayload() != null;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::CompleteUiDrag drop-notify branch @004A24E8.
     */
    private static boolean shouldNotifyGridDropCommit(CMainWindow mainWindow, int packedModeCode) {
        return packedModeCode < 5
                || packedModeCode > 8
                || mainWindow.getUiLockPackedModeCode() != packedModeCode;
    }

    /**
     * Native owner: GridOverlayVisualObject visible cell count at +0x88/+0x8C.
     * not ported.
     */
    private int getVisibleCellCount() {
        return Math.max(0, visibleColumns * visibleRows);
    }

    /**
     * Native owner: CArray<>::operator[] reads used throughout GridOverlayVisualObject.
     * not ported.
     */
    private Object getGridSourceEntry(int index) {
        if (index < 0) {
            return null;
        }
        if (gridSource instanceof List<?> list) {
            return index < list.size() ? list.get(index) : null;
        }
        if (gridSource instanceof Object[] array) {
            return index < array.length ? array[index] : null;
        }
        return null;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::MergeOrInsertEntryAt @004A1DFE,
     * GridOverlayVisualObject::MergeOrAppendEntry @004A1F72,
     * GridOverlayVisualObject::DetachTokenEntryByEntryId @004A2050, and
     * GridOverlayVisualObject::DetachMatchingTokenEntry @004A218B.
     */
    @SuppressWarnings("unchecked")
    private List<Object> requireMutableGridSourceList() {
        return (List<Object>) gridSource;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::RefreshVisibleCellSprites @004A176C,
     * GridOverlayVisualObject::BeginUiDrag @004A235D, and
     * GridOverlayVisualObject::AdvanceAnimatedCells @004A26C1.
     */
    @SuppressWarnings("unchecked")
    private List<Object> requireGridSourceList() {
        return (List<Object>) gridSource;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ClampVisibleStart @004A16D8,
     * GridOverlayVisualObject::RefreshVisibleCellSprites @004A176C,
     * GridOverlayVisualObject::SetVisibleStart @004A1D1F,
     * GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78,
     * GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF, and
     * GridOverlayVisualObject::AdvanceAnimatedCells @004A26C1.
     * Fully ported.
     */
    private int getVisibleStartValue() {
        return requireVisibleStartRef()[0];
    }

    /**
     * Native: GridOverlayVisualObject::GetVisibleStart @0046E480.
     * Fully ported.
     */
    public int getVisibleStart() {
        return getVisibleStartValue();
    }

    /**
     * Native: GridOverlayVisualObject::SetVisibleStart @004A1D1F.
     * Fully ported.
     */
    public void setVisibleStart(int visibleStart) {
        if (gridSource == null) {
            return;
        }

        List<Object> source = requireGridSourceList();
        setVisibleStartValue(visibleStart);
        if (source.size() < getVisibleStartValue()) {
            setVisibleStartValue(source.size() - getVisibleCellCount());
        }
        if (getVisibleStartValue() < 0) {
            setVisibleStartValue(0);
        }
        for (int visibleIndex = 0; visibleIndex < getVisibleCellCount(); visibleIndex++) {
            cellUpdateCounters.set(visibleIndex, 0);
        }
        refreshVisibleCellSprites();
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ClampVisibleStart @004A16D8,
     * GridOverlayVisualObject::SetVisibleStart @004A1D1F,
     * GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78, and
     * GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF.
     * Fully ported.
     */
    private void setVisibleStartValue(int value) {
        requireVisibleStartRef()[0] = value;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ClampVisibleStart @004A16D8,
     * GridOverlayVisualObject::RefreshVisibleCellSprites @004A176C,
     * GridOverlayVisualObject::SetVisibleStart @004A1D1F,
     * GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78,
     * GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF, and
     * GridOverlayVisualObject::AdvanceAnimatedCells @004A26C1.
     * Fully ported.
     */
    private int[] requireVisibleStartRef() {
        return (int[]) visibleStartRef;
    }

    /**
     * Java helper for tolerant `CDWordArray` reads in partial ports.
     * not ported.
     */
    private static int getIntegerOrZero(List<Integer> values, int index) {
        if (index < 0 || index >= values.size()) {
            return 0;
        }
        Integer value = values.get(index);
        return value == null ? 0 : value;
    }

    /**
     * Java helper for partial list sizing used by slot 0x9C.
     * not ported.
     */
    private static void resizeObjectList(List<Object> values, int size) {
        while (values.size() < size) {
            values.add(null);
        }
        while (values.size() > size) {
            values.remove(values.size() - 1);
        }
    }

    /**
     * Java helper for partial list sizing used by slot 0x9C and the animated-cell helper.
     * not ported.
     */
    private static void resizeIntegerList(List<Integer> values, int size) {
        while (values.size() < size) {
            values.add(0);
        }
        while (values.size() > size) {
            values.remove(values.size() - 1);
        }
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78.
     * not ported as a standalone native method.
     */
    private void rotateVisibleCellStateForward(int visibleCellCount) {
        Object firstSprite = visibleCellSprites.get(0);
        for (int index = 0; index < visibleCellCount - 1; index++) {
            visibleCellSprites.set(index, visibleCellSprites.get(index + 1));
            cellAnimationFrames.set(index, cellAnimationFrames.get(index + 1));
        }
        int lastIndex = visibleCellCount - 1;
        visibleCellSprites.set(lastIndex, firstSprite);
        cellAnimationFrames.set(lastIndex, 0);
        cellUpdateCounters.set(lastIndex, 0);
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF.
     * not ported as a standalone native method.
     */
    private void rotateVisibleCellStateBackward(int visibleCellCount) {
        int lastIndex = visibleCellCount - 1;
        Object lastSprite = visibleCellSprites.get(lastIndex);
        for (int index = lastIndex; index > 0; index--) {
            visibleCellSprites.set(index, visibleCellSprites.get(index - 1));
            cellAnimationFrames.set(index, cellAnimationFrames.get(index - 1));
        }
        visibleCellSprites.set(0, lastSprite);
        cellUpdateCounters.set(0, 0);
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78 and
     * GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF.
     * not ported as a standalone native method.
     */
    private int getScrollVisibleStart() {
        return getVisibleStartValue();
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78 and
     * GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF.
     * not ported as a standalone native method.
     */
    private void setScrollVisibleStart(int value) {
        setVisibleStartValue(value);
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78 and
     * GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF.
     * not ported as a standalone native method.
     */
    private int getScrollVisibleCellCount() {
        return Math.max(0, visibleColumns * visibleRows);
    }

    /**
     * Native support extracted from GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78.
     * not ported as a standalone native method.
     */
    private int getScrollSourceSize() {
        if (gridSource instanceof List<?> list) {
            return list.size();
        }
        if (gridSource instanceof Object[] array) {
            return array.length;
        }
        return 0;
    }

    /**
     * Native owner: detached payload state reads from TokenEntry +0x10.
     * not ported for non-TokenEntry entry contracts.
     */
    private static int getGridEntryDetachedQuantity(Object entry) {
        if (entry instanceof TokenEntry tokenEntry) {
            return tokenEntry.quantity;
        }
        return 0;
    }

    /**
     * Native owner: animated-cell flag check on the source entry payload.
     * not ported.
     */
    private static boolean hasGridEntryAnimation(Object entry) {
        return entry instanceof TokenEntry tokenEntry
                && (tokenEntry.wireFlags & DYNAMIC_PAYLOAD_FLAG) != 0;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D.
     */
    private static boolean isGridEntryBlockedForCurrentContext(CGameObject selectedObject, TokenEntry entry) {
        return entry.getType() < 2
                && (selectedObject.lastAction == 3 || selectedObject.lastAction == 7 || selectedObject.lastAction == 8);
    }

    /**
     * Native owner: carried-entry exclusion branch inside GridOverlayVisualObject::AdvanceAnimatedCells.
     * not ported.
     */
    private static boolean isHeldSingleQuantityEntry(CMainWindow mainWindow, Object entry) {
        return mainWindow.getUiLockPayload() == entry
                && getGridEntryDetachedQuantity(entry) == 1;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::RefreshVisibleCellSprites @004A176C.
     */
    private static CA16 loadVisibleCellSprite(TokenEntry entry) {
        CA16 sprite = new CA16(Resources.path(
                GRAPHICS_DIRECTORY,
                INVENTORY_DIRECTORY,
                entry.getEquipmentPortraitResourceName() + A16_SUFFIX
        ));
        sprite.initPalette(0x10, 4, 0);
        return sprite;
    }
}
