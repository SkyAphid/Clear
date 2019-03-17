package nokori.clear.vg.widget.text;

import java.util.HashMap;

import org.joml.Vector2i;
import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.FontStyle;

public class ClearEscapeSequences {
	
	/**
	 * This hard-coded escape sequence will reset the text formatting to the default style.
	 */
	public static final char ESCAPE_SEQUENCE_RESET = '\01';
	
	/**
	 * This hard-coded escape sequence will set the text to bold styling.
	 */
	public static final char ESCAPE_SEQUENCE_BOLD = '\02';
	
	/**
	 * This hard-coded escape sequence will set the text to light styling.
	 */
	public static final char ESCAPE_SEQUENCE_LIGHT = '\03';
	
	/**
	 * This hard-coded escape sequence will set the text to italic styling.
	 */
	public static final char ESCAPE_SEQUENCE_ITALIC = '\04';
	
	/**
	 * This is a hard-coded escape sequence to signify a HEX color. Usage looks like this: <code>\05#FFFFFF</code>
	 */
	public static final char ESCAPE_SEQUENCE_COLOR = '\05';
	
	public static final char[] ESCAPE_SEQUENCES = {
		ESCAPE_SEQUENCE_RESET, ESCAPE_SEQUENCE_BOLD, ESCAPE_SEQUENCE_LIGHT, ESCAPE_SEQUENCE_ITALIC, ESCAPE_SEQUENCE_COLOR	
	};
	
	public static HashMap<String, String> initDefault() {
		HashMap<String, String> specialCaseCharacters = new HashMap<>();
		initDefaultReplacements(specialCaseCharacters);
		return specialCaseCharacters;
	}
	
	public static void initDefaultReplacements(HashMap<String, String> specialCaseCharacters) {
		specialCaseCharacters.put("\n", "");
		specialCaseCharacters.put("\t", "    ");
	}
	
	public static boolean processSequence(TextAreaContentHandler textContentHandler, char c) {
		return processSequence(null, null, textContentHandler, 0, c);
	}

	public static boolean processSequence(NanoVGContext context, TextAreaWidget widget, TextAreaContentHandler textContentHandler, int characterIndex, char c) {
		boolean checkOnly = (context == null || widget == null);
		
		if (c == ESCAPE_SEQUENCE_RESET) {
			if (!checkOnly) {
				widget.resetRenderConfiguration(context);
			}
			
			return true;
		}
		
		if (c == ESCAPE_SEQUENCE_BOLD) {
			if (!checkOnly) {
				textContentHandler.notifyTextStyleChanged(FontStyle.BOLD);
				widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.BOLD);
			}
			
			return true;
		}
		
		if (c == ESCAPE_SEQUENCE_LIGHT) {
			if (!checkOnly) {
				textContentHandler.notifyTextStyleChanged(FontStyle.LIGHT);
				widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.LIGHT);
			}
			
