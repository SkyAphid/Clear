package nokori.clear.vg.widget;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetSynch;
import org.lwjgl.nanovg.NanoVG;

/**
 * A widget that draws a rectangle at the given coordinates. It can be manually configured or set to sync up to the parent container.
 */
public class RectangleWidget extends Widget  {
	
	protected ClearColor fill, strokeFill;
	protected float cornerRadius;
	private float strokeWidth = 1.0f;
	
	/*
	 * 
	 * Sync to parent modes
	 * 
	 */
	
	public RectangleWidget(ClearColor fill, boolean addWidgetSynch) {
		this(fill, null, addWidgetSynch);
	}
	
	public RectangleWidget(float cornerRadius, ClearColor fill, boolean addWidgetSynch) {
		this(cornerRadius, fill, null, addWidgetSynch);
	}
	
	public RectangleWidget(ClearColor fill, ClearColor strokeFill, boolean addWidgetSynch) {
		this(0f, fill, strokeFill, addWidgetSynch);
	}
	
	/**
	 * Creates a new automatically configured RectangleWidget 
	 * 
	 * @param cornerRadius - determines the radius of the corners. Set to 0 to create a hard rectangle.
	 * @param fill - the internal fill of the rectangle
	 * @param strokeFill - the outline fill of the rectangle
	 * @param addWidgetSynch - if true, a WidgetSynch is added to this RectangleWidget, configuring it to synchronize itself with its parent widget.
	 */
	public RectangleWidget(float cornerRadius, ClearColor fill, ClearColor strokeFill, boolean addWidgetSynch) {
		this(0, 0, 0, 0, cornerRadius, fill, strokeFill);
		
		if (addWidgetSynch) {
			addChild(new WidgetSynch(WidgetSynch.Mode.WITH_PARENT));
		}
	}
	
	/*
	 * 
	 * Manual configuration modes
	 * 
	 */
	
	public RectangleWidget(float width, float height, ClearColor fill) {
		this(0f, 0f, width, height, fill);
	}
	
	public RectangleWidget(float x, float y, float width, float height, ClearColor fill) {
		this(x, y, width, height, 0, fill);
	}
	
	public RectangleWidget(float x, float y, float width, float height, ClearColor fill, ClearColor strokeFill) {
		this(x, y, width, height, 0, fill, strokeFill);
	}
	
	public RectangleWidget(float x, float y, float width, float height, float cornerRadius, ClearColor fill) {
		this(x, y, width, height, fill, null);
	}
	
	public RectangleWidget(float x, float y, float width, float height, float cornerRadius, ClearColor fill, ClearColor strokeFill) {
		super(x, y, width, height);
		this.cornerRadius = cornerRadius;
		this.fill = fill;
		this.strokeFill = strokeFill;
	}

	/*
	 * 
	 * Methods start
	 * 
	 */
	
	@Override
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		long vg = context.get();
		
		float x = getClippedX();
		float y = getClippedY();
		float w = getWidth();
		float h = getHeight();
		
		if (fill != null) {
			fill.tallocNVG(fill -> {
				NanoVG.nvgBeginPath(vg);
				NanoVG.nvgRoundedRect(vg, x, y, w, h, cornerRadius);
				NanoVG.nvgFillColor(vg, fill);
				NanoVG.nvgFill(vg);
				NanoVG.nvgClosePath(vg);
			});
		}
		
		if (strokeFill != null) {
			strokeFill.tallocNVG(strokeFill -> {
				NanoVG.nvgBeginPath(vg);
				NanoVG.nvgRoundedRect(vg, x, y, w, h, cornerRadius);
				NanoVG.nvgStrokeWidth(vg, strokeWidth);
				NanoVG.nvgStrokeColor(vg, strokeFill);
				NanoVG.nvgStroke(vg);
				NanoVG.nvgClosePath(vg);
			});
		}
		
	}
	
	public float getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(float strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	@Override
	public void dispose() {
		
	}

	public ClearColor getStrokeFill() {
		return strokeFill;
	}

	public ClearColor getFill() {
		return fill;
	}

	public void setFill(ClearColor fill) {
		this.fill = fill;
	}

	public void setStrokeFill(ClearColor strokeFill) {
		this.strokeFill = strokeFill;
	}
}
