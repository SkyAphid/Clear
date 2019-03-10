package nokori.clear.vg.widget.text;

import org.lwjgl.glfw.GLFW;

import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;

/**
 * A wrapper that handles any content-related inputs for TextAreaWidget. Mostly separated to prevent the TextAreaWidget from ballooning beyond sustainable ranges, this class
 * allows you to quickly access and edit any content-editing behaviors for the widget.
 */
public class TextAreaContentInputHandler {
	
	public static void charEvent(TextAreaWidget widget, TextAreaContentHandler textContentHandler, StringBuilder textBuilder, CharEvent event) {
		if (!widget.isEditingEnabled()) return;
		
		int caretPos = textContentHandler.getCaretPosition();
		
		if (caretPos >= 0 && caretPos <= textBuilder.length()) {
			textBuilder.insert(caretPos, event.getCharString());
			textContentHandler.textInsertedCallback(caretPos, true);
			widget.requestRefresh();
		}
	}
	
	public static void keyEvent(TextAreaWidget widget, TextAreaContentHandler textContentHandler, StringBuilder textBuilder, KeyEvent event) {
		if (!event.isPressed()) return;
		
		/*
		 * Backspace 
		 */
		
		if (event.getKey() == GLFW.GLFW_KEY_BACKSPACE) {
			backspace(widget, textContentHandler, textBuilder);
		}
		
		/*
		 * Move Cursor
		 */
		
		//Move cursor right
		if (event.getKey() == GLFW.GLFW_KEY_RIGHT) {
			
		}
		
		//Move cursor left
		if (event.getKey() == GLFW.GLFW_KEY_LEFT) {
			
		}
		
		//Move cursor down
		if (event.getKey() == GLFW.GLFW_KEY_DOWN) {
			
		}
	}
	
	public static void backspace(TextAreaWidget widget, TextAreaContentHandler textContentHandler, StringBuilder textBuilder) {
		int caretPos = textContentHandler.getCaretPosition();
		textBuilder.deleteCharAt(caretPos-1);
		textContentHandler.textInsertedCallback(caretPos, false);
		widget.requestRefresh();
	}
	

}
