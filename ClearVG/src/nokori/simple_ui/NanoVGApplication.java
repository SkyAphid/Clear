package nokori.simple_ui;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.opengl.GL11.*;

import nokori.simple_windows.Window;
import nokori.simple_windows.WindowManager;
import nokori.simple_windows.util.WindowedApplication;

public abstract class NanoVGApplication extends WindowedApplication {

	private NanoVGContext context;
	
	@Override
	public void init(WindowManager windowManager, Window window, String[] args) {
		context.init();
		init(windowManager, window, context, args);
	}
	
	public abstract void init(WindowManager windowManager, Window window, NanoVGContext context, String[] args);

	@Override
	public void run() {
		glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
		glClearColor(1, 1, 1, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		
        float pxRatio = window.getFramebufferWidth() / (float) window.getHeight();
        nvgBeginFrame(context.get(), window.getWidth(), window.getHeight(), pxRatio);
		
        render(windowManager, window, context);
        
        nvgEndFrame(context.get());
	}

	
	public abstract void render(WindowManager windowManager, Window window, NanoVGContext context);

	@Override
	protected void endOfApplicationCallback() {
		context.dispose();
	}

	protected abstract void endOfNanoVGApplicationCallback();
}
