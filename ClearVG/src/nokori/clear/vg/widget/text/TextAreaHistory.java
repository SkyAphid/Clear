package nokori.clear.vg.widget.text;

import java.util.Stack;

public class TextAreaHistory {
	
	private Stack<CharAction> undoHistory = new Stack<CharAction>();
	private Stack<CharAction> redoHistory = new Stack<CharAction>();
	
	private class CharAction {
		int index;
		char c;
		boolean added; //subtracted if false
		
		public CharAction(int index, char c, boolean added) {
			this.index = index;
			this.c = c;
			this.added = added;
		}
	}
}
