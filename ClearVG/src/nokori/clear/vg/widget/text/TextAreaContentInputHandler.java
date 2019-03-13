package nokori.clear.vg.widget.text;

import org.lwjgl.glfw.GLFW;

import nokori.clear.windows.Window;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;

/**
 * This class handles input for the TextAreaContentHandler (anything pertaining to editing text content).
 */
public class TextAreaContentInputHandler {
	
	private TextAreaWidget widget;
	private TextAreaContentHandler textContentHandler;
	
	private boolean mousePressed = false;
	
	public TextAreaContentInputHandler(TextAreaWidget widget, TextAreaContentHandler textContentHandler) {
		this.widget = widget;
		this.textContentHandler = textContentHandler;
	}
	
	private TextAreaInputSettings settings() {
		return widget.getInputSettings();
	}
	
	public void charEvent(Window window, CharEvent event) {
		if (settings().isEditingEnabled()) {
			textContentHandler.insertCharacterAtCaret(event.getCharString());
		}
	}
	
	public void mouseMotionEvent(Window window, MouseMotionEvent event) {
		if (mousePressed) {
			textContentHandler.queueCaret((float) event.getMouseX(), (float) event.getMouseY());
		}
	}
	
	public void mouseButtonEvent(Window window, MouseButtonEvent event) {
		if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && settings().isCaretEnabled()) {
			//This toggles highlighting mode
			boolean bMousePressed = mousePressed;
			mousePressed = event.isPressed();

			if (mousePressed) {
				//This queues up caret repositioning based on the mouse coordinates
				// Update the caret positioning on next render when we have the character
				// locations available
				textContentHandler.queueCaret((float) event.getMouseX(), (float) event.getMouseY());

				// If the mouse wasn't previously pressed, reset the highlighting.
				if (!bMousePressed) {
					textContentHandler.resetHighlighting();
				}
			}
		} else {
			mousePressed = false;
		}
	}

	/**
	 * 
	 * @param widget
	 * @param textContentHandler
	 * @param textBuilder
	 * @param event
	 */
	public void keyEvent(Window window, KeyEvent event) {
		if (!event.isPressed()) return;
		
		int key = event.getKey();
		TextAreaInputSettings config = settings();
		
		/*
		 * Backspace 
		 */
		
		if (config.isBackspaceEnabled() && key == GLFW.GLFW_KEY_BACKSPACE) {
			textContentHandler.backspaceAtCaret();
			return;
		}
		
		/*
		 * Tab
		 */
		
		if (config.isTabEnabled() && key == GLFW.GLFW_KEY_TAB) {
			textContentHandler.tabAtCaret();
			return;
		}
		
		/*
		 * Return
		 */
		
		if (config.isReturnEnabled() && key == GLFW.GLFW_KEY_ENTER) {
			textContentHandler.newLineAtCaret();
			return;
		}
		
		/*
		 * CTRL-[button] Commands
		 */
		
		if (window.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			/*
			 * Copy
			 */
			
			if (config.isCopyEnabled() && event.getKey() == GLFW.GLFW_KEY_C) {
				textContentHandler.copySelectionToClipboard(window);
				return;
			}
			
			/*
			 * Cut
			 */
			
			if (config.isCutEnabled() && event.getKey() == GLFW.GLFW_KEY_X) {
				textContentHandler.cutSelectionToClipboard(window);
				return;
			}
			
			/*
			 * Paste
			 */
			
			if (config.isPasteEnabled() && event.getKey() == GLFW.GLFW_KEY_V) {
				textContentHandler.pasteClipboardAtCaret(window);
				return;
			}
			
			/*
			 * Bold
			 */
			
			if (config.isBoldEnabled() && event.getKey() == GLFW.GLFW_KEY_B) {
				textContentHandler.boldenSelection();
				return;
			}
			
			/*
			 * Italic
			 */
			
			if (config.isItalicEnabled() && event.getKey() == GLFW.GLFW_KEY_I) {
				textContentHandler.italicizeSelection();
				return;
			}
		}
		
		/*
		 * Move Caret
		 */
		
		if (config.isArrowKeysEnabled()) {
			//Move caret right
			if (key == GLFW.GLFW_KEY_RIGHT) {
				textContentHandler.moveCaretRight();
				return;
			}
			
			//Move caret left
			if (key == GLFW.GLFW_KEY_LEFT) {
				textContentHandler.moveCaretLeft();
				return;
			}
		}
		
	}
	


	

}
