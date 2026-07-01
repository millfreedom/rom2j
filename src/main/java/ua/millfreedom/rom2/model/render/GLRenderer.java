package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;

import static org.lwjgl.opengl.GL40.*;
import static ua.millfreedom.rom2.model.color.Consts.RENDER_EFFECT_TABLE;

public class GLRenderer implements Renderer {
    private static final int TERRAIN_TILE_SIZE = 0x20;
    private static final int TERRAIN_BRIGHTNESS_FIXED_SHIFT = 9;
    private static final int TERRAIN_BRIGHTNESS_FIXED_HALF = 0x100;
    private static final int TERRAIN_SLOPE_DIM_BRIGHTNESS = 8;
    private static final int TERRAIN_SLOPE_DIM_MASK_PAGE = 0x08;
    private static final int TERRAIN_SLOPE_CLEAR_MASK_PAGE = 0x10;

    public final Screen screen;
    private int presentationTextureId;
    private int presentationProgramId;
    private int presentationSurfaceUniform;
    private int presentationTextureSizeUniform;
    private int presentationScaleUniform;
    private ByteBuffer uploadBuffer;
    private Screen activeRenderTarget;
    private final Deque<int[]> clipStack = new ArrayDeque<>();
    private final Deque<RenderTargetState> renderTargetStack = new ArrayDeque<>();
    private int clipLeft;
    private int clipTop;
    private int clipRight;
    private int clipBottom;

    // not ported.
    public GLRenderer(Screen screen) {
        this.screen = screen;
        this.activeRenderTarget = screen;
        resetClip();
    }

    private record RenderTargetState(Screen renderTarget, int clipLeft, int clipTop, int clipRight, int clipBottom,
                                     Deque<int[]> clipStack) {
    }

    /**
     * Native support for the default `_g_screenRect` clipping rectangle.
     */
    private void resetClip() {
        clipLeft = activeRenderTarget.x();
        clipTop = activeRenderTarget.y();
        clipRight = activeRenderTarget.cx();
        clipBottom = activeRenderTarget.cy();
    }

    /**
     * Clears the software-backed render surface to transparent black.
     * not ported.
     */
    @Override
    public synchronized void clearSurface() {
        Arrays.fill(activeRenderTarget.surface(), (byte) 0);
    }

