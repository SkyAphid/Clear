package nokori.clear.vg.widget.text;

import nokori.clear.vg.ClearStaticResources;
import nokori.clear.vg.util.NanoVGScaler;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.windows.Window;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;
import org.lwjgl.glfw.GLFW;

public class DefaultTextAreaContentInputHandler extends TextAreaContentInputHandler {
	
	private boolean mousePressed = false;
	
	private NanoVGScaler scaler;
	
	public DefaultTextAreaContentInputHandler(TextAreaWidget textAreaWidget, TextAreaContentHandler textAreaContentHandler) {
		super(textAreaWidget, textAreaContentHandler);
		this.scaler = textAreaWidget.getScaler();
	}

	public void charEvent(Window window, CharEvent event) {
		if (ClearStaticResources.isFocused(widget) && settings().isEditingEnabled()) {
			contentHandler.insertCharacterAtCaret(event.getCharString());
		}
	}
	
	public void mouseMotionEvent(Window window, MouseMotionEvent event) {
		if (mousePressed && !widget.isScrollbarSelected() && ClearStaticResources.isFocused(widget)) {
			contentHandler.queueCaret((float) event.getScaledMouseX(scaler.getScale()), (float) event.getScaledMouseY(scaler.getScale()));
		} else {
			reset(widget.isScrollbarSelected());
		}
	}
	
	public void mouseButtonEvent(Window window, MouseButtonEvent event) {
		if (widget.isScrollbarSelected()) return;
		
		if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && settings().isCaretEnabled()) {
			
			/*
			 * Re-focusing for other Text Areas
			 * 
			 * Allows us to seamlessly move the caret between text areas
			 */
			
			Widget focused = ClearStaticResources.getFocusedWidget();
			
			//Cancel editing if the mouse is hovering this text field but its currently focused on another text field
			if (event.isPressed() && widget.isMouseIntersectingThisWidget(window) 
					&& (focused instanceof TextAreaWidget && focused != widget)) {
				
				((TextAreaWidget) focused).endEditing();
			}
			
			//Reset this input handler if its not currently possible to focus on it
			if (!ClearStaticResources.isFocusedOrCanFocus(widget)) {
				reset(true);
				return;
			}
			
			/*
			 * Caret Placement & Highlighting
			 */
			boolean bMousePressed = mousePressed;
			mousePressed = event.isPressed();
			
			if (mousePressed) {
				//This queues up caret repositioning based on the mouse coordinates
				//Update the caret positioning on next render when we have the character
				//locations available
				contentHandler.queueCaret((float) event.getScaledMouseX(scaler.getScale()), (float) event.getScaledMouseY(scaler.getScale()));

				// If the mouse wasn't previously pressed, reset the highlighting.
				if (!bMousePressed) {
					contentHandler.resetHighlighting();
				}
			}
		} else {
			reset(true);
		}
	}
	
	/**
	 * Resets this input handler (sets mousePressed to false, resets the content handlers highlighting)
	 * 
	 * @param resetHighlighting
	 */
	public void reset(boolean resetHighlighting) {
		mousePressed = false;
		
		if (resetHighlighting) {
			contentHandler.resetHighlighting();
		}
	}
	
	public void keyEvent(Window window, KeyEvent event) {
		if (!ClearStaticResources.isFocused(widget) || !event.isPressed()) return;
		
		int key = event.getKey();
		TextAreaInputSettings config = settings();
		
		/*
		 * Backspace 
		 */
		
		if (config.isBackspaceEnabled() && key == GLFW.GLFW_KEY_BACKSPACE) {
			
			if (contentHandler.isContentHighlighted()) {
				contentHandler.deleteHighlightedContent();
			} else {
				contentHandler.backspaceAtCaret();
			}
			
			return;
		}
		
		/*
		 * Tab
		 */
		
		if (config.isTabEnabled() && key == GLFW.GLFW_KEY_TAB) {
			contentHandler.tabAtCaret();
			return;
		}
		
		/*
		 * Return
		 */
		
		if (key == GLFW.GLFW_KEY_ENTER) {
			if (config.returnEndsEditing()) {
				contentHandler.endEditing();
				return;
			}
			
			if (config.isReturnEnabled()) {
				contentHandler.newLineAtCaret();
				return;
			}
		}
		
		/*
		 * CTRL-[button] Commands
		 */
		
		if (window.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			/*
			 * Copy
			 */
			
			if (config.isCopyEnabled() && event.getKey() == GLFW.GLFW_KEY_C) {
				contentHandler.copySelectionToClipboard(window);
				return;
			}
			
			/*
			 * Cut
			 */
			
			if (config.isCutEnabled() && event.getKey() == GLFW.GLFW_KEY_X) {
				contentHandler.cutSelectionToClipboard(window);
				return;
			}
			
			/*
			 * Paste
			 */
			
			if (config.isPasteEnabled() && event.getKey() == GLFW.GLFW_KEY_V) {
				contentHandler.pasteClipboardAtCaret(window);
				return;
			}
			
			/*
			 * Bold
			 */
			
			if (config.isBoldEnabled() && event.getKey() == GLFW.GLFW_KEY_B) {
				contentHandler.boldenSelection();
				return;
			}
			
			/*
			 * Italic
			 */
			
			if (config.isItalicEnabled() && event.getKey() == GLFW.GLFW_KEY_I) {
				contentHandler.italicizeSelection();
				return;
			}
			
			/*
			 * Undo
			 */
			
			if (config.isUndoEnabled() && event.getKey() == GLFW.GLFW_KEY_Z) {
				contentHandler.undo();
				return;
			}
			
			/*
			 * Redo
			 */
			
			if (config.isRedoEnabled() && event.getKey() == GLFW.GLFW_KEY_Y) {
				contentHandler.redo();
				return;
			}
		}
		
		/*
		 * Move Caret
		 */
		
		if (config.isArrowKeysEnabled()) {
			//Move caret right
			if (key == GLFW.GLFW_KEY_RIGHT) {
				contentHandler.moveCaretRight();
				return;
			}
			
			//Move caret left
			if (key == GLFW.GLFW_KEY_LEFT) {
				contentHandler.moveCaretLeft();
				return;
			}
		}
		
	}
}
