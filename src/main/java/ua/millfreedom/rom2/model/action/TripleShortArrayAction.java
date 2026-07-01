package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;

/**
 * Fixed-size packet with a two-byte payload prefix and three 26-word arrays.
 */
public class TripleShortArrayAction extends CGameAction {
    private static final int SHORT_ARRAY_ELEMENT_COUNT = 0x1A;
    private static final int WIRE_SIZE = 0x9F;
    private static final int BODY_SIZE = WIRE_SIZE - 1;

    public static final TripleShortArrayAction global = new TripleShortArrayAction();

    //0x0A
    public final Property<byte[]> payloadPrefix = bytes(BODY_OFFSET, Short.BYTES);
    //0x0C
    public final Property<byte[]> firstShortValues = u16Array(BODY_OFFSET + Short.BYTES, SHORT_ARRAY_ELEMENT_COUNT);
    //0x40
    public final Property<byte[]> secondShortValues = u16Array(
            BODY_OFFSET + Short.BYTES + SHORT_ARRAY_ELEMENT_COUNT * Short.BYTES,
            SHORT_ARRAY_ELEMENT_COUNT
    );
    //0x74
    public final Property<byte[]> thirdShortValues = u16Array(
            BODY_OFFSET + Short.BYTES + SHORT_ARRAY_ELEMENT_COUNT * Short.BYTES * 2,
            SHORT_ARRAY_ELEMENT_COUNT
    );

    /**
     * Native: TripleShortArrayAction::TripleShortArrayAction @0050BCEE.
     * Fully ported.
     */
    public TripleShortArrayAction() {
        super();
        firstShortValues.fill((byte) 0);
        secondShortValues.fill((byte) 0);
        thirdShortValues.fill((byte) 0);
    }

    /**
     * vtbl +0x08: TripleShortArrayAction::WritePayload @0050BD50.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        return target.Write(this, ID_OFFSET, WIRE_SIZE);
    }

    /**
     * vtbl +0x0C: TripleShortArrayAction::ReadPayload @0050BD79.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        return source.Read(this, BODY_OFFSET, BODY_SIZE);
    }

    /**
     * vtbl +0x10: TripleShortArrayAction::GetPayloadSize @00541070.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return WIRE_SIZE;
    }
}
