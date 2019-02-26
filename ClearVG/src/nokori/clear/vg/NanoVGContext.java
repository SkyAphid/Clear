package nokori.clear.vg;

import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class NanoVGContext {
	private boolean modernOpenGL;
	private long nvgContext;
	
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
