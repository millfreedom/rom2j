package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;
import java.util.Iterator;

public class UnitGroupList implements MfcSerializable, Iterable<UnitGroup> {
    //0x00
    public final CustomList<UnitGroup> unitGroups = new CustomList<>(UnitGroup.class);

    /**
     * Native: UnitGroupList::Serialize @0052C3F3.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        ar.serialize(unitGroups);
    }

    /**
     * Native: UnitGroupList::createGroupReplacingFirstEmpty @0052B06F.
     * Fully ported.
     */
    public UnitGroup createGroupReplacingFirstEmpty() {
        Iterator<UnitGroup> groups = unitGroups.iterator();
        while (groups.hasNext()) {
            UnitGroup group = groups.next();
            if (group.units.isEmpty()) {
                groups.remove();
                break;
            }
        }
        return new UnitGroup();
    }

    /**
     * not ported. Java list delegation for native callers that access UnitGroupList::unitGroups.
     */
    public boolean add(UnitGroup group) {
        return unitGroups.add(group);
    }

    /**
     * not ported. Java list delegation for native callers that access UnitGroupList::unitGroups.
     */
    public boolean remove(UnitGroup group) {
        return unitGroups.remove(group);
    }

    /**
     * not ported. Java list delegation for native callers that access UnitGroupList::unitGroups.
     */
    public void clear() {
        unitGroups.clear();
    }

    /**
     * not ported. Java list delegation for native callers that access UnitGroupList::unitGroups.
     */
    public boolean isEmpty() {
        return unitGroups.isEmpty();
    }

    /**
     * not ported. Java list delegation for native callers that access UnitGroupList::unitGroups.
     */
    public int size() {
        return unitGroups.size();
    }

    /**
     * not ported. Java list delegation for native callers that access UnitGroupList::unitGroups.
     */
    public UnitGroup get(int index) {
        return unitGroups.get(index);
    }

    /**
     * not ported. Java list delegation for native callers that access UnitGroupList::unitGroups.
     */
    @Override
    public Iterator<UnitGroup> iterator() {
        return unitGroups.iterator();
    }
}
