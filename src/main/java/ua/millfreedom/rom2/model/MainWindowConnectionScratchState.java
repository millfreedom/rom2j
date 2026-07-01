package ua.millfreedom.rom2.model;

/**
 * Native type: MainWindowConnectionScratchState.
 * Purpose: temporary main-window holder for direct-address login, server-list routing, and pending session connection state.
 */
public class MainWindowConnectionScratchState {
    /**
     * Native: MainWindowConnectionScratchState::MainWindowConnectionScratchState @00492E10.
     * Java port status: fully ported.
     */
    public MainWindowConnectionScratchState() {
    }

    //0x0 - Native CString login name used by CMainWindow::sendDirectAddressLoginRequest @0040D6F6.
    public String loginName = "";
    //0x4 - Native CString login password used by CMainWindow::sendDirectAddressLoginRequest @0040D6F6.
    public String loginPassword = "";
    //0x8 - Native CString direct-address target read by CMainWindow::connectToServerAddress @0048E90F.
    public String directAddress = "";
    //0xc - Native DWORD accepted by FIXED_DWORD_ACTION_0E and reused as the character-file owner id.
    public int acceptedCharacterFileOwnerId;
    //0x10 - Native flag set from HatServerListDialogVisualObject::OnHeaderDialogAction @004492F0 for web-page server-list flow.
    public int serverListSourceIsWebPage;
    //0x14 - Native CString passed to CLlDriver::PrepareForConnect from CMainWindow::WindowProc @004852D8.
    public String pendingSessionConnectionString = "";

    // Java support, not a native field.
    public boolean directAddressLoginAccepted;
}
