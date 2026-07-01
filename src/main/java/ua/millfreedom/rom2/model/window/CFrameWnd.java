package ua.millfreedom.rom2.model.window;

import ua.millfreedom.rom2.model.CRect;

public class CFrameWnd extends CWnd {
    //0x3c
    public int m_bAutoMenuEnable;
    //0x40
    public int m_nWindow;
    //0x44
    public Object m_hMenuDefault;
    //0x48
    public Object m_hAccelTable;
    //0x4c
    public int m_dwPromptContext;
    //0x50
    public int m_bHelpMode;
    //0x54
    public CFrameWnd m_pNextFrameWnd;
    //0x58
    public CRect m_rectBorder = new CRect();
    //0x68
    public Object m_pNotifyHook;
    //0x6c
    public Object m_listControlBars;
    //0x88
    public int m_nShowDelay;
    //0x8c
    public int m_nIDHelp;
    //0x90
    public int m_nIDTracking;
    //0x94
    public int m_nIDLastMessage;
    //0x98
    public Object m_pViewActive;
    //0x9c
    public Object m_lpfnCloseProc;
    //0xa0
    public int m_cModalStack;
    //0xa4
    public Object m_phWndDisable;
    //0xa8
    public Object m_hMenuAlt;
    //0xac
    public String m_strTitle = "";
    //0xb0
    public int m_bInRecalcLayout;
    //0xb4
    public Object m_pFloatingFrameClass;
    //0xb8
    public int m_nIdleFlags;

    /**
     * Native boundary: CFrameWnd::OnCreate @005B43D8, called by CMainWindow::OnCreate @004826A0.
     * Java keeps stock MFC default creation behavior as success for modeled windows.
     */
    public int onCreate(Object createStruct) {
        return 0;
    }

    /**
     * Native boundary: CFrameWnd::OnDestroy @005B479E, called by CMainWindow::OnDestroy @00482526.
     * Java has no modeled stock MFC frame destruction state.
     */
    public void onDestroy() {
    }

    /**
     * Native: CFrameWnd::OnSize @005B5A84.
     * Java port status: skipped stock MFC default/recalc behavior; Java records the modeled client size for recovered
     * CMainWindow layout handlers.
     */
    public void onSize(int nType, int cx, int cy) {
        CWndState.setClientSize(this, cx, cy);
    }
}
