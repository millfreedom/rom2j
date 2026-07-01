package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.sound.Sound;

/**
 * Native class: DruidShopDialogVisualObject.
 * Purpose: concrete druid shop dialog recovered from `shop_druid` strings and druid-only shop ambient assets.
 */
public class DruidShopDialogVisualObject extends ShopDialogVisualObject {
    public static final int NATIVE_SIZE = 0x190; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String SFX_TOWN_DRUID_SHOP_DOTDEL_WAV = "sfx/town_druid/shop/dotdel.wav";
    private static final String SFX_TOWN_DRUID_SHOP_DIN2_WAV = "sfx/town_druid/shop/din2.wav";
    private static final String SFX_TOWN_DRUID_SHOP_DDRUID5_WAV = "sfx/town_druid/shop/ddruid5.wav";
    private static final String SFX_TOWN_DRUID_SHOP_DDRUID6_WAV = "sfx/town_druid/shop/ddruid6.wav";
    private static final String SFX_TOWN_DRUID_INN_DFOREST2_WAV = "sfx/town_druid/inn/dforest2.wav";
    private static final String SFX_TOWN_DRUID_INN_DBIRD4_WAV = "sfx/town_druid/inn/dbird4.wav";
    private static final String SFX_TOWN_DRUID_INN_DBIRD41_WAV = "sfx/town_druid/inn/dbird41.wav";
    private static final String SFX_TOWN_DRUID_INN_DBIRD42_WAV = "sfx/town_druid/inn/dbird42.wav";
    private static final String SFX_TOWN_DRUID_SHOP_DTOOLS1_WAV = "sfx/town_druid/shop/dtools1.wav";
    private static final String SFX_TOWN_DRUID_SHOP_DTOOLS2_WAV = "sfx/town_druid/shop/dtools2.wav";
    private static final String SFX_TOWN_DRUID_SHOP_DTOOLS3_WAV = "sfx/town_druid/shop/dtools3.wav";
    private static final String SFX_TOWN_DRUID_SHOP_DTOOLS4_WAV = "sfx/town_druid/shop/dtools4.wav";
    private static final String SHOP_DRUID = "shop_druid/";

    private static boolean ambientTimerGlobalsInitialized;
    private static int ambientTimerGlobalTick;

    //0x164
    public Sound birdAmbientSound0;
    //0x168
    public Sound birdAmbientSound1;
    //0x16c
    public Sound birdAmbientSound2;
    //0x170
    public Sound toolAmbientSound0;
    //0x174
    public Sound toolAmbientSound1;
    //0x178
    public Sound toolAmbientSound2;
    //0x17c
    public Sound toolAmbientSound3;
    //0x180
    public int birdAmbientTick;
    //0x184
    public int toolAmbientTick;
    //0x188
    public int nextBirdAmbientDelay;
    //0x18c
    public int nextToolAmbientDelay;

