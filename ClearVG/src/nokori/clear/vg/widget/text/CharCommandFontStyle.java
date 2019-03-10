package nokori.clear.vg.widget.text;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;

class CharCommandFontStyle extends CharCommand{
	
	public static final int COMMAND_ARRAY_INDEX = 1;
	
	private Font font;
	private FontStyle fontStyle;

	public CharCommandFontStyle(Font font, FontStyle fontStyle) {
		this.font = font;
		this.fontStyle = fontStyle;
	}

	@Override
	public void run(NanoVGContext context, TextAreaWidget textArea, String text, int index, boolean indexHighlighted) {
		font.configureNVG(context, textArea.getFontSize(), fontStyle);
	}

	@Override
	public int getCommandArrayIndex() {
		return COMMAND_ARRAY_INDEX;
	}
}
