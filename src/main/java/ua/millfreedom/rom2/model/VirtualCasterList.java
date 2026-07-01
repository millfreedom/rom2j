package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;

public class VirtualCasterList extends CustomList<VirtualCaster> {

    /**
     * Native: VirtualCasterList::VirtualCasterList @0053C510.
     * Fully ported.
     */
    public VirtualCasterList() {
        super(VirtualCaster.class);
    }

    /**
     * Native: VirtualCasterList::Serialize @0053BBD0.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
    }

    /**
     * Native: VirtualCasterList::RemoveAll @0053BD70.
     * Fully ported.
     */
    @Override
    public void clear() {
        super.clear();
    }
}
