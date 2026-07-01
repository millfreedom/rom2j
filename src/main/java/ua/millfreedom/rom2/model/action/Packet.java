package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;

/**
 * Java contract for native game-action packet vtable slots.
 */
public interface Packet {
    /**
     * Native packet vtable clone slot; mirrors CGameAction::Clone @00540490 and descendants.
     */
    Packet Clone();

    /**
     * Native packet vtable write slot; mirrors CGameAction::WritePayload @0050BAE5 and descendants.
     */
    boolean WritePayload(CBufferManager target);

    /**
     * Native packet vtable read slot; mirrors CGameAction::ReadPayload @0050BB15 and descendants.
     */
    boolean ReadPayload(CBufferManager source);

    /**
     * Native packet vtable size slot; mirrors CGameAction::GetPayloadSize @00540510 and descendants.
     */
    int GetPayloadSize();
}
