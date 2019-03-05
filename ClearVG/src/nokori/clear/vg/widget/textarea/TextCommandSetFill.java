package nokori.clear.vg.widget.textarea;

import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;

public class TextCommandSetFill extends TextCommand {
	
	private ClearColor fill;
	private ClearColor cachedAltFill = null;

	public TextCommandSetFill(int startIndex, int endIndex, ClearColor fill) {
		super(startIndex, endIndex);
		this.fill = fill;
	}

	@Override
	public void run(NanoVGContext context, TextAreaWidget textArea, String text, int index, boolean indexHighlighted) {
		ClearColor fill = this.fill;
		
		if (indexHighlighted && textArea.getHighlightFill().rgbMatches(fill)) {
			if (cachedAltFill == null) {
				cachedAltFill = fill.copy().divide(2.5f);
			} else {
				fill = cachedAltFill;
			}
		}
		
		fill.tallocNVG(f -> {
			NanoVG.nvgFillColor(context.get(), f);
		});
	}

	@Override
	boolean matches(TextCommand command) {
		return (command instanceof TextCommandSetFill && ((TextCommandSetFill) command).fill.rgbMatches(fill));
	}
}
