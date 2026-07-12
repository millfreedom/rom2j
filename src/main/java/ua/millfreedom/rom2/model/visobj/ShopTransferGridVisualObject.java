package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_PUT_ITEMS_HERE_TO_BUY_SELL_59;

/**
 * Native class: ShopTransferGridVisualObject (vtbl @0x005CF240).
 * Purpose: shop transfer item-slot grid with left/right scroll controls and an internal transaction-entry list.
 */
public class ShopTransferGridVisualObject extends ShopItemGridVisualObject {
    public static final int NATIVE_SIZE = 0x2108; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String GRAPHICS_INTERFACE_DIRECTORY = "graphics/interface/";
    private static final String SHOP_TABLE_BMP = "shoptable.bmp";
    private static final int SHOP_SPEECH_ROLL_MAX = 100;
    private static final int SHOP_SPEECH_CHANCE = 0x1E;
    private static final byte SHOP_SPEECH_PRIORITY = (byte) 0x80;

    //0x20c4
    public CRect[] visibleCellRects;

    //0x20cc
    public final CRect leftScrollButtonRect = new CRect();

    //0x20dc
    public final CRect rightScrollButtonRect = new CRect();

    //0x20ec
    public CBmp64k tableBackgroundBitmap;
    //0x20f4
    public final List<TokenEntry> transferEntries = new ArrayList<>();

    /**
     * Native: ShopTransferGridVisualObject::ShopTransferGridVisualObject @004B5242.
     * Full port.
     */
    public ShopTransferGridVisualObject() {
        super();
        initializeShopTransferGrid(false);
    }

    /**
     * Native: ShopTransferGridVisualObject::ShopTransferGridVisualObject @004B5325.
     * Full port.
     */
    public ShopTransferGridVisualObject(int id, CRect rect, ShopDialogVisualObject ownerDialog) {
        super(id, rect, ownerDialog);
        initializeShopTransferGrid(true);
    }

