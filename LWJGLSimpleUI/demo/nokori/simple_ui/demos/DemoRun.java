package nokori.simple_ui.demos;

import nokori.simple_ui.NanoVGContext;
import nokori.simple_ui.demos.Demo.DemoData;
import nokori.simple_windows.GLFWException;
import nokori.simple_windows.Window;
import nokori.simple_windows.WindowManager;
import nokori.simple_windows.callback.MouseCallback;
import nokori.simple_windows.util.SimpleApplication;

public class DemoRun extends SimpleApplication {
	
	private NanoVGContext context;
	private Demo demo;
	
	private double mouseX, mouseY;

	public static void main(String args[]) {
		SimpleApplication.launch(new DemoRun(), args);
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, String[] args) {
		context = new NanoVGContext().init();
		demo = new Demo();

		window.addInputCallback(new MouseCallback() {
			@Override
			public void mouseEvent(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods) {
				DemoRun.this.mouseX = mouseX;
				DemoRun.this.mouseY = mouseY;
			}
		});
	}

	@Override
	public void run() {
		Demo.renderDemo(context.get(), (float) mouseX, (float) mouseY, 512, 512, 1f, true, new DemoData());
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		return windowManager.createWindow(50, 50, 512, 512, true, true);
	}

	@Override
	protected void dispose() {
		context.dispose();
	}

}
