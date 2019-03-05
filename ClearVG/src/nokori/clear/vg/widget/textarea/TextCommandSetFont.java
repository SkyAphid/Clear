package nokori.clear.vg.widget.textarea;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;

public class TextCommandSetFont extends TextCommand{
	
	private Font font;
	private FontStyle fontStyle;

	public TextCommandSetFont(int startIndex, int endIndex, Font font, FontStyle fontStyle) {
		super(startIndex, endIndex);
		this.font = font;
		this.fontStyle = fontStyle;
	}

	@Override
	public void run(NanoVGContext context, TextAreaWidget textArea, String text, int index, boolean indexHighlighted) {
		font.configureNVG(context, textArea.getFontSize(), fontStyle);
	}

	@Override
	boolean matches(TextCommand command) {
		if (command instanceof TextCommandSetFont) {
			TextCommandSetFont c = ((TextCommandSetFont) command);
			
			if (c.font == font && c.fontStyle == fontStyle) {
				return true;
			}
		}
		
		return false;
	}

}
