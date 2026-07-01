package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

public class Player implements MfcSerializable {
    private static final int KNOWLEDGE_TABLE_SIZE = 0xA00;
    private static final int XOR_KEY_00546740 = 0x5C073F4D;
    private static final int WORD_MAX_SIGNED = 0x7FFF;

    //0x04
    public int playerId;
    //0x08
    public int scenarioPlayerId;
    //0x10
    public int characterSessionKeyPart1;
    //0x14
    public int characterSessionKeyPart2;
    //0x18
    public String name = "";
    //0x24
    public final UnitList ownedUnits = new UnitList();
    //0x28
    public final UnitGroupList unitGroups = new UnitGroupList();
    //0x2c
    public int isActive;
    //0x30
    public int scanMask;
    //0x32
    public int scanMaskMirror;
    //0x34
    public PlayerBattlePreferences battlePreferences = new PlayerBattlePreferences();
    //0x38
    public Object controlledUnit;
    //0x3c
    public int gold;
    //0x40
    public int missionResultState;
    //0x41
    public int missionEntryStateSent;
    //0x42
    public int clientConnected;
    //0x43
    public int mapLoadPending;
    //0x44
    public byte[] knowledgeTable = new byte[KNOWLEDGE_TABLE_SIZE];
    //0xa44
    public int colorSlot;
    //0xa45
    public int joinOptions;
    //0xa48. Creature/non-humanoid kill counter incremented by UnitList::updateActiveUnits @0052B459
    // and Unit::updateSkills @005175AB; serialized obfuscated by Player::Serialize @0052D1C5.
    public int creatureKillCount;
    //0xa4c. Player/humanoid kill counter incremented by UnitList::updateActiveUnits @0052B459
    // in the PLAYER_KILL_ANNOUNCEMENT_ACTION_94 branch; serialized as a clamped/sign-extended word.
    public int playerKillCount;
    //0xa50
    public int pendingRemovalServerTick;
    //0xa54. Saved-character deaths displayed as "Deaths" in CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
    public int deathCount;
    //0xa58. Saved-character frags displayed as "Frags" in CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
    public int fragCount;
    //0xa5c. Percent of max MP copied to Unit::m_wRegenStore by Unit/Humanoid::recalculateDerivedStats.
    public int mpRegenPercent;
    //0xa60
    public int playerEliminationQuestEnabled;
    //0xa64
    public int returnAfterDeathPending;
    //0xa68
    public int shoutDelayTicksRemaining;
    //0xa6c
    public int missionEntryDropCell;
    //0xa70
    public String characterLockName = "";
    //0xa74
    public int lastSaveTick;
    //0xa78
    public int cheatCommandFlag;

    /**
     * Native support extracted from Player::New @00515CD8 constructor defaults used by scenario player materialization.
     * Fully ported.
     */
    public Player() {
        name = "-unnamed-";
        scenarioPlayerId = 1;
        isActive = 1;
        mpRegenPercent = 0x5F;
    }

    /**
     * Native: Player::Serialize @0052D1C5.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        // CObject::Serialize(this, ar) is a no-op in this port.

        if (ar.isStoring()) {
            serializeStoreKnownBlock(ar);
        } else {
            serializeLoadKnownBlock(ar);
        }

        // Always called at tail of Player::Serialize in native.
        ar.serialize(unitGroups);
        ar.serialize(battlePreferences);

        if (!ar.isStoring()) {
            runPostLoadFixups();
        }
    }

    /**
     * Native: Global::getPlayerFormationMode @00573816.
     * Fully ported.
     */
    public int getFormationMode() {
        return battlePreferences.formationMode & 0xFF;
    }

    /**
     * Native: Global::togglePlayerFormationMode @005737DB.
     * Fully ported.
     */
    public int toggleFormationMode() {
        battlePreferences.formationMode = (battlePreferences.formationMode & 0xFF) == 0 ? 1 : 0;
        return battlePreferences.formationMode;
    }

    /**
     * Native: Player::isMapLoadPending @005160EF.
     * Fully ported.
     */
    public boolean isMapLoadPending() {
        return (mapLoadPending & 0xFF) != 0;
    }

    /**
     * Native: Player::markDisconnectedForRemoval @00516165.
     * Fully ported.
     */
    public void markDisconnectedForRemoval() {
        mapLoadPending = 0;
        clientConnected = 0;
        pendingRemovalServerTick = Globals.gameServer.someValue;
    }

    /**
     * Native: Player::AdjustGoldAndNotify @00516238.
     * Fully ported.
     */
    public void adjustGoldAndNotify(int deltaGold, int context) {
        gold += deltaGold;
        CServerApp.sendTwoDwordAction(this, GameActionId.MONEY_ACTION_67, gold, context);
    }

