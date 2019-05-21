package nokori.clear.vg.widget.text;

import nokori.clear.windows.Window;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;

/**
 * This class handles input for the TextAreaContentHandler (anything pertaining to editing text content). If you want to change the controls, you can override this class and
 * call setTextAreaContentInputHandler() on TextAreaWidget to do so however you wish.
 */
public abstract class TextAreaContentInputHandler {
	
	protected TextAreaWidget widget;
	protected TextAreaContentHandler contentHandler;

	public TextAreaContentInputHandler(TextAreaWidget textAreaWidget, TextAreaContentHandler textAreaContentHandler) {
		this.widget = textAreaWidget;
		this.contentHandler = textAreaContentHandler;
	}

	/**
	 * This is a shortcut function for getting the TextAreaInputSettings in the linked TextAreaWidget. It's recommended that these settings be considered
	 * when handling inputs so that the user can easily enable/disable features.
	 */
	protected TextAreaInputSettings settings() {
		return widget.getInputSettings();
	}
	
	public abstract void charEvent(Window window, CharEvent event);
	
	public abstract void mouseMotionEvent(Window window, MouseMotionEvent event);
	
	public abstract void mouseButtonEvent(Window window, MouseButtonEvent event);

	public abstract void keyEvent(Window window, KeyEvent event);
}
