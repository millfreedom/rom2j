package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.text.HeroPictureText;

import java.io.IOException;
import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.HEROPICTURE;

/**
 * Native Token is laid out as vtbl + Token_Base; field comments use Token object offsets.
 */
public class Token implements MfcSerializable {
    private static final int TYPE_SPELL = 0x0E;

    //0x04; native Token_Base.id. Kept distinct from child `id` fields such as Effect.id and Building.id.
    public int idFull;
    //0x08
    public int scenarioObjectId;
    //0x0c
    public int key;
    //0x0e
    public int typeID;
    //0x10
    public final TargetHandle m_pTargetHandle = new TargetHandle();
    //0x14
    public Player owner;
    //0x18
    public int word;
    //0x1c
    public int price;
    //0x20
    public final CustomList<Effect> effects = new CustomList<>(Effect.class);

    /**
     * Native: Token::Token @0050D493.
     * Fully ported.
     */
    public Token() {
        owner = null;
        m_pTargetHandle.initFromPackedCellWord(0, Globals.worldMap);
        initializeNativeDefaults();
    }

    /**
     * Native: Token::initFromTargetHandle @0050D549.
     * Fully ported.
     */
    protected Token(TargetHandle targetHandle) {
        owner = null;
        m_pTargetHandle.assignFrom(targetHandle);
        initializeNativeDefaults();
    }

    /**
     * Native: Token::constructFromTargetHandleAndOwner @0050D5FC.
     * Fully ported.
     */
    protected Token(TargetHandle targetHandle, Player owner) {
        this.owner = owner;
        m_pTargetHandle.assignFrom(targetHandle);
        initializeNativeDefaults();
    }

    /**
     * Native: Token::initializeNativeDefaults @0050D6AE.
     * Fully ported.
     */
    private void initializeNativeDefaults() {
        word = 0;
        price = 0;
        idFull = 0;
    }

