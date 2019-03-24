package nokori.clear.vg.widget.assembly;

import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

/**
 * This is an empty generic implementation of a Widget that can be used primarily as a container for other Widgets.
 */
public class WidgetAssembly extends Widget {
	
	private ClearColor backgroundFill = null;
	
	public WidgetAssembly() {
		this(0f, 0f);
	}
	
	public WidgetAssembly(float x, float y) {
		this(x, y, 0f, 0f);
	}

	public WidgetAssembly(float x, float y, float width, float height) {
		super(x, y, width, height);
	}
	
	public WidgetAssembly(WidgetClip widgetClip) {
		addChild(widgetClip);
	}
	
	public WidgetAssembly(float width, float height, WidgetClip widgetClip) {
		super(0, 0, width, height);
		addChild(widgetClip);
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		
	}
	
	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		if (backgroundFill != null) {
			backgroundFill.tallocNVG(fill -> {
				long vg = context.get();
				float x = getClippedX();
				float y = getClippedY();
				float w = getWidth();
				float h = getHeight();
				
				NanoVG.nvgBeginPath(vg);
				NanoVG.nvgRoundedRect(vg, x, y, w, h, 0);
				NanoVG.nvgFillColor(vg, fill);
				NanoVG.nvgFill(vg);
				NanoVG.nvgClosePath(vg);
			});
		}
	}
	
	@Override
	protected void addChildCallback(Widget widget) {
		super.addChildCallback(widget);
		//resizeWidthHeight();
	}

	@Override
	protected void removeChildCallback(Widget widget) {
		super.removeChildCallback(widget);
		//resizeWidthHeight();
	}

	public void setBackgroundFill(ClearColor backgroundFill) {
		this.backgroundFill = backgroundFill;
	}

	public ClearColor getBackgroundFill() {
		return backgroundFill;
	}
	
	@Override
	public void dispose() {}
}