    /**
     * Native: ShopTransferGridVisualObject::ShopTransferGridVisualObject @004B5424.
     * Full port.
     */
    public ShopTransferGridVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ShopDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, ownerDialog);
        initializeShopTransferGrid(true);
    }

    /**
     * Native support extracted from ShopTransferGridVisualObject constructors @004B5242, @004B5325, and @004B5424.
     */
    private void initializeShopTransferGrid(boolean clearTransferEntries) {
        this.visibleColumns = cRect.width() / 0x50 - 1;
        this.visibleRows = cRect.height() / 0x50;
        this.gridSource = transferEntries;
        if (clearTransferEntries) {
            transferEntries.clear();
        }
        initializeOverlayLayout();
        initArrays();
        this.tableBackgroundBitmap = null;
    }

    /**
     * Native: ShopTransferGridVisualObject::ClearTransferEntries @004B5F78.
     * Full port. Java collection ownership represents the native TokenEntry delete/remove loop.
     */
    public void clearTransferEntries() {
        transferEntries.clear();
    }

    /**
     * vtbl +0x2C: ShopTransferGridVisualObject::Update @004B59BF.
     * Full port.
     */
    @Override
    public void update() {
        if (ownerDialog.dialogActiveFlag == 0 || tableBackgroundBitmap == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        tableBackgroundBitmap.drawRectMasked(
                screenRect.left,
                screenRect.top,
                0,
                0,
                cRect.width(),
                cRect.height()
        );

        super.update();
    }

    /**
     * vtbl +0x14: ShopTransferGridVisualObject::GetText @004B5939.
     * Full port.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0 || Globals.mainWindow.getUiLockPayload() != null) {
            return null;
        }
        if (getGridIndexAtScreenPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY()) < 0) {
            return get(MAIN_PUT_ITEMS_HERE_TO_BUY_SELL_59);
        }
        return super.getText();
    }

    /**
     * vtbl +0x4C: ShopTransferGridVisualObject::OnMouseMove @004B5B63.
     * Full port.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if ((nFlags & 1) != 0) {
            CMainWindow mainWindow = Globals.mainWindow;
            Object payload = mainWindow.getUiLockPayload();
            if (payload != null) {
                if (isTransferGridFullForDistinctEntry((TokenEntry) payload)) {
                    ownerDialog.uiLockPlacementAllowedFlag = 0;
                    CMousePointer.Cursor_CantPut.setToMousePointer();
                } else if (getCatalogEntryValue((TokenEntry) payload) > 0) {
                    ownerDialog.uiLockPlacementAllowedFlag = 1;
                    mainWindow.cursor.setToMousePointer();
                }
            }
        }
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x54: ShopTransferGridVisualObject::OnLButtonDown @004B5C18.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x58: ShopTransferGridVisualObject::OnLButtonUp @004B5C39.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        Object payload = Globals.mainWindow.getUiLockPayload();
        if (payload == null) {
            return 1;
        }
        if (isTransferGridFullForDistinctEntry((TokenEntry) payload)) {
            ownerDialog.uiLockPlacementAllowedFlag = 0;
        }
        return super.onLButtonUp(nFlags, x, y);
    }

    /**
     * vtbl +0x5C: ShopTransferGridVisualObject::OnLButtonDblClk @004B5CA0.
     * Full port.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return super.onLButtonDblClk(nFlags, x, y);
    }

    /**
     * vtbl +0x78: ShopTransferGridVisualObject::MergeOrInsertEntryAt @004B5CC1.
     * Full port.
     */
    @Override
    public int mergeOrInsertEntryAt(Object entry, int insertIndex) {
        TokenEntry tokenEntry = (TokenEntry) entry;
        if (tokenEntry.gridModeCode == 1) {
            tokenEntry.gridModeCode = 2;
        }
        return super.mergeOrInsertEntryAt(tokenEntry, insertIndex);
    }

    /**
     * vtbl +0x7C: ShopTransferGridVisualObject::MergeOrAppendEntry @004B5D04.
     * Full port.
     */
    @Override
    public int mergeOrAppendEntry(Object entry) {
        if (gridSource == null) {
            return -1;
        }
        TokenEntry requested = (TokenEntry) entry;
        List<TokenEntry> sourceEntries = requireTransferGridSourceEntries();
        for (int index = 0; index < sourceEntries.size(); index++) {
            TokenEntry existing = sourceEntries.get(index);
            if (existing.entryId == requested.entryId && existing.gridModeCode == requested.gridModeCode) {
                existing.addQuantity(requested.quantity);
                return index;
            }
        }
        if (sourceEntries.size() < visibleColumns * visibleRows) {
            sourceEntries.add(requested);
        }
        return sourceEntries.size() - 1;
    }

    /**
     * vtbl +0xBC: ShopTransferGridVisualObject::DetachTransferEntryByEntryIdAndMode @004B5E2F.
     * Full port.
     */
    public TokenEntry detachTransferEntryByEntryIdAndMode(int entryId, int gridModeCode) {
        if (gridSource == null) {
            return null;
        }
        List<TokenEntry> sourceEntries = requireTransferGridSourceEntries();
        for (int index = 0; index < sourceEntries.size(); index++) {
            TokenEntry entry = sourceEntries.get(index);
            if (entry.entryId == entryId && entry.gridModeCode == gridModeCode) {
                if (entry.tryRemoveQuantityLeavingRemainder(1)) {
                    TokenEntry detached = new TokenEntry(entry);
                    detached.quantity = 1;
                    clampVisibleStart();
                    return detached;
                }
                sourceEntries.remove(index);
                entry.quantity = 1;
                clampVisibleStart();
                return entry;
            }
        }
        clampVisibleStart();
        return null;
    }

    /**
     * vtbl +0xA0: ShopTransferGridVisualObject::BeginUiDrag @004B64A6.
     */
    @Override
    public Object beginUiDrag(int sourceIndex, int dragState) {
        playPointerSound(putOffSound);
        return super.beginUiDrag(sourceIndex, dragState);
    }

    /**
     * vtbl +0xA4: ShopTransferGridVisualObject::CompleteUiDrag @004B5FFA.
     * Full port.
     */
    @Override
    public int completeUiDrag(int insertIndex) {
        playCampaignShopSpeechIfNeeded(Globals.mainWindow.getUiLockPayload());
        playPointerSound(putOnSound);
        return completeUiDragCore(insertIndex);
    }

    /**
     * Native: ShopTransferGridVisualObject::CompleteUiDragCore @004B6399.
     * Full port.
     */
    private int completeUiDragCore(int insertIndex) {
        CMainWindow mainWindow = Globals.mainWindow;
        Object payload = mainWindow.getUiLockPayload();
        if (payload == null) {
            return -1;
        }

        int quantity = getDetachedTokenQuantity(payload);
        int sourceIndex = resolveInventoryTransferSourceIndex(mainWindow, payload);
        int result = mergeOrInsertEntryAt(payload, insertIndex);
        setGridSource(gridSource);
        notifyGridOverlayDropCommitted(
                mainWindow,
                mainWindow.getUiLockPackedModeCode(),
                sourceIndex,
                getGridModeCode(),
                result,
                quantity
        );
        mainWindow.clearUiLockState();
        mainWindow.getInputController().onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, 0);
        return result;
    }

    /**
     * Native support extracted from TokenEntry +0x10 quantity reads in ShopTransferGridVisualObject::CompleteUiDragCore @004B6399.
     */
    private static int getDetachedTokenQuantity(Object payload) {
        return ((TokenEntry) payload).quantity;
    }

    /**
     * Native support extracted from ShopTransferGridVisualObject::CompleteUiDragCore @004B6399.
     */
    @Override
    protected int resolveInventoryTransferSourceIndex(CMainWindow mainWindow, Object payload) {
        int sourceModeCode = mainWindow.getUiLockPackedModeCode();
        if (sourceModeCode >= 5 && sourceModeCode <= 8) {
            return ((TokenEntry) payload).sourceSlotDescriptor;
        }
        return mainWindow.getUiLockSourceIndex();
    }

    /**
     * vtbl +0xA8: ShopTransferGridVisualObject::GetGridModeCode @004B6780.
     * Full port.
     */
    @Override
    public int getGridModeCode() {
        return 4;
    }

    /**
     * vtbl +0xAC: ShopTransferGridVisualObject::InitializeOverlayLayout @004B5704.
     * Full port.
     */
    @Override
    public void initializeOverlayLayout() {
        int visibleCellCount = visibleColumns * visibleRows;
        visibleCellRects = new CRect[visibleCellCount];

        leftScrollButtonRect.set(cRect.left, cRect.top, cRect.left + 0x20, cRect.bottom);
        rightScrollButtonRect.set(cRect.left + 0x1B0, cRect.top, cRect.right, cRect.bottom);

        for (int column = 0; column < visibleColumns; column++) {
            for (int row = 0; row < visibleRows; row++) {
                visibleCellRects[row * visibleColumns + column] = new CRect(
                        leftScrollButtonRect.right + column * 0x50,
                        cRect.top + row * 0x50,
                        leftScrollButtonRect.right + 0x50 + column * 0x50,
                        cRect.top + 0x50 + row * 0x50
                );
            }
        }
    }

    /**
     * vtbl +0xB4: ShopTransferGridVisualObject::InitializeOverlayBitmaps @004B55AD.
     * Full port. Native stores the loaded `CBmp64k *` in `tableBackgroundBitmap` at +0x20EC.
     */
    @Override
    public void initializeOverlayBitmaps() {
        releaseOverlayBitmaps();
        String shopResourceDirectory = ownerDialog == null ? "" : ownerDialog.getShopResourceDirectory();
        tableBackgroundBitmap = new CBmp64k(GRAPHICS_INTERFACE_DIRECTORY + shopResourceDirectory + SHOP_TABLE_BMP);
    }

    /**
     * vtbl +0xB8: ShopTransferGridVisualObject::ReleaseOverlayBitmaps @004B56AD.
     * Full port. Java releases the native `tableBackgroundBitmap` slot by dropping the managed bitmap reference.
     */
    @Override
    public void releaseOverlayBitmaps() {
        tableBackgroundBitmap = null;
    }

    /**
     * Native support hook for inherited ShopItemGridVisualObject::Update @004B18E2.
     * ShopTransferGridVisualObject::Update @004B59BF draws the transfer table before delegating to the inherited item renderer.
     */
    @Override
    protected void drawPanelBackground(CRect screenRect) {
    }

    /**
     * Java helper for the child-owned `CRect[]` geometry populated by ShopTransferGridVisualObject::InitializeOverlayLayout @004B5704.
     */
    @Override
    protected CRect getCellRect(int visibleIndex) {
        return visibleCellRects[visibleIndex];
    }

    /**
     * Native support: ShopTransferGridVisualObject::IsTransferGridFullForDistinctEntry @004B5A63.
     * Full support port.
     */
    boolean isTransferGridFullForDistinctEntry(TokenEntry requested) {
        TokenEntry comparable = new TokenEntry(requested);
        comparable.gridModeCode = comparable.gridModeCode == 1 ? 2 : comparable.gridModeCode;
        List<TokenEntry> sourceEntries = requireTransferGridSourceEntries();
        for (TokenEntry existing : sourceEntries) {
            if (comparable.matchesStackIdentity(existing)) {
                return false;
            }
        }
        return sourceEntries.size() >= visibleColumns * visibleRows;
    }

    /**
     * Native support: TokenEntry::GetCatalogEntryValue @004B6680.
     */
    private static int getCatalogEntryValue(TokenEntry entry) {
        return entry.getCatalogEntryValue();
    }

    /**
     * Native support extracted from ShopTransferGridVisualObject::IsTransferGridFullForDistinctEntry @004B5A63,
     * MergeOrAppendEntry @004B5D04, and DetachTransferEntryByEntryIdAndMode @004B5E2F.
     * Full support port.
     */
    @SuppressWarnings("unchecked")
    private List<TokenEntry> requireTransferGridSourceEntries() {
        return (List<TokenEntry>) gridSource;
    }

    /**
     * Native support extracted from ShopTransferGridVisualObject::CompleteUiDrag @004B5FFA.
     */
    private void playCampaignShopSpeechIfNeeded(Object payload) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (Utils.randInclusive(SHOP_SPEECH_ROLL_MAX) >= SHOP_SPEECH_CHANCE
                || mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }
        TokenEntry tokenEntry = (TokenEntry) payload;
        if (tokenEntry.gridModeCode <= 4 || tokenEntry.gridModeCode >= 9) {
            return;
        }

        String speechPath = resolveCampaignShopSpeechPath(mainWindow, tokenEntry);
        if (primaryGridSound != null && SoundSystem.get().isSoundPlaying(primaryGridSound)) {
            return;
        }
        if (primaryGridSound != null) {
            SoundSystem.get().releaseSound(primaryGridSound);
        }

        primaryGridSound = new Sound(speechPath);
        primaryGridSound.playIfNotPlaying(Globals.soundPreferences.speechVolume, false, SHOP_SPEECH_PRIORITY, 0);
    }

    /**
     * Native support extracted from ShopTransferGridVisualObject::CompleteUiDrag @004B5FFA.
     */
    private String resolveCampaignShopSpeechPath(CMainWindow mainWindow, TokenEntry tokenEntry) {
        String shopSpeechDirectory = resolveShopSpeechDirectory(mainWindow);
        int effectAttributeId = tokenEntry.findShopSpeechEffectAttributeId();
        if (effectAttributeId != 0) {
            return String.format("speech/%s/effects/%02d.wav", shopSpeechDirectory, effectAttributeId);
        }

        int bookSpellId = tokenEntry.getShopSpeechBookSpellId();
        if (bookSpellId != 0) {
            return String.format("speech/%s/books/%02d.wav", shopSpeechDirectory, bookSpellId);
        }

        return String.format(
                "speech/%s/s%02di%02dp%d.wav",
                shopSpeechDirectory,
                tokenEntry.getType(),
                tokenEntry.getId(),
                Utils.randBasedInclusive(1, 3)
        );
    }

    /**
     * Native support extracted from ShopTransferGridVisualObject::CompleteUiDrag @004B5FFA.
     */
    private String resolveShopSpeechDirectory(CMainWindow mainWindow) {
        if (mainWindow.pKaargShopDialogVisualObject == ownerDialog) {
            return "shop_kaarg";
        }
        if (mainWindow.pDruidShopDialogVisualObject == ownerDialog) {
            return "shop_druid";
        }
        return "shop";
    }

    /**
     * Native helper thunk pair: Sound::StopAndRewindPointerSound @004385B0 + Sound::PlayPointer @00438570.
     */
    private static void playPointerSound(Sound sound) {
        if (sound == null) {
            return;
        }
        SoundSystem.get().stopAndRewind(sound);
        sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
    }
}
