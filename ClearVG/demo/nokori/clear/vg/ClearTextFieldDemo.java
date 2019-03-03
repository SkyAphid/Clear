package nokori.clear.vg;

import nokori.clear.vg.ClearColor;

import java.io.IOException;
import nokori.clear.vg.ClearApplication;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.text_rendering.CommandSetFill;
import nokori.clear.vg.text_rendering.CommandSetFont;
import nokori.clear.vg.widget.DropShadow;
import nokori.clear.vg.widget.Rectangle;
import nokori.clear.vg.widget.TextArea;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetClip;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class ClearTextFieldDemo extends ClearApplication {

	private static final int WINDOW_WIDTH = 1280;
	private static final int WINDOW_HEIGHT = 720;
	
	public static void main(String[] args) {
		ClearApplication.launch(new ClearTextFieldDemo(), args);
	}

	public ClearTextFieldDemo() {
		super(new WidgetAssembly());
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		//WidgetAssemblies act as containers for various widgets. This will allow you to "assemble" a variety of UI components.
		WidgetAssembly button = new WidgetAssembly(1000, 500, new WidgetClip(WidgetClip.Alignment.CENTER));
		
		button.addChild(new DropShadow(ClearColor.LIGHT_BLACK));
		button.addChild(new Rectangle(ClearColor.WHITE_SMOKE, ClearColor.LIGHT_GRAY));

		try {
			Font font = new Font("fonts/NotoSans/", "NotoSans-Regular", "NotoSans-Bold", "NotoSans-Italic", "NotoSans-Light").load(context);

			TextArea textArea = new TextArea(900, 400, ClearColor.BLACK, getText(), font, 18);
			textArea.getTextContentRenderer().addCommand(new CommandSetFont(0, 11, font, FontStyle.BOLD));
			textArea.getTextContentRenderer().addCommand(new CommandSetFont(13, 35, font, FontStyle.ITALIC));
			textArea.getTextContentRenderer().addCommand(new CommandSetFill(0, 35, ClearColor.CORAL));
			
			textArea.addChild(new WidgetClip(WidgetClip.Alignment.CENTER));
			button.addChild(textArea);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		rootWidgetAssembly.addChild(button);
	}

	private String getText() {
		String s = "";
		
		for (int i = 0; i < 50; i++) {
			if (i > 0) {
				s += "\n\n";
			}
			
			s += "Hello World! This is entry number " + i + ". ";
			s += "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ";
			s += "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ";
			s += "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.";
		}
		
		return s;
	}

	@Override
	protected void endOfNanoVGApplicationCallback() {
		
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		return windowManager.createWindow("Hello World!", WINDOW_WIDTH, WINDOW_HEIGHT, true, true);
	}

}
