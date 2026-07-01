package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.action.ItemListAction;
import ua.millfreedom.rom2.model.enums.ShapeId;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.text.SpellText;
import ua.millfreedom.rom2.text.StatsText;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_CASTS_92;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_DAMAGE_118;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_DURATION_124;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MAGIC_189;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MAXIMUM_DAMAGE_PROBABILITY_185;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MINIMUM_DAMAGE_PROBABILITY_187;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_OF_90;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_RANGE_123;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_RAYS_186;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_RESISTANCE_183;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SIGHT_184;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SPEED_182;
import static ua.millfreedom.rom2.text.TextTableId.SPELL;
import static ua.millfreedom.rom2.text.TextTableId.STATS;

public class TokenEntry implements MfcSerializable {
    public static final int NATIVE_SIZE = 0x24;
    public static final int MONEY_ENTRY_HASH = 0xFFFF;
    public static final int TYPE_SPELL = 0x0E;
    public static final int FLAG_STACKABLE = 0x01;
    public static final int FLAG_EQUIPPABLE_BY_NON_MAGIC_UNIT = 0x02;
    public static final int FLAG_EQUIPPABLE_BY_MAGIC_UNIT = 0x04;
    public static final int FLAG_SELECTION_SPELL_ORDER = 0x10;
    public static final int CANCELABLE_SPELL_FLAG = 0x10;
    public static final int CAST_SPELL_ATTRIBUTE_ID = 0x29;
    public static final int SPELL_LEVEL_ATTRIBUTE_ID = 0x32;

    private static final int INLINE_QUANTITY_FLAG = 0x80;
    private static final int CHANGED_PAYLOAD_FLAG = 0x40;
    private static final int DYNAMIC_PAYLOAD_FLAG = 0x20;
    private static final int INLINE_QUANTITY_MASK = 0x3F;
    private static final int DYNAMIC_ENTRY_COUNT_MASK = 0x0F;
    private static final int EFFECT_PRICE_OPCODE = 0x33;

    //0x04
    public int entryId;

    //0x06
    public int packedTokenHash;

    //0x08
    public int wireFlags;

    //0x09
    public int payloadEntryCount;

    //0x0A
    public int payloadSize;

    //0x0C
    public byte[] payloadBytes;

    //0x10
    public int quantity;

    //0x14
    public int categoryIndex;

    //0x18
    public int gridModeCode;

    //0x1C
    public int sourceIndex;

    //0x20
    public int sourceSlotDescriptor;

    /**
     * Native: TokenEntry::TokenEntry @00438640.
     * Fully ported.
     */
    public TokenEntry() {
        initializeDefaults(0, 0);
    }

    /**
     * Native: TokenEntry::TokenEntryWithEntryId @004386B9.
     * Fully ported.
     */
    public TokenEntry(int entryId) {
        initializeDefaults(entryId, 0);
    }

    /**
     * Native: TokenEntry::TokenEntryWithCategory @00438736.
     * Fully ported.
     */
    public TokenEntry(int entryId, int categoryIndex) {
        initializeDefaults(entryId, categoryIndex);
    }

    /**
     * Native: TokenEntry::TokenEntryFromItemPayload @004387B2.
     * Fully ported.
     */
    public TokenEntry(ByteBuffer payloadCursor, boolean includeGridModeTag) {
        initializeDefaults(0, 0);
        readItemPayload(payloadCursor, includeGridModeTag);
    }

    /**
     * Native: TokenEntry::TokenEntryCopy @00438B13.
     * Fully ported.
     */
    public TokenEntry(TokenEntry source) {
        initializeDefaults(0, 0);
        copyFrom(source);
    }

    /**
     * Native support extracted from TokenEntry constructors @00438640, @004386B9, @00438736, and @004387B2.
     */
    private void initializeDefaults(int entryId, int categoryIndex) {
        this.entryId = entryId & 0xFFFF;
        this.packedTokenHash = 0;
        this.wireFlags = 0;
        this.payloadEntryCount = 0;
        this.payloadSize = 0;
        this.payloadBytes = null;
        this.quantity = 1;
        this.categoryIndex = categoryIndex;
        this.gridModeCode = 0;
        this.sourceIndex = -1;
        this.sourceSlotDescriptor = -1;
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
    }

