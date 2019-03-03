package nokori.clear.vg.widget;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.attachments.FillAttachment;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

import static org.lwjgl.nanovg.NanoVG.*;

public class Label extends Widget implements FillAttachment{
	
	private String text;
	private Font font;
	private FontStyle style;
	private float fontSize;
	private int textAlignment;
	
	private Vector2f bounds = new Vector2f();
	
	private ClearColor fill;
	
	public Label(ClearColor fill, String text, Font font, FontStyle style, float fontSize) {
		this(0, 0, fill, text, font, style, fontSize, Font.DEFAULT_TEXT_ALIGNMENT);
	}
	
	public Label(ClearColor fill, String text, Font font, FontStyle style, float fontSize, int textAlignment) {
		this(0, 0, fill, text, font, style, fontSize, textAlignment);
	}
	
	public Label(float x, float y, ClearColor fill, String text, Font font, FontStyle style, float fontSize) {
		this(x, y, fill, text, font, style, fontSize, Font.DEFAULT_TEXT_ALIGNMENT);
	}

	public Label(float x, float y, ClearColor fill, String text, Font font, FontStyle style, float fontSize, int textAlignment) {
		super(x, y, 0, 0);
		this.fill = fill;
		this.text = text;
		this.font = font;
		this.style = style;
		this.fontSize = fontSize;
		this.textAlignment = textAlignment;
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		font.getTextBounds(context, bounds, text, fontSize, textAlignment, style);
		
		setWidth(bounds.x);
		setHeight(bounds.y);
	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		font.configureNVG(context, fontSize, textAlignment, style);
		
		long vg = context.get();
		
		NVGColor fill = (this.fill != null ? this.fill.callocNVG() : null);

		nvgBeginPath(vg);

		if (fill != null) {
			nvgFillColor(vg, fill);
		}
		
		nvgText(vg, getRenderX(), getRenderY(), text);
		
		nvgClosePath(vg);
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public ClearColor getFill() {
		return fill;
	}

	public void setFill(ClearColor fill) {
		this.fill = fill;
	}

}
