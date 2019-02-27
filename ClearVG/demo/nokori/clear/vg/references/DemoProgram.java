package nokori.clear.vg.references;


import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.references.Demo.DemoData;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;
import nokori.clear.windows.util.WindowedApplication;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.opengl.GL11.*;

/**
 * Runs LWJGL3's Demo.java using Clear components.
 */
public class DemoProgram extends WindowedApplication {
	
	private NanoVGContext context;
	private DemoData data = new DemoData();
	
	private double mouseX, mouseY;

	public static void main(String args[]) {
		WindowedApplication.launch(new DemoProgram(), args);
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, String[] args) {
		context = new NanoVGContext().init();
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
		return windowManager.createWindow("NanoVG Demo", 50, 50, 512, 512, true, true);
	}

	@Override
	protected void endOfApplicationCallback() {
		Demo.freeDemoData();
		context.dispose();
	}

}
