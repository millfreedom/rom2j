package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.GameBitmapFrame;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;

import static ua.millfreedom.rom2.model.color.Consts.RENDER_EFFECT_TABLE;
import static ua.millfreedom.rom2.model.color.Utils.clamp255;

public class SoftRenderer implements Renderer {
    protected static final int TERRAIN_TILE_SIZE = 0x20;
    protected static final int TERRAIN_BRIGHTNESS_FIXED_SHIFT = 9;
    protected static final int TERRAIN_BRIGHTNESS_FIXED_HALF = 0x100;
    protected static final int TERRAIN_SLOPE_DIM_BRIGHTNESS = 8;
    protected static final int TERRAIN_SLOPE_DIM_MASK_PAGE = 0x08;
    protected static final int TERRAIN_SLOPE_CLEAR_MASK_PAGE = 0x10;
    /**
     * Native support extracted from ApplyShadeAdditiveToRect @004565CD.
     */
    private static final int[] NATIVE_SHADE_ADDITIVE_COLORS = initNativeShadeAdditiveColors();

    public final Screen screen;
    protected Screen activeRenderTarget;
    protected final Deque<int[]> clipStack = new ArrayDeque<>();
    protected final Deque<RenderTargetState> renderTargetStack = new ArrayDeque<>();
    protected int clipLeft;
    protected int clipTop;
    protected int clipRight;
    protected int clipBottom;
    // Java-only cached nearest-neighbor X mapping for stable scaled presentation dimensions.
    private ScaleAxisLookup horizontalScaleLookup;
    // Java-only cached nearest-neighbor Y mapping for stable scaled presentation dimensions.
    private ScaleAxisLookup verticalScaleLookup;

    // not ported.
    public SoftRenderer(Screen screen) {
        this.screen = screen;
        this.activeRenderTarget = screen;
        resetClip();
    }

    protected record RenderTargetState(Screen renderTarget, int clipLeft, int clipTop, int clipRight, int clipBottom,
                                       Deque<int[]> clipStack) {
    }

    /**
     * Native support for the default `_g_screenRect` clipping rectangle.
     */
    protected void resetClip() {
        clipLeft = activeRenderTarget.x();
        clipTop = activeRenderTarget.y();
        clipRight = activeRenderTarget.cx();
        clipBottom = activeRenderTarget.cy();
    }

    /**
     * Clears the software-backed render surface to opaque black.
     * not ported.
     */
    @Override
    public synchronized void clearSurface() {
        Arrays.fill(activeRenderTarget.surface(), RGB32.BLACK);
    }

    /**
     * Native support boundary for FlipPrimaryDirectDrawSurface @00452685 and
     * WaitForDirectDrawVerticalBlankEnd @00452237.
     * skipped: SoftRenderer has no native presentation target.
     */
    @Override
    public void presentSurface(int framebufferWidth, int framebufferHeight) {
    }

    /**
     * Native support boundary for ReleaseDirectDrawInterface @00452B89.
     * skipped: SoftRenderer owns no presentation resources.
     */
    @Override
    public void releasePresentationResources() {
    }

    /**
     * not ported.
     */
    @Override
    public void refreshMousePointer() {
    }

    /**
     * Native support boundary for LockViewport @00452D3A, LockPrimaryDirectDrawSurface @00452C8C, and
     * LockBackBufferDirectDrawSurface @00452BDE.
     * skipped: Java writes directly to the software render target.
     */
    @Override
    public void lockSurface() {
    }

    /**
     * Native support boundary for UnlockViewport @00452DBF, UnlockPrimaryDirectDrawSurface @00452CE8, and
     * UnlockBackBufferDirectDrawSurface @00452C3A.
     * skipped: SoftRenderer draws directly into the software render target.
     */
    @Override
    public void unlockSurface() {
    }

    /**
     * Java-only render-target extension for MapVisualObject zoom. This deliberately does not model native DirectDraw
     * behavior; it isolates the Java-only logical-map framebuffer from the real software screen surface.
     * not ported.
     */
    @Override
    public void pushJavaRenderTarget(int[] surfaceArgb, int width, int height) {
        Objects.requireNonNull(surfaceArgb, "surfaceArgb");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Render target dimensions must be positive");
        }
        int expectedLength = Math.multiplyExact(width, height);
        if (surfaceArgb.length < expectedLength) {
            throw new IllegalArgumentException("Render target ARGB buffer is too small");
        }

