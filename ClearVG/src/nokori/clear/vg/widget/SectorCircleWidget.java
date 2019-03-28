package nokori.clear.vg.widget;

import static org.lwjgl.nanovg.NanoVG.*;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class SectorCircleWidget extends CircleWidget {
	
	protected float completion, rotation;
	protected float offsetX, offsetY;
	protected int circlePoints;
	
	/**
	 * Creates a sector circle with the given settings. Stroke fill is set to null automatically and rotation is set to zero.
	 * 
	 * @param fill - the fill color (set to null to have none)
	 * @param radius - the radius of the circle
	 * @param completion - the percentage of the circle that's filled (value between 0 and 1, where 1 is 100% filled)
	 * @param rotation - the rotation of the circle in radians
	 */
	public SectorCircleWidget(ClearColor fill, float radius, float completion) {
		this(fill, null, radius, completion, 0f, 0f, 0f);
	}

	/**
	 * Creates a sector circle with the given settings
	 * 
	 * @param fill - the fill color (set to null to have none)
	 * @param strokeFill - the stroke fill color (the outline - set to null to have none)
	 * @param radius - the radius of the circle
	 * @param completion - the percentage of the circle that's filled (value between 0 and 1, where 1 is 100% filled)
	 * @param rotation - the rotation of the circle in radians
	 * @param offsetX - the offset of the x-coordinate
	 * @param offsetY - the offset of the y-coordinate
	 */
	public SectorCircleWidget(ClearColor fill, ClearColor strokeFill, float radius, float completion, float rotation, float offsetX, float offsetY) {
		super(fill, strokeFill, radius);
		this.completion = completion;
		this.rotation = rotation;
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		long vg = context.get();
		
		float x = getX();
		float y = getY();
		
		float centerX = x + radius + offsetX;
		float centerY = y + radius + offsetY;
		
        float pi2 = (float) (Math.PI * 2) * completion;
		
		nvgBeginPath(vg);
		
		//The anti-aliasing behaves weird for some reason if I start at zero, giving the starting line a slight slant.
		int startOffset = 1;
		
		for (int i = startOffset; i < circlePoints; i++) {
        	float px = (float) (centerX + (radius * Math.cos(rotation + (i * pi2 / circlePoints))));
        	float py = (float) (centerY + (radius * Math.sin(rotation + (i * pi2 / circlePoints))));
			
			if (i == startOffset) {
				nvgMoveTo(vg, px, py);
			} else {
				nvgLineTo(vg, px, py);
			}
		}

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
