package nokori.clear.vg.widget.text;

import nokori.clear.vg.NanoVGContext;

public abstract class CharCommand {
	
	public abstract void run(NanoVGContext context, TextAreaWidget textArea, String text, int index, boolean indexHighlighted);
	
	public abstract int getCommandArrayIndex();
}
