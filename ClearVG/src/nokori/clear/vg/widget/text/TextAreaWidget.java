package nokori.clear.vg.widget.text;

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

/**
 * This class contains all needed functionality for creating TextAreas. All features are enabled by default. If you want to make "quick-access" versions
 * with specific features turned off and on, I recommend extending this class and having those constructors toggle the desired features as necessary.<br><br>
 * 
 * Text areas in software always tend to be highly complex systems. I've tried my best to organize Clear's text areas in a clean and easily modifiable fashion. 
 * In simple terms, as I like to keep them, Clear's text area widget is organized as the following:<br><br>
 * 
 * • <code>TextAreaWidget:</code> This is the core container for the text area and the interface for adding one to a Clear UI. All primary text data is contained in this class.<br><br>
 * 
 * • <code>TextAreaContentHandler:</code> This class handles rendering and editing the text content of this <code>TextAreaWidget.</code><br><br>
 * 
 * • <code>TextAreaContentInputHandler:</code> This class is called by the <code>TextAreaWidget</code> to handle sending input instructions to the <code>TextAreaContentHandler.</code> 
 * 	 It's an abstract class and is meant to be extended to allow for custom input configurations. A default one with settings common to most text editing engines is 
 * 	 included via <code>DefaultTextAreaContentInputHandler.</code><br><br>
 * 
 * • <code>TextAreaInputSettings:</code> This class contains some booleans for easily enabling/disabling input features of the <code>TextAreaWidget.</code> 
 *   However, whether or not the settings are actually read depends on the <code>TextAreaContentInputHandler</code> implementation.<br><br>
 *   
 * • <code>ClearEscapeSequences:</code> This class is an "under the hood" class that handles interpreting special escape sequences that can allow for special text formatting.
 *   It's not recommended that this class be tampered with, but new features can be added if necessary. Constants for all of the available escape sequences can be found in 
 *   this class.<br><br>
 *   
 * • <code>TextAreaHistory:</code> This class contains <code>TextState</code> objects that allow for undo/redoing during editing.<br><br>
 * 
 * If you need to edit or add new features to this text area implementation, the above list should help you in getting started on where to find functionality and how to edit it.
 */
public class TextAreaWidget extends Widget implements FillAttachment {
	
	private Vector2f tempVec = new Vector2f();
	private boolean resetCursor = false;

	/*
	 * Text rendering
	 */
	
	private static final int TEXT_AREA_ALIGNMENT = Font.DEFAULT_TEXT_ALIGNMENT;
	
	//We store the text in a StringBuilder to make editing perform better. toString() is called when the actual string is needed, e.g. for splitting.
	private StringBuilder textBuilder;
	
	private boolean wordWrappingEnabled = true;
	
	//The text is then cut up into lines by font.split() for the text content handler to render. 
	//refreshLines can be set to true to cause font.split to be called again so that the current arraylist is recycled.
	private ArrayList<String> lines = null;
	private boolean refreshLines = false;

	//TextAreaContentHandler handles formatting and rendering of the lines created above.
	private TextAreaContentHandler textContentHandler;
	
	private TextAreaInputSettings inputSettings = new TextAreaInputSettings();
	private TextAreaContentInputHandler textContentInputHandler;
	
	private float textContentX = -1f, textContentY = -1f, textContentW = -1f, textContentH = -1f;
	
	/*
	 * Font Settings
	 */
	
	private Font font;
	private float fontSize;
	private FontStyle fontStyle = FontStyle.REGULAR;
	
	private ClearColor defaultTextFill;
	
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
	
	//Customization
	public static final float DEFAULT_SCROLLBAR_THICKNESS = 10f;
	private float scrollbarThickness = DEFAULT_SCROLLBAR_THICKNESS;
	
	public static final float DEFAULT_SCROLLBAR_CORNER_RADIUS = 5;
	private float scrollbarCornerRadius = DEFAULT_SCROLLBAR_CORNER_RADIUS;
	
