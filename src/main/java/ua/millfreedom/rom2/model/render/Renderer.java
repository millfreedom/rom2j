package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.palette.Palette16;

public interface Renderer {

    /**
     * Refreshes the software cursor after surface updates.
     * not ported.
     */
    void refreshMousePointer();

    /**
     * Native support boundary for LockViewport @00452D3A, LockPrimaryDirectDrawSurface @00452C8C, and
     * LockBackBufferDirectDrawSurface @00452BDE.
     * skipped: Java draws into a software surface without DirectDraw lock acquisition.
     */
    void lockSurface();

    /**
     * Native support boundary for UnlockViewport @00452DBF, UnlockPrimaryDirectDrawSurface @00452CE8, and
     * UnlockBackBufferDirectDrawSurface @00452C3A.
     * skipped: Java presents the software surface through GLRenderer instead of unlocking DirectDraw surfaces.
     */
    void unlockSurface();

    /**
     * Java-only render-target extension for MapVisualObject zoom. This is not native behavior: it exists so the Java
     * port can render the map into a fixed logical tile surface and scale that surface into the current screen viewport.
     * not ported.
     */
    void pushJavaRenderTarget(byte[] surfaceBgra, int width, int height);

    /**
     * Java-only counterpart to pushJavaRenderTarget. This is not native behavior and must stay treated as Java
     * presentation infrastructure rather than a recovered game-rendering routine.
     * not ported.
     */
    void popJavaRenderTarget();

    /**
     * Native support for SetScreenClipRect @0045332A clipping used by clipped visual repaints.
     */
    void pushClip(int left, int top, int right, int bottom);

    /**
     * Native support for restoring the previous SetScreenClipRect @0045332A clipping rectangle.
     */
    void popClip();

    /**
     * Native: DrawRect @004569A3.
     * Fully ported.
     */
    void drawRect(int left, int top, int right, int bottom, short color);

    /**
     * Native support: ApplyShadeToRect @004564DF.
     */
    void applyShadeToRect(int left, int top, int right, int bottom, int brightness);

    /**
     * Native support: AddColorToRect @00456416.
     */
    void addColorToRect(int left, int top, int right, int bottom, short color565);

    /**
     * Native support: ApplyShadeAdditiveToRect @004565CD.
     */
    void applyShadeAdditiveToRect(int left, int top, int right, int bottom, int brightness);

    /**
     * Native: DrawLine @0045673C.
     * Fully ported.
     */
    void drawLine(int x, int y, int cx, int cy, short color565);

    /**
     * Native: FillScreenRect @00456348.
     * Fully ported.
     */
    void fillScreenRect(int left, int top, int right, int bottom, short color565);

    /**
     * Native: DrawSprite_RLE4_to_16 @004540D1.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE4(int nX, int nY, int nWidth, int nHeight, byte[] rleData, RGB16[] palette16);

    /**
     * Native: DrawSprite_A16 @0045889B.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteA16(int nX, int nY, int nWidth, int nHeight, byte[] pA16Data, Palette16[] palettePages);

    /**
     * Native: DrawSprite_A16_FlipX @00458C10.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteA16FlipX(int nX, int nY, int nWidth, int nHeight, byte[] pA16Data, Palette16[] palettePages);

    /**
     * Native support extracted from CA16Font::DrawTextInternal @0045E8FD explicit color-table dispatch.
     */
    void drawSpriteA16WithBasePalette(int nX, int nY, int nWidth, int nHeight, byte[] pA16Data, RGB16[] palette16, boolean bFlipX);

    /**
     * Native: DrawSprite_RLE8_to_16 @00454344.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE8(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16[] pPalette);

    /**
     * Native: DrawSprite_RLE8_to_16_FlipX @0045537D.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE8FlipX(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16[] pPalette);

    /**
     * Native: DrawSprite_RLE8_SolidIndexed8 @004545C5.
     * Java 32bpp render-target boundary for native solid RLE8 sprite coverage.
     */
    void drawSpriteRLE8Solid(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16 fillColor);

    /**
     * Native: drawSpriteRLE8To16Blend @00454656.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE8Blend(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16[] pPalette);

    /**
     * Native: drawSpriteRLE8To16BlendFlipX @00455617.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE8BlendFlipX(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16[] pPalette);

    /**
     * Native: DrawSprite_RLE8_AlphaBlend @00454ABC.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE8AlphaBlend(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, int shadePage);

    /**
     * Native: DrawSprite_RLE8_AlphaBlend_FlipX @00455AB1.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE8AlphaBlendFlipX(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, int shadePage);

    /**
     * Native: DrawSpriteRLE8To16Lut @0045506F.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE8To16Lut(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, int lutIndex, int slope);

    /**
     * Native: DrawSpriteRLE8To16LutFlipX @00456021.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSpriteRLE8To16LutFlipX(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, int lutIndex, int slope);

    /**
     * Native: DrawSoftDot @0045724A.
     * Fully ported.
     */
    void drawSoftDot(int x, int y, int red, int green, int blue, int alpha);

