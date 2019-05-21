package nokori.clear.vg.widget;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;

import static org.lwjgl.nanovg.NanoVG.*;

public class CircleWidget extends Widget {
	
	protected float radius;
	protected ClearColor fill, strokeFill;
	
	public CircleWidget(ClearColor fill, float radius) {
		this(0f, 0f, radius, fill, null);
	}
	
	public CircleWidget(float x, float y, float radius, ClearColor fill, ClearColor strokeFill) {
		super(x, y, radius * 2, radius * 2);
		this.fill = fill;
		this.strokeFill = strokeFill;
		this.radius = radius;
	}
	
	@Override
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		long vg = context.get();
		
		nvgBeginPath(vg);
		nvgCircle(vg, getClippedX() + getWidth()/2f, getClippedY() + getHeight()/2f, radius);
		
		if (fill != null) {
			fill.tallocNVG(fill -> {
				nvgFillColor(vg, fill);
				nvgFill(vg);
			});
		}
		
		if (strokeFill != null) {
			strokeFill.tallocNVG(strokeFill -> {
				nvgStrokeColor(vg, strokeFill);
				nvgStroke(vg);
			});
		}

		nvgClosePath(vg);
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public ClearColor getFill() {
		return fill;
	}

	public void setFill(ClearColor fill) {
		this.fill = fill;
	}

	public ClearColor getStrokeFill() {
		return strokeFill;
	}

	public void setStrokeFill(ClearColor strokeFill) {
		this.strokeFill = strokeFill;
	}

	@Override
	public void dispose() {}
}
