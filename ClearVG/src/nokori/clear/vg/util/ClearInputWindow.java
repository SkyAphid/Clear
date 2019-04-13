package nokori.clear.vg.util;

import nokori.clear.vg.ClearApp;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.RootWidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class ClearInputWindow extends ClearApp {
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 250;
	
	private String title;
	
	private ClearInputWindow(WindowManager windowManager, WidgetAssembly rootWidgetAssembly) {
		super(windowManager, rootWidgetAssembly);
	}

	public static ClearInputWindow show(ClearApp parent, String title, String message, String defaultInput) throws GLFWException {
		ClearInputWindow c = new ClearInputWindow(parent.getWindowManager(), new RootWidgetAssembly());
		c.title = title;
		
		launch(c, null, false);
		
		return c;
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		
	}

	@Override
	protected void endOfNanoVGApplicationCallback() {
		
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		return windowManager.createWindow(title, WIDTH, HEIGHT, false, true);
	}

	@Override
	protected boolean exitProgramOnEndOfApplication() {
		return false;
	}
}
