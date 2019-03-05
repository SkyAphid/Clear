package nokori.clear.vg.widget.textarea;

import nokori.clear.vg.NanoVGContext;

public abstract class TextCommand {
	private int startIndex, endIndex;
	boolean deleteFlag = false;
	
	public TextCommand(int startIndex, int endIndex) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	
	public abstract void run(NanoVGContext context, TextAreaWidget textArea, String text, int index, boolean indexHighlighted);
	
	public int getStartIndex() {
		return startIndex;
	}
	
	public int getEndIndex() {
		return endIndex;
	}
	
	/**
	 * Adjusts the start/end indices. If the range becomes zero or less, a delete flag will be triggered to garbage collect this command.
	 * 
	 * @param startIndex
	 * @param endIndex
	 */
	public void adjust(int startIndex, int endIndex) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		
		if (endIndex <= startIndex) {
			deleteFlag = true;
		}
	}
	
	/**
	 * @param index
	 * @return true if the passed in string character index is within the range of this commands start/end indices.
	 */
	public boolean inRange(int index) {
		return (index >= getStartIndex() && index <= getEndIndex());
	}
	
	void merge(TextCommand c) {
		this.startIndex = Math.min(c.startIndex, startIndex);
		this.endIndex = Math.max(c.endIndex, endIndex);
	}
	
	/**
	 * @param command
	 * @return true if the passed-in command matches this one. Used for merging similar added commands together in the TextContentRenderer.
	 */
	abstract boolean matches(TextCommand command);
}
