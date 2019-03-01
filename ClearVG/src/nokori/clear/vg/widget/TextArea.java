package nokori.clear.vg.widget;

import static org.lwjgl.nanovg.NanoVG.*;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;
import nokori.clear.vg.ClearColor;
import nokori.clear.vg.ClearStaticResources;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.transition.FillTransition;
import nokori.clear.vg.transition.SimpleTransition;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetUtil;
import nokori.clear.vg.widget.attachments.FillAttachment;
import nokori.clear.windows.Cursor;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;
import nokori.clear.windows.event.MouseScrollEvent;

public class TextArea extends Widget implements FillAttachment {

	/*
	 * Text rendering
	 */
	
	private String text;
	private ArrayList<String> lines = new ArrayList<>();
	
	private Font font;
	private float fontSize;
	private FontStyle fontStyle = FontStyle.REGULAR;
	
	private ClearColor fill;
	private ClearColor highlightFill = ClearColor.LIGHT_BLUE;

	/*
	 * Line numbers
	 */
	
	private boolean lineNumbersEnabled = true;
	private Font lineNumberFont;
	private FontStyle lineNumberFontStyle = FontStyle.REGULAR;
	private float lineNumberFontSize;
	private ClearColor lineNumberFill = ClearColor.LIGHT_BLACK;

	/*
	 * Scrollbar
	 */
	
	//customization
	public static final float DEFAULT_SCROLLBAR_WIDTH = 10f;
	private float scrollbarWidth = DEFAULT_SCROLLBAR_WIDTH;
	
	public static final float DEFAULT_SCROLLBAR_CORNER_RADIUS = 5;
	private float scrollbarCornerRadius = DEFAULT_SCROLLBAR_CORNER_RADIUS;
	
	public static final float DEFAULT_SCROLLBAR_LEFT_PADDING = 50;
	private float scrollbarLeftPadding = DEFAULT_SCROLLBAR_LEFT_PADDING;
	
	public static final float DEFAULT_SCROLLBAR_RIGHT_PADDING = 0;
	private float scrollbarRightPadding = DEFAULT_SCROLLBAR_RIGHT_PADDING;
	
	private ClearColor scrollbarBackgroundFill = ClearColor.LIGHT_GRAY;
	private ClearColor scrollbarFill = ClearColor.DARK_GRAY;
	private ClearColor scrollbarHighlightFill = ClearColor.LIGHT_BLUE;
	
	//usage
	private float scroll = 0.0f;
	private float scrollIncrement = 0.01f;
	private SimpleTransition scrollTransition = null;
	
	private static final float SCROLLBAR_MIN_HEIGHT = 60f;
	private float scrollbarDefaultHeight;
	private float scrollbarHeight;
	
	private float scrollbarX = -1f, scrollbarY = -1f;
	
	private boolean scrollbarHovering = false;
	private boolean scrollbarSelected = false;
	
	private ClearColor scrollbarRenderFill = new ClearColor(scrollbarFill);
	private FillTransition scrollbarFillTransition = null;
	
	/*
	 * Caret
	 */
	private long lastTime = -1;
	private float caretFader = 0f;
	
	private ClearColor caretFill = ClearColor.LIGHT_BLACK;

	public TextArea(float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		this(0, 0, width, height, fill, text, font, fontSize);
	}

	public TextArea(float x, float y, float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		super(x, y, width, height);
		this.fill = fill;
		this.font = font;
		this.fontSize = fontSize;

		lineNumberFont = font;
		lineNumberFontSize = (int) (fontSize * 0.75);

		setText(text);
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		caretFader += (lastTime - System.currentTimeMillis());
		lastTime = System.currentTimeMillis();
	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		renderParagraph(context, getRenderX(), getRenderY(), size.x, size.y);
	}
	
