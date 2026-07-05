package ua.millfreedom.rom2;

import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.visobj.RightPanelLayout;
import ua.millfreedom.rom2.res.Resources;

import java.util.LinkedHashMap;
import java.util.Map;

import static ua.millfreedom.rom2.res.Constants.*;

public final class GUI {
    public static CBmp64k crystalR;
    public static CBmp64k crystalL;
    public static CBmp64k headsR;
    public static CBmp64k headsL;
    public static CBmp64k commandBarR;
    public static CBmp64k commandBarL;
    public static CBmp64k commandDnR;
    public static CBmp64k commandEmpR;
    public static CBmp64k humanBackL;
    public static CBmp64k humanBackR;
    public static CBmp64k textBackL;
    public static CBmp64k textBackR;
    public static CBmp64k bookOpened;
    public static CBmp64k bookClosed;
    public static CBmp64k backPackOpen;
    public static CBmp64k backPackClosed;
    public static CBmp64k humanMode;
    public static CBmp64k textMode;
    public static CBmp64k diskette;
    public static CBmp64k arrow1;
    public static CBmp64k arrow2;
    public static CBmp64k arrow3;
    public static CBmp64k arrow4;
    public static CBmp64k spellBook;
    public static CBmp64k spellBack;
    public static CBmp64k ball;
    public static CSprite256 sprBackpack;
    public static CSprite256 sprBackpackB;
    public static CBmp64k invFrame;
    public static CBmp64k invArrow1;
    public static CBmp64k invArrow2;
    public static CBmp64k invArrow3;
    public static CBmp64k invArrow4;
    public static CBmp64k backInv;
    public static CBmp64k spbLeft800;
    public static CBmp64k spbRight800;
    public static CBmp64k extraLeft800;
    public static CBmp64k extraRight800;
    public static CBmp64k spbLeft1024;
    public static CBmp64k spbRight1024;
    public static CBmp64k extraLeft1024;
    public static CBmp64k extraRight1024;
    public static CBmp64k invLeft1024;
    public static CBmp64k invRight1024;
    public static CSprite256 uiFrameSprite;
    public static CSprite256 sprScrollBars;
    public static CSprite256 sprRadioButtons;
    public static CA16 sprMoney;
    public static CSprite256 sprTBorder;
    public static CBmp64k tBack;
    public static CSprite256 sprHeroBackMale;
    public static CSprite256 sprHeroBackFemale;
    public static CBmp64k server;
    public static CBmp64k miniMapData;
    public static CBmp256 testIva;

    public static final Map<String, CBmp64k> bmp64k = new LinkedHashMap<>();
    public static final Map<String, CBmp256> bmp256 = new LinkedHashMap<>();
    public static final Map<String, CSprite256> sprite256 = new LinkedHashMap<>();
    public static final Map<String, CA16> sprite16a = new LinkedHashMap<>();

    // not ported.
    private GUI() {
    }

