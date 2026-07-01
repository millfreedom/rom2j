package ua.millfreedom.rom2.model.world.scenario;

import ua.millfreedom.rom2.model.enums.BuildingId;

import java.nio.ByteBuffer;

/**
 * Native BuildingDTO struct constructed by BuildingDTO::BuildingDTO @0054C620 and @0054C690.
 */
public final class BuildingDTO {
    //0x00
    public int x;
    //0x02
    public int y;
    //0x04
    public int typeID;
    //0x06
    public int hp;
    //0x08
    public int playerID;
    //0x0A
    public int sizeX;
    //0x0C
    public int sizeY;
    //0x10
    public int buildingID;

    /**
     * Native: BuildingDTO::BuildingDTO @0054C620.
     * Fully ported.
     */
    public BuildingDTO(int x, int y, int typeID, int hp, int playerID, int buildingID) {
        this.x = x & 0xFFFF;
        this.y = y & 0xFFFF;
        this.typeID = typeID & 0xFFFF;
        this.hp = hp & 0xFFFF;
        this.playerID = playerID & 0xFFFF;
        this.buildingID = buildingID;
        this.sizeX = 0;
        this.sizeY = 0;
    }

    /**
     * Native: BuildingDTO::BuildingDTO @0054C690.
     * Fully ported.
     */
    public BuildingDTO(int x, int y, int typeID, int hp, int playerID, int buildingID, int sizeX, int sizeY) {
        this.x = x & 0xFFFF;
        this.y = y & 0xFFFF;
        this.typeID = typeID & 0xFFFF;
        this.hp = hp & 0xFFFF;
        this.playerID = playerID & 0xFFFF;
        this.buildingID = buildingID;
        this.sizeX = sizeX & 0xFFFF;
        this.sizeY = sizeY & 0xFFFF;
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 building section.
     * Fully ported.
     */
    public static BuildingDTO read(ByteBuffer section) {
        int extendedSizeTypeFlag = 0x01000000;
        int packedX = section.getInt();
        int packedY = section.getInt();
        int rawTypeID = section.getInt();
        int hp = Short.toUnsignedInt(section.getShort());
        int playerID = section.getInt() & 0xFFFF;
        int buildingID = section.getShort();
        int x = (packedX >> 8) & 0xFFFF;
        int y = (packedY >> 8) & 0xFFFF;
        if (rawTypeID == BuildingId.VERTICAL_WOODEN_BRIDGE.id || (rawTypeID & extendedSizeTypeFlag) != 0) {
            int sizeX = section.getInt() & 0xFFFF;
            int sizeY = section.getInt() & 0xFFFF;
            return new BuildingDTO(
                    x,
                    y,
                    BuildingId.VERTICAL_WOODEN_BRIDGE.id,
                    hp,
                    playerID,
                    buildingID,
                    sizeX,
                    sizeY
            );
        }
        return new BuildingDTO(x, y, rawTypeID, hp, playerID, buildingID);
    }
}
