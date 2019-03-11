package nokori.clear.vg.widget.text;

import static org.lwjgl.nanovg.NanoVG.*;

import java.util.HashMap;

import org.joml.Vector2d;
import org.joml.Vector2f;
import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.transition.SimpleTransition;
import nokori.clear.vg.widget.assembly.WidgetUtil;

/**
 * Handles the internal logic for TextAreas when it comes to rendering, formatting, and selecting text.
 */
public class TextAreaContentHandler {
	
	private TextAreaWidget widget;
	
	/*
	 * 
	 * Escape Sequence Handlers
	 * 
	 */
	
	private HashMap<String, String> escapeSequenceReplacements = TextAreaContentEscapeSequences.initDefault();
	
	//If true, a color escape sequence was found and we need to skip ahead seven charactes to accomodate a HEX value.
	boolean hexIndexOffsetRequested = false;
	
	//This is a cache of colors created by escape sequences, to try and prevent unnecessary garbage collections
	HashMap<String, ClearColor> colorCache = new HashMap<String, ClearColor>();
	
	/*
	 * Caret
	 */

	private float caretFader = 0f;
	
	private SimpleTransition caretFadeTransition = (SimpleTransition) new SimpleTransition(750, 0f, 1f, p -> {
		caretFader = p;
	});
	
	private boolean updateCaret = false;
	private Vector2d caretUpdateQueue = new Vector2d(-1, -1);

	private int caret = -1;

	/*
	 * Highlighting
	 */
	
	private boolean mousePressed = false;
	
	//The character indices of the users selection
	private int highlightIndex1 = -1;
	private int highlightIndex2 = -1;
	
	//The rendering locations of the starting and ending highlight indices
	private Vector2f highlightStartPos = new Vector2f();
	private Vector2f highlightEndPos = new Vector2f();
	
	public TextAreaContentHandler(TextAreaWidget textArea) {
		this.widget = textArea;
	}

	/**
	 * Renders the given line of text and returns the number of characters rendered. Font.split() has to be used before this will work.
	 * 
	 * @param context - NanoVG Context
	 * @param font - the font to be used
	 * @param text - the line of text to render
	 * @param startIndex - the number of characters rendered so far
	 * @param textContentX - the start render x
	 * @param lineY - the render y
	 * @return - the number of characters rendered
	 */
	public int renderLine(NanoVGContext context, int totalTextLength, String text, int startIndex, float textContentX, float lineY, float scissorY, float fontHeight) {
		long vg = context.get();

		int totalCharacters = 0;
		float advanceX = textContentX;
		
		float adjustedClickY = lineY + scissorY;
		
		for (int i = 0; i < text.length(); i++) {
			int characterIndex = startIndex + i;

			String c = checkString(context, characterIndex, Character.toString(text.charAt(i)));
			
			if (hexIndexOffsetRequested) {
				i += ClearColor.HEX_COLOR_LENGTH;
				hexIndexOffsetRequested = false;
			}

			// records data to be used for rendering the highlighted segments of the text
			highlightLogic(advanceX, lineY, characterIndex);

			// save state so that text formatting commands don't carry over into the next rendering
			nvgSave(vg);

			// render text
			float bAdvanceX = advanceX;
			advanceX = nvgText(vg, advanceX, lineY, c);

			// caret systems
			if (widget.isCaretEnabled()) {
				forEachCharCaretLogic(vg, characterIndex, bAdvanceX, lineY, adjustedClickY, (advanceX - bAdvanceX), fontHeight);
			}

			// add this character to total characters rendered
			totalCharacters++;

			// pop state
			nvgRestore(vg);
		}

		edgeCaretLogic(vg, totalTextLength, startIndex, startIndex + totalCharacters, textContentX, advanceX, lineY, adjustedClickY, fontHeight);
		
		return totalCharacters;
	}
	
	/**
	 * Cross-references the given string with the special cases hashmap. If this string is a special case, the replacement is returned instead. 
	 * Otherwise the string given is just returned.
	 */
	private String checkString(NanoVGContext context, int characterIndex, String c) {
		if (escapeSequenceReplacements.containsKey(c)) {
			return escapeSequenceReplacements.get(c);
		}
		
		if (TextAreaContentEscapeSequences.processSequence(context, widget, this, characterIndex, c)) {
			return "";
		}
		
		return c;
	}
	
