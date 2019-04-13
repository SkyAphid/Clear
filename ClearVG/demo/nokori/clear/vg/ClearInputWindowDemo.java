package nokori.clear.vg;

import nokori.clear.vg.ClearApp;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.util.ClearInputWindow;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class ClearInputWindowDemo extends ClearHelloWorldDemo {

	public static void main(String[] args) {
		ClearApp.launch(new ClearInputWindowDemo(), args);
	}

	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		super.init(windowManager, window, context, rootWidgetAssembly, args);
		
		button.setOnMouseButtonEvent(e -> {
			if (button.isMouseWithin() && e.isPressed()) {
				
				try {
					ClearInputWindow.show(this, "Input Window Demo", "Input something here:", "Default text");
				} catch (GLFWException e1) {
					e1.printStackTrace();
				}
			}
		});
	}
}
