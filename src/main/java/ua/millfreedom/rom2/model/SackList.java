package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SackList implements MfcSerializable, Iterable<Sack> {
    //0x0
    public final CustomList<Sack> sacks = new CustomList<>(Sack.class);

    /**
     * Native: SackList::New @0053AB10.
     * Fully ported.
     */
    public SackList() {
    }

    /**
     * Native: SackList::serialize @0052D5B2.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        ar.serialize(sacks);
    }

    /**
     * Native: SackList::addTail @00544ED0.
     * Fully ported.
     */
    public void addTail(Sack sack) {
        sacks.add(sack);
    }

    /**
     * Native support extracted from SackList::remove @0053CBF0 and Unit::Update @0050F12C.
     */
    public void remove(Sack sack) {
        sacks.remove(sack);
    }

    /**
     * not ported.
     */
    @Override
    public Iterator<Sack> iterator() {
        return sacks.iterator();
    }

    /**
     * Native: SackList::createOrMergeSackAtTargetAndNotify @0052ACF6.
     * Fully ported.
     */
    public void createOrMergeSackAtTargetAndNotify(
            TargetHandle targetHandle,
            Inventory inventory,
            int gold,
            int ownerOnlyFlag
    ) {
        if (createOrMergeSackAtTarget(targetHandle, inventory, gold, ownerOnlyFlag)) {
            Sack sack = Globals.worldMap.findSackAtTargetHandle(targetHandle);
            CServerApp.notifyStateChanged(sack);
        }
    }

    /**
     * Native: SackList::createOrMergeSackAtTarget @0052AB07.
     * Fully ported.
     */
    public boolean createOrMergeSackAtTarget(TargetHandle targetHandle, Inventory inventory, int gold, int ownerOnlyFlag) {
        Sack sack = findAtTargetHandle(targetHandle);
        if (sack == null) {
            sack = inventory == null
                    ? new Sack().initAtTarget(targetHandle)
                    : new Sack().initAtTargetWithInventory(targetHandle, inventory);
            if (!attachSackToWorld(sack)) {
                return false;
            }
        } else if (inventory != null) {
            remove(sack);
            addTail(sack);
            sack.inventory.drainItemsFrom(inventory);
        }

        sack.gold += gold;
        sack.ownerOnlyFlag = sack.ownerOnlyFlag == 0 && ownerOnlyFlag == 0 ? 0 : 1;
        int previousLogBucket = sack.price < 1 ? -1 : (int) Math.log10(sack.price);
        sack.recalculateTotalValue();
        double currentLogValue = Math.log10(sack.price);
        if ((double) previousLogBucket != currentLogValue) {
            if (Globals.gameServer.networkSessionActive == 0) {
                CServerApp.notifyStateChanged(sack);
            } else {
                sack.visibilityRefreshMask = 0xFFFF;
            }
        }
        return true;
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and SackList::createOrMergeSackAtTarget @0052AB07.
     */
    public Sack findAtTargetHandle(TargetHandle targetHandle) {
        return Globals.worldMap.findSackAtTargetHandle(targetHandle);
    }

    /**
     * Native: SackList::clearVisibilityMaskForPlayer @0052ADA6.
     * Fully ported.
     */
    public void clearVisibilityMaskForPlayer(Player player) {
        for (Sack sack : sacks) {
            sack.word &= ~player.scanMask;
        }
    }

    /**
     * Fully ported. Native: SackList::updateVisibilityAndPopulation @0052A8F3.
     */
    public void updateVisibilityAndPopulation() {
        for (Sack sack : new ArrayList<>(sacks)) {
            int xBucket = (sack.m_pTargetHandle.getX() >>> 3) + 1;
            int yBucket = (sack.m_pTargetHandle.getY() >>> 3) + 1;
            int visibleMask = Globals.worldMap.unitVisibilityState0x92ECC.coarseUnitMaskGrid0x0400[xBucket][yBucket]
                    & 0xFFFF;
            sack.visiblePlayerMask = visibleMask;
            if (sack.visiblePlayerMask != sack.lastPublishedVisiblePlayerMask
                    && Globals.gameServer.networkSessionActive != 0) {
                for (int playerIndex = 0; playerIndex < 0x10; playerIndex++) {
                    int playerMask = 1 << playerIndex;
                    if ((sack.visiblePlayerMask & playerMask) != 0
                            && (sack.lastPublishedVisiblePlayerMask & playerMask) == 0) {
                        Player player = Globals.gameServer.playerList.getPlayerById(playerIndex + 0x10);
                        if (player != null
                                && (sack.needsInitialSyncForPlayer(player)
                                || (sack.visibilityRefreshMask & player.scanMask) != 0)) {
                            CServerApp.notifyStateChanged(sack, player);
                        }
                    }
                }
                sack.lastPublishedVisiblePlayerMask = sack.visiblePlayerMask;
            }
            if (Globals.gameServer.networkSessionActive != 0) {
                if (sackPopulationLimit() < size() && sack.ownerOnlyFlag == 0) {
                    Globals.worldMap.detachSack(sack);
                    CServerApp.notifySackRemoved(sack);
                    remove(sack);
                }
            }
        }
        if (Globals.gameServer.networkSessionActive != 0) {
            if (Utils.randInclusive(0x13) == 0) {
                if (size() < sackPopulationLimit()) {
                    spawnRandomWorldSacks(1);
                }
            }
        }
    }

    /**
     * Native: SackList::size @0044FC60.
     * Fully ported.
     */
    public int size() {
        return sacks.size();
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::sackPopulationLimit @00564B3F.
     */
    private static int sackPopulationLimit() {
        return Globals.worldMap.getMapWidth() / 2;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::spawnRandomWorldSacks @00564B5A.
     */
    private void spawnRandomWorldSacks(int count) {
        int maxXOffset = (Globals.worldMap.getMapWidth() - 0x14) & 0xFF;
        int maxYOffset = (Globals.worldMap.getMapHeight() - 0x14) & 0xFF;
        if (count == 0) {
            count = Math.max(sackPopulationLimit() / 2, 10);
        }
        for (int i = 0; i < count; i++) {
            Inventory inventory = new Inventory();
            for (int itemCount = Utils.randInclusive(2); itemCount > 0; itemCount--) {
                inventory.addItem(ItemAssortmentGenerator.createRandomItemForPriceRange(10, 500));
            }
            int gold = 0;
            if (inventory.size() == 0) {
                gold = Utils.randBased(100, 400);
            }
            TargetHandle targetHandle = new TargetHandle();
            targetHandle.initFromBytes(
                    Utils.randBased(10, maxXOffset),
                    Utils.randBased(10, maxYOffset),
                    Globals.worldMap
            );
            createOrMergeSackAtTarget(targetHandle, inventory, gold, 0);
        }
    }

    /**
     * Native: SackList::attachSackToWorld @0052AD3D.
     * Fully ported.
     */
    private boolean attachSackToWorld(Sack sack) {
        if (!Globals.worldMap.attachSack(sack)) {
            return false;
        }
        addTail(sack);
        return true;
    }
}
