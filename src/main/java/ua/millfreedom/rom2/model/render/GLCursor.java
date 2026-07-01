package ua.millfreedom.rom2.model.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.GameBitmapFrame;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.stb.STBImageResize.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class GLCursor extends CMousePointer {
    private final long window;
    private double scale = Double.NaN;
    private long[] cursors;
    private int sourceHotSpotX;
    private int sourceHotSpotY;
    //private ByteBuffer[] pixelData; // keep until shutdown

    /**
     * not ported.
     */
    public GLCursor(long window) {
        this.window = window;
        showCount = 1;
    }

    /**
     * not ported.
     */
    @Override
    public void postInit() {
        sourceHotSpotX = hotSpotX;
        sourceHotSpotY = hotSpotY;
        rebuildCursors(currentPresentationScale());
        if (showCount > 0 && cursorVisible != 0) {
            applyCurrentCursorFrame();
        }
    }

    /**
     * Rebuilds native OS cursor handles for the active presentation scale.
     * not ported.
     */
    private void rebuildCursors(double nextScale) {
        destroyCursorHandles();
        scale = nextScale;
        hotSpotX = (int) Math.max(1, Math.round(sourceHotSpotX * scale));
        hotSpotY = (int) Math.max(1, Math.round(sourceHotSpotY * scale));
        this.cursors = new long[frameCount];
        //this.pixelData = new ByteBuffer[frameCount];
        boolean isA16Cursor = sourceBitmap instanceof CA16;
        Palette16[] palettePages = sourceBitmap.palette.paletteData;
        Palette16 basePalette = palettePages[0];

        int w = sourceBitmap.frames.getFirst().xSize();
        int h = sourceBitmap.frames.getFirst().ySize();
        ByteBuffer bb = BufferUtils.createByteBuffer(w * h * 4);
        byte b = (byte) 0;
        for (int i = 0; i < frameCount; i++) {
            GameBitmapFrame frame = sourceBitmap.frames.get(i);
            bb.clear();
            while (bb.hasRemaining()) {
                bb.put(b);
            }
            bb.clear();
            if (isA16Cursor) {
                A16SpriteDecoder.decodeClipped(0, 0, w, h, frame.data(), 0, 0, w, h,
                        (x, y, encodedPixels, offset, count, stepX) ->
                                writeA16RgbaRun(bb, w, x, y, encodedPixels, offset, count, stepX, palettePages)
                );
            } else {
                Rle8SpriteDecoder.decodeClipped(0, 0, w, h, frame.data(), 0, 0, w, h,
                        (x, y, paletteIndices, offset, count, stepX) ->
                                writeRgbaRun(bb, w, x, y, paletteIndices, offset, count, stepX, basePalette)
                );
            }
            bb.position(bb.capacity());
            GLFWImage image = glfwImage(w, h, bb.flip(), (float) scale);
            try {
                cursors[i] = glfwCreateCursor(image, hotSpotX, hotSpotY);
                if (cursors[i] == NULL) {
                    throw new RuntimeException("glfwCreateCursor failed for frame " + i);
                }
            } finally {
                freeGlfwImage(image);
            }
        }
        currentFrame = 0;
    }

    /**
     * Creates a scaled GLFW cursor image from one decoded cursor frame.
     * not ported.
     */
    static GLFWImage glfwImage(int w, int h, ByteBuffer pixels, float scale) {

        int dstW = Math.max(1, Math.round(w * scale));
        int dstH = Math.max(1, Math.round(h * scale));

        ByteBuffer dstPixels = MemoryUtil.memAlloc(dstW * dstH * 4); // RGBA8

        stbir_resize(
                pixels,   // input RGBA8
                w,
                h,
                w * 4,       // input stride
                dstPixels,
                dstW,
                dstH,
                dstW * 4,       // output stride
                STBIR_RGBA,
                STBIR_TYPE_UINT8_SRGB_ALPHA,
                STBIR_EDGE_CLAMP,
                STBIR_FILTER_POINT_SAMPLE
        );

        GLFWImage out = GLFWImage.malloc();
        out.set(dstW, dstH, dstPixels);
        return out;
    }

    /**
     * Releases the transient GLFW image buffer after GLFW copies it into a cursor handle.
     * not ported.
     */
    private static void freeGlfwImage(GLFWImage image) {
        MemoryUtil.memFree(image.pixels(image.width() * image.height() * 4));
        image.free();
    }

    /**
     * not ported.
     */
    @Override
    public void hide() {
        if (Globals.isWindowed != 0) {
            return;
        }

        showCount -= 1;
        if (showCount == 0) {
            cursorVisible = 0;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        }
    }

    /**
     * not ported.
     */
    @Override
    public void show() {
        if (Globals.isWindowed != 0) {
            return;
        }

        if (showCount == 0) {
            refreshPresentationScale();
            applyCurrentCursorFrame();
            cursorVisible = 1;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
        showCount += 1;
    }

    /**
     * Applies the current animation frame to the GLFW cursor handle without changing cursor visibility.
     * not ported.
     */
    private void applyCurrentCursorFrame() {
        if (cursors != null) {
            glfwSetCursor(window, cursors[currentFrame]);
        }
    }

    /**
     * Refreshes active cursor handles when the presentation source crop changes scale.
     * not ported.
     */
    private void refreshPresentationScale() {
        if (sourceBitmap == null) {
            return;
        }
        double nextScale = currentPresentationScale();
        if (cursors == null || Math.abs(nextScale - scale) > 0.001) {
            rebuildCursors(nextScale);
            applyCurrentCursorFrame();
        }
    }

    /**
     * Native support extracted from CMainWindow::OnSetCursor @00484A09.
     * Java fullscreen uses GLFW cursor handles for the game cursor, so only the native windowed arrow restore maps here.
     */
    @Override
    public int applyMainWindowSetCursor() {
        if (Globals.isWindowed != 0) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            glfwSetCursor(window, NULL);
        }
        return 1;
    }

    /**
     * Calculates the active cursor image scale from the current Java presentation transform.
     * not ported.
     */
    private double currentPresentationScale() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(window, width, height);
            PresentationTransform transform = PresentationSupport.currentTransform(width.get(0), height.get(0));
            return Math.min(transform.scaleX(), transform.scaleY());
        }
    }

    /**
     * Updates the cursor animation and presentation-scale-dependent OS cursor handles.
     * not ported.
     */
    @Override
    public void update() {
        refreshPresentationScale();
        super.update();
    }

    /**
     * Applies animation frame changes while preserving the current GLFW cursor visibility mode.
     * not ported.
     */
    @Override
    protected void onCursorFrameChanged() {
        refreshPresentationScale();
        applyCurrentCursorFrame();
    }

    /**
     * not ported.
     */
    public void destroy() {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        glfwSetCursor(window, NULL); // restore default cursor
        destroyCursorHandles();
    }

    /**
     * Destroys native GLFW cursor handles currently owned by this mouse pointer.
     * not ported.
     */
    private void destroyCursorHandles() {
        if (cursors == null) {
            return;
        }
        for (long cursor : cursors) {
            if (cursor != NULL) {
                glfwDestroyCursor(cursor);
            }
        }
        cursors = null;
    }

    /**
     * Writes one decoded cursor run into the RGBA buffer expected by GLFW cursor images.
     * not ported.
     */
    private static void writeRgbaRun(
            ByteBuffer target,
            int width,
            int x,
            int y,
            byte[] paletteIndices,
            int offset,
            int count,
            int stepX,
            Palette16 palette
    ) {
        RGB16[] pal = palette.data();
        int di = (y * width + x) * 4;
        for (int i = 0; i < count; i++) {
            RGB16 rgb16 = pal[paletteIndices[offset + i] & 0xFF];
            writeRgbaPixel(target, di, rgb16.r(), rgb16.g(), rgb16.b(), 0xFF);
            di += stepX * 4;
        }
    }

    /**
     * Writes one decoded CA16 cursor run into the RGBA buffer expected by GLFW cursor images.
     * not ported.
     */
    private static void writeA16RgbaRun(
            ByteBuffer target,
            int width,
            int x,
            int y,
            byte[] encodedPixels,
            int offset,
            int count,
            int stepX,
            Palette16[] palettePages
    ) {
        int di = (y * width + x) * 4;
        ByteBuffer bb = ByteBuffer.wrap(encodedPixels)
                .order(ByteOrder.LITTLE_ENDIAN)
                .position(offset);

        for (int i = 0; i < count; i++) {
            int encodedPixel = bb.getShort() & 0xFFFF;
            int alphaLevel = (encodedPixel >>> 9) & 0x0F;
            if (alphaLevel != 0) {
                RGB16 rgb16 = palettePages[alphaLevel].data()[(encodedPixel >>> 1) & 0xff];
                writeRgbaPixel(target, di, rgb16.r(), rgb16.g(), rgb16.b(), alphaLevel * 0x11);
            }
            di += stepX * 4;
        }
    }

    /**
     * Java GLFW cursor image support. GLFWImage pixels are byte-addressed RGBA; do not use packed Java ints here
     * because ByteBuffer.putInt writes in big-endian order unless the buffer order is changed.
     * not ported.
     */
    private static void writeRgbaPixel(ByteBuffer target, int offset, int r, int g, int b, int a) {
        target.put(offset, (byte) r);
        target.put(offset + 1, (byte) g);
        target.put(offset + 2, (byte) b);
        target.put(offset + 3, (byte) a);
    }

}
