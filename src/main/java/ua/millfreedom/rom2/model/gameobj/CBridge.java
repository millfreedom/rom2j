package ua.millfreedom.rom2.model.gameobj;

public class CBridge extends CStructure {
    //0x144
    public int bridgeSizeX;

    //0x148
    public int bridgeSizeY;

    /**
     * Native: CBridge::CBridge @004625AD.
     * Fully ported.
     */
    public CBridge(int bridgeSizeX, int bridgeSizeY) {
        setBridgeSize(bridgeSizeX, bridgeSizeY);
    }

    /**
     * Native: CBridge::CBridge @0046260B.
     * Fully ported. Native delegates to CStructure copy construction and copies the bridge dimensions.
     */
    public CBridge(CBridge source) {
        super(source);
        bridgeSizeX = source.bridgeSizeX;
        bridgeSizeY = source.bridgeSizeY;
    }

    /**
     * vtbl +0x20: CBridge::GetTileWidth @0046DE00.
     * Full port. Native returns `bridgeSizeX`.
     */
    @Override
    public int getTileWidth() {
        return bridgeSizeX;
    }

    /**
     * vtbl +0x24: CBridge::GetTileHeight @0046DE20.
     * Full port. Native returns `bridgeSizeY`.
     */
    @Override
    public int getTileHeight() {
        return bridgeSizeY;
    }

    /**
     * Native: CBridge::SetBridgeSize @0046DE40.
     * Fully ported.
     */
    public final void setBridgeSize(int bridgeSizeX, int bridgeSizeY) {
        this.bridgeSizeX = bridgeSizeX;
        this.bridgeSizeY = bridgeSizeY;
    }
}