	/**
	 * Notifies this content handler that setText() has been called in TextAreaWidget.
	 */
	public void refresh() {
		colorCache.clear();
	}
	
	/*
	 * 
	 * 
	 * Caret logic
	 * 
	 * 
	 */
	
	/**
	 * Handles mouse positioning of the caret within text content.
	 */
	private void forEachCharCaretLogic(long vg, int characterIndex, float x, float y, float adjustedClickY, float advanceW, float fontHeight) {
		
		//Checks if the mouse click was in the bounding of this character - if so, the caret is set to this index.
		if (updateCaret && WidgetUtil.pointWithinRectangle(caretUpdateQueue.x, caretUpdateQueue.y, x, adjustedClickY, advanceW, fontHeight)) {
			
			int bCaretPosition = caret;
			caret = characterIndex;
			refreshHighlightIndex();

			if (bCaretPosition != caret) {
				resetCaretFader();
			}
			
			updateCaret = false;
		}
		
		//Render the caret
		if (caret == characterIndex) {
			renderCaret(vg, x, y, fontHeight);
		}
	}

	/**
	 * Applies extra logic at the end of line rendering for positioning the caret on the edges of a line (e.g. at the very start of a line or the very end), 
	 * which would be somewhat difficult with just the base controls.
	 */
	private void edgeCaretLogic(long vg, int totalTextLength, int startIndex, int endIndex, float startX, float endX, float y, float adjustedClickY, float fontHeight) {
		double mX = caretUpdateQueue.x;
		double mY = caretUpdateQueue.y;
		
		if (updateCaret) {
			if (mY >= adjustedClickY && mY <= adjustedClickY + fontHeight) {
				//Places the caret at the very start of a line if the mouse is past the very left edge of the rendering area.
				if (mX < startX) {
					caret = startIndex;
					refreshHighlightIndex();
					resetCaretFader();
					updateCaret = false;
				}
				
				//Places the caret on the very end of the right side of the line if the mouse is past the very right edge of the rendering area.
				//A special case is added to put the caret past the end of the text if it's the end of the entire string.
				if (mX > endX) {
					if (endIndex < totalTextLength-1) {
						caret = endIndex-1;
					} else {
						caret = endIndex;
					}
					
					refreshHighlightIndex();
					resetCaretFader();
					updateCaret = false;
				}
			}
		}
		
		//In a special case where the caret is at the end of the entirety of the text, we render the caret at the very tail end.
		//Normally it's rendered during normal character rendering - but if the caret is outside the text - then it won't draw otherwise.
		if (caret == endIndex && endIndex == totalTextLength) {
			renderCaret(vg, endX, y, fontHeight);
		}
	}
	
	/**
	 * Renders the caret using a SimpleTransition to fade in and out smoothly.
	 */
	private void renderCaret(long vg, float x, float y, float fontHeight) {
		if (isContentHighlighted()) return;
		
		/*
		 * Caret fading logic
		 */
		
		if (widget.isHighlightingEnabled()) {
			if (caretFadeTransition.isFinished()) {
				if (caretFader == 1f) {
					caretFadeTransition.setStartAndEnd(1f, 0f);
				} else {
					caretFadeTransition.setStartAndEnd(0f, 1f);
				}
				
				caretFadeTransition.play();
			}
		}

		/*
		 * Render the caret
		 */
		
		ClearColor caretFill = widget.getCaretFill().alpha(caretFader);
		
		caretFill.tallocNVG(fill -> {
			WidgetUtil.nvgRect(vg, fill, x, y, 2.0f, fontHeight);
		});
	}
	
	/**
	 * Forces the caret alpha value to 1f and then resets the animation appropriately for when the user clicks and sets the new position (so that it's immediately visible)
	 */
	private void resetCaretFader() {
		caretFader = 1f;
		caretFadeTransition.setStartAndEnd(1f, 0f);
		caretFadeTransition.play();
	}
	
	/*
	 * 
	 * 
	 * Highlighting
	 * 
	 * 
	 */
	
	private void highlightLogic(float x, float y, int characterIndex) {
		if (characterIndex == getHighlightStartIndex()) {
			highlightStartPos.set(x, y);
		}
		
		if (characterIndex == getHighlightEndIndex()) {
			highlightEndPos.set(x, y);
		}
	}
	
