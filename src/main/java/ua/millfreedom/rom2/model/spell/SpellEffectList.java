package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SpellEffectList implements MfcSerializable, Iterable<SpellEffect> {
    //0x04
    public final CustomList<SpellEffect> spellEffects = new CustomList<>(SpellEffect.class);

    /**
     * Native: SpellEffectList::SpellEffectList @0052C1E9.
     * Fully ported.
     */
    public SpellEffectList() {
    }

    /**
     * Native: SpellEffectList::Serialize @0052D5CB.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        ar.serialize(spellEffects);
    }

    /**
     * not ported. Java list delegation for native callers that access SpellEffectList::spellEffects.
     */
    public boolean add(SpellEffect spellEffect) {
        return spellEffects.add(spellEffect);
    }

    /**
     * not ported. Java list delegation for native callers that access SpellEffectList::spellEffects.
     */
    public boolean remove(SpellEffect spellEffect) {
        return spellEffects.remove(spellEffect);
    }

    /**
     * not ported. Java list delegation for native callers that access SpellEffectList::spellEffects.
     */
    public void clear() {
        spellEffects.clear();
    }

    /**
     * not ported. Java list delegation for native callers that access SpellEffectList::spellEffects.
     */
    @Override
    public Iterator<SpellEffect> iterator() {
        return spellEffects.iterator();
    }

    /**
     * Native: SpellEffectList::Update @0052C279.
     * Fully ported.
     */
    public void update() {
        for (SpellEffect spellEffect : new ArrayList<>(spellEffects)) {
            if (spellEffect.sourceCaster != null && spellEffect.sourceCaster.owner == null) {
                spellEffect.sourceCaster = null;
            }
            publishNewlyVisibleAreaEffectIfNeeded(spellEffect);
            spellEffect.update();
            if (spellEffect.completedFlag != 0) {
                remove(spellEffect);
            }
        }
    }

    /**
     * Native support extracted from SpellEffectList::Update @0052C279 and
     * CServerApp::sendSpellEffectStateAction @005045A5.
     * Fully ported.
     */
    private static void publishNewlyVisibleAreaEffectIfNeeded(SpellEffect spellEffect) {
        if (spellEffect.visiblePlayerMask == spellEffect.lastPublishedVisiblePlayerMask) {
            return;
        }
        if (spellEffect instanceof AreaEffect areaEffect && Globals.gameServer.networkSessionActive != 0) {
            for (int playerIndex = 0; playerIndex < 0x10; playerIndex++) {
                int playerMask = 1 << playerIndex;
                if ((spellEffect.visiblePlayerMask & playerMask) != 0
                        && (spellEffect.lastPublishedVisiblePlayerMask & playerMask) == 0) {
                    Player player = Globals.gameServer.playerList.getPlayerById(playerIndex + 0x10);
                    if (player != null) {
                        CServerApp.sendSpellEffectStateAction(areaEffect, 1);
                    }
                }
            }
            areaEffect.lastPublishedVisiblePlayerMask = areaEffect.visiblePlayerMask;
        }
    }

    /**
     * vtbl +0x00: SpellEffectList::RestoreContext @0052D5E7.
     * Fully ported.
     */
    public void restoreContext() {
        for (SpellEffect spellEffect : spellEffects) {
            spellEffect.restoreContext();
        }
    }
}
