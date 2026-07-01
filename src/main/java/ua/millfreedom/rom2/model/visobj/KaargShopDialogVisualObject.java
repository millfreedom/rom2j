package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.sound.Sound;

/**
 * Native class: KaargShopDialogVisualObject.
 * Purpose: concrete kaarg shop dialog recovered from `shop_kaarg` strings and kaarg-only shop ambient assets.
 */
public class KaargShopDialogVisualObject extends ShopDialogVisualObject {
    public static final int NATIVE_SIZE = 0x190; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String SFX_TOWN_KAARG_SHOP_KOTDEL_WAV = "sfx/town_kaarg/shop/kotdel.wav";
    private static final String SFX_TOWN_KAARG_SHOP_KIN2_WAV = "sfx/town_kaarg/shop/kin2.wav";
    private static final String SFX_TOWN_KAARG_SHOP_KMAN4_WAV = "sfx/town_kaarg/shop/kman4.wav";
    private static final String SFX_TOWN_KAARG_INN_KVOX5_WAV = "sfx/town_kaarg/inn/kvox5.wav";
    private static final String SFX_TOWN_KAARG_INN_KVOX6_WAV = "sfx/town_kaarg/inn/kvox6.wav";
    private static final String SFX_TOWN_KAARG_INN_KVOX7_WAV = "sfx/town_kaarg/inn/kvox7.wav";
    private static final String SFX_TOWN_KAARG_INN_KVOX8_WAV = "sfx/town_kaarg/inn/kvox8.wav";
    private static final String SFX_TOWN_KAARG_SHOP_KTOOLS1_WAV = "sfx/town_kaarg/shop/ktools1.wav";
    private static final String SFX_TOWN_KAARG_SHOP_KTOOLS2_WAV = "sfx/town_kaarg/shop/ktools2.wav";
    private static final String SFX_TOWN_KAARG_SHOP_KTOOLS3_WAV = "sfx/town_kaarg/shop/ktools3.wav";
    private static final String SFX_TOWN_KAARG_SHOP_KTOOLS4_WAV = "sfx/town_kaarg/shop/ktools4.wav";
    private static final String SHOP_KAARG = "shop_kaarg/";

    private static boolean ambientTimerGlobalsInitialized;
    private static int ambientTimerGlobalTick;

    //0x164
    public Sound voiceAmbientSound0;
    //0x168
    public Sound voiceAmbientSound1;
    //0x16c
    public Sound voiceAmbientSound2;
    //0x170
    public Sound toolAmbientSound0;
    //0x174
    public Sound toolAmbientSound1;
    //0x178
    public Sound toolAmbientSound2;
    //0x17c
    public Sound toolAmbientSound3;
    //0x180
    public int voiceAmbientTick;
    //0x184
    public int toolAmbientTick;
    //0x188
    public int nextVoiceAmbientDelay;
    //0x18c
    public int nextToolAmbientDelay;