    /**
     * Native: TokenEntry::writeRawPayloadArchive @00439B32.
     * Fully ported.
     */
    public void writeRawPayloadArchive(CArchive ar) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
        header.putShort((short) packedTokenHash);
        header.put((byte) wireFlags);
        header.put((byte) payloadEntryCount);
        header.put((byte) payloadSize);
        ar.writeBytes(header.array());
        ar.writeBytes(payloadSize == 0 ? new byte[0] : Arrays.copyOf(payloadBytes, payloadSize));
    }

    /**
     * Native: TokenEntry::readRawPayloadArchive @00439B6E.
     * Fully ported.
     */
    public void readRawPayloadArchive(CArchive ar) throws IOException {
        if (payloadSize != 0) {
            payloadBytes = null;
        }
        ByteBuffer header = ByteBuffer.wrap(ar.readBytes(5)).order(ByteOrder.LITTLE_ENDIAN);
        packedTokenHash = Short.toUnsignedInt(header.getShort());
        wireFlags = Byte.toUnsignedInt(header.get());
        payloadEntryCount = Byte.toUnsignedInt(header.get());
        payloadSize = Byte.toUnsignedInt(header.get());
        if (payloadSize != 0) {
            payloadBytes = ar.readBytes(payloadSize);
        }
    }

    /**
     * Native: TokenEntry::allocateRawPayloadBuffer @00439BE8.
     * Fully ported.
     */
    public void allocateRawPayloadBuffer(int packedTokenHash, int payloadSize) {
        this.packedTokenHash = packedTokenHash & 0xFFFF;
        this.payloadSize = payloadSize & 0xFF;
        payloadBytes = new byte[this.payloadSize];
    }

    /**
     * Native: TokenEntry::appendTwoBytePayloadAttribute @00439C20.
     * Fully ported.
     */
    public void appendTwoBytePayloadAttribute(int attributeId, int value) {
        int payloadOffset = payloadEntryCount * 2;
        payloadBytes[payloadOffset] = (byte) attributeId;
        payloadBytes[payloadOffset + 1] = (byte) value;
        payloadEntryCount = (payloadEntryCount + 1) & 0xFF;
    }

    /**
     * Native: TokenEntry::copyFrom @00439A5B.
     * Fully ported.
     */
    public TokenEntry copyFrom(TokenEntry source) {
        entryId = source.entryId & 0xFFFF;
        packedTokenHash = source.packedTokenHash & 0xFFFF;
        wireFlags = source.wireFlags & 0xFF;
        payloadEntryCount = source.payloadEntryCount & 0xFF;
        payloadSize = source.payloadSize & 0xFF;
        payloadBytes = payloadSize == 0 ? null : Arrays.copyOf(source.payloadBytes, payloadSize);
        quantity = source.quantity;
        categoryIndex = source.categoryIndex;
        gridModeCode = source.gridModeCode;
        sourceIndex = source.sourceIndex;
        sourceSlotDescriptor = source.sourceSlotDescriptor;
        return this;
    }

    /**
     * Native support extracted from Item::appendNetworkItemPayload @005241BF and TokenEntry::TokenEntryFromItemPayload @004387B2.
     */
    public static TokenEntry fromEquipmentItem(Item item) {
        if (item == null) {
            return null;
        }

        ItemListAction action = new ItemListAction();
        item.appendNetworkItemPayload(action, false);
        TokenEntry entry = new TokenEntry(
                ByteBuffer.wrap(action.trailingData.get()).order(ByteOrder.LITTLE_ENDIAN),
                false
        );
        if (entry.packedTokenHash == 0) {
            return null;
        }
        entry.gridModeCode = 1;
        return entry;
    }

    /**
     * Native: TokenEntry::TokenEntryFromItemPayload @004387B2.
     * Fully ported.
     */
    public final void readItemPayload(ByteBuffer payloadCursor, boolean includeGridModeTag) {
        payloadCursor.order(ByteOrder.LITTLE_ENDIAN);
        packedTokenHash = Short.toUnsignedInt(payloadCursor.getShort());
        wireFlags = Byte.toUnsignedInt(payloadCursor.get());

        ItemPayloadTemplate payloadTemplate = null;
        if (packedTokenHash < 0xFFF0) {
            payloadTemplate = Globals.itemNames.lookupStaticPayloadTemplate(packedTokenHash);
            if (payloadTemplate == null) {
                return;
            }
            payloadSize = payloadTemplate.payloadBytes.length;
            payloadEntryCount = payloadTemplate.payloadEntryCount;
        }

        if ((wireFlags & INLINE_QUANTITY_FLAG) != 0) {
            quantity = wireFlags & INLINE_QUANTITY_MASK;
            copyDefaultPayload(payloadTemplate);
            wireFlags &= CHANGED_PAYLOAD_FLAG;
        } else if ((wireFlags & DYNAMIC_PAYLOAD_FLAG) == 0) {
            quantity = Short.toUnsignedInt(payloadCursor.getShort());
            copyDefaultPayload(payloadTemplate);
        } else {
            int extraEntryCount = wireFlags & DYNAMIC_ENTRY_COUNT_MASK;
            int defaultPayloadSize = payloadSize;
            payloadEntryCount += extraEntryCount + 1;
            payloadSize += extraEntryCount * 2 + 2;

            int price = payloadCursor.getInt();
            copyDefaultPayload(payloadTemplate);
            if (payloadBytes == null || payloadBytes.length < payloadSize) {
                payloadBytes = Arrays.copyOf(payloadBytes == null ? new byte[0] : payloadBytes, payloadSize);
            }
            payloadBytes[defaultPayloadSize] = EFFECT_PRICE_OPCODE;
            payloadBytes[defaultPayloadSize + 1] = 0;
            payloadCursor.get(payloadBytes, defaultPayloadSize + 2, extraEntryCount * 2);
            ByteBuffer.wrap(payloadBytes).order(ByteOrder.LITTLE_ENDIAN).putInt(1, price);

            wireFlags &= DYNAMIC_PAYLOAD_FLAG | CHANGED_PAYLOAD_FLAG;
            quantity = 1;
        }

        if (payloadTemplate != null) {
            wireFlags |= payloadTemplate.staticFlags;
        }
        if (includeGridModeTag) {
            int tag = Byte.toUnsignedInt(payloadCursor.get());
            gridModeCode = tag == 0 ? 2 : tag + 4;
        }
    }

    /**
     * Native support extracted from TokenEntry::TokenEntryFromItemPayload @004387B2.
     */
    private void copyDefaultPayload(ItemPayloadTemplate payloadTemplate) {
        payloadBytes = payloadTemplate == null ? null : Arrays.copyOf(payloadTemplate.payloadBytes, payloadTemplate.payloadBytes.length);
    }

    /**
     * Native: TokenEntry::addQuantity @004A4100.
     * Fully ported.
     */
    public boolean addQuantity(int amount) {
        quantity += amount;
        return true;
    }

    /**
     * Native: TokenEntry::tryRemoveQuantityLeavingRemainder @004A4130.
     * Fully ported.
     */
    public boolean tryRemoveQuantityLeavingRemainder(int amount) {
        quantity -= amount;
        if (quantity > 0) {
            return true;
        }
        quantity += amount;
        return false;
    }

    /**
     * Native: TokenEntry::isMoneyEntry @0041EA40.
     * Fully ported.
     */
    public boolean isMoneyEntry() {
        return normalizedPackedTokenHash() == MONEY_ENTRY_HASH;
    }

    /**
     * Native: TokenEntry::isEmptyRegularEntry @004A4170.
     * Fully ported.
     */
    public boolean isEmptyRegularEntry() {
        return !isMoneyEntry() && quantity == 0;
    }

    /**
     * Native: TokenEntry::HasKnownItemName @00438D84.
     * Fully ported.
     */
    public boolean hasKnownItemName() {
        return Globals.itemNames.itemNameByHash.containsKey(packedTokenHash & 0xFFFF);
    }

    /**
     * Native: TokenEntry::matchesStackIdentity @004A41B0.
     * Fully ported.
     */
    public boolean matchesStackIdentity(TokenEntry other) {
        return this == other
                || (normalizedPackedTokenHash() == other.normalizedPackedTokenHash()
                && (gridModeCode == other.gridModeCode || isSubtypeGridMode(gridModeCode) && isSubtypeGridMode(other.gridModeCode))
                && (wireFlags & DYNAMIC_PAYLOAD_FLAG) == 0
                && (other.wireFlags & DYNAMIC_PAYLOAD_FLAG) == 0);
    }

    /**
     * Native support extracted from TokenEntry::matchesStackIdentity @004A41B0.
     */
    private static boolean isSubtypeGridMode(int modeCode) {
        return modeCode >= 5 && modeCode <= 8;
    }

    /**
     * Native support equivalent to Token::GetType @0041E9E0 over TokenEntry::packedTokenHash.
     */
    public int getType() {
        return (normalizedPackedTokenHash() >>> 8) & 0x0F;
    }

    /**
     * Native support: Token::GetShape @00439E70 over TokenEntry::packedTokenHash.
     */
    public int getShape() {
        return (normalizedPackedTokenHash() >>> 5) & 0x07;
    }

    /**
     * Native support: Token::GetId @00439E90 over TokenEntry::packedTokenHash.
     */
    public int getId() {
        return normalizedPackedTokenHash() & 0x1F;
    }

    /**
     * Native support: Token::GetMaterial @00439E50 over TokenEntry::packedTokenHash.
     */
    public int getMaterial() {
        return (normalizedPackedTokenHash() >>> 12) & 0x0F;
    }

    /**
     * Native support extracted from TokenEntry::isMoneyEntry @0041EA40 and Token::GetType/GetMaterial/GetShape/GetId.
     */
    private int normalizedPackedTokenHash() {
        return packedTokenHash & 0xFFFF;
    }

    /**
     * Native: TokenEntry::GetAttr @00438E8A.
     * Fully ported.
     */
    public int getAttribute(int attributeId) {
        int result = 0;
        int offset = 0;
        for (int i = 0; i < payloadEntryCount; i++) {
            int key = Byte.toUnsignedInt(payloadBytes[offset]);
            if (key == attributeId) {
                if (key == 1) {
                    result = ByteBuffer.wrap(payloadBytes, offset + 1, Integer.BYTES)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .getInt();
                } else {
                    result = Byte.toUnsignedInt(payloadBytes[offset + 1]);
                }
            }
            offset += key == 1 ? 5 : 2;
        }
        return result;
    }

    /**
     * Native: TokenEntry::GetCastSpellId @0041EB20.
     * Fully ported.
     */
    public int getCastSpellId() {
        return getAttribute(CAST_SPELL_ATTRIBUTE_ID);
    }

    /**
     * Native: TokenEntry::GetSpellLevel @0046DD00.
     * Fully ported.
     */
    public int getSpellLevel() {
        return getAttribute(SPELL_LEVEL_ATTRIBUTE_ID);
    }

    /**
     * Native: TokenEntry::FindShopSpeechEffectAttributeId @00438F3A.
     * Fully ported.
     */
    public int findShopSpeechEffectAttributeId() {
        int payloadOffset = 0;
        for (int index = 0; index < payloadEntryCount; index++) {
            int attributeId = Byte.toUnsignedInt(payloadBytes[payloadOffset]);
            if (isShopSpeechEffectAttribute(attributeId)) {
                return attributeId;
            }
            payloadOffset += attributeId == 1 ? 5 : 2;
        }
        return 0;
    }

    /**
     * Native: TokenEntry::GetShopSpeechBookSpellId @004B10B0.
     * Full port.
     */
    public int getShopSpeechBookSpellId() {
        return getAttribute(0x2A);
    }

    /**
     * Native: TokenEntry::GetCatalogEntryValue @004B6680.
     * Full port.
     */
    public int getCatalogEntryValue() {
        return getAttribute(1);
    }

    /**
     * Native support extracted from TokenEntry::FindShopSpeechEffectAttributeId @00438F3A.
     */
    private static boolean isShopSpeechEffectAttribute(int attributeId) {
        return switch (attributeId) {
            case 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                 0x0A, 0x0B, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1B,
                 0x1C, 0x1D, 0x1E, 0x1F, 0x21, 0x22, 0x23, 0x24,
                 0x25 -> true;
            default -> false;
        };
    }

    /**
     * Native support extracted from Token::GetEquipmentPortraitResourceName @00438BA1 over the packed token hash.
     * Java support suppresses the synthetic money entry because native money UI does not use this token-portrait path.
     */
    public String getEquipmentPortraitResourceName() {
        if (isEmptyRegularEntry() || isMoneyEntry()) {
            return null;
        }
        if (getType() == TYPE_SPELL) {
            return String.format("%02d%02d%03d", getMaterial(), getType(), (getShape() << 5) | getId());
        }
        return String.format("%02d%02d%d%02d", getMaterial(), getType(), getShape(), getId());
    }

    /**
     * Native: TokenEntry::resolveTooltipText @0043901F.
     * Fully ported.
     */
    public String resolveTooltipText() {
        ShapeId shape = ShapeId.fromId(getShape());
        String itemName = Globals.itemNames.resolveItemNameFromHash(packedTokenHash);
        StringBuilder result = new StringBuilder(TooltipText.colorSpan(shape.color, itemName));
        appendTooltipPayloadLines(result);
        return result.toString();
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F token payload tooltip loop.
     */
    private void appendTooltipPayloadLines(StringBuilder result) {
        if (payloadEntryCount == 0) {
            return;
        }

        ByteBuffer payload = ByteBuffer.wrap(payloadBytes, 0, payloadSize).order(ByteOrder.LITTLE_ENDIAN);
        int currentSpellId = 0;
        for (int entryIndex = 0; entryIndex < payloadEntryCount; entryIndex++) {
            int attributeId = Byte.toUnsignedInt(payload.get());
            if (attributeId == SPELL_LEVEL_ATTRIBUTE_ID) {
                appendSpellLevelTooltipLines(result, currentSpellId, Byte.toUnsignedInt(payload.get()));
                continue;
            }
            if (attributeId == EFFECT_PRICE_OPCODE) {
                payload.get();
                if (getType() != TYPE_SPELL) {
                    result.append('#').append(get(MAIN_MAGIC_189));
                }
                continue;
            }

            StatsText attributeText = StatsText.byIndex(attributeId);
            switch (attributeText) {
                case BLANK_1 -> payload.position(payload.position() + Integer.BYTES);
                case DAMAGE_13, FIRE_DAMAGE_44, WATER_DAMAGE_45, AIR_DAMAGE_46, EARTH_DAMAGE_47,
                     ASTRAL_DAMAGE_48 -> {
                    int min = Byte.toUnsignedInt(payload.get());
                    payload.get();
                    int max = min + Byte.toUnsignedInt(payload.get());
                    entryIndex++;
                    appendNamedRangeTooltipLine(result, attributeText, min, max);
                }
                case RANGE_38 -> appendNamedUnsignedTooltipLine(result, attributeText, Byte.toUnsignedInt(payload.get()));
                case OF_41 -> {
                    currentSpellId = Byte.toUnsignedInt(payload.get());
                    appendCastSpellTooltipLine(result, currentSpellId);
                }
                case CASTS_42 -> {
                    currentSpellId = Byte.toUnsignedInt(payload.get());
                    appendSpellNameSuffix(result, currentSpellId);
                }
                default -> appendSignedTooltipLine(result, attributeText, Byte.toUnsignedInt(payload.get()));
            }
        }
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F spell-stat expansion for payload opcode `0x32`.
     * Fully ported using the modeled Spell stat APIs.
     */
    private void appendSpellLevelTooltipLines(StringBuilder result, int spellId, int spellLevel) {
        Spell spell = new Spell((byte) spellId);
        spell.updateStats(spellLevel);
        if (spellId == SpellId.STONE_CURSE.id) {
            spell.duration >>= 1;
        }

        if (spell.getDamageSpread() != 0) {
            result.append('#')
                    .append(get(MAIN_DAMAGE_118))
                    .append(": ")
                    .append(spell.getMinDamage())
                    .append('-')
                    .append(spell.getMaxDamage());
        }

        if (getType() == TYPE_SPELL) {
            if (spell.getMaxRange() != 0) {
                appendSpellRangeLine(result, spell.getMaxRange());
            }
        } else {
            appendSpellRangeLine(result, readCastItemTooltipRange());
        }

        if (spell.getDuration() != 0) {
            double durationSeconds = spell.getDuration() / 16.0;
            if (spellId == SpellId.STONE_CURSE.id) {
                result.append(String.format(Locale.ROOT, "#%s: 0.0 -%5.1f", get(MAIN_DURATION_124), durationSeconds));
            } else {
                result.append(String.format(Locale.ROOT, "#%s: %5.1f", get(MAIN_DURATION_124), durationSeconds));
            }
        }

        appendSpellUnsignedLine(result, MAIN_SPEED_182, spell.getSpeed(spellLevel));
        appendSpellPercentLine(result, MAIN_MAXIMUM_DAMAGE_PROBABILITY_185, spell.getMaximumDamageProbability(spellLevel));
        appendSpellPercentLine(result, MAIN_MINIMUM_DAMAGE_PROBABILITY_187, spell.getMinimumDamageProbability(spellLevel));
        appendSpellSignedLine(result, MAIN_RESISTANCE_183, spell.getResistance(spellLevel));
        appendSpellUnsignedLine(result, MAIN_RAYS_186, spell.getRays(spellLevel));
        appendSpellUnsignedLine(result, MAIN_SIGHT_184, spell.getSight(spellLevel));
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F `#%s: %d` range formatting.
     * Fully ported.
     */
    private static void appendSpellRangeLine(StringBuilder result, int range) {
        result.append('#').append(get(MAIN_RANGE_123)).append(": ").append(range);
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F `#%s: %d` spell-stat formatting.
     * Fully ported.
     */
    private static void appendSpellUnsignedLine(StringBuilder result, int textIndex, int value) {
        if (value != 0) {
            result.append('#').append(get(textIndex)).append(": ").append(value);
        }
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F `#%s: +%d` spell-stat formatting.
     * Fully ported.
     */
    private static void appendSpellSignedLine(StringBuilder result, int textIndex, int value) {
        if (value != 0) {
            result.append('#').append(get(textIndex)).append(": ");
            if (value > 0) {
                result.append('+');
            }
            result.append(value);
        }
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F `#%s: +%d%%` spell-stat formatting.
     * Fully ported.
     */
    private static void appendSpellPercentLine(StringBuilder result, int textIndex, int value) {
        if (value != 0) {
            result.append('#').append(get(textIndex)).append(": +").append(value).append('%');
        }
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F raw item byte read at offset 0x54.
     */
    private int readCastItemTooltipRange() {
        Item item = Globals.staticDataMgr.createItemFromPackedHash(normalizedPackedTokenHash());
        return switch (item) {
            case Weapon weapon -> weapon.range & 0xFF;
            case Armor armor -> armor.armorSlot & 0xFF;
            case Shield shield -> shield.statData.defence & 0xFF;
            default -> throw new IllegalStateException(
                    "Unsupported Item +0x54 tooltip range source: " + item.getClass().getName()
            );
        };
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F `#%s %+d` stat formatting.
     */
    private static void appendSignedTooltipLine(StringBuilder result, StatsText statText, int value) {
        String statName = get(STATS, statText);
        if (statName.isEmpty()) {
            return;
        }
        result.append('#').append(statName).append(' ').append(value >= 0 ? "+" : "").append(value);
        if (statText == StatsText.MANA_REGENERATION_11 || statText == StatsText.HEALTH_REGENERATION_8) {
            result.append('%');
        }
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F `#%s %d` stat formatting.
     */
    private static void appendNamedUnsignedTooltipLine(StringBuilder result, StatsText statText, int value) {
        String statName = get(STATS, statText);
        if (!statName.isEmpty()) {
            result.append('#').append(statName).append(' ').append(value);
        }
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F ranged stat formatting.
     */
    private static void appendNamedRangeTooltipLine(StringBuilder result, StatsText statText, int min, int max) {
        String statName = get(STATS, statText);
        if (!statName.isEmpty()) {
            result.append('#').append(statName).append(' ').append(min).append('-').append(max);
        }
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F spell-name suffix branches.
     */
    private void appendCastSpellTooltipLine(StringBuilder result, int spellId) {
        if (getType() == TYPE_SPELL) {
            appendSpellNameSuffix(result, spellId);
            return;
        }
        result.append('#').append(get(MAIN_CASTS_92)).append(' ').append(resolveSpellName(spellId));
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F `of <spell>` suffix branches.
     */
    private static void appendSpellNameSuffix(StringBuilder result, int spellId) {
        result.append(' ').append(get(MAIN_OF_90)).append(' ').append(resolveSpellName(spellId));
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F CTextFile::GetAt(&g_spell, spellId - 1).
     */
    private static String resolveSpellName(int spellId) {
        return get(SPELL, SpellText.fromSpellId(spellId));
    }

}
