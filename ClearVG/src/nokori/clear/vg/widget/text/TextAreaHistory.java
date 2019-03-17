package nokori.clear.vg.widget.text;

import java.util.Stack;

public class TextAreaHistory {
	
	private Stack<TextState> undoStack = new Stack<TextState>();
	private Stack<TextState> redoStack = new Stack<TextState>();
	
	private static final long STATE_SAVE_TIME = 2000L; //Two seconds
	
	private long lastEditTime = -1;
	
	private static class TextState {
		StringBuilder textBuilder;
		int caret;
		
		public TextState(StringBuilder textBuilder, int caret) {
			this.textBuilder = textBuilder;
			this.caret = caret;
		}
		
		public void apply(TextAreaWidget widget, TextAreaContentHandler textAreaContentHandler) {
			widget.setTextBuilder(new StringBuilder(textBuilder));
			textAreaContentHandler.setCaretPosition(caret);
		}
	}
	
	public void notifyEditing(TextAreaWidget widget, TextAreaContentHandler contentHandler) {
		long currentTime = System.currentTimeMillis();
		
		if (lastEditTime == -1 || (currentTime - lastEditTime) >= STATE_SAVE_TIME) {
			pushState(undoStack, widget, contentHandler);
		}
		
		lastEditTime = currentTime;
	}
	
	private void pushState(Stack<TextState> stack, TextAreaWidget widget, TextAreaContentHandler contentHandler) {
		stack.push(new TextState(new StringBuilder(widget.getTextBuilder()), contentHandler.getCaretPosition()));
	}
	
	public void undo(TextAreaWidget widget, TextAreaContentHandler textAreaContentHandler) {
		if (!undoStack.isEmpty()) {
			pushState(redoStack, widget, textAreaContentHandler);
			TextState state = undoStack.pop();
			state.apply(widget, textAreaContentHandler);
		}
	}
	
	public void redo(TextAreaWidget widget, TextAreaContentHandler textAreaContentHandler) {
		if (!redoStack.isEmpty()) {
			pushState(undoStack, widget, textAreaContentHandler);
			TextState state = redoStack.pop();
			state.apply(widget, textAreaContentHandler);
		}
	}
}