	private void renderParagraph(NanoVGContext context, float x, float y, float width, float height) {

		/*
		 * Text calculations
		 */
		
		int textAlignment = Font.DEFAULT_TEXT_ALIGNMENT;
		font.configureNVG(context, fontSize, fontStyle);
		
		float scrollbarCompleteWidth = (scrollbarLeftPadding + scrollbarWidth + scrollbarRightPadding);
		float textAreaWidth = width - scrollbarCompleteWidth;

		if (lines == null) {
			font.split(context, lines = new ArrayList<>(), text, textAreaWidth, fontSize, textAlignment, fontStyle);
		}
		
		scrollIncrement = (1f / lines.size());
		
		float fontHeight = font.getHeight(context, fontSize, textAlignment, fontStyle);
		float stringHeight = font.getHeight(context, lines.size(), fontHeight, textAlignment, fontStyle);
		float renderAreaHeight = (height / fontHeight) * fontHeight;
		int indicesVisible = (int) (renderAreaHeight / fontHeight);
		
		/*
		 * 
		 * Culling calculations
		 * 
		 */
		
		//Scissor translation Y
		float scissorY = -(stringHeight * scroll);
		
		//Current start/end indices for text rendering so that anything outside the scissor range isn't rendered
		int indexOffset = 5;
		
		int startIndex = (int) (lines.size() * scroll) - indexOffset;
		
		if (startIndex < 0) {
			startIndex = 0;
		}
		
		int endIndex = (int) (startIndex + indicesVisible) + (indexOffset * 2);
		
		if (endIndex > lines.size()) {
			endIndex = lines.size();
		}
		
		/*
		 * Rendering
		 */
		
		//System.err.println(fontHeight + " " + renderAreaHeight + " " + indicesVisible + " | " + startIndex + " " + endIndex + " " + lines.size());
		
		long vg = context.get();
		
		//Text
		NVGColor fill = this.fill.callocNVG();

		nvgBeginPath(vg);
		nvgScissor(vg, x, y, width, height);
		nvgTranslate(vg, 0, scissorY);
		
		for (int i = 0; i < lines.size(); i++) {
			//Culls text that's outside of the scissoring range
			if (i < startIndex || i > endIndex) {
				continue;
			}
			
			float rY = y + (fontHeight * i);
			
			nvgFillColor(vg, fill);
			nvgText(vg, x, rY, lines.get(i));
		}
		
		nvgResetTransform(vg);
		nvgResetScissor(vg);
		nvgClosePath(vg);
		
		fill.free();
		
		//Scrollbar
		renderScrollbar(context, x, y, width, height, stringHeight/5f);
	}
	
	private void renderScrollbar(NanoVGContext context, float x, float y, float width, float height, float stringHeight) {
		
		/*
		 * 
		 * Calculations
		 * 
		 */
		
		scrollbarDefaultHeight = getHeight()/2;
		scrollbarHeight = Math.max(scrollbarDefaultHeight * (height / stringHeight), SCROLLBAR_MIN_HEIGHT);
		
		scrollbarX = (x + width) - scrollbarWidth - scrollbarRightPadding;
		scrollbarY = (y + ((height - scrollbarHeight) * scroll));
		
		ClearColor currentFill = (scrollbarHovering || scrollbarSelected) ? scrollbarHighlightFill : scrollbarFill;
		
		if (scrollbarSelected) {
			currentFill = currentFill.multiply(0.9f);
		}
		
		if (!scrollbarRenderFill.rgbMatches(currentFill) && scrollbarFillTransition == null) {
			scrollbarFillTransition = new FillTransition(100, scrollbarRenderFill, currentFill);
			scrollbarFillTransition.play();
			scrollbarFillTransition.setOnCompleted(t -> {
				scrollbarFillTransition = null;
			});
		}
		
		/*
		 * 
		 * Rendering
		 * 
		 */
		
		long vg = context.get();
		
		NVGColor scrollbarBackgroundFill = this.scrollbarBackgroundFill.callocNVG();
		NVGColor scrollbarFill = scrollbarRenderFill.callocNVG();
		
		//scrollbar background
		nvgBeginPath(vg);
		nvgRoundedRect(vg, scrollbarX, y, scrollbarWidth, height, scrollbarCornerRadius);
		nvgFillColor(vg, scrollbarBackgroundFill);
		nvgFill(vg);
		nvgClosePath(vg);
		
		//scrollbar
		nvgBeginPath(vg);
		nvgRoundedRect(vg, scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, scrollbarCornerRadius);
		nvgFillColor(vg, scrollbarFill);
		nvgFill(vg);
		nvgClosePath(vg);
		
		scrollbarBackgroundFill.free();
		scrollbarFill.free();
	}
	