	public static final float DEFAULT_RIGHT_SCROLLBAR_LEFT_PADDING = 50;
	private float verticalScrollbarLeftPadding = DEFAULT_RIGHT_SCROLLBAR_LEFT_PADDING;
	
	public static final float DEFAULT_BOTTOM_SCROLLBAR_TOP_PADDING = 50;
	private float horizontalScrollbarTopPadding = DEFAULT_BOTTOM_SCROLLBAR_TOP_PADDING;
	
	private ClearColor scrollbarBackgroundFill = ClearColor.LIGHT_GRAY;
	private ClearColor scrollbarFill = ClearColor.DARK_GRAY;
	private ClearColor scrollbarHighlightFill = ClearColor.CORAL;

	//Vertical Scrollbar
	private float verticalScroll = 0.0f;
	private float verticalScrollIncrement = 0.01f;
	private SimpleTransition verticalScrollTransition = null;
	
	private static final float VERTICAL_SCROLLBAR_MIN_HEIGHT = 60f;
	private float verticalScrollbarDefaultHeight;
	private float verticalScrollbarHeight;
	
	private float verticalScrollbarX = -1f, verticalScrollbarY = -1f;
	
	private boolean verticalScrollbarActive = false;
	private boolean verticalScrollbarHovering = false;
	private boolean verticalScrollbarSelected = false;
	
	private ClearColor verticalScrollbarRenderFill = new ClearColor(scrollbarFill);
	private FillTransition verticalScrollbarFillTransition = null;
	
	//Horizontal Scrollbar
	private float horizontalScroll = 0.0f;
	private float horizontalScrollIncrement = 0.01f;
	private SimpleTransition horizontalScrollTransition = null;
	
	private static final float HORIZONTAL_SCROLLBAR_MIN_WIDTH = 60f;
	private float horizontalScrollbarDefaultWidth;
	private float horizontalScrollbarWidth;
	
	private float horizontalScrollbarX = -1f, horizontalScrollbarY = -1f;
	
	private boolean horizontalScrollbarActive = false;
	private boolean horizontalScrollbarHovering = false;
	private boolean horizontalScrollbarSelected = false;
	
	private ClearColor horizontalScrollbarRenderFill = new ClearColor(scrollbarFill);
	private FillTransition horizontalScrollbarFillTransition = null;
	
	/*
	 * 
	 * Editing
	 * 
	 */
	
	private ClearColor highlightFill = ClearColor.CORAL;
	
	public TextAreaWidget(float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		this(0, 0, width, height, fill, text, font, fontSize);
	}

