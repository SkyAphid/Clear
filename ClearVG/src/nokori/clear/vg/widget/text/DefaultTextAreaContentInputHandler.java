package nokori.clear.vg.widget.text;

import org.lwjgl.glfw.GLFW;

import nokori.clear.windows.Window;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;

public class DefaultTextAreaContentInputHandler extends TextAreaContentInputHandler {
	
	private boolean mousePressed = false;
	
	public DefaultTextAreaContentInputHandler(TextAreaWidget textAreaWidget, TextAreaContentHandler textAreaContentHandler) {
		super(textAreaWidget, textAreaContentHandler);
	}

	public void charEvent(Window window, CharEvent event) {
		if (settings().isEditingEnabled()) {
			content().insertCharacterAtCaret(event.getCharString());
		}
	}
	
	public void mouseMotionEvent(Window window, MouseMotionEvent event) {
		if (mousePressed && !widget().isScrollbarSelected()) {
			content().queueCaret((float) event.getMouseX(), (float) event.getMouseY());
		} else {
			mousePressed = false;
			
			if (widget().isScrollbarSelected()) {
				content().resetHighlighting();
			}
		}
	}
	
	public void mouseButtonEvent(Window window, MouseButtonEvent event) {
		if (widget().isScrollbarSelected()) return;
		
		if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && settings().isCaretEnabled()) {
			//This toggles highlighting mode
			boolean bMousePressed = mousePressed;
			mousePressed = event.isPressed();

			if (mousePressed) {
				//This queues up caret repositioning based on the mouse coordinates
				//Update the caret positioning on next render when we have the character
				//locations available
				content().queueCaret((float) event.getMouseX(), (float) event.getMouseY());

				// If the mouse wasn't previously pressed, reset the highlighting.
				if (!bMousePressed) {
					content().resetHighlighting();
				}
			}
		} else {
			mousePressed = false;
			content().resetHighlighting();
		}
	}

	public void keyEvent(Window window, KeyEvent event) {
		if (!event.isPressed()) return;
		
		int key = event.getKey();
		TextAreaInputSettings config = settings();
		
		/*
		 * Backspace 
		 */
		
		if (config.isBackspaceEnabled() && key == GLFW.GLFW_KEY_BACKSPACE) {
			
			if (content().isContentHighlighted()) {
				content().deleteHighlightedContent();
			} else {
				content().backspaceAtCaret();
			}
			
			return;
		}
		
		/*
		 * Tab
		 */
		
		if (config.isTabEnabled() && key == GLFW.GLFW_KEY_TAB) {
			content().tabAtCaret();
			return;
		}
		
		/*
		 * Return
		 */
		
		if (config.isReturnEnabled() && key == GLFW.GLFW_KEY_ENTER) {
			content().newLineAtCaret();
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
				content().copySelectionToClipboard(window);
				return;
			}
			
			/*
			 * Cut
			 */
			
			if (config.isCutEnabled() && event.getKey() == GLFW.GLFW_KEY_X) {
				content().cutSelectionToClipboard(window);
				return;
			}
			
			/*
			 * Paste
			 */
			
			if (config.isPasteEnabled() && event.getKey() == GLFW.GLFW_KEY_V) {
				content().pasteClipboardAtCaret(window);
				return;
			}
			
			/*
			 * Bold
			 */
			
			if (config.isBoldEnabled() && event.getKey() == GLFW.GLFW_KEY_B) {
				content().boldenSelection();
				return;
			}
			
			/*
			 * Italic
			 */
			
			if (config.isItalicEnabled() && event.getKey() == GLFW.GLFW_KEY_I) {
				content().italicizeSelection();
				return;
			}
			
			/*
			 * Undo
			 */
			
			if (config.isUndoEnabled() && event.getKey() == GLFW.GLFW_KEY_Z) {
				content().undo();
				return;
			}
			
			/*
			 * Redo
			 */
			
			if (config.isRedoEnabled() && event.getKey() == GLFW.GLFW_KEY_Y) {
				content().redo();
				return;
			}
		}
		
		/*
		 * Move Caret
		 */
		
		if (config.isArrowKeysEnabled()) {
			//Move caret right
			if (key == GLFW.GLFW_KEY_RIGHT) {
				content().moveCaretRight();
				return;
			}
			
			//Move caret left
			if (key == GLFW.GLFW_KEY_LEFT) {
				content().moveCaretLeft();
				return;
			}
		}
		
	}
}
