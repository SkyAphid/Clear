package nokori.clear.vg.widget.text;

import java.util.HashMap;

import org.joml.Vector2i;
import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.FontStyle;

public class TextAreaContentEscapeSequences {
	
	/**
	 * This hard-coded escape sequence will reset the text formatting to the default style.
	 */
	public static final String ESCAPE_SEQUENCE_RESET = "\01";
	
	/**
	 * This hard-coded escape sequence will set the text to bold styling.
	 */
	public static final String ESCAPE_SEQUENCE_BOLD = "\02";
	
	/**
	 * This hard-coded escape sequence will set the text to light styling.
	 */
	public static final String ESCAPE_SEQUENCE_LIGHT = "\03";
	
	/**
	 * This hard-coded escape sequence will set the text to italic styling.
	 */
	public static final String ESCAPE_SEQUENCE_ITALIC = "\04";
	
	/**
	 * This is a hard-code escape sequence to signify a HEX color.
	 */
	public static final String ESCAPE_SEQUENCE_COLOR = "\05";
	
	public static final String[] ESCAPE_SEQUENCES = {
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
	
	public static boolean processSequence(TextAreaContentHandler textContentHandler, String c) {
		return processSequence(null, null, textContentHandler, 0, c);
	}

	public static boolean processSequence(NanoVGContext context, TextAreaWidget widget, TextAreaContentHandler textContentHandler, int characterIndex, String c) {
		boolean checkOnly = (context == null || widget == null);
		
		if (c.equals(ESCAPE_SEQUENCE_RESET)) {
			if (!checkOnly) {
				widget.resetRenderConfiguration(context);
			}
			
			return true;
		}
		
		if (c.equals(ESCAPE_SEQUENCE_BOLD)) {
			if (!checkOnly) {
				textContentHandler.notifyTextStyleChanged(FontStyle.BOLD);
				widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.BOLD);
			}
			
			return true;
		}
		
		if (c.equals(ESCAPE_SEQUENCE_LIGHT)) {
			if (!checkOnly) {
				textContentHandler.notifyTextStyleChanged(FontStyle.LIGHT);
				widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.LIGHT);
			}
			
			return true;
		}
		
		if (c.equals(ESCAPE_SEQUENCE_ITALIC)) {
			if (!checkOnly) {
				textContentHandler.notifyTextStyleChanged(FontStyle.ITALIC);
				widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.ITALIC);
			}
			
			return true;
		}
		
		if (c.equals(ESCAPE_SEQUENCE_COLOR)) {
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
	
	public static boolean insideSequence(String sequence, StringBuilder textBuilder, int index) {
		/*
		 * Check to the left of the index. We don't have to check to the right because only commands to the left have an effect.
		 */
		for (int i = index; i > 0; i--) {
			String c = Character.toString(textBuilder.charAt(i));
			
			if (c.equals(ESCAPE_SEQUENCE_RESET)) {
				return false;
			}
			
			if (c.equals(sequence)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * In cases where we backspace an escape sequence, we'll want to scan ahead and delete its corresponding reset sequence if applicable.
	 * @param textBuilder
	 * @param index
	 */
	public static void deleteResetEscapeSequenceAhead(StringBuilder textBuilder, int index) {
		/*
		 * Check to the left to make sure there aren't any uncaught escape sequences.
		 */
		for (int i = index; i > 0; i--) {
			String c = Character.toString(textBuilder.charAt(i));
			
			if (c.equals(ESCAPE_SEQUENCE_RESET)) {
				break;
			}
			
			for (int j = 1; j < ESCAPE_SEQUENCES.length; j++) {
				if (c.equals(ESCAPE_SEQUENCES[j])) {
					//System.err.println("1 " + j);
					return;
				}
			}
		}
		
		/*
		 * If nothing was caught, scan ahead for a reset tag and delete it. If we run into another escape sequence, we quit.
		 */
		
		for (int i = index; i < textBuilder.length(); i++) {
			String c = Character.toString(textBuilder.charAt(i));
			
			System.err.println(c);
			
			if (c.equals(ESCAPE_SEQUENCE_RESET)) {
				textBuilder.deleteCharAt(i);
				//System.err.println("3 " + c);
				return;
			}
			
			for (int j = 1; j < ESCAPE_SEQUENCES.length; j++) {
				if (c.equals(ESCAPE_SEQUENCES[j])) {
					//System.err.println("2 " + j);
					return;
				}
			}
		}
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
	public static void colorEscapeSequence(NanoVGContext context, TextAreaWidget widget, TextAreaContentHandler textContentHandler, int characterIndex, String c) {
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
	
	public static Vector2i colorEscapeSequence(StringBuilder textBuilder, int characterIndex) {
		int colorLength = ClearColor.HEX_COLOR_LENGTH + 1;
		int start = -1;
		
		for (int i = characterIndex - colorLength; i < characterIndex + colorLength; i++) {
			if (i < 0 || i >= textBuilder.length()) continue;
			
			if (Character.toString(textBuilder.charAt(i)).equals(ESCAPE_SEQUENCE_COLOR)) {
				start = i;
				break;
			}
		}
		
		int end = start + colorLength;
		
		if (start == -1) {
			return null;
		}
		
		return new Vector2i(start, end);
	}
}
