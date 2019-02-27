package nokori.clear.vg.widget.assembly;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

/**
 * Contains and manages widgets. E.G. a WidgetAssembly is a window, and a Widget would be a button in that window.
 */
public class WidgetAssembly extends Widget {
	
	private ClearColor backgroundFill = null;
	
	protected Vector2f pos = new Vector2f(0, 0);
	protected Vector2f size = new Vector2f(0, 0);
	
	public WidgetAssembly() {
		this(0, 0);
	}
	
	public WidgetAssembly(float x, float y) {
		this(x, y, 0, 0);
	}

	public WidgetAssembly(float x, float y, float width, float height) {
		pos.x = x;
		pos.y = y;
		size.x = width;
		size.y = height;
	}
	
	public WidgetAssembly(WidgetClip widgetClip) {
		addChild(widgetClip);
	}
	
	public WidgetAssembly(float width, float height, WidgetClip widgetClip) {
		size.x = width;
		size.y = height;
		addChild(widgetClip);
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		calculateWidthHeight();
	}
	
	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		if (backgroundFill != null) {
			backgroundFill.stackPushLambda(fill -> {
				long vg = context.get();
				float x = getRenderX();
				float y = getRenderY();
				float w = size.x;
				float h = size.y;
				
				NanoVG.nvgBeginPath(vg);
				NanoVG.nvgRoundedRect(vg, x, y, w, h, 0);
				NanoVG.nvgFillColor(vg, fill);
				NanoVG.nvgFill(vg);
				NanoVG.nvgClosePath(vg);
			});
		}
	}
	
	/**
	 * Calculates the width and height of this container by using the widgets in this container. The container's own x/y acts as the minX/minY, then
	 * the maxX/maxY is derived from the children widgets. From there, a width and height is calculated for the Container.
	 */
	private void calculateWidthHeight() {
		float minX = pos.x;
		float minY = pos.y;
		float maxX = minX + size.x;
		float maxY = minY + size.y;
		
		for (Widget w : children) {
			float wMaxX = pos.x + w.getX() + w.getWidth();
			float wMaxY = pos.y + w.getY() + w.getHeight();

			if (wMaxX > maxX) {
				maxX = wMaxX;
			}

			if (wMaxY > maxY) {
				maxY = wMaxY;
			}
		}
		
		size.x = (maxX - minX);
		size.y = (maxY - minY);
	}

	public void setBackgroundFill(ClearColor backgroundFill) {
		this.backgroundFill = backgroundFill;
	}

	public ClearColor getBackgroundFill() {
		return backgroundFill;
	}
	
	@Override
	public float getX() {
		return pos.x();
	}

	public void setX(float x) {
		pos.x = x;
	}

	@Override
	public float getY() {
		return pos.y();
	}

	public void setY(float y) {
		pos.y = y;
	}

	@Override
	public float getWidth() {
		return size.x();
	}

	@Override
	public float getHeight() {
		return size.y();
	}
	
	@Override
	public void dispose() {}
}
