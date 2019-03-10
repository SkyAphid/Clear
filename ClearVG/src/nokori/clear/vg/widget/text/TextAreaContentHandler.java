package nokori.clear.vg.widget.text;

import static org.lwjgl.nanovg.NanoVG.*;

import java.nio.FloatBuffer;
import java.util.HashMap;

import org.joml.Vector2d;
import org.lwjgl.BufferUtils;
import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.transition.SimpleTransition;
import nokori.clear.vg.widget.assembly.WidgetUtil;

/**
 * Handles the internal logic for TextAreas when it comes to rendering, formatting, and selecting text.
 */
public class TextAreaContentHandler {
	
	private TextAreaWidget textArea;
	
	private FloatBuffer boundsTempBuffer = BufferUtils.createFloatBuffer(4);
	
	private HashMap<String, String> specialCaseStrings = TextAreaContentSpecialCaseStrings.initDefault();
	
	/*
	 * Commands
	 */
	
	private static final int TOTAL_COMMAND_TYPES = 2;
	private HashMap<Integer, CharCommand[]> commands = new HashMap<Integer, CharCommand[]>();
	
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
	private int caretLine = -1;

	/*
	 * Highlighting
	 */
	
	private boolean mousePressed = false;
	private int highlightedStartIndex = -1;
	private int highlightedEndIndex = -1;
	
	public TextAreaContentHandler(TextAreaWidget textArea) {
		this.textArea = textArea;
	}

	/**
	 * Renders the given line of text and returns the number of characters rendered. Font.split() has to be used before this will work.
	 * 
	 * @param context - NanoVG Context
	 * @param font - the font to be used
	 * @param text - the line of text to render
	 * @param startIndex - the number of characters rendered so far
	 * @param x - the start render x
	 * @param y - the render y
	 * @return - the number of characters rendered
	 */
	public int render(NanoVGContext context, int totalTextLength, String text, int startIndex, float x, float y, float scissorY, float fontHeight) {
		long vg = context.get();

		int totalCharacters = 0;
		float advanceX = x;
		
		float adjustedClickY = y + scissorY;
		
		for (int i = 0; i < text.length(); i++) {
			int characterIndex = startIndex + i;

			String c = checkString(Character.toString(text.charAt(i)));

			// render highlighted text
			if (textArea.isHighlightingEnabled() && inHighlightedRange(characterIndex)) {
				renderHighlight(vg, advanceX, y, fontHeight, characterIndex, c);
			}

			// save state so that text formatting commands don't carry over into the next rendering
			nvgSave(vg);

			// render text
			runCommands(context, text, characterIndex);
			float bAdvanceX = advanceX;
			advanceX = nvgText(vg, advanceX, y, c);

			// caret systems
			if (textArea.isCaretEnabled()) {
				forEachCharCaretLogic(vg, characterIndex, bAdvanceX, y, adjustedClickY, (advanceX - bAdvanceX), fontHeight);
			}

			// add this character to total characters rendered
			totalCharacters++;

			// pop state
			nvgRestore(vg);
		}
		
		edgeCaretLogic(vg, totalTextLength, startIndex, startIndex + totalCharacters, x, advanceX, y, adjustedClickY, fontHeight);
		
		return totalCharacters;
	}
	
