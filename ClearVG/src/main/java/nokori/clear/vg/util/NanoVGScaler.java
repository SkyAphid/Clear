package nokori.clear.vg.util;

import nokori.clear.vg.NanoVGContext;
import org.lwjgl.nanovg.NanoVG;

/**
 * This is a wrapper that allows for the easy managing of scaling in NanoVG. This class will store the current scale and allow you to apply it and reset it as needed.
 */
public class NanoVGScaler {
	private float scale = 1.0f;
	
	/**
	 * Calls nvgSave() and nvgScale() with the settings in this object.
	 */
	public void pushScale(NanoVGContext context) {
		if (scale != 1.0f) {
			long ctx = context.get();
			
			NanoVG.nvgSave(ctx);
			NanoVG.nvgScale(ctx, scale, scale);
		}
	}
	
	/**
	 * Shortcut for calling nvgRestore() after you've used pushScale() already, but this only works if you called pushScale() first.
	 */
	public void popScale(NanoVGContext context) {
		if (scale != 1.0f) {
			NanoVG.nvgRestore(context.get());
		}
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public void offsetScale(float amount) {
		scale += amount;
	}
	
	public float applyScale(float value) {
		return applyScale(value, scale);
	}
	
	public static float applyScale(float value, float scale) {
		return value / scale;
	}
}
