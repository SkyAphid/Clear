package nokori.clear.vg.widget.text;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.font.Font;

/**
 * This class is an extension of TextAreaWidget that simplifies its functionality down into just being a one-line field input.
 */
public class TextFieldWidget extends TextAreaWidget {
	
	public TextFieldWidget(float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		this(0, 0, width, height, fill, text, font, fontSize);
	}

	public TextFieldWidget(float x, float y, float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		super(x, y, width, height, fill, text, font, fontSize);
		
		setWordWrappingEnabled(false);
		setLineNumbersEnabled(false);
		
		getInputSettings().setVerticalScrollbarEnabled(false);
		getInputSettings().setHorizontalScrollbarEnabled(false);
		getInputSettings().setReturnEnabled(false);
		getInputSettings().setTabEnabled(false);
	}

}