	/**
	 * Cross-references the given string with the special cases hashmap. If this string is a special case, the replacement is returned instead. 
	 * Otherwise the string given is just returned.
	 */
	private String checkString(String s) {
		if (specialCaseStrings.containsKey(s)) {
			return specialCaseStrings.get(s);
		}
		
		return s;
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
		/*
		 * Caret fading logic
		 */
		
		if (textArea.isHighlightingEnabled()) {
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
		
		ClearColor caretFill = textArea.getCaretFill().alpha(caretFader);
		
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
	
	private boolean inHighlightedRange(int index) {
		return (index >= highlightedStartIndex && index < highlightedEndIndex);
	}
	
	private void renderHighlight(long vg, float x, float y, float fontHeight, int characterIndex, String character) {
		nvgSave(vg);
		
		textArea.getHighlightFill().tallocNVG(fill -> {
			//Get bounding for the highlight coordinates.
			nvgTextBounds(vg, 0, 0, character, boundsTempBuffer);
			float highlightW = (boundsTempBuffer.get(2) - boundsTempBuffer.get(0));

			WidgetUtil.nvgRect(vg, fill, x, y, highlightW, fontHeight);
		});
		
		nvgRestore(vg);
	}
	
	
	/**
	 * Updates the highlighted start/end indices to match the caret index when it's updated
	 */
	/*
	 * TODO: Highlighting indices need to be reworked so that they still work even when you highlight backwards. 
	 * Here's my current idea (but too tired to implement): The start index merely represents where the caret begins. Then the end index is where it stops.
	 * The key difference between that and the current implementation is that you should make proxy getStart/EndHighlight() functions that translate those values into
	 * ones the rendering and logic can use effectively.
	 */
	private void refreshHighlightIndex() {
		if (highlightedStartIndex == -1) {
			highlightedStartIndex = caret;
		} else {
			highlightedEndIndex = caret;
		}
	}
	
	public void resetHighlightIndex() {
		highlightedStartIndex = -1;
		highlightedEndIndex = -1;
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
		if (textArea.isScrollbarSelected()) return;
		
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
	
	void textInsertedCallback(int index, boolean added) {
		if (added) {
			if (commands.containsKey(index)) {
				CharCommand[] c = commands.get(index);
				clearCommand(index);
				commands.put(index+1, c);
			}
			
			caret++;
		} else {
			clearCommand(index);
			caret--;
		}
	}
	
	public void moveCaretDown() {
		
	}
	
	public void moveCaretUp() {
		
	}
	
	/*
	 * 
	 * 
	 * Command functionality
	 * 
	 * 
	 */
	
	private void runCommands(NanoVGContext context, String text, int index) {
		if (commands.containsKey(index)) {
			CharCommand[] c = commands.get(index);
			
			for (int i = 0; i < c.length; i++) {
				if (c[i] == null) {
					continue;
				}
				
				c[i].run(context, textArea, text, index, inHighlightedRange(index));
			}
		}
	}
	
	private void addCommand(int index, CharCommand command) {
		if (commands.containsKey(index)) {
			commands.get(index)[command.getCommandArrayIndex()] = command;
		} else {
			commands.put(index, new CharCommand[TOTAL_COMMAND_TYPES]);
			addCommand(index, command);
		}
	}
	
	public void clearCommand(int index) {
		commands.remove(index);
	}
	
	private void clearCommand(int startIndex, int endIndex, int commandArrayIndex) {
		for (int i = startIndex; i < endIndex; i++) {
			if (commands.containsKey(i)) {
				commands.get(i)[commandArrayIndex] = null;
			}
		}
	}
	
	public void clearAllCommands(int startIndex, int endIndex) {
		for (int i = startIndex; i < endIndex; i++) {
			clearCommand(i);
		}
	}
	
	public void clearAllCommands() {
		commands.clear();
	}

	/*
	 * 
	 * Command wrappers
	 * 
	 * 
	 */
	
	public void addCharacterFillAt(int startIndex, int endIndex, ClearColor fill) {
		for (int i = startIndex; i < endIndex; i++) {
			addCommand(i, new CharCommandFill(fill));
		}
	}
	
	public void clearCharacterFillAt(int index) {
		clearCharacterFillAt(index, index);
	}
	
	public void clearCharacterFillAt(int startIndex, int endIndex) {
		clearCommand(startIndex, endIndex, CharCommandFill.COMMAND_ARRAY_INDEX);
	}
	
	public void addCharacterFontStyleAt(int startIndex, int endIndex, FontStyle fontStyle) {
		for (int i = startIndex; i < endIndex; i++) {
			addCommand(i, new CharCommandFontStyle(textArea.getFont(), fontStyle));
		}
	}
	
	public void clearCharacterFontStyleAt(int index) {
		clearCharacterFontStyleAt(index, index);
	}
	
	public void clearCharacterFontStyleAt(int startIndex, int endIndex) {
		clearCommand(startIndex, endIndex, CharCommandFontStyle.COMMAND_ARRAY_INDEX);
	}
	
	/*
	 * 
	 * 
	 * Other getters/setters
	 * 
	 * 
	 */
	
	public HashMap<String, String> getSpecialCaseStrings() {
		return specialCaseStrings;
	}
	
	public int getCaretPosition() {
		return caret;
	}
}
