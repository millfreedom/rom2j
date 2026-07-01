package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.actiondata.UnitChangePayloads;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.model.spell.Spellbook;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;
import ua.millfreedom.rom2.model.unit.UnitInfo;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.model.unit.humanoid.human.Human;
import ua.millfreedom.rom2.model.unit.humanoid.human.HumanInfo;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.NpcNamesText;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.NPCNAMES;

/**
 * Native action class `UnitChangeAction` used by `CServerApp::NetUpdate @00502019`
 * to send unit state delta payloads. The payload buffer is native-owned at UnitChangeAction_Base +0x14,
 * after the UnitChangeAction size and flag fields.
 */
public class UnitChangeAction extends CGameAction {
    public static final UnitChangeAction global = new UnitChangeAction();
    // Native CUnit::unitFlags humanoid bit used by MapVisualObject::HandleGameAction @0041020F.
    private static final int UNIT_FLAG_HUMANOID = 0x01;
    // Native CUnit::unitFlags bit included in the UnitChange selection-state refresh gate @00410A76.
    private static final int UNIT_FLAG_REFRESH_SELECTION_STATE = 0x02;
    // Native CUnit::unitFlags bit gating CGameSession saved-character progress refresh @00410521.
    private static final int UNIT_FLAG_SAVED_CHARACTER_PROGRESS_SOURCE = 0x20;
    private static final int UNIT_ACTION_DYING = 6;
    private static final int LOW_RUNTIME_OBJECT_TOKEN_LIMIT = 0x6000;
    private static final int IMMEDIATE_SAVED_CHARACTER_PROGRESS_MASK = 0x22100000;
    private static final int THROTTLED_SAVED_CHARACTER_PROGRESS_MASK = 0x00001F00;

    //0x06
    public final Property<Integer> size2 = i32(UnitChangePayloads.NATIVE_SIZE2_OBJECT_OFFSET);
    //0x0A
    public final Property<Integer> size1 = i32(UnitChangePayloads.NATIVE_SIZE1_OBJECT_OFFSET);
    //0x0E
    public final Property<Integer> size1Start = u16(UnitChangePayloads.NATIVE_HEADER_OBJECT_OFFSET);
    //0x10
    public final Property<Integer> flags = i32(UnitChangePayloads.NATIVE_FLAGS_OBJECT_OFFSET);
    //0x14
    public final Property<byte[]> payload = bytes(
            UnitChangePayloads.NATIVE_PAYLOAD_OBJECT_OFFSET,
            UnitChangePayloads.NATIVE_PAYLOAD_CAPACITY
    );