	public TextAreaWidget(float x, float y, float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		super(x, y, width, height);
		this.defaultTextFill = fill;
		this.font = font;
		this.fontSize = fontSize;
		
		lineNumberFont = font;
		
		textContentHandler = new TextAreaContentHandler(this);
		textContentInputHandler = new DefaultTextAreaContentInputHandler(this, textContentHandler);
		
		setText(text);
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		float x = getClippedX();
		float y = getClippedY();
		float width = size.x;
		float height = size.y;
		
		String text = textBuilder.toString();

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
		
		float scrollbarCompleteWidth = (verticalScrollbarLeftPadding + scrollbarThickness);
		
		/*
		 * Line number calculations
		 */
		
		//If this is the first time initialization, we'll just use 9999 as the estimated max lines. If we go over it in the future, it'll auto-correct.
		int defaultMaxLines = 999;
		int maxLines = (lines != null ? Math.max(lines.size(), defaultMaxLines) : defaultMaxLines);
		float maxLineNumberWidth = lineNumberFont.getTextBounds(context, tempVec, Integer.toString(maxLines)).x() + lineNumberLeftPadding;
		float lineNumberCompleteWidth = lineNumbersEnabled ? lineNumberLeftPadding + maxLineNumberWidth + lineNumberRightPadding : 0f;
		
		/*
		 * Line split and text content area calculations
		 */
		
		textContentW = width - scrollbarCompleteWidth - lineNumberCompleteWidth;
		float lineSplitW = (wordWrappingEnabled ? textContentW : Float.MAX_VALUE);
		
		if (lines == null || refreshLines) {
			font.split(context, lines = new ArrayList<>(), text, lineSplitW, fontSize, TEXT_AREA_ALIGNMENT, fontStyle);
			
			if (lines.isEmpty()) {
				lines.add("");
			}
			
			refreshLines = false;
		}
		
		//Used for activating the I-Beam cursor
		textContentX = x + lineNumberCompleteWidth;
		textContentY = y;
		textContentW = width - scrollbarThickness - verticalScrollbarLeftPadding;
		textContentH = height;
		
		//Scroll increment is adjusted based on the length of the text content (higher increments for more text, lower increments for less text)
		verticalScrollIncrement = (5f / lines.size());
		
		float fontHeight = font.getHeight(context, fontSize, TEXT_AREA_ALIGNMENT, fontStyle);
		float renderAreaHeight = (height / fontHeight) * fontHeight;
		float stringHeight = font.getHeight(context, lines.size(), fontHeight, TEXT_AREA_ALIGNMENT, fontStyle);

		verticalScrollbarActive = (stringHeight > height);
		
		/*
		 * 
		 * 
		 * Rendering
		 * 
		 * 
		 */
		
		//Scissor translation Y
		float scissorY = -((stringHeight - renderAreaHeight) * verticalScroll);
		
		//System.err.println(fontHeight + " " + renderAreaHeight + " " + indicesVisible + " | " + startIndex + " " + endIndex + " " + lines.size());
		
		long vg = context.get();
		
		//Text
		nvgBeginPath(vg);
		nvgScissor(vg, x, y, textContentW, height);
		nvgTranslate(vg, 0, scissorY);
		
		int totalCharacters = 0;
		
		textContentHandler.renderHighlight(vg, textContentX, textContentW, fontHeight);
		
		resetRenderConfiguration(context);
		
		for (int i = 0; i < lines.size(); i++) {
			float rY = textContentY + (fontHeight * i);
			
			//Culls text that's outside of the scissoring range. We include a bit of padding. 
			//You can test the culling by commenting out the nvgScissor call above.
			float cullY = rY + scissorY;
			float cullOffset = fontHeight * 5;

			if (cullY < (y - cullOffset) || cullY > y + height + cullOffset) {
				totalCharacters += lines.get(i).length();
				continue;
			}
			
			//Draw line number if applicable
			renderLineNumber(context, x, rY, lineNumberCompleteWidth, fontHeight, i);
			
			//Draw the text
			boolean isFinalLine = (i+1 >= lines.size());
			totalCharacters += textContentHandler.renderLine(context, text.length(), lines.get(i), totalCharacters, textContentX, rY, scissorY, fontHeight, isFinalLine);
		}
		
		textContentHandler.endOfRenderingCallback();

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
		
		defaultTextFill.tallocNVG(fill -> {
			nvgFillColor(context.get(), fill);
		});
		
		textContentHandler.notifyTextFillChanged(defaultTextFill);
		textContentHandler.notifyTextStyleChanged(FontStyle.REGULAR);
	}
	
