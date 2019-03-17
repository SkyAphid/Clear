package nokori.clear.vg.widget.text;

import static org.lwjgl.nanovg.NanoVG.*;

import java.util.HashMap;

import org.joml.Vector2f;
import org.joml.Vector2i;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.transition.SimpleTransition;
import nokori.clear.vg.widget.assembly.WidgetUtil;
import nokori.clear.windows.Window;

import static nokori.clear.vg.widget.text.ClearEscapeSequences.*;

/**
 * Handles the internal logic for TextAreas when it comes to rendering, formatting, and selecting text.
 */
public class TextAreaContentHandler {
	
	/**
	 * This is a debug value that will allow you to render the underlying escape sequences in the text instead of running them.
	 */
	protected static boolean renderEscapeSequences = true;
	
	private TextAreaWidget widget;
	
	private TextAreaHistory editHistory = new TextAreaHistory();
	
	/*
	 * 
	 * Escape Sequence Handlers
	 * 
	 */
	
	private HashMap<String, String> escapeSequenceReplacements = ClearEscapeSequences.initDefault();
	
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
	
	private SimpleTransition caretFadeTransition = (SimpleTransition) new SimpleTransition(750, 0f, 1f, p -> {
		caretFader = p;
	});
	
	private boolean updateCaret = false;
	private Vector2f caretUpdateQueue = new Vector2f(-1, -1);

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
			
			//Render the caret
			if (caret == characterIndex) {
				renderCaret(vg, advanceX, lineY, fontHeight);
			}