    /**
     * Native: KaargShopDialogVisualObject::KaargShopDialogVisualObject @004C0017.
     * Full port.
     */
    public KaargShopDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CSprite256 image) {
        super(id, xLeft, yTop, xRight, yBottom, image);
    }

    /**
     * vtbl +0x78: KaargShopDialogVisualObject::InitializeBitmapCatalogDialog @004C0050.
     * Full port.
     */
    @Override
    public void initialize() {
        tipsPrompt = null;
        tradeTransferGrid = new ShopTransferGridVisualObject(0x3EB, 0, 0x12F, 0x1E0, 0x186, this);
        unitInventoryGrid = new ShopSelectionGridVisualObject(0x3E9, 0, 0x186, 0x1E0, 0x1E0, this);
        shopCatalogGrid = new KaargShopCatalogGridVisualObject(0x3EA, 0, 0, 0xA4, 0x12F, this);
        shopCompass = new KaargShopCompassVisualObject(0x3ED, 0xA4, 0, 0x1E0, 0x12F, this);
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
     * vtbl +0x80: KaargShopDialogVisualObject::ShowDialog @004C03C2.
     * Full port. The shared parent flow calls initializeVariantAmbientTimers at the native kaarg timer point.
     */
    @Override
    public void showDialog() {
        super.showDialog();
    }

    /**
     * Native support extracted from KaargShopDialogVisualObject::ShowDialog @004C03C2.
     * Full support port of the kaarg ambient timer initialization tail.
     */
    @Override
    protected void initializeVariantAmbientTimers() {
        nextVoiceAmbientDelay = Utils.randInclusive(2000) + 2000;
        voiceAmbientTick = currentTick();
        nextToolAmbientDelay = Utils.randInclusive(2000) + 2000;
        toolAmbientTick = currentTick();
    }

    /**
     * vtbl +0x88: KaargShopDialogVisualObject::UpdateShopAmbientSound @004C09C2.
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

        if (nextVoiceAmbientDelay < currentTick - voiceAmbientTick) {
            switch (Utils.randInclusive(3)) {
                case 0 -> playPointerSound(voiceAmbientSound0);
                case 1 -> playPointerSound(voiceAmbientSound1);
                case 2 -> playPointerSound(voiceAmbientSound2);
                default -> {
                }
            }
            nextVoiceAmbientDelay = Utils.randInclusive(2000) + 2000;
            voiceAmbientTick = currentTick();
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
     * vtbl +0x8C: KaargShopDialogVisualObject::LoadCatalogSounds @004C0BF5.
     * Full port.
     */
    @Override
    public void loadCatalogSounds() {
        releaseCatalogSounds();
        noFitSound = loadSound(noFitSound, "sfx/town/shop/nofit.wav");
        shopDepartSound = loadSound(shopDepartSound, SFX_TOWN_KAARG_SHOP_KOTDEL_WAV);
        buySound = loadSound(buySound, "sfx/town/buy.wav");
        sellSound = loadSound(sellSound, "sfx/town/sell.wav");
        shopEnterSound = loadSound(shopEnterSound, SFX_TOWN_KAARG_SHOP_KIN2_WAV);
        shopTurn1Sound = loadSound(shopTurn1Sound, SFX_TOWN_KAARG_SHOP_KMAN4_WAV);
        shopTurn2Sound = loadSound(shopTurn2Sound, SFX_TOWN_KAARG_SHOP_KMAN4_WAV);
        shopInShopSound = loadSound(shopInShopSound, SFX_TOWN_KAARG_INN_KVOX5_WAV);
        outSound = loadSound(outSound, "sfx/out.wav");
        undoSound = loadSound(undoSound, "sfx/undo.wav");
        voiceAmbientSound0 = loadSound(voiceAmbientSound0, SFX_TOWN_KAARG_INN_KVOX6_WAV);
        voiceAmbientSound1 = loadSound(voiceAmbientSound1, SFX_TOWN_KAARG_INN_KVOX7_WAV);
        voiceAmbientSound2 = loadSound(voiceAmbientSound2, SFX_TOWN_KAARG_INN_KVOX8_WAV);
        toolAmbientSound0 = loadSound(toolAmbientSound0, SFX_TOWN_KAARG_SHOP_KTOOLS1_WAV);
        toolAmbientSound1 = loadSound(toolAmbientSound1, SFX_TOWN_KAARG_SHOP_KTOOLS2_WAV);
        toolAmbientSound2 = loadSound(toolAmbientSound2, SFX_TOWN_KAARG_SHOP_KTOOLS3_WAV);
        toolAmbientSound3 = loadSound(toolAmbientSound3, SFX_TOWN_KAARG_SHOP_KTOOLS4_WAV);
    }

    /**
     * vtbl +0x90: KaargShopDialogVisualObject::ReleaseCatalogSounds @004C0D8F.
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
        voiceAmbientSound0 = releaseSound(voiceAmbientSound0);
        voiceAmbientSound1 = releaseSound(voiceAmbientSound1);
        voiceAmbientSound2 = releaseSound(voiceAmbientSound2);
        toolAmbientSound0 = releaseSound(toolAmbientSound0);
        toolAmbientSound1 = releaseSound(toolAmbientSound1);
        toolAmbientSound2 = releaseSound(toolAmbientSound2);
        toolAmbientSound3 = releaseSound(toolAmbientSound3);
    }

    /**
     * vtbl +0x94: KaargShopDialogVisualObject::GetShopResourceDirectory @004C3440.
     * Full port.
     */
    @Override
    public String getShopResourceDirectory() {
        return SHOP_KAARG;
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
     * Native support extracted from KaargShopDialogVisualObject::ShowDialog @004C03C2 and
     * KaargShopDialogVisualObject::UpdateShopAmbientSound @004C09C2.
     * Full support port for `timeGetTime` call sites.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }

}
