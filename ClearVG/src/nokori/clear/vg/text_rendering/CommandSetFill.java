package nokori.clear.vg.text_rendering;

import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.TextArea;

public class CommandSetFill extends TextRenderCommand {
	
	private ClearColor fill;

	public CommandSetFill(int startIndex, int endIndex, ClearColor fill) {
		super(startIndex, endIndex);
		this.fill = fill;
	}

	@Override
	public void run(NanoVGContext context, TextArea textArea, String text, int index) {
		fill.memoryStackPush(fill -> {
			NanoVG.nvgFillColor(context.get(), fill);
		});
	}

	@Override
	boolean matches(TextRenderCommand command) {
		return (command instanceof CommandSetFill && ((CommandSetFill) command).fill.rgbMatches(fill));
	}
}