    /**
     * Native: Player::isCheatCommandEnabled @0053DD90.
     * Fully ported.
     */
    public boolean isCheatCommandEnabled() {
        return (cheatCommandFlag & 0xFF) > 0x32;
    }

    /**
     * Native: Player::setCheatCommandFlag @0053DDB0.
     * Fully ported.
     */
    public void setCheatCommandFlag(int cheatCommandFlag) {
        this.cheatCommandFlag = cheatCommandFlag & 0xFF;
    }

    /**
     * Native: Player::killOwnedUnits @00516104.
     * Fully ported.
     */
    public void killOwnedUnits() {
        for (Unit unit : ownedUnits) {
            unit.m_nHP = -50;
        }
    }

    /**
     * Native: Player::detachDeadUnitFromOwner @0051618E.
     * Fully ported.
     */
    public void detachDeadUnitFromOwner(Unit unit) {
        UnitGroup unitGroup = unit.unitGroup;
        unitGroup.removeUnit(unit);
        if (unitGroup.units.isEmpty() && unitGroup.owner.isActive == 0) {
            unitGroups.remove(unitGroup);
        }
        ownedUnits.remove(unit);
    }

    /**
     * Native support extracted from Player::Serialize @0052D1C5.
     * Fully ported.
     */
    private void serializeStoreKnownBlock(CArchive ar) throws IOException {
        ar.writeCString(name);
        ar.writeShort(playerId);
        ar.writeInt(scenarioPlayerId);
        ar.writeInt(characterSessionKeyPart1);
        ar.writeInt(characterSessionKeyPart2);
        ar.writeByte(colorSlot);
        ar.writeInt(isActive);
        ar.writeShort(scanMask);
        ar.writeInt(xorObfuscatedInt(gold));
        ar.writeByte(missionResultState);
        ar.writeByte(missionEntryStateSent);
        ar.writeInt(xorObfuscatedInt(creatureKillCount));
        ar.writeInt(pendingRemovalServerTick);
        ar.writeShort(clampWordUpper(deathCount));
        ar.writeShort(clampWordUpper(playerKillCount));
        ar.writeInt(mpRegenPercent);
        ar.writeInt(Utils.encodePointerLike(controlledUnit));

        // Native writes `this` pointer; use a stable token surrogate in Java.
        ar.writeInt(getSelfPointerToken());

        ar.writeBytes(knowledgeTable);
    }

    /**
     * Native support extracted from Player::Serialize @0052D1C5.
     * Fully ported.
     */
    private void serializeLoadKnownBlock(CArchive ar) throws IOException {
        name = ar.readCString();
        playerId = ar.readUShort();
        scenarioPlayerId = ar.readInt();
        characterSessionKeyPart1 = ar.readInt();
        characterSessionKeyPart2 = ar.readInt();
        colorSlot = ar.readByte() & 0xFF;
        isActive = ar.readInt();
        scanMask = ar.readUShort();
        scanMaskMirror = scanMask;
        gold = xorObfuscatedInt(ar.readInt());
        missionResultState = ar.readByte() & 0xFF;
        missionEntryStateSent = ar.readByte() & 0xFF;
        creatureKillCount = xorObfuscatedInt(ar.readInt());
        pendingRemovalServerTick = ar.readInt();
        deathCount = ar.readShort(); // sign-extended word in native
        playerKillCount = ar.readShort(); // sign-extended word in native
        mpRegenPercent = ar.readInt();
        controlledUnit = ar.readInt();

        Globals.gameServer.setPointerMapEntry(ar.readInt(), this);

        knowledgeTable = ar.readBytes(KNOWLEDGE_TABLE_SIZE);
    }

    /**
     * Native: Global::xorObfuscatedInt @00546740.
     * Fully ported.
     */
    private static int xorObfuscatedInt(int param1) {
        return param1 ^ XOR_KEY_00546740;
    }

    /**
     * Native support extracted from Player::Serialize @0052D1C5 clamped word stores.
     * Fully ported.
     */
    private static int clampWordUpper(int value) {
        return value > WORD_MAX_SIGNED ? WORD_MAX_SIGNED : value;
    }

    // not ported.
    private int getSelfPointerToken() {
        return System.identityHashCode(this);
    }

    /**
     * Native: Player::Serialize post-load fixups @0052D52E.
     * Fully ported.
     */
    private void runPostLoadFixups() {
        // Native call: Unit::RestoreContext @00546700 on Player +0x38.
        controlledUnit = Unit.restoreContextToken(controlledUnit);

        for (UnitGroup group : unitGroups) {
            group.restoreContext();
            for (Unit unit : group.units) {
                ownedUnits.add(unit);
                unit.owner = this;
            }
        }
    }

    /**
     * Native support represented by Player::operator>> @00515CBC and Player::CRuntimeClass metadata.
     * Fully ported.
     */
    @Override
    public boolean isDirect() {
        return true;
    }
}
