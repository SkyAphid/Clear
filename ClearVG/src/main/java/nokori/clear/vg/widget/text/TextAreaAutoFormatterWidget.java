package nokori.clear.vg.widget.text;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nokori.clear.vg.widget.text.ClearEscapeSequences.*;

/**
 * 
 * This widget will automatically add escape sequences to the text based on user-designated settings. This effectively allows you to create systems such as 
 * syntax highlighters for your TextAreas. Keep in kind that escape sequences configured in this widget will be open to automatic editing, so there's a chance it will
 * also be removed automatically. Make sure to configure this widget to match your exact preferences if you intend on using it. Additionally, escape sequences that aren't 
 * added to this widget for management will be ignored entirely.
 * <br><br>
 * One more thing to keep in mind is that when this widget is active, format editing in the widget will be disabled so that the system doesn't confuse auto-formatted text 
 * with manually formatted text.
 */
public class TextAreaAutoFormatterWidget extends Widget {
	
	//When the formatter is added, we'll want to perform an initial refresh to sync it up to the widget
	//Otherwise, this widget is only updated when the parent is
	private boolean initialRefresh = false;
	private int cachedTextLength = -1;
	
	private ArrayList<Syntax> syntaxSettings = new ArrayList<Syntax>();
	
	private boolean autoAddEnabled = true;
	private boolean autoRemoveEnabled = true;
	
	public TextAreaAutoFormatterWidget() {}
	
	/**
	 * This constructor allows you to pass in existing syntax settings to allow them to be shared across formatter widgets.
	 * 
	 * @param syntaxSettings
	 */
	public TextAreaAutoFormatterWidget(ArrayList<Syntax> syntaxSettings) {
		this.syntaxSettings = syntaxSettings;
	}

	@Override
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		/*TextAreaWidget textAreaWidget = (TextAreaWidget) parent;
		TextAreaContentHandler h = textAreaWidget.getTextContentHandler();
		System.out.println("Caret position: " + h.getCaretPosition() + " (" + textAreaWidget.getTextBuilder().length() + ")");*/
		
