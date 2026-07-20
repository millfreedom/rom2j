package ua.millfreedom.rom2.model.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.GameBitmapFrame;
import ua.millfreedom.rom2.model.color.RGB32;

import java.nio.ByteBuffer;
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

        GameBitmapFrame firstFrame = sourceBitmap.frame(0);
        int w = firstFrame.width();
        int h = firstFrame.height();
        boolean a16 = sourceBitmap instanceof CA16;
        A16PaletteLookup a16PaletteLookup = a16
                ? A16PaletteLookup.resolve(sourceBitmap.palette.paletteData)
                : null;
        int[] indexedPalette = a16 ? null : sourceBitmap.palette.paletteData[0].data();
        ByteBuffer bb = BufferUtils.createByteBuffer(w * h * 4);
        for (int i = 0; i < frameCount; i++) {
            GameBitmapFrame frame = sourceBitmap.frame(i);
            bb.clear();
            int[] pixelCodes = frame.pixels();
            for (int pixel = 0; pixel < pixelCodes.length; pixel++) {
                int pixelCode = pixelCodes[pixel];
                int color = a16
                        ? a16PaletteLookup.sourceColor(pixelCode)
                        : pixelCode == GameBitmapFrame.TRANSPARENT_INDEX ? RGB32.TBLACK : indexedPalette[pixelCode];
                writeRgbaPixel(bb, pixel * 4, color);
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
     * Java GLFW cursor image support. GLFWImage pixels are byte-addressed RGBA; do not use packed Java ints here
     * because ByteBuffer.putInt writes in big-endian order unless the buffer order is changed.
     * not ported.
     */
    private static void writeRgbaPixel(ByteBuffer target, int offset, int color) {
        target.put(offset, (byte) RGB32.r(color));
        target.put(offset + 1, (byte) RGB32.g(color));
        target.put(offset + 2, (byte) RGB32.b(color));
        target.put(offset + 3, (byte) RGB32.a(color));
    }

}