    /**
     * Native: GUI::loadInterfaceGraphics @00476A6D.
     * Fully ported at the Java managed-allocation boundary. Native allocates the interface bitmap/sprite globals,
     * conditionally loads the 800px or 1024px side-panel art based on screen height.
     */
    public static void loadInterfaceGraphics() {
        bmp64k.clear();
        bmp256.clear();
        sprite256.clear();
        sprite16a.clear();

        crystalR = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, CRYSTAL_R_BMP));
        bmp64k.put(CRYSTAL_R_BMP, crystalR);
        crystalL = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, CRYSTAL_L_BMP));
        bmp64k.put(CRYSTAL_L_BMP, crystalL);
        headsR = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, HEADS_R_BMP));
        bmp64k.put(HEADS_R_BMP, headsR);
        headsL = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, HEADS_L_BMP));
        bmp64k.put(HEADS_L_BMP, headsL);

        commandBarR = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, COMMAND_BAR_R_BMP));
        bmp64k.put(COMMAND_BAR_R_BMP, commandBarR);
        commandBarL = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, COMMAND_BAR_L_BMP));
        bmp64k.put(COMMAND_BAR_L_BMP, commandBarL);
        commandDnR = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, COMMAND_DN_R_BMP));
        bmp64k.put(COMMAND_DN_R_BMP, commandDnR);
        commandEmpR = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, COMMAND_EMP_R_BMP));
        bmp64k.put(COMMAND_EMP_R_BMP, commandEmpR);
        humanBackL = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, HUMAN_BACK_L_BMP));
        bmp64k.put(HUMAN_BACK_L_BMP, humanBackL);

        humanBackR = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, HUMAN_BACK_R_BMP));
        bmp64k.put(HUMAN_BACK_R_BMP, humanBackR);
        textBackL = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, TEXT_BACK_L_BMP));
        bmp64k.put(TEXT_BACK_L_BMP, textBackL);
        textBackR = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, TEXT_BACK_R_BMP));
        bmp64k.put(TEXT_BACK_R_BMP, textBackR);
        bookOpened = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, BOOK_OPENED_BMP));
        bmp64k.put(BOOK_OPENED_BMP, bookOpened);
        bookClosed = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, BOOK_CLOSED_BMP));
        bmp64k.put(BOOK_CLOSED_BMP, bookClosed);
        backPackOpen = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, BACKPACK_OPEN_BMP));
        bmp64k.put(BACKPACK_OPEN_BMP, backPackOpen);
        backPackClosed = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, BACKPACK_CLOSED_BMP));
        bmp64k.put(BACKPACK_CLOSED_BMP, backPackClosed);

        humanMode = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, HUMAN_MODE_BMP));
        bmp64k.put(HUMAN_MODE_BMP, humanMode);
        textMode = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, TEXT_MODE_BMP));
        bmp64k.put(TEXT_MODE_BMP, textMode);
        diskette = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, DISKETTE_BMP));
        bmp64k.put(DISKETTE_BMP, diskette);
        arrow1 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, ARROW1_BMP));
        bmp64k.put(ARROW1_BMP, arrow1);
        arrow2 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, ARROW2_BMP));
        bmp64k.put(ARROW2_BMP, arrow2);
        arrow3 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, ARROW3_BMP));
        bmp64k.put(ARROW3_BMP, arrow3);
        arrow4 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, ARROW4_BMP));
        bmp64k.put(ARROW4_BMP, arrow4);

        spellBook = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, SPELLBOOK_BMP));
        bmp64k.put(SPELLBOOK_BMP, spellBook);
        spellBack = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, SPELLBACK_BMP));
        bmp64k.put(SPELLBACK_BMP, spellBack);
        ball = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, BALL_BMP));
        bmp64k.put(BALL_BMP, ball);

        loadBackpackSprites();

        invFrame = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, INV_FRAME_BMP));
        bmp64k.put(INV_FRAME_BMP, invFrame);
        invArrow1 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, INV_ARROW1_BMP));
        bmp64k.put(INV_ARROW1_BMP, invArrow1);
        invArrow2 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, INV_ARROW2_BMP));
        bmp64k.put(INV_ARROW2_BMP, invArrow2);
        invArrow3 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, INV_ARROW3_BMP));
        bmp64k.put(INV_ARROW3_BMP, invArrow3);
        invArrow4 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, INV_ARROW4_BMP));
        bmp64k.put(INV_ARROW4_BMP, invArrow4);
        backInv = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, BACK_INV_BMP));
        bmp64k.put(BACK_INV_BMP, backInv);

        RightPanelLayout rightPanelLayout = RightPanelLayout.forScreenHeight(Globals.screenRect.bottom);
        if (rightPanelLayout.usesHighResolutionArt()) {
            spbLeft1024 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, SPB_LEFT_1024_BMP));
            bmp64k.put(SPB_LEFT_1024_BMP, spbLeft1024);
            spbRight1024 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, SPB_RIGHT_1024_BMP));
            bmp64k.put(SPB_RIGHT_1024_BMP, spbRight1024);
            if (rightPanelLayout.usesTallExtraFillArt()) {
                extraLeft800 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, EXTRA_LEFT_800_BMP));
                bmp64k.put(EXTRA_LEFT_800_BMP, extraLeft800);
                extraRight800 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, EXTRA_RIGHT_800_BMP));
                bmp64k.put(EXTRA_RIGHT_800_BMP, extraRight800);
            } else {
                extraLeft1024 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, EXTRA_LEFT_1024_BMP));
                bmp64k.put(EXTRA_LEFT_1024_BMP, extraLeft1024);
                extraRight1024 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, EXTRA_RIGHT_1024_BMP));
                bmp64k.put(EXTRA_RIGHT_1024_BMP, extraRight1024);
            }
            invLeft1024 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, INV_LEFT_1024_BMP));
            bmp64k.put(INV_LEFT_1024_BMP, invLeft1024);
            invRight1024 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, INV_RIGHT_1024_BMP));
            bmp64k.put(INV_RIGHT_1024_BMP, invRight1024);
        } else if (rightPanelLayout.usesMediumResolutionArt()) {
            spbLeft800 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, SPB_LEFT_800_BMP));
            bmp64k.put(SPB_LEFT_800_BMP, spbLeft800);
            spbRight800 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, SPB_RIGHT_800_BMP));
            bmp64k.put(SPB_RIGHT_800_BMP, spbRight800);
            extraLeft800 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, EXTRA_LEFT_800_BMP));
            bmp64k.put(EXTRA_LEFT_800_BMP, extraLeft800);
            extraRight800 = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, EXTRA_RIGHT_800_BMP));
            bmp64k.put(EXTRA_RIGHT_800_BMP, extraRight800);
        }

        uiFrameSprite = new CSprite256(Resources.path(GRAPHICS, INTERFACE, UI_FRAME_256));
        uiFrameSprite.initPalette(1, 1, 0);
        sprite256.put(UI_FRAME_256, uiFrameSprite);

        sprScrollBars = new CSprite256(Resources.path(GRAPHICS, INTERFACE, SCROLLBARS_256));
        sprite256.put(SCROLLBARS_256, sprScrollBars);
        sprScrollBars.initPalette(1, 1, 0);

        sprRadioButtons = new CSprite256(Resources.path(GRAPHICS, INTERFACE, RADIOB_256));
        sprite256.put(RADIOB_256, sprRadioButtons);
        sprRadioButtons.initPalette(1, 1, 0);

        sprMoney = new CA16(Resources.path(GRAPHICS, INTERFACE, MONEY, MONEY_16A));
        sprite16a.put(MONEY_16A, sprMoney);
        sprMoney.initPalette(16, 4, 0);

        sprTBorder = new CSprite256(Resources.path(GRAPHICS, INTERFACE, T_BORDER_256));
        sprite256.put(T_BORDER_256, sprTBorder);
        sprTBorder.initPalette(1, 1, 0);

        tBack = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, T_BACK_BMP));
        bmp64k.put(T_BACK_BMP, tBack);

        sprHeroBackMale = new CSprite256(Resources.path(GRAPHICS, INTERFACE, HERO_BACK, HERO_BACK_MALE_256));
        sprite256.put(HERO_BACK_MALE_256, sprHeroBackMale);
        sprHeroBackMale.initPalette(1, 1, 0);

        sprHeroBackFemale = new CSprite256(Resources.path(GRAPHICS, INTERFACE, HERO_BACK, HERO_BACK_FEMALE_256));
        sprite256.put(HERO_BACK_FEMALE_256, sprHeroBackFemale);
        sprHeroBackFemale.initPalette(1, 1, 0);

        server = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, SERVER_BMP));
        bmp64k.put(SERVER_BMP, server);
        miniMapData = new CBmp64k(Resources.path(GRAPHICS, INTERFACE, MINIMAP_DATA_BMP));
        bmp64k.put(MINIMAP_DATA_BMP, miniMapData);

        testIva = new CBmp256(Resources.path(GRAPHICS, INTERFACE, TEST_IVA_BMP));
        bmp256.put(TEST_IVA_BMP, testIva);
        testIva.initPalette(16, 2, 0);
    }

    /**
     * Java support for loading backpack sprites used by the standalone MapEditor preview.
     * not ported.
     */
    public static void loadBackpackSpritesForEditorPreview() {
        loadBackpackSprites();
    }

    /**
     * Native support extracted from GUI::loadInterfaceGraphics @00476A6D backpack sprite load.
     */
    private static void loadBackpackSprites() {
        sprBackpack = new CSprite256(Resources.path(GRAPHICS, BACKPACK, BACKPACK_SPRITES_256));
        sprite256.put(BACKPACK_SPRITES_256, sprBackpack);
        sprBackpackB = new CSprite256(Resources.path(GRAPHICS, BACKPACK, BACKPACK_SPRITES_B_256));
        sprite256.put(BACKPACK_SPRITES_B_256, sprBackpackB);
        sprBackpack.initPalette(16, 2, 1);
    }

    /**
     * Native: GUI::releaseInterfaceGraphics @00477EEC.
     * Fully ported at the Java managed-resource boundary. Native scalar-deletes the interface bitmap/sprite globals
     * during application shutdown; Java drops the corresponding static holders and helper lookup maps.
     */
    public static void releaseInterfaceGraphics() {
        crystalR = null;
        crystalL = null;
        headsR = null;
        headsL = null;
        commandBarR = null;
        commandBarL = null;
        commandDnR = null;
        commandEmpR = null;
        humanBackL = null;
        humanBackR = null;
        textBackL = null;
        textBackR = null;
        bookOpened = null;
        bookClosed = null;
        backPackOpen = null;
        backPackClosed = null;
        humanMode = null;
        textMode = null;
        diskette = null;
        arrow1 = null;
        arrow2 = null;
        arrow3 = null;
        arrow4 = null;
        spellBook = null;
        spellBack = null;
        invFrame = null;
        invArrow1 = null;
        invArrow2 = null;
        invArrow3 = null;
        invArrow4 = null;
        backInv = null;
        spbLeft1024 = null;
        spbRight1024 = null;
        extraLeft1024 = null;
        extraRight1024 = null;
        invLeft1024 = null;
        invRight1024 = null;
        spbLeft800 = null;
        spbRight800 = null;
        extraLeft800 = null;
        extraRight800 = null;
        uiFrameSprite = null;
        sprScrollBars = null;
        sprRadioButtons = null;
        sprMoney = null;
        sprTBorder = null;
        tBack = null;
        sprHeroBackMale = null;
        sprHeroBackFemale = null;
        miniMapData = null;
        server = null;
        ball = null;
        sprBackpack = null;
        sprBackpackB = null;
        testIva = null;

        bmp64k.clear();
        bmp256.clear();
        sprite256.clear();
        sprite16a.clear();
    }
}
