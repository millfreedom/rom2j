package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.GameBitmapFrame;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.color.RGB32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

public class GLRenderer extends SoftRenderer {

    private int presentationTextureId;
    private int presentationProgramId;
    private int presentationSurfaceUniform;
    private int presentationTextureSizeUniform;
    private int presentationScaleUniform;
    private IntBuffer uploadBuffer;

    // not ported. GPU source texture for the deferred opaque scaled blit.
    private int scaledBlitTextureId;
    // not ported. Current scaled-blit texture width.
    private int scaledBlitTextureWidth;
    // not ported. Current scaled-blit texture height.
    private int scaledBlitTextureHeight;
    // not ported. Exact nearest-neighbor logical-screen blit shader.
    private int scaledBlitProgramId;
    // not ported. Scaled-blit source sampler uniform.
    private int scaledBlitSourceUniform;
    // not ported. Scaled-blit source-size uniform.
    private int scaledBlitSourceSizeUniform;
    // not ported. Scaled-blit destination-size uniform.
    private int scaledBlitDestinationSizeUniform;
    // not ported. Reused direct buffer for uploading the logical map frame.
    private IntBuffer scaledBlitUploadBuffer;

    // not ported. Logical-screen texture composed entirely by OpenGL.
    private int compositionTextureId;
    // not ported. Framebuffer that owns the logical-screen composition texture.
    private int compositionFramebufferId;

    // not ported. CPU source retained only for exact materialization of destination-dependent overlay effects.
    private int[] pendingScaledBlitSourceArgb;
    // not ported. Deferred scaled-blit source width.
    private int pendingScaledBlitSourceWidth;
    // not ported. Deferred scaled-blit source height.
    private int pendingScaledBlitSourceHeight;
    // not ported. Deferred scaled-blit destination X.
    private int pendingScaledBlitDestX;
    // not ported. Deferred scaled-blit destination Y.
    private int pendingScaledBlitDestY;
    // not ported. Deferred scaled-blit destination width.
    private int pendingScaledBlitDestWidth;
    // not ported. Deferred scaled-blit destination height.
    private int pendingScaledBlitDestHeight;
    // not ported. Deferred scaled-blit clipped left edge.
    private int pendingScaledBlitClipLeft;
    // not ported. Deferred scaled-blit clipped top edge.
    private int pendingScaledBlitClipTop;
    // not ported. Deferred scaled-blit clipped right edge.
    private int pendingScaledBlitClipRight;
    // not ported. Deferred scaled-blit clipped bottom edge.
    private int pendingScaledBlitClipBottom;
    // not ported. Distinguishes a second blit in one frame from replacement on the following rendered frame.
    private boolean pendingScaledBlitPresented;


    // not ported.
    public GLRenderer(Screen screen) {
        super(screen);
    }

    /**
     * Clears the software overlay and retires any deferred GPU-scaled frame from the preceding scene.
     * not ported.
     */
    @Override
    public synchronized void clearSurface() {
        super.clearSurface();
        clearPendingScaledBlit();
    }

    /**
     * Uploads a known-opaque logical ARGB frame to OpenGL and defers its nearest-neighbor scaling to the logical-screen
     * composition pass. The corresponding software rectangle becomes a transparent overlay so UI drawn after the map
     * remains above the GPU-scaled result.
     * not ported.
     */
    @Override
    public void blitOpaqueArgbScaled(int[] sourceArgb, int sourceWidth, int sourceHeight,
                                     int destX, int destY, int destWidth, int destHeight) {
        if (activeRenderTarget != screen) {
            super.blitOpaqueArgbScaled(sourceArgb, sourceWidth, sourceHeight,
                    destX, destY, destWidth, destHeight);
            return;
        }
        if (sourceWidth <= 0 || sourceHeight <= 0 || destWidth <= 0 || destHeight <= 0) {
            return;
        }

        int clippedLeft = Math.max(destX, clipLeft);
        int clippedTop = Math.max(destY, clipTop);
        int clippedRight = Math.min(destX + destWidth, clipRight);
        int clippedBottom = Math.min(destY + destHeight, clipBottom);
        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        if (pendingScaledBlitSourceArgb != null && !pendingScaledBlitPresented) {
            materializePendingScaledBlitRect(
                    pendingScaledBlitClipLeft,
                    pendingScaledBlitClipTop,
                    pendingScaledBlitClipRight,
                    pendingScaledBlitClipBottom
            );
        }

        uploadScaledBlitTexture(sourceArgb, sourceWidth, sourceHeight);
        pendingScaledBlitSourceArgb = sourceArgb;
        pendingScaledBlitSourceWidth = sourceWidth;
        pendingScaledBlitSourceHeight = sourceHeight;
        pendingScaledBlitDestX = destX;
        pendingScaledBlitDestY = destY;
        pendingScaledBlitDestWidth = destWidth;
        pendingScaledBlitDestHeight = destHeight;
        pendingScaledBlitClipLeft = clippedLeft;
        pendingScaledBlitClipTop = clippedTop;
        pendingScaledBlitClipRight = clippedRight;
        pendingScaledBlitClipBottom = clippedBottom;
        pendingScaledBlitPresented = false;

        int[] surface = screen.surface();
        int pitchPixels = screen.pitchPixels();
        for (int y = clippedTop; y < clippedBottom; y++) {
            int rowStart = (y - screen.y()) * pitchPixels + clippedLeft - screen.x();
            Arrays.fill(surface, rowStart, rowStart + clippedRight - clippedLeft, RGB32.TBLACK);
        }
    }

