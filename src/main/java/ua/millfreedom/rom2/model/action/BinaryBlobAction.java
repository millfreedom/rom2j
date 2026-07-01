package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;

/**
 * Variable-size binary blob packet (IDs 0xBA / 0xBE family).
 */
public class BinaryBlobAction extends CGameAction {
    public static final BinaryBlobAction global = new BinaryBlobAction();

    //0x0A
    public final Property<Integer> payloadSize = i32(BODY_OFFSET);
    //0x0E
    public final Property<byte[]> data = bytes(
            BODY_OFFSET + Integer.BYTES,
            () -> Math.max(payloadSize.get(), 0)
    );

    /**
     * Native: BinaryBlobAction::BinaryBlobAction @0050C663.
     * Fully ported.
     */
    public BinaryBlobAction() {
        super();
        payloadSize.set(0);
        data.set(new byte[0]);
    }

    /**
     * Native: BinaryBlobAction::BinaryBlobAction @0050C682.
     * Fully ported.
     */
    public BinaryBlobAction(BinaryBlobAction from) {
        super();
        int copySize = payloadSize.get() + 5;
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, copySize), 0, copySize);
    }

    /**
     * vtbl +0x04: BinaryBlobAction::Clone @00541870.
     * Fully ported.
     */
    @Override
    public BinaryBlobAction Clone() {
        return new BinaryBlobAction(this);
    }

    /**
     * vtbl +0x08: BinaryBlobAction::WritePayload @0050C6C3.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        return target.Write(this, ID_OFFSET, payloadSize.get() + 5);
    }

    /**
     * vtbl +0x10: BinaryBlobAction::GetPayloadSize @005418F0.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return payloadSize.get() + 5;
    }

    /**
     * vtbl +0x0C: BinaryBlobAction::Action2 @0050C6E9.
     * Port name: ReadPayload.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        boolean result = source.Read(this, BODY_OFFSET, Integer.BYTES);
        if (payloadSize.get() > 0) {
            result = source.Read(this, BODY_OFFSET + Integer.BYTES, payloadSize.get());
        }
        return result;
    }
}
