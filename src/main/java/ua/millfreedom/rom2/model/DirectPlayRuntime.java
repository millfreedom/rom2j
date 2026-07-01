package ua.millfreedom.rom2.model;

/**
 * Native support for global DirectPlay runtime availability probes used by multiplayer setup paths.
 */
public final class DirectPlayRuntime {
    /**
     * Java utility constructor.
     * not ported.
     */
    private DirectPlayRuntime() {
    }

    /**
     * Native: Global::IsDirectPlayRuntimeAvailable @00450420.
     * Fully ported. Native disassembly returns TRUE unconditionally and has no side effects.
     */
    public static boolean isAvailable() {
        return true;
    }

    /**
     * Native support extracted from ignored Global::IsDirectPlayRuntimeAvailable @00450420 call sites in
     * CenteredDialogContextArrayVisualObject::OnMessage @004463C4 and CMainWindow::WindowProc @004852D8.
     * Fully ported. The native return is ignored at these call sites, and the native stub has no side effects.
     */
    public static void probeAvailabilityBoundary() {
    }
}
