package nokori.clear.vg;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.text.TextAreaAutoFormatterWidget;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

import static nokori.clear.vg.widget.text.ClearEscapeSequences.*;

public class ClearTextAreaSyntaxHighlightingDemo extends ClearTextAreaDemo {

	public static void main(String[] args) {
		ClearApplication.launch(new ClearTextAreaSyntaxHighlightingDemo(), args);
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		super.init(windowManager, window, context, rootWidgetAssembly, args);
		
		//Create the auto-formatting widget and add it to the text area we want it to operate on.
		TextAreaAutoFormatterWidget syntaxHighlighter = new TextAreaAutoFormatterWidget();
		textAreaWidget.addChild(syntaxHighlighter);
		
		//Create definitions for the highlighting. Keep in mind, the auto-formatter is a general use widget for TextAreaWidget's.
		//In our case, we're building it to work as a syntax highlighter.
		
		syntaxHighlighter.addSyntax("Test2", ESCAPE_SEQUENCE_COLOR, "#950055");
		
		/*syntaxHighlighter.addSyntax("public void", ESCAPE_SEQUENCE_COLOR + "#950055");
		syntaxHighlighter.addSyntax("blue", ESCAPE_SEQUENCE_COLOR + "#0026FF");
		syntaxHighlighter.addSyntax("//", ESCAPE_SEQUENCE_COLOR + "#3F7F5F", TextAreaAutoFormatterWidget.EscapeSequenceResetType.RESET_ON_NEW_LINE);*/
	}

	@Override
	protected String getText() {
		return "Test Test2 Test3 Test4";
		/*return "public void highlightingExample() {"
				+ "\n\t//This is a comment"
				+ "\n\tblue normal"
				+ "\n}";*/
	}
}
