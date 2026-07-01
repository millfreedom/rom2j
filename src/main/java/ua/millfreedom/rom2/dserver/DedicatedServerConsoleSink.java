package ua.millfreedom.rom2.dserver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Java support sink for headless dedicated-server operator messages.
 * not ported.
 */
public final class DedicatedServerConsoleSink {
    private static final List<Consumer<String>> serverMessageSinks = new CopyOnWriteArrayList<>();
    private static final Consumer<String> STDOUT_SINK = message -> System.out.println("[server] " + message);
    private static boolean stdoutSinkInstalled;

    /**
     * Java utility constructor.
     * not ported.
     */
    private DedicatedServerConsoleSink() {
    }

    /**
     * Java support boundary for routing dedicated-server log lines to stdout.
     * not ported.
     */
    public static void installStdoutSink() {
        if (!stdoutSinkInstalled) {
            serverMessageSinks.add(STDOUT_SINK);
            stdoutSinkInstalled = true;
        }
    }

    /**
     * Java support boundary for adding an operator UI sink alongside stdout/probe sinks.
     * not ported.
     */
    public static AutoCloseable addSink(Consumer<String> sink) {
        serverMessageSinks.add(sink);
        return () -> serverMessageSinks.remove(sink);
    }

    /**
     * Java support boundary for tests/probes that need to silence the headless sink.
     * not ported.
     */
    public static void clearSink() {
        serverMessageSinks.clear();
        stdoutSinkInstalled = false;
    }

    /**
     * Java support boundary called from Global::PushMessage @0043A0A8 after the native dedicated-mode gate.
     * not ported.
     */
    public static void emitServerMessage(String message) {
        for (Consumer<String> sink : serverMessageSinks) {
            sink.accept(message);
        }
    }
}
