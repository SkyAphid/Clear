package nokori.clear.vg.widget;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;

import java.util.ArrayList;

import static org.lwjgl.nanovg.NanoVG.*;

public class LabelWidget extends Widget {
	
	private static final float AUTO_CALCULATE_WIDTH = Float.MAX_VALUE;
	
	private String text;
	private Font font;
	private FontStyle style;
	private float fontSize;
	private int textAlignment;
	
	private boolean autoCalculateWidth = false;
	
	private ArrayList<String> lines = null;
	private Vector2f bounds = new Vector2f();
	
	private ClearColor fill;
	
	/*
	 * Auto calculate width based on text bounds
	 */
	
	public LabelWidget(ClearColor fill, String text, Font font, FontStyle style, float fontSize) {
		this(0, 0, AUTO_CALCULATE_WIDTH, fill, text, font, style, fontSize, Font.DEFAULT_TEXT_ALIGNMENT);
	}
	
	public LabelWidget(ClearColor fill, String text, Font font, FontStyle style, float fontSize, int textAlignment) {
		this(0, 0, AUTO_CALCULATE_WIDTH, fill, text, font, style, fontSize, textAlignment);
	}
	
	public LabelWidget(float x, float y, ClearColor fill, String text, Font font, FontStyle style, float fontSize) {
		this(x, y, AUTO_CALCULATE_WIDTH, fill, text, font, style, fontSize);
	}
	
	public LabelWidget(float x, float y, ClearColor fill, String text, Font font, FontStyle style, float fontSize, int textAlignment) {
		this(x, y, AUTO_CALCULATE_WIDTH, fill, text, font, style, fontSize, textAlignment);
	}
	
	/*
	 * Width is set manually constructors
	 */

	public LabelWidget(float width, ClearColor fill, String text, Font font, FontStyle style, float fontSize) {
		this(0, 0, width, fill, text, font, style, fontSize, Font.DEFAULT_TEXT_ALIGNMENT);
	}
	
	public LabelWidget(float width, ClearColor fill, String text, Font font, FontStyle style, float fontSize, int textAlignment) {
		this(0, 0, width, fill, text, font, style, fontSize, textAlignment);
	}
	
	public LabelWidget(float x, float y, float width, ClearColor fill, String text, Font font, FontStyle style, float fontSize) {
		this(x, y, width, fill, text, font, style, fontSize, Font.DEFAULT_TEXT_ALIGNMENT);
	}

	public LabelWidget(float x, float y, float width, ClearColor fill, String text, Font font, FontStyle style, float fontSize, int textAlignment) {
		super(x, y, 0, 0);
		this.fill = fill;
		this.text = text;
		this.font = font;
		this.style = style;
		this.fontSize = fontSize;
		this.textAlignment = textAlignment;

		if (width == AUTO_CALCULATE_WIDTH) {
			autoCalculateWidth = true;
		} else {
	 		setWidth(width);
		}
	}

	@Override
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	/**
	 * Splits the text into lines (if there are line breaks) and calculates the height of this LabelWidget. This is called by <code>tick()</code> automatically, 
	 * but if desired, this can be called manually if the bounds are needed immediately.
	 * 
	 * @param context
	 * @return - this LabelWidget (for pretty code purposes)
	 */
	public LabelWidget calculateBounds(NanoVGContext context) {
		//Calculate the line split and then derive the height from that
		if (lines == null) {
			font.split(context, lines = new ArrayList<String>(), text, autoCalculateWidth ? AUTO_CALCULATE_WIDTH : getWidth(), fontSize, textAlignment, style);
			
			font.getTextBounds(context, bounds, lines, fontSize, textAlignment, style);
			
			//Calculate width automatically if applicable
			if (autoCalculateWidth) {
				setWidth(bounds.x());
			}
			
			setHeight(bounds.y());
		}
		
		return this;
	}
	
	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		font.configureNVG(context, fontSize, textAlignment, style);
		calculateBounds(context);
		
		long vg = context.get();
		
		NVGColor fill = (this.fill != null ? this.fill.callocNVG() : null);

		nvgBeginPath(vg);

		if (fill != null) {
			nvgFillColor(vg, fill);
		}
		
		for (int i = 0; i < lines.size(); i++) {
			String renderText = lines.get(i).replaceAll("\n", "");
			nvgText(vg, getClippedX(), getClippedY() + (font.getHeight(context) * i), renderText);
		}
		
		nvgClosePath(vg);
	}

	@Override
	public void dispose() {
		
	}

	public String getText() {
		return text;
	}

	public void setText(NanoVGContext context, String text) {
		if (!this.text.equals(text)) {
			lines = null;
		}
		
		this.text = text;
		
		calculateBounds(context);
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public FontStyle getStyle() {
		return style;
	}

	public void setStyle(FontStyle style) {
		this.style = style;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public int getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
	}

	public ClearColor getFill() {
		return fill;
	}

	public void setFill(ClearColor fill) {
		this.fill = fill;
	}
}
