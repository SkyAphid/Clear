package nokori.clear.vg.widget.text;

import java.util.HashMap;

import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.FontStyle;

public class TextAreaContentEscapeSequences {
	
	/**
	 * This hard-coded escape sequence will reset the text formatting to the default style.
	 */
	public static final String ESCAPE_SEQUENCE_RESET = "\00";
	
	/**
	 * This hard-coded escape sequence will set the text to bold styling.
	 */
	public static final String ESCAPE_SEQUENCE_BOLD = "\01";
	
	/**
	 * This hard-coded escape sequence will set the text to light styling.
	 */
	public static final String ESCAPE_SEQUENCE_LIGHT = "\02";
	
	/**
	 * This hard-coded escape sequence will set the text to italic styling.
	 */
	public static final String ESCAPE_SEQUENCE_ITALIC = "\03";
	
	/**
	 * This is a hard-code escape sequence to signify a HEX color.
	 */
	public static final String ESCAPE_SEQUENCE_COLOR = "\04";
	
	public static HashMap<String, String> initDefault() {
		HashMap<String, String> specialCaseCharacters = new HashMap<>();
		initDefaultReplacements(specialCaseCharacters);
		return specialCaseCharacters;
	}
	
	public static void initDefaultReplacements(HashMap<String, String> specialCaseCharacters) {
		specialCaseCharacters.put("\n", "");
		specialCaseCharacters.put("\t", "    ");
	}
	
	public static boolean processSequence(NanoVGContext context, TextAreaWidget widget, TextAreaContentHandler textContentHandler, int characterIndex, String c) {
		if (c.equals(ESCAPE_SEQUENCE_RESET)) {
			widget.resetRenderConfiguration(context);
			return true;
		}
		
		if (c.equals(ESCAPE_SEQUENCE_BOLD)) {
			widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.BOLD);
			return true;
		}
		
		if (c.equals(ESCAPE_SEQUENCE_LIGHT)) {
			widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.LIGHT);
			return true;
		}
		
		if (c.equals(ESCAPE_SEQUENCE_ITALIC)) {
			widget.getFont().configureNVG(context, widget.getFontSize(), FontStyle.ITALIC);
			return true;
		}
		
		if (c.equals(ESCAPE_SEQUENCE_COLOR)) {
			colorEscapeSequence(context, widget, textContentHandler, characterIndex, c);
			return true;
		}
		
		return false;
	}
	
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
		textContentHandler.hexIndexOffsetRequested = true;
		
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
					
					//If this character is highlighted, tweak the color if it matches the highlight color.
					if (textContentHandler.isContentHighlighted(characterIndex) && widget.getHighlightFill().rgbMatches(color)) {
						color = color.divide(2.5f);
					}
					
					//Finally, set the color for rendering.
					color.tallocNVG(fill -> {
						NanoVG.nvgFillColor(context.get(), fill);
					});
				} else {
					System.err.println("WARNING: Caught invalid Color Escape Sequence at index " + characterIndex + ". Input: " + hexColor);
				}
			} else {
				System.err.println("WARNING: Incomplete Color Escape Sequence at index " + characterIndex + " (HEX color is incorrect length!)");
			}
		}
	}
}
