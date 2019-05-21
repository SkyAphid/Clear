package nokori.clear.vg.widget.text;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.util.NanoVGScaler;

/**
 * This is an extension of TextAreaWidget that creates a TextAreaWidget that is viewable, but not editable in anyway. This is for cases where you want rich text labels that have formatting,
 * but you don't want to allow the users to edit the area.
 */
public class ObservableTextAreaWidget extends TextAreaWidget {
	
	public ObservableTextAreaWidget(ClearColor fill, String text, Font font, float fontSize) {
		super(fill, text, font, fontSize);
		makeImmutable();
	}
	
	public ObservableTextAreaWidget(float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		super(width, height, fill, text, font, fontSize);
		makeImmutable();
	}
	public ObservableTextAreaWidget(float x, float y, float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		this(x, y, width, height, new NanoVGScaler(), fill, text, font, fontSize);
	}

	public ObservableTextAreaWidget(float x, float y, float width, float height, NanoVGScaler scaler, ClearColor fill, String text, Font font, float fontSize) {
		super(x, y, width, height, scaler, fill, text, font, fontSize);
		makeImmutable();
	}
	
	private void makeImmutable() {
		getInputSettings().setEditingEnabled(false);
		getInputSettings().setHighlightingEnabled(false);
	}
}