			String c = checkEscapeSequences(context, characterIndex, text.charAt(i));
			
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
					forEachCharCaretLogic(vg, characterIndex, bAdvanceX, lineY, adjustedClickY, (advanceX - bAdvanceX), fontHeight);
				}
				
				// pop state
				nvgRestore(vg);
			} else {
				skipsRequested--;
				
				//Character rendering is skipped, but caret logic is still checked.
				nvgSave(vg);
				forEachCharCaretLogic(vg, characterIndex, advanceX, lineY, adjustedClickY, 0, fontHeight);
				nvgRestore(vg);
			}

			// add this character to total characters rendered
			totalCharacters++;
		}
		
		/*
		 * For moving the caret to the very start/end of this line.
		 */
		int endIndex = startIndex + totalCharacters;
		edgeCaretLogic(vg, totalTextLength, startIndex, endIndex, textContentX, advanceX, lineY, adjustedClickY, fontHeight);
		
		//In a special case where the caret is at the end of the entirety of the text, we render the caret at the very tail end.
		//Normally it's rendered during normal character rendering - but if the caret is outside the text - then it won't draw otherwise.
		if (caret == endIndex && endIndex == totalTextLength) {
			//Highlight logic for the very end of the text.
			highlightRenderLogic(vg, advanceX, lineY, caret);
			
			//Render the caret at the very end of the text.
			renderCaret(vg, advanceX, lineY, fontHeight);
		}
		
		return totalCharacters;
	}
	
	private String checkEscapeSequences(NanoVGContext context, int characterIndex, char c) {
		if (!renderEscapeSequences) {
			String sC = Character.toString(c);
			
			if (escapeSequenceReplacements.containsKey(sC)) {
				return escapeSequenceReplacements.get(sC);
			}
			
			if (processSequence(context, widget, this, characterIndex, c)) {
				return "";
			}
		}
		
		return Character.toString(c);
	}
	
	/**
	 * To be called at the end of a rendering cycle after all lines have been rendered.
	 */
	void endOfRenderingCallback() {
		//If updateCaret is still true by the end of a rendering cycle, that means it was never successfully moved (invalid caret queue coordinates).
		//We'll turn it off so that it doesn't get deadlocked.
		updateCaret = false;
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
			setCaretPosition(characterIndex);
			refreshHighlightIndex();
	
			if (bCaretPosition != caret) {
				resetCaretFader();
			}
			
			updateCaret = false;
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
					setCaretPosition(startIndex);
					refreshHighlightIndex();
					resetCaretFader();
					updateCaret = false;
				}
				
				//Places the caret on the very end of the right side of the line if the mouse is past the very right edge of the rendering area.
				//A special case is added to put the caret past the end of the text if it's the end of the entire string.
				if (mX > endX) {
					if (endIndex < totalTextLength-1) {
						setCaretPosition(endIndex-1);
					} else {
						setCaretPosition(endIndex);
					}
					
					refreshHighlightIndex();
					resetCaretFader();
					updateCaret = false;
				}
			}
		}
	}
	
	/**
	 * Renders the caret using a SimpleTransition to fade in and out smoothly.
	 */
	private void renderCaret(long vg, float x, float y, float fontHeight) {
		if (isContentHighlighted()) return;
		
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
				WidgetUtil.nvgRect(vg, fill, x, y, defaultLineThickness * 2f, fontHeight);
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
				
				WidgetUtil.nvgShape(vg, fill, topX, topY, topX2, topY, botX2, botY, botX, botY);
				break;
			case LIGHT:
				WidgetUtil.nvgRect(vg, fill, x, y, defaultLineThickness/2f, fontHeight);
				break;
			case REGULAR:
			default:
				WidgetUtil.nvgRect(vg, fill, x, y, defaultLineThickness, fontHeight);
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
	
	public boolean isCaretActive() {
		return (widget.getInputSettings().isEditingEnabled() && widget.getInputSettings().isCaretEnabled() && caret >= 0);
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
	
	/*
	 * 
	 * 
	 * Input Commands
	 * 
	 * 
	 */

	private void deleteHighlightedContent() {
		if (isContentHighlighted()) {
			widget.getTextBuilder().delete(getHighlightStartIndex(), getHighlightEndIndex());
			setCaretPosition(getHighlightStartIndex());
			resetHighlighting();
		}
	}
	
	public void insertCharacterAtCaret(String character) {
		editHistory.notifyEditing(widget, this);
		
		StringBuilder textBuilder = widget.getTextBuilder();
		
		deleteHighlightedContent();

		if (caret >= 0 && caret <= textBuilder.length()) {
			textBuilder.insert(caret, character);
			offsetCaret(1);
		}

		widget.requestRefresh();
	}
	
	public void backspaceAtCaret() {
		offsetCaret(-backspace(caret));
	}
	
	public int backspace(int position) {
		if (position > 0) {
			StringBuilder textBuilder = widget.getTextBuilder();
			int charsDeleted = 0;
			
			/*
			 * Backspace special case for color sequences
			 */
			
			Vector2i colorSequence = colorEscapeSequence(textBuilder, position, false);
			
			if (colorSequence != null) {
				int start = colorSequence.x();
				int end = colorSequence.y();
				
				textBuilder.delete(start, end);
				charsDeleted = (position - start);
			}
			
			/*
			 * Standard backspace otherwise
			 */
			
			if (charsDeleted == 0) {
				textBuilder.deleteCharAt(position-1);
				charsDeleted = 1;
			}
			
			deleteResetEscapeSequenceAhead(textBuilder, position-1);
			widget.requestRefresh();
			return charsDeleted;
		}
		
		return 0;
	}
	
	public void tabAtCaret() {
		tab(caret);
		offsetCaret(1);
	}
	
	public void tab(int position) {
		String c = "\t";
		widget.getTextBuilder().insert(position, c);
		widget.requestRefresh();
	}
	
	public void newLineAtCaret() {
		newLine(caret);
		offsetCaret(1);
	}
	
	public void newLine(int position) {
		StringBuilder s = widget.getTextBuilder();
		String c = "\n";
		
		if (position == s.length()) {
			c += c;
		}
		
		s.insert(position, c);
		
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
	}
	
	public int pasteClipboard(Window window, int position) {
		deleteHighlightedContent();
		
		String clipboard = window.getClipboardString();
		
		widget.getTextBuilder().insert(position, clipboard);
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
			
			/*
			 * Caret offset for color sequences
			 */
			
			Vector2i colorSequence = colorEscapeSequence(widget.getTextBuilder(), caret, false);
			
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
		if (caret < widget.getTextBuilder().length()) {
			resetHighlighting();
			resetCaretFader();
			
			/*
			 * Caret offset for color sequences
			 */
			
			Vector2i colorSequence = colorEscapeSequence(widget.getTextBuilder(), caret, true);
			
			if (colorSequence != null) {
				int offset = (colorSequence.y() - caret);
				
				if (offset > 0) {
					offsetCaret(offset);
					return;
				}
			}
			
			/*
			 * Standard behavior
			 */
			
			offsetCaret(1);
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
	
	void offsetCaret(int offset) {
		caret += offset;
		/*System.err.println("offset: "+caret);
		Thread.dumpStack();*/
	}
	
	void setCaretPosition(int caret) {
		this.caret = caret;
		/*System.err.println("pos: "+caret);
		Thread.dumpStack();*/
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