	//TODO:
	@SuppressWarnings("unused")
	private void renderCaret(NanoVGContext context) {
		float alpha = 1.0f- (float) (Math.sin(caretFader * 0.004f) * 0.5 + 0.5);
	}
	
	@Override
	public void charEvent(Window window, CharEvent event) {
		super.charEvent(window, event);
	}
	
	@Override
	public void keyEvent(Window window, KeyEvent event) {
		super.keyEvent(window, event);
	}
	
	@Override
	public void mouseButtonEvent(Window window, MouseButtonEvent event) {
		super.mouseButtonEvent(window, event);
		
		if ((scrollbarHovering || scrollbarSelected) && event.isPressed() && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			scrollbarSelected = true;
		} else {
			scrollbarSelected = false;
		}
	}
	
	@Override
	public void mouseMotionEvent(Window window, MouseMotionEvent event) {
		super.mouseMotionEvent(window, event);
		
		boolean bScrollbarHovering = scrollbarHovering;
		scrollbarHovering = WidgetUtil.mouseWithinRectangle(window, scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight);
		
		if (!bScrollbarHovering && scrollbarHovering) {
			ClearStaticResources.getCursor(Cursor.Type.HAND).apply(window);
		}
		
		if (bScrollbarHovering && !scrollbarHovering) {
			ClearStaticResources.getCursor(Cursor.Type.ARROW).apply(window);
		}
	}
	
	@Override
	public void mouseScrollEvent(Window window, MouseScrollEvent event) {
		super.mouseScrollEvent(window, event);
		
		if (!isMouseWithin(window)) {
			return;
		}
		
		double dx = -event.getYoffset();
		
		float start = scroll;
		float end = (float) (scroll + (dx * scrollIncrement));
		
		if (scrollTransition != null) {
			scrollTransition.stop();
		}
		
		scrollTransition = new SimpleTransition(100, start, end);
		
		scrollTransition.setProgressCallback(v -> {
			scroll = v;
			
			if (scroll < 0f) {
				scroll = 0f;
			}
			
			if (scroll > 1f) {
				scroll = 1f;
			}
		});
		
		scrollTransition.play();
	}
	
	/*
	 * 
	 * General Settings
	 * 
	 */

	@Override
	public ClearColor getFill() {
		return fill;
	}

	public void setFill(ClearColor fill) {
		this.fill = fill;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
		lines = null;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
		lines = null;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
		lines = null;
	}
	
	public ClearColor getHighlightFill() {
		return highlightFill;
	}

	public void setHighlightFill(ClearColor highlightFill) {
		this.highlightFill = highlightFill;
	}
	
	/*
	 * Scrollbar settings
	 */
	
	public float getScrollbarWidth() {
		return scrollbarWidth;
	}

	public void setScrollbarWidth(float scrollbarWidth) {
		this.scrollbarWidth = scrollbarWidth;
	}

	public float getScrollbarCornerRadius() {
		return scrollbarCornerRadius;
	}

	public void setScrollbarCornerRadius(float scrollbarCornerRadius) {
		this.scrollbarCornerRadius = scrollbarCornerRadius;
	}

	public float getScrollbarLeftPadding() {
		return scrollbarLeftPadding;
	}

	public void setScrollbarLeftPadding(float scrollbarLeftPadding) {
		this.scrollbarLeftPadding = scrollbarLeftPadding;
	}

	public float getScrollbarRightPadding() {
		return scrollbarRightPadding;
	}

	public void setScrollbarRightPadding(float scrollbarRightPadding) {
		this.scrollbarRightPadding = scrollbarRightPadding;
	}

	public float getScrollIncrement() {
		return scrollIncrement;
	}

	public void setScrollIncrement(float scrollIncrement) {
		this.scrollIncrement = scrollIncrement;
	}

