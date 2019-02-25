package nokori.simple_ui.demos;

import org.lwjgl.glfw.GLFW;

import nokori.simple_ui.NanoVGContext;
import nokori.simple_ui.demos.Demo.DemoData;
import nokori.simple_windows.GLFWException;
import nokori.simple_windows.Window;
import nokori.simple_windows.WindowManager;
import nokori.simple_windows.callback.MouseCallback;
import nokori.simple_windows.util.WindowedApplication;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.opengl.GL11.*;

public class DemoRun extends WindowedApplication {
	
	private NanoVGContext context;
	private Demo demo;
	private DemoData data = new DemoData();
	
	private double mouseX, mouseY;

	public static void main(String args[]) {
		WindowedApplication.launch(new DemoRun(), args);
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
		
        glfwSetTime(0);
	}

	@Override
	public void run() {
		glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
		glClearColor(1, 1, 1, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		
        float pxRatio = window.getFramebufferWidth() / (float) window.getHeight();
        nvgBeginFrame(context.get(), window.getWidth(), window.getHeight(), pxRatio);
		
		Demo.renderDemo(context.get(), (float) mouseX, (float) mouseY, window.getWidth(), window.getHeight(), (float) glfwGetTime(), false, data);
		
        nvgEndFrame(context.get());
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		return windowManager.createWindow(50, 50, 512, 512, true, true);
	}

	@Override
	protected void endOfApplicationCallback() {
		context.dispose();
	}

}
