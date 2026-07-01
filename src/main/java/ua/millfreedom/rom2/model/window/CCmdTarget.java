package ua.millfreedom.rom2.model.window;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MessageTarget;

public class CCmdTarget implements MessageTarget {
    //0x4
    public int m_dwRef;
    //0x8
    public Object m_pOuterUnknown;
    //0xc
    public Object m_xInnerUnknown;
    //0x10
    public Object m_xDispatch;
    //0x14
    public int m_bResultExpected;
    //0x18
    public Object m_xConnPtContainer;

    /**
     * Native: CCmdTarget::CCmdTarget @005a7c1a. Fully ported.
     */
    public CCmdTarget() {
        m_dwRef = 1;
        m_bResultExpected = 1;
    }

    /**
     * Java message-target bridge for non-window command targets.
     * not ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        return 0;
    }
}
