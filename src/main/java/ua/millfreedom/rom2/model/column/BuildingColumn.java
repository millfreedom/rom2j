package ua.millfreedom.rom2.model.column;

import lombok.Getter;

/**
 * Indices for BuildingInfo.values columns.
 */
public enum BuildingColumn {
    SIZE_X(0),
    SIZE_Y(1),
    SCAN_RANGE(2),
    HEALTH_MAX(3),
    PASSABILITY(4),
    BUILDING_PRESENT(5),
    START_ID(6),
    TILES(7);

    public final int index;

    BuildingColumn(int index) {
        this.index = index;
    }
    public static BuildingColumn from(int index){
        for (var column : BuildingColumn.values()) {
            if (column.index == index){
                return column;
            }
        }
        throw new RuntimeException("no such column");
    }
}
