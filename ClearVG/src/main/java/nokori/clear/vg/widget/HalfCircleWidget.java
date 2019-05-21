package nokori.clear.vg.widget;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.WidgetAssembly;

import static org.lwjgl.nanovg.NanoVG.*;

public class HalfCircleWidget extends CircleWidget {
	
	public enum Orientation {
		LEFT,
		RIGHT;

		public float getCenterX(HalfCircleWidget widget) {
			switch(this) {
			case RIGHT:
				return widget.getClippedX();
			case LEFT:
			default:
				return widget.getClippedX() + widget.radius;
			}
		}
	};
	
	protected Orientation orientation;

	public HalfCircleWidget(float radius, ClearColor fill, Orientation orientation) {
		this(0f, 0f, radius, fill, null, orientation);
	}
	
	public HalfCircleWidget(float x, float y, float radius, ClearColor fill, ClearColor strokeFill, Orientation orientation) {
		super(x, y, radius, fill, strokeFill);
		this.orientation = orientation;
		
		setWidth(radius);
	}

	@Override
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		long vg = context.get();
		
		float x = orientation.getCenterX(this);
		float y = getClippedY() + radius;

        float startAngle;
        float endAngle;

        switch(orientation) {
		case RIGHT:
			startAngle = -NVG_PI/2;
			endAngle = NVG_PI/2;
			break;
		case LEFT:
		default:
			startAngle = NVG_PI/2;
			endAngle = NVG_PI * 1.5f;
			break;
        
        }

		nvgBeginPath(vg);
		
		nvgArc(vg, x, y, radius, startAngle, endAngle, NVG_CW);
		
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

	@Override
	public void dispose() {}

}
