package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.GameBitmapFrame;
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
    void pushJavaRenderTarget(int[] surfaceArgb, int width, int height);

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
    void drawRect(int left, int top, int right, int bottom, int color);

    /**
     * Native support: ApplyShadeToRect @004564DF.
     */
    void applyShadeToRect(int left, int top, int right, int bottom, int brightness);

    /**
     * Native support: AddColorToRect @00456416.
     */
    void addColorToRect(int left, int top, int right, int bottom, int color);

    /**
     * Native support: ApplyShadeAdditiveToRect @004565CD.
     */
    void applyShadeAdditiveToRect(int left, int top, int right, int bottom, int brightness);

    /**
     * Native: DrawLine @0045673C.
     * Fully ported.
     */
    void drawLine(int x, int y, int cx, int cy, int color);

    /**
     * Native: FillScreenRect @00456348.
     * Fully ported.
     */
    void fillScreenRect(int left, int top, int right, int bottom, int color);

    /**
     * Java decoded-frame boundary for normal palette-indexed sprite rendering.
     * not ported.
     */
    void drawIndexedSprite(int x, int y, GameBitmapFrame frame, int[] palette, boolean flipX);

    /**
     * Java decoded-frame boundary for native fixed-half sprite blending.
     * not ported.
     */
    void drawIndexedSpriteBlend(int x, int y, GameBitmapFrame frame, int[] palette, boolean flipX);

    /**
     * Java decoded-frame boundary that uses decoded RLE coverage for native shade-page semantics.
     * not ported.
     */
    void drawIndexedSpriteShade(int x, int y, GameBitmapFrame frame, int shadePage, boolean flipX);

    /**
     * Java decoded-frame boundary for sheared destination shade-page semantics using decoded RLE coverage.
     * not ported.
     */
    void drawIndexedSpriteShearedShade(int x, int y, GameBitmapFrame frame,
                                       int shadePage, int slope, boolean flipX);

    /**
     * Java decoded-frame boundary that fills decoded RLE coverage with one straight-ARGB color.
     * not ported.
     */
    void drawIndexedSpriteSolid(int x, int y, GameBitmapFrame frame, int fillColor, boolean flipX);

    /**
     * Java decoded-frame boundary for packed A16 codes and one compact palette-generation lookup.
     * not ported.
     */
    void drawA16Sprite(int x, int y, GameBitmapFrame frame, A16PaletteLookup paletteLookup, boolean flipX);

    /**
     * Native: DrawSoftDot @0045724A.
     * Fully ported.
     */
    void drawSoftDot(int x, int y, int red, int green, int blue, int alpha);

    /**
     * Native: BlitToScreen @004538DD.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void blitToScreen(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, int[] srcData, int srcWidth, int srcHeight);

    /**
     * Native: BlitToScreenMasked @00453BCA.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void blitToScreenMasked(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, int[] srcData, int srcWidth, int srcHeight);

    /**
     * Native: BlitToScreenAdditive @00453A58.
     */
    void blitToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, int[] srcData, int srcWidth, int srcHeight);

    /**
     * Native: BlitIndexedToScreen @00453D4B.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void blitIndexedToScreen(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                              int[] srcData, int srcWidth, int srcHeight, int[] palette);

    /**
     * Native: BlitIndexedToScreenAdditive @00453F69.
     */
    void blitIndexedToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                      int[] srcData, int srcWidth, int srcHeight, int[] palette);

    /**
     * Native: DrawFlatTerrainTileToScreen @00457487.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawFlatTerrainTile(int dstX, int topY,
                             int topLeftBrightness, int topRightBrightness,
                             int bottomLeftBrightness, int bottomRightBrightness,
                              int[] sourcePixels, int sourceOffset, Palette16[] palettePages);

    /**
     * Native: DrawSkewedTerrainTileToScreen @004575FE.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    void drawSkewedTerrainTile(int leftX, int rightX,
                               int topLeftY, int topRightY,
                               int bottomLeftY, int bottomRightY,
                               int topLeftBrightness, int topRightBrightness,
                               int bottomLeftBrightness, int bottomRightBrightness,
                                int[] sourcePixels, int sourceOffset, Palette16[] palettePages);

    /**
     * Java-only terrain-cache clipping support. Clears pixels above the first cached viewport terrain edge so terrain
     * protruding from the preceding world row cannot leak into the copied logical frame.
     * not ported.
     */
    void clearRowsAboveTerrainEdge(int leftX, int topLeftY, int topRightY, int color);

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
                                    int[] srcData, int srcWidth, int srcHeight, int[] palette);

    /**
     * Blits known-opaque straight-ARGB pixels into the screen surface with nearest-neighbor scaling.
     * not ported.
     */
    void blitOpaqueArgbScaled(int[] sourceArgb, int sourceWidth, int sourceHeight,
                              int destX, int destY, int destWidth, int destHeight);

    /**
     * Blits opaque palette colors resolved directly from a canonical integer selector plane, with source cropping and
     * nearest-neighbor scaling.
     * not ported.
     */
    void blitOpaqueIndexedScaled(int[] sourceSelectors, int sourceWidth, int sourceHeight,
                                 int sourceX, int sourceY, int sourceRectWidth, int sourceRectHeight,
                                 int[] palette, int destX, int destY, int destWidth, int destHeight);

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
