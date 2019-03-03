package nokori.clear.vg.text_rendering;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.widget.TextArea;

public class CommandSetFont extends TextRenderCommand{
	
	private Font font;
	private FontStyle fontStyle;

	public CommandSetFont(int startIndex, int endIndex, Font font, FontStyle fontStyle) {
		super(startIndex, endIndex);
		this.font = font;
		this.fontStyle = fontStyle;
	}

	@Override
	public void run(NanoVGContext context, TextArea textArea, String text, int index) {
		font.configureNVG(context, textArea.getFontSize(), fontStyle);
	}

	@Override
	boolean matches(TextRenderCommand command) {
		if (command instanceof CommandSetFont) {
			CommandSetFont c = ((CommandSetFont) command);
			
			if (c.font == font && c.fontStyle == fontStyle) {
				return true;
			}
		}
		
		return false;
	}

}
