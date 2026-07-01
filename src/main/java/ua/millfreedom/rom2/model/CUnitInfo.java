package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette256;

import java.util.ArrayList;
import java.util.List;

/**
 * Native class: CUnitInfo.
 * Purpose: runtime unit graphics/animation descriptor stored in the native `g_UnitTypes` array.
 */
public final class CUnitInfo implements MfcSerializable {
    //0x04
    public int m_ID;

    //0x08
    public int m_ParentID;

    //0x0C
    public int m_FileID;

    //0x10
    public int m_SpriteIndex;

    //0x14
    public int m_MovePhases;

    //0x18
    public int m_MoveBeginPhases;

    //0x1C
    public int m_AttackPhases;

    //0x20
    public int m_DyingPhases;

    //0x24
    public int m_BonePhases;

    //0x28
    public int m_IdlePhases;

    //0x2C
    public int m_Width;

    //0x30
    public int m_Height;

    //0x34
    public int m_CenterX;

    //0x38
    public int m_CenterY;

    //0x3C
    public final List<Integer> m_MoveFrameSequence = new ArrayList<>();

    //0x50
    public int m_MoveFrameSequenceCount;

    //0x54
    public final List<Integer> m_AttackFrameSequence = new ArrayList<>();

    //0x68
    public int m_AttackFrameSequenceCount;

    //0x6C
    public final List<Integer> m_IdleFrameSequence = new ArrayList<>();

    //0x80
    public int m_IdleFrameSequenceCount;

    //0x84
    public final CRect m_SelectionRect = new CRect();

    //0x94
    public int m_bDying;

    //0x98
    public int m_PaletteIndex;

    //0x9C
    public final CGamePalette[] m_Palettes = new CGamePalette[8];

    //0xBC
    public final Palette256[] m_PaletteRawData = new Palette256[8];

    //0xDC
    public final List<Integer> m_SoundIDs = new ArrayList<>();

    //0xF0
    public int m_TileSize;

    //0xF4
    public int m_ProjectileID;

    //0xF8
    public String m_InfoPicture = "";

    //0x108
    public final List<Integer> m_ShootOffsets = new ArrayList<>();

    //0x11C
    public int m_ShootDelay;

    //0x120
    public int m_AttackDelay;

    //0x124
    public int m_ZOffset;

    //0x128
    public int m_Flip;

    //0x12C
    public String m_DescText = "";

    /**
     * Native: CUnitInfo::New @0047B3F0.
     * Fully ported by Java field initializers and default zero/null values.
     */
    public CUnitInfo() {
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CUnitInfo::getOrInitPalette @0046DF70.
     * Full port. Native initializes the existing local `CGamePalette` from `m_PaletteRawData[slot]` on first use.
     */
    public CGamePalette getOrInitPalette(int paletteSlot) {
        if (m_Palettes[paletteSlot].paletteData == null) {
            Palette256 palette256 = m_PaletteRawData[paletteSlot];
            m_Palettes[paletteSlot].init(palette256, 0x10, 2, 1);
        }
        return m_Palettes[paletteSlot];
    }

    /**
     * Native: CUnitInfo::getRawPalette @0046DFD0.
     * Full port. Native returns the raw palette pointer from `m_PaletteRawData[slot]`.
     */
    public Palette256 getRawPalette(int paletteSlot) {
        return m_PaletteRawData[paletteSlot];
    }
}
