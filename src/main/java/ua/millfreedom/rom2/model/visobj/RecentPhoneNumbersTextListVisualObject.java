package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.List;

import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DELETE;

/**
 * Native class: RecentPhoneNumbersTextListVisualObject.
 * Purpose: modem/phone dialog text-list specialization for recent phone numbers.
 */
public class RecentPhoneNumbersTextListVisualObject extends TextListVisualObject {
    public static final int NATIVE_SIZE = 0x98; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x94
    public List<String> phoneNumberArray;

    /**
     * Native: RecentPhoneNumbersTextListVisualObject::RecentPhoneNumbersTextListVisualObject @0044E4A0.
     * Fully ported.
     */
    public RecentPhoneNumbersTextListVisualObject(int id, CRect rect, Object bitmapFont, Palette16 field0x7c, Palette16 field0x80, int field0x90, String name, List<String> phoneNumberArray) {
        super(id, rect, bitmapFont, field0x7c, field0x80, field0x90, name);
        this.phoneNumberArray = phoneNumberArray;
    }

    /**
     * vtbl +0x6C: RecentPhoneNumbersTextListVisualObject::OnKeyDown @0044E4F0.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar != VK_DELETE) {
            return super.onKeyDown(nChar);
        }

        if (selectedRow >= 0) {
            int removedRow = selectedRow;
            removeRowAndAdjustSelection(removedRow);
            phoneNumberArray.remove(removedRow);

            int rowCount = rows.size();
            if (selectedRow == rowCount) {
                selectedRow -= 1;
            }

            CVisualObject linkedChild = m_pParent.getChildById(linkedChildId);
            if (linkedChild instanceof PostSetupVisualObject postSetupVisualObject && selectedRow != 0) {
                postSetupVisualObject.syncSelectionState(selectedRow, rowCount);
            }

            draw();
        }
        return 1;
    }
}