	public SimpleTransition getScrollTransition() {
		return scrollTransition;
	}

	public void setScrollTransition(SimpleTransition scrollTransition) {
		this.scrollTransition = scrollTransition;
	}

	public ClearColor getScrollbarBackgroundFill() {
		return scrollbarBackgroundFill;
	}

	public void setScrollbarBackgroundFill(ClearColor scrollbarBackgroundFill) {
		this.scrollbarBackgroundFill = scrollbarBackgroundFill;
	}

	public ClearColor getScrollbarFill() {
		return scrollbarFill;
	}

	public void setScrollbarFill(ClearColor scrollbarFill) {
		this.scrollbarFill = scrollbarFill;
	}
	
	public ClearColor getScrollbarHighlightFill() {
		return scrollbarHighlightFill;
	}

	public void setScrollbarHighlightFill(ClearColor scrollbarHighlightFill) {
		this.scrollbarHighlightFill = scrollbarHighlightFill;
	}

	/*
	 * Caret settings
	 */

	public ClearColor getCaretFill() {
		return caretFill;
	}

	public void setCaretFill(ClearColor caretFill) {
		this.caretFill = caretFill;
	}
	
	/*
	 * 
	 * Line Number settings
	 * 
	 */

	public boolean isLineNumbersEnabled() {
		return lineNumbersEnabled;
	}

	public void setLineNumbersEnabled(boolean lineNumbersEnabled) {
		this.lineNumbersEnabled = lineNumbersEnabled;
	}

	public Font getLineNumberFont() {
		return lineNumberFont;
	}

	public void setLineNumberFont(Font lineNumberFont) {
		this.lineNumberFont = lineNumberFont;
	}

	public float getLineNumberFontSize() {
		return lineNumberFontSize;
	}

	public void setLineNumberFontSize(float lineNumberFontSize) {
		this.lineNumberFontSize = lineNumberFontSize;
	}

	public ClearColor getLineNumberFill() {
		return lineNumberFill;
	}

	public void setLineNumberFill(ClearColor lineNumberFill) {
		this.lineNumberFill = lineNumberFill;
	}

	public FontStyle getLineNumberFontStyle() {
		return lineNumberFontStyle;
	}

	public void setLineNumberFontStyle(FontStyle lineNumberFontStyle) {
		this.lineNumberFontStyle = lineNumberFontStyle;
	}

	@Override
	public void dispose() {
	}
	
	@SuppressWarnings("unused")
	private void drawParagraphOld(NanoVGContext context, float x, float y, float width, float height) {
		long vg = context.get();
		NVGColor fill = this.fill.callocNVG();
		
		/*
		 * Text calculations
		 */
		
		int textAlignment = Font.DEFAULT_TEXT_ALIGNMENT;
		
		font.configureNVG(context, fontSize, fontStyle);
		font.split(context, lines, text, width, fontSize, textAlignment, fontStyle);
		scrollIncrement = (1f / lines.size());
		
		float fontHeight = font.getHeight(context, fontSize, textAlignment, fontStyle);
		float renderAreaHeight = (height / fontHeight) * fontHeight;
		int indicesVisible = (int) (renderAreaHeight / fontHeight);
		
		int startIndex = (int) ((lines.size() - indicesVisible) * scroll);
		
		if (startIndex < 0) {
			startIndex = 0;
		}
		
		int endIndex = (int) (startIndex + indicesVisible);
		
		if (endIndex > lines.size()) {
			endIndex = lines.size();
		}
		
		/*
		 * Rendering
		 */

		//System.err.println(fontHeight + " " + renderAreaHeight + " " + indicesVisible + " | " + startIndex + " " + endIndex + " " + lines.size());
		
		nvgBeginPath(vg);
		
		for (int i = startIndex; i < endIndex; i++) {
			float rY = y + (fontHeight * (i - startIndex));
			
			nvgFillColor(vg, fill);
			nvgText(vg, x, rY, lines.get(i));
		}
		
		fill.free();
		
		nvgClosePath(vg);
	}
}
