package nokori.clear.vg;

import nokori.clear.vg.font.Font;
import nokori.clear.vg.widget.DropShadowWidget;
import nokori.clear.vg.widget.RectangleWidget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetClip;
import nokori.clear.vg.widget.text.TextFieldWidget;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

import java.io.IOException;

/**
 * This demo show-cases the TextArea functionality available in ClearVG.
 *
 */
public class ClearTextFieldDemo extends ClearApp {

	private static final int WINDOW_WIDTH = 512;
	private static final int WINDOW_HEIGHT = 256;
	
	protected TextFieldWidget textFieldWidget;
	
	public static void main(String[] args) {
		ClearApp.launch(new ClearTextFieldDemo(), args);
	}

	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		//WidgetAssemblies act as containers for various widgets. This will allow you to "assemble" a variety of UI components.
		WidgetAssembly assembly = new WidgetAssembly(450, 100, new WidgetClip(WidgetClip.Alignment.CENTER));
		
		assembly.addChild(new DropShadowWidget());
		assembly.addChild(new RectangleWidget(ClearColor.WHITE_SMOKE, ClearColor.LIGHT_GRAY, true));
		
		try {
			Font font = new Font("fonts/NotoSans/", "NotoSans-Regular", "NotoSans-Bold", "NotoSans-Italic", "NotoSans-Light").load(context);

			textFieldWidget = new TextFieldWidget(350, ClearColor.LIGHT_BLACK, getText(), font, 18);
			textFieldWidget.setBackgroundFill(ClearColor.WHITE_SMOKE.multiply(0.9f));
			textFieldWidget.setUnderlineFill(ClearColor.LIGHT_BLACK);
			
			textFieldWidget.addChild(new WidgetClip(WidgetClip.Alignment.CENTER));
			assembly.addChild(textFieldWidget);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		rootWidgetAssembly.addChild(assembly);
	}

	protected String getText() {
		return "This is a text field! Try typing!";
	}

	@Override
	protected void endOfNanoVGApplicationCallback() {
		
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		return windowManager.createWindow(getTitle(), WINDOW_WIDTH, WINDOW_HEIGHT, true, true);
	}

	public String getTitle() {
		return "Clear TextFieldWidget Demo";
	}
}