			return true;
		}
		
		if (c == ESCAPE_SEQUENCE_ITALIC) {
			if (!checkOnly) {
				textContentHandler.notifyTextStyleChanged(FontStyle.ITALIC);
				widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.ITALIC);
			}
			
			return true;
		}
		
		if (c == ESCAPE_SEQUENCE_COLOR) {
			if (!checkOnly) {			
				textContentHandler.skipsRequested = ClearColor.HEX_COLOR_LENGTH + 1;
				colorEscapeSequence(context, widget, textContentHandler, characterIndex, c);
			} else {
				textContentHandler.skipsRequested = ClearColor.HEX_COLOR_LENGTH;
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean insideSequence(char escapeSequence, StringBuilder textBuilder, int index) {
		/*
		 * Check to the left of the index. We don't have to check to the right because only commands to the left have an effect.
		 */
		for (int i = index; i > 0; i--) {
			char c = textBuilder.charAt(i);
			
			if (c == ESCAPE_SEQUENCE_RESET) {
				return false;
			}
			
			if (c == escapeSequence) {
				return true;
			}
			
			//System.err.println(i + " " + c + " -> " + c.equals(escapeSequence));
		}

		return false;
	}
	
	/**
	 * Deletes an escape sequence at the index. Also delete a reset sequence ahead if applicable.
	 * 
	 * @param textBuilder
	 * @param index
	 * @param deleteResetEscapeSequenceAhead
	 * @return characters deleted
	 */
	public static int deleteSequenceAt(StringBuilder textBuilder, int index, boolean deleteResetEscapeSequenceAhead) {
		char c = textBuilder.charAt(index);
		int deleted = 0;
		
		if (c == ESCAPE_SEQUENCE_COLOR) {
			Vector2i colorSequence = ClearEscapeSequences.colorEscapeSequence(textBuilder, index, true);
			textBuilder.delete(colorSequence.x(), colorSequence.y());
			deleted += (colorSequence.y() - colorSequence.x());
		} else {
			textBuilder.deleteCharAt(index);
			deleted++;
		}
		
		if (deleteResetEscapeSequenceAhead) {
			deleted += deleteResetEscapeSequenceAhead(textBuilder, index);
		}
		
		return deleted;
	}
	
	public static int deleteResetEscapeSequenceAhead(StringBuilder textBuilder, int index) {
		return deleteResetEscapeSequenceAhead(textBuilder, index, null);
	}
	
	/**
	 * In cases where we backspace an escape sequence, we'll want to scan ahead and delete its corresponding reset sequence if applicable.
	 * @param textBuilder
	 * @param index
	 * @return how many characters were deleted (1 if the reset sequence was found and deleted, 0 if nothing was found)
	 */
	public static int deleteResetEscapeSequenceAhead(StringBuilder textBuilder, int index, CharProcessor processor) {
		if (index < 0 || index >= textBuilder.length()) return 0;
		
		/*
		 * Check to the left to make sure there aren't any uncaught escape sequences.
		 */
		
		for (int i = index; i > 0; i--) {
			char c = textBuilder.charAt(i);
			
			if (c == ESCAPE_SEQUENCE_RESET) {
				break;
			}
			
			for (int j = 1; j < ESCAPE_SEQUENCES.length; j++) {
				if (c == ESCAPE_SEQUENCES[j]) {
					return 0;
				}
			}
		}
		
		/*
		 * If nothing was caught, scan ahead for a reset tag and delete it. If we run into another escape sequence, we quit.
		 */
		
		for (int i = index; i < textBuilder.length(); i++) {
			char c = textBuilder.charAt(i);
			
			if (c == ESCAPE_SEQUENCE_RESET) {
				if (processor != null) {
					processor.process(i, c);
				}
				
				textBuilder.deleteCharAt(i);
				return 1;
			}
			
			for (int j = 1; j < ESCAPE_SEQUENCES.length; j++) {
				if (c == ESCAPE_SEQUENCES[j]) {
					return 0;
				}
			}
		}
		
		return 0;
	}
	
	/*
	 * 
	 * 
	 * COLOR ESCAPE SEQUENCE HANDLING
	 * 
	 * 
	 */
	
	/**
	 * Processes a color-setting escape sequence. They should look like this: \04#FFFFF. The 04 indicates this is the color escape sequence, then it is followed by a HEX value for the color.
	 * 
	 * @param context
	 * @param widget
	 * @param textContentHandler
	 * @param characterIndex
	 * @param c
	 */
	public static void colorEscapeSequence(NanoVGContext context, TextAreaWidget widget, TextAreaContentHandler textContentHandler, int characterIndex, char c) {
		StringBuilder t = widget.getTextBuilder();
		
		if (characterIndex + ClearColor.HEX_COLOR_LENGTH < t.length()) {
			
			int start = characterIndex + 1;
			int end = start + ClearColor.HEX_COLOR_LENGTH;
			int length = (end - start);
			
			if (length == ClearColor.HEX_COLOR_LENGTH) {
				String hexColor = t.substring(start, end);
				
				if (hexColor.startsWith("#")) {
					
					//We've processed the hex color. Now we want to check and see if we've cached it before to save memory.
					//If so, use the cached ClearColor. If not, make a new one and cache it for later.
					HashMap<String, ClearColor> colorCache = textContentHandler.colorCache;
					ClearColor color = null;
					
					if (colorCache.containsKey(hexColor)) {
						color = colorCache.get(hexColor);
					} else {
						color = new ClearColor(hexColor);
						colorCache.put(hexColor, color);
					}
					
					//Finally, set the color for rendering.
					color.tallocNVG(fill -> {
						NanoVG.nvgFillColor(context.get(), fill);
					});

					textContentHandler.notifyTextFillChanged(color);
				} else {
					System.err.println("WARNING: Caught invalid Color Escape Sequence at index " + characterIndex + ". Input: " + hexColor);
				}
			} else {
				System.err.println("WARNING: Incomplete Color Escape Sequence at index " + characterIndex + " (HEX color is incorrect length!)");
			}
		}
	}
	
	public static Vector2i colorEscapeSequence(StringBuilder textBuilder, int characterIndex, boolean checkRight) {
		int colorLength = ClearColor.HEX_COLOR_LENGTH + 1;
		int start = -1;
		
		int startIndex = (checkRight ? characterIndex : characterIndex - colorLength);
		int endIndex   = (checkRight ? characterIndex + colorLength : characterIndex);
		
		for (int i = startIndex; i < endIndex; i++) {
			if (i < 0 || i >= textBuilder.length()) continue;
			
			char c = textBuilder.charAt(i);

			if (c == ESCAPE_SEQUENCE_COLOR) {
				start = i;
				break;
			}
			
			for (int j = 0; j < ESCAPE_SEQUENCES.length; j++) {
				if (c == ESCAPE_SEQUENCES[j]) {
					return null;
				}
			}
		}
		
		int end = start + colorLength;
		
		if (start == -1) {
			return null;
		}
		
		return new Vector2i(start, end);
	}

	
	public interface CharProcessor {
		public void process(int index, char c);
	}
}