	/**
	 * Renders the highlight background for text that has been selected by the user.
	 * 
	 * @param vg
	 * @param textContentX
	 * @param textContentW
	 * @param fontHeight
	 */
	public void renderHighlight(long vg, float textContentX, float textContentW, float fontHeight) {
		nvgSave(vg);
		
		if (widget.isHighlightingEnabled() && isContentHighlighted()) {
			widget.getHighlightFill().tallocNVG(fill -> {
				for (float y = highlightStartPos.y; y <= highlightEndPos.y; y += fontHeight) {
					float x = textContentX;
					float w = textContentW - 1;
					float h = fontHeight + 1;
					
					if (y == highlightStartPos.y) {
						x = highlightStartPos.x;
					}
					
					if (y == highlightEndPos.y) {
						w = (highlightEndPos.x - textContentX);
					}
					
					WidgetUtil.nvgRect(vg, fill, x, y, w, h);
				}
			});
		}
		
		nvgRestore(vg);
	}
	
	/**
	 * Updates the highlighted indices to match the caret index when it's updated
	 */
	private void refreshHighlightIndex() {
		if (highlightIndex1 == -1) {
			highlightIndex1 = caret;
		} else {
			highlightIndex2 = caret;
		}
	}
	
	/**
	 * Resets the highlighted indices (disables it)
	 */
	public void resetHighlightIndex() {
		highlightIndex1 = -1;
		highlightIndex2 = -1;
	}
	
	public int getHighlightStartIndex() {
		return Math.min(highlightIndex1, highlightIndex2);
	}
	
	public int getHighlightEndIndex() {
		return Math.max(highlightIndex1, highlightIndex2);
	}
	
	public boolean isContentHighlighted() {
		return (highlightIndex1 != -1 && highlightIndex2 != -1);
	}
	
	public boolean isContentHighlighted(int index) {
		return (isContentHighlighted() && index >= getHighlightStartIndex() && index < getHighlightEndIndex());
	}
	
	/*
	 * 
	 * 
	 * Input
	 * 
	 * 
	 */
	
	void mouseEvent(double mouseX, double mouseY) {
		mouseEvent(mouseX, mouseY, mousePressed);
	}
	
	void mouseEvent(double mouseX, double mouseY, boolean pressed) {
		if (widget.isScrollbarSelected()) return;
		
		//This toggles highlighting mode
		boolean bMousePressed = mousePressed;
		mousePressed = pressed;

		if (mousePressed) {
			//This queues up caret repositioning based on the mouse coordinates
			caretUpdateQueue.set(mouseX, mouseY);
			
			// Update the caret positioning on next render when we have the character
			// locations available
			updateCaret = true;

			// If the mouse wasn't previously pressed, reset the highlighting.
			if (!bMousePressed) {
				resetHighlightIndex();
			}
		}
	}
	
	public void insertCharacterAtCaret(String character) {
		insertCharacter(caret, character);
		caret++;
	}
	
	public void insertCharacter(int position, String character) {
		StringBuilder textBuilder = widget.getTextBuilder();
		int length = textBuilder.length();
		
		if (caret >= 0 && caret <= length) {
			textBuilder.insert(caret, character);
			widget.requestRefresh();
		}
	}
	
	public void backspaceAtCaret() {
		backspace(caret);
		caret--;
	}
	
	public void backspace(int position) {
		if (position > 0) {
			widget.getTextBuilder().deleteCharAt(position-1);
			widget.requestRefresh();
		}
	}
	
	public void moveCaretDown() {
		
	}
	
	public void moveCaretUp() {
		
	}
	
	public void moveCaretLeft() {
		if (caret > 0) {
			caret--;
		}
	}
	
	public void moveCaretRight() {
		if (caret < widget.getTextBuilder().length()) {
			caret++;
		}
	}
	
	/*
	 * 
	 * 
	 * Other getters/setters
	 * 
	 * 
	 */
	
	/**
	 * This returns a hashmap containing escape sequences and their replacements. This hashmap is used to get around limitations of NanoVG, such as not supporting \t. 
	 * It's initialized with default values from TextAreaDefaultEscapeSequenceReplacements, however it can be cleared and modified to your heart's content.
	 * <br><br>
	 * Keep in mind that TextAreaContentHandler has some escape sequences that are hardcoded. These are visible via the final static escape sequence strings.
	 */
	public HashMap<String, String> getEscapeSequenceReplacements() {
		return escapeSequenceReplacements;
	}
	
	void setCaretPosition(int caret) {
		this.caret = caret;
	}
	
	public int getCaretPosition() {
		return caret;
	}

}