	private void renderLineNumber(NanoVGContext context, float x, float y, float lineNumberCompleteWidth, float fontHeight, int line) {
		if (!lineNumbersEnabled) return;
		
		long vg = context.get();
		
		nvgSave(vg);
		
		float bgWidth = lineNumberCompleteWidth - lineNumberRightPadding;
		float bgHeight = fontHeight + 1f; //adds a teensy bit of padding to prevent minor rounding errors when scrolling. May cause issues if the background has transparency enabled.
		
		if (lineNumberBackgroundFill != ClearColor.TRANSPARENT) {
			lineNumberBackgroundFill.tallocNVG(bgFill -> {
				nvgBeginPath(vg);
				nvgFillColor(vg, bgFill);
				nvgRect(vg, x, y, bgWidth, bgHeight);
				nvgFill(vg);
				nvgClosePath(vg);
			});
		}
		
		lineNumberFill.tallocNVG(fill -> {
			lineNumberFont.configureNVG(context, fontSize, TEXT_AREA_ALIGNMENT, lineNumberFontStyle);
			nvgFillColor(vg, fill);
			nvgText(vg, x + lineNumberLeftPadding, y, Integer.toString(line));
		});
		
		nvgRestore(vg);
	}
	
	private void renderScrollbar(NanoVGContext context, float x, float y, float width, float height, float stringHeight) {
		
		/*
		 * 
		 * Calculations
		 * 
		 */
		
		float scrollbarMaxHeight = getHeight() * 0.75f;
		verticalScrollbarDefaultHeight = getHeight()/2;
		verticalScrollbarHeight = WidgetUtil.clamp(verticalScrollbarDefaultHeight * (height / stringHeight), VERTICAL_SCROLLBAR_MIN_HEIGHT, scrollbarMaxHeight);
		
		verticalScrollbarX = (x + width) - scrollbarThickness;
		verticalScrollbarY = (y + ((height - verticalScrollbarHeight) * verticalScroll));
		
		//Set the selected fill based on if the mouse is hovering or selecting the scrollbar
		ClearColor currentFill = (verticalScrollbarHovering || verticalScrollbarSelected) ? scrollbarHighlightFill : scrollbarFill;
		
		//Brighten the select color if the scrollbar is selected
		if (verticalScrollbarSelected) {
			currentFill = currentFill.multiply(0.9f);
		}
		
		//Transitions the scrollbar color smoothly to the currently selected fill color
		if (!verticalScrollbarRenderFill.rgbMatches(currentFill) && verticalScrollbarFillTransition == null) {
			verticalScrollbarFillTransition = new FillTransition(200, verticalScrollbarRenderFill, currentFill);
			verticalScrollbarFillTransition.play();
			verticalScrollbarFillTransition.setOnCompleted(t -> {
				verticalScrollbarFillTransition = null;
			});
		}
		
		/*
		 * 
		 * Rendering
		 * 
		 */
		
		long vg = context.get();
		
		NVGColor scrollbarBackgroundFill = this.scrollbarBackgroundFill.callocNVG();
		NVGColor scrollbarFill = verticalScrollbarRenderFill.callocNVG();
		
		//scrollbar background
		nvgBeginPath(vg);
		nvgRoundedRect(vg, verticalScrollbarX, y, scrollbarThickness, height, scrollbarCornerRadius);
		nvgFillColor(vg, scrollbarBackgroundFill);
		nvgFill(vg);
		nvgClosePath(vg);
		
		
		if (verticalScrollbarActive) {
			//scrollbar
			nvgBeginPath(vg);
			nvgRoundedRect(vg, verticalScrollbarX, verticalScrollbarY, scrollbarThickness, verticalScrollbarHeight, scrollbarCornerRadius);
			nvgFillColor(vg, scrollbarFill);
			nvgFill(vg);
			nvgClosePath(vg);
		}
		
		scrollbarBackgroundFill.free();
		scrollbarFill.free();
	}
	
	/**
	 * Requests that this widget re-splice the text builder (effectively refreshing the text area with the latest text). 
	 * This should be called every time the text in this widget is edited or otherwise changed. Keep in mind that the update doesn't happen immediately,
	 * but rather on the next render frame - hence <code>requestRefresh()</code> and not just <code>refresh()</code>.
	 */
	public void requestRefresh() {
		refreshLines = true;
		
		//Update Auto-Formatters
		for (int i = 0; i < getNumChildren(); i++) {
			Widget widget = (Widget) getChild(i);
			
			if (widget instanceof TextAreaAutoFormatterWidget) {
				((TextAreaAutoFormatterWidget) widget).refresh();
			}
		}
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
		textContentInputHandler.charEvent(window, event);
	}
	
