package ua.millfreedom.rom2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Native class: DiplomacyWrapper.
 * Purpose: owner for the diplomacy array embedded in `CMainWindow`.
 */
public class DiplomacyWrapper {
    //0x0
    public final List<Diplomacy> m_pDiplomacyArray;

    /**
     * Native: DiplomacyWrapper::DiplomacyWrapper @00444C7C.
     * Fully ported. Java owns the backing list directly instead of native heap-allocating a CArray pointer.
     */
    public DiplomacyWrapper() {
        m_pDiplomacyArray = new ArrayList<>();
    }
}
