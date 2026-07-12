package ua.millfreedom.rom2.model.actiondata;

import ua.millfreedom.rom2.GameCharsets;
import ua.millfreedom.rom2.model.action.CGameAction;
import ua.millfreedom.rom2.model.action.UnitChangeAction;
import ua.millfreedom.rom2.model.action.UnitChangeAction_6C;
import ua.millfreedom.rom2.model.action.UnitChangeAction_6E;
import ua.millfreedom.rom2.model.action.UnitChangeAction_6F;
import ua.millfreedom.rom2.model.action.UnitChangeAction_70;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

/**
 * Native support extracted from UnitChangeAction packet packing routines.
 */
public final class UnitChangePayloads {
    public static final int NATIVE_PAYLOAD_CAPACITY = 0x400;
    public static final int NATIVE_SIZE2_OBJECT_OFFSET = CGameAction.BODY_OFFSET;
    public static final int NATIVE_SIZE1_OBJECT_OFFSET = CGameAction.BODY_OFFSET + Integer.BYTES;
    public static final int NATIVE_HEADER_OBJECT_OFFSET = CGameAction.BASE_OFFSET + 0x0E;
    public static final int NATIVE_FLAGS_OBJECT_OFFSET = NATIVE_HEADER_OBJECT_OFFSET + Short.BYTES;
    public static final int NATIVE_PAYLOAD_OBJECT_OFFSET = CGameAction.BASE_OFFSET + 0x14;

    private static final int[] PAYLOAD_SIZES = {
            2, 2, 5, 4, 3, 2, 1, 2,
            4, 4, 4, 4, 4, 2, 2, 1,
            2, 2, 2, 1, 5, 0, 0, 0,
            5, 16, 0, 0, 0, 8, 0, 24
    };

    /**
     * not ported.
     */
    private UnitChangePayloads() {
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    public static void resetForNetUpdate(UnitChangeAction action, int playerId, int tokenIdLowWord) {
        action.playerID.set(playerId & 0xFFFF);
        action.ID.set(0);
        action.size2.set(0);
        action.size1.set(0);
        action.size1Start.set(tokenIdLowWord & 0xFFFF);
        action.flags.set(0);
        action.payload.fill((byte) 0);
    }

    /**
     * Native support extracted from UnitChangeAction::appendByteField @0050CF04.
     * Fully ported support helper.
     */
    public static void appendByteField(UnitChangeAction action, int mask, int value) {
        markFlag(action, mask);
        int offset = action.size2.get();
        action.payload.setByteAt(offset, value);
        action.size2.set(offset + Byte.BYTES);
    }

    /**
     * Native support extracted from UnitChangeAction::appendWordField @0050CF3F.
     * Fully ported support helper.
     */
    public static void appendWordField(UnitChangeAction action, int mask, int value) {
        markFlag(action, mask);
        int offset = action.size2.get();
        action.payload.setUnsignedWordAt(offset, value);
        action.size2.set(offset + Short.BYTES);
    }

    /**
     * Native support extracted from UnitChangeAction::appendDwordField @0050CF7C.
     * Fully ported support helper.
     */
    public static void appendDwordField(UnitChangeAction action, int mask, int value) {
        markFlag(action, mask);
        int offset = action.size2.get();
        action.payload.setIntAt(offset, value);
        action.size2.set(offset + Integer.BYTES);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    public static void setDisplayName(UnitChangeAction action, String displayName) {
        markFlag(action, UnitDirtyFlags.DISPLAY_NAME.value);
        int offset = action.size2.get();
        action.payload.fillAt(offset, 0x18, (byte) 0);
        byte[] encoded = displayName.getBytes(GameCharsets.GAME_TEXT);
        action.payload.setBytesAt(offset, encoded, 0, Math.min(encoded.length, 0x17));
        action.size2.set(offset + 0x18);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    public static void markFlag(UnitChangeAction action, int mask) {
        action.flags.set(action.flags.get() | mask);
    }

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6 and CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    public static UnitChangeAction toResolvedUnitChangeAction(UnitChangeAction action) {
        return switch (GameActionId.fromId(resolveWireActionId(action))) {
            case UNIT_CHANGE_ACTION_6C -> new UnitChangeAction_6C(action);
            case UNIT_CHANGE_ACTION_6E -> new UnitChangeAction_6E(action);
            case UNIT_CHANGE_ACTION_6F -> new UnitChangeAction_6F(action);
            case UNIT_CHANGE_ACTION_70 -> new UnitChangeAction_70(action);
            default ->
                    throw new IllegalStateException("Unsupported UnitChangeAction size1 bucket: " + action.size1.get());
        };
    }

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6 and UnitChangeAction::GetType @0050CFB7.
     * Fully ported support helper.
     */
    public static int resolveWireActionId(UnitChangeAction action) {
        int resolvedSize1 = resolveSize1FromFlags(action.flags.get());
        action.size1.set(resolvedSize1);
        return resolveActionIdFromSize1(resolvedSize1);
    }

    /**
     * Native support extracted from UnitChangeAction::GetTotalSize @0050D01A and SIZES @005D0750.
     * Fully ported support helper.
     */
    public static int resolvePayloadSizeFromFlags(int flags) {
        int payloadSize = 0;
        int bit = 1;
        for (int size : PAYLOAD_SIZES) {
            if ((flags & bit) != UnitDirtyFlags.NONE.value) {
                payloadSize += size;
            }
            bit <<= 1;
        }
        return payloadSize;
    }

    /**
     * Native support extracted from UnitChangeAction::ReadPayload @0050CE4B.
     * Fully ported support helper.
     */
    public static int resolveSize1FromActionId(int actionId) {
        return switch (GameActionId.fromId(actionId)) {
            case UNIT_CHANGE_ACTION_6E -> 1;
            case UNIT_CHANGE_ACTION_6F -> 2;
            case UNIT_CHANGE_ACTION_70 -> 3;
            case UNIT_CHANGE_ACTION_6C -> 4;
            default -> throw new IllegalArgumentException(
                    "Unsupported UnitChangeAction packet id: 0x" + Integer.toHexString(actionId)
            );
        };
    }

    /**
     * Native support extracted from UnitChangeAction::GetType @0050CFB7.
     * Fully ported support helper.
     */
    private static int resolveSize1FromFlags(int flags) {
        if (Integer.compareUnsigned(flags, 0x00000100) < 0) {
            return 1;
        }
        if (Integer.compareUnsigned(flags, 0x00010000) < 0) {
            return 2;
        }
        if (Integer.compareUnsigned(flags, 0x01000000) < 0) {
            return 3;
        }
        return 4;
    }

    /**
     * Native support extracted from UnitChangeAction::GetType @0050CFB7.
     * Fully ported support helper.
     */
    private static int resolveActionIdFromSize1(int size1) {
        return switch (size1) {
            case 1 -> UnitChangeAction_6E.ACTION_ID;
            case 2 -> UnitChangeAction_6F.ACTION_ID;
            case 3 -> UnitChangeAction_70.ACTION_ID;
            case 4 -> UnitChangeAction_6C.ACTION_ID;
            default -> throw new IllegalStateException("Unsupported UnitChangeAction size1 bucket: " + size1);
        };
    }
}