    /**
     * Preserves exact destination-dependent shade semantics over a deferred GPU-scaled frame.
     * not ported.
     */
    @Override
    public synchronized void applyShadeToRect(int left, int top, int right, int bottom, int shade) {
        materializePendingScaledBlitRect(left, top, right, bottom);
        super.applyShadeToRect(left, top, right, bottom, shade);
    }

    /**
     * Preserves exact destination-dependent additive semantics over a deferred GPU-scaled frame.
     * not ported.
     */
    @Override
    public synchronized void addColorToRect(int left, int top, int right, int bottom, int color) {
        materializePendingScaledBlitRect(left, top, right, bottom);
        super.addColorToRect(left, top, right, bottom, color);
    }

    /**
     * Preserves exact destination-dependent shade/additive semantics over a deferred GPU-scaled frame.
     * not ported.
     */
    @Override
    public synchronized void applyShadeAdditiveToRect(int left, int top, int right, int bottom, int brightness) {
        materializePendingScaledBlitRect(left, top, right, bottom);
        super.applyShadeAdditiveToRect(left, top, right, bottom, brightness);
    }

    /**
     * Preserves destination shade-page semantics for overlays drawn after the deferred map blit.
     * not ported.
     */
    @Override
    public void drawIndexedSpriteShade(int x, int y, GameBitmapFrame frame, int shadePage, boolean flipX) {
        materializePendingScaledBlitRect(x, y, x + frame.width(), y + frame.height());
        super.drawIndexedSpriteShade(x, y, frame, shadePage, flipX);
    }

    /**
     * Preserves destination shade-page semantics for sheared overlays drawn after the deferred map blit.
     * not ported.
     */
    @Override
    public void drawIndexedSpriteShearedShade(int x, int y, GameBitmapFrame frame,
                                              int shadePage, int slope, boolean flipX) {
        int shearWidth = Math.abs((int) (((long) slope * frame.height()) >> 16));
        materializePendingScaledBlitRect(x - shearWidth, y, x + frame.width() + shearWidth, y + frame.height());
        super.drawIndexedSpriteShearedShade(x, y, frame, shadePage, slope, flipX);
    }

    /**
     * Preserves native additive ARGB blit semantics over a deferred GPU-scaled frame.
     * not ported.
     */
    @Override
    public void blitToScreenAdditive(int dstX, int dstY, int srcLeft, int srcTop, int srcRight, int srcBottom,
                                     int[] srcData, int srcWidth, int srcHeight) {
        materializePendingScaledBlitRect(dstX, dstY,
                dstX + srcRight - srcLeft, dstY + srcBottom - srcTop);
        super.blitToScreenAdditive(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom,
                srcData, srcWidth, srcHeight);
    }

