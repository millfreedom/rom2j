package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.model.enums.MessageCodes;

/**
 * Native support interface for handler dialogs that expose their close reason.
 * not ported.
 */
public interface DialogResultCarrier {
    /**
     * Native: HandlerVisualObject::GetClosedReason @00437F80.
     */
    MessageCodes getClosedReason();
}
