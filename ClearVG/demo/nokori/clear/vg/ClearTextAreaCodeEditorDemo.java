package nokori.clear.vg;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.text.TextAreaAutoFormatterWidget;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

import static nokori.clear.vg.widget.text.ClearEscapeSequences.*;

/**
 * This demo extends the TextArea demo and re-purposes it into a code-editor.
 */
public class ClearTextAreaCodeEditorDemo extends ClearTextAreaDemo {

	public static void main(String[] args) {
		ClearApplication.launch(new ClearTextAreaCodeEditorDemo(), args);
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		super.init(windowManager, window, context, rootWidgetAssembly, args);
		
		//Disable word wrapping for a more code-editor-ish look
		textAreaWidget.setWordWrappingEnabled(false);
		
		//Create the auto-formatting widget and add it to the text area we want it to operate on.
		TextAreaAutoFormatterWidget syntaxHighlighter = new TextAreaAutoFormatterWidget();
		textAreaWidget.addChild(syntaxHighlighter);
		
		//Create definitions for the highlighting. Keep in mind, the auto-formatter is a general use widget for TextAreaWidgets.
		//In our case, we're building it to work as a syntax highlighter.	
		syntaxHighlighter.addSyntax("public void", ESCAPE_SEQUENCE_COLOR, "#950055");
		syntaxHighlighter.addSyntax("blue", ESCAPE_SEQUENCE_COLOR, "#0026FF");
		syntaxHighlighter.addSyntax("bold", ESCAPE_SEQUENCE_BOLD);
		syntaxHighlighter.addSyntax("italic", ESCAPE_SEQUENCE_ITALIC);
		syntaxHighlighter.addSyntax("light", ESCAPE_SEQUENCE_LIGHT);
		syntaxHighlighter.addSyntax("//", ESCAPE_SEQUENCE_COLOR, "#3F7F5F", TextAreaAutoFormatterWidget.SyntaxResetMode.RESET_AFTER_NEW_LINE);
	}

	@Override
	protected String getText() {
		return "public void highlightingExample() {"
				+ "\n\t//This is a comment"
				+ "\n\tblue bold italic light normal"
				+ "\n}";
	}
}
