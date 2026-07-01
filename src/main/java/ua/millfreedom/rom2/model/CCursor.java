package ua.millfreedom.rom2.model;

import lombok.SneakyThrows;
import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;

/**
 * Native cursor asset wrapper used by the global `g_Cursor_*` objects.
 * Size in native code: 0x18.
 */
public final class CCursor implements MfcSerializable {


    //0x4
    public CSprite256 m_pBitmap;

    //0x8
    public int m_HotSpotX;

    //0xc
    public int m_HotSpotY;

    //0x10
    public int m_FrameCount;

    //0x14
    public int m_AnimSpeed;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CCursor::CCursor @0047B736.
     * Fully ported.
     */
    @SneakyThrows
    public CCursor(String filename, int hotspotX, int hotspotY, int speed)  {
        if (filename.contains("16a")) {
            m_pBitmap = new CA16(filename);
            m_pBitmap.initPalette(0x10, 4, 0);
        } else {
            m_pBitmap = new CSprite256(filename);
            m_pBitmap.initPalette(1, 1, 0);
        }
//        System.out.println(filename);
//        System.out.println(m_pBitmap);
        m_HotSpotX = hotspotX;
        m_HotSpotY = hotspotY;
        m_AnimSpeed = speed;
        m_FrameCount = m_pBitmap.frameCount;
    }

    /**
     * Native: CCursor::GetBitmap @0041F2E0.
     * Fully ported.
     */
    public CGameBitmap getBitmap() {
        return m_pBitmap;
    }

    /**
     * Native: CCursor::SetToMousePointer @0047B8F5.
     * Fully ported.
     */
    public void setToMousePointer() {
        Globals.mousePointer.init(m_pBitmap, m_HotSpotX, m_HotSpotY, m_FrameCount, m_AnimSpeed);
    }

    /**
     * Native: CCursor::SetToPointer @004739B0.
     * Fully ported.
     */
    public boolean setToPointer() {
        if (Globals.mousePointer.getSourceBitmap() != getBitmap()) {
            setToMousePointer();
            return true;
        }
        return false;
    }

}
