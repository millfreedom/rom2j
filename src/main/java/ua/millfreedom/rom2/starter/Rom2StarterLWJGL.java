package ua.millfreedom.rom2.starter;

import ua.millfreedom.rom2.CMainApp;

/**
 * Compatibility launcher for the historical Java entry-point class.
 * not ported.
 */
public final class Rom2StarterLWJGL {
    /**
     * Java utility constructor.
     * not ported.
     */
    private Rom2StarterLWJGL() {
    }

    /**
     * Delegates the legacy entry point to the recovered CMainApp shell.
     * not ported.
     */
    public static void main(String[] args) throws Exception {
        new CMainApp(args).run();
    }
}
