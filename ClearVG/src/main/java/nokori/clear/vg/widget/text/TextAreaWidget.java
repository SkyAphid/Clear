package nokori.clear.vg.widget.text;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.ClearStaticResources;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.transition.FillTransition;
import nokori.clear.vg.transition.TransitionImpl;
import nokori.clear.vg.util.NanoVGScaler;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetUtils;
import nokori.clear.windows.Cursor;
import nokori.clear.windows.Window;
import nokori.clear.windows.event.*;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;

import java.util.ArrayList;

import static org.lwjgl.nanovg.NanoVG.*;

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
public class TextAreaWidget extends Widget {
	
	Vector2f tempVec = new Vector2f();
	private boolean resetCursor = false;

	/*
	 * Text rendering
	 */
	
	public static final int TEXT_AREA_ALIGNMENT = Font.DEFAULT_TEXT_ALIGNMENT;
	
	//We store the text in a StringBuilder to make editing perform better. toString() is called when the actual string is needed, e.g. for splitting.
	private StringBuilder textBuilder;
	
	private boolean wordWrappingEnabled = true;
	
	//The text is then cut up into lines by font.split() for the text content handler to render. 
	//refreshLines can be set to true to cause font.split to be called again so that the current arraylist is recycled.
	private ArrayList<String> lines = null;
	private boolean refreshLines = false;
	
	private boolean lineSplitOverrideEnabled = false;
	private float lineSplitOverrideWidth;
	
	//TextAreaContentHandler handles formatting and rendering of the lines created above.
	private TextAreaContentHandler textContentHandler;
	
	private TextAreaInputSettings inputSettings;
	private TextAreaContentInputHandler textContentInputHandler;
	
	/*
	 * Rendering data
	 */
	
	private ClearColor backgroundFill = null;
	
	private ClearColor underlineFill = null;
	private int underlineThickness = 1;
	private float underlineYPadding = 2f;
	
	private int firstLineInView = 0;
	
	private float renderAreaHeight, stringHeight;
	private float scissorX = 0f, scissorY = 0f;
	
	private float cullOffset = 0f;
	private float textContentX = -1f, textContentY = -1f, textContentW = -1f, textContentH = -1f;
	private float stringWidth = 0f;
	
	/*
	 * Font Settings
	 */
	
	private Font font;
	private float fontSize;
	private FontStyle defaultFontStyle = FontStyle.REGULAR;
	private float fontHeight = 0f;
	
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
	
	private ClearColor scrollbarBackgroundFill = ClearColor.LIGHT_GRAY;
	private ClearColor scrollbarFill = ClearColor.DARK_GRAY;
	private ClearColor scrollbarHighlightFill = ClearColor.CORAL;

	//Vertical Scrollbar
	public static final float DEFAULT_RIGHT_SCROLLBAR_LEFT_PADDING = 20;
	private float verticalScrollbarLeftPadding = DEFAULT_RIGHT_SCROLLBAR_LEFT_PADDING;
	
	private float verticalScroll = 0.0f;
	private float verticalScrollIncrement = 0.01f;
	private TransitionImpl verticalScrollTransition = null;
	
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
	public static final float DEFAULT_BOTTOM_SCROLLBAR_TOP_PADDING = 10;
	private float horizontalScrollbarTopPadding = DEFAULT_BOTTOM_SCROLLBAR_TOP_PADDING;
	
	private float horizontalScroll = 0.0f;
	
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
	
	EditingEndedCallback editingEndedCallback = null;
	
	public TextAreaWidget(ClearColor fill, String text, Font font, float fontSize) {
		this(0f, 0f, fill, text, font, fontSize);
	}
	
	public TextAreaWidget(float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		this(0, 0, width, height, new NanoVGScaler(), fill, text, font, fontSize);
	}

	public TextAreaWidget(float x, float y, float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		this(x, y, width, height, new NanoVGScaler(), fill, text, font, fontSize);
	}
	
