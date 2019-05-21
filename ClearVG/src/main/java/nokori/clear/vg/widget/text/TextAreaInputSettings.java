package nokori.clear.vg.widget.text;

/**
 * Contains all input settings for a text area. This class allows you to customize all inputs for a text area in-depth and switch them in and out as needed.
 */
public class TextAreaInputSettings {
	
	private TextAreaWidget widget;
	
	//This is a general toggle variable for all inputs.
	private boolean inputEnabled = true;
	
	private int characterLimit = Integer.MAX_VALUE;
	
	private boolean caretEnabled = true;
	private boolean highlightingEnabled = true;
	
	private boolean editingEnabled = true;
	
	//If true, users can edit the formatting of the text (setting bold, italics, etc). This is disabled automatically by widgets like the Auto-Formatter (for syntax highlighting).
	//Spacing commands like \n and \t aren't affected by this, however.
	private boolean manualFormattingEnabled = true;
	
	private boolean backspaceEnabled = true;
	private boolean tabEnabled = true;
	
	private boolean returnEnabled = true;
	private boolean returnEndsEditing = false;
	
	private boolean copyEnabled = true;
	private boolean cutEnabled = true;
	private boolean pasteEnabled = true;
	
	private boolean boldEnabled = true;
	private boolean italicEnabled = true;
	
	private boolean undoEnabled = true;
	private boolean redoEnabled = true;
	
	private boolean arrowKeysEnabled = true;
	
	private boolean verticalScrollbarEnabled = true;
	private boolean horizontalScrollbarEnabled = true;
	
	/*
	 * 
	 * 
	 * GETTERS / SETTERS START
	 * 
	 * 
	 */
	
	public TextAreaInputSettings(TextAreaWidget widget) {
		this.widget = widget;
	}
	
	public boolean isInputEnabled() {
		return inputEnabled;
	}

	public void setInputEnabled(boolean inputEnabled) {
		this.inputEnabled = inputEnabled;
		
		if (!inputEnabled) {
			widget.getTextContentHandler().endEditing();
		}
	}

	public int getCharacterLimit() {
		return characterLimit;
	}

	public void setCharacterLimit(int characterLimit) {
		this.characterLimit = characterLimit;
	}

	public boolean isCaretEnabled() {
		return (inputEnabled && caretEnabled);
	}

	public void setCaretEnabled(boolean caretEnabled) {
		this.caretEnabled = caretEnabled;
	}

	public boolean isHighlightingEnabled() {
		return (inputEnabled && highlightingEnabled);
	}

	public void setHighlightingEnabled(boolean highlightingEnabled) {
		this.highlightingEnabled = highlightingEnabled;
	}
	
	public boolean isEditingEnabled() {
		return (inputEnabled && editingEnabled);
	}

	public void setEditingEnabled(boolean editingEnabled) {
		this.editingEnabled = editingEnabled;
		
		if (!editingEnabled) {
			widget.getTextContentHandler().endEditing();
		}
	}
	
	public boolean isManualFormattingEnabled() {
		return manualFormattingEnabled;
	}

	public void setManualFormattingEnabled(boolean manualFormattingEnabled) {
		this.manualFormattingEnabled = manualFormattingEnabled;
	}

	public boolean isBackspaceEnabled() {
		return (inputEnabled && backspaceEnabled);
	}

	public void setBackspaceEnabled(boolean backspaceEnabled) {
		this.backspaceEnabled = backspaceEnabled;
	}

	public boolean isTabEnabled() {
		return (inputEnabled && tabEnabled);
	}

	public void setTabEnabled(boolean tabEnabled) {
		this.tabEnabled = tabEnabled;
	}

	public boolean isReturnEnabled() {
		return (inputEnabled && returnEnabled);
	}

	public void setReturnEnabled(boolean returnEnabled) {
		this.returnEnabled = returnEnabled;
	}

	public boolean returnEndsEditing() {
		return returnEndsEditing;
	}

	public void setReturnEndsEditing(boolean returnExitsEditing) {
		this.returnEndsEditing = returnExitsEditing;
	}

	public boolean isCopyEnabled() {
		return (inputEnabled && copyEnabled);
	}

	public void setCopyEnabled(boolean copyEnabled) {
		this.copyEnabled = copyEnabled;
	}

	public boolean isCutEnabled() {
		return (inputEnabled && cutEnabled);
	}

	public void setCutEnabled(boolean cutEnabled) {
		this.cutEnabled = cutEnabled;
	}

	public boolean isPasteEnabled() {
		return (inputEnabled && pasteEnabled);
	}

	public void setPasteEnabled(boolean pasteEnabled) {
		this.pasteEnabled = pasteEnabled;
	}

	public boolean isBoldEnabled() {
		return (inputEnabled && boldEnabled);
	}

	public void setBoldEnabled(boolean boldEnabled) {
		this.boldEnabled = boldEnabled;
	}

	public boolean isItalicEnabled() {
		return (inputEnabled && italicEnabled);
	}

	public void setItalicEnabled(boolean italicEnabled) {
		this.italicEnabled = italicEnabled;
	}

	public boolean isUndoEnabled() {
		return undoEnabled;
	}

	public void setUndoEnabled(boolean undoEnabled) {
		this.undoEnabled = undoEnabled;
	}

	public boolean isRedoEnabled() {
		return redoEnabled;
	}

	public void setRedoEnabled(boolean redoEnabled) {
		this.redoEnabled = redoEnabled;
	}

	public boolean isArrowKeysEnabled() {
		return (inputEnabled && arrowKeysEnabled);
	}

	public void setArrowKeysEnabled(boolean arrowKeysEnabled) {
		this.arrowKeysEnabled = arrowKeysEnabled;
	}

	public boolean isVerticalScrollbarEnabled() {
		return (inputEnabled && verticalScrollbarEnabled);
	}

	public void setVerticalScrollbarEnabled(boolean verticalScrollbarEnabled) {
		this.verticalScrollbarEnabled = verticalScrollbarEnabled;
	}

	public boolean isHorizontalScrollbarEnabled() {
		return (inputEnabled && horizontalScrollbarEnabled);
	}

	public void setHorizontalScrollbarEnabled(boolean horizontalScrollbarEnabled) {
		this.horizontalScrollbarEnabled = horizontalScrollbarEnabled;
	}
}
