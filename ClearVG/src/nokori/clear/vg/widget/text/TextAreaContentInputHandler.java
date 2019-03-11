package nokori.clear.vg.widget.text;

import org.lwjgl.glfw.GLFW;

import nokori.clear.windows.event.KeyEvent;

/**
 * This class handles special editing shortcuts (e.g. CTRL-V, Backspace, etc.) for the TextAreaContentHandler.
 */
public class TextAreaContentInputHandler {
	
	private TextAreaContentHandler textContentHandler;
	
	public TextAreaContentInputHandler(TextAreaContentHandler textContentHandler) {
		this.textContentHandler = textContentHandler;
	}

	/**
	 * 
	 * @param widget
	 * @param textContentHandler
	 * @param textBuilder
	 * @param event
	 */
	public void keyEvent(KeyEvent event) {
		if (!event.isPressed()) return;
		
		/*
		 * Backspace 
		 */
		
		if (event.getKey() == GLFW.GLFW_KEY_BACKSPACE) {
			textContentHandler.backspaceAtCaret();
		}
		
		/*
		 * Move Caret
		 */
		
		//Move caret right
		if (event.getKey() == GLFW.GLFW_KEY_RIGHT) {
			textContentHandler.moveCaretRight();
		}
		
		//Move caret left
		if (event.getKey() == GLFW.GLFW_KEY_LEFT) {
			textContentHandler.moveCaretLeft();
		}
		
		//Move caret down
		if (event.getKey() == GLFW.GLFW_KEY_DOWN) {
			
		}
		
		
	}
	


	

}