		if (!initialRefresh) {
			refresh();
		}
	}

	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}
	
	/**
	 * This is called from TextAreaWidget's requestRefresh() function.
	 */
	
	private String cS;

	void refresh() {
		//System.err.println("Auto-Formatter: Refresh start");
		
		if (parent instanceof TextAreaWidget) {
			TextAreaWidget textAreaWidget = (TextAreaWidget) parent;
			TextAreaContentHandler h = textAreaWidget.getTextContentHandler();
			StringBuilder textBuilder = textAreaWidget.getTextBuilder();
			
			//Disables manual formatting so that the system isn't confused by manual formatting tags and the automatic ones added autonomously
			textAreaWidget.getInputSettings().setManualFormattingEnabled(false);
			
			/*
			 * Clean existing syntax settings
			 */
			
			for (int i = 0; i < syntaxSettings.size(); i++) {
				Syntax syntax = syntaxSettings.get(i);
				
				//System.out.println("Auto-Formatter: Checking syntax -> " + syntax.key + " (" + i + ")");
				
				char escapeSequence = syntax.escapeSequence;
				
				for (int j = 0; j < textBuilder.length(); j++) {
					char c = textBuilder.charAt(j);
					cS = Character.toString(c);
					
					if (h.getEscapeSequenceReplacements().containsKey(cS)) {
						cS = h.getEscapeSequenceReplacements().get(cS);
					}
					
					if (c == escapeSequence) {
						int deleted = deleteSequenceAt(textBuilder, j, false);
						
						if (j < h.getCaretPosition()) {
							adjustCaret(h, -deleted);
							//System.out.println("Auto-Formatter: Adjust caret (Deleted " + c + " -> " + cS + ") " + -deleted);
						}
						
						deleteResetEscapeSequenceAhead(textBuilder, j, (index, character) -> {
							if (index < h.getCaretPosition()) {
								adjustCaret(h, -1);
								//System.out.println("Auto-Formatter: Adjust caret (Deleted " + c + " -> " + cS +  ") -1");
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

				String replacement = escapeSequence + key;

				if (syntax.resetMode == SyntaxResetMode.RESET_AFTER_KEY) {
					replacement += ESCAPE_SEQUENCE_RESET;
				}
				
				//Adjust caret if needed
				replaceAll(h, textBuilder, key, replacement, syntax.resetMode);
			}

			//System.out.println("Auto-Formatter: Final caret position: " + h.getCaretPosition() + " (" + textAreaWidget.getTextBuilder().length() + ")");
		} else {
			System.err.println("WARNING: TextAreaAutoFormatterWidget (" + this + ") added to incompatble widget (" + parent + "). "
					+ "Please add this widget to a TextAreaWidget to experience proper functionality.");
		}
		
		initialRefresh = true;
	}
	
	private void replaceAll(TextAreaContentHandler h, StringBuilder sb, String regex, String replacement, SyntaxResetMode resetMode) {
	    Matcher m = Pattern.compile(Pattern.quote(regex)).matcher(sb);
	    int start = 0;
	    
	    while (m.find(start)) {
	    	int s = m.start();
	    	int e = m.end();

	    	if (m.group().equals(regex)) {
		    	//System.out.println("Auto-Formatter: replaceAll() match: Regex: " + regex + " Start: " + s + " End: " + e + " (" + sb.length() + ") = " + sb.substring(s, e) + " -> " + replacement);

		    	//Replace the string
		        sb.replace(s, e, replacement);
		        start = s + replacement.length();
		        
		        //Update caret location
		    	int caret = h.getCaretPosition();
		    	
		    	if ((caret > s || caret > e) && sb.length() > cachedTextLength) {
			    	int offset = (replacement.length() - regex.length());
			    	adjustCaret(h, offset);
					//System.out.println("Auto-Formatter: Adjust caret (Replaced " + regex + " with " + replacement + "): +" + offset);
		    	}
		    	
		    	//If the reset mode is after new line, find the next \n to put the reset at.
		    	if (resetMode == SyntaxResetMode.RESET_AFTER_NEW_LINE) {
		    		for (int i = e; i < sb.length(); i++) {
		    			if (sb.charAt(i) == '\n' && (i + 1 < sb.length() && sb.charAt(i + 1) != ESCAPE_SEQUENCE_RESET)) {
		    				if (h.getCaretPosition() > i) {
		    					h.offsetCaret(1);
		    				}
		    				
		    				sb.insert(i + 1, ESCAPE_SEQUENCE_RESET);
		    				break;
		    			}
		    		}
		    	}
	    	} else {
	    		System.err.println("Auto-Formatter: replaceAll() match failure: Regex: " + regex + " Matcher Group Result: " + m.group() + " (Does not match!)");
	    		System.err.println("This is likely due to incorrect regex! Skipping search to prevent infinite loop...");
		    	start = e;
	    	}
	    }
	}
	
	private void adjustCaret(TextAreaContentHandler h, int charsModified) {
		int offset = h.getCaretPosition() + charsModified;
		h.setCaretPosition(offset);
	}
	
	public Syntax addSyntax(String key, char escapeSequence) {
		return addSyntax(key, escapeSequence, null, SyntaxResetMode.RESET_AFTER_KEY);
	}
	
	public Syntax addSyntax(String key, char escapeSequence, String instructions) {
		return addSyntax(key, escapeSequence, instructions, SyntaxResetMode.RESET_AFTER_KEY);
	}
	
	public Syntax addSyntax(String key, char escapeSequence, String instructions, SyntaxResetMode resetMode) {
		Syntax s = new Syntax(key, escapeSequence, instructions, resetMode);
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

	public void clearAllSyntax() {
		syntaxSettings.clear();
	}
	
	public ArrayList<Syntax> getSyntaxSettings() {
		return syntaxSettings;
	}

	public void setSyntaxSettings(ArrayList<Syntax> syntaxSettings) {
		this.syntaxSettings = syntaxSettings;
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
		private SyntaxResetMode resetMode;
		
		public Syntax(String key, char escapeSequence, String instructions, SyntaxResetMode resetMode) {
			this.key = key;
			this.escapeSequence = escapeSequence;
			this.instructions = instructions;
			this.resetMode = resetMode;
		}

		public String getKey() {
			return key;
		}

		public char getEscapeSequence() {
			return escapeSequence;
		}

		public String getInstructions() {
			return instructions;
		}

		public SyntaxResetMode getResetMode() {
			return resetMode;
		}
	}
	
	public enum SyntaxResetMode {
		RESET_AFTER_KEY,
		RESET_AFTER_NEW_LINE
	}
}
