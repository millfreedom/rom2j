package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.Screen;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL40.*;

public class GLRenderer extends SoftRenderer {

    private int presentationTextureId;
    private int presentationProgramId;
    private int presentationSurfaceUniform;
    private int presentationTextureSizeUniform;
    private int presentationScaleUniform;
    private ByteBuffer uploadBuffer;


    // not ported.
    public GLRenderer(Screen screen) {
        super(screen);
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
