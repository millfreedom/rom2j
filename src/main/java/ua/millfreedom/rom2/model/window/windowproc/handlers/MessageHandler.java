package ua.millfreedom.rom2.model.window.windowproc.handlers;

@FunctionalInterface
public interface MessageHandler<T> {
    int handle(T wnd, Object wParam, Object lParam);
}
