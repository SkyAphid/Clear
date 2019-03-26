package nokori.clear.vg.widget.text;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

/**
 * This class is an extension of TextAreaWidget that simplifies its functionality down into just being a one-line field input.
 */
public class TextFieldWidget extends TextAreaWidget {
	
	/**
	 * This constructor allows the widget to be initialized without the NanoVGContext but comes at the cost of 
	 * not initializing the widget height right away (it will be zero until <code>tick()</code> is called for the first time).
	 */
	public TextFieldWidget(float width, ClearColor fill, String text, Font font, float fontSize) {
		this(0, 0, width, fill, text, font, fontSize);
	}
	
	/**
	 * This constructor allows the widget to be initialized without the NanoVGContext but comes at the cost of 
	 * not initializing the widget height right away (it will be zero until <code>tick()</code> is called for the first time).
	 */
	public TextFieldWidget(float x, float y, float width, ClearColor fill, String text, Font font, float fontSize) {
		this(null, x, y, width, fill, text, font, fontSize);
	}
	
	/**
	 * This shortened version of the primary constructor will calculate the widget height automatically by using the NanoVGContext.
	 */
	public TextFieldWidget(NanoVGContext context, float width, ClearColor fill, String text, Font font, float fontSize) {
		this(context, 0, 0, width, fill, text, font, fontSize);
	}
	
	/**
	 * This is the primary constructor which allows full and complete customization of the TextFieldWidget. Additionally, the widget's height will be 
	 * calculated at initialization thanks to the inclusion of a NanoVGContext.
	 */
	public TextFieldWidget(NanoVGContext context, float x, float y, float width, ClearColor fill, String text, Font font, float fontSize) {
		super(x, y, width, 0f, fill, text, font, fontSize);
		
		if (context != null) {
			calculateHeight(context);
		}
		
		setWordWrappingEnabled(false);
		setLineNumbersEnabled(false);
		
		getInputSettings().setVerticalScrollbarEnabled(false);
		getInputSettings().setHorizontalScrollbarEnabled(false);
		getInputSettings().setReturnEnabled(false);
		getInputSettings().setTabEnabled(false);
	}


	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		super.tick(windowManager, window, context, rootWidgetAssembly);
		calculateHeight(context);
	}

	/**
	 * Calculates the height for this TextFieldWidget by getting the height of the font configured to this widget.
	 * This is called automatically in the <code>tick()</code> function, however it can be called manually in instances 
	 * where you're initializing it in a constructor and want the most up-to-date data immediately.
	 */
	public void calculateHeight(NanoVGContext context) {
		setHeight(getFont().getHeight(context, getFontSize(), TEXT_AREA_ALIGNMENT, getDefaultFontStyle()));
	}
}
