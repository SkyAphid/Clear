package nokori.clear.vg.widget.text;

import java.util.Stack;

public class TextAreaHistory {
	
	private Stack<TextState> undoStack = new Stack<TextState>();
	private Stack<TextState> redoStack = new Stack<TextState>();
	
	private static class TextState {
		StringBuilder textBuilder;
		int caret;
		
		public TextState(StringBuilder textBuilder, int caret) {
			this.textBuilder = textBuilder;
			this.caret = caret;
		}
	}
	
	public void pushState(StringBuilder textBuilder, int caret) {
		undoStack.push(new TextState(new StringBuilder(textBuilder), caret));
	}
	
	public void undo(TextAreaWidget widget, TextAreaContentHandler textAreaContentHandler) {
		TextState state = undoStack.pop();
		widget.setTextBuilder(state.textBuilder);
		textAreaContentHandler.setCaretPosition(state.caret);
	}
}
