package nokori.clear.windows;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Cursor {

    private long handle;
    private Type type = null;

    public Cursor(Type type) {
        this.type = type;
        handle = glfwCreateStandardCursor(type.shape);
    }

    public Cursor(File file, int hotX, int hotY) {
        int[] w = new int[1];
        int[] h = new int[1];
        int[] c = new int[1];

        ByteBuffer buffer = STBImage.stbi_load(file.getAbsolutePath(), w, h, c, 4);

        GLFWImage glfwImg = GLFWImage.malloc();
        glfwImg.set(w[0], h[0], buffer);

        handle = glfwCreateCursor(glfwImg, hotX, hotY);

        glfwImg.free();
        STBImage.stbi_image_free(buffer);
    }

    Cursor(long handle) {
        this.handle = handle;
    }

    public void apply(Window window) {
        apply(window.getHandle());
    }

    public void apply(long windowHandle) {
        glfwSetCursor(windowHandle, handle);
    }

    public long getHandle() {
        return handle;
    }

    public Type getType() {
        return type;
    }

    public void destroy() {
        glfwDestroyCursor(handle);
    }

    public enum Type {
        /**
         * The default arrow-type mouse icon.
         */
        ARROW(GLFW_ARROW_CURSOR),

        /**
         * The I cursor used frequently for typing interfaces.
         */
        I_BEAM(GLFW_IBEAM_CURSOR),

        /**
         * A cross-shaped crosshair cursor.
         */
        CROSSHAIR(GLFW_CROSSHAIR_CURSOR),

        /**
         * A pointing finger icon.
         */
        HAND(GLFW_HAND_CURSOR),

        /**
         * A cursor used for resizing an element horizontally.
         */
        HORIZONTAL_RESIZE(GLFW_HRESIZE_CURSOR),

        /**
         * A cursor used for resizing an element vertically.
         */
        VERTICAL_RESIZE(GLFW_VRESIZE_CURSOR);

        private int shape;

        private Type(int shape) {
            this.shape = shape;
        }
    }
}