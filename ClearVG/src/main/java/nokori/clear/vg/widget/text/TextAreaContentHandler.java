package nokori.clear.vg.widget.text;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.ClearStaticResources;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.transition.TransitionImpl;
import nokori.clear.vg.widget.assembly.WidgetUtils;
import nokori.clear.windows.Window;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;

import static nokori.clear.vg.widget.text.ClearEscapeSequences.*;
import static org.lwjgl.nanovg.NanoVG.*;

/**
 * Handles the internal logic for TextAreas when it comes to rendering, formatting, and selecting text.
 */
public class TextAreaContentHandler {
	
	/*
	 * Debug switches
	 */
	
	/** Sets up the escape sequence replacement hashmap to show the names of the commands in the rendering for debugging purposes */
	private static final boolean SHOW_ESCAPE_SEQUENCES = false;
	
	/** Disables character skipping, meaning that longer escape sequences such as HEX color setters will be displayed for debugging purposes */
	private static final boolean SKIPPING_ENABLED = true;
	
	/*
	 * Core data
	 */
	
	private TextAreaWidget widget;
	private TextAreaHistory editHistory = new TextAreaHistory();
	
	/*
	 * 
	 * Escape Sequence Handlers
	 * 
	 */
	
	private HashMap<String, String> escapeSequenceReplacements = ClearEscapeSequences.initDefault(SHOW_ESCAPE_SEQUENCES);
	
	//If true, a color escape sequence was found and we need to skip ahead seven characters to accommodate a HEX value.
	int skipsRequested = 0;
	
	//This is a cache of colors created by escape sequences, to try and prevent unnecessary garbage collections
	HashMap<String, ClearColor> colorCache = new HashMap<String, ClearColor>();
	private ClearColor currentTextFill = null;
	
	private FontStyle currentTextStyle = FontStyle.REGULAR;
	
	/*
	 * Caret
	 */

	private float caretFader = 0f;
	
	private TransitionImpl caretFadeTransition = (TransitionImpl) new TransitionImpl(750, 0f, 1f, p -> {
		caretFader = p;
	});
	
	private boolean updateCaret = false;
	private Vector2f caretUpdateQueue = new Vector2f(-1, -1);
	private Vector2f caretPosition = new Vector2f(-1, -1);
	
	private boolean requestScrollToCaret = false;
	
	private int caret = -1;

	/*
	 * Highlighting
	 */

	//The character indices of the users selection
	private int highlightIndex1 = -1;
	private int highlightIndex2 = -1;
	
	//The rendering locations of the starting and ending highlight indices
	private Vector2f highlightStartPos = null;
	private Vector2f highlightEndPos = null;
	
	public TextAreaContentHandler(TextAreaWidget textArea) {
		this.widget = textArea;
	}

	/**
	 * Configures the TextAreaContentHandler for rendering on a new frame. This function is used to reset various features back to their defaults so that data from the last frame 
	 * doesn't carry over and cause rendering problems.
	 */
	public void beginFrame() {
		skipsRequested = 0;
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
	public int renderLine(NanoVGContext context, int totalTextLength, int lineNumber, String text, int startIndex, float textContentX, float lineY, float scissorY, float fontHeight) {
		long vg = context.get();

		int totalCharacters = 0;
		float advanceX = textContentX;
		
		float adjustedClickY = lineY + scissorY;

		for (int i = 0; i < text.length(); i++) {
			int characterIndex = startIndex + i;
			
			//Render the caret
			if (caret == characterIndex) {
				renderCaret(vg, advanceX, lineY, fontHeight);
			}

			String c = checkEscapeSequences(context, characterIndex, text.charAt(i));
			
			if (!SKIPPING_ENABLED) {
				skipsRequested = 0;
			}

			if (skipsRequested == 0) {

				// save state so that text formatting commands don't carry over into the next rendering
				nvgSave(vg);
				
				// records data to be used for rendering the highlighted segments of the text
				highlightRenderLogic(vg, advanceX, lineY, characterIndex);
	
				// render text
				float bAdvanceX = advanceX;
				advanceX = nvgText(vg, advanceX, lineY, c);
				
				// caret systems
				if (widget.getInputSettings().isCaretEnabled()) {
					forEachCharCaretLogic(vg, characterIndex, lineNumber, bAdvanceX, lineY, adjustedClickY, (advanceX - bAdvanceX), fontHeight);
				}
				
				// pop state
				nvgRestore(vg);
			} else {
				skipsRequested--;
				
				//Character rendering is skipped, but caret logic is still checked.
				nvgSave(vg);
				forEachCharCaretLogic(vg, characterIndex, lineNumber, advanceX, lineY, adjustedClickY, 0, fontHeight);
				nvgRestore(vg);
			}

			// add this character to total characters rendered
			totalCharacters++;
		}
		
		/*
		 * Underline
		 */
		
		if (widget.getUnderlineFill() != null) {
			nvgSave(vg);
			WidgetUtils.nvgRect(vg, widget.getUnderlineFill(), textContentX, lineY + fontHeight + widget.getUnderlineYPadding(), widget.getWidth(), widget.getUnderlineThickness());
			nvgRestore(vg);
		}
		
		/*
		 * For moving the caret to the very start/end of this line.
		 */
		
		int endIndex = startIndex + totalCharacters;
		edgeCaretLogic(vg, totalTextLength, lineNumber, startIndex, endIndex, textContentX, advanceX, lineY, adjustedClickY, fontHeight);
		
		//In a special case where the caret is at the end of the entirety of the text, we render the caret at the very tail end.
		//Normally it's rendered during normal character rendering - but if the caret is outside the text - then it won't draw otherwise.
		if (caret == endIndex && endIndex == totalTextLength 
				&& (!widget.isFinalLine(lineNumber) && !widget.getLines().get(lineNumber + 1).isEmpty() || widget.isFinalLine(lineNumber))) {
			
			//Highlight logic for the very end of the text.
			highlightRenderLogic(vg, advanceX, lineY, caret);
			
			//Render the caret at the very end of the text.
			renderCaret(vg, advanceX, lineY, fontHeight);
		}
		
		return totalCharacters;
	}
	
	public float calculateMaxAdvance(NanoVGContext context, float width, Font font, ArrayList<String> lines) {
		//Calculate the max advance every time the text changes.
		float maxAdvance = width;
		int characterIndex = 0;
		
		for (int i = 0; i < lines.size(); i++) {
			String l = lines.get(i);
			String s = "";
			
			for (int j = 0; j < l.length(); j++) {
				s += checkEscapeSequences(context, characterIndex, l.charAt(j));
				characterIndex++;
			}
			
			float a = font.getTextBounds(context, widget.tempVec, s).x();

			if (a > maxAdvance) {
				maxAdvance = a;
			}
		}
		
		return maxAdvance;
	}
	
	/**
	 * Processes the given char and returns the modified version from the replacement hashmap if applicable.
	 * 
	 * @param context
	 * @param characterIndex
	 * @param c
	 * @return - processed char into new string or just return the input char if a replacement isn't found
	 */
	public String checkEscapeSequences(NanoVGContext context, int characterIndex, char c) {
		String sC = Character.toString(c);
			
		processSequence(context, widget, this, characterIndex, c);
		
		if (escapeSequenceReplacements.containsKey(sC)) {
			return escapeSequenceReplacements.get(sC);
		}
		
		return Character.toString(c);
	}
	
	/**
	 * To be called at the end of a rendering cycle after all lines have been rendered.
	 */
	void endOfRenderingCallback() {
		//If updateCaret is still true by the end of a rendering cycle, that means it was never successfully moved (invalid caret queue coordinates).
		//We'll turn it off so that it doesn't get deadlocked.
		//Also automatically unfocus this widget if the caret is no longer in the text bounding
		notifyCaretUpdated(false);
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
	private void forEachCharCaretLogic(long vg, int characterIndex, int lineNumber, float x, float y, float adjustedClickY, float advanceW, float fontHeight) {
		//Checks if the mouse click was in the bounding of this character - if so, the caret is set to this index.
		float clickHeight = (widget.isFinalLine(lineNumber) && (widget.isMouseWithin() || isHighlighting()) ? Float.MAX_VALUE : fontHeight);

		float clickX = caretUpdateQueue.x - widget.getScissorX();
		float clickY = caretUpdateQueue.y;
		
		if (updateCaret && WidgetUtils.pointWithinRectangle(clickX, clickY, x, adjustedClickY, advanceW, clickHeight)) {
			int bCaretPosition = caret;
			setCaretPosition(characterIndex);
			refreshHighlightIndex();
	
			if (bCaretPosition != caret) {
				resetCaretFader();
			}
			
			notifyCaretUpdated(true);
		}
	}

	/**
	 * Applies extra logic at the end of line rendering for positioning the caret on the edges of a line (e.g. at the very start of a line or the very end), 
	 * which would be somewhat difficult with just the base controls.
	 */
	private void edgeCaretLogic(long vg, int totalTextLength, int lineNumber, int startIndex, int endIndex, float startX, float endX, float y, float adjustedClickY, float fontHeight) {
		float mX = caretUpdateQueue.x;
		float mY = caretUpdateQueue.y;
		
		if (updateCaret) {
			//System.out.println(lineNumber + " Pass 1");
			
			//If this causes weird behavior in the future, change it to where it only takes the widget itself into account and remove the parent check
			boolean withinBounds = widget.intersects(mX, mY); /*(widget.getParent() != null && widget.getParent().isPointWithinThisWidget(mX, mY) || */;

			if (!withinBounds && !isHighlighting()) {
				endEditing();
				return;
			}
			
			if (mY >= adjustedClickY && mY <= adjustedClickY + fontHeight || (mY >= adjustedClickY && widget.isFinalLine(lineNumber))) {
				//Places the caret at the very start of a line if the mouse is past the very left edge of the rendering area.
				if (mX < startX) {
					//System.out.println(lineNumber + " Pass 2");
					
					setCaretPosition(startIndex);
					
					refreshHighlightIndex();
					highlightRenderLogic(vg, startX, y, startIndex);
					
					resetCaretFader();
					notifyCaretUpdated(true);
					return;
				}
				
				//Places the caret on the very end of the right side of the line if the mouse is past the very right edge of the rendering area.
				//A special case is added to put the caret past the end of the text if it's the end of the entire string.
				if (mX > endX) {
					//System.out.println(lineNumber + " Pass 3");
					
					if (endIndex < totalTextLength) {
						setCaretPosition(endIndex-1);
					} else {
						setCaretPosition(endIndex);
					}
					
					refreshHighlightIndex();
					highlightRenderLogic(vg, endX, y, endIndex);
					
					resetCaretFader();
					notifyCaretUpdated(true);
					return;
				}
			}
		}
	}
	
	/**
	 * Renders the caret using a SimpleTransition to fade in and out smoothly.
	 */
	private void renderCaret(long vg, float x, float y, float fontHeight) {
		if (isContentHighlighted()) return;
		
		caretPosition.set(x, y);
		
		nvgSave(vg);
		
		/*
		 * Caret fading logic
		 */
		
		if (widget.getInputSettings().isHighlightingEnabled()) {
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
		
		ClearColor caretFill = currentTextFill.alpha(caretFader);
		
		caretFill.tallocNVG(fill -> {
			float defaultLineThickness = 2.0f;
			
			switch(this.currentTextStyle) {
			case BOLD:
				WidgetUtils.nvgRect(vg, fill, x, y, defaultLineThickness * 2f, fontHeight);
				break;
			case ITALIC:
				float thickness = defaultLineThickness;
				float offset = 2.0f;
				
				float topX = x + offset;
				float topX2 = x + offset + thickness;
				float topY = y;
				float botY = y + fontHeight;
				float botX = x;
				float botX2 = x + thickness;
				
				WidgetUtils.nvgShape(vg, fill, topX, topY, topX2, topY, botX2, botY, botX, botY);
				break;
			case LIGHT:
				WidgetUtils.nvgRect(vg, fill, x, y, defaultLineThickness/2f, fontHeight);
				break;
			case REGULAR:
			default:
				WidgetUtils.nvgRect(vg, fill, x, y, defaultLineThickness, fontHeight);
				break;
			}
		});
		
		nvgRestore(vg);
	}
	
	/**
	 * Forces the caret alpha value to 1f and then resets the animation appropriately for when the user clicks and sets the new position (so that it's immediately visible)
	 */
	private void resetCaretFader() {
		caretFader = 1f;
		caretFadeTransition.setStartAndEnd(1f, 0f);
		caretFadeTransition.play();
	}

	void queueCaret(float x, float y) {
		caretUpdateQueue.set(x, y);
		updateCaret = true;
	}
	
	/**
	 * Sets updateCaret to false, meaning that the caret has been updated for this frame.
	 * 
	 * Also calls any other commands that require the up-to-date caret position data.
	 * 
	 * @param focusWidget - if true, this widget will be set as the focused widget. If false, this widget will be unfocused.
	 * @see <code>scrollToCaret()</code>
	 */
	private void notifyCaretUpdated(boolean focusWidget) {
		if (requestScrollToCaret) {
			scrollToCaret();
			requestScrollToCaret = false;
		}
		
		if (focusWidget) {
			ClearStaticResources.setFocusedWidget(widget);
		}

		updateCaret = false;
	}
	
	/**
	 * @return true if the caret is active (the caret position is greater than -1 and less than the text length)
	 */
	public boolean isCaretActive() {
		return (widget.getInputSettings().isEditingEnabled() && widget.getInputSettings().isCaretEnabled() && caret >= 0);
	}
	
	/**
	 * Removes the caret, resets highlighting, and unfocuses the parent widget if applicable
	 */
	public void endEditing() {
		//Call editing ended callback
		if (isCaretActive() && widget.editingEndedCallback != null) {
			widget.editingEndedCallback.process();
		}
		
		//Reset caret & highlighting
		setCaretPosition(-1);
		resetCaretFader();
		resetHighlighting();
		notifyCaretUpdated(false);
		
		//Clear focus
		ClearStaticResources.clearFocusIfApplicable(widget);
	}
	
	/*
	 * 
	 * 
	 * Highlighting
	 * 
	 * 
	 */
	
	/**
	 * Call this during character rendering. Checks if this character index matches the highlight indices. If so, record their positions. 
	 * 
	 * Additionally, tweaks the current text color if it matches the current highlight fill.
	 * 
	 * @param x
	 * @param y
	 * @param characterIndex
	 */
	private void highlightRenderLogic(long vg, float x, float y, int characterIndex) {
		
		/*
		 * Record highlight positions
		 */
		
		if (characterIndex == getHighlightStartIndex()) {
			if (highlightStartPos == null) {
				highlightStartPos = new Vector2f(x, y);
			} else {
				highlightStartPos.set(x, y);
			}
		}
		
		if (characterIndex == getHighlightEndIndex()) {
			if (highlightEndPos == null) {
				highlightEndPos = new Vector2f(x, y);
			} else {
				highlightEndPos.set(x, y);
			}
		}

		/*
		 * If this character is highlighted, tweak the color if it matches the highlight color.
		 */
		
		if (isContentHighlighted(characterIndex) && widget.getHighlightFill().rgbMatches(currentTextFill)) {
			widget.getHighlightFill().divide(2.5f).tallocNVG(fill -> {
				nvgFillColor(vg, fill);
			});
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
		if (highlightStartPos == null || highlightEndPos == null) return;
		
		nvgSave(vg);
		
		if (widget.getInputSettings().isHighlightingEnabled() && isContentHighlighted()) {
			widget.getHighlightFill().tallocNVG(fill -> {
				for (float y = highlightStartPos.y; y <= highlightEndPos.y; y += fontHeight) {
					float x = textContentX;
					float w = textContentW - 1;
					float h = fontHeight + 1;
					
					if (y == highlightStartPos.y) {
						x = highlightStartPos.x;
					}
					
					if (y == highlightEndPos.y) {
						w = (highlightEndPos.x - x);
					}
					
					WidgetUtils.nvgRect(vg, fill, x, y, w, h);
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
	 * Resets the highlighted indices and deletes the highlight rendering positions. This resets highlighting and disables it (not permanently).
	 */
	public void resetHighlighting() {
		highlightIndex1 = highlightIndex2 = -1;
		highlightStartPos = highlightEndPos = null;
	}
	
	public int getHighlightStartIndex() {
		return Math.min(highlightIndex1, highlightIndex2);
	}
	
	public int getHighlightEndIndex() {
		return Math.max(highlightIndex1, highlightIndex2);
	}
	
	public boolean isContentHighlighted() {
		return (highlightIndex1 != -1 && highlightIndex2 != -1 && (Math.abs(highlightIndex1 - highlightIndex2) > 0));
	}
	
	public boolean isContentHighlighted(int index) {
		return (isContentHighlighted() && index >= getHighlightStartIndex() && index < getHighlightEndIndex());
	}
	
	public boolean isHighlighting() {
		return (highlightIndex1 != -1 || highlightIndex2 != -1);
	}
	
	/*
	 * 
	 * 
	 * Input Commands
	 * 
	 * 
	 */

	public void deleteHighlightedContent() {
		if (isContentHighlighted()) {
			widget.getTextBuilder().delete(getHighlightStartIndex(), getHighlightEndIndex());
			setCaretPosition(getHighlightStartIndex());
			resetHighlighting();
			widget.requestRefresh();
			requestScrollToCaret();
		}
	}
	
	public void insertStringAtPosition(int position, String string) {
		StringBuilder s = widget.getTextBuilder();
		int characterLimit = widget.getInputSettings().getCharacterLimit();
		
		if (s.length() + string.length() < characterLimit) {
			s.insert(position, string);
		} else if (string.length() > 1){
			int index = string.length();
			
			while(s.length() + index > characterLimit && index > 0) {
				index--;
			}
			
			if (index > 0) {
				s.insert(position, string.substring(0, index));
			}
		}
	}
	
	public void insertCharacterAtCaret(String character) {
		editHistory.notifyEditing(widget, this);
		
		StringBuilder textBuilder = widget.getTextBuilder();
		
		if (caret >= 0 && caret <= textBuilder.length()) {
			deleteHighlightedContent();
			insertStringAtPosition(caret, character);
			offsetCaret(1);
			widget.requestRefresh();
		}
		
		requestScrollToCaret();
	}
	
	public void backspaceAtCaret() {
		offsetCaret(-backspace(caret, false), false);
		requestScrollToCaret();
		widget.requestRefresh();
	}
	
	public int backspace(int position, boolean requestRefresh) {
		StringBuilder textBuilder = widget.getTextBuilder();
		boolean manualFormattingEnabled = widget.getInputSettings().isManualFormattingEnabled();
		
		int indicesMoved = 0;
		int charIndex = position-1;
		
		while(!manualFormattingEnabled && position > 0 && isCharEscapeSequence(textBuilder, charIndex)){
			position--;
			charIndex = position-1;
			indicesMoved++;
		}
		
		if (position > 0) {
			int charsDeleted = 0;
			
			/*
			 * Backspace special cases for escape sequences
			 */
			
			if (manualFormattingEnabled) {
				Vector2i colorSequence = colorEscapeSequenceToLeft(textBuilder, charIndex);

				if (colorSequence != null) {
					int start = colorSequence.x();
					int end = colorSequence.y();

					textBuilder.delete(start, end);
					charsDeleted = (charIndex - start);
				}
			}
			
			/*
			 * Standard backspace otherwise
			 */
			
			if (charsDeleted == 0) {
				textBuilder.deleteCharAt(charIndex);
				charsDeleted = 1;
			}
			
			deleteResetEscapeSequenceAhead(textBuilder, charIndex);
			
			if (requestRefresh) {
				widget.requestRefresh();
			}
			
			return charsDeleted + indicesMoved;
		}
		
		return 0;
	}
	
	public void tabAtCaret() {
		tab(caret);
		offsetCaret(1);
		requestScrollToCaret();
	}
	
	public void tab(int position) {
		insertStringAtPosition(position, "\t");
		widget.requestRefresh();
	}
	
	public void newLineAtCaret() {
		/*
		 * Add the new line.
		 */
		
		newLine(caret);
		
		/*
		 * Set the caret to the next line index
		 */

	 	setCaretPosition(caret + 1);
		requestScrollToCaret();
	}
	
	public void newLine(int position) {
		insertStringAtPosition(position, "\n");
		widget.requestRefresh();
	}
	
	public void cutSelectionToClipboard(Window window) {
		copySelectionToClipboard(window);
		
		int start = getHighlightStartIndex();
		int end = getHighlightEndIndex();
		
		widget.getTextBuilder().delete(start, end);
		widget.requestRefresh();
		
		setCaretPosition(start);
		resetCaretFader();
		resetHighlighting();
	}
	
	public void copySelectionToClipboard(Window window) {
		int start = getHighlightStartIndex();
		int end = getHighlightEndIndex();
		
		String s = widget.getTextBuilder().substring(start, end);

		window.setClipboardString(getProcessedText(s));
	}
	
	public void pasteClipboardAtCaret(Window window) {
		offsetCaret(pasteClipboard(window, caret));
		requestScrollToCaret();
	}
	
	public int pasteClipboard(Window window, int position) {
		deleteHighlightedContent();
		
		String clipboard = window.getClipboardString();
		
		insertStringAtPosition(position, clipboard);
		widget.requestRefresh();
		
		resetCaretFader();
		resetHighlighting();
		
		return clipboard.length();
	}
	
	public void boldenSelection() {
		styleSelection(ESCAPE_SEQUENCE_BOLD);
	}
	
	public void italicizeSelection() {
		styleSelection(ESCAPE_SEQUENCE_ITALIC);
	}
	
	private void styleSelection(char escapeSequence) {
		if (!widget.getInputSettings().isManualFormattingEnabled()) {
			return;
		}
		
		if (insideSequence(escapeSequence, widget.getTextBuilder(), caret)) {
			return;
		}
		
		int start = caret;
		int end = caret;
		
		if (isContentHighlighted()) {
			start = getHighlightStartIndex();
			end = getHighlightEndIndex();
			
			highlightIndex1++;
			highlightIndex2++;
		} else {
			offsetCaret(1);
		}
		

		widget.getTextBuilder().insert(start, escapeSequence);
		widget.getTextBuilder().insert(end + 1, ESCAPE_SEQUENCE_RESET);

		widget.requestRefresh();
	}
	
	public void undo() {
		editHistory.undo(widget, this);
	}
	
	public void redo() {
		editHistory.redo(widget, this);
	}
	
	public void moveCaretLeft() {
		if (caret > 0) {
			resetHighlighting();
			resetCaretFader();
			requestScrollToCaret();
			
			/*
			 * Caret offset for color sequences
			 */
			
			Vector2i colorSequence = colorEscapeSequenceToLeft(widget.getTextBuilder(), caret);
			
			if (colorSequence != null) {
				int offset = (caret - colorSequence.x());
				
				if (offset > 0) {
					offsetCaret(-offset);
					return;
				}
			}
			
			/*
			 * Standard Behavior
			 */
			
			offsetCaret(-1);
		}
	}
	
	public void moveCaretRight() {
		StringBuilder s = widget.getTextBuilder();
		
		if (caret < s.length()) {
			resetHighlighting();
			resetCaretFader();
			requestScrollToCaret();
			
			/*
			 * Caret offset for color sequences
			 */
			
			if (s.charAt(caret) == ESCAPE_SEQUENCE_COLOR) {
				offsetCaret(ESCAPE_SEQUENCE_COLOR_LENGTH);
				return;
			}
			
			/*
			 * Standard behavior
			 */
			
			offsetCaret(1);
		}
	}
	
	/**
	 * This function attempts to scroll the text area to the caret location.
	 * <br><br>
	 * It's not perfect due to the fact that NanoVG seems to supply faulty advance data for text, so this will become more unstable as the text expands. 
	 * The good news is that it takes an absurd amount of text to be inputted before this becomes a problem. To get around it, you can either enable word wrapping 
	 * or set line 
	 */
	private void scrollToCaret() {
		
		/*
		 * Vertical Scroll
		 */
		
		float scissorY = widget.getScissorY();
		float scissorH = widget.getTextContentH();
		
		float caretY = caretPosition.y() - widget.getTextContentY();
		float caretH = widget.getFontHeight();
		
		float newScissorY = -WidgetUtils.clamp(-scissorY, caretY + caretH - scissorH, caretY);
		float newVerticalScroll = -(newScissorY / Math.max(widget.getMaxScissorY(), 1.0f));
		
		widget.setVerticalScroll(newVerticalScroll);

		/*
		 * Horizontal Scroll
		 */
		
		float scissorX = widget.getScissorX();
		float scissorW = widget.getTextContentW();
		
		float caretX = caretPosition.x() - widget.getTextContentX();
		float caretW = 1.0f;
		
		float newScissorX = -WidgetUtils.clamp(-scissorX, caretX + caretW - scissorW, caretX);
		float newHorizontalScroll = -(newScissorX / Math.max(widget.getMaxScissorX(), 1.0f));
		
		widget.setHorizontalScroll(newHorizontalScroll);
	}
	
	/**
	 * Requests that the scrollbar be scrolled to an approximation of the caret location.
	 * 
	 * It's queued up to ensure that the scroll happens after the caret has been updated to its latest position (otherwise it'll use an outdated position).
	 * 
	 * @see <code>scrollToCaret()</code>
	 * @see <code>notifyCaretUpdated()</code>
	 */
	private void requestScrollToCaret() {
		requestScrollToCaret = true;
	}
	
	/*
	 * 
	 * 
	 * Other getters/setters
	 * 
	 * 
	 */
	
	/**
	 * Returns a string that is processed by the content handler; containing no escape sequences (recognized by the system). The algorithm used here
	 * is the same one that is used by the renderer, just with the functionality stripped. This is useful for getting text readable by a user, whereas normally
	 * if you just pulled the text from the widget, you'd get text meant to be read by the renderer which includes all of the escape sequences.
	 * 
	 * @param s - the string to process
	 * @return
	 */
	public String getProcessedText(String s) {
		StringBuilder copy = new StringBuilder();
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			
			if (skipsRequested == 0) {
				if (!processSequence(this, c)) {
					copy.append(c);
				}
			} else {
				skipsRequested--;
				continue;
			}
		}
		
		skipsRequested = 0;
		
		return copy.toString();
	}
	
	/**
	 * This returns a hashmap containing escape sequences and their replacements. This hashmap is used to get around limitations of NanoVG, such as not supporting \t. 
	 * It's initialized with default values from TextAreaDefaultEscapeSequenceReplacements, however it can be cleared and modified to your heart's content.
	 * <br><br>
	 * Keep in mind that TextAreaContentHandler has some escape sequences that are hardcoded. These are visible via the final static escape sequence strings.
	 */
	public HashMap<String, String> getEscapeSequenceReplacements() {
		return escapeSequenceReplacements;
	}
	
	private void offsetCaret(int offset, boolean allowLeftMovement) {
		setCaretPosition(caret + offset, allowLeftMovement);
		/*System.err.println("offset: " + offset + " (" + caret + ")");
		Thread.dumpStack();*/
	}
	
	void offsetCaret(int offset) {
		offsetCaret(offset, true);
	}

	/**
	 * Sets the caret position. If manual formatting is disabled, this command will ensure that the caret never lands on an escape sequence. 
	 * Always use this function, never reference the caret variable directly outside of comparisons.
	 */
	private void setCaretPosition(int newCaret, boolean allowLeftMovement) {
		boolean movingRight = (caret < newCaret);
		
		caret = newCaret;
		
		//If manual formatting is disabled, make sure the caret never lands on an escape sequence.
		if (!widget.getInputSettings().isManualFormattingEnabled()) {
			StringBuilder s = widget.getTextBuilder();

			if (movingRight || !allowLeftMovement) {
				//Skip right if moving the caret right
				while(caret < s.length()-1 && isCharEscapeSequence(s, caret)) {
					caret++;
				}
			} else {
				//Skip left if moving the caret left
				while(caret > 0 && isCharEscapeSequence(s, caret)) {
					caret--;
				}
			}
		}

		/*System.err.println("pos: " + this.caret);
		Thread.dumpStack();*/
	}
	
	public void setCaretPosition(int newCaret) {
		setCaretPosition(newCaret, true);
	}

	public int getCaretPosition() {
		return caret;
	}

	void notifyTextFillChanged(ClearColor newTextFill) {
		if (currentTextFill == null) {
			currentTextFill = newTextFill.copy();
		} else if (!newTextFill.rgbMatches(currentTextFill)) {
			currentTextFill.red(newTextFill.getRed()).green(newTextFill.getGreen()).blue(newTextFill.getBlue());
		}
	}
	
	void notifyTextStyleChanged(FontStyle fontStyle) {
		currentTextStyle = fontStyle;
	}
}
