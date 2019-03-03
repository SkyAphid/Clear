package nokori.clear.vg.text_rendering;

import static org.lwjgl.nanovg.NanoVG.*;

import java.util.ArrayList;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.TextArea;

public class TextContentRenderer {
	
	private TextArea textArea;
	private ArrayList<TextRenderCommand> commands = new ArrayList<>();
	
	public TextContentRenderer(TextArea textArea) {
		this.textArea = textArea;
	}
	
	/**
	 * Renders the given line of text and returns the number of characters rendered. Font.split() has to be used before this will work.
	 * 
	 * @param context - NanoVG Context
	 * @param font - the font to be used
	 * @param text - the line of text to render
	 * @param startIndex - the number of characters rendered so far
	 * @param x - the start render x
	 * @param y - the render y
	 * @return - the number of characters rendered
	 */
	public int render(NanoVGContext context, String text, int startIndex, float x, float y) {
		long vg = context.get();
		
		int totalCharacters = 0;

		for (int i = 0; i < text.length(); i++) {
			String c = Character.toString(text.charAt(i));

			boolean commandRan = runCommands(context, text, startIndex + i);
			x = nvgText(vg, x, y, c);
			totalCharacters++;
			
			//Reset styling to TextArea default after commands have been ran and the text has been rendered.
			if (commandRan) {
				textArea.resetRenderConfiguration(context);
			}
		}
		
		return totalCharacters;
	}
	
	
	private boolean runCommands(NanoVGContext context, String text, int index) {
		boolean commandRan = false;
		
		for (int i = 0; i < commands.size(); i++) {
			TextRenderCommand c = commands.get(i);
			
			//Delete the command if the delete flag has been set to true through external circumstances
			if (c.deleteFlag) {
				commands.remove(i);
				i--;
			}
			
			//Run the command if it's in range
			if (c.inRange(index)) {
				c.run(context, textArea, text, index);
				commandRan = true;
			}
		}
		
		return commandRan;
	}
	
	public void addCommand(TextRenderCommand command) {
		if (!commandMerge(command)) {
			commands.add(command);
		}
	}
	
	public void clearCommands(int startIndex, int endIndex) {
		for (int i = startIndex; i < endIndex; i++) {
			for (int j = 0; j < commands.size(); j++) {
				if (commands.get(j).inRange(j)) {
					commands.remove(j);
					j--;
				}
			}
		}
	}
	
	public void clearCommands() {
		commands.clear();
	}
	
	/**
	 * Attempts to merge similar commands that are in the same indices to try and save space. That way we don't have tons of "set bold" commands in the same indices, etc.
	 */
	private boolean commandMerge(TextRenderCommand command) {
		for (int i = 0; i < commands.size(); i++) {
			TextRenderCommand c = commands.get(i);
			
			if (command.matches(c)) {
				c.merge(command);
				return true;
			}
		}
		
		return false;
	}
}
