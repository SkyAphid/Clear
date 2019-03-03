package nokori.clear.vg.widget;

import static org.lwjgl.nanovg.NanoVG.*;
import java.util.ArrayList;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.ClearStaticResources;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.text_rendering.TextContentRenderer;
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
	
	private Vector2f tempVec = new Vector2f();

	/*
	 * Text rendering
	 */
	
	private static final int TEXT_AREA_ALIGNMENT = Font.DEFAULT_TEXT_ALIGNMENT;
	
	private String text;
	private ArrayList<String> lines = new ArrayList<>();
	private TextContentRenderer textContentRenderer;
	
	private Font font;
	private float fontSize;
	private FontStyle fontStyle = FontStyle.REGULAR;
	
	private ClearColor fill;
	private ClearColor highlightFill = ClearColor.CORAL;
	
	/*
	 * Line numbers
	 */
	
	private boolean lineNumbersEnabled = true;
	private Font lineNumberFont;
	private FontStyle lineNumberFontStyle = FontStyle.REGULAR;
	private ClearColor lineNumberBackgroundFill = ClearColor.LIGHT_GRAY.multiply(1.1f);
	private ClearColor lineNumberFill = ClearColor.DARK_GRAY.multiply(0.8f);
	private float lineNumberLeftPadding = 5f;
	private float lineNumberRightPadding = 20f;

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
	private ClearColor scrollbarHighlightFill = highlightFill;
	
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
		
		textContentRenderer = new TextContentRenderer(this);

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
		 * 
		 * 
		 * Text calculations
		 * 
		 * 
		 */
		
		font.configureNVG(context, fontSize, fontStyle);
		
		/*
		 * Scrollbar calculations
		 */
		
		float scrollbarCompleteWidth = (scrollbarLeftPadding + scrollbarWidth + scrollbarRightPadding);
		
		/*
		 * Line number calculations
		 */
		
		//If this is the first time initialization, we'll just use 9999 as the estimated max lines. If we go over it in the future, it'll auto-correct.
		int numLines = (lines != null ? lines.size() : 9999);
		float maxLineNumberWidth = lineNumberFont.getTextBounds(context, tempVec, Integer.toString(numLines)).x();
		float lineNumberCompleteWidth = lineNumbersEnabled ? lineNumberLeftPadding + maxLineNumberWidth + lineNumberRightPadding : 0f;
		
		/*
		 * Line split and text area width calculations
		 */
		
		float textAreaWidth = width - scrollbarCompleteWidth - lineNumberCompleteWidth;

		if (lines == null) {
			font.split(context, lines = new ArrayList<>(), text, textAreaWidth, fontSize, TEXT_AREA_ALIGNMENT, fontStyle);
		}
		
		//Scroll increment is adjusted based on the length of the text content (higher increments for more text, lower increments for less text)
		scrollIncrement = (5f / lines.size());
		
		float fontHeight = font.getHeight(context, fontSize, TEXT_AREA_ALIGNMENT, fontStyle);
		float renderAreaHeight = (height / fontHeight) * fontHeight;
		float stringHeight = font.getHeight(context, lines.size(), fontHeight, TEXT_AREA_ALIGNMENT, fontStyle);
		
		/*
		 * 
		 * Culling calculations
		 * 
		 */
		
		//Scissor translation Y
		float scissorY = -((stringHeight - renderAreaHeight) * scroll);

		/*
		 * Rendering
		 */
		
		//System.err.println(fontHeight + " " + renderAreaHeight + " " + indicesVisible + " | " + startIndex + " " + endIndex + " " + lines.size());
		
		long vg = context.get();
		
		//Text
		nvgBeginPath(vg);
		nvgScissor(vg, x, y, width, height);
		nvgTranslate(vg, 0, scissorY);
		
		int totalCharacters = 0;
		
		for (int i = 0; i < lines.size(); i++) {
			float rX = x + lineNumberCompleteWidth;
			float rY = y + (fontHeight * i);
			
			//Culls text that's outside of the scissoring range. We include a bit of padding. 
			//You can test the culling by commenting out the nvgScissor call above.
			float cullY = rY + scissorY;
			float cullOffset = fontHeight * 5;

			if (cullY < (y - cullOffset) || cullY > y + height + cullOffset) {
				continue;
			}
			
			//Draw line number if applicable
			renderLineNumber(context, x, rY, lineNumberCompleteWidth, fontHeight, i);
			
			//Reset render configuration for TextContentRenderer
			resetRenderConfiguration(context);
			
			//Draw the text
			totalCharacters += textContentRenderer.render(context, lines.get(i), totalCharacters, rX, rY);
		}
		
		nvgResetTransform(vg);
		nvgResetScissor(vg);
		nvgClosePath(vg);

		//Scrollbar
		renderScrollbar(context, x, y, width, height, stringHeight/5f);
	}
	
	/**
	 * Used by TextContentRenderer to set this TextArea back to the user's defined parameters in-between TextRenderCommands.
	 */
	public void resetRenderConfiguration(NanoVGContext context) {
		font.configureNVG(context, fontSize, TEXT_AREA_ALIGNMENT, fontStyle);
		
		fill.memoryStackPush(fill -> {
			nvgFillColor(context.get(), fill);
		});
	}
	
	private void renderLineNumber(NanoVGContext context, float x, float y, float lineNumberCompleteWidth, float fontHeight, int line) {
		if (!lineNumbersEnabled) return;
		
		long vg = context.get();
		
		float bgWidth = lineNumberCompleteWidth - lineNumberRightPadding;
		float bgHeight = fontHeight + 1f; //adds a teensy bit of padding to prevent minor rounding errors when scrolling. May cause issues if the background has transparency enabled.
		
		if (lineNumberBackgroundFill != ClearColor.TRANSPARENT) {
			lineNumberBackgroundFill.memoryStackPush(bgFill -> {
				nvgBeginPath(vg);
				nvgFillColor(vg, bgFill);
				nvgRect(vg, x, y, bgWidth, bgHeight);
				nvgFill(vg);
				nvgClosePath(vg);
			});
		}
		
		lineNumberFill.memoryStackPush(fill -> {
			lineNumberFont.configureNVG(context, fontSize, TEXT_AREA_ALIGNMENT, lineNumberFontStyle);
			nvgFillColor(vg, fill);
			nvgText(vg, x + lineNumberLeftPadding, y, Integer.toString(line));
		});
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
		
		//Set the selected fill based on if the mouse is hovering or selecting the scrollbar
		ClearColor currentFill = (scrollbarHovering || scrollbarSelected) ? scrollbarHighlightFill : scrollbarFill;
		
		//Brighten the select color if the scrollbar is selected
		if (scrollbarSelected) {
			currentFill = currentFill.multiply(0.9f);
		}
		
		//Transitions the scrollbar color smoothly to the currently selected fill color
		if (!scrollbarRenderFill.rgbMatches(currentFill) && scrollbarFillTransition == null) {
			scrollbarFillTransition = new FillTransition(200, scrollbarRenderFill, currentFill);
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
	
	
	/*
	 * 
	 * 
	 * Input
	 * 
	 * 
	 */
	
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
		scrollbarMouseButtonEvent(window, event);
	}
	
	private void scrollbarMouseButtonEvent(Window window, MouseButtonEvent event) {
		//Set scrollbar to selected if hovering/clicked or if the mouse button is held down and it's already selected
		if ((scrollbarHovering || scrollbarSelected) && event.isPressed() && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			scrollbarSelected = true;
		} else {
			scrollbarSelected = false;
		}
	}
	
	@Override
	public void mouseMotionEvent(Window window, MouseMotionEvent event) {
		super.mouseMotionEvent(window, event);
		scrollbarMouseMotionEvent(window, event);
	}

	private void scrollbarMouseMotionEvent(Window window, MouseMotionEvent event) {
		//Is mouse hovering scrollbar?
		boolean bScrollbarHovering = scrollbarHovering;
		scrollbarHovering = WidgetUtil.mouseWithinRectangle(window, scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight);
		
		//The scrollbar value follows mouse. 
		//We subtract the mouse Y from the widget render Y and then divide that by the height of the widget to get a normalized value we can use for the scroller.
		if (scrollbarSelected) {
			float relativeMouseY = (float) (event.getMouseY() - getRenderY());
			float mouseYMult = WidgetUtil.clamp((relativeMouseY  / getHeight()), 0f, 1f);
			scroll = mouseYMult;
		}
		
		/*
		 * Change mouse cursor when hovering scrollbar
		 */
		
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
		scrollbarMouseScrollEvent(window, event);
	}
	
	private void scrollbarMouseScrollEvent(Window window, MouseScrollEvent event) {
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
	
	public TextContentRenderer getTextContentRenderer() {
		return textContentRenderer;
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

	public ClearColor getLineNumberFill() {
		return lineNumberFill;
	}

	public void setLineNumberFill(ClearColor lineNumberFill) {
		this.lineNumberFill = lineNumberFill;
	}
	
	public ClearColor getLineNumberBackgroundFill() {
		return lineNumberBackgroundFill;
	}

	public void setLineNumberBackgroundFill(ClearColor lineNumberBackgroundFill) {
		this.lineNumberBackgroundFill = lineNumberBackgroundFill;
	}

	public FontStyle getLineNumberFontStyle() {
		return lineNumberFontStyle;
	}

	public void setLineNumberFontStyle(FontStyle lineNumberFontStyle) {
		this.lineNumberFontStyle = lineNumberFontStyle;
	}
	
	/**
	 * @return the blank area padding between start of the line numbers background x and the text rendering x
	 */
	public float getLineNumberLeftPadding() {
		return lineNumberLeftPadding;
	}

	/**
	 * @see getLineNumberLeftPadding() for more information
	 */
	public void setLineNumberLeftPadding(float lineNumberLeftPadding) {
		this.lineNumberLeftPadding = lineNumberLeftPadding;
	}

	/**
	 * @return the blank area padding between the line numbers and the content body
	 */
	public float getLineNumberRightPadding() {
		return lineNumberRightPadding;
	}

	/**
	 * @see getLineNumberRightPadding() for more information
	 */
	public void setLineNumberRightPadding(float lineNumberRightPadding) {
		this.lineNumberRightPadding = lineNumberRightPadding;
	}

	@Override
	public void dispose() {
	}
}