        renderTargetStack.push(new RenderTargetState(
                activeRenderTarget,
                clipLeft,
                clipTop,
                clipRight,
                clipBottom,
                new ArrayDeque<>(clipStack)
        ));
        activeRenderTarget = new Screen(0, 0, width, height, width, surfaceArgb);
        clipStack.clear();
        resetClip();
    }

    /**
     * Java-only render-target extension for MapVisualObject zoom. Restores the previous software target and clipping
     * state; this is Java presentation infrastructure, not native game-rendering logic.
     * not ported.
     */
    @Override
    public void popJavaRenderTarget() {
        if (renderTargetStack.isEmpty()) {
            throw new IllegalStateException("No Java render target is active");
        }
        RenderTargetState state = renderTargetStack.pop();
        activeRenderTarget = state.renderTarget();
        clipLeft = state.clipLeft();
        clipTop = state.clipTop();
        clipRight = state.clipRight();
        clipBottom = state.clipBottom();
        clipStack.clear();
        clipStack.addAll(state.clipStack());
    }

    /**
     * Native support for SetScreenClipRect @0045332A clipping used by HandlerVisualObject::RenderSelf @004DBD5F.
     */
    @Override
    public void pushClip(int left, int top, int right, int bottom) {
        clipStack.push(new int[]{clipLeft, clipTop, clipRight, clipBottom});
        clipLeft = Math.max(activeRenderTarget.x(), left);
        clipTop = Math.max(activeRenderTarget.y(), top);
        clipRight = Math.min(activeRenderTarget.cx(), right);
        clipBottom = Math.min(activeRenderTarget.cy(), bottom);
        if (clipRight < clipLeft) {
            clipRight = clipLeft;
        }
        if (clipBottom < clipTop) {
            clipBottom = clipTop;
        }
    }

    /**
     * Native support for restoring SetScreenClipRect @0045332A after clipped visual repaints.
     */
    @Override
    public void popClip() {
        if (clipStack.isEmpty()) {
            resetClip();
            return;
        }
        int[] previous = clipStack.pop();
        clipLeft = previous[0];
        clipTop = previous[1];
        clipRight = previous[2];
        clipBottom = previous[3];
    }

    /**
     * Native: DrawRect @004569A3.
     * Fully ported.
     */
    @Override
    public void drawRect(int left, int top, int right, int bottom, int color) {
        drawLine(left, top, right, top, color);
        drawLine(left, bottom, right, bottom, color);
        drawLine(right, top, right, bottom, color);
        drawLine(left, top, left, bottom, color);
    }

    /**
     * Native support: ApplyShadeToRect @004564DF.
     */
    @Override
    public synchronized void applyShadeToRect(int left, int top, int right, int bottom, int shade) {
        final int clippedLeft = Math.max(left, clipLeft);
        final int clippedTop = Math.max(top, clipTop);
        final int clippedRight = Math.min(right, clipRight);
        final int clippedBottom = Math.min(bottom, clipBottom);
        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();
        for (int y = clippedTop; y < clippedBottom; y++) {
            int offset = (y - surfaceTop) * pitchPixels + (clippedLeft - surfaceLeft);
            for (int x = clippedLeft; x < clippedRight; x++) {
                int color = getPixel(surface, offset);
                putPixel(surface, offset, RGB32.withShade(color, shade));
                offset++;
            }
        }
    }

    /**
     * Native support: AddColorToRect @00456416.
     */
    @Override
    public synchronized void addColorToRect(int left, int top, int right, int bottom, int color) {
        final int clippedLeft = Math.max(left, clipLeft);
        final int clippedTop = Math.max(top, clipTop);
        final int clippedRight = Math.min(right, clipRight);
        final int clippedBottom = Math.min(bottom, clipBottom);
        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();
        for (int y = clippedTop; y < clippedBottom; y++) {
            int offset = (y - surfaceTop) * pitchPixels + (clippedLeft - surfaceLeft);
            for (int x = clippedLeft; x < clippedRight; x++) {
                putPixelAdditive(surface, offset, color);
                offset++;
            }
        }
    }

    /**
     * Native support: ApplyShadeAdditiveToRect @004565CD.
     */
    @Override
    public synchronized void applyShadeAdditiveToRect(int left, int top, int right, int bottom, int brightness) {
        final int clippedLeft = Math.max(left, clipLeft);
        final int clippedTop = Math.max(top, clipTop);
        final int clippedRight = Math.min(right, clipRight);
        final int clippedBottom = Math.min(bottom, clipBottom);
        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        final int additiveColor = nativeShadeAdditiveColor(brightness);
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();
        for (int y = clippedTop; y < clippedBottom; y++) {
            int offset = (y - surfaceTop) * pitchPixels + (clippedLeft - surfaceLeft);
            for (int x = clippedLeft; x < clippedRight; x++) {
                putPixelShadeAdditive(surface, offset, brightness, additiveColor);
                offset++;
            }
        }
    }

    /**
     * Native: DrawLine @0045673C.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawLine(int x, int y, int cx, int cy, int color) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int left = clipLeft;
        final int top = clipTop;
        final int right = clipRight; // exclusive
        final int bottom = clipBottom; // exclusive
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surf = activeRenderTarget.surface();

        int dx = Math.abs(cx - x);
        int dy = Math.abs(cy - y);

        int err, errStep, errAltStep;
        int xEnd = 0, yEnd = 0;

        if (dy < dx) {
            err = dy * 2 - dx;
            errStep = 2 * dy;
            errAltStep = (dy - dx) * 2;
            xEnd = cx;
        } else {
            err = dx * 2 - dy;
            errStep = 2 * dx;
            errAltStep = (dx - dy) * 2;
            yEnd = cy;
        }

        int sx = (cx < x) ? -1 : 1;
        int sy = (cy < y) ? -1 : 1;

        int curX = x, curY = y;

        if (curX >= left && curX < right && curY >= top && curY < bottom) {
            putAtXY(surf, pitchPixels, surfaceLeft, surfaceTop, curX, curY, color);
        }

        if (dy < dx) {
            while (curX != xEnd) {
                curX += sx;
                int delta = errStep;
                if (err >= 0) {
                    curY += sy;
                    delta = errAltStep;
                }
                err += delta;

                if (curX >= left && curX < right && curY >= top && curY < bottom) {
                    putAtXY(surf, pitchPixels, surfaceLeft, surfaceTop, curX, curY, color);
                }
            }
        } else {
            while (curY != yEnd) {
                curY += sy;
                int delta = errStep;
                if (err >= 0) {
                    curX += sx;
                    delta = errAltStep;
                }
                err += delta;

                if (curX >= left && curX < right && curY >= top && curY < bottom) {
                    putAtXY(surf, pitchPixels, surfaceLeft, surfaceTop, curX, curY, color);
                }
            }
        }
    }

    /**
     * Native: FillScreenRect @00456348.
     * Fully ported.
     */
    @Override
    public void fillScreenRect(int left, int top, int right, int bottom, int color) {
        if (Globals.isWindowed != 0) {
            return;
        }
        int clippedLeft = Math.max(left, clipLeft);
        int clippedRight = Math.min(right, clipRight);
        if (clippedLeft >= clippedRight) {
            return;
        }

        int clippedTop = Math.max(top, clipTop);
        int clippedBottom = Math.min(bottom, clipBottom);
        if (clippedTop >= clippedBottom) {
            return;
        }

        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();
        for (int y = clippedTop; y < clippedBottom; y++) {
            int offset = (y - surfaceTop) * pitchPixels + (clippedLeft - surfaceLeft);
            for (int x = clippedLeft; x < clippedRight; x++) {
                putPixel(surface, offset, color);
                offset++;
            }
        }
    }

    /**
     * Blits known-opaque straight-ARGB pixels into the software surface using nearest-neighbor scaling and the current
     * clip rectangle.
     * not ported.
     */
    @Override
    public void blitOpaqueArgbScaled(int[] sourceArgb, int sourceWidth, int sourceHeight,
                                     int destX, int destY, int destWidth, int destHeight) {
        if (sourceWidth <= 0 || sourceHeight <= 0 || destWidth <= 0 || destHeight <= 0) {
            return;
        }

        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clippedLeft = Math.max(destX, clipLeft);
        final int clippedTop = Math.max(destY, clipTop);
        final int clippedRight = Math.min(destX + destWidth, clipRight);
        final int clippedBottom = Math.min(destY + destHeight, clipBottom);
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surfacePixels = activeRenderTarget.surface();

        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        horizontalScaleLookup = ScaleAxisLookup.resolve(horizontalScaleLookup, sourceWidth, destWidth);
        verticalScaleLookup = ScaleAxisLookup.resolve(verticalScaleLookup, sourceHeight, destHeight);
        int[] sourceXByDestinationX = horizontalScaleLookup.sourceIndexByDestinationIndex();
        int[] sourceYByDestinationY = verticalScaleLookup.sourceIndexByDestinationIndex();
        for (int y = clippedTop; y < clippedBottom; y++) {
            int srcY = sourceYByDestinationY[y - destY];
            int srcRow = srcY * sourceWidth;
            int destination = (y - surfaceTop) * pitchPixels + (clippedLeft - surfaceLeft);
            for (int x = clippedLeft; x < clippedRight; x++) {
                int srcX = sourceXByDestinationX[x - destX];
                surfacePixels[destination++] = sourceArgb[srcRow + srcX];
            }
        }
    }

    /**
     * Blits opaque palette colors resolved directly from a canonical integer selector plane, with source cropping,
     * nearest-neighbor scaling, and the current clip rectangle.
     * not ported.
     */
    @Override
    public void blitOpaqueIndexedScaled(int[] sourceSelectors, int sourceWidth, int sourceHeight,
                                        int sourceX, int sourceY, int sourceRectWidth, int sourceRectHeight,
                                        int[] palette, int destX, int destY, int destWidth, int destHeight) {
        if (sourceWidth <= 0 || sourceHeight <= 0
                || sourceRectWidth <= 0 || sourceRectHeight <= 0
                || destWidth <= 0 || destHeight <= 0) {
            return;
        }

        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clippedLeft = Math.max(destX, clipLeft);
        final int clippedTop = Math.max(destY, clipTop);
        final int clippedRight = Math.min(destX + destWidth, clipRight);
        final int clippedBottom = Math.min(destY + destHeight, clipBottom);
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surfacePixels = activeRenderTarget.surface();

        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        horizontalScaleLookup = ScaleAxisLookup.resolve(horizontalScaleLookup, sourceRectWidth, destWidth);
        verticalScaleLookup = ScaleAxisLookup.resolve(verticalScaleLookup, sourceRectHeight, destHeight);
        int[] sourceXByDestinationX = horizontalScaleLookup.sourceIndexByDestinationIndex();
        int[] sourceYByDestinationY = verticalScaleLookup.sourceIndexByDestinationIndex();
        for (int y = clippedTop; y < clippedBottom; y++) {
            int selectorRow = (sourceY + sourceYByDestinationY[y - destY]) * sourceWidth + sourceX;
            int destination = (y - surfaceTop) * pitchPixels + (clippedLeft - surfaceLeft);
            for (int x = clippedLeft; x < clippedRight; x++) {
                int selectorX = sourceXByDestinationX[x - destX];
                surfacePixels[destination++] = palette[sourceSelectors[selectorRow + selectorX]];
            }
        }
    }

    /**
     * Java-only immutable nearest-neighbor axis mapping retained while source/destination dimensions stay unchanged.
     */
    private record ScaleAxisLookup(int sourceSize, int destinationSize, int[] sourceIndexByDestinationIndex) {
        /**
         * not ported. Reuses or rebuilds one static axis mapping for opaque scaled presentation.
         */
        private static ScaleAxisLookup resolve(ScaleAxisLookup current, int sourceSize, int destinationSize) {
            if (current != null
                    && current.sourceSize == sourceSize
                    && current.destinationSize == destinationSize) {
                return current;
            }
            int[] sourceIndexes = new int[destinationSize];
            for (int destinationIndex = 0; destinationIndex < destinationSize; destinationIndex++) {
                sourceIndexes[destinationIndex] = (destinationIndex * sourceSize) / destinationSize;
            }
            return new ScaleAxisLookup(sourceSize, destinationSize, sourceIndexes);
        }
    }

    /**
     * Java decoded-frame boundary for normal palette-indexed sprite rendering.
     * not ported.
     */
    @Override
    public void drawIndexedSprite(int x, int y, GameBitmapFrame frame, int[] palette, boolean flipX) {
        int width = frame.width();
        int height = frame.height();
        if (width <= 0 || height <= 0) {
            return;
        }

        int[] indices = frame.pixels();
        GameBitmapFrame.RowCoverageSpans coverageSpans = frame.coverageSpans();
        int[] surface = activeRenderTarget.surface();
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int pitchPixels = activeRenderTarget.pitchPixels();

        for (int sourceY = 0; sourceY < height; sourceY++) {
            int destinationY = y + sourceY;
            if (destinationY < clipTop || destinationY >= clipBottom) {
                continue;
            }
            int sourceRowIndex = sourceY * width;
            for (int spanIndex = coverageSpans.firstSpanIndex(sourceY);
                 spanIndex < coverageSpans.endSpanIndex(sourceY);
                 spanIndex++) {
                int sourceStart = coverageSpans.start(spanIndex);
                int sourceEnd = coverageSpans.end(spanIndex);
                int destinationSpanLeft = flipX ? x + width - sourceEnd : x + sourceStart;
                int destinationSpanRight = flipX ? x + width - sourceStart : x + sourceEnd;
                int visibleLeft = Math.max(destinationSpanLeft, clipLeft);
                int visibleRight = Math.min(destinationSpanRight, clipRight);
                if (visibleLeft >= visibleRight) {
                    continue;
                }

                int sourceX = flipX ? width - 1 - (visibleLeft - x) : visibleLeft - x;
                int sourceStep = flipX ? -1 : 1;
                int sourceIndex = sourceRowIndex + sourceX;
                int destinationIndex = (destinationY - surfaceTop) * pitchPixels + (visibleLeft - surfaceLeft);
                for (int destinationX = visibleLeft; destinationX < visibleRight; destinationX++) {
                    surface[destinationIndex] = palette[indices[sourceIndex]];
                    sourceIndex += sourceStep;
                    destinationIndex++;
                }
            }
        }
    }

    /**
     * Java decoded-frame boundary for native fixed-half palette-indexed sprite blending.
     * not ported.
     */
    @Override
    public void drawIndexedSpriteBlend(int x, int y, GameBitmapFrame frame, int[] palette, boolean flipX) {
        int width = frame.width();
        int height = frame.height();
        if (width <= 0 || height <= 0) {
            return;
        }

        int[] indices = frame.pixels();
        GameBitmapFrame.RowCoverageSpans coverageSpans = frame.coverageSpans();
        int[] surface = activeRenderTarget.surface();
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int pitchPixels = activeRenderTarget.pitchPixels();
        for (int sourceY = 0; sourceY < height; sourceY++) {
            int destinationY = y + sourceY;
            if (destinationY < clipTop || destinationY >= clipBottom) {
                continue;
            }
            int sourceRowIndex = sourceY * width;
            for (int spanIndex = coverageSpans.firstSpanIndex(sourceY);
                 spanIndex < coverageSpans.endSpanIndex(sourceY);
                 spanIndex++) {
                int sourceStart = coverageSpans.start(spanIndex);
                int sourceEnd = coverageSpans.end(spanIndex);
                int destinationSpanLeft = flipX ? x + width - sourceEnd : x + sourceStart;
                int destinationSpanRight = flipX ? x + width - sourceStart : x + sourceEnd;
                int visibleLeft = Math.max(destinationSpanLeft, clipLeft);
                int visibleRight = Math.min(destinationSpanRight, clipRight);
                if (visibleLeft >= visibleRight) {
                    continue;
                }

                int sourceX = flipX ? width - 1 - (visibleLeft - x) : visibleLeft - x;
                int sourceStep = flipX ? -1 : 1;
                int sourceIndex = sourceRowIndex + sourceX;
                int destinationIndex = (destinationY - surfaceTop) * pitchPixels + (visibleLeft - surfaceLeft);
                for (int destinationX = visibleLeft; destinationX < visibleRight; destinationX++) {
                    int source = palette[indices[sourceIndex]];
                    int destination = surface[destinationIndex];
                    surface[destinationIndex] = RGB32.a(destination) == 0xFF
                            ? RGB32.blendHalfOpaque(source, destination)
                            : RGB32.sourceOver(RGB32.scaleAlpha(source, 0x80), destination);
                    sourceIndex += sourceStep;
                    destinationIndex++;
                }
            }
        }
    }

    /**
     * Java decoded-frame boundary for resolving canonical A16 codes through one compact palette-generation lookup.
     * not ported.
     */
    @Override
    public void drawA16Sprite(
            int x,
            int y,
            GameBitmapFrame frame,
            A16PaletteLookup paletteLookup,
            boolean flipX
    ) {
        int width = frame.width();
        int height = frame.height();
        if (width <= 0 || height <= 0) {
            return;
        }

        int[] encodedPixels = frame.pixels();
        GameBitmapFrame.RowCoverageSpans coverageSpans = frame.coverageSpans();
        int[] surface = activeRenderTarget.surface();
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int pitchPixels = activeRenderTarget.pitchPixels();

        for (int sourceY = 0; sourceY < height; sourceY++) {
            int destinationY = y + sourceY;
            if (destinationY < clipTop || destinationY >= clipBottom) {
                continue;
            }
            int sourceRowIndex = sourceY * width;
            for (int spanIndex = coverageSpans.firstSpanIndex(sourceY);
                 spanIndex < coverageSpans.endSpanIndex(sourceY);
                 spanIndex++) {
                int sourceStart = coverageSpans.start(spanIndex);
                int sourceEnd = coverageSpans.end(spanIndex);
                int destinationSpanLeft = flipX ? x + width - sourceEnd : x + sourceStart;
                int destinationSpanRight = flipX ? x + width - sourceStart : x + sourceEnd;
                int visibleLeft = Math.max(destinationSpanLeft, clipLeft);
                int visibleRight = Math.min(destinationSpanRight, clipRight);
                if (visibleLeft >= visibleRight) {
                    continue;
                }

                int sourceX = flipX ? width - 1 - (visibleLeft - x) : visibleLeft - x;
                int sourceStep = flipX ? -1 : 1;
                int sourceIndex = sourceRowIndex + sourceX;
                int destinationIndex = (destinationY - surfaceTop) * pitchPixels + (visibleLeft - surfaceLeft);
                for (int destinationX = visibleLeft; destinationX < visibleRight; destinationX++) {
                    int encodedPixel = encodedPixels[sourceIndex];
                    int source = paletteLookup.sourceColor(encodedPixel);
                    surface[destinationIndex] = RGB32.sourceOver(source, surface[destinationIndex]);
                    sourceIndex += sourceStep;
                    destinationIndex++;
                }
            }
        }
    }

    /**
     * Java decoded-frame boundary that uses decoded RLE coverage for native shade-page semantics.
     * not ported.
     */
    @Override
    public void drawIndexedSpriteShade(int x, int y, GameBitmapFrame frame, int shadePage, boolean flipX) {
        drawIndexedSpriteShadeInternal(x, y, frame, shadePage, 0, false, flipX);
    }

    /**
     * Java decoded-frame boundary for sheared destination shade-page semantics using decoded RLE coverage.
     * not ported.
     */
    @Override
    public void drawIndexedSpriteShearedShade(int x, int y, GameBitmapFrame frame,
                                              int shadePage, int slope, boolean flipX) {
        drawIndexedSpriteShadeInternal(x, y, frame, shadePage, slope, true, flipX);
    }

    /**
     * Java decoded-frame boundary that fills decoded RLE coverage with one straight-ARGB color.
     * not ported.
     */
    @Override
    public void drawIndexedSpriteSolid(int x, int y, GameBitmapFrame frame, int fillColor, boolean flipX) {
        int width = frame.width();
        int height = frame.height();
        if (width <= 0 || height <= 0) {
            return;
        }

        GameBitmapFrame.RowCoverageSpans coverageSpans = frame.coverageSpans();
        int[] surface = activeRenderTarget.surface();
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int pitchPixels = activeRenderTarget.pitchPixels();
        boolean directOpaqueFill = RGB32.a(fillColor) == 0xFF;

        for (int sourceY = 0; sourceY < height; sourceY++) {
            int destinationY = y + sourceY;
            if (destinationY < clipTop || destinationY >= clipBottom) {
                continue;
            }
            for (int spanIndex = coverageSpans.firstSpanIndex(sourceY);
                 spanIndex < coverageSpans.endSpanIndex(sourceY);
                 spanIndex++) {
                int sourceStart = coverageSpans.start(spanIndex);
                int sourceEnd = coverageSpans.end(spanIndex);
                int destinationSpanLeft = flipX ? x + width - sourceEnd : x + sourceStart;
                int destinationSpanRight = flipX ? x + width - sourceStart : x + sourceEnd;
                int visibleLeft = Math.max(destinationSpanLeft, clipLeft);
                int visibleRight = Math.min(destinationSpanRight, clipRight);
                if (visibleLeft >= visibleRight) {
                    continue;
                }

                int destinationIndex = (destinationY - surfaceTop) * pitchPixels + (visibleLeft - surfaceLeft);
                if (directOpaqueFill) {
                    Arrays.fill(surface, destinationIndex, destinationIndex + visibleRight - visibleLeft, fillColor);
                    continue;
                }
                for (int destinationX = visibleLeft; destinationX < visibleRight; destinationX++) {
                    surface[destinationIndex] = RGB32.sourceOver(fillColor, surface[destinationIndex]);
                    destinationIndex++;
                }
            }
        }
    }

    /**
     * not ported. Traverses decoded RLE row coverage for direct and sheared destination shade-page rendering.
     */
    protected void drawIndexedSpriteShadeInternal(int x, int y, GameBitmapFrame frame, int shadePage,
                                                  int slope, boolean sheared, boolean flipX) {
        int width = frame.width();
        int height = frame.height();
        if (width <= 0 || height <= 0) {
            return;
        }

        GameBitmapFrame.RowCoverageSpans coverageSpans = frame.coverageSpans();
        int[] surface = activeRenderTarget.surface();
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int pitchPixels = activeRenderTarget.pitchPixels();
        int shearFraction = 0;
        int shearX = sheared ? (slope * height) / 0x10000 : 0;

        for (int sourceY = 0; sourceY < height; sourceY++) {
            int destinationY = y + sourceY;
            int destinationRowX = x + shearX;
            if (destinationY >= clipTop && destinationY < clipBottom) {
                for (int spanIndex = coverageSpans.firstSpanIndex(sourceY);
                     spanIndex < coverageSpans.endSpanIndex(sourceY);
                     spanIndex++) {
                    int sourceStart = coverageSpans.start(spanIndex);
                    int sourceEnd = coverageSpans.end(spanIndex);
                    int destinationSpanLeft = flipX
                            ? destinationRowX + width - sourceEnd
                            : destinationRowX + sourceStart;
                    int destinationSpanRight = flipX
                            ? destinationRowX + width - sourceStart
                            : destinationRowX + sourceEnd;
                    int visibleLeft = Math.max(destinationSpanLeft, clipLeft);
                    int visibleRight = Math.min(destinationSpanRight, clipRight);
                    if (visibleLeft >= visibleRight) {
                        continue;
                    }

                    int destinationIndex = (destinationY - surfaceTop) * pitchPixels
                            + (visibleLeft - surfaceLeft);
                    for (int destinationX = visibleLeft; destinationX < visibleRight; destinationX++) {
                        surface[destinationIndex] = RGB32.withShade(surface[destinationIndex], shadePage);
                        destinationIndex++;
                    }
                }
            }

            if (sheared) {
                int shearStep = shearFraction + slope;
                shearFraction = shearStep & 0xFFFF;
                shearX -= shearStep >> 16;
            }
        }
    }

    /**
     * Native: DrawSoftDot @0045724A.
     * Fully ported.
     */
    @Override
    public void drawSoftDot(int x, int y, int red, int green, int blue, int alpha) {
        blendPixel16(x, y, red, green, blue, alpha);
        int alphaThreeQuarter = (alpha * 3) / 4;
        blendPixel16(x + 1, y, red, green, blue, alphaThreeQuarter);
        blendPixel16(x, y + 1, red, green, blue, alphaThreeQuarter);
        blendPixel16(x - 1, y, red, green, blue, alphaThreeQuarter);
        blendPixel16(x, y - 1, red, green, blue, alphaThreeQuarter);
        int alphaHalf = alpha / 2;
        blendPixel16(x + 2, y, red, green, blue, alphaHalf);
        blendPixel16(x, y + 2, red, green, blue, alphaHalf);
        blendPixel16(x - 2, y, red, green, blue, alphaHalf);
        blendPixel16(x, y - 2, red, green, blue, alphaHalf);
        int alphaQuarter = alpha / 4;
        blendPixel16(x + 1, y + 1, red, green, blue, alphaQuarter);
        blendPixel16(x - 1, y + 1, red, green, blue, alphaQuarter);
        blendPixel16(x + 1, y - 1, red, green, blue, alphaQuarter);
        blendPixel16(x - 1, y - 1, red, green, blue, alphaQuarter);
    }

    /**
     * Native: BlitToScreen @004538DD.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void blitToScreen(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, int[] srcData, int srcWidth, int srcHeight) {
        blitToScreenCore(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom, srcData, srcWidth, srcHeight, false);
    }

    /**
     * Native: BlitToScreenMasked @00453BCA.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void blitToScreenMasked(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, int[] srcData, int srcWidth, int srcHeight) {
        blitToScreenCore(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom, srcData, srcWidth, srcHeight, true);
    }

    /**
     * Native: BlitToScreenAdditive @00453A58.
     */
    @Override
    public void blitToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                     int[] srcData, int srcWidth, int srcHeight) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();

        int width = srcRight - srcLeft;
        int height = srcBottom - srcTop;

        if (srcLeft < 0) {
            int delta = -srcLeft;
            width -= delta;
            dstX += delta;
            srcLeft = 0;
        }
        if (srcTop < 0) {
            int delta = -srcTop;
            height -= delta;
            dstY += delta;
            srcTop = 0;
        }
        if (srcLeft + width > srcWidth) {
            width = srcWidth - srcLeft;
        }
        if (srcTop + height > srcHeight) {
            height = srcHeight - srcTop;
        }

        if (dstX < clipLeft) {
            int delta = clipLeft - dstX;
            width -= delta;
            srcLeft += delta;
            dstX = clipLeft;
        }

        if (dstY < clipTop) {
            int delta = clipTop - dstY;
            height -= delta;
            srcTop += delta;
            dstY = clipTop;
        }

        if (dstX + width > clipRight) {
            width -= (dstX + width - clipRight);
        }

        if (dstY + height > clipBottom) {
            height -= (dstY + height - clipBottom);
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        int srcRowIndex = (srcHeight - 1 - srcTop) * srcWidth + srcLeft;
        int dstOffset = (dstY - surfaceTop) * pitchPixels + (dstX - surfaceLeft);

        for (int rowsLeft = height; rowsLeft > 0; rowsLeft--) {
            int srcIndex = srcRowIndex;
            int di = dstOffset;
            for (int x = 0; x < width; x++) {
                putPixelAdditive(surface, di, srcData[srcIndex + x]);
                di++;
            }
            srcRowIndex -= srcWidth;
            dstOffset += pitchPixels;
        }
    }

    /**
     * Native: BlitIndexedToScreen @00453D4B.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void blitIndexedToScreen(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                    int[] srcData, int srcWidth, int srcHeight, int[] palette) {
        blitIndexedToScreenCore(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom, srcData, srcWidth, srcHeight, palette, false);
    }

    /**
     * Native: BlitIndexedToScreenAdditive @00453F69.
     */
    @Override
    public void blitIndexedToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                            int[] srcData, int srcWidth, int srcHeight, int[] palette) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();

        int width = srcRight - srcLeft;
        int height = srcBottom - srcTop;

        if (dstX < clipLeft) {
            int delta = clipLeft - dstX;
            width -= delta;
            srcLeft += delta;
            dstX = clipLeft;
        }

        if (dstY < clipTop) {
            int delta = clipTop - dstY;
            height -= delta;
            srcTop += delta;
            dstY = clipTop;
        }

        if (dstX + width > clipRight) {
            width -= (dstX + width - clipRight);
        }

        if (dstY + height > clipBottom) {
            height -= (dstY + height - clipBottom);
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        int srcRowIndex = (srcHeight - 1 - srcTop) * srcWidth + srcLeft;
        int dstOffset = (dstY - surfaceTop) * pitchPixels + (dstX - surfaceLeft);

        for (int rowsLeft = height; rowsLeft > 0; rowsLeft--) {
            int srcIndex = srcRowIndex;
            int di = dstOffset;
            for (int x = 0; x < width; x++) {
                int paletteIndex = srcData[srcIndex + x];
                putPixelAdditive(surface, di, palette[paletteIndex]);
                di++;
            }
            srcRowIndex -= srcWidth;
            dstOffset += pitchPixels;
        }
    }

    /**
     * Java helper for paletted screen blits with palette-index zero transparency.
     * not ported.
     */
    @Override
    public void blitIndexedToScreenMasked(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                          int[] srcData, int srcWidth, int srcHeight, int[] palette) {
        blitIndexedToScreenCore(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom, srcData, srcWidth, srcHeight, palette, true);
    }

    /**
     * Java helper for straight-ARGB screen blits used by the native global blit ports.
     * Native support extracted from BlitToScreen @004538DD and BlitToScreenMasked @00453BCA.
     */
    protected void blitToScreenCore(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                    int[] srcData, int srcWidth, int srcHeight, boolean zeroTransparent) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();

        int width = srcRight - srcLeft;
        int height = srcBottom - srcTop;

        // Native clips only the destination rect and trusts the source pointer range. Java CBmp64k sources are bounded
        // arrays, so clamp the requested source rect before the native-style bottom-up row walk.
        if (srcLeft < 0) {
            int delta = -srcLeft;
            width -= delta;
            dstX += delta;
            srcLeft = 0;
        }
        if (srcTop < 0) {
            int delta = -srcTop;
            height -= delta;
            dstY += delta;
            srcTop = 0;
        }
        if (srcLeft + width > srcWidth) {
            width = srcWidth - srcLeft;
        }
        if (srcTop + height > srcHeight) {
            height = srcHeight - srcTop;
        }

        if (dstX < clipLeft) {
            int delta = clipLeft - dstX;
            width -= delta;
            srcLeft += delta;
            dstX = clipLeft;
        }

        if (dstY < clipTop) {
            int delta = clipTop - dstY;
            height -= delta;
            srcTop += delta;
            dstY = clipTop;
        }

        if (dstX + width > clipRight) {
            width -= (dstX + width - clipRight);
        }

        if (dstY + height > clipBottom) {
            height -= (dstY + height - clipBottom);
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        int srcStartRow = (srcHeight - 1 - srcTop) * srcWidth + srcLeft;
        int dstOffset = (dstY - surfaceTop) * pitchPixels + (dstX - surfaceLeft);

        int rowsLeft = height;
        int srcRowIndex = srcStartRow;

        while (rowsLeft > 0) {
            int srcIndex = srcRowIndex;
            int di = dstOffset;

            for (int x = 0; x < width; x++) {
                int pixel = srcData[srcIndex + x];
                if (!zeroTransparent || (pixel & 0x00FF_FFFF) != 0) {
                    putPixelSourceOver(surface, di, pixel);
                }
                di++;
            }

            srcRowIndex -= srcWidth;
            dstOffset += pitchPixels;
            rowsLeft--;
        }
    }

    /**
     * Java helper for indexed screen blits used by the CBmp256 native helper ports.
     * Native support extracted from BlitIndexedToScreen @00453D4B.
     */
    protected void blitIndexedToScreenCore(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                           int[] srcData, int srcWidth, int srcHeight, int[] palette, boolean zeroTransparent) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();

        int width = srcRight - srcLeft;
        int height = srcBottom - srcTop;

        if (dstX < clipLeft) {
            int delta = clipLeft - dstX;
            width -= delta;
            srcLeft += delta;
            dstX = clipLeft;
        }

        if (dstY < clipTop) {
            int delta = clipTop - dstY;
            height -= delta;
            srcTop += delta;
            dstY = clipTop;
        }

        if (dstX + width > clipRight) {
            width -= (dstX + width - clipRight);
        }

        if (dstY + height > clipBottom) {
            height -= (dstY + height - clipBottom);
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        int srcRowIndex = srcTop * srcWidth + srcLeft;
        int dstOffset = (dstY - surfaceTop) * pitchPixels + (dstX - surfaceLeft);

        for (int rowsLeft = height; rowsLeft > 0; rowsLeft--) {
            int srcIndex = srcRowIndex;
            int di = dstOffset;
            for (int x = 0; x < width; x++) {
                int paletteIndex = srcData[srcIndex + x];
                if (!(zeroTransparent && paletteIndex == 0)) {
                    putPixelSourceOver(surface, di, palette[paletteIndex]);
                }
                di++;
            }
            srcRowIndex += srcWidth;
            dstOffset += pitchPixels;
        }
    }

    /**
     * Native: DrawFlatTerrainTileToScreen @00457487.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawFlatTerrainTile(int dstX, int topY,
                                    int topLeftBrightness, int topRightBrightness,
                                    int bottomLeftBrightness, int bottomRightBrightness,
                                    int[] sourcePixels, int sourceOffset, Palette16[] palettePages) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();

        if (dstX < clipLeft || dstX + TERRAIN_TILE_SIZE > clipRight) {
            return;
        }

        int topBrightnessFixed = topLeftBrightness << TERRAIN_BRIGHTNESS_FIXED_SHIFT;
        int bottomBrightnessFixed = bottomLeftBrightness << TERRAIN_BRIGHTNESS_FIXED_SHIFT;
        int topBrightnessStep = (topRightBrightness - topLeftBrightness) << 4;
        int bottomBrightnessStep = (bottomRightBrightness - bottomLeftBrightness) << 4;
        for (int column = 0; column < TERRAIN_TILE_SIZE; column++) {
            int screenX = dstX + column;
            int brightnessFixed = topBrightnessFixed + TERRAIN_BRIGHTNESS_FIXED_HALF;
            int brightnessStep = truncateDivideBy32(bottomBrightnessFixed - topBrightnessFixed);
            int sourceIndex = sourceOffset + column;
            if (screenX >= clipLeft && screenX < clipRight) {
                for (int row = 0; row < TERRAIN_TILE_SIZE; row++) {
                    int screenY = topY + row;
                    if (screenY >= clipTop && screenY < clipBottom) {
                        int destinationOffset = (screenY - surfaceTop) * pitchPixels + (screenX - surfaceLeft);
                        putTerrainPixel(surface, destinationOffset, sourcePixels, sourceIndex, palettePages,
                                brightnessFixed >> TERRAIN_BRIGHTNESS_FIXED_SHIFT);
                    }
                    sourceIndex += TERRAIN_TILE_SIZE;
                    brightnessFixed += brightnessStep;
                }
            }
            topBrightnessFixed += topBrightnessStep;
            bottomBrightnessFixed += bottomBrightnessStep;
        }
    }

    /**
     * Native: DrawSkewedTerrainTileToScreen @004575FE.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSkewedTerrainTile(int leftX, int rightX,
                                      int topLeftY, int topRightY,
                                      int bottomLeftY, int bottomRightY,
                                      int topLeftBrightness, int topRightBrightness,
                                      int bottomLeftBrightness, int bottomRightBrightness,
                                      int[] sourcePixels, int sourceOffset, Palette16[] palettePages) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();

        if (leftX < clipLeft || rightX > clipRight
                || (topLeftY >= clipBottom && topRightY >= clipBottom)
                || (bottomLeftY < clipTop && bottomRightY < clipTop)) {
            return;
        }

        int topBrightnessFixed = topLeftBrightness << TERRAIN_BRIGHTNESS_FIXED_SHIFT;
        int bottomBrightnessFixed = bottomLeftBrightness << TERRAIN_BRIGHTNESS_FIXED_SHIFT;
        int topBrightnessStep = (topRightBrightness - topLeftBrightness) << 4;
        int bottomBrightnessStep = (bottomRightBrightness - bottomLeftBrightness) << 4;
        for (int column = 0; column < TERRAIN_TILE_SIZE; column++) {
            int screenX = leftX + column;

            int topY = skewedTopTerrainEdgeY(topLeftY, topRightY, column);
            int bottomY = skewedBottomTerrainEdgeY(bottomLeftY, bottomRightY, column);
            int height = bottomY - topY;
            if (height > 0) {
                int brightnessFixed = topBrightnessFixed + TERRAIN_BRIGHTNESS_FIXED_HALF;
                int brightnessStep = truncateDivide(bottomBrightnessFixed - topBrightnessFixed, height);
                for (int y = topY; y < bottomY; y++) {
                    int sourceY = ((y - topY) * TERRAIN_TILE_SIZE) / height;
                    int sourceIndex = sourceOffset + sourceY * TERRAIN_TILE_SIZE + column;
                    if (y >= clipTop && y < clipBottom) {
                        int destinationOffset = (y - surfaceTop) * pitchPixels + (screenX - surfaceLeft);
                        putTerrainPixel(surface, destinationOffset, sourcePixels, sourceIndex, palettePages,
                                brightnessFixed >> TERRAIN_BRIGHTNESS_FIXED_SHIFT);
                    }
                    brightnessFixed += brightnessStep;
                }
            }
            topBrightnessFixed += topBrightnessStep;
            bottomBrightnessFixed += bottomBrightnessStep;
        }
    }

    /**
     * Native support extracted from DrawSkewedTerrainTileToScreen @004575FE edge stepping.
     */
    protected static int skewedTopTerrainEdgeY(int leftY, int rightY, int column) {
        int delta = Math.abs(rightY - leftY);
        if (delta == 0) {
            return leftY;
        }
        int y = leftY;
        if (leftY < rightY) {
            for (int edgeIndex = 0;
                 edgeIndex < delta && Byte.toUnsignedInt(RENDER_EFFECT_TABLE[delta][edgeIndex]) <= column;
                 edgeIndex++) {
                y++;
            }
        } else {
            for (int edgeIndex = delta - 1;
                 edgeIndex >= 0 && TERRAIN_TILE_SIZE - Byte.toUnsignedInt(RENDER_EFFECT_TABLE[delta][edgeIndex]) <= column;
                 edgeIndex--) {
                y--;
            }
        }
        return y;
    }

    /**
     * Native support extracted from DrawSkewedTerrainTileToScreen @004575FE edge stepping.
     */
    protected static int skewedBottomTerrainEdgeY(int leftY, int rightY, int column) {
        int delta = Math.abs(rightY - leftY);
        if (delta == 0) {
            return leftY;
        }
        int y = leftY;
        if (leftY < rightY) {
            for (int edgeIndex = delta - 1;
                 edgeIndex >= 0 && TERRAIN_TILE_SIZE - Byte.toUnsignedInt(RENDER_EFFECT_TABLE[delta][edgeIndex]) <= column;
                 edgeIndex--) {
                y++;
            }
        } else {
            for (int edgeIndex = 0;
                 edgeIndex < delta && Byte.toUnsignedInt(RENDER_EFFECT_TABLE[delta][edgeIndex]) <= column;
                 edgeIndex++) {
                y--;
            }
        }
        return y;
    }

    /**
     * Java-only terrain-cache clipping support. Reuses the exact native skewed top-edge stepping recovered in
     * DrawSkewedTerrainTileToScreen @004575FE, but clears the preceding-row overdraw from a copied world cache.
     * not ported.
     */
    @Override
    public void clearRowsAboveTerrainEdge(int leftX, int topLeftY, int topRightY, int color) {
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int pitchPixels = activeRenderTarget.pitchPixels();
        int[] surface = activeRenderTarget.surface();
        int clippedTop = Math.max(clipTop, surfaceTop);
        int clippedRight = Math.min(clipRight, activeRenderTarget.cx());
        for (int column = 0; column < TERRAIN_TILE_SIZE; column++) {
            int screenX = leftX + column;
            if (screenX < clipLeft || screenX < surfaceLeft || screenX >= clippedRight) {
                continue;
            }
            int clippedBottom = Math.min(
                    Math.min(skewedTopTerrainEdgeY(topLeftY, topRightY, column), clipBottom),
                    activeRenderTarget.cy()
            );
            int destinationOffset = (clippedTop - surfaceTop) * pitchPixels + screenX - surfaceLeft;
            for (int screenY = clippedTop; screenY < clippedBottom; screenY++) {
                surface[destinationOffset] = color;
                destinationOffset += pitchPixels;
            }
        }
    }

    /**
     * Native: ClearFlatTerrainSlopeMask @00457944.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void clearFlatTerrainSlopeMask(int dstX, int topY, int bottomY) {
        fillScreenRect(dstX, topY, dstX + TERRAIN_TILE_SIZE, bottomY, RGB32.BLACK);
    }

    /**
     * Native: ClearSkewedTerrainSlopeMask @00457A4D.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void clearSkewedTerrainSlopeMask(int leftX, int rightX,
                                            int topLeftY, int topRightY,
                                            int bottomLeftY, int bottomRightY) {
        applyTerrainSlopeMaskShape(leftX, rightX, topLeftY, topRightY, bottomLeftY, bottomRightY,
                TerrainSlopeMaskMode.CLEAR);
    }

    /**
     * Native: DimFlatTerrainSlopeMask @004582BC.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void dimFlatTerrainSlopeMask(int dstX, int topY, int bottomY) {
        applyShadeToRect(dstX, topY, dstX + TERRAIN_TILE_SIZE, bottomY, TERRAIN_SLOPE_DIM_BRIGHTNESS);
    }

    /**
     * Native: DimSkewedTerrainSlopeMask @004584D0.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void dimSkewedTerrainSlopeMask(int leftX, int rightX,
                                          int topLeftY, int topRightY,
                                          int bottomLeftY, int bottomRightY) {
        applyTerrainSlopeMaskShape(leftX, rightX, topLeftY, topRightY, bottomLeftY, bottomRightY,
                TerrainSlopeMaskMode.DIM);
    }

    /**
     * Native: ApplyFlatTerrainSlopeMaskBrightness @00457DA7.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void applyFlatTerrainSlopeMaskBrightness(int dstX, int topY,
                                                    int topLeftBrightness, int topRightBrightness,
                                                    int bottomLeftBrightness, int bottomRightBrightness) {
        if (dstX < clipLeft || dstX + TERRAIN_TILE_SIZE > clipRight
                || topY >= clipBottom
                || topY + TERRAIN_TILE_SIZE < clipTop) {
            return;
        }

        int topBrightnessFixed = topLeftBrightness << TERRAIN_BRIGHTNESS_FIXED_SHIFT;
        int bottomBrightnessFixed = bottomLeftBrightness << TERRAIN_BRIGHTNESS_FIXED_SHIFT;
        int topBrightnessStep = (topRightBrightness - topLeftBrightness) << 4;
        int bottomBrightnessStep = (bottomRightBrightness - bottomLeftBrightness) << 4;
        for (int column = 0; column < TERRAIN_TILE_SIZE; column++) {
            int brightnessFixed = topBrightnessFixed + TERRAIN_BRIGHTNESS_FIXED_HALF;
            int brightnessStep = truncateDivideBy32(bottomBrightnessFixed - topBrightnessFixed);
            applyTerrainSlopeBrightnessColumn(dstX + column, topY, topY + TERRAIN_TILE_SIZE,
                    brightnessFixed, brightnessStep);
            topBrightnessFixed += topBrightnessStep;
            bottomBrightnessFixed += bottomBrightnessStep;
        }
    }

    /**
     * Native: ApplySkewedTerrainSlopeMaskBrightness @00457F5B.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void applySkewedTerrainSlopeMaskBrightness(int leftX, int rightX,
                                                      int topLeftY, int topRightY,
                                                      int bottomLeftY, int bottomRightY,
                                                      int topLeftBrightness, int topRightBrightness,
                                                      int bottomLeftBrightness, int bottomRightBrightness) {
        if (leftX < clipLeft || rightX > clipRight
                || (topLeftY >= clipBottom && topRightY >= clipBottom)
                || (bottomLeftY < clipTop && bottomRightY < clipTop)) {
            return;
        }

        int topBrightnessFixed = topLeftBrightness << TERRAIN_BRIGHTNESS_FIXED_SHIFT;
        int bottomBrightnessFixed = bottomLeftBrightness << TERRAIN_BRIGHTNESS_FIXED_SHIFT;
        int topBrightnessStep = (topRightBrightness - topLeftBrightness) << 4;
        int bottomBrightnessStep = (bottomRightBrightness - bottomLeftBrightness) << 4;
        for (int column = 0; column < TERRAIN_TILE_SIZE; column++) {
            int screenX = leftX + column;
            int topY = skewedTopTerrainEdgeY(topLeftY, topRightY, column);
            int bottomY = skewedBottomTerrainEdgeY(bottomLeftY, bottomRightY, column);
            int height = bottomY - topY;
            if (height > 0) {
                int brightnessFixed = topBrightnessFixed + TERRAIN_BRIGHTNESS_FIXED_HALF;
                int brightnessStep = truncateDivide(bottomBrightnessFixed - topBrightnessFixed, height);
                applyTerrainSlopeBrightnessColumn(screenX, topY, bottomY, brightnessFixed, brightnessStep);
            }
            topBrightnessFixed += topBrightnessStep;
            bottomBrightnessFixed += bottomBrightnessStep;
        }
    }

    /**
     * Native support extracted from ClearSkewedTerrainSlopeMask @00457A4D and DimSkewedTerrainSlopeMask @004584D0.
     */
    protected void applyTerrainSlopeMaskShape(int leftX, int rightX,
                                              int topLeftY, int topRightY,
                                              int bottomLeftY, int bottomRightY,
                                              TerrainSlopeMaskMode mode) {
        if (leftX < clipLeft || rightX > clipRight
                || (topLeftY >= clipBottom && topRightY >= clipBottom)
                || (bottomLeftY < clipTop && bottomRightY < clipTop)) {
            return;
        }

        int middleTopY = Math.max(topLeftY, topRightY);
        int middleBottomY = Math.min(bottomLeftY, bottomRightY);
        if (middleTopY >= middleBottomY) {
            int nativeMaskPage = mode == TerrainSlopeMaskMode.CLEAR
                    ? TERRAIN_SLOPE_CLEAR_MASK_PAGE
                    : TERRAIN_SLOPE_DIM_MASK_PAGE;
            applySkewedTerrainSlopeMaskBrightness(leftX, rightX, topLeftY, topRightY, bottomLeftY, bottomRightY,
                    nativeMaskPage, nativeMaskPage, nativeMaskPage, nativeMaskPage);
            return;
        }

        for (int column = 0; column < TERRAIN_TILE_SIZE; column++) {
            int screenX = leftX + column;
            if (screenX >= rightX) {
                break;
            }
            int topY = skewedTopTerrainEdgeY(topLeftY, topRightY, column);
            int bottomY = skewedBottomTerrainEdgeY(bottomLeftY, bottomRightY, column);
            if (mode == TerrainSlopeMaskMode.CLEAR) {
                fillScreenRect(screenX, topY, screenX + 1, bottomY, RGB32.BLACK);
            } else {
                applyShadeToRect(screenX, topY, screenX + 1, bottomY, TERRAIN_SLOPE_DIM_BRIGHTNESS);
            }
        }
    }

    protected enum TerrainSlopeMaskMode {
        CLEAR,
        DIM
    }

    /**
     * Native support extracted from ApplyFlatTerrainSlopeMaskBrightness @00457DA7 and
     * ApplySkewedTerrainSlopeMaskBrightness @00457F5B.
     */
    protected void applyTerrainSlopeBrightnessColumn(int screenX, int topY, int bottomY,
                                                     int brightnessFixed, int brightnessStep) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int surfaceRight = activeRenderTarget.cx();
        final int surfaceBottom = activeRenderTarget.cy();

        // Native clips against gScreenRect before dereferencing. Java can draw this native sequence into the
        // zoom offscreen target, so clamp to the active bounded render target too.
        int clippedLeft = Math.max(clipLeft, surfaceLeft);
        int clippedRight = Math.min(clipRight, surfaceRight);
        if (screenX < clippedLeft || screenX >= clippedRight) {
            return;
        }
        int clippedTop = Math.max(Math.max(topY, clipTop), surfaceTop);
        int clippedBottom = Math.min(Math.min(bottomY, clipBottom), surfaceBottom);
        if (clippedTop >= clippedBottom) {
            return;
        }

        final int pitchPixels = activeRenderTarget.pitchPixels();
        final int[] surface = activeRenderTarget.surface();
        brightnessFixed += (clippedTop - topY) * brightnessStep;
        int offset = (clippedTop - surfaceTop) * pitchPixels + (screenX - surfaceLeft);
        for (int y = clippedTop; y < clippedBottom; y++) {
            int shade = brightnessFixed >> TERRAIN_BRIGHTNESS_FIXED_SHIFT;
            int color = getPixel(surface, offset);
            putPixel(surface, offset, RGB32.withShade(color, shade));
            brightnessFixed += brightnessStep;
            offset += pitchPixels;
        }
    }


    /**
     * Native support extracted from DrawFlatTerrainTileToScreen @00457487 signed fixed-point division.
     */
    protected static int truncateDivideBy32(int value) {
        return (value + ((value >> 31) & 0x1F)) >> 5;
    }

    /**
     * Native support extracted from DrawSkewedTerrainTileToScreen @004575FE signed integer division.
     */
    protected static int truncateDivide(int value, int divisor) {
        return value / divisor;
    }

    /**
     * Native support extracted from DrawFlatTerrainTileToScreen @00457487 and DrawSkewedTerrainTileToScreen @004575FE.
     */
    protected static void putTerrainPixel(int[] surface, int destinationIndex, int[] sourcePixels,
                                           int sourceIndex, Palette16[] palettePages, int brightnessPage) {
        int[] palette = palettePages[brightnessPage].data();
        int sourcePixel = sourcePixels[sourceIndex];
        int color = palette[sourcePixel];
        putPixel(surface, destinationIndex, color);
    }

    // not ported.
    protected static void putPixel(int[] surface, int pixelIndex, int color) {
       // if (pixelIndex < 0 || pixelIndex >= surface.length) return;
        surface[pixelIndex] = color;
    }

    /**
     * not ported. Reads one packed straight-ARGB pixel from a software-surface pixel index.
     */
    protected static int getPixel(int[] surface, int pixelIndex) {
        return surface[pixelIndex];
    }

    /**
     * not ported. Composites one straight-ARGB pixel into the software surface.
     */
    protected static void putPixelSourceOver(int[] surface, int pixelIndex, int source) {
       // if (pixelIndex < 0 || pixelIndex >= surface.length) return;
        surface[pixelIndex] = RGB32.sourceOver(source, surface[pixelIndex]);
    }

    /**
     * Native support extracted from BlitToScreenAdditive @00453A58 and BlitIndexedToScreenAdditive @00453F69.
     */
    protected static void putPixelAdditive(int[] surface, int pixelIndex, int source) {
       // if (pixelIndex < 0 || pixelIndex >= surface.length) return;
        surface[pixelIndex] = RGB32.additiveOver(source, surface[pixelIndex]);
    }

    /**
     * Native support extracted from ApplyShadeAdditiveToRect @004565CD.
     */
    protected static void putPixelShadeAdditive(int[] surface, int pixelIndex, int shade, int additiveColor) {
        //if (pixelIndex < 0 || pixelIndex >= surface.length) return;
        int shadedDestination = RGB32.withShade(surface[pixelIndex], shade);
        surface[pixelIndex] = RGB32.additiveOver(additiveColor, shadedDestination);
    }

    /**
     * Native support extracted from ApplyShadeAdditiveToRect @004565CD gray RGB565 offset.
     */
    protected static int nativeShadeAdditiveColor(int shade) {
        return NATIVE_SHADE_ADDITIVE_COLORS[shade];
    }

    /**
     * Native support extracted from ApplyShadeAdditiveToRect @004565CD.
     * Precomputes the native RGB565-quantized gray ramp directly as expanded opaque ARGB channels.
     */
    private static int[] initNativeShadeAdditiveColors() {
        int[] colors = new int[17];
        for (int shade = 0; shade < colors.length; shade++) {
            int nativeChannel = shade * 0x10 - 1;
            int red5 = (nativeChannel >> 3) & 0x1F;
            int green6 = (nativeChannel >> 2) & 0x3F;
            int redBlue = RGB16.expand5(red5);
            int green = RGB16.expand6(green6);
            colors[shade] = RGB32.from(redBlue, green, redBlue);
        }
        return colors;
    }

    /**
     * Native: BlendPixel16 @0045705C.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    protected void blendPixel16(int x, int y, int red, int green, int blue, int alpha) {
        if (x < clipLeft || x >= clipRight || y < clipTop || y >= clipBottom) {
            return;
        }
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int offset = (y - surfaceTop) * activeRenderTarget.pitchPixels() + (x - surfaceLeft);
        int[] surface = activeRenderTarget.surface();
        if (offset < 0 || offset >= surface.length) {
            return;
        }
        int source = RGB32.ARGB(clamp255(red), clamp255(green), clamp255(blue), clamp255(alpha));
        putPixelSourceOver(surface, offset, source);
    }

    // not ported.
    protected static void putAtXY(int[] surface, int pitchPixels, int left, int top, int x, int y, int color) {
        int sx = x - left;
        int sy = y - top;
        int pixelIndex = sy * pitchPixels + sx;
        if (pixelIndex < 0 || pixelIndex >= surface.length) return;
        putPixel(surface, pixelIndex, color);
    }

}
