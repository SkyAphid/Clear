package nokori.clear.vg.widget;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.widget.assembly.ColoredWidgetImpl;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

import static org.lwjgl.nanovg.NanoVG.*;

public class Label extends ColoredWidgetImpl  {
	
	private String text;
	private Font font;
	private FontStyle style;
	private float size;
	private int textAlignment;
	
	public Label(ClearColor fill, String text, Font font, FontStyle style, float size) {
		this(0, 0, fill, null, text, font, style, size, Font.DEFAULT_TEXT_ALIGNMENT);
	}
	
	public Label(ClearColor fill, String text, Font font, FontStyle style, float size, int textAlignment) {
		this(0, 0, fill, null, text, font, style, size, textAlignment);
	}
	
	public Label(float x, float y, ClearColor fill, String text, Font font, FontStyle style, float size) {
		this(x, y, fill, text, font, style, size, Font.DEFAULT_TEXT_ALIGNMENT);
	}
	
	public Label(float x, float y, ClearColor fill, String text, Font font, FontStyle style, float size, int textAlignment) {
		this(x, y, fill, null, text, font, style, size, textAlignment);
	}

	public Label(float x, float y, ClearColor fill, ClearColor strokeFill, String text, Font font, FontStyle style, float size, int textAlignment) {
		super(x, y, 0, 0, fill, strokeFill);
		this.text = text;
		this.font = font;
		this.style = style;
		this.size = size;
		this.textAlignment = textAlignment;
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		Vector2f bounds = font.getTextBounds(context, text, size, textAlignment, style);
		
		setWidth(bounds.x);
		setHeight(bounds.y);
	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		font.configureNVG(context, size, textAlignment, style);
		
		long vg = context.get();
		
		NVGColor fill = (this.fill != null ? this.fill.callocNVG() : null);
		NVGColor strokeFill = (this.strokeFill != null ? this.strokeFill.callocNVG() : null);
		
		nvgBeginPath(vg);

		if (fill != null) {
			nvgFillColor(vg, fill);
		}
		
		if (strokeFill != null) {
			nvgStrokeColor(vg, strokeFill);
		}
		
		nvgText(vg, getRenderX(pos.x), getRenderY(pos.y), text);
		
		nvgClosePath(vg);
	}

	@Override
	public void dispose() {
		
	}

}
