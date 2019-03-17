package nokori.clear.vg.widget.text;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

import static nokori.clear.vg.widget.text.ClearEscapeSequences.*;

/**
 * 
 * This widget will automatically add escape sequences to the text based on user-designated settings. This effectively allows you to create systems such as 
 * syntax highlighters for your TextAreas. Keep in kind that escape sequences configured in this widget will be open to automatic editing, so there's a chance it will
 * also be removed automatically. Make sure to configure this widget to match your exact preferences if you intend on using it. Additionally, escape sequences that aren't 
 * added to this widget for management will be ignored entirely.
 * 
 */
public class TextAreaAutoFormatterWidget extends Widget {
	
	//When the formatter is added, we'll want to perform an initial refresh to sync it up to the widget
	//Otherwise, this widget is only updated when the parent is
	private boolean initialRefresh = false;
	private int cachedTextLength = -1;
	
	private ArrayList<Syntax> syntaxSettings = new ArrayList<Syntax>();
	
	private boolean autoAddEnabled = true;
	private boolean autoRemoveEnabled = true;

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		/*TextAreaWidget textAreaWidget = (TextAreaWidget) parent;
		TextAreaContentHandler h = textAreaWidget.getTextContentHandler();
		System.out.println("Caret position: " + h.getCaretPosition() + " (" + textAreaWidget.getTextBuilder().length() + ")");*/
		
		if (!initialRefresh) {
			refresh();
		}
	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}
	
	
	/**
	 * This is called from TextAreaWidget's requestRefresh() function.
	 */
	void refresh() {
		if (parent instanceof TextAreaWidget) {
			TextAreaWidget textAreaWidget = (TextAreaWidget) parent;
			TextAreaContentHandler h = textAreaWidget.getTextContentHandler();
			StringBuilder textBuilder = textAreaWidget.getTextBuilder();
			
			/*
			 * Clean existing syntax settings
			 */

			for (int i = 0; i < syntaxSettings.size(); i++) {
				Syntax syntax = syntaxSettings.get(i);
				
				char escapeSequence = syntax.escapeSequence;
				
				for (int j = 0; j < textBuilder.length(); j++) {
					if (textBuilder.charAt(j) == escapeSequence) {
						int deleted = deleteSequenceAt(textBuilder, j, false);
						
						if (j <= h.getCaretPosition()) {
							adjustCaret(h, -deleted);
							//System.out.println("Adjust caret 1 " + -deleted);
						}
						
						deleteResetEscapeSequenceAhead(textBuilder, j, (index, c) -> {
							if (index <= h.getCaretPosition()) {
								adjustCaret(h, -1);
								//System.out.println("Adjust caret 2");
							}
						});
					}
				}
			}
			
			cachedTextLength = textBuilder.length();

			/*
			 * Go through all syntax settings and apply them
			 */
			
			for (int i = 0; i < syntaxSettings.size(); i++) {
				Syntax syntax = syntaxSettings.get(i);
				
				String key = syntax.key;
				String escapeSequence = Character.toString(syntax.escapeSequence);
				
				if (syntax.instructions != null) {
					escapeSequence += syntax.instructions;
				}

				String replacement = escapeSequence + key + ESCAPE_SEQUENCE_RESET;

				//Adjust caret if needed
				replaceAll(h, textBuilder, key, replacement);
			}

			//System.err.println("Final caret position: " + h.getCaretPosition() + " (" + textAreaWidget.getTextBuilder().length() + ")");
		} else {
			System.err.println("WARNING: TextAreaAutoFormatterWidget (" + this + ") added to incompatble widget (" + parent + "). "
					+ "Please add this widget to a TextAreaWidget to experience proper functionality.");
		}
		
		initialRefresh = true;
	}
	
	private void replaceAll(TextAreaContentHandler h, StringBuilder sb, String regex, String replacement) {
	    Matcher m = Pattern.compile(regex).matcher(sb);
	    int start = 0;
	    
	    while (m.find(start)) {
	    	int s = m.start();
	    	int e = m.end();

	    	//Replace the string
	        sb.replace(s, e, replacement);
	        start = m.start() + replacement.length();
	        
	        //Update caret location
	    	int caret = h.getCaretPosition();
	    	
	    	if ((caret > s || caret > e) && sb.length() > cachedTextLength) {
		    	int offset = (replacement.length() - regex.length());
		    	adjustCaret(h, offset);
				//System.out.println("Adjust caret 3: +" + offset);
	    	}
	    }
	}
	
	private void adjustCaret(TextAreaContentHandler h, int charsModified) {
		int offset = h.getCaretPosition() + charsModified;
		h.setCaretPosition(offset);
	}
	
	public Syntax addSyntax(String key, char escapeSequence) {
		return addSyntax(key, escapeSequence, null);
	}
	
	public Syntax addSyntax(String key, char escapeSequence, String instructions) {
		Syntax s = new Syntax(key, escapeSequence, instructions);
		syntaxSettings.add(s);
		return s;
	}
	
	public void addSyntax(Syntax syntax) {
		syntaxSettings.add(syntax);
	}
	
	public boolean removeSyntax(Syntax syntax) {
		return syntaxSettings.remove(syntax);
	}
	
	public boolean removeSyntax(String key) {
		boolean removed = false;
		
		for (int i = 0; i < syntaxSettings.size(); i++) {
			if (syntaxSettings.get(i).key.equals(key)) {
				syntaxSettings.remove(i);
				removed = true;
				i--;
			}
		}
		
		return removed;
	}

	/**
	 * 
	 * This setting determines whether or not this auto-formatter will automatically add formatting to the TextAreaWidget (such as when syntax becomes present via editing). 
	 * If false, then no new escape sequences will be added.
	 * 
	 * @return true if auto-adding is enabled
	 */
	public boolean isAutoAddEnabled() {
		return autoAddEnabled;
	}

	/**
	 * @see <code>isAutoAddEnabled()</code>
	 * @param autoAddEnabled
	 */
	public void setAutoAddEnabled(boolean autoAddEnabled) {
		this.autoAddEnabled = autoAddEnabled;
	}

	/**
	 * 
	 * This setting determines whether or not this auto-formatter will automatically remove formatting in the TextAreaWidget (e.g. when the corresponding syntax is no longer present). 
	 * If false, then no escape sequences will be removed automatically.
	 * 
	 * @return true if auto-removing is enabled
	 */
	public boolean isAutoRemoveEnabled() {
		return autoRemoveEnabled;
	}

	/**
	 * @see <code>isAutoRemoveEnabled()</code>
	 * @param autoRemoveEnabled
	 */
	public void setAutoRemoveEnabled(boolean autoRemoveEnabled) {
		this.autoRemoveEnabled = autoRemoveEnabled;
	}
	
	@Override
	public void dispose() {}
	
	public class Syntax {
		private String key;
		private char escapeSequence;
		private String instructions;
		
		public Syntax(String key, char escapeSequence, String instructions) {
			this.key = key;
			this.escapeSequence = escapeSequence;
			this.instructions = instructions;
		}
	}
}