	@Override
	public void keyEvent(Window window, KeyEvent event) {
		super.keyEvent(window, event);
		textContentInputHandler.keyEvent(window, event);
	}
	
	@Override
	public void mouseButtonEvent(Window window, MouseButtonEvent event) {
		super.mouseButtonEvent(window, event);
		scrollbarMouseButtonEvent(window, event);
		textContentInputHandler.mouseButtonEvent(window, event);
	}
	
	private void scrollbarMouseButtonEvent(Window window, MouseButtonEvent event) {
		//Set scrollbar to selected if hovering/clicked or if the mouse button is held down and it's already selected
		if ((verticalScrollbarHovering || verticalScrollbarSelected) && event.isPressed() && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			verticalScrollbarSelected = true;
		} else {
			verticalScrollbarSelected = false;
		}
	}
	
	@Override
	public void mouseMotionEvent(Window window, MouseMotionEvent event) {
		super.mouseMotionEvent(window, event);
		
		resetCursor = true;
		
		scrollbarMouseMotionEvent(window, event);
		textContentInputHandler.mouseMotionEvent(window, event);
		
		/*
		 * Change the cursor icon 
		 */
		
		if (inputSettings.isCaretEnabled()) {
			if (WidgetUtil.mouseWithinRectangle(window, textContentX - lineNumberRightPadding, textContentY, textContentW, textContentH)) {
				ClearStaticResources.getCursor(Cursor.Type.I_BEAM).apply(window);
				resetCursor = false;
			}
		}
		
		if (resetCursor) {
			ClearStaticResources.getCursor(Cursor.Type.ARROW).apply(window);
			resetCursor = false;
		}
	}

	private void scrollbarMouseMotionEvent(Window window, MouseMotionEvent event) {
		//Is mouse hovering scrollbar?
		verticalScrollbarHovering = (verticalScrollbarActive && inputSettings.isScrollbarEnabled() && WidgetUtil.mouseWithinRectangle(window, verticalScrollbarX, verticalScrollbarY, scrollbarThickness, verticalScrollbarHeight));
		
		if (verticalScrollbarHovering) {
			ClearStaticResources.getCursor(Cursor.Type.HAND).apply(window);
			resetCursor = false;
		}
		
		//The scrollbar value follows mouse. 
		//We subtract the mouse Y from the widget render Y and then divide that by the height of the widget to get a normalized value we can use for the scroller.
		if (verticalScrollbarSelected) {
			float relativeMouseY = (float) (event.getMouseY() - getClippedY());
			float mouseYMult = WidgetUtil.clamp((relativeMouseY  / getHeight()), 0f, 1f);
			verticalScroll = mouseYMult;
		}
	}
	
	@Override
	public void mouseScrollEvent(Window window, MouseScrollEvent event) {
		super.mouseScrollEvent(window, event);
		scrollbarMouseScrollEvent(window, event);
	}
	
	private void scrollbarMouseScrollEvent(Window window, MouseScrollEvent event) {
		if (!isMouseWithinThisWidget(window)) {
			return;
		}
		
		double dx = -event.getYoffset();

		float start = verticalScroll;
		float end = (float) (verticalScroll + (dx * verticalScrollIncrement));
		
		if (verticalScrollTransition != null) {
			verticalScrollTransition.stop();
		}
		
		verticalScrollTransition = new SimpleTransition(100, start, end);
		
		verticalScrollTransition.setProgressCallback(v -> {
			verticalScroll = v;
			
			if (verticalScroll < 0f) {
				verticalScroll = 0f;
			}
			
			if (verticalScroll > 1f) {
				verticalScroll = 1f;
			}
		});
		
		verticalScrollTransition.play();
	}

