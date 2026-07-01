package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;

import java.io.IOException;

public class Sack extends Token {
    //0x3C
    public int gold;
    //0x40
    public Inventory inventory = new Inventory();
    //0x44
    public int ownerOnlyFlag;
    //0x48
    public int visiblePlayerMask;
    //0x4A
    public int lastPublishedVisiblePlayerMask;
    //0x4C
    public int visibilityRefreshMask = 0xFFFF;

    /**
     * Native: Sack::Sack @0052A5EF.
     * Fully ported.
     */
    public Sack() {
        initializeSackDefaults();
    }

    /**
     * Native support extracted from CServerApp::notifyStateChanged @00503672 sack branch byte store.
     */
    public int computeSackTypeByte() {
        return (int) Math.clamp(Math.log10(price), 0,255);
    }

    /**
     * vtbl +0x08: Sack::serialize @0052D8DE.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar); // Token::serialize
        if (!ar.isStoring()) {
            gold = ar.readInt();
        } else {
            ar.writeInt(gold);
        }
        ar.serialize(inventory);
    }

    /**
     * Native: Sack::initAtTarget @0052A683.
     * Fully ported.
     */
    public Sack initAtTarget(TargetHandle targetHandle) {
        initializeTokenFromTarget(targetHandle);
        inventory = new Inventory();
        initializeSackDefaults();
        return this;
    }

    /**
     * Native: Sack::initAtTargetWithInventory @0052A71D.
     * Fully ported.
     */
    public Sack initAtTargetWithInventory(TargetHandle targetHandle, Inventory sourceInventory) {
        initializeTokenFromTarget(targetHandle);
        inventory = sourceInventory;
        initializeSackDefaults();
        return this;
    }

    /**
     * Native support extracted from Token::Init @0050D549 and Token::initializeNativeDefaults @0050D6AE for
     * Sack::initAtTarget @0052A683 and Sack::initAtTargetWithInventory @0052A71D.
     * Fully ported.
     */
    private void initializeTokenFromTarget(TargetHandle targetHandle) {
        owner = null;
        effects.clear();
        m_pTargetHandle.assignFrom(targetHandle);
        word = 0;
        price = 0;
        idFull = 0;
    }

    /**
     * Native: Sack::initializeSackDefaults @0052A780.
     * Fully ported.
     */
    private Sack initializeSackDefaults() {
        gold = 0;
        ownerOnlyFlag = 0;
        idFull = Globals.gameServer.allocateNextFreeId() & 0xFFFF;
        if (!m_pTargetHandle.isSubPosUnknown()) {
            m_pTargetHandle.setPosition(m_pTargetHandle.getX(), m_pTargetHandle.getY());
        }
        visiblePlayerMask = 0;
        lastPublishedVisiblePlayerMask = 0;
        visibilityRefreshMask = 0xFFFF;
        return this;
    }

    /**
     * Native: Sack::recalculateTotalValue @0052A895.
     * Fully ported.
     */
    public int recalculateTotalValue() {
        price = gold;
        for (Item item : inventory.items) {
            price += item.price;
        }
        return price;
    }

    /**
     * vtbl +0x14: Sack::updateRegen @00544E80.
     * Fully ported.
     */
    @Override
    public void updateRegen() {
    }

    /**
     * vtbl +0x18: Sack::update @00544E90.
     * Fully ported.
     */
    @Override
    public Object update() {
        return null;
    }
}
