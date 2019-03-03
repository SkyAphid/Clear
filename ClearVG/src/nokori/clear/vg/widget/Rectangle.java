package nokori.clear.vg.widget;

import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.attachments.FillAttachment;
import nokori.clear.vg.widget.attachments.StrokeFillAttachment;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

/**
 * A widget that draws a rectangle at the given coordinates. It can be manually configured or set to sync up to the parent container.
 */
public class Rectangle extends Widget implements FillAttachment, StrokeFillAttachment {
	
	protected float cornerRadius;
	protected boolean syncToParent = false;
	
	protected ClearColor fill, strokeFill;
	
	private float strokeWidth = 1.0f;
	
	/*
	 * 
	 * Sync to parent modes
	 * 
	 */
	
	public Rectangle(ClearColor fill) {
		this(fill, null);
	}
	
	public Rectangle(float cornerRadius, ClearColor fill) {
		this(cornerRadius, fill, null);
	}
	
	public Rectangle(ClearColor fill, ClearColor strokeFill) {
		this(0f, fill, strokeFill);
	}
	
	public Rectangle(float cornerRadius, ClearColor fill, ClearColor strokeFill) {
		this(0, 0, 0, 0, cornerRadius, fill, strokeFill);
		syncToParent = true;
	}
	
	/*
	 * 
	 * Manual configuration modes
	 * 
	 */
	
	public Rectangle(float x, float y, float width, float height, ClearColor fill) {
		this(x, y, width, height, 0, fill);
	}
	
	public Rectangle(float x, float y, float width, float height, ClearColor fill, ClearColor strokeFill) {
		this(x, y, width, height, 0, fill, strokeFill);
	}
	
	public Rectangle(float x, float y, float width, float height, float cornerRadius, ClearColor fill) {
		this(x, y, width, height, fill, null);
	}
	
	public Rectangle(float x, float y, float width, float height, float cornerRadius, ClearColor fill, ClearColor strokeFill) {
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
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		if (syncToParent) {
			pos.x = 0;
			pos.y = 0;
			size.x = parent.getWidth();
			size.y = parent.getHeight();
		}
	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		long vg = context.get();
		
		float x = getRenderX();
		float y = getRenderY();
		float w = getWidth();
		float h = getHeight();
		
		fill.memoryStackPush(fill -> {
			NanoVG.nvgBeginPath(vg);
			NanoVG.nvgRoundedRect(vg, x, y, w, h, cornerRadius);
			NanoVG.nvgFillColor(vg, fill);
			NanoVG.nvgFill(vg);
			
			if (strokeFill != null) {
				strokeFill.memoryStackPush(strokeFill -> {
					NanoVG.nvgStrokeWidth(vg, strokeWidth);
					NanoVG.nvgStrokeColor(vg, strokeFill);
					NanoVG.nvgStroke(vg);
				});
			}
			
			NanoVG.nvgClosePath(vg);
		});
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

	@Override
	public ClearColor getStrokeFill() {
		return strokeFill;
	}

	@Override
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
