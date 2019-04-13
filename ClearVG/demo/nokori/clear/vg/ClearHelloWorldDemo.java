package nokori.clear.vg;

import nokori.clear.vg.ClearColor;

import java.io.IOException;

import nokori.clear.vg.ClearApp;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.ClearStaticResources;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.widget.DropShadowWidget;
import nokori.clear.vg.widget.LabelWidget;
import nokori.clear.vg.widget.RectangleWidget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetClip;
import nokori.clear.windows.Cursor;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class ClearHelloWorldDemo extends ClearApp {

	private static final int WINDOW_WIDTH = 256;
	private static final int WINDOW_HEIGHT = 256;
	
	protected WidgetAssembly button = new WidgetAssembly(100, 50, new WidgetClip(WidgetClip.Alignment.CENTER));
	
	public static void main(String[] args) {
		ClearApp.launch(new ClearHelloWorldDemo(), args);
	}

	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		
		/*
		 * WidgetAssemblies act as containers for various widgets. This will allow you  to "assemble" a variety of UI components. 
		 * 
		 * Note: I recommend building classes that do the following assembly for you instead of redoing this entire process
		 * every single time. Normally I'd provide classes that do this for you, but the goal of Clear is to be somewhat lightweight. 
		 * It's best that you make your own implementations that fit your specific needs perfectly.
		 * 
		 * E.G. Make a class that extends WidgetAssembly and then add all of the children in its constructor.
		 * 
		 * If you're interested in seeing a real use-case of Clear and how I personally use it, check out my program, JDialogue, here:
		 * https://github.com/SkyAphid/JDialogue/
		 */
		
		button = new WidgetAssembly(100, 50, new WidgetClip(WidgetClip.Alignment.CENTER));
		
		/*
		 * Background - rectangle with dropshadow
		 */
		
		float cornerRadius = 3f;
		
		button.addChild(new DropShadowWidget(cornerRadius));
		button.addChild(new RectangleWidget(cornerRadius, ClearColor.CORAL, true));
		
		/*
		 * Text
		 */
		
		try {
			Font font = new Font("fonts/NotoSans/", "NotoSans-Regular", "NotoSans-Bold", "NotoSans-Italic", "NotoSans-Light").load(context);
			
			LabelWidget label = new LabelWidget(ClearColor.WHITE_SMOKE, "Hello World!", font, FontStyle.REGULAR, 20);
			label.addChild(new WidgetClip(WidgetClip.Alignment.CENTER));
			button.addChild(label);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		/*
		 * Input - click the WidgetAssembly to toggle rendering and show the bounding
		 */
		
		button.setOnMouseMotionEvent(e -> {
			if (button.isMouseIntersecting(window)) {
				ClearStaticResources.getCursor(Cursor.Type.HAND).apply(window);
			} else {
				ClearStaticResources.getCursor(Cursor.Type.ARROW).apply(window);
			}
		});
		
		button.setOnMouseButtonEvent(e -> {
			if (e.isPressed() && button.isMouseIntersecting(e.getWindow())) {
				button.setBackgroundFill(button.getBackgroundFill() != null ? null : ClearColor.LIGHT_GRAY);
				button.setRenderChildren(!button.isRenderingChildren());
			}
		});
		
		/*
		 * Add button to root assembly
		 */
		
		rootWidgetAssembly.addChild(button);
	}


	@Override
	protected void endOfNanoVGApplicationCallback() {
		
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		return windowManager.createWindow("Clear", WINDOW_WIDTH, WINDOW_HEIGHT, true, true);
	}

}