    /**
     * Native: BlitToScreen @004538DD.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void blitToScreen(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, RGB16[] srcData, int srcWidth, int srcHeight);

    /**
     * Native: BlitToScreenMasked @00453BCA.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void blitToScreenMasked(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, RGB16[] srcData, int srcWidth, int srcHeight);

    /**
     * Native: BlitToScreenAdditive @00453A58.
     */
    void blitToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, RGB16[] srcData, int srcWidth, int srcHeight);

    /**
     * Native: BlitIndexedToScreen @00453D4B.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void blitIndexedToScreen(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                             byte[] srcData, int srcWidth, int srcHeight, RGB16[] palette16);

    /**
     * Native: BlitIndexedToScreenAdditive @00453F69.
     */
    void blitIndexedToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                     byte[] srcData, int srcWidth, int srcHeight, RGB16[] palette16);

    /**
     * Native: DrawFlatTerrainTileToScreen @00457487.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawFlatTerrainTile(int dstX, int topY,
                             int topLeftBrightness, int topRightBrightness,
                             int bottomLeftBrightness, int bottomRightBrightness,
                             byte[] sourcePixels, int sourceOffset, Palette16[] palettePages);

    /**
     * Native: DrawSkewedTerrainTileToScreen @004575FE.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSkewedTerrainTile(int leftX, int rightX,
                               int topLeftY, int topRightY,
                               int bottomLeftY, int bottomRightY,
                               int topLeftBrightness, int topRightBrightness,
                               int bottomLeftBrightness, int bottomRightBrightness,
                               byte[] sourcePixels, int sourceOffset, Palette16[] palettePages);

    /**
     * Native: ClearFlatTerrainSlopeMask @00457944.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void clearFlatTerrainSlopeMask(int dstX, int topY, int bottomY);

    /**
     * Native: ClearSkewedTerrainSlopeMask @00457A4D.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void clearSkewedTerrainSlopeMask(int leftX, int rightX,
                                     int topLeftY, int topRightY,
                                     int bottomLeftY, int bottomRightY);

    /**
     * Native: DimFlatTerrainSlopeMask @004582BC.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void dimFlatTerrainSlopeMask(int dstX, int topY, int bottomY);

    /**
     * Native: DimSkewedTerrainSlopeMask @004584D0.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void dimSkewedTerrainSlopeMask(int leftX, int rightX,
                                   int topLeftY, int topRightY,
                                   int bottomLeftY, int bottomRightY);

    /**
     * Native: ApplyFlatTerrainSlopeMaskBrightness @00457DA7.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void applyFlatTerrainSlopeMaskBrightness(int dstX, int topY,
                                             int topLeftBrightness, int topRightBrightness,
                                             int bottomLeftBrightness, int bottomRightBrightness);

    /**
     * Native: ApplySkewedTerrainSlopeMaskBrightness @00457F5B.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void applySkewedTerrainSlopeMaskBrightness(int leftX, int rightX,
                                               int topLeftY, int topRightY,
                                               int bottomLeftY, int bottomRightY,
                                               int topLeftBrightness, int topRightBrightness,
                                               int bottomLeftBrightness, int bottomRightBrightness);

    /**
     * Java helper for paletted screen blits with palette-index zero transparency.
     * not ported.
     */
    void blitIndexedToScreenMasked(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                   byte[] srcData, int srcWidth, int srcHeight, RGB16[] palette16);

    /**
     * Blits 16-bit source pixels into the screen surface.
     * not ported.
     */
    void blitPixels(int destX, int destY, int width, int height, int srcPitchPixels, int srcHeight, byte[] pSrcPointer);

    /**
     * Blits BGRA pixels into the screen surface with nearest-neighbor scaling.
     * not ported.
     */
    void blitBgraScaled(byte[] sourceBgra, int sourceWidth, int sourceHeight,
                        int destX, int destY, int destWidth, int destHeight);

    /**
     * Clears the software-backed render surface before a new frame.
     * not ported.
     */
    void clearSurface();

    /**
     * Native support boundary for FlipPrimaryDirectDrawSurface @00452685 and
     * WaitForDirectDrawVerticalBlankEnd @00452237.
     * skipped: Java uploads the software surface into OpenGL and uses the platform swap interval instead of flipping or
     * waiting on DirectDraw surfaces.
     */
    void presentSurface(int framebufferWidth, int framebufferHeight);

    /**
     * Native support boundary for ReleaseDirectDrawInterface @00452B89.
     * skipped: Java releases renderer-owned presentation resources instead of a DirectDraw global.
     */
    void releasePresentationResources();

}