	public TextAreaWidget(float x, float y, float width, float height, NanoVGScaler scaler, ClearColor fill, String text, Font font, float fontSize) {
		super(x, y, width, height);
		this.defaultTextFill = fill;
		this.font = font;
		this.fontSize = fontSize;
		
		setScaler(scaler);
		
		lineSplitOverrideWidth = width;
		
		lineNumberFont = font;
		
		textContentHandler = new TextAreaContentHandler(this);
		
		inputSettings =  new TextAreaInputSettings(this);
		textContentInputHandler = new DefaultTextAreaContentInputHandler(this, textContentHandler);
		
		setText(text);
	}

	@Override
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		//System.out.println(ClearStaticResources.getFocusedWidget());
	}

	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		float x = getClippedX();
		float y = getClippedY();
		float width = getWidth();
		float height = getHeight();
		
		String text = textBuilder.toString();

		/*
		 * 
		 * 
		 * Text calculations
		 * 
		 * 
		 */
		
		font.configureNVG(context, fontSize, TEXT_AREA_ALIGNMENT, defaultFontStyle);
		fontHeight = font.getHeight(context);
		
		/*
		 * Line number calculations
		 */
		
		//If this is the first time initialization, we'll just use 9999 as the estimated max lines. If we go over it in the future, it'll auto-correct.
		int defaultMaxLines = 99_999;
		int maxLines = (lines != null ? Math.max(lines.size(), defaultMaxLines) : defaultMaxLines);
		float maxLineNumberWidth = lineNumberFont.getTextBounds(context, tempVec, Integer.toString(maxLines)).x() + lineNumberLeftPadding;
		float lineNumberCompleteWidth = lineNumbersEnabled ? lineNumberLeftPadding + maxLineNumberWidth + lineNumberRightPadding : 0f;
		
		/*
		 * Text Content Bounding
		 * 
		 * The bounding area of the text content (used for input and scissoring)
		 */
		
		textContentX = x + lineNumberCompleteWidth;
		textContentY = y;
		
		textContentW = width - ((textContentX + width) - (x + width));
		
		if (inputSettings.isVerticalScrollbarEnabled()) {
			textContentW -= (scrollbarThickness + verticalScrollbarLeftPadding);
		}
		
		textContentH = height;
		
		if (inputSettings.isHorizontalScrollbarEnabled() && !wordWrappingEnabled) {
			textContentH -= (scrollbarThickness + horizontalScrollbarTopPadding);
		}
		
		if (underlineFill != null) {
			textContentH += underlineYPadding + underlineThickness;
		}
		
		/*
		 * 
		 * Line split and text content area calculations
		 * 
		 */
		
		if (lines == null || refreshLines) {
			calculateLineSplits(context, text);
			refreshLines = false;
		}
		
		/*
		 * 
		 * 
		 * Rendering
		 * 
		 * 
		 */
		
		renderAreaHeight = (textContentH / fontHeight) * fontHeight;
		calculateStringHeight(context);
		
		/*
		 * 
		 * Begin path
		 * 
		 */
		
		long vg = context.get();
		
		if (backgroundFill != null) {
			WidgetUtils.nvgRect(vg, backgroundFill, x, y, width, height);
		}

		nvgSave(vg);
		nvgBeginPath(vg);
		
		scissorY = -Math.max((getMaxScissorY() * verticalScroll), 0f);
		scissorX = -(getMaxScissorX() * horizontalScroll);
		
		nvgScissor(vg, textContentX, textContentY, textContentW, textContentH);
		nvgTranslate(vg, scissorX, scissorY);
		
		/*
		 * 
		 * 
		 * Begin Rendering
		 * 
		 * 
		 */
		
		int totalCharacters = 0;
		firstLineInView = -1;
		cullOffset = fontHeight * 5;
		
		textContentHandler.beginFrame();

		textContentHandler.renderHighlight(vg, textContentX, textContentW, fontHeight);
		
		resetTextRenderConfiguration(context);

		/*
		 * Render text
		 */
		for (int i = 0; i < lines.size(); i++) {
			//Calculate the renderY for this line
			float rY = getLineRenderY(i);
			
			//Culls text that's outside of the scissoring range. We include a bit of padding. 
			//You can test the culling by commenting out the nvgScissor call above.
			if (isLineCulled(rY, height)) {
				totalCharacters += lines.get(i).length();
				continue;
			}
			
			//Sets the lines in view
			if (firstLineInView == -1) {
				firstLineInView = i;
			}
			
			//Draw the text
			totalCharacters += textContentHandler.renderLine(context, text.length(), i, lines.get(i), totalCharacters, textContentX, rY, scissorY, fontHeight);
		}
		
		nvgClosePath(vg);
		nvgRestore(vg);
		
		/*
		 * Render Line numbers
		 */
		
		nvgSave(vg);	
		nvgBeginPath(vg);
		
		nvgScissor(vg, x, y, textContentW, textContentH);
		nvgTranslate(vg, 0f, scissorY);
		
		for (int i = 0; i < lines.size(); i++) {
			//Calculate the renderY for this line
			float rY = getLineRenderY(i);
			
			//Culls text that's outside of the scissoring range. We include a bit of padding. 
			//You can test the culling by commenting out the nvgScissor call above.
			if (isLineCulled(rY, height)) {
				continue;
			}

			//Draw line number if applicable
			renderLineNumber(context, x, rY, lineNumberCompleteWidth, fontHeight, i);
		}
		
		textContentHandler.endOfRenderingCallback();
		
		nvgClosePath(vg);
		nvgRestore(vg);

		/*
		 * 
		 * Scrollbar Calculations & Rendering
		 * 
		 */

		//Vertical scrollbar
		//Scroll increment is adjusted based on the length of the text content (higher increments for more text, lower increments for less text)
		verticalScrollIncrement = (5f / lines.size());

		if (inputSettings.isVerticalScrollbarEnabled()) {
			verticalScrollbarActive = (inputSettings.isVerticalScrollbarEnabled() && stringHeight > height);
			renderVerticalScrollbar(context, x, y, width, height, stringHeight/5f);
		} else {
			verticalScrollbarActive = false;
		}
		
		//Horizontal scrollbar
		if (inputSettings.isHorizontalScrollbarEnabled()) {
			horizontalScrollbarActive = (!wordWrappingEnabled && stringWidth > textContentW);
			renderHorizontalScrollbar(context, x + lineNumberCompleteWidth, y, width - lineNumberCompleteWidth, height, stringWidth);
		} else {
			horizontalScrollbarActive = false;
		}
	}
	
	/**
	 * This function splits the content text into an arraylist containing individual lines, allowing us to render the text content piece by piece.
	 * 
	 * @param context
	 * @param text
	 */
	public void calculateLineSplits(NanoVGContext context, String text) {
		float lineSplitW = (wordWrappingEnabled ? (lineSplitOverrideEnabled ? lineSplitOverrideWidth : textContentW) : Float.MAX_VALUE);
		
		font.split(context, lines = new ArrayList<>(), text, lineSplitW, fontSize, TEXT_AREA_ALIGNMENT, defaultFontStyle);
		
		//Don't allow the lines array to be empty.
		if (lines.isEmpty()) {
			lines.add("");
		}

		stringWidth = textContentHandler.calculateMaxAdvance(context, textContentW, font, lines);
	}
	
	private float getLineRenderY(int lineIndex) {
		return textContentY + (fontHeight * lineIndex);
	}
	
	private boolean isLineCulled(float renderY, float height) {
		float clipY = getClippedY();
		float cullY = renderY + scissorY;

		if (cullY < (clipY - cullOffset) || cullY > clipY + height + cullOffset) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Used by TextContentRenderer to set this TextArea back to the user's defined parameters in-between TextRenderCommands.
	 */
	public void resetTextRenderConfiguration(NanoVGContext context) {
		font.configureNVG(context, fontSize, TEXT_AREA_ALIGNMENT, defaultFontStyle);
		
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
		
		if (lineNumberBackgroundFill != null && lineNumberBackgroundFill.getAlpha() > 0f) {
			lineNumberBackgroundFill.tallocNVG(bgFill -> {
				nvgBeginPath(vg);
				nvgFillColor(vg, bgFill);
				nvgRect(vg, x, y, bgWidth, bgHeight);
				nvgFill(vg);
				nvgClosePath(vg);
			});
		}
		
		if (lineNumberFill != null && lineNumberFill.getAlpha() > 0f) {
			lineNumberFill.tallocNVG(fill -> {
				lineNumberFont.configureNVG(context, fontSize, TEXT_AREA_ALIGNMENT, lineNumberFontStyle);
				nvgFillColor(vg, fill);
				nvgText(vg, x + lineNumberLeftPadding, y, Integer.toString(line));
			});
		}
		
		nvgRestore(vg);
	}
	
	private void renderVerticalScrollbar(NanoVGContext context, float x, float y, float width, float height, float stringHeight) {
		
		/*
		 * 
		 * Calculations
		 * 
		 */
		
		float scrollbarBackgroundHeight = (wordWrappingEnabled ? height : height - scrollbarThickness);
		
		verticalScrollbarDefaultHeight = getHeight()/2;
		float scrollbarMaxHeight = getHeight() * 0.5f;
		float scrollbarMinHeight = Math.min(VERTICAL_SCROLLBAR_MIN_HEIGHT, verticalScrollbarDefaultHeight);
		
		verticalScrollbarHeight = WidgetUtils.clamp(verticalScrollbarDefaultHeight * (scrollbarBackgroundHeight / stringHeight), scrollbarMinHeight, scrollbarMaxHeight);
		
		verticalScrollbarX = (x + width) - scrollbarThickness;
		verticalScrollbarY = (y + ((scrollbarBackgroundHeight - verticalScrollbarHeight) * verticalScroll));
		
		/*
		 * 
		 * Colors
		 * 
		 */
		
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
		nvgRoundedRect(vg, verticalScrollbarX, y, scrollbarThickness, scrollbarBackgroundHeight, scrollbarCornerRadius);
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
	
	private void renderHorizontalScrollbar(NanoVGContext context, float x, float y, float width, float height, float stringWidth) {
		
		if (wordWrappingEnabled) return;
		
		/*
		 * 
		 * Calculations
		 * 
		 */
		
		float scrollbarBackgroundWidth = width - scrollbarThickness;
		
		horizontalScrollbarDefaultWidth = width/2;
		float scrollbarMaxWidth = width * 0.75f;
		float scrollbarMinWidth = Math.min(HORIZONTAL_SCROLLBAR_MIN_WIDTH, horizontalScrollbarDefaultWidth);
		
		horizontalScrollbarWidth = WidgetUtils.clamp(horizontalScrollbarDefaultWidth * (scrollbarBackgroundWidth / stringWidth), scrollbarMinWidth, scrollbarMaxWidth);
		
		horizontalScrollbarX = (x + ((scrollbarBackgroundWidth - horizontalScrollbarWidth) * horizontalScroll));
		horizontalScrollbarY = (y + height) - scrollbarThickness;
		
		/*
		 * 
		 * Colors
		 * 
		 */
		
		//Set the selected fill based on if the mouse is hovering or selecting the scrollbar
		ClearColor currentFill = (horizontalScrollbarHovering || horizontalScrollbarSelected) ? scrollbarHighlightFill : scrollbarFill;
		
		//Brighten the select color if the scrollbar is selected
		if (horizontalScrollbarSelected) {
			currentFill = currentFill.multiply(0.9f);
		}
		
		//Transitions the scrollbar color smoothly to the currently selected fill color
		if (!horizontalScrollbarRenderFill.rgbMatches(currentFill) && horizontalScrollbarFillTransition == null) {
			horizontalScrollbarFillTransition = new FillTransition(200, horizontalScrollbarRenderFill, currentFill);
			horizontalScrollbarFillTransition.play();
			horizontalScrollbarFillTransition.setOnCompleted(t -> {
				horizontalScrollbarFillTransition = null;
			});
		}
		
		/*
		 * 
		 * Rendering
		 * 
		 */
		
		long vg = context.get();
		
		NVGColor scrollbarBackgroundFill = this.scrollbarBackgroundFill.callocNVG();
		NVGColor scrollbarFill = horizontalScrollbarRenderFill.callocNVG();
		
		//scrollbar background
		nvgBeginPath(vg);
		nvgRoundedRect(vg, x, horizontalScrollbarY, scrollbarBackgroundWidth, scrollbarThickness, scrollbarCornerRadius);
		nvgFillColor(vg, scrollbarBackgroundFill);
		nvgFill(vg);
		nvgClosePath(vg);
		
		if (horizontalScrollbarActive) {
			//scrollbar
			nvgBeginPath(vg);
			nvgRoundedRect(vg, horizontalScrollbarX, horizontalScrollbarY, horizontalScrollbarWidth, scrollbarThickness, scrollbarCornerRadius); 
			nvgFillColor(vg, scrollbarFill);
			nvgFill(vg);
			nvgClosePath(vg);
		}
		
		scrollbarBackgroundFill.free();
		scrollbarFill.free();
	}
	
	/**
	 * Requests that this widget re-splice the text builder (effectively refreshing the text area with the latest text). 
	 * Make sure that this is only called after <i><b>all</b></i> editing operations are completed - lest you get strange caret positioning bugs.
	 * <br><br>
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
		textContentInputHandler.charEvent(window, event);
		super.charEvent(window, event);
	}
	
	@Override
	public void keyEvent(Window window, KeyEvent event) {
		textContentInputHandler.keyEvent(window, event);
		super.keyEvent(window, event);
	}
	
	@Override
	public void mouseButtonEvent(Window window, MouseButtonEvent event) {
		verticalScrollbarMouseButtonEvent(window, event);
		horizontalScrollbarMouseButtonEvent(window, event);
			
		textContentInputHandler.mouseButtonEvent(window, event);
		
		super.mouseButtonEvent(window, event);
	}
	
	private void verticalScrollbarMouseButtonEvent(Window window, MouseButtonEvent event) {
		if (!verticalScrollbarActive) return;
		
		//Set scrollbar to selected if hovering/clicked or if the mouse button is held down and it's already selected
		if ((verticalScrollbarHovering || verticalScrollbarSelected) && event.isPressed() && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			verticalScrollbarSelected = true;
			ClearStaticResources.setFocusedWidget(this);
		} else if (verticalScrollbarSelected) {
			verticalScrollbarSelected = false;
			
			if (!textContentHandler.isCaretActive()) {
				ClearStaticResources.clearFocusIfApplicable(this);
			}
		}
	}
	
	private void horizontalScrollbarMouseButtonEvent(Window window, MouseButtonEvent event) {
		if (!horizontalScrollbarActive) return;
		
		//Set scrollbar to selected if hovering/clicked or if the mouse button is held down and it's already selected
		if ((horizontalScrollbarHovering || horizontalScrollbarSelected) && event.isPressed() && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			horizontalScrollbarSelected = true;
			ClearStaticResources.setFocusedWidget(this);
		} else if (horizontalScrollbarSelected) {
			horizontalScrollbarSelected = false;
			
			if (!textContentHandler.isCaretActive()) {
				ClearStaticResources.clearFocusIfApplicable(this);
			}
		}
	}
	
	@Override
	public void mouseMotionEvent(Window window, MouseMotionEvent event) {
		resetCursorIfApplicable(window);
		
		//I-Beam for when the mouse is hovering the text content
		if (ClearStaticResources.isFocusedOrCanFocus(this) && inputSettings.isCaretEnabled() 
				&& WidgetUtils.mouseWithinRectangle(this, window, textContentX, textContentY, textContentW, textContentH)) {
			
			applyCursor(window, Cursor.Type.I_BEAM);
		}
		
		//Scrollbars
		verticalScrollbarMouseMotionEvent(window, event);
		horizontalScrollbarMouseMotionEvent(window, event);
		
		//Text content motion events
		textContentInputHandler.mouseMotionEvent(window, event);
		
		super.mouseMotionEvent(window, event);
	}

	private void verticalScrollbarMouseMotionEvent(Window window, MouseMotionEvent event) {
		if (!verticalScrollbarActive) return;
		
		//Is mouse hovering scrollbar?
		verticalScrollbarHovering = (verticalScrollbarActive 
				&& inputSettings.isVerticalScrollbarEnabled()
				&& WidgetUtils.mouseWithinRectangle(this, window, verticalScrollbarX, verticalScrollbarY, scrollbarThickness, verticalScrollbarHeight));
		
		if (verticalScrollbarHovering) {
			applyCursor(window, Cursor.Type.HAND);
		}
		
		//The scrollbar value follows mouse. 
		//We subtract the mouse Y from the widget render Y and then divide that by the height of the widget to get a normalized value we can use for the scroller.
		if (verticalScrollbarSelected) {
			float relativeMouseY = (float) (event.getScaledMouseY(scaler.getScale()) - textContentY);
			float mouseYMult = WidgetUtils.clamp((relativeMouseY  / textContentH), 0f, 1f);
			setVerticalScroll(mouseYMult);
		}
	}
	
	private void horizontalScrollbarMouseMotionEvent(Window window, MouseMotionEvent event) {
		if (!horizontalScrollbarActive) return;
		
		//Is mouse hovering scrollbar?
		horizontalScrollbarHovering = (horizontalScrollbarActive 
				&& inputSettings.isHorizontalScrollbarEnabled()
				&& WidgetUtils.mouseWithinRectangle(this, window, horizontalScrollbarX, horizontalScrollbarY, horizontalScrollbarWidth, scrollbarThickness));
		
		if (horizontalScrollbarHovering) {
			applyCursor(window, Cursor.Type.HAND);
		}
		
		//The scrollbar value follows mouse. 
		//We subtract the mouse Y from the widget render Y and then divide that by the height of the widget to get a normalized value we can use for the scroller.
		if (horizontalScrollbarSelected) {
			float relativeMouseX = (float) (event.getScaledMouseX(scaler.getScale()) - textContentX);
			float mouseXMult = WidgetUtils.clamp((relativeMouseX  / textContentW), 0f, 1f);
			setHorizontalScroll(mouseXMult);
		}
	}
	
	@Override
	public void mouseScrollEvent(Window window, MouseScrollEvent event) {
		verticalScrollbarMouseScrollEvent(window, event);
		super.mouseScrollEvent(window, event);
	}
	
	private void verticalScrollbarMouseScrollEvent(Window window, MouseScrollEvent event) {
		if (!isMouseIntersectingThisWidget(window) || !verticalScrollbarActive) {
			return;
		}
		
		double dx = -event.getYOffset();

		float start = verticalScroll;
		float end = (float) (verticalScroll + (dx * verticalScrollIncrement));
		
		if (verticalScrollTransition != null) {
			verticalScrollTransition.stop();
		}
		
		verticalScrollTransition = new TransitionImpl(100, start, end);
		
		verticalScrollTransition.setProgressCallback(v -> {
			setVerticalScroll(WidgetUtils.clamp(v, 0f, 1f));
		});
		
		verticalScrollTransition.play();
	}
	
	private void applyCursor(Window window, Cursor.Type type) {
		ClearStaticResources.getCursor(type).apply(window);
		resetCursor = true;
	}
	
	private void resetCursorIfApplicable(Window window) {
		if (resetCursor) {
			ClearStaticResources.getCursor(Cursor.Type.ARROW).apply(window);
			resetCursor = false;
		}
	}
	
	/*
	 * 
	 * Text Content
	 * 
	 */
	
	public TextAreaContentHandler getTextContentHandler() {
		return textContentHandler;
	}

	public void endEditing() {
		textContentHandler.endEditing();
	}
	
	/**
	 * A StringBuilder is stored in this class instead of a basic String for editing/performance purposes. It's modified frequently by the various mechanics of this class. 
	 * This function will allow you to access it, but be careful in doing so.
	 * 
	 * <br><br><b>WARNING: </b> This is the raw rendering text. If you save this text to a file, any codes or escape sequences used for rendering will also be saved. 
	 * Use the <code>getText()</code> function instead if you wish to get the uncoded text.
	 * 
	 * @return the StringBuilder containing the text for this TextAreaWidget
	 */
	public StringBuilder getTextBuilder() {
		return textBuilder;
	}
	
	/**
	 * This function uses the <code>getProcessedText()</code> function in the TextContentHandler of this TextAreaWidget to retrieve the text content of this widget, allowing you to 
	 * access a text end-result in this TextAreaWidget.
	 * 
	 * @return the processed raw text content of this TextAreaWidget
	 */
	public String getText() {
		return textContentHandler.getProcessedText(getTextBuilder().toString());
	}
	
	public void setTextBuilder(StringBuilder textBuilder) {
		this.textBuilder = textBuilder;
		textContentHandler.refresh();
		requestRefresh();
	}
	
	public void setText(String text) {
		setTextBuilder(new StringBuilder(text));
	}

	/**
	 * @return the text content of this TextAreaWidget in its split arraylist form.
	 */
	public ArrayList<String> getLines() {
		return lines;
	}

	/**
	 * Sets the dimensions for the line split override.
	 * 
	 * @param lineSplitAreaWidth
	 * @param lineSplitAreaHeight
	 * @see setLineSplitOverrideEnabled()
	 * @see isLineSplitOverrideEnabled()
	 */
	public void setLineSplitOverrideWidth(float lineSplitOverrideWidth) {
		this.lineSplitOverrideWidth = lineSplitOverrideWidth;
	}
	
	/**
	 * Toggles the line split override. If true, the dimensions that the text is split with is overriden by the configured values. If false, the text is split 
	 * by using the widget's own width and height.
	 * 
	 * @param lineSplitOverrideEnabled
	 */
	public void setLineSplitOverrideEnabled(boolean lineSplitOverrideEnabled) {
		this.lineSplitOverrideEnabled = lineSplitOverrideEnabled;
	}
	
	/**
	 * @return true is the line split override is enabled, meaning that the text of this widget will be split using separate dimensions from the widget's actual size.
	 */
	public boolean isLineSplitOverrideEnabled() {
		return lineSplitOverrideEnabled;
	}
	
	public boolean isFinalLine(int lineNumber) {
		return (lineNumber + 1 >= lines.size());
	}
	
	public float getTextContentX() {
		return textContentX;
	}

	public float getTextContentY() {
		return textContentY;
	}

	public float getTextContentW() {
		return textContentW;
	}

	public float getTextContentH() {
		return textContentH;
	}

	float getScissorX() {
		return scissorX;
	}

	float getScissorY() {
		return scissorY;
	}
	
	float getMaxScissorY() {
		return (stringHeight - renderAreaHeight);
	}
	
	float getMaxScissorX() {
		return (stringWidth - textContentW);
	}

	public float getStringWidth() {
		return stringWidth;
	}

	public float calculateStringHeight(NanoVGContext context) {
		if (lines == null) {
			System.err.println("WARNING: TextAreaWidget: calculateStringHeight(): Lines are null - this function is going to fail. Calculate the line splits first (calculateLineSplits()).");
		}
		
		stringHeight = font.getHeight(context, lines.size(), fontHeight, TEXT_AREA_ALIGNMENT, defaultFontStyle);
		return stringHeight;
	}
	
	public float getStringHeight() {
		return stringHeight;
	}

	public int getFirstLineInView() {
		return firstLineInView;
	}
	
	public int getLastLineInView() {
		int linesVisible = (int) ((float) (textContentH - cullOffset) / (float) fontHeight);
		return Math.min(lines.size(), firstLineInView + linesVisible);
	}
	
	public int getLineIndexOfCharacterIndex(int charIndex) {
		int count = 0;
		
		for (int i = 0; i < lines.size(); i++) {
			int start = count;
			int end = start + lines.get(i).length();
			
			if (charIndex >= start && charIndex <= end) {
				return i;
			}
			
			count = end;
		}
		
		return -1;
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

	public ClearColor getDefaultTextFill() {
		return defaultTextFill;
	}

	public void setDefaultTextFill(ClearColor fill) {
		this.defaultTextFill = fill;
	}
	
	public FontStyle getDefaultFontStyle() {
		return defaultFontStyle;
	}

	public void setDefaultFontStyle(FontStyle defaultFontStyle) {
		this.defaultFontStyle = defaultFontStyle;
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

	public float getFontHeight() {
		return fontHeight;
	}
	
	/*
	 * General Scrollbar settings
	 */
	
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
	
	public boolean isScrollbarSelected() {
		return (isHorizontalScrollbarSelected() || isVerticalScrollbarSelected());
	}
	
	/*
	 * Horizontal Scrollbar
	 */
	
	public float getHorizontalScrollbarTopPadding() {
		return horizontalScrollbarTopPadding;
	}

	public void setHorizontalScrollbarTopPadding(float horizontalScrollbarTopPadding) {
		this.horizontalScrollbarTopPadding = horizontalScrollbarTopPadding;
	}

	/**
	 * @return the horizontal scrollbar value of this TextAreaWidget. The value is a normalized number between 0 and 1.
	 */
	public float getHorizontalScroll() {
		return horizontalScroll;
	}

	/**
	 * Sets the horizontal scroll.
	 * 
	 * @param horizontalScroll - the normalized scroll value between 0 and 1.
	 */
	public void setHorizontalScroll(float horizontalScroll) {
		this.horizontalScroll = WidgetUtils.clamp(horizontalScroll, 0f, 1f);
	}

	public boolean isHorizontalScrollbarActive() {
		return horizontalScrollbarActive;
	}

	public boolean isHorizontalScrollbarHovering() {
		return horizontalScrollbarHovering;
	}

	public boolean isHorizontalScrollbarSelected() {
		return horizontalScrollbarSelected;
	}
	
	/*
	 * Vertical Scrollbar
	 */
	
	public float getVerticalScrollbarLeftPadding() {
		return verticalScrollbarLeftPadding;
	}

	public void setVerticalScrollbarLeftPadding(float verticalScrollbarLeftPadding) {
		this.verticalScrollbarLeftPadding = WidgetUtils.clamp(verticalScrollbarLeftPadding, 0f, 1f);
	}

	/**
	 * @return the vertical scrollbar value of this TextAreaWidget. The value is a normalized number between 0 and 1.
	 */
	public float getVerticalScroll() {
		return verticalScroll;
	}

	/**
	 * Sets the vertical scroll.
	 * 
	 * @param verticalScroll - the normalized scroll value between 0 and 1.
	 */
	public void setVerticalScroll(float verticalScroll) {
		this.verticalScroll = verticalScroll;
	}

	public boolean isVerticalScrollbarActive() {
		return verticalScrollbarActive;
	}

	public boolean isVerticalScrollbarHovering() {
		return verticalScrollbarHovering;
	}

	public boolean isVerticalScrollbarSelected() {
		return verticalScrollbarSelected;
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
	
	public EditingEndedCallback getEditingEndedCallback() {
		return editingEndedCallback;
	}

	public void setOnEditingEnded(EditingEndedCallback editingEndedCallback) {
		this.editingEndedCallback = editingEndedCallback;
	}
	
	/*
	 * 
	 * 
	 * Misc.
	 * 
	 * 
	 */
	
	/**
	 * @return the fill for an underline that's drawn beneath the text of this widget.
	 */
	public ClearColor getUnderlineFill() {
		return underlineFill;
	}

	/**
	 * Sets a fill for an underline that's drawn beneath the text of this widget. Set to null to disable the feature.
	 * @param underlineFill
	 */
	public void setUnderlineFill(ClearColor underlineFill) {
		this.underlineFill = underlineFill;
	}

	public int getUnderlineThickness() {
		return underlineThickness;
	}

	public void setUnderlineThickness(int underlineThickness) {
		this.underlineThickness = underlineThickness;
	}

	public float getUnderlineYPadding() {
		return underlineYPadding;
	}

	public void setUnderlineYPadding(float underlineYPadding) {
		this.underlineYPadding = underlineYPadding;
	}

	/**
	 * @return the fill that will encompass the background of the entire widget's bounds. Set to null to disable the feature.
	 */
	public ClearColor getBackgroundFill() {
		return backgroundFill;
	}
	
	/**
	 * Sets a fill that will encompass the background of the entire widget's bounds. Set to null to disable the feature.
	 * @param backgroundFill
	 */
	public void setBackgroundFill(ClearColor backgroundFill) {
		this.backgroundFill = backgroundFill;
	}
	
	@Override
	public void dispose() {
	}

}
