package nokori.clear.vg.widget.text;

import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;

class CharCommandFill extends CharCommand {
	
	public static final int COMMAND_ARRAY_INDEX = 0;
	
	private ClearColor fill;
	private ClearColor cachedAltFill = null;

	public CharCommandFill(ClearColor fill) {
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
	public int getCommandArrayIndex() {
		return COMMAND_ARRAY_INDEX;
	}
}
