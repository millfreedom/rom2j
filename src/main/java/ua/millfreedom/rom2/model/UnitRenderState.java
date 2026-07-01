package ua.millfreedom.rom2.model;

/**
 * Java support tuple for the frame-selection state reused by `CUnit::Draw` and `CUnit::DrawShadow`.
 */
public final class UnitRenderState {
    public int unitTypeId;
    public int fileId;
    public int frameIndex;
    public boolean flipX;
    public CUnitInfo info;
}