    /**
     * Native: Token::operator>>(CArchive*, Token**) @0050D477.
     * Fully ported.
     */
    public static Token readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(Token.class);
    }

    /**
     * vtbl +0x08: Token::serialize @0052CF8D.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        // CObject::Serialize(this, ar) is a no-op in this port.
        ar.serialize(m_pTargetHandle); // TargetHandle::Serialize

        if (!ar.isStoring()) {
            idFull = ar.readInt();
            if (idFull != 0) {
                Globals.gameServer.setBitForId(idFull);
            }

            key = ar.readUShort();
            typeID = ar.readUShort();

            scenarioObjectId = ar.readInt();
            word = ar.readUShort();
            word = 0; // native zeroes it right after reading
            price = ar.readInt();

            int selfPtrToken = ar.readInt();
            Globals.gameServer.setPointerMapEntry(selfPtrToken, this);

            owner = (Player) Globals.gameServer.lookupPointerMapOrNull(ar.readInt());
        } else {
            ar.writeInt(idFull);
            ar.writeShort(key);
            ar.writeShort(typeID);
            ar.writeInt(scenarioObjectId);
            ar.writeShort(word);
            ar.writeInt(price);
            ar.writeInt(System.identityHashCode(this));
            ar.writeInt(Utils.encodePointerLike(owner));
        }
    }

    /**
     * Native support extracted from CList<Effect>::Serialize @00546410.
     * Fully ported.
     */
    protected void serializeEffectsList(CArchive ar) throws IOException {
        ar.serialize(effects);
    }

    /**
     * Native: Token::getEffect @005106F4.
     * Fully ported.
     */
    public Effect getEffect(int effectKey) {
        for (Effect effect : effects) {
            if ((effect.key & 0xFFFF) == (effectKey & 0xFFFF)) {
                return effect;
            }
        }
        return null;
    }

    /**
     * Native: Token::getTokenTypeId @005407B0.
     * Fully ported.
     */
    public int getTokenTypeId() {
        return (short) typeID;
    }

    /**
     * Native: Token::GetType @0041E9E0.
     * Fully ported.
     */
    public int getType() {
        return (packedIdentityWord() >>> 8) & 0x0F;
    }

    /**
     * Native: Token::GetMaterial @00439E50.
     * Fully ported.
     */
    public int getMaterial() {
        return packedIdentityWord() >>> 12;
    }

    /**
     * Native: Token::GetShape @00439E70.
     * Fully ported.
     */
    public int getShape() {
        return (packedIdentityWord() >>> 5) & 0x07;
    }

    /**
     * Native: Token::GetId @00439E90.
     * Fully ported.
     */
    public int getId() {
        return packedIdentityWord() & 0x1F;
    }

    /**
     * Native support extracted from Token::GetType @0041E9E0, Token::GetMaterial @00439E50,
     * Token::GetShape @00439E70, and Token::GetId @00439E90.
     */
    private int packedIdentityWord() {
        return (idFull >>> 16) & 0xFFFF;
    }

    /**
     * Native: Token::GetEquipmentPortraitResourceName @00438BA1.
     * Fully ported.
     */
    public String getEquipmentPortraitResourceName() {
        if (getType() == TYPE_SPELL) {
            return String.format(Locale.ROOT, "%02d%02d%03d", getMaterial(), getType(), (getShape() << 5) | getId());
        }
        return String.format(Locale.ROOT, "%02d%02d%d%02d", getMaterial(), getType(), getShape(), getId());
    }

    /**
     * Native: Token::IsTwoHandedHeroPictureToken @00438DA7.
     * Fully ported.
     */
    public boolean isTwoHandedHeroPictureToken() {
        String pictureName = get(HEROPICTURE, HeroPictureText.byIndex(getId() - 1));
        return switch (pictureName) {
            case "bowman", "archer", "xbowman", "axeman2h", "swordsman2h", "mage_st" -> true;
            default -> false;
        };
    }

    /**
     * Native: Token::hasInitialSyncSentToPlayer @0050D8FC.
     * Fully ported.
     */
    public boolean hasInitialSyncSentToPlayer(Player player) {
        return (word & player.scanMask) != 0;
    }

    /**
     * Native: Token::needsInitialSyncForPlayer @0050D91F.
     * Fully ported.
     */
    public boolean needsInitialSyncForPlayer(Player player) {
        return (word & player.scanMask) == 0;
    }

    /**
     * Native: Token::markInitialSyncSentToPlayer @0050D947.
     * Fully ported.
     */
    public void markInitialSyncSentToPlayer(Player player) {
        word |= player.scanMask;
    }

    /**
     * Native: Token::hasScenarioObjectFlag1 @00542D80.
     * Fully ported.
     */
    public int hasScenarioObjectFlag1() {
        return scenarioObjectId & 1;
    }

    /**
     * Native: Token::hasScenarioObjectFlag2 @00542DA0.
     * Fully ported.
     */
    public int hasScenarioObjectFlag2() {
        return scenarioObjectId & 2;
    }

    /**
     * Native support extracted from Token::Token(copy) @0050D772.
     * Fully ported support helper.
     */
    protected void copyTokenStateFrom(Token source) {
        key = source.key;
        typeID = source.typeID;
        scenarioObjectId = source.scenarioObjectId;
        m_pTargetHandle.assignFrom(source.m_pTargetHandle);
        owner = source.owner;
        word = source.word;
        price = source.price;
        effects.clear();
    }

    /**
     * not ported.
     */
    @Override
    public boolean isDirect() {
        // Token-derived classes are typically serialized in lists via CArchive ReadObject/WriteObject.
        return true;
    }

    // ---- Token vtable API (slots +0x14 .. +0x34) ----

    /**
     * vtbl +0x14: Token::updateRegen @00541FD0.
     * Fully ported.
     */
    public void updateRegen() {
        // no-op in base Token
    }

    /**
     * vtbl +0x18: Token::update @0050D879.
     * Fully ported no-op; native leaves the return register unchanged and callers ignore the slot result.
     */
    public Object update() {
        return null;
    }

    /**
     * vtbl +0x1C: Token::getTokenSizeVirtual @00541FE0.
     * Fully ported.
     */
    public int getTokenSizeVirtual() {
        return 1;
    }

    /**
     * Native: Token::getCenterXdX @0050D884.
     * Fully ported.
     */
    public int getCenterXdX() {
        int xdx = m_pTargetHandle.packXdX() & 0xFFFF;
        return (short) (xdx + ((getTokenSizeVirtual() - 1) * 0x80));
    }

    /**
     * Native: Token::getCenterYdY @0050D8C0.
     * Fully ported.
     */
    public int getCenterYdY() {
        int ydy = m_pTargetHandle.packYdY() & 0xFFFF;
        return (short) (ydy + ((getTokenSizeVirtual() - 1) * 0x80));
    }

    /**
     * vtbl +0x20: Token::getMovementType @00541FF0.
     * Base Token returns 0; Unit overrides return the serialized movement-type byte.
     * Fully ported.
     */
    public int getMovementType() {
        return 0;
    }

    /**
     * vtbl +0x24: Token::restoreContext @0052D105.
     * Fully ported.
     */
    public void restoreContext() {
        m_pTargetHandle.restoreContext();
    }

    /**
     * vtbl +0x28: Token::isItemToken @00542000.
     * Fully ported.
     */
    public int isItemToken() {
        return 0;
    }

    /**
     * vtbl +0x2C: Token::isUnitToken @00542010.
     * Fully ported.
     */
    public int isUnitToken() {
        return 0;
    }

    /**
     * vtbl +0x30: Token::isHumanoidToken @00542020.
     * Base token marker returns 0 for non-humanoid tableline resolution.
     * Fully ported.
     */
    public int isHumanoidToken() {
        return 0;
    }

    /**
     * vtbl +0x34: Token::isBuildingToken @00542030.
     * Used by Effect_DirectDamage::applyToTarget @0051E65B and
     * CServerApp::notifyStateChanged @00503672 to select the building-health token path.
     * Fully ported.
     */
    public int isBuildingToken() {
        return 0;
    }

}
