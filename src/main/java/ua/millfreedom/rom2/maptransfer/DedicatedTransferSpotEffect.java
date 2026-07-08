package ua.millfreedom.rom2.maptransfer;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.spell.AreaEffect;

/**
 * Java-only permanent area-effect marker for a dedicated map-transfer source cell.
 * not ported.
 */
public final class DedicatedTransferSpotEffect extends AreaEffect {
    private static final int TRANSFER_SPOT_SPELL_ID = SpellId.TELEPORT.id;
    private static final int TRANSFER_SPOT_PROJECTILE_ID = TRANSFER_SPOT_SPELL_ID * 2 + 8;
    private static final int TRANSFER_SPOT_PROJECTILE_PHASES = 21;
    private static final int TRANSFER_SPOT_VISUAL_PULSE_INTERVAL_TICKS = 11;
    private static final int TRANSFER_SPOT_EFFECT_TYPE_ID = TRANSFER_SPOT_PROJECTILE_ID;

    // Java support, not a native field.
    private final boolean neverExpires;
    // Java support, not a native field.
    private int visualPulseTicks;

    /**
     * Java support constructor for a dedicated transfer-zone spot marker.
     * not ported.
     */
    public DedicatedTransferSpotEffect(TransferZone transferZone, boolean neverExpires) {
        super();
        this.neverExpires = neverExpires;
        m_pTargetHandle.initFromBytes(transferZone.sourceX(), transferZone.sourceY(), Globals.worldMap);
        key = TRANSFER_SPOT_SPELL_ID;
        typeID = TRANSFER_SPOT_EFFECT_TYPE_ID;
        payload = null;
        mode = 1;
        radiusLengthHalf = 0;
        durationTicks = 0xFFFF;
        damageAttributionEnabled = 0;
        visiblePlayerMask = -1;
        lastPublishedVisiblePlayerMask = -1;
    }

    /**
     * Java support predicate for transfer-zone marker expiration policy.
     * not ported.
     */
    public boolean neverExpires() {
        return neverExpires;
    }

    /**
     * Java support publisher for the transfer-zone spot's Teleport projectile visual.
     * not ported.
     */
    public void publishTransferSpotVisual() {
        CServerApp.sendPointProjectileVisualAction(
                m_pTargetHandle,
                TRANSFER_SPOT_PROJECTILE_ID,
                TRANSFER_SPOT_PROJECTILE_PHASES
        );
        visualPulseTicks = TRANSFER_SPOT_VISUAL_PULSE_INTERVAL_TICKS;
    }

    /**
     * vtbl +0x18: Java-only override of AreaEffect::update for permanent transfer spot markers.
     * not ported.
     */
    @Override
    public Object update() {
        if (neverExpires) {
            if (visualPulseTicks <= 0) {
                publishTransferSpotVisual();
            } else {
                visualPulseTicks--;
            }
            return null;
        }
        return super.update();
    }

    /**
     * vtbl +0x38: Java-only no-op payload application for visual-only transfer spot markers.
     * not ported.
     */
    @Override
    public void applyPayloadToObject(Token target) {
        // Transfer spots are visual markers only.
    }
}
