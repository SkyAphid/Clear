package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import org.lwjgl.nanovg.NanoVG;

/**
 * This is an empty generic implementation of a Widget that can be used primarily as a container for other Widgets.
 */
public class WidgetAssembly extends Widget {
	
	private ClearColor backgroundFill = null;
	

	/**
	 * Initializes the WidgetAssembly with the coordinates (0, 0) and the dimensions (0, 0).
	 */
	public WidgetAssembly() {
		super(0f, 0f, 0f, 0f);
	}
	
	/**
	 * Creates a WidgetAssembly configured to be synchronized to its parent with a WidgetSynch. 

	 * @param widgetSynch - the WidgetSynch to add to this WidgetAssembly.
	 */
	public WidgetAssembly(WidgetSynch widgetSynch) {
		this();
		addChild(widgetSynch);
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
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}
	
	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
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
