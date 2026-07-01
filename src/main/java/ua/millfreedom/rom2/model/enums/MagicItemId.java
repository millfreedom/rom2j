package ua.millfreedom.rom2.model.enums;

public enum MagicItemId {
    EMPTY(0, ""),
    BOOK_AIR(1, "Book Air"),
    BOOK_WATER(2, "Book Water"),
    BOOK_FIRE(3, "Book Fire"),
    BOOK_EARTH(4, "Book Earth"),
    BOOK_ASTRAL(5, "Book Astral"),
    SCROLL_FIRE_ARROW(6, "Scroll Fire Arrow"),
    SCROLL_FIRE_BALL(7, "Scroll Fire Ball"),
    SCROLL_FIRE_WALL(8, "Scroll Fire Wall"),
    SCROLL_PROTECTION_FROM_FIRE(9, "Scroll Protection from Fire"),
    SCROLL_ICE_MISSILE(10, "Scroll Ice Missile"),
    SCROLL_POISON_CLOUD(11, "Scroll Poison Cloud"),
    SCROLL_BLIZZARD(12, "Scroll Blizzard"),
    SCROLL_PROTECTION_FROM_WATER(13, "Scroll Protection from Water"),
    SCROLL_ACID_STREAM(14, "Scroll Acid Stream"),
    SCROLL_LIGHTNING(15, "Scroll Lightning"),
    SCROLL_PRISMATIC_SPRAY(16, "Scroll Prismatic Spray"),
    SCROLL_INVISIBILITY(17, "Scroll Invisibility"),
    SCROLL_PROTECTION_FROM_AIR(18, "Scroll Protection from Air"),
    SCROLL_DARKNESS(19, "Scroll Darkness"),
    SCROLL_LIGHT(20, "Scroll Light-"),
    SCROLL_DIAMOND_DUST(21, "Scroll Diamond Dust"),
    SCROLL_WALL_OF_EARTH(22, "Scroll Wall of Earth"),
    SCROLL_STONE_CURSE(23, "Scroll Stone Curse"),
    SCROLL_PROTECTION_FROM_EARTH(24, "Scroll Protection from Earth"),
    SCROLL_BLESS(25, "Scroll Bless"),
    SCROLL_HASTE(26, "Scroll Haste"),
    SCROLL_CONTROL_SPIRIT(27, "Scroll Control Spirit"),
    SCROLL_TELEPORT(28, "Scroll Teleport"),
    SCROLL_HEAL(29, "Scroll Heal"),
    SCROLL_SUMMON(30, "Scroll Summon"),
    SCROLL_DRAIN_LIFE(31, "Scroll Drain Life"),
    SCROLL_SHIELD(32, "Scroll Shield"),
    SCROLL_CURSE(33, "Scroll Curse"),
    SCROLL_SLOW(34, "Scroll Slow"),
    SUPERSCROLL_FIRE_ARROW(35, "SuperScroll Fire Arrow"),
    SUPERSCROLL_FIRE_BALL(36, "SuperScroll Fire Ball"),
    SUPERSCROLL_FIRE_WALL(37, "SuperScroll Fire Wall"),
    SUPERSCROLL_PROTECTION_FROM_FIRE(38, "SuperScroll Protection from Fire"),
    SUPERSCROLL_ICE_MISSILE(39, "SuperScroll Ice Missile"),
    SUPERSCROLL_POISON_CLOUD(40, "SuperScroll Poison Cloud"),
    SUPERSCROLL_BLIZZARD(41, "SuperScroll Blizzard"),
    SUPERSCROLL_PROTECTION_FROM_WATER(42, "SuperScroll Protection from Water"),
    SUPERSCROLL_ACID_STREAM(43, "SuperScroll Acid Stream"),
    SUPERSCROLL_LIGHTNING(44, "SuperScroll Lightning"),
    SUPERSCROLL_PRISMATIC_SPRAY(45, "SuperScroll Prismatic Spray"),
    SUPERSCROLL_INVISIBILITY(46, "SuperScroll Invisibility"),
    SUPERSCROLL_PROTECTION_FROM_AIR(47, "SuperScroll Protection from Air"),
    SUPERSCROLL_DARKNESS(48, "SuperScroll Darkness"),
    SUPERSCROLL_LIGHT(49, "SuperScroll Light-"),
    SUPERSCROLL_DIAMOND_DUST(50, "SuperScroll Diamond Dust"),
    SUPERSCROLL_WALL_OF_EARTH(51, "SuperScroll Wall of Earth"),
    SUPERSCROLL_STONE_CURSE(52, "SuperScroll Stone Curse"),
    SUPERSCROLL_PROTECTION_FROM_EARTH(53, "SuperScroll Protection from Earth"),
    SUPERSCROLL_BLESS(54, "SuperScroll Bless"),
    SUPERSCROLL_HASTE(55, "SuperScroll Haste"),
    SUPERSCROLL_CONTROL_SPIRIT(56, "SuperScroll Control Spirit"),
    SUPERSCROLL_TELEPORT(57, "SuperScroll Teleport"),
    SUPERSCROLL_HEAL(58, "SuperScroll Heal"),
    SUPERSCROLL_SUMMON(59, "SuperScroll Summon"),
    SUPERSCROLL_DRAIN_LIFE(60, "SuperScroll Drain Life"),
    SUPERSCROLL_SHIELD(61, "SuperScroll Shield"),
    SUPERSCROLL_CURSE(62, "SuperScroll Curse"),
    SUPERSCROLL_SLOW(63, "SuperScroll Slow"),
    POTION_ANTIPOISON(64, "Potion Antipoison"),
    POTION_BODY(65, "Potion Body"),
    POTION_REACTION(66, "Potion Reaction"),
    POTION_MIND(67, "Potion Mind"),
    POTION_SPIRIT(68, "Potion Spirit"),
    POTION_HEALTH_REGENERATION(69, "Potion Health Regeneration"),
    POTION_MEDIUM_HEALING(70, "Potion Medium Healing"),
    POTION_BIG_HEALING(71, "Potion Big Healing"),
    POTION_MANA_REGENERATION(72, "Potion Mana Regeneration"),
    POTION_MEDIUM_MANA(73, "Potion Medium Mana"),
    POTION_BIG_MANA(74, "Potion Big Mana"),
    POTION_FIGHTER_BONUS(75, "Potion Fighter Bonus"),
    POTION_MAGE_BONUS(76, "Potion Mage Bonus"),
    QUEST_DOCUMENTS(77, "Quest Documents"),
    QUEST_BANNER(78, "Quest Banner"),
    QUEST_CROWN1(79, "Quest Crown1"),
    QUEST_CROWN12(80, "Quest Crown12"),
    QUEST_CROWN13(81, "Quest Crown13"),
    QUEST_INGREDIENT(82, "Quest Ingredient"),
    QUEST_TREASURE(83, "Quest Treasure"),
    QUEST_AMULET(84, "Quest Amulet"),
    QUEST_MAP(85, "Quest Map"),
    QUEST_HEAD(86, "Quest Head"),
    QUEST_STONE(87, "Quest Stone"),
    QUEST_META1(88, "Quest Meta1"),
    QUEST_META2(89, "Quest Meta2"),
    QUEST_META3(90, "Quest Meta3"),
    QUEST_META4(91, "Quest Meta4"),
    QUEST_RUNEA(92, "Quest RuneA"),
    QUEST_RUNEE(93, "Quest RuneE"),
    QUEST_RUNEF(94, "Quest RuneF"),
    QUEST_RUNEW(95, "Quest RuneW"),
    QUEST_RING(96, "Quest Ring"),
    UNKNOWN(-1, "unknown");

    private static final MagicItemId[] BY_ID = createLookup();

    public final int id;
    public final String tableName;

    MagicItemId(int id, String tableName) {
        this.id = id;
        this.tableName = tableName;
    }

    public static MagicItemId fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return UNKNOWN;
        }
        MagicItemId value = BY_ID[id];
        return value == null ? UNKNOWN : value;
    }

    public boolean isBetween(MagicItemId from, MagicItemId to) {
        return id >= from.id && id <= to.id;
    }

    private static MagicItemId[] createLookup() {
        int maxId = QUEST_RING.id;
        MagicItemId[] values = new MagicItemId[maxId + 1];
        for (MagicItemId value : MagicItemId.values()) {
            if (value.id >= 0) {
                values[value.id] = value;
            }
        }
        return values;
    }
}
