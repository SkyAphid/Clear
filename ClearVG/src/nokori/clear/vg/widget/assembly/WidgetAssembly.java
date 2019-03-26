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
	private boolean syncToWindow = false;
	
	/**
	 * This creates a Widget Assembly that will synchronize its size with the window it's in. If you want to make a shapeless container for widgets, 
	 * I recommend using a WidgetContainer instead.
	 */
	public WidgetAssembly() {
		this(0f, 0f, 0f, 0f);
		syncToWindow = true;
	}
	
	/**
	 * Creates a WidgetAssembly with the given dimensions.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public WidgetAssembly(float x, float y, float width, float height) {
		super(x, y, width, height);
	}
	
	/**
	 * Creates a WidgetAssembly with the given width and height and configures it to use the given WidgetClip for positioning.
	 * 
	 * @param width
	 * @param height
	 * @param widgetClip
	 */
	public WidgetAssembly(float width, float height, WidgetClip widgetClip) {
		super(0, 0, width, height);
		addChild(widgetClip);
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		if (syncToWindow) {
			getPosition().set(0, 0);
			getSize().set(window.getFramebufferWidth(), window.getFramebufferHeight());
		}
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

	public void setBackgroundFill(ClearColor backgroundFill) {
		this.backgroundFill = backgroundFill;
	}

	public ClearColor getBackgroundFill() {
		return backgroundFill;
	}
	
	@Override
	public void dispose() {}
}
