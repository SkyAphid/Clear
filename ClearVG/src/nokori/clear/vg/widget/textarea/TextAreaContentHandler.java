package nokori.clear.vg.widget.textarea;

import static org.lwjgl.nanovg.NanoVG.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2d;
import org.lwjgl.BufferUtils;
import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.transition.SimpleTransition;
import nokori.clear.vg.widget.assembly.WidgetUtil;

/**
 * Handles the internal logic for TextAreas when it comes to rendering, formatting, and selecting text.
 */
public class TextAreaContentHandler {
	
	private TextAreaWidget textArea;
	private ArrayList<TextCommand> commands = new ArrayList<>();
	private FloatBuffer bounds = BufferUtils.createFloatBuffer(4);
	
	private HashMap<String, String> specialCaseStrings = TextAreaContentSpecialCaseStrings.initDefault();
	
	/*
	 * Caret
	 */

	private SimpleTransition caretFadeTransition = null;
	private float caretFader = 0f;
	
	private boolean updateCaret = false;
	private Vector2d caretUpdateQueue = new Vector2d(-1, -1);
	private int caretPosition = 0;
	
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
	public int render(NanoVGContext context, String text, int startIndex, float x, float y, float fontHeight) {
		long vg = context.get();

		int totalCharacters = 0;
		float advanceX = x;
		
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

			// Caret systems
			if (textArea.isCaretEnabled()) {
				forEachCharCaretLogic(vg, characterIndex, bAdvanceX, y, (advanceX - bAdvanceX), fontHeight);
			}

			// Add this character to total characters rendered
			totalCharacters++;

			// pop state
			nvgRestore(vg);
		}
		
		edgeCaretLogic(vg, startIndex, startIndex + totalCharacters, x, advanceX, y, fontHeight);
		
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
	private void forEachCharCaretLogic(long vg, int characterIndex, float x, float y, float advanceW, float fontHeight) {
		
		/*
		 * Logic
		 */

		if (updateCaret && WidgetUtil.pointWithinRectangle(caretUpdateQueue.x, caretUpdateQueue.y, x, y, advanceW, fontHeight)) {
			
			int bCaretPosition = caretPosition;
			caretPosition = characterIndex;
			refreshHighlightIndex();

			if (bCaretPosition != caretPosition) {
				resetCaretFader();
			}
			
			updateCaret = false;
		}
		
		/*
		 * Rendering
		 */
		
		//render caret
		if (caretPosition == characterIndex) {
			renderCaret(vg, x, y, fontHeight);
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
			if (caretFadeTransition == null) {
				caretFadeTransition = (SimpleTransition) new SimpleTransition(750, 0f, 1f, p -> {
					caretFader = p;
				}).play();
			} else if (caretFadeTransition.isFinished()) {
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
	 * Applies extra logic at the end of line rendering for positioning the caret on the edges of a line (e.g. at the very start of a line or the very end), 
	 * which would be somewhat difficult with just the base controls.
	 */
	private void edgeCaretLogic(long vg, int startIndex, int endIndex, float startX, float endX, float y, float fontHeight) {
		double mX = caretUpdateQueue.x;
		double mY = caretUpdateQueue.y;
		
		if (updateCaret) {
			if (mY >= y && mY <= y + fontHeight) {
				if (mX < startX) {
					caretPosition = startIndex;
					refreshHighlightIndex();
					resetCaretFader();
					updateCaret = false;
				}
				
				if (mX > endX) {
					caretPosition = endIndex-1;
					refreshHighlightIndex();
					resetCaretFader();
					updateCaret = false;
				}
			}
		}
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
			highlightedStartIndex = caretPosition;
		} else {
			highlightedEndIndex = caretPosition;
		}
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
			nvgTextBounds(vg, 0, 0, character, bounds);
			float highlightW = (bounds.get(2) - bounds.get(0));
			
			System.err.println(character + " " + highlightW);
			
			//I'm not proud of it, but I hate there being small spaces between highlight rectangles.
			if (characterIndex < highlightedEndIndex-2) {
				highlightW *= 2.5;
			}
			
			WidgetUtil.nvgRect(vg, fill, x, y, highlightW, fontHeight);
		});
		
		nvgRestore(vg);
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
		//This queues up caret repositioning based on the mouse coordinates
		caretUpdateQueue.set(mouseX, mouseY);
		
		//This toggles highlighting mode
		boolean bMousePressed = mousePressed;
		mousePressed = pressed;

		if (mousePressed) {
			// Update the caret positioning on next render when we have the character
			// locations available
			updateCaret = true;

			// If the mouse wasn't previously pressed, reset the highlighting.
			if (!bMousePressed) {
				highlightedStartIndex = -1;
				highlightedEndIndex = -1;
			}
		}
	}
	
	/*
	 * 
	 * 
	 * Commands
	 * 
	 * 
	 */
	
	private void runCommands(NanoVGContext context, String text, int index) {
		for (int i = 0; i < commands.size(); i++) {
			TextCommand c = commands.get(i);
			
			//Delete the command if the delete flag has been set to true through external circumstances
			if (c.deleteFlag) {
				commands.remove(i);
				i--;
			}
			
			//Run the command if it's in range
			if (c.inRange(index)) {
				c.run(context, textArea, text, index, inHighlightedRange(index));
			}
		}
	}
	
	public void addCommand(TextCommand command) {
		if (!commandMerge(command)) {
			commands.add(command);
		}
	}
	
	public void clearCommands(int startIndex, int endIndex) {
		for (int i = startIndex; i < endIndex; i++) {
			for (int j = 0; j < commands.size(); j++) {
				if (commands.get(j).inRange(j)) {
					commands.remove(j);
					j--;
				}
			}
		}
	}
	
	public void clearCommands() {
		commands.clear();
	}
	
	/**
	 * Attempts to merge similar commands that are in the same indices to try and save space. That way we don't have tons of "set bold" commands in the same indices, etc.
	 */
	private boolean commandMerge(TextCommand command) {
		for (int i = 0; i < commands.size(); i++) {
			TextCommand c = commands.get(i);
			
			if (command.matches(c)) {
				c.merge(command);
				return true;
			}
		}
		
		return false;
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
}