    /**
     * Native: DruidShopDialogVisualObject::DruidShopDialogVisualObject @004BDE01.
     * Full port.
     */
    public DruidShopDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CSprite256 image) {
        super(id, xLeft, yTop, xRight, yBottom, image);
    }

    /**
     * vtbl +0x78: DruidShopDialogVisualObject::InitializeBitmapCatalogDialog @004BDE3A.
     * Full port.
     */
    @Override
    public void initialize() {
        tipsPrompt = null;
        tradeTransferGrid = new ShopTransferGridVisualObject(0x3EB, 0, 0x12F, 0x1E0, 0x186, this);
        unitInventoryGrid = new ShopSelectionGridVisualObject(0x3E9, 0, 0x186, 0x1E0, 0x1E0, this);
        shopCatalogGrid = new DruidShopCatalogGridVisualObject(0x3EA, 0, 0, 0xA4, 0x12F, this);
        shopCompass = new DruidShopCompassVisualObject(0x3ED, 0xA4, 0, 0x1E0, 0x12F, this);
        ringButtons = new ShopRingButtonsVisualObject(0x3EE, 0x1D0, 0, 0x280, 0xEE, this);
        addChild(shopCatalogGrid);
        addChild(unitInventoryGrid);
        addChild(tradeTransferGrid);
        addChild(shopCompass);
        addChild(ringButtons);
        selectionInfoPanel = null;
        mapVisual = null;
        embeddedSpellPanel = null;
        noFitSound = null;
        shopStep1Sound = null;
        shopStep2Sound = null;
        shopBreathSound = null;
        shopDepartSound = null;
        buySound = null;
        sellSound = null;
        shopEnterSound = null;
        shopStartSound = null;
        shopTurn1Sound = null;
        shopTurn2Sound = null;
        shopInShopSound = null;
        outSound = null;
        undoSound = null;
        backInventoryGreenBitmap = null;
        backInventoryBlueBitmap = null;
        backInventorySelectedBitmap = null;
        playerItemSprite = null;
        shopItemSprite = null;
    }

    /**
     * vtbl +0x80: DruidShopDialogVisualObject::ShowDialog @004BE6B0.
     * Full port. The shared parent flow calls initializeVariantAmbientTimers at the native druid timer point.
     */
    @Override
    public void showDialog() {
        super.showDialog();
    }

    /**
     * Native support extracted from DruidShopDialogVisualObject::ShowDialog @004BE6B0.
     * Full support port of the druid ambient timer initialization tail.
     */
    @Override
    protected void initializeVariantAmbientTimers() {
        nextBirdAmbientDelay = Utils.randInclusive(2000) + 2000;
        birdAmbientTick = currentTick();
        nextToolAmbientDelay = Utils.randInclusive(2000) + 2000;
        toolAmbientTick = currentTick();
    }

    /**
     * vtbl +0x88: DruidShopDialogVisualObject::UpdateShopAmbientSound @004BE1AC.
     * Full port.
     */
    @Override
    public void updateShopAmbientSound() {
        if (!ambientTimerGlobalsInitialized) {
            ambientTimerGlobalsInitialized = true;
            ambientTimerGlobalTick = currentTick();
        }

        int currentTick = currentTick();
        Utils.randInclusive(30000);

        if ((shopCompass.stateFlags & 0x10) != 0
                && shopCompass.centerAnimationFrame == 1) {
            playPointerSound(shopTurn1Sound);
        }
        if ((shopCompass.stateFlags & 0x20) != 0
                && shopCompass.centerAnimationFrame == 1) {
            playPointerSound(shopTurn2Sound);
        }

        if (nextBirdAmbientDelay < currentTick - birdAmbientTick) {
            switch (Utils.randInclusive(3)) {
                case 0 -> playPointerSound(birdAmbientSound0);
                case 1 -> playPointerSound(birdAmbientSound1);
                case 2 -> playPointerSound(birdAmbientSound2);
                default -> {
                }
            }
            nextBirdAmbientDelay = Utils.randInclusive(2000) + 2000;
            birdAmbientTick = currentTick();
        }

        if (nextToolAmbientDelay < currentTick - toolAmbientTick) {
            switch (Utils.randInclusive(4)) {
                case 0 -> playPointerSound(toolAmbientSound0);
                case 1 -> playPointerSound(toolAmbientSound1);
                case 2 -> playPointerSound(toolAmbientSound2);
                case 3 -> playPointerSound(toolAmbientSound3);
                default -> {
                }
            }
            nextToolAmbientDelay = Utils.randInclusive(2000) + 2000;
            toolAmbientTick = currentTick();
        }
    }

    /**
     * vtbl +0x8C: DruidShopDialogVisualObject::LoadCatalogSounds @004BE3DF.
     * Full port.
     */
    @Override
    public void loadCatalogSounds() {
        releaseCatalogSounds();
        noFitSound = loadSound(noFitSound, "sfx/town/shop/nofit.wav");
        shopDepartSound = loadSound(shopDepartSound, SFX_TOWN_DRUID_SHOP_DOTDEL_WAV);
        buySound = loadSound(buySound, "sfx/town/buy.wav");
        sellSound = loadSound(sellSound, "sfx/town/sell.wav");
        shopEnterSound = loadSound(shopEnterSound, SFX_TOWN_DRUID_SHOP_DIN2_WAV);
        shopTurn1Sound = loadSound(shopTurn1Sound, SFX_TOWN_DRUID_SHOP_DDRUID5_WAV);
        shopTurn2Sound = loadSound(shopTurn2Sound, SFX_TOWN_DRUID_SHOP_DDRUID6_WAV);
        shopInShopSound = loadSound(shopInShopSound, SFX_TOWN_DRUID_INN_DFOREST2_WAV);
        outSound = loadSound(outSound, "sfx/out.wav");
        undoSound = loadSound(undoSound, "sfx/undo.wav");
        birdAmbientSound0 = loadSound(birdAmbientSound0, SFX_TOWN_DRUID_INN_DBIRD4_WAV);
        birdAmbientSound1 = loadSound(birdAmbientSound1, SFX_TOWN_DRUID_INN_DBIRD41_WAV);
        birdAmbientSound2 = loadSound(birdAmbientSound2, SFX_TOWN_DRUID_INN_DBIRD42_WAV);
        toolAmbientSound0 = loadSound(toolAmbientSound0, SFX_TOWN_DRUID_SHOP_DTOOLS1_WAV);
        toolAmbientSound1 = loadSound(toolAmbientSound1, SFX_TOWN_DRUID_SHOP_DTOOLS2_WAV);
        toolAmbientSound2 = loadSound(toolAmbientSound2, SFX_TOWN_DRUID_SHOP_DTOOLS3_WAV);
        toolAmbientSound3 = loadSound(toolAmbientSound3, SFX_TOWN_DRUID_SHOP_DTOOLS4_WAV);
    }

    /**
     * vtbl +0x90: DruidShopDialogVisualObject::ReleaseCatalogSounds @004BE579.
     * Full port.
     */
    @Override
    public void releaseCatalogSounds() {
        noFitSound = releaseSound(noFitSound);
        shopDepartSound = releaseSound(shopDepartSound);
        buySound = releaseSound(buySound);
        sellSound = releaseSound(sellSound);
        shopEnterSound = releaseSound(shopEnterSound);
        shopTurn1Sound = releaseSound(shopTurn1Sound);
        shopTurn2Sound = releaseSound(shopTurn2Sound);
        shopInShopSound = releaseSound(shopInShopSound);
        outSound = releaseSound(outSound);
        undoSound = releaseSound(undoSound);
        birdAmbientSound0 = releaseSound(birdAmbientSound0);
        birdAmbientSound1 = releaseSound(birdAmbientSound1);
        birdAmbientSound2 = releaseSound(birdAmbientSound2);
        toolAmbientSound0 = releaseSound(toolAmbientSound0);
        toolAmbientSound1 = releaseSound(toolAmbientSound1);
        toolAmbientSound2 = releaseSound(toolAmbientSound2);
        toolAmbientSound3 = releaseSound(toolAmbientSound3);
    }

    /**
     * vtbl +0x94: DruidShopDialogVisualObject::GetShopResourceDirectory @004C3410.
     * Full port.
     */
    @Override
    public String getShopResourceDirectory() {
        return SHOP_DRUID;
    }

    /**
     * Native support thunk: LoadSound @004384F0.
     * Full support port.
     */
    private static Sound loadSound(Sound sound, String resourcePath) {
        releaseSound(sound);
        return new Sound(resourcePath);
    }

    /**
     * Native support thunk: DeleteSound @00438480.
     * Full support port.
     */
    private static Sound releaseSound(Sound sound) {
        if (sound != null) {
            sound.release();
        }
        return null;
    }

    /**
     * Native support thunk: Sound::PlayPointer @00438570.
     * Full support port.
     */
    private static void playPointerSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Native support extracted from DruidShopDialogVisualObject::ShowDialog @004BE6B0 and
     * DruidShopDialogVisualObject::UpdateShopAmbientSound @004BE1AC.
     * Full support port for `timeGetTime` call sites.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }

}