    /**
     * Native support boundary for FlipPrimaryDirectDrawSurface @00452685 and
     * WaitForDirectDrawVerticalBlankEnd @00452237.
     * skipped: GLRenderer uploads the software surface into OpenGL and relies on GLFW swap interval instead of
     * DirectDraw Flip/WaitForVerticalBlank.
     */
    @Override
    public void presentSurface(int framebufferWidth, int framebufferHeight) {
        ensurePresentationTexture();
        syncUploadBuffer();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, presentationTextureId);
        glTexSubImage2D(
                GL_TEXTURE_2D,
                0,
                0,
                0,
                screen.w(),
                screen.h(),
                GL_BGRA,
                GL_UNSIGNED_BYTE,
                uploadBuffer
        );
        renderPresentationQuad(framebufferWidth, framebufferHeight);
    }

    /**
     * Native support boundary for ReleaseDirectDrawInterface @00452B89.
     * skipped: GLRenderer releases OpenGL presentation resources instead of a DirectDraw interface.
     */
    @Override
    public void releasePresentationResources() {
        if (presentationTextureId != 0) {
            glDeleteTextures(presentationTextureId);
            presentationTextureId = 0;
        }
        if (presentationProgramId != 0) {
            glDeleteProgram(presentationProgramId);
            presentationProgramId = 0;
            presentationSurfaceUniform = 0;
            presentationTextureSizeUniform = 0;
            presentationScaleUniform = 0;
        }
        uploadBuffer = null;
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
     * skipped: Java presents the software render target through OpenGL after the frame is drawn.
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
    public void pushJavaRenderTarget(byte[] surfaceBgra, int width, int height) {
        Objects.requireNonNull(surfaceBgra, "surfaceBgra");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Render target dimensions must be positive");
        }
        int expectedLength = Math.multiplyExact(Math.multiplyExact(width, height), 4);
        if (surfaceBgra.length < expectedLength) {
            throw new IllegalArgumentException("Render target BGRA buffer is too small");
        }

        renderTargetStack.push(new RenderTargetState(
                activeRenderTarget,
                clipLeft,
                clipTop,
                clipRight,
                clipBottom,
                new ArrayDeque<>(clipStack)
        ));
        activeRenderTarget = new Screen(0, 0, width, height, width * 4, surfaceBgra);
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
    public void drawRect(int left, int top, int right, int bottom, short color) {
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
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        for (int y = clippedTop; y < clippedBottom; y++) {
            int offset = (y - surfaceTop) * pitch + (clippedLeft - surfaceLeft) * 4;
            for (int x = clippedLeft; x < clippedRight; x++) {
                RGB32 color = new RGB32(surface.getInt(offset));
                put32LE(surface, offset, color.withShade(shade));
                offset += 4;
            }
        }
    }

    /**
     * Native support: AddColorToRect @00456416.
     */
    @Override
    public synchronized void addColorToRect(int left, int top, int right, int bottom, short color565) {
        final int clippedLeft = Math.max(left, clipLeft);
        final int clippedTop = Math.max(top, clipTop);
        final int clippedRight = Math.min(right, clipRight);
        final int clippedBottom = Math.min(bottom, clipBottom);
        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        final RGB16 additiveColor = RGB16.of(color565);
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        for (int y = clippedTop; y < clippedBottom; y++) {
            int offset = (y - surfaceTop) * pitch + (clippedLeft - surfaceLeft) * 4;
            for (int x = clippedLeft; x < clippedRight; x++) {
                put32Additive(surface, offset, additiveColor);
                offset += 4;
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

        final RGB16 additiveColor = nativeShadeAdditiveColor(brightness);
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        for (int y = clippedTop; y < clippedBottom; y++) {
            int offset = (y - surfaceTop) * pitch + (clippedLeft - surfaceLeft) * 4;
            for (int x = clippedLeft; x < clippedRight; x++) {
                put32ShadeAdditive(surface, offset, brightness, additiveColor);
                offset += 4;
            }
        }
    }

    /**
     * Native: DrawLine @0045673C.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawLine(int x, int y, int cx, int cy, short color565) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int left = clipLeft;
        final int top = clipTop;
        final int right = clipRight; // exclusive
        final int bottom = clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surf = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

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
            putAtXY(surf, pitch, surfaceLeft, surfaceTop, curX, curY, color565);
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
                    putAtXY(surf, pitch, surfaceLeft, surfaceTop, curX, curY, color565);
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
                    putAtXY(surf, pitch, surfaceLeft, surfaceTop, curX, curY, color565);
                }
            }
        }
    }

    /**
     * Native: FillScreenRect @00456348.
     * Fully ported.
     */
    @Override
    public void fillScreenRect(int left, int top, int right, int bottom, short color565) {
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
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        RGB16 color = RGB16.of(color565);

        for (int y = clippedTop; y < clippedBottom; y++) {
            int offset = (y - surfaceTop) * pitch + (clippedLeft - surfaceLeft) * 4;
            for (int x = clippedLeft; x < clippedRight; x++) {
                put32LE(surface, offset, color);
                offset += 4;
            }
        }
    }

    @Override
    // not ported.
    public void blitPixels(int destX, int destY, int width, int height, int srcPitchPixels, int srcHeight, byte[] pSrcPointer) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

        Objects.requireNonNull(pSrcPointer, "pSrcPointer");
        if (width <= 0 || height <= 0) return;

        int srcOffsetX = 0;
        int srcOffsetY = 0;

        if (destX < clipLeft) {
            int delta = clipLeft - destX;
            width -= delta;
            srcOffsetX += delta;
            destX = clipLeft;
        }

        if (destY < clipTop) {
            int delta = clipTop - destY;
            height -= delta;
            srcOffsetY += delta;
            destY = clipTop;
        }

        if (destX + width >= clipRight) {
            width -= (destX + width) - clipRight;
        }

        if (destY + height >= clipBottom) {
            height -= (destY + height) - clipBottom;
        }

        if (width <= 0 || height <= 0) return;

        int destOffset = (destY - surfaceTop) * pitch + (destX - surfaceLeft) * 4;
        int srcPitch = srcPitchPixels * 2;
        int srcOffset = (srcHeight - srcOffsetY - 1) * srcPitch + srcOffsetX * 2;

        for (int row = 0; row < height; row++) {
            int rowSrcOffset = srcOffset - row * srcPitch;
            int rowDestOffset = destOffset + row * pitch;
            for (int col = 0; col < width; col++) {
                int srcIndex = rowSrcOffset + col * 2;
                int rgb565 = (pSrcPointer[srcIndex] & 0xFF) | ((pSrcPointer[srcIndex + 1] & 0xFF) << 8);
                put32LE(surface, rowDestOffset + col * 4, RGB16.of(rgb565));
            }
        }
    }

    /**
     * Blits BGRA pixels into the software surface using nearest-neighbor scaling and the current clip rectangle.
     * not ported.
     */
    @Override
    public void blitBgraScaled(byte[] sourceBgra, int sourceWidth, int sourceHeight,
                               int destX, int destY, int destWidth, int destHeight) {
//        Objects.requireNonNull(sourceBgra, "sourceBgra");
        if (sourceWidth <= 0 || sourceHeight <= 0 || destWidth <= 0 || destHeight <= 0) {
            return;
        }
//        int expectedLength = Math.multiplyExact(Math.multiplyExact(sourceWidth, sourceHeight), 4);
//        if (sourceBgra.length < expectedLength) {
//            throw new IllegalArgumentException("BGRA source buffer is too small");
//        }

        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clippedLeft = Math.max(destX, clipLeft);
        final int clippedTop = Math.max(destY, clipTop);
        final int clippedRight = Math.min(destX + destWidth, clipRight);
        final int clippedBottom = Math.min(destY + destHeight, clipBottom);
        final int pitch = activeRenderTarget.pitchBytes();
        final byte[] surfaceBytes = activeRenderTarget.surface();
        final IntBuffer sourcePixels = ByteBuffer.wrap(sourceBgra)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        final IntBuffer surfacePixels = ByteBuffer.wrap(surfaceBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        final int pitchPixels = pitch / 4;

        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        for (int y = clippedTop; y < clippedBottom; y++) {
            int srcY = ((y - destY) * sourceHeight) / destHeight;
            int srcRow = srcY * sourceWidth;
            int destRow = (y - surfaceTop) * pitchPixels + (clippedLeft - surfaceLeft);
            for (int x = clippedLeft; x < clippedRight; x++) {
                int srcX = ((x - destX) * sourceWidth) / destWidth;
                int src = srcRow + srcX;
                int dest = destRow + (x - clippedLeft);
                surfacePixels.put(dest, sourcePixels.get(src));
            }
        }
    }

    /**
     * Native: DrawSprite_RLE4_to_16 @004540D1.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE4(int nX, int nY, int nWidth, int nHeight, byte[] rleData, RGB16[] palette16) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

        if (nX + nWidth <= clipLeft) return;
        if (nY + nHeight <= clipTop) return;
        if (nX >= clipRight) return;
        if (nY >= clipBottom) return;

        int src = 0;
        int curX = nX;
        int curY = nY;
        int rowsLeft = nHeight;

        while (rowsLeft > 0) {
            int cmd = rleData[src++] & 0xFF;
            int op = cmd & 0xC0;
            int cnt = cmd & 0x3F;

            if (op != 0) {
                if (op == 0x40) {
                    curY += cnt;
                    rowsLeft -= cnt;
                    if (rowsLeft <= 0) break;
                } else {
                    curX += cnt;
                }
            } else {
                for (int i = 0; i < cnt; i++) {
                    int b = rleData[src++] & 0xFF;

                    int lo = b & 0x0F;
                    put32Clipped(surface, pitch, surfaceLeft, surfaceTop, clipLeft, clipTop, clipRight, clipBottom, curX, curY, palette16[lo]);
                    curX++;

                    int hi = (b >>> 4) & 0x0F;
                    if (hi == 0) {
                        break;
                    }

                    put32Clipped(surface, pitch, surfaceLeft, surfaceTop, clipLeft, clipTop, clipRight, clipBottom, curX, curY, palette16[hi]);
                    curX++;
                }
            }

            if ((curX - nX) >= nWidth) {
                curX = nX;
                curY += 1;
                rowsLeft -= 1;
            }
        }

    }

    /**
     * Native support extracted from DrawSprite_A16 @0045889B and DrawSprite_A16_FlipX @00458C10.
     */
    private static A16RunWriter getA16CompositingRunWriter(int surfaceLeft, int surfaceTop, int pitch, ByteBuffer surface, Palette16[] palettePages) {
        return (x, y, encodedPixels, offset, count, stepX) -> {
            int di = (y - surfaceTop) * pitch + (x - surfaceLeft) * 4;
            ByteBuffer bb = ByteBuffer.wrap(encodedPixels)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .position(offset);
            for (int i = 0; i < count; i++) {
                RGB32 destinationColor = new RGB32(surface.getInt(di));
                RGB32 composedColor = A16SpriteDecoder.composeColor(bb.getShort(), destinationColor, palettePages);
                put32LE(surface, di, composedColor);
                di += stepX * 4;
            }
        };
    }

    /**
     * Native support extracted from CA16Font::DrawTextInternal @0045E8FD explicit color-table dispatch.
     */
    private static A16RunWriter getA16BasePaletteCompositingRunWriter(int surfaceLeft, int surfaceTop, int pitch, ByteBuffer surface, RGB16[] palette16) {
        return (x, y, encodedPixels, offset, count, stepX) -> {
            int di = (y - surfaceTop) * pitch + (x - surfaceLeft) * 4;
            ByteBuffer bb = ByteBuffer.wrap(encodedPixels)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .position(offset);
            for (int i = 0; i < count; i++) {
                RGB32 destinationColor = new RGB32(surface.getInt(di));
                RGB32 composedColor = A16SpriteDecoder.composeColor(bb.getShort(), destinationColor, palette16);
                put32LE(surface, di, composedColor);
                di += stepX * 4;
            }
        };
    }

    /**
     * Native: DrawSprite_A16 @0045889B.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteA16(int nX, int nY, int nWidth, int nHeight, byte[] pA16Data, Palette16[] palettePages) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

        A16SpriteDecoder.decodeClipped(
                nX,
                nY,
                nWidth,
                nHeight,
                pA16Data,
                clipLeft,
                clipTop,
                clipRight,
                clipBottom,
                getA16CompositingRunWriter(surfaceLeft, surfaceTop, pitch, surface, palettePages)
        );
    }


    /**
     * Native: DrawSprite_A16_FlipX @00458C10.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteA16FlipX(int nX, int nY, int nWidth, int nHeight, byte[] pA16Data, Palette16[] palettePages) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

        A16SpriteDecoder.decodeClippedFlipX(
                nX,
                nY,
                nWidth,
                nHeight,
                pA16Data,
                clipLeft,
                clipTop,
                clipRight,
                clipBottom,
                getA16CompositingRunWriter(surfaceLeft, surfaceTop, pitch, surface, palettePages)
        );
    }

    /**
     * Native support extracted from CA16Font::DrawTextInternal @0045E8FD explicit color-table dispatch.
     */
    @Override
    public void drawSpriteA16WithBasePalette(int nX, int nY, int nWidth, int nHeight, byte[] pA16Data, RGB16[] palette16, boolean bFlipX) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

        A16RunWriter writer = getA16BasePaletteCompositingRunWriter(surfaceLeft, surfaceTop, pitch, surface, palette16);
        if (!bFlipX) {
            A16SpriteDecoder.decodeClipped(
                    nX,
                    nY,
                    nWidth,
                    nHeight,
                    pA16Data,
                    clipLeft,
                    clipTop,
                    clipRight,
                    clipBottom,
                    writer
            );
        } else {
            A16SpriteDecoder.decodeClippedFlipX(
                    nX,
                    nY,
                    nWidth,
                    nHeight,
                    pA16Data,
                    clipLeft,
                    clipTop,
                    clipRight,
                    clipBottom,
                    writer
            );
        }
    }


    /**
     * Native: DrawSprite_RLE8_to_16 @00454344.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE8(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16[] pPalette) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        Rle8SpriteDecoder.decodeClipped(nX, nY, nWidth, nHeight, pRLEData, clipLeft, clipTop, clipRight, clipBottom,
                getRle8To32RunWriter(pPalette, pitch, surfaceTop, surfaceLeft, surface, false)
        );
    }

    /**
     * Native: DrawSprite_RLE8_to_16_FlipX @0045537D.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE8FlipX(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16[] pPalette) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        Rle8SpriteDecoder.decodeClippedFlipX(nX, nY, nWidth, nHeight, pRLEData, clipLeft, clipTop, clipRight, clipBottom,
                getRle8To32RunWriter(pPalette, pitch, surfaceTop, surfaceLeft, surface, false)
        );
    }

    /**
     * Native: DrawSprite_RLE8_SolidIndexed8 @004545C5.
     * Java 32bpp render-target boundary for native solid RLE8 sprite coverage.
     */
    @Override
    public void drawSpriteRLE8Solid(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16 fillColor) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight;
        final int clipBottom = this.clipBottom;
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

        Objects.requireNonNull(fillColor, "fillColor");
        Rle8SpriteDecoder.decodeClipped(nX, nY, nWidth, nHeight, pRLEData, clipLeft, clipTop, clipRight, clipBottom,
                (x, y, paletteIndices, offset, count, stepX) -> {
                    int di = (y - surfaceTop) * pitch + (x - surfaceLeft) * 4;
                    for (int i = 0; i < count; i++) {
                        put32LE(surface, di, fillColor);
                        di += stepX * 4;
                    }
                }
        );
    }

    /**
     * Native: drawSpriteRLE8To16Blend @00454656.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE8Blend(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16[] pPalette) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        Rle8SpriteDecoder.decodeClipped(nX, nY, nWidth, nHeight, pRLEData, clipLeft, clipTop, clipRight, clipBottom,
                getRle8To32RunWriter(pPalette, pitch, surfaceTop, surfaceLeft, surface, true)
        );
    }

    /**
     * Native: drawSpriteRLE8To16BlendFlipX @00455617.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE8BlendFlipX(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, RGB16[] pPalette) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        Rle8SpriteDecoder.decodeClippedFlipX(nX, nY, nWidth, nHeight, pRLEData, clipLeft, clipTop, clipRight, clipBottom,
                getRle8To32RunWriter(pPalette, pitch, surfaceTop, surfaceLeft, surface, true)
        );
    }

    /**
     * Native: DrawSprite_RLE8_AlphaBlend @00454ABC.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE8AlphaBlend(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, int shadePage) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        Rle8SpriteDecoder.decodeClipped(nX, nY, nWidth, nHeight, pRLEData, clipLeft, clipTop, clipRight, clipBottom,
                getRle8ShadeRunWriter(shadePage, pitch, surfaceTop, surfaceLeft, surface)
        );
    }

    /**
     * Native: DrawSprite_RLE8_AlphaBlend_FlipX @00455AB1.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE8AlphaBlendFlipX(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, int shadePage) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        Rle8SpriteDecoder.decodeClippedFlipX(nX, nY, nWidth, nHeight, pRLEData, clipLeft, clipTop, clipRight, clipBottom,
                getRle8ShadeRunWriter(shadePage, pitch, surfaceTop, surfaceLeft, surface)
        );
    }

    /**
     * Native: DrawSpriteRLE8To16Lut @0045506F.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE8To16Lut(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, int lutIndex, int slope) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        Rle8SpriteDecoder.decodeClippedSheared(nX, nY, nWidth, nHeight, pRLEData, slope, clipLeft, clipTop, clipRight,
                clipBottom, getRle8LutRunWriter(lutIndex, pitch, surfaceTop, surfaceLeft, surface));
    }

    /**
     * Native: DrawSpriteRLE8To16LutFlipX @00456021.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void drawSpriteRLE8To16LutFlipX(int nX, int nY, int nWidth, int nHeight, byte[] pRLEData, int lutIndex, int slope) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        Rle8SpriteDecoder.decodeClippedShearedFlipX(nX, nY, nWidth, nHeight, pRLEData, slope, clipLeft, clipTop,
                clipRight, clipBottom, getRle8LutRunWriter(lutIndex, pitch, surfaceTop, surfaceLeft, surface));
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
     * Java support for RLE8 palette-index runs on the 32bpp software surface.
     * not ported.
     */
    private static Rle8RunWriter getRle8To32RunWriter(RGB16[] palette16, int pitch, int clipTop, int clipLeft, ByteBuffer surface, boolean blend) {
        Objects.requireNonNull(palette16, "palette16");
        return (x, y, paletteIndices, offset, count, stepX) -> {
            int di = (y - clipTop) * pitch + (x - clipLeft) * 4;
            for (int i = 0; i < count; i++) {
                RGB16 color = palette16[paletteIndices[offset + i] & 0xFF];
                if (blend) {
                    put32Blend(surface, di, color.toRGB32());
                } else {
                    put32LE(surface, di, color);
                }
                di += stepX * 4;
            }
        };
    }

    /**
     * Native support extracted from DrawSprite_RLE8_AlphaBlend @00454ABC and DrawSprite_RLE8_AlphaBlend_FlipX @00455AB1.
     */
    private static Rle8RunWriter getRle8ShadeRunWriter(int shadePage, int pitch, int clipTop, int clipLeft, ByteBuffer surface) {
        return (x, y, paletteIndices, offset, count, stepX) -> {
            int di = (y - clipTop) * pitch + (x - clipLeft) * 4;
            for (int i = 0; i < count; i++) {
                RGB32 destinationColor = new RGB32(surface.getInt(di));
                put32LE(surface, di, destinationColor.withShade(shadePage));
                di += stepX * 4;
            }
        };
    }

    /**
     * Native support extracted from DrawSpriteRLE8To16Lut @0045506F and DrawSpriteRLE8To16LutFlipX @00456021.
     */
    private static Rle8RunWriter getRle8LutRunWriter(int lutIndex, int pitch, int clipTop, int clipLeft, ByteBuffer surface) {
        return (x, y, paletteIndices, offset, count, stepX) -> {
            int di = (y - clipTop) * pitch + (x - clipLeft) * 4;
            for (int i = 0; i < count; i++) {
                RGB32 destinationColor = new RGB32(surface.getInt(di));
                put32LE(surface, di, destinationColor.withShade(lutIndex));
                di += stepX * 4;
            }
        };
    }

    /**
     * Native: BlitToScreen @004538DD.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void blitToScreen(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, RGB16[] srcData, int srcWidth, int srcHeight) {
        blitToScreenCore(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom, srcData, srcWidth, srcHeight, false);
    }

    /**
     * Native: BlitToScreenMasked @00453BCA.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void blitToScreenMasked(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom, RGB16[] srcData, int srcWidth, int srcHeight) {
        blitToScreenCore(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom, srcData, srcWidth, srcHeight, true);
    }

    /**
     * Native: BlitToScreenAdditive @00453A58.
     */
    @Override
    public void blitToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                     RGB16[] srcData, int srcWidth, int srcHeight) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

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
        int dstOffset = (dstY - surfaceTop) * pitch + (dstX - surfaceLeft) * 4;

        for (int rowsLeft = height; rowsLeft > 0; rowsLeft--) {
            int srcIndex = srcRowIndex;
            int di = dstOffset;
            for (int x = 0; x < width; x++) {
                put32Additive(surface, di, srcData[srcIndex + x]);
                di += 4;
            }
            srcRowIndex -= srcWidth;
            dstOffset += pitch;
        }
    }

    /**
     * Native: BlitIndexedToScreen @00453D4B.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void blitIndexedToScreen(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                    byte[] srcData, int srcWidth, int srcHeight, RGB16[] palette16) {
        blitIndexedToScreenCore(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom, srcData, srcWidth, srcHeight, palette16, false);
    }

    /**
     * Native: BlitIndexedToScreenAdditive @00453F69.
     */
    @Override
    public void blitIndexedToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                            byte[] srcData, int srcWidth, int srcHeight, RGB16[] palette16) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

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
        int dstOffset = (dstY - surfaceTop) * pitch + (dstX - surfaceLeft) * 4;

        for (int rowsLeft = height; rowsLeft > 0; rowsLeft--) {
            int srcIndex = srcRowIndex;
            int di = dstOffset;
            for (int x = 0; x < width; x++) {
                int paletteIndex = Byte.toUnsignedInt(srcData[srcIndex + x]);
                put32Additive(surface, di, palette16[paletteIndex]);
                di += 4;
            }
            srcRowIndex -= srcWidth;
            dstOffset += pitch;
        }
    }

    /**
     * Java helper for paletted screen blits with palette-index zero transparency.
     * not ported.
     */
    @Override
    public void blitIndexedToScreenMasked(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                          byte[] srcData, int srcWidth, int srcHeight, RGB16[] palette16) {
        blitIndexedToScreenCore(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom, srcData, srcWidth, srcHeight, palette16, true);
    }

    /**
     * Java helper for 16-bit screen blits used by the native global blit ports.
     * Native support extracted from BlitToScreen @004538DD and BlitToScreenMasked @00453BCA.
     */
    private void blitToScreenCore(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                  RGB16[] srcData, int srcWidth, int srcHeight, boolean zeroTransparent) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

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
        int dstOffset = (dstY - surfaceTop) * pitch + (dstX - surfaceLeft) * 4;

        int rowsLeft = height;
        int srcRowIndex = srcStartRow;

        while (rowsLeft > 0) {
            int srcIndex = srcRowIndex;
            int di = dstOffset;

            for (int x = 0; x < width; x++) {
                RGB16 pixel = srcData[srcIndex + x];
                if (pixel != null && (!zeroTransparent || pixel.val() != 0)) {
                    put32LE(surface, di, pixel);
                }
                di += 4;
            }

            srcRowIndex -= srcWidth;
            dstOffset += pitch;
            rowsLeft--;
        }
    }

    /**
     * Java helper for indexed screen blits used by the CBmp256 native helper ports.
     * Native support extracted from BlitIndexedToScreen @00453D4B.
     */
    private void blitIndexedToScreenCore(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                         byte[] srcData, int srcWidth, int srcHeight, RGB16[] palette16, boolean zeroTransparent) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int clipLeft = this.clipLeft;
        final int clipTop = this.clipTop;
        final int clipRight = this.clipRight; // exclusive
        final int clipBottom = this.clipBottom; // exclusive
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

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
        int dstOffset = (dstY - surfaceTop) * pitch + (dstX - surfaceLeft) * 4;

        for (int rowsLeft = height; rowsLeft > 0; rowsLeft--) {
            int srcIndex = srcRowIndex;
            int di = dstOffset;
            for (int x = 0; x < width; x++) {
                int paletteIndex = Byte.toUnsignedInt(srcData[srcIndex + x]);
                if (!(zeroTransparent && paletteIndex == 0)) {
                    RGB16 color = palette16[paletteIndex];
                    if (color != null) {
                        put32LE(surface, di, color);
                    }
                }
                di += 4;
            }
            srcRowIndex += srcWidth;
            dstOffset += pitch;
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
                                    byte[] sourcePixels, int sourceOffset, Palette16[] palettePages) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

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
                        int destinationOffset = (screenY - surfaceTop) * pitch + (screenX - surfaceLeft) * 4;
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
                                      byte[] sourcePixels, int sourceOffset, Palette16[] palettePages) {
        final int surfaceLeft = activeRenderTarget.x();
        final int surfaceTop = activeRenderTarget.y();
        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);

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
                        int destinationOffset = (y - surfaceTop) * pitch + (screenX - surfaceLeft) * 4;
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
    private static int skewedTopTerrainEdgeY(int leftY, int rightY, int column) {
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
    private static int skewedBottomTerrainEdgeY(int leftY, int rightY, int column) {
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
     * Native: ClearFlatTerrainSlopeMask @00457944.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    @Override
    public void clearFlatTerrainSlopeMask(int dstX, int topY, int bottomY) {
        fillScreenRect(dstX, topY, dstX + TERRAIN_TILE_SIZE, bottomY, RGB16.BLACK.val());
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
    private void applyTerrainSlopeMaskShape(int leftX, int rightX,
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
                fillScreenRect(screenX, topY, screenX + 1, bottomY, RGB16.BLACK.val());
            } else {
                applyShadeToRect(screenX, topY, screenX + 1, bottomY, TERRAIN_SLOPE_DIM_BRIGHTNESS);
            }
        }
    }

    private enum TerrainSlopeMaskMode {
        CLEAR,
        DIM
    }

    /**
     * Native support extracted from ApplyFlatTerrainSlopeMaskBrightness @00457DA7 and
     * ApplySkewedTerrainSlopeMaskBrightness @00457F5B.
     */
    private void applyTerrainSlopeBrightnessColumn(int screenX, int topY, int bottomY,
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

        final int pitch = activeRenderTarget.pitchBytes();
        final ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        brightnessFixed += (clippedTop - topY) * brightnessStep;
        int offset = (clippedTop - surfaceTop) * pitch + (screenX - surfaceLeft) * 4;
        for (int y = clippedTop; y < clippedBottom; y++) {
            int shade = brightnessFixed >> TERRAIN_BRIGHTNESS_FIXED_SHIFT;
            RGB32 color = new RGB32(surface.getInt(offset));
            put32LE(surface, offset, color.withShade(shade));
            brightnessFixed += brightnessStep;
            offset += pitch;
        }
    }


    /**
     * Native support extracted from DrawFlatTerrainTileToScreen @00457487 signed fixed-point division.
     */
    private static int truncateDivideBy32(int value) {
        return (value + ((value >> 31) & 0x1F)) >> 5;
    }

    /**
     * Native support extracted from DrawSkewedTerrainTileToScreen @004575FE signed integer division.
     */
    private static int truncateDivide(int value, int divisor) {
        return value / divisor;
    }

    /**
     * Native support extracted from DrawFlatTerrainTileToScreen @00457487 and DrawSkewedTerrainTileToScreen @004575FE.
     */
    private static void putTerrainPixel(ByteBuffer surface, int destinationOffset, byte[] sourcePixels,
                                        int sourceIndex, Palette16[] palettePages, int brightnessPage) {
        RGB16[] palette = palettePages[brightnessPage].data();
        put32LE(surface, destinationOffset, palette[Byte.toUnsignedInt(sourcePixels[sourceIndex])]);
    }

    // not ported.
    private static void put32LE(ByteBuffer surface, int off, RGB32 rgb32) {
        if (off < 0 || off + 3 >= surface.limit()) return;
        surface.putInt(off, rgb32.val());
    }

    // not ported.
    private static void put32LE(ByteBuffer surface, int off, RGB16 rgb16) {
        if (off < 0 || off + 3 >= surface.limit()) return;
        put32LE(surface, off, rgb16.toRGB32());
    }

    /**
     * Native support extracted from BlitToScreenAdditive @00453A58 and BlitIndexedToScreenAdditive @00453F69.
     */
    private static void put32Additive(ByteBuffer surface, int off, RGB16 src) {
        if (off < 0 || off + 3 >= surface.limit()) return;
        RGB16 dst = new RGB32(surface.getInt(off)).toRGB16();
        int sum = (Short.toUnsignedInt(dst.val()) + Short.toUnsignedInt(src.val())) & 0xFFFF;
        put32LE(surface, off, RGB16.of(sum));
    }

    /**
     * Native support extracted from ApplyShadeAdditiveToRect @004565CD.
     */
    private static void put32ShadeAdditive(ByteBuffer surface, int off, int shade, RGB16 additiveColor) {
        if (off < 0 || off + 3 >= surface.limit()) return;
        RGB16 dst = new RGB32(surface.getInt(off)).toRGB16().withShade(shade);
        int sum = (Short.toUnsignedInt(dst.val()) + Short.toUnsignedInt(additiveColor.val())) & 0xFFFF;
        put32LE(surface, off, RGB16.of(sum));
    }

    /**
     * Native support extracted from ApplyShadeAdditiveToRect @004565CD gray RGB565 offset.
     */
    private static RGB16 nativeShadeAdditiveColor(int shade) {
        int channel = shade * 0x10 - 1;
        int packed565 = ((channel >> 3) << 11) | ((channel >> 2) << 5) | (channel >> 3);
        return RGB16.of(packed565);
    }

    /**
     * Native support extracted from drawSpriteRLE8To16Blend @00454656 and drawSpriteRLE8To16BlendFlipX @00455617.
     */
    private static void put32Blend(ByteBuffer surface, int off, RGB32 src) {
        if (off < 0 || off + 3 >= surface.limit()) return;
        RGB32 dst = new RGB32(surface.getInt(off));
        put32LE(surface, off, RGB32.from(
                (dst.r() + src.r()) >>> 1,
                (dst.g() + src.g()) >>> 1,
                (dst.b() + src.b()) >>> 1
        ));
    }

    /**
     * Native: BlendPixel16 @0045705C.
     * Fully ported at the Java 32bpp render-target boundary.
     */
    private void blendPixel16(int x, int y, int red, int green, int blue, int alpha) {
        if (x < clipLeft || x >= clipRight || y < clipTop || y >= clipBottom) {
            return;
        }
        int clampedAlpha = Math.max(0, alpha);
        int inverseAlpha = 0xFF - clampedAlpha;
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int offset = (y - surfaceTop) * activeRenderTarget.pitchBytes() + (x - surfaceLeft) * 4;
        ByteBuffer surface = ByteBuffer.wrap(activeRenderTarget.surface()).order(ByteOrder.LITTLE_ENDIAN);
        if (offset < 0 || offset + 3 >= surface.limit()) {
            return;
        }
        RGB32 destination = new RGB32(surface.getInt(offset));
        put32LE(surface, offset, RGB32.from(
                (destination.r() * inverseAlpha + red * clampedAlpha) / 0xFF,
                (destination.g() * inverseAlpha + green * clampedAlpha) / 0xFF,
                (destination.b() * inverseAlpha + blue * clampedAlpha) / 0xFF
        ));
    }

    // not ported.
    private static void put32Clipped(
            ByteBuffer surface,
            int pitchBytes,
            int surfaceLeft,
            int surfaceTop,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            int x,
            int y,
            RGB16 rgb16
    ) {
        if (x < clipLeft || x >= clipRight || y < clipTop || y >= clipBottom) return;

        int sx = x - surfaceLeft;
        int sy = y - surfaceTop;
        int off = sy * pitchBytes + sx * 4; // 32bpp

        put32LE(surface, off, rgb16);
    }

    // not ported.
    private static void putAtXY(ByteBuffer surface, int pitchBytes, int left, int top, int x, int y, short rgb565) {
        int sx = x - left;
        int sy = y - top;
        int off = sy * pitchBytes + sx * 4; // 32bpp
        if (off < 0 || off + 3 >= surface.limit()) return;
        put32LE(surface, off, RGB16.of(rgb565));
    }

    /**
     * Lazily allocates the direct upload buffer that OpenGL reads from.
     * not ported.
     */
    private void ensureUploadBuffer() {
        if (uploadBuffer == null || uploadBuffer.capacity() != screen.surface().length) {
            uploadBuffer = ByteBuffer.allocateDirect(screen.surface().length);
        }
    }

    /**
     * Copies the heap-backed software framebuffer into the direct OpenGL upload buffer.
     * not ported.
     */
    private void syncUploadBuffer() {
        ensureUploadBuffer();
        uploadBuffer.clear();
        uploadBuffer.put(screen.surface());
        uploadBuffer.flip();
    }

    /**
     * Lazily allocates and sizes the OpenGL texture used to present the software surface.
     * not ported.
     */
    private void ensurePresentationTexture() {
        if (presentationTextureId != 0) {
            return;
        }

        presentationTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, presentationTextureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        syncUploadBuffer();
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                screen.w(),
                screen.h(),
                0,
                GL_BGRA,
                GL_UNSIGNED_BYTE,
                uploadBuffer
        );
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Lazily creates the OpenGL 2.0-compatible sharp-bilinear presentation shader.
     * not ported.
     */
    private void ensurePresentationShader() {
        if (presentationProgramId != 0) {
            return;
        }

        int vertexShader = compilePresentationShader(GL_VERTEX_SHADER, """
                #version 120
                varying vec2 vTexCoord;
                
                void main() {
                    gl_Position = ftransform();
                    vTexCoord = gl_MultiTexCoord0.st;
                }
                """);
        int fragmentShader = compilePresentationShader(GL_FRAGMENT_SHADER, """
                #version 120
                uniform sampler2D uSurface;
                uniform vec2 uTextureSize;
                uniform vec2 uScale;
                varying vec2 vTexCoord;
                
                void main() {
                    vec2 scale = max(uScale, vec2(1.0));
                    vec2 texel = vTexCoord * uTextureSize - vec2(0.5);
                    vec2 base = floor(texel);
                    vec2 fraction = texel - base;
                    vec2 sharpFraction = clamp(fraction * scale + vec2(0.5) - scale * 0.5, 0.0, 1.0);
                    vec2 sharpTexCoord = (base + sharpFraction + vec2(0.5)) / uTextureSize;
                    gl_FragColor = texture2D(uSurface, sharpTexCoord);
                }
                """);

        presentationProgramId = glCreateProgram();
        glAttachShader(presentationProgramId, vertexShader);
        glAttachShader(presentationProgramId, fragmentShader);
        glLinkProgram(presentationProgramId);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        if (glGetProgrami(presentationProgramId, GL_LINK_STATUS) == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(presentationProgramId);
            glDeleteProgram(presentationProgramId);
            presentationProgramId = 0;
            throw new IllegalStateException("Unable to link presentation shader: " + infoLog);
        }

        presentationSurfaceUniform = glGetUniformLocation(presentationProgramId, "uSurface");
        presentationTextureSizeUniform = glGetUniformLocation(presentationProgramId, "uTextureSize");
        presentationScaleUniform = glGetUniformLocation(presentationProgramId, "uScale");
    }

    /**
     * Compiles one GLSL shader used by the Java presentation pass.
     * not ported.
     */
    private static int compilePresentationShader(int shaderType, String source) {
        int shaderId = glCreateShader(shaderType);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(shaderId);
            glDeleteShader(shaderId);
            throw new IllegalStateException("Unable to compile presentation shader: " + infoLog);
        }
        return shaderId;
    }

    /**
     * Draws the active source crop of the presentation texture through the active Java presentation transform.
     * not ported.
     */
    private void renderPresentationQuad(int framebufferWidth, int framebufferHeight) {
        PresentationTransform transform = PresentationSupport.currentTransform(framebufferWidth, framebufferHeight);
        ensurePresentationShader();
        float drawX = (float) transform.drawX();
        float drawY = (float) transform.drawY();
        float drawWidth = (float) transform.drawWidth();
        float drawHeight = (float) transform.drawHeight();
        double sourceLeft = transform.sourceLeft() - screen.x();
        double sourceTop = transform.sourceTop() - screen.y();
        double sourceRight = transform.sourceRight() - screen.x();
        double sourceBottom = transform.sourceBottom() - screen.y();
        float textureLeft = (float) (sourceLeft / screen.w());
        float textureTop = (float) (sourceTop / screen.h());
        float textureRight = (float) (sourceRight / screen.w());
        float textureBottom = (float) (sourceBottom / screen.h());

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glUseProgram(presentationProgramId);
        glUniform1i(presentationSurfaceUniform, 0);
        glUniform2f(presentationTextureSizeUniform, screen.w(), screen.h());
        glUniform2f(presentationScaleUniform, (float) transform.scaleX(), (float) transform.scaleY());

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, framebufferWidth, framebufferHeight, 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glBegin(GL_QUADS);
        glTexCoord2f(textureLeft, textureTop);
        glVertex2f(drawX, drawY);
        glTexCoord2f(textureRight, textureTop);
        glVertex2f(drawX + drawWidth, drawY);
        glTexCoord2f(textureRight, textureBottom);
        glVertex2f(drawX + drawWidth, drawY + drawHeight);
        glTexCoord2f(textureLeft, textureBottom);
        glVertex2f(drawX, drawY + drawHeight);
        glEnd();

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }


}
