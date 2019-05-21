package nokori.clear.vg.widget;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.ClearStaticResources;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.transition.FillTransition;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetClip;
import nokori.clear.windows.Cursor;

public class ButtonAssembly extends WidgetAssembly {
	
	public static final ClearColor DEFAULT_OUTLINE_COLOR = ClearColor.GRAY;
	
	public ButtonAssembly(float width, float height, ClearColor fill, ClearColor outlineFill, float cornerRadius, Font font, int fontSize, ClearColor fontColor, String label) {
		this(0f, 0f, width, height, fill, outlineFill, cornerRadius, font, fontSize, fontColor, label);
	}
	
	public ButtonAssembly(float x, float y, float width, float height, ClearColor fill, ClearColor outlineFill, float cornerRadius, Font font, int fontSize, ClearColor fontColor, String label) {
		super(x, y, width, height);
		
		/*
		 * Background - rectangle with dropshadow
		 */

		addChild(new DropShadowWidget(cornerRadius));
		
		RectangleWidget background = new RectangleWidget(cornerRadius, fill, DEFAULT_OUTLINE_COLOR.copy(), true);
		addChild(background);
		
		/*
		 * Text
		 */
		
		LabelWidget labelWidget = new LabelWidget(fontColor, label, font, FontStyle.REGULAR, fontSize);
		labelWidget.addChild(new WidgetClip(WidgetClip.Alignment.CENTER));
		addChild(labelWidget);
		
		/*
		 * Input
		 */
		
		setOnInternalMouseEnteredEvent(e -> {
			ClearStaticResources.getCursor(Cursor.Type.HAND).apply(e.getWindow());
			
			FillTransition outlineFader = new FillTransition(200, background.getStrokeFill(), outlineFill);
			outlineFader.play();
		});
		
		setOnInternalMouseExitedEvent(e -> {
			ClearStaticResources.getCursor(Cursor.Type.ARROW).apply(e.getWindow());
			
			FillTransition outlineFader = new FillTransition(200, background.getStrokeFill(),  DEFAULT_OUTLINE_COLOR);
			outlineFader.play();
		});
	}
}