    /**
     * Preserves native additive indexed blit semantics over a deferred GPU-scaled frame.
     * not ported.
     */
    @Override
    public void blitIndexedToScreenAdditive(int dstX, int dstY,
                                            int srcLeft, int srcTop, int srcRight, int srcBottom,
                                            int[] srcData, int srcWidth, int srcHeight, int[] palette) {
        materializePendingScaledBlitRect(dstX, dstY,
                dstX + srcRight - srcLeft, dstY + srcBottom - srcTop);
        super.blitIndexedToScreenAdditive(dstX, dstY, srcLeft, srcTop, srcRight, srcBottom,
                srcData, srcWidth, srcHeight, palette);
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
                GL_UNSIGNED_INT_8_8_8_8_REV,
                uploadBuffer
        );
        if (pendingScaledBlitSourceArgb != null) {
            renderLogicalComposition();
            pendingScaledBlitPresented = true;
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, compositionTextureId);
        }
        renderPresentationQuad(framebufferWidth, framebufferHeight);
    }

    /**
     * Native support boundary for ReleaseDirectDrawInterface @00452B89.
     * skipped: GLRenderer releases OpenGL presentation resources instead of a DirectDraw interface.
     */
    @Override
    public void releasePresentationResources() {
        if (compositionFramebufferId != 0) {
            glDeleteFramebuffers(compositionFramebufferId);
            compositionFramebufferId = 0;
        }
        if (compositionTextureId != 0) {
            glDeleteTextures(compositionTextureId);
            compositionTextureId = 0;
        }
        if (scaledBlitTextureId != 0) {
            glDeleteTextures(scaledBlitTextureId);
            scaledBlitTextureId = 0;
            scaledBlitTextureWidth = 0;
            scaledBlitTextureHeight = 0;
        }
        if (scaledBlitProgramId != 0) {
            glDeleteProgram(scaledBlitProgramId);
            scaledBlitProgramId = 0;
            scaledBlitSourceUniform = 0;
            scaledBlitSourceSizeUniform = 0;
            scaledBlitDestinationSizeUniform = 0;
        }
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
        scaledBlitUploadBuffer = null;
        clearPendingScaledBlit();
    }

    /**
     * Drops the logical description of the deferred GPU-scaled blit without releasing reusable OpenGL objects.
     * not ported.
     */
    private void clearPendingScaledBlit() {
        pendingScaledBlitSourceArgb = null;
        pendingScaledBlitSourceWidth = 0;
        pendingScaledBlitSourceHeight = 0;
        pendingScaledBlitDestX = 0;
        pendingScaledBlitDestY = 0;
        pendingScaledBlitDestWidth = 0;
        pendingScaledBlitDestHeight = 0;
        pendingScaledBlitClipLeft = 0;
        pendingScaledBlitClipTop = 0;
        pendingScaledBlitClipRight = 0;
        pendingScaledBlitClipBottom = 0;
        pendingScaledBlitPresented = false;
    }

    /**
     * Materializes only the requested part of a deferred scaled blit when a later software operation must read and
     * transform its destination. Straight-alpha overlay pixels are first composed over the exact nearest-neighbor map
     * pixel, after which the normal software operation can retain its established integer behavior.
     * not ported.
     */
    private void materializePendingScaledBlitRect(int left, int top, int right, int bottom) {
        if (activeRenderTarget != screen || pendingScaledBlitSourceArgb == null) {
            return;
        }

        int materializeLeft = Math.max(Math.max(left, clipLeft), pendingScaledBlitClipLeft);
        int materializeTop = Math.max(Math.max(top, clipTop), pendingScaledBlitClipTop);
        int materializeRight = Math.min(Math.min(right, clipRight), pendingScaledBlitClipRight);
        int materializeBottom = Math.min(Math.min(bottom, clipBottom), pendingScaledBlitClipBottom);
        if (materializeLeft >= materializeRight || materializeTop >= materializeBottom) {
            return;
        }

        int[] surface = screen.surface();
        int pitchPixels = screen.pitchPixels();
        for (int y = materializeTop; y < materializeBottom; y++) {
            int sourceY = ((y - pendingScaledBlitDestY) * pendingScaledBlitSourceHeight)
                    / pendingScaledBlitDestHeight;
            int sourceRow = sourceY * pendingScaledBlitSourceWidth;
            int destination = (y - screen.y()) * pitchPixels + materializeLeft - screen.x();
            for (int x = materializeLeft; x < materializeRight; x++) {
                int sourceX = ((x - pendingScaledBlitDestX) * pendingScaledBlitSourceWidth)
                        / pendingScaledBlitDestWidth;
                int scaledPixel = pendingScaledBlitSourceArgb[sourceRow + sourceX];
                surface[destination] = RGB32.sourceOver(surface[destination], scaledPixel);
                destination++;
            }
        }
    }

    /**
     * Copies one logical opaque ARGB source into a reusable OpenGL texture without creating a CPU-scaled frame.
     * not ported.
     */
    private void uploadScaledBlitTexture(int[] sourceArgb, int sourceWidth, int sourceHeight) {
        int sourcePixelCount = Math.multiplyExact(sourceWidth, sourceHeight);
        if (scaledBlitUploadBuffer == null || scaledBlitUploadBuffer.capacity() != sourcePixelCount) {
            scaledBlitUploadBuffer = ByteBuffer.allocateDirect(Math.multiplyExact(sourcePixelCount, Integer.BYTES))
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer();
        }
        scaledBlitUploadBuffer.clear();
        scaledBlitUploadBuffer.put(sourceArgb, 0, sourcePixelCount);
        scaledBlitUploadBuffer.flip();

        if (scaledBlitTextureId == 0) {
            scaledBlitTextureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, scaledBlitTextureId);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        } else {
            glBindTexture(GL_TEXTURE_2D, scaledBlitTextureId);
        }

        if (scaledBlitTextureWidth != sourceWidth || scaledBlitTextureHeight != sourceHeight) {
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    sourceWidth,
                    sourceHeight,
                    0,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    scaledBlitUploadBuffer
            );
            scaledBlitTextureWidth = sourceWidth;
            scaledBlitTextureHeight = sourceHeight;
        } else {
            glTexSubImage2D(
                    GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    sourceWidth,
                    sourceHeight,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    scaledBlitUploadBuffer
            );
        }
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Lazily creates the exact nearest-neighbor shader used to scale a logical ARGB frame into screen coordinates.
     * not ported.
     */
    private void ensureScaledBlitShader() {
        if (scaledBlitProgramId != 0) {
            return;
        }

        int vertexShader = compileShader(GL_VERTEX_SHADER, "scaled blit", """
                #version 120
                varying vec2 vDestinationOffset;

                void main() {
                    gl_Position = ftransform();
                    vDestinationOffset = gl_MultiTexCoord0.st;
                }
                """);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, "scaled blit", """
                #version 120
                uniform sampler2D uSource;
                uniform vec2 uSourceSize;
                uniform vec2 uDestinationSize;
                varying vec2 vDestinationOffset;

                void main() {
                    vec2 destinationPixel = floor(vDestinationOffset);
                    vec2 sourcePixel = floor(destinationPixel * uSourceSize / uDestinationSize);
                    vec2 sourceTexCoord = (sourcePixel + vec2(0.5)) / uSourceSize;
                    gl_FragColor = texture2D(uSource, sourceTexCoord);
                }
                """);

        scaledBlitProgramId = linkProgram("scaled blit", vertexShader, fragmentShader);
        scaledBlitSourceUniform = glGetUniformLocation(scaledBlitProgramId, "uSource");
        scaledBlitSourceSizeUniform = glGetUniformLocation(scaledBlitProgramId, "uSourceSize");
        scaledBlitDestinationSizeUniform = glGetUniformLocation(scaledBlitProgramId, "uDestinationSize");
    }

    /**
     * Lazily creates the screen-sized RGBA texture and framebuffer used for GPU composition before window scaling.
     * not ported.
     */
    private void ensureCompositionTarget() {
        if (compositionFramebufferId != 0) {
            return;
        }

        compositionTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, compositionTextureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                screen.w(),
                screen.h(),
                0,
                GL_BGRA,
                GL_UNSIGNED_INT_8_8_8_8_REV,
                (ByteBuffer) null
        );

        compositionFramebufferId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, compositionFramebufferId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, compositionTextureId, 0);
        int framebufferStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        if (framebufferStatus != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Unable to create logical-screen composition framebuffer: 0x"
                    + Integer.toHexString(framebufferStatus));
        }
    }

    /**
     * Builds the logical screen in OpenGL: first the exact nearest-neighbor map quad, then the straight-alpha software
     * overlay containing UI and effects drawn after the map frame.
     * not ported.
     */
    private void renderLogicalComposition() {
        ensureCompositionTarget();
        ensureScaledBlitShader();

        glBindFramebuffer(GL_FRAMEBUFFER, compositionFramebufferId);
        glViewport(0, 0, screen.w(), screen.h());
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        // Store logical row zero at texture row zero, matching the CPU-uploaded presentation texture convention.
        glOrtho(0, screen.w(), 0, screen.h(), -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, scaledBlitTextureId);
        glUseProgram(scaledBlitProgramId);
        glUniform1i(scaledBlitSourceUniform, 0);
        glUniform2f(scaledBlitSourceSizeUniform,
                pendingScaledBlitSourceWidth, pendingScaledBlitSourceHeight);
        glUniform2f(scaledBlitDestinationSizeUniform,
                pendingScaledBlitDestWidth, pendingScaledBlitDestHeight);
        float sourceOffsetLeft = pendingScaledBlitClipLeft - pendingScaledBlitDestX;
        float sourceOffsetTop = pendingScaledBlitClipTop - pendingScaledBlitDestY;
        float sourceOffsetRight = pendingScaledBlitClipRight - pendingScaledBlitDestX;
        float sourceOffsetBottom = pendingScaledBlitClipBottom - pendingScaledBlitDestY;
        glBegin(GL_QUADS);
        glTexCoord2f(sourceOffsetLeft, sourceOffsetTop);
        glVertex2f(pendingScaledBlitClipLeft, pendingScaledBlitClipTop);
        glTexCoord2f(sourceOffsetRight, sourceOffsetTop);
        glVertex2f(pendingScaledBlitClipRight, pendingScaledBlitClipTop);
        glTexCoord2f(sourceOffsetRight, sourceOffsetBottom);
        glVertex2f(pendingScaledBlitClipRight, pendingScaledBlitClipBottom);
        glTexCoord2f(sourceOffsetLeft, sourceOffsetBottom);
        glVertex2f(pendingScaledBlitClipLeft, pendingScaledBlitClipBottom);
        glEnd();

        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D, presentationTextureId);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(0.0f, 0.0f);
        glTexCoord2f(1.0f, 0.0f);
        glVertex2f(screen.w(), 0.0f);
        glTexCoord2f(1.0f, 1.0f);
        glVertex2f(screen.w(), screen.h());
        glTexCoord2f(0.0f, 1.0f);
        glVertex2f(0.0f, screen.h());
        glEnd();
        glDisable(GL_BLEND);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
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
                GL_UNSIGNED_INT_8_8_8_8_REV,
                uploadBuffer
        );
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Lazily allocates the direct upload buffer that OpenGL reads from.
     * not ported.
     */
    private void ensureUploadBuffer() {
        if (uploadBuffer == null || uploadBuffer.capacity() != screen.surface().length) {
            uploadBuffer = ByteBuffer.allocateDirect(
                            Math.multiplyExact(screen.surface().length, Integer.BYTES)
                    )
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer();
        }
    }

    /**
     * Copies straight Java 0xAARRGGBB pixels into a native-order direct buffer. GL_BGRA paired with
     * GL_UNSIGNED_INT_8_8_8_8_REV maps those integer bit fields to OpenGL's blue, green, red, and alpha components
     * without depending on the host byte order.
     * not ported.
     */
    private void syncUploadBuffer() {
        ensureUploadBuffer();
        uploadBuffer.clear();
        uploadBuffer.put(screen.surface());
        uploadBuffer.flip();
    }

    /**
     * Lazily creates the OpenGL 2.0-compatible sharp-bilinear presentation shader.
     * not ported.
     */
    private void ensurePresentationShader() {
        if (presentationProgramId != 0) {
            return;
        }

        int vertexShader = compileShader(GL_VERTEX_SHADER, "presentation", """
                #version 120
                varying vec2 vTexCoord;
                
                void main() {
                    gl_Position = ftransform();
                    vTexCoord = gl_MultiTexCoord0.st;
                }
                """);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, "presentation", """
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

        presentationProgramId = linkProgram("presentation", vertexShader, fragmentShader);

        presentationSurfaceUniform = glGetUniformLocation(presentationProgramId, "uSurface");
        presentationTextureSizeUniform = glGetUniformLocation(presentationProgramId, "uTextureSize");
        presentationScaleUniform = glGetUniformLocation(presentationProgramId, "uScale");
    }

    /**
     * Compiles one GLSL shader used by a Java OpenGL rendering pass.
     * not ported.
     */
    private static int compileShader(int shaderType, String passName, String source) {
        int shaderId = glCreateShader(shaderType);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(shaderId);
            glDeleteShader(shaderId);
            throw new IllegalStateException("Unable to compile " + passName + " shader: " + infoLog);
        }
        return shaderId;
    }

    /**
     * Links one vertex/fragment shader pair and releases the individual shader objects.
     * not ported.
     */
    private static int linkProgram(String passName, int vertexShader, int fragmentShader) {
        int programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(programId);
            glDeleteProgram(programId);
            throw new IllegalStateException("Unable to link " + passName + " shader: " + infoLog);
        }
        return programId;
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

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, framebufferWidth, framebufferHeight);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
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
