package nokori.clear.vg;

import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.opengl.GL11.*;

public class NanoVGContext {
	private boolean modernOpenGL;
	private long nvgContext;
	
	private int framebufferWidth, framebufferHeight;
	
	/**
	 * Initializes a NanoVG context and returns this object.
	 */
	public NanoVGContext init() {
		modernOpenGL = (GL11.glGetInteger(GL30.GL_MAJOR_VERSION) > 3) || (GL11.glGetInteger(GL30.GL_MAJOR_VERSION) == 3 && GL11.glGetInteger(GL30.GL_MINOR_VERSION) >= 2);
		
		if (modernOpenGL) {
			int flags = NanoVGGL3.NVG_STENCIL_STROKES | NanoVGGL3.NVG_ANTIALIAS;
			nvgContext = NanoVGGL3.nvgCreate(flags);
		} else {
			int flags = NanoVGGL2.NVG_STENCIL_STROKES | NanoVGGL2.NVG_ANTIALIAS;
			nvgContext = NanoVGGL2.nvgCreate(flags);
		}
		
		return this;
	}
	
	/**
	 * This is a shortcut function for the typical OpenGL clearing sequence you have to do before rendering. The following functions are called in the given order:<br>
	 * <br><code>glViewport(0, 0, viewportWidth, viewportHeight)</code>
	 * <br><code>glClearColor(clearColorR, G, B, A)</code>
	 * <br><code>glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT)</code>
	 */
	public static void glClearFrame(int viewportWidth, int viewportHeight, float clearColorR, float clearColorG, float clearColorB, float clearColorA) {
		glViewport(0, 0, viewportWidth, viewportHeight);
		glClearColor(clearColorR, clearColorG, clearColorB, clearColorA);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}
	
	/**
	 * Automatically configures NanoVG for rendering a single frame  (<code>nvgBeginFrame(context)</code>). If this is called, make sure <code>endFrame()</code> is called at the end of the corresponding frame.
	 * 
	 * @param windowWidth - the actual window width in pixels
	 * @param windowHeight - the actual window height in pixels
	 * @param framebufferWidth - the window framebuffer width (internal rendering)
	 * @param framebufferHeight - the window framebuffer height (internal rendering)
	 */
	public void beginFrame(int windowWidth, int windowHeight, int framebufferWidth, int framebufferHeight) {
		this.framebufferWidth = framebufferWidth;
		this.framebufferHeight = framebufferHeight;
		
      	float pxRatio = (float) framebufferWidth / (float) windowHeight;
        nvgBeginFrame(nvgContext, windowWidth, windowHeight, pxRatio);
	}
	
	/**
	 * Ends the current frame of rendering (<code>nvgEndFrame(context)</code>)
	 */
	public void endFrame() {
        nvgEndFrame(nvgContext);
	}

	/**
	 * @return true if the machine this program is running on supports modern OpenGL (Version 3 and above)
	 */
	public boolean isModernOpenGL() {
		return modernOpenGL;
	}

	/**
	 * @return the NanoVG context long ID.
	 */
	public long get() {
		return nvgContext;
	}
	
	/**
	 * @return the framebuffer width of the window configured in <code>beginFrame()</code>
	 */
	public int getFramebufferWidth() {
		return framebufferWidth;
	}

	/**
	 * @return the framebuffer height of the window configured in <code>beginFrame()</code>
	 */
	public int getFramebufferHeight() {
		return framebufferHeight;
	}

	/**
	 * Disposes this NanoVG context.
	 */
	public void dispose() {
		if (modernOpenGL) {
			NanoVGGL3.nvgDelete(nvgContext);
		} else {
			NanoVGGL2.nvgDelete(nvgContext);
		}
	}
}