	/*
	 * 
	 * Text Content
	 * 
	 */
	
	public TextAreaContentHandler getTextContentHandler() {
		return textContentHandler;
	}

	/**
	 * @return the StringBuilder containing the text for this TextAreaWidget. A StringBuilder is stored instead of a basic String for editing/performance purposes.
	 */
	public StringBuilder getTextBuilder() {
		return textBuilder;
	}
	
	public void setTextBuilder(StringBuilder textBuilder) {
		this.textBuilder = textBuilder;
		textContentHandler.refresh();
		requestRefresh();
	}
	
	public void setText(String text) {
		setTextBuilder(new StringBuilder(text));
	}

	public boolean isWordWrappingEnabled() {
		return wordWrappingEnabled;
	}

	public void setWordWrappingEnabled(boolean wordWrappingEnabled) {
		this.wordWrappingEnabled = wordWrappingEnabled;
	}	
	
	/*
	 * 
	 * 
	 * Font Settings
	 * 
	 * 
	 */

	@Override
	public ClearColor getDefaultTextFill() {
		return defaultTextFill;
	}

	public void setDefaultTextFill(ClearColor fill) {
		this.defaultTextFill = fill;
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

	/**
	 * @return the number of lines in this text area. The text split is done before rendering, so in a case where the lines list hasn't been initialized yet, -1 will be returned.
	 */
	public int getNumLines() {
		return (lines != null ? lines.size() : -1);
	}
	
	/*
	 * Scrollbar settings
	 */
	
	public boolean isScrollbarSelected() {
		return verticalScrollbarSelected;
	}
	
	public float getScrollbarThickness() {
		return scrollbarThickness;
	}

	public void setScrollbarThickness(float scrollbarWidth) {
		this.scrollbarThickness = scrollbarWidth;
	}

	public float getScrollbarCornerRadius() {
		return scrollbarCornerRadius;
	}

	public void setScrollbarCornerRadius(float scrollbarCornerRadius) {
		this.scrollbarCornerRadius = scrollbarCornerRadius;
	}

	public float getVerticalScrollbarLeftPadding() {
		return verticalScrollbarLeftPadding;
	}

	public void setVerticalScrollbarLeftPadding(float scrollbarLeftPadding) {
		this.verticalScrollbarLeftPadding = scrollbarLeftPadding;
	}

	public float getScrollIncrement() {
		return verticalScrollIncrement;
	}

	public void setScrollIncrement(float scrollIncrement) {
		this.verticalScrollIncrement = scrollIncrement;
	}

	public SimpleTransition getScrollTransition() {
		return verticalScrollTransition;
	}

	public void setScrollTransition(SimpleTransition scrollTransition) {
		this.verticalScrollTransition = scrollTransition;
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
	 * @return the blank area padding between start of the line numbers background x and the text rendering x. 
	 * This value is also added to the right side of the background width (not padding between the text and line numbers, but internally) 
	 * so that numbers don't become congested at larger sizes.
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

	/*
	 * 
	 * Content Editing Input settings
	 * 
	 * 
	 */

	public TextAreaInputSettings getInputSettings() {
		return inputSettings;
	}

	public void setInputSettings(TextAreaInputSettings inputSettings) {
		this.inputSettings = inputSettings;
	}
	
	public TextAreaContentInputHandler getTextContentInputHandler() {
		return textContentInputHandler;
	}

	public void setTextContentInputHandler(TextAreaContentInputHandler textContentInputHandler) {
		this.textContentInputHandler = textContentInputHandler;
	}

	public ClearColor getHighlightFill() {
		return highlightFill;
	}

	public void setHighlightFill(ClearColor highlightFill) {
		this.highlightFill = highlightFill;
	}
	
	/*
	 * 
	 * 
	 * Misc.
	 * 
	 * 
	 */
	
	@Override
	public void dispose() {
	}

}
