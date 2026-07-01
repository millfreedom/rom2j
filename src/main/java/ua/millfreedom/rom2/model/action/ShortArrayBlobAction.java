package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;

/**
 * Variable-size short-array payload packet.
 */
public class ShortArrayBlobAction extends CGameAction {
    public static final ShortArrayBlobAction global = new ShortArrayBlobAction();

    //0x0A
    public final Property<Integer> shortValueCount = i32(BODY_OFFSET);
    //0x0E
    public final Property<byte[]> shortValues = u16Array(
            BODY_OFFSET + Integer.BYTES,
            () -> Math.max(shortValueCount.get(), 0)
    );

    /**
     * Native: ShortArrayBlobAction::ShortArrayBlobAction @0050C726.
     * Fully ported.
     */
    public ShortArrayBlobAction() {
        super();
        shortValueCount.set(0);
        shortValues.set(new byte[0]);
    }

    /**
     * Native: ShortArrayBlobAction::ShortArrayBlobAction @0050C745.
     * Fully ported.
     */
    public ShortArrayBlobAction(ShortArrayBlobAction from) {
        super();
        int copySize = shortValueCount.get() * Short.BYTES + 5;
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, copySize), 0, copySize);
    }

    /**
     * vtbl +0x04: ShortArrayBlobAction::Clone @00541940.
     * Fully ported.
     */
    @Override
    public ShortArrayBlobAction Clone() {
        return new ShortArrayBlobAction(this);
    }

    /**
     * vtbl +0x08: ShortArrayBlobAction::WritePayload @0050C787.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        return target.Write(this, ID_OFFSET, shortValueCount.get() * Short.BYTES + 5);
    }

    /**
     * vtbl +0x10: ShortArrayBlobAction::GetPayloadSize @005419C0.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return shortValueCount.get() * Short.BYTES + 5;
    }

    /**
     * vtbl +0x0C: ShortArrayBlobAction::Action2 @0050C7AE.
     * Port name: ReadPayload.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        boolean result = source.Read(this, BODY_OFFSET, Integer.BYTES);
        if (shortValueCount.get() > 0) {
            result = source.Read(this, shortValues.startPosition(), shortValueCount.get() << 1);
        }
        return result;
    }
}
