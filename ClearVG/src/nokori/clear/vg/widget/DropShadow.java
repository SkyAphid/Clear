package nokori.clear.vg.widget;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class DropShadow extends Rectangle {
	
	private float shadowRadius;
	private Vector2f shadowOffset = new Vector2f(0, 0);
	
	public DropShadow(float cornerRadius, ClearColor fill) {
		this(cornerRadius, 4f, 2f, 2f, fill);
	}
	
	public DropShadow(float cornerRadius, float shadowRadius, float shadowOffsetX, float shadowOffsetY, ClearColor fill) {
		this(0, 0, 0, 0, cornerRadius, shadowRadius, shadowOffsetX, shadowOffsetY, fill);
		syncToParent = true;
	}

	public DropShadow(float x, float y, float width, float height, float cornerRadius, float shadowRadius, float shadowOffsetX, float shadowOffsetY, ClearColor fill) {
		super(x, y, width, height, fill, null);
		this.shadowRadius = shadowRadius;
		shadowOffset.x = shadowOffsetX;
		shadowOffset.y = shadowOffsetY;
	}
	
	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		long vg = context.get();
		
		float x = getRenderX(pos.x) + shadowOffset.x;
		float y = getRenderY(pos.y) + shadowOffset.y;
		float w = getWidth();
		float h = getHeight();
		
		try (MemoryStack stack = MemoryStack.stackPush()){
			NVGColor fill = this.fill.mallocNVG(stack);
			NVGColor transparent = ClearColor.TRANSPARENT.mallocNVG(stack);

			NVGPaint paint = NanoVG.nvgBoxGradient(vg, x + shadowRadius, y + shadowRadius, w - (shadowRadius * 2), h - (shadowRadius * 2), 4, 12, fill, transparent, NVGPaint.create());
			NanoVG.nvgBeginPath(vg);
			NanoVG.nvgRect(vg, x, y, w, h);
			NanoVG.nvgFillPaint(vg, paint);
			NanoVG.nvgFill(vg);
			NanoVG.nvgClosePath(vg);
		}
	}
}
