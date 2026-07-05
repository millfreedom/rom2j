package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.gameobj.CUnit;

/**
 * Java-only item tooltip composition helpers.
 * not ported.
 */
public final class ItemTooltipText {
    /**
     * Java-only utility class guard.
     * not ported.
     */
    private ItemTooltipText() {
    }

    /**
     * Java-only ALT tooltip extension over TokenEntry::resolveTooltipText @0043901F and Item::GetSlot @0053BFF0.
     * not ported.
     */
    public static String withAltEquippedComparison(TokenEntry highlightedToken, String tooltip, CUnit comparisonUnit) {
        if (!Globals.altKeyDown || tooltip == null) {
            return tooltip;
        }

        TokenEntry equippedToken = resolveEquippedComparisonToken(highlightedToken, comparisonUnit);
        if (equippedToken == null || equippedToken == highlightedToken) {
            return tooltip;
        }
        return TooltipText.sideBySide(tooltip, equippedToken.resolveTooltipText());
    }

    /**
     * Java-only helper mapping a highlighted token's resolved Item slot onto a unit equipment snapshot.
     * not ported.
     */
    private static TokenEntry resolveEquippedComparisonToken(TokenEntry highlightedToken, CUnit comparisonUnit) {
        if (comparisonUnit == null) {
            return null;
        }

        Item highlightedItem = Globals.staticDataMgr.createItemFromPackedHash(highlightedToken.packedTokenHash & 0xFFFF);
        if (highlightedItem == null) {
            return null;
        }

        int slotIndex = highlightedItem.getSlot() - 1;
        if (slotIndex < 0 || slotIndex >= comparisonUnit.equipmentTokenEntries.length) {
            return null;
        }
        return comparisonUnit.equipmentTokenEntries[slotIndex];
    }
}