    /**
     * Native: UnitChangeAction::UnitChangeAction @0050CD66.
     * Native support also extracted from
     * CServerApp::NetUpdate @00502019,
     * UnitChangeAction::WritePayload @0050CDA6, and
     * UnitChangeAction::GetType @0050CFB7.
     * Fully ported.
     */
    public UnitChangeAction() {
        super();
        size2.set(0);
        size1.set(0);
        size1Start.set(0);
        flags.set(0);
        payload.fill((byte) 0);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 packet header reset.
     * Fully ported support helper.
     */
    public static UnitChangeAction prepareForNetUpdate(Unit unit, Player targetPlayer) {
        UnitChangeAction action = global;
        UnitChangePayloads.resetForNetUpdate(action, targetPlayer.playerId, unit.idFull);
        return action;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 unit-change payload field writes.
     * Fully ported support helper.
     */
    public void appendNetUpdatePayload(Unit unit, int updateMask) {
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.HP)) {
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.HP.value, unit.m_nHP);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.MP)) {
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.MP.value, unit.m_nMP);
        }
        if (unit.isHumanoidToken() != 0 && hasUnitChangeFlag(updateMask, UnitDirtyFlags.SKILLS)) {
            Humanoid humanoid = (Humanoid) unit;
            for (int skillIndex = 1; skillIndex < 6; skillIndex++) {
                UnitChangePayloads.appendByteField(this, UnitDirtyFlags.SKILLS.value, humanoid.skillData.skillLevels[skillIndex]);
            }
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.VITALS_DERIVED)) {
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.VITALS_DERIVED.value, unit.m_nMaxHP);
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.VITALS_DERIVED.value, unit.m_nMaxMP);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.POSITION_AND_FACING)) {
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.POSITION_AND_FACING.value, unit.movementState.getFacingStep2());
            if (!unit.m_pTargetHandle.isSubPosUnknown()) {
                UnitChangePayloads.appendByteField(this, UnitDirtyFlags.POSITION_AND_FACING.value, unit.movementState.positionCellX);
                UnitChangePayloads.appendByteField(this, UnitDirtyFlags.POSITION_AND_FACING.value, unit.movementState.positionCellY);
            } else {
                UnitChangePayloads.appendByteField(this, UnitDirtyFlags.POSITION_AND_FACING.value, unit.m_pTargetHandle.getX());
                UnitChangePayloads.appendByteField(this, UnitDirtyFlags.POSITION_AND_FACING.value, unit.m_pTargetHandle.getY());
            }
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.TYPE_AND_FACE)) {
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.TYPE_AND_FACE.value, unit.getTokenTypeId());
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.TYPE_AND_FACE.value, unit.face);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.OWNER_PLAYER_ID)) {
            UnitChangePayloads.appendByteField(this,
                    UnitDirtyFlags.OWNER_PLAYER_ID.value,
                    unit.owner == null ? 1 : unit.owner.playerId
            );
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.SERVER_ID)) {
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.SERVER_ID.value, unit.serverID);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.TO_HIT)) {
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.TO_HIT.value, unit.skillData.toHit);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.DAMAGE_PROFILE)) {
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.DAMAGE_PROFILE.value, unit.skillData.damageMinimumSum());
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.DAMAGE_PROFILE.value, unit.skillData.damageModifierSum());
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.DEFENCE)) {
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.DEFENCE.value, unit.unitStatData.defence);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.ABSORBTION)) {
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.ABSORBTION.value, unit.unitStatData.absorbtion);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.SIGHT_RANGE)) {
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.SIGHT_RANGE.value, unit.packedSightRangeForNetUpdate());
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.ENCUMBRANCE_WEIGHT)) {
            UnitChangePayloads.appendWordField(this, UnitDirtyFlags.ENCUMBRANCE_WEIGHT.value, unit.m_nEncumbranceWeight);
        }
        appendProtections(unit, updateMask);
        appendOwnerStats(unit, updateMask);
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.SPEED)) {
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.SPEED.value, unit.speed);
        }
        if (unit.spellbook != null && hasUnitChangeFlag(updateMask, UnitDirtyFlags.SPELLBOOK)) {
            UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.SPELLBOOK.value, unit.spellbook.getSpellbookMask());
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.SPELLBOOK.value, unit.missionRuntimeState.spellIndex);
        }
        appendSkillBonuses(unit, updateMask);
        appendPrimaryAttributes(unit, updateMask);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 display-name payload branch.
     * Fully ported support helper.
     */
    public int appendNetUpdateDisplayName(Unit unit, int updateMask) {
        if (!hasUnitChangeFlag(updateMask, UnitDirtyFlags.DISPLAY_NAME)) {
            return updateMask;
        }
        if (unit.str.isEmpty()) {
            return updateMask & ~UnitDirtyFlags.DISPLAY_NAME.value;
        }
        UnitChangePayloads.setDisplayName(this, unit.str);
        return updateMask;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 effects flag branch.
     * Fully ported support helper.
     */
    public void markNetUpdateEffectsFlag(int updateMask) {
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.EFFECTS)) {
            UnitChangePayloads.markFlag(this, UnitDirtyFlags.EFFECTS.value);
        }
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 send branch.
     * Fully ported support helper.
     */
    public UnitChangeAction toResolvedNetUpdateAction() {
        return UnitChangePayloads.toResolvedUnitChangeAction(this);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 protections payload branch.
     * Fully ported support helper.
     */
    private void appendProtections(Unit unit, int updateMask) {
        if (!hasUnitChangeFlag(updateMask, UnitDirtyFlags.PROTECTIONS)) {
            return;
        }
        for (int protectionIndex = 1; protectionIndex < 6; protectionIndex++) {
            UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PROTECTIONS.value, unit.unitStatData.protections[protectionIndex]);
        }
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 controlled-owner-stats payload branch.
     * Fully ported support helper.
     */
    private void appendOwnerStats(Unit unit, int updateMask) {
        if (Globals.gameServer.networkSessionActive == 0 || !hasUnitChangeFlag(updateMask, UnitDirtyFlags.CONTROLLED_OWNER_STATS)) {
            return;
        }
        if (unit.owner == null || unit != unit.owner.controlledUnit) {
            return;
        }
        Player owner = unit.owner;
        UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.CONTROLLED_OWNER_STATS.value, owner.creatureKillCount);
        UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.CONTROLLED_OWNER_STATS.value, owner.playerKillCount);
        UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.CONTROLLED_OWNER_STATS.value, owner.deathCount);
        UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.CONTROLLED_OWNER_STATS.value, owner.fragCount);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 skill-bonus payload branch.
     * Fully ported support helper.
     */
    private void appendSkillBonuses(Unit unit, int updateMask) {
        if (unit.isHumanoidToken() == 0) {
            return;
        }
        Humanoid humanoid = (Humanoid) unit;
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.SKILL_BONUS_1)) {
            UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.SKILL_BONUS_1.value, humanoid.skillBonusesPermille.data[1]);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.SKILL_BONUS_2)) {
            UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.SKILL_BONUS_2.value, humanoid.skillBonusesPermille.data[2]);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.SKILL_BONUS_3)) {
            UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.SKILL_BONUS_3.value, humanoid.skillBonusesPermille.data[3]);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.SKILL_BONUS_4)) {
            UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.SKILL_BONUS_4.value, humanoid.skillBonusesPermille.data[4]);
        }
        if (hasUnitChangeFlag(updateMask, UnitDirtyFlags.SKILL_BONUS_5)) {
            UnitChangePayloads.appendDwordField(this, UnitDirtyFlags.SKILL_BONUS_5.value, humanoid.skillBonusesPermille.data[5]);
        }
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 primary-attributes payload branch.
     * Fully ported support helper.
     */
    private void appendPrimaryAttributes(Unit unit, int updateMask) {
        if (!hasUnitChangeFlag(updateMask, UnitDirtyFlags.PRIMARY_ATTRIBUTES)) {
            return;
        }
        UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PRIMARY_ATTRIBUTES.value, unit.m_nBody);
        UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PRIMARY_ATTRIBUTES.value, unit.m_nReaction);
        UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PRIMARY_ATTRIBUTES.value, unit.m_nMind);
        UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PRIMARY_ATTRIBUTES.value, unit.m_nSpirit);
        UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PRIMARY_ATTRIBUTES.value, unit.m_nBody - unit.mModifiers.body);
        UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PRIMARY_ATTRIBUTES.value, unit.m_nReaction - unit.mModifiers.reaction);
        UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PRIMARY_ATTRIBUTES.value, unit.m_nMind - unit.mModifiers.mind);
        UnitChangePayloads.appendByteField(this, UnitDirtyFlags.PRIMARY_ATTRIBUTES.value, unit.m_nSpirit - unit.mModifiers.spirit);
    }

    /**
     * Native: UnitChangeAction::UnitChangeAction(copy) @0050CD85.
     * Fully ported.
     */
    public UnitChangeAction(UnitChangeAction from) {
        super();
        ID.set(from.ID.get());
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 and
     * UnitChangeAction::WritePayload @0050CDA6 for Java concrete packet-id splitting.
     */
    protected UnitChangeAction(UnitChangeAction from, int actionId) {
        super();
        unitOrderMode.set(from.unitOrderMode.get());
        netID.set(from.netID.get());
        playerID.set(from.playerID.get());
        ID.set(actionId);
        size2.set(from.size2.get());
        size1.set(from.size1.get());
        size1Start.set(from.size1Start.get());
        flags.set(from.flags.get());
        payload.setBytesAt(0, from.payload.get(), 0, UnitChangePayloads.NATIVE_PAYLOAD_CAPACITY);
    }

    /**
     * vtbl +0x04: UnitChangeAction::Clone @00541D10.
     * Fully ported.
     */
    @Override
    public UnitChangeAction Clone() {
        return new UnitChangeAction(this);
    }

    /**
     * vtbl +0x10: UnitChangeAction::GetPayloadSize @00541D90.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return size1.get() + 3 + size2.get();
    }

    /**
     * vtbl +0x08: UnitChangeAction::WritePayload @0050CDA6.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        ID.set(UnitChangePayloads.resolveWireActionId(this));
        target.Write(this, ID_OFFSET, 1);
        target.Write(this, UnitChangePayloads.NATIVE_HEADER_OBJECT_OFFSET, size1.get() + 2);
        return target.Write(this, UnitChangePayloads.NATIVE_PAYLOAD_OBJECT_OFFSET, size2.get());
    }

    /**
     * vtbl +0x0C: UnitChangeAction::ReadPayload @0050CE4B.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        size1.set(UnitChangePayloads.resolveSize1FromActionId(ID.get()));
        flags.set(UnitDirtyFlags.NONE.value);
        source.Read(this, UnitChangePayloads.NATIVE_HEADER_OBJECT_OFFSET, size1.get() + 2);
        size2.set(UnitChangePayloads.resolvePayloadSizeFromFlags(flags.get()));
        return source.Read(this, UnitChangePayloads.NATIVE_PAYLOAD_OBJECT_OFFSET, size2.get());
    }

    /**
     * Native support extracted from the UnitChangeAction branch of MapVisualObject::HandleGameAction @0040EEA5.
     * Partial port boundary: Java maps all modeled CUnit state present in the recovered CServerApp::NetUpdate
     * payload stream and preserves native packet cursor order; native-only combat UI refreshes remain owned by the
     * existing panel update paths.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int tokenId = size1Start.get() & 0xFFFF;
        if (tokenId == 0) {
            return;
        }

        int actionFlags = flags.get();
        byte[] payloadBytes = payload.get();
        int offset = 0;

        int hp = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.HP)) {
            hp = u16(payloadBytes, offset);
            offset += Short.BYTES;
        }

        int mp = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.MP)) {
            mp = u16(payloadBytes, offset);
            offset += Short.BYTES;
        }

        int[] skillLevels = new int[5];
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SKILLS)) {
            for (int i = 0; i < skillLevels.length; i++) {
                skillLevels[i] = u8(payloadBytes, offset++);
            }
        }

        int maxHp = 0;
        int maxMp = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.VITALS_DERIVED)) {
            maxHp = u16(payloadBytes, offset);
            offset += Short.BYTES;
            maxMp = u16(payloadBytes, offset);
            offset += Short.BYTES;
        }

        int facing = 0;
        int tileX = 0;
        int tileY = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.POSITION_AND_FACING)) {
            facing = u8(payloadBytes, offset++);
            tileX = u8(payloadBytes, offset++);
            tileY = u8(payloadBytes, offset++);
        }

        int typeId = 0;
        int faceId = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.TYPE_AND_FACE)) {
            typeId = u8(payloadBytes, offset++);
            faceId = u8(payloadBytes, offset++);
        }

        int ownerPlayerId = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.OWNER_PLAYER_ID)) {
            ownerPlayerId = u8(payloadBytes, offset++);
        }

        int serverId = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SERVER_ID)) {
            serverId = u16(payloadBytes, offset);
            offset += Short.BYTES;
        }

        int toHit = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.TO_HIT)) {
            toHit = u16(payloadBytes, offset);
            offset += Short.BYTES;
        }

        int damageMin = 0;
        int damageSpread = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.DAMAGE_PROFILE)) {
            damageMin = u8(payloadBytes, offset++);
            damageSpread = u8(payloadBytes, offset++);
        }

        int defence = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.DEFENCE)) {
            defence = u16(payloadBytes, offset);
            offset += Short.BYTES;
        }

        int absorption = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.ABSORBTION)) {
            absorption = u8(payloadBytes, offset++);
        }

        int packedSightRange = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SIGHT_RANGE)) {
            packedSightRange = u16(payloadBytes, offset);
            offset += Short.BYTES;
        }

        int encumbranceWeight = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.ENCUMBRANCE_WEIGHT)) {
            encumbranceWeight = u16(payloadBytes, offset);
            offset += Short.BYTES;
        }

        int[] protections = new int[5];
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.PROTECTIONS)) {
            for (int i = 0; i < protections.length; i++) {
                protections[i] = u8(payloadBytes, offset++);
            }
        }

        int monstersKilled = 0;
        int playersKilled = 0;
        int deathCount = 0;
        int fragCount = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.CONTROLLED_OWNER_STATS)) {
            monstersKilled = i32(payloadBytes, offset);
            offset += Integer.BYTES;
            playersKilled = i32(payloadBytes, offset);
            offset += Integer.BYTES;
            deathCount = i32(payloadBytes, offset);
            offset += Integer.BYTES;
            fragCount = i32(payloadBytes, offset);
            offset += Integer.BYTES;
        }

        int speed = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SPEED)) {
            speed = u8(payloadBytes, offset++);
        }

        int spellbookMask = 0;
        int autoCastSpellId = 0;
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SPELLBOOK)) {
            spellbookMask = i32(payloadBytes, offset);
            offset += Integer.BYTES;
            autoCastSpellId = u8(payloadBytes, offset++);
        }

        int[] skillBonuses = new int[5];
        boolean[] skillBonusUpdated = new boolean[5];
        UnitDirtyFlags[] skillBonusFlags = {
                UnitDirtyFlags.SKILL_BONUS_1,
                UnitDirtyFlags.SKILL_BONUS_2,
                UnitDirtyFlags.SKILL_BONUS_3,
                UnitDirtyFlags.SKILL_BONUS_4,
                UnitDirtyFlags.SKILL_BONUS_5
        };
        for (int i = 0; i < skillBonusFlags.length; i++) {
            if (hasUnitChangeFlag(actionFlags, skillBonusFlags[i])) {
                skillBonuses[i] = i32(payloadBytes, offset);
                offset += Integer.BYTES;
                skillBonusUpdated[i] = true;
            }
        }

        int[] primaryAttributes = new int[8];
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.PRIMARY_ATTRIBUTES)) {
            for (int i = 0; i < primaryAttributes.length; i++) {
                primaryAttributes[i] = u8(payloadBytes, offset++);
            }
        }

        int displayNameOffset = offset;

        CPlayer owner = mapVisualObject.findClientPlayerById(ownerPlayerId);
        if (owner == null) {
            return;
        }

        CGameObject existingObject = mapVisualObject.getObjectByToken((short) tokenId);
        CUnit unit = existingObject == null ? null : (CUnit) existingObject;
        boolean createdUnitWithoutRuntimeTemplate = false;
        if (unit == null) {
            if ((actionFlags & (UnitDirtyFlags.TYPE_AND_FACE.value | UnitDirtyFlags.SERVER_ID.value)) == 0) {
                return;
            }
            Unit runtimeUnit = createUnitChangeRuntimeUnit(actionFlags, serverId, typeId);
            createdUnitWithoutRuntimeTemplate = runtimeUnit == null;
            int visualTypeId = runtimeUnit == null ? typeId : runtimeUnit.getTokenTypeId();
            unit = MapVisualObject.createVisualUnitForType(visualTypeId);
            unit.m_id = tokenId;
            unit.pMapVisualObject = mapVisualObject;
            if (runtimeUnit != null) {
                unit.copyFromRuntimeUnit(runtimeUnit);
            }
            mapVisualObject.putScenarioObject((short) tokenId, unit);
        }

        if (createdUnitWithoutRuntimeTemplate && hasUnitChangeFlag(actionFlags, UnitDirtyFlags.TYPE_AND_FACE)) {
            applyUnitChangeTypeAndFace(unit, typeId, faceId);
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.OWNER_PLAYER_ID)) {
            unit.cPlayer = owner;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.HP)) {
            short newHp = (short) hp;
            mapVisualObject.addFloatingUnitTextForHpLoss(unit, newHp);
            unit.HP = newHp;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.MP)) {
            unit.MP = (short) mp;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SKILLS)) {
            for (int i = 0; i < skillLevels.length; i++) {
                unit.sphereLevels[i] = (byte) skillLevels[i];
            }
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.VITALS_DERIVED)) {
            unit.MaxHP = (short) maxHp;
            unit.MaxMP = (short) maxMp;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.POSITION_AND_FACING)) {
            unit.dir = facing;
            unit.location.x = MapVisualObject.pixelCenterFromTile(tileX);
            unit.location.y = MapVisualObject.pixelCenterFromTile(tileY);
            unit.location2.x = unit.location.x;
            unit.location2.y = unit.location.y;
            unit.z = 0;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SERVER_ID)) {
            unit.serverID = (short) serverId;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.TO_HIT)) {
            unit.attackSkill = (short) toHit;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.DAMAGE_PROFILE)) {
            unit.physicalDamageMin = (byte) damageMin;
            unit.physicalDamageSpread = (byte) damageSpread;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.DEFENCE)) {
            unit.defence = (short) defence;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.ABSORBTION)) {
            unit.absorption = (byte) absorption;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SIGHT_RANGE)) {
            unit.packedSightRange = (short) packedSightRange;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.ENCUMBRANCE_WEIGHT)) {
            unit.copiedEncumbranceWeight = (short) encumbranceWeight;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.PROTECTIONS)) {
            for (int i = 0; i < protections.length; i++) {
                unit.protectionLevels[i] = (byte) protections[i];
            }
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.CONTROLLED_OWNER_STATS)) {
            applyUnitChangeOwnerStats(monstersKilled, playersKilled, deathCount, fragCount);
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SPEED)) {
            unit.speed = (short) speed;
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SPELLBOOK)) {
            unit.spellbookMask = spellbookMask;
            unit.autoCastSpellId = (byte) autoCastSpellId;
            unit.availableSpellMask = availableSpellMaskFromUnitChangeSpellbook(unit, mapVisualObject);
        }
        for (int i = 0; i < skillBonusUpdated.length; i++) {
            if (skillBonusUpdated[i]) {
                unit.copiedSkillBonusesPermille[i] = skillBonuses[i];
            }
        }
        recalculateUnitChangeExperience(unit);
        clearUnitChangeHighTokenPositionFlag(unit, actionFlags);
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.PRIMARY_ATTRIBUTES)) {
            unit.body = (byte) primaryAttributes[0];
            unit.reaction = (byte) primaryAttributes[1];
            unit.mind = (byte) primaryAttributes[2];
            unit.spirit = (byte) primaryAttributes[3];
            unit.copiedBody = (byte) primaryAttributes[4];
            unit.copiedReaction = (byte) primaryAttributes[5];
            unit.copiedMind = (byte) primaryAttributes[6];
            unit.copiedSpirit = (byte) primaryAttributes[7];
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.DISPLAY_NAME)) {
            applyUnitChangeDisplayName(unit, payloadBytes, displayNameOffset);
        }
        if (shouldMarkPlayerUnit(mapVisualObject, unit)) {
            mapVisualObject.markPlayerUnit(unit);
        }
        if (unit.name.isEmpty() && Short.toUnsignedInt(unit.serverID) != 0) {
            applyUnitChangeNpcName(unit);
        }
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.EFFECTS)) {
            unit.dwarr_130.clear();
        }

        int previousDeathState = Byte.toUnsignedInt(unit.field51_0x184);
        updateUnitChangeDeathState(unit);
        clearUnitChangeScenarioVarForDeadMissionUnit(unit);
        refreshSavedCharacterProgressAfterUnitChange(unit, actionFlags);
        applyUnitChangeDeathVisualTransition(mapVisualObject, unit, previousDeathState);
        if (unit.field51_0x184 < 5) {
            if (shouldRefreshMapDerivedStateAfterUnitChange(mapVisualObject)) {
                unit.refreshMapDerivedState();
            }
            if (shouldRefreshSelectionStateAfterUnitChange(unit, actionFlags)) {
                mapVisualObject.updateSelectionState();
            }
            unit.m_bSelectionDirty = 1;
        }
        mapVisualObject.markMapOccupancyDirty();
        mapVisualObject.renderFrameDirty = 1;
    }

    /**
     * Native support extracted from UnitChangeAction flag checks in MapVisualObject::HandleGameAction @0040EEA5.
     */
    private static boolean hasUnitChangeFlag(int actionFlags, UnitDirtyFlags flag) {
        return (actionFlags & flag.value) != 0;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040F6FD and CUnit::ApplyScenarioFace @0046A0CD.
     */
    private static void applyUnitChangeTypeAndFace(CUnit unit, int typeId, int faceId) {
        unit.type = typeId;
        unit.applyScenarioFace(faceId);
        unit.refreshUnitSpritesAfterRuntimeCopy();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00410363.
     */
    private static int availableSpellMaskFromUnitChangeSpellbook(CUnit unit, MapVisualObject mapVisualObject) {
        int availableSpellMask = 0;
        if (unit.cPlayer == mapVisualObject.currentPlayer && unit.spellbookMask != 0) {
            for (int spellId = 0; spellId < Integer.SIZE; spellId++) {
                if ((unit.spellbookMask & (1 << (spellId & 0x1F))) != 0) {
                    availableSpellMask |= Spellbook.availableSpellMaskBitForSpellId(spellId);
                }
            }
        }
        return availableSpellMask;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041005B.
     */
    private static void recalculateUnitChangeExperience(CUnit unit) {
        unit.experience = 0;
        for (int bonusPermille : unit.copiedSkillBonusesPermille) {
            unit.experience += bonusPermille;
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00410047.
     */
    private static void clearUnitChangeHighTokenPositionFlag(CUnit unit, int actionFlags) {
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.POSITION_AND_FACING) && (unit.m_id & 0xFFFF) > 0x5FFF) {
            unit.unitFlags &= ~0x80;
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040FBAF.
     */
    private static void applyUnitChangeOwnerStats(int monstersKilled, int playersKilled, int deathCount, int fragCount) {
        CGameSession gameSession = Globals.mainWindow.m_GameSession;
        gameSession.monstersKilled = monstersKilled;
        gameSession.playersKilled = playersKilled;
        gameSession.deathCount = deathCount;
        gameSession.fragCount = fragCount;
    }

    /**
     * Native support extracted from the UnitChangeAction server-id creation path in
     * MapVisualObject::HandleGameAction @0040F35B.
     */
    private static Unit createUnitChangeRuntimeUnit(int actionFlags, int serverId, int typeId) {
        if (hasUnitChangeFlag(actionFlags, UnitDirtyFlags.TYPE_AND_FACE)) {
            return null;
        }
        if (!hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SERVER_ID)) {
            return null;
        }

        int humanIndex = Globals.staticDataMgr.findHumanByServerId(serverId);
        if (humanIndex != 0) {
            HumanInfo humanInfo = Globals.staticDataMgr.humans.get(humanIndex);
            Human human = Human.createFromTemplate(humanInfo.name, false, false);
            human.serverID = serverId;
            return human;
        }

        int unitIndex = Globals.staticDataMgr.findUnitByServerId(serverId);
        if (unitIndex != 0) {
            UnitInfo unitInfo = Globals.staticDataMgr.units.get(unitIndex);
            Unit unit = new Unit();
            unit.key = (unitIndex) & 0xFFFF;
            unit.applyUnitInfoValues(unitInfo);
            unit.serverID = serverId;
            return unit;
        }
        return null;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004100F8.
     */
    private static void applyUnitChangeDisplayName(CUnit unit, byte[] payloadBytes, int offset) {
        String displayName = readFixedUnitChangeString(payloadBytes, offset, 0x18);
        int split = displayName.indexOf('|');
        if (split >= 0) {
            unit.name = clampUnitChangeName(displayName.substring(0, split));
            unit.clan = clampUnitChangeName(displayName.substring(split + 1));
            return;
        }
        unit.name = clampUnitChangeName(displayName);
        unit.clan = "";
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041028A.
     */
    private static void applyUnitChangeNpcName(CUnit unit) {
        unit.name = get(NPCNAMES, NpcNamesText.byIndex(Short.toUnsignedInt(unit.serverID) - 1));
    }

    /**
     * Native support extracted from the fixed C-string copy in MapVisualObject::HandleGameAction @004100F8.
     */
    private static String readFixedUnitChangeString(byte[] payloadBytes, int offset, int length) {
        int end = offset;
        int limit = offset + length;
        while (end < limit && payloadBytes[end] != 0) {
            end++;
        }
        return new String(payloadBytes, offset, end - offset, StandardCharsets.ISO_8859_1);
    }

    /**
     * Native support extracted from the 12-byte CGameObject name fields written at MapVisualObject::HandleGameAction @004100F8.
     */
    private static String clampUnitChangeName(String value) {
        return value.length() > 0x0B ? value.substring(0, 0x0B) : value;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004101B5 / @0041020F.
     */
    private static boolean shouldMarkPlayerUnit(MapVisualObject mapVisualObject, CUnit unit) {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN
                && Short.toUnsignedInt(unit.serverID) == 0x15) {
            return true;
        }
        return unit.cPlayer == mapVisualObject.currentPlayer && (unit.unitFlags & UNIT_FLAG_HUMANOID) != 0;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00410515 saved-character progress refresh.
     */
    private static void refreshSavedCharacterProgressAfterUnitChange(CUnit unit, int actionFlags) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN
                || (unit.unitFlags & UNIT_FLAG_SAVED_CHARACTER_PROGRESS_SOURCE) == 0) {
            return;
        }
        CGameSession gameSession = mainWindow.m_GameSession;
        if ((actionFlags & IMMEDIATE_SAVED_CHARACTER_PROGRESS_MASK) != 0) {
            gameSession.refreshSavedCharacterProgress();
        } else if ((actionFlags & THROTTLED_SAVED_CHARACTER_PROGRESS_MASK) != 0) {
            gameSession.refreshSavedCharacterProgressIfDue();
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004109D2 / @00410A02.
     */
    private static boolean shouldRefreshMapDerivedStateAfterUnitChange(MapVisualObject mapVisualObject) {
        return GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask) && mapVisualObject.mapDescriptor != null;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00410A61.
     */
    private static boolean shouldRefreshSelectionStateAfterUnitChange(CUnit unit, int actionFlags) {
        return unit.isSelected()
                && ((unit.unitFlags & UNIT_FLAG_REFRESH_SELECTION_STATE) != 0
                || hasUnitChangeFlag(actionFlags, UnitDirtyFlags.SPELLBOOK));
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004103F4.
     */
    private static void updateUnitChangeDeathState(CUnit unit) {
        final int hp = unit.HP;
        unit.field51_0x184 = (byte) switch (hp) {
            case int i when i < -600 -> 5;
            case int i when i < -40 -> 4;
            case int i when i < -20 -> 3;
            case int i when i < -10 -> 2;
            case int i when i <= 0 -> 1;
            default -> 0;
        };
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004105A5-004107B6 and @004107C0-0041095A.
     */
    private static void applyUnitChangeDeathVisualTransition(
            MapVisualObject mapVisualObject,
            CUnit unit,
            int previousDeathState
    ) {
        int deathState = Byte.toUnsignedInt(unit.field51_0x184);
        switch (deathState) {
            case 0 -> {
                if (previousDeathState != 0) {
                    unit.action = 0;
                    if ((unit.unitFlags & UNIT_FLAG_HUMANOID) != 0) {
                        unit.refreshUnitSpritesAfterRuntimeCopy();
                    }
                }
            }
            case 1 -> applyUnitChangeDyingAction(unit, previousDeathState);
            case 2, 3, 4 -> clearUnitChangeDeadVisualState(mapVisualObject, unit);
            case 5 -> {
                clearUnitChangeDeadVisualState(mapVisualObject, unit);
                if ((unit.m_id & 0xFFFF) < LOW_RUNTIME_OBJECT_TOKEN_LIMIT) {
                    mapVisualObject.removeObjectByToken((short) unit.m_id);
                }
            }
            default -> {
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004105E4-004107B6 death-state `1` branch.
     */
    private static void applyUnitChangeDyingAction(CUnit unit, int previousDeathState) {
        if (previousDeathState == 0) {
            unit.actionSegments = resolveUnitChangeDyingPhases(unit) << 1;
            setUnitChangeDyingActionState(unit, 0);
            unit.playHurtResponseSound(3);
            unit.refreshUnitSpritesAfterRuntimeCopy();
        } else if (previousDeathState == 1) {
            unit.actionSegments = 4;
            setUnitChangeDyingActionState(unit, resolveUnitChangeDyingPhases(unit) * 2 - 4);
            unit.playHurtResponseSound(2);
            unit.refreshUnitSpritesAfterRuntimeCopy();
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00410632-004106A8 and @004106F5-00410797.
     */
    private static void setUnitChangeDyingActionState(CUnit unit, int actionPhase) {
        unit.action = UNIT_ACTION_DYING;
        unit.actionDir = (byte) unit.dir;
        unit.actionPhase = actionPhase;
        unit.field40_0xa4 = 0;
        unit.field39_0xa0 = 0;
        unit.actionX = unit.location.x;
        unit.actionY = unit.location.y;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00410601-0041062C and @00410717-0041074D.
     */
    private static int resolveUnitChangeDyingPhases(CUnit unit) {
        CUnitInfo unitInfo = UnitTypes.getUnitInfo(unit.type);
        return UnitTypes.getUnitInfo(unitInfo.m_bDying).m_DyingPhases;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041089C-0041095A and @00410900-0041095A.
     */
    private static void clearUnitChangeDeadVisualState(MapVisualObject mapVisualObject, CUnit unit) {
        unit.controlGroupMask = 0;
        unit.dwarr_130.clear();
        unit.transientVisualElements.clear();
        if (unit.isSelected()) {
            unit.setSelected(false);
            mapVisualObject.updateSelectionState();
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00410442.
     */
    private static void clearUnitChangeScenarioVarForDeadMissionUnit(CUnit unit) {
        int serverId = Short.toUnsignedInt(unit.serverID);
        if (serverId != 0 && serverId < 0x15 && unit.field51_0x184 > 1) {
            Globals.scenarioLib.setVar(serverId + 0x213, 0);
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004135BA / @004119FC.
     */
    private static int u8(byte[] data, int offset) {
        return Byte.toUnsignedInt(data[offset]);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004135BA / @004119FC.
     */
    private static int u16(byte[] data, int offset) {
        return Short.toUnsignedInt(ByteBuffer.wrap(data, offset, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort());
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040FC7B / @0040FD47 / @0040FDB0.
     */
    private static int i32(byte[] data, int offset) {
        return ByteBuffer.wrap(data, offset, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
