package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Native support structure: global selected-units snapshot at DAT_006212d0.
 * Native destructor SelectedUnitsSnapshot::~SelectedUnitsSnapshot @00473870 is represented by Java object lifecycle.
 */
public final class SelectedUnitsSnapshot {
    public static final int NATIVE_SIZE = 0x30;

    // Native global: DAT_006212d0.
    public static final SelectedUnitsSnapshot GLOBAL = new SelectedUnitsSnapshot();

    //0x00
    public final List<CUnit> primaryUnits = new ArrayList<>();

    //0x14
    public final List<CUnit> secondaryUnits = new ArrayList<>();

    //0x28
    public int secondaryGroup0Count;

    //0x2c
    public int secondaryGroup1Count;

    /**
     * Native: SelectedUnitsSnapshot::SelectedUnitsSnapshot @00473800.
     * Fully ported through Java list fields and default zero counts.
     */
    public SelectedUnitsSnapshot() {
    }

    /**
     * Native: SelectedUnitsSnapshot::rebuildFromCurrentPlayerUnits @00472460.
     * Fully ported. Java receives the native AfxGetMainWnd()->pMapVisualObject dependency as a parameter.
     */
    public void rebuildFromCurrentPlayerUnits(MapVisualObject mapVisualObject) {
        clear();
        mapVisualObject.collectCurrentPlayerUnits(this);
    }

    /**
     * Native: SelectedUnitsSnapshot::addPrimaryUnit @004738D0.
     * Fully ported.
     */
    public void addPrimaryUnit(CUnit unit) {
        primaryUnits.add(unit);
    }

    /**
     * Native: SelectedUnitsSnapshot::clear @004738F0.
     * Fully ported. Native CArray<>::RemoveAll @00473930 is represented by Java List.clear().
     */
    public SelectedUnitsSnapshot clear() {
        primaryUnits.clear();
        secondaryUnits.clear();
        secondaryGroup0Count = 0;
        secondaryGroup1Count = 0;
        return this;
    }

    /**
     * Native: SelectedUnitsSnapshot::getPrimaryUnits @00473950.
     * Fully ported.
     */
    public List<CUnit> getPrimaryUnits() {
        return primaryUnits;
    }

    /**
     * Native: SelectedUnitsSnapshot::addSecondaryUnit @00473960.
     * Fully ported.
     */
    public void addSecondaryUnit(CUnit unit) {
        secondaryUnits.add(unit);
        if ((unit.unitFlags & 0x2) == 0) {
            secondaryGroup0Count++;
        } else {
            secondaryGroup1Count++;
        }
    }

    /**
     * Native: SelectedUnitsSnapshot::findFlag20PrimaryUnitIndex @004725F0.
     * Fully ported.
     */
    public int findFlag20PrimaryUnitIndex() {
        for (int unitIndex = 0; unitIndex < primaryUnits.size(); unitIndex++) {
            if ((primaryUnits.get(unitIndex).unitFlags & 0x20) != 0) {
                return unitIndex;
            }
        }
        return -1;
    }
}
