package nokori.clear.vg.text_rendering;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.TextArea;

public abstract class TextRenderCommand {
	private int startIndex, endIndex;
	boolean deleteFlag = false;
	
	public TextRenderCommand(int startIndex, int endIndex) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	
	public abstract void run(NanoVGContext context, TextArea textArea, String text, int index);
	
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
	
	void merge(TextRenderCommand c) {
		this.startIndex = Math.min(c.startIndex, startIndex);
		this.endIndex = Math.max(c.endIndex, endIndex);
	}
	
	/**
	 * @param command
	 * @return true if the passed-in command matches this one. Used for merging similar added commands together in the TextContentRenderer.
	 */
	abstract boolean matches(TextRenderCommand command);
}
