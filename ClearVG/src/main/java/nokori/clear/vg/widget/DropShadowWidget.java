package nokori.clear.vg.widget;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetSynch;
import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;

public class DropShadowWidget extends RectangleWidget {
	
	public static final ClearColor DEFAULT_FILL = ClearColor.LIGHT_BLACK;
	public static final float DEFAULT_SHADOW_RADIUS = 4f;
	public static final float DEFAULT_SHADOW_OFFSET = 2f;
	
	private float shadowRadius;
	private Vector2f shadowOffset = new Vector2f(0, 0);
	
	public DropShadowWidget() {
		this(DEFAULT_FILL);
	}
	
	public DropShadowWidget(float cornerRadius) {
		this(cornerRadius, DEFAULT_FILL);
	}
	
	public DropShadowWidget(ClearColor fill) {
		this(0, DEFAULT_SHADOW_RADIUS, DEFAULT_SHADOW_OFFSET, DEFAULT_SHADOW_OFFSET, fill);
	}
	
	public DropShadowWidget(float cornerRadius, ClearColor fill) {
		this(cornerRadius, DEFAULT_SHADOW_RADIUS, DEFAULT_SHADOW_OFFSET, DEFAULT_SHADOW_OFFSET, fill);
	}
	
	public DropShadowWidget(float cornerRadius, float shadowRadius, float shadowOffset) {
		this(cornerRadius, shadowRadius, shadowOffset, shadowOffset, DEFAULT_FILL);
	}
	
	public DropShadowWidget(float cornerRadius, float shadowRadius, float shadowOffsetX, float shadowOffsetY, ClearColor fill) {
		this(0, 0, 0, 0, cornerRadius, shadowRadius, shadowOffsetX, shadowOffsetY, fill);
		addChild(new WidgetSynch(WidgetSynch.Mode.WITH_PARENT));
	}

	public DropShadowWidget(float x, float y, float width, float height, float cornerRadius, float shadowRadius, float shadowOffsetX, float shadowOffsetY, ClearColor fill) {
		super(x, y, width, height, fill, null);
		this.shadowRadius = shadowRadius;
		shadowOffset.x = shadowOffsetX;
		shadowOffset.y = shadowOffsetY;
	}
	
	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		long vg = context.get();
		
		float x = getClippedX() + shadowOffset.x;
		float y = getClippedY() + shadowOffset.y;
		float w = getWidth();
		float h = getHeight();
		
		try (MemoryStack stack = MemoryStack.stackPush()){
			NVGColor fill = this.fill.mallocNVG(stack);
			NVGColor transparent = this.fill.copy().alpha(0f).mallocNVG(stack);

			NVGPaint paint = NanoVG.nvgBoxGradient(vg, x + shadowRadius, y + shadowRadius, w - (shadowRadius * 2), h - (shadowRadius * 2), 4, 12, fill, transparent, NVGPaint.create());
			NanoVG.nvgBeginPath(vg);
			NanoVG.nvgRect(vg, x, y, w, h);
			NanoVG.nvgFillPaint(vg, paint);
			NanoVG.nvgFill(vg);
			NanoVG.nvgClosePath(vg);
		}
	}
}
