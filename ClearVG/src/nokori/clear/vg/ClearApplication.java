package nokori.clear.vg;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.opengl.GL11.*;

import org.joml.Vector4f;

import nokori.clear.vg.transition.TransitionManager;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;
import nokori.clear.windows.callback.CharCallback;
import nokori.clear.windows.callback.KeyCallback;
import nokori.clear.windows.callback.MouseButtonCallback;
import nokori.clear.windows.callback.MouseMotionCallback;
import nokori.clear.windows.callback.MouseScrollCallback;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;
import nokori.clear.windows.event.MouseScrollEvent;
import nokori.clear.windows.util.WindowedApplication;

/**
 * An application wrapper that allows users to quickly create ClearVG applications.
 */
public abstract class ClearApplication extends WindowedApplication {

	private Vector4f bgClearColor = new Vector4f(1f, 1f, 1f, 1f);
	
	private NanoVGContext context;

	private WidgetAssembly rootWidgetAssembly;
	
	public ClearApplication(WidgetAssembly rootWidgetAssembly) {
		this.rootWidgetAssembly = rootWidgetAssembly;
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, String[] args) {
		SharedStaticVariables.loadAllCursors();
		addInputCallbacks(window, rootWidgetAssembly);
		context = new NanoVGContext().init();
		init(windowManager, window, context, rootWidgetAssembly, args);
	}
	
	public abstract void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args);

	@Override
	public void run() {
		/*
		 * Ticking
		 */
		
		TransitionManager.tick();
		rootWidgetAssembly.tick(windowManager, window, context, rootWidgetAssembly);
		rootWidgetAssembly.tickChildren(windowManager, window, context, rootWidgetAssembly);
		
		/*
		 * Rendering
		 */
		
		glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
		glClearColor(bgClearColor.x, bgClearColor.y, bgClearColor.z, bgClearColor.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		
        float pxRatio = window.getFramebufferWidth() / (float) window.getHeight();
        nvgBeginFrame(context.get(), window.getWidth(), window.getHeight(), pxRatio);
		
        rootWidgetAssembly.render(windowManager, window, context, rootWidgetAssembly);
        rootWidgetAssembly.renderChildren(windowManager, window, context, rootWidgetAssembly);
        
        nvgEndFrame(context.get());
	}

	@Override
	protected void endOfApplicationCallback() {
		endOfNanoVGApplicationCallback();
		SharedStaticVariables.destroyAllCursors();
		rootWidgetAssembly.dispose();
		context.dispose();
	}

	protected abstract void endOfNanoVGApplicationCallback();

	public WidgetAssembly getRootWidgetAssembly() {
		return rootWidgetAssembly;
	}
	
	private void addInputCallbacks(Window window, WidgetAssembly rootWidgetAssembly) {
		//Char events
		window.addInputCallback(new CharCallback() {
			
			@Override
			public void charEvent(Window window, long timestamp, int codepoint, String c, int mods) {
				CharEvent event = CharEvent.fire(window, timestamp, codepoint, c, mods);
				rootWidgetAssembly.charEvent(event);
				rootWidgetAssembly.childrenCharEvent(event);
			}
			
		});
		
		//Key events
		window.addInputCallback(new KeyCallback() {
			
			@Override
			public void keyEvent(Window window, long timestamp, int key, int scanCode, boolean pressed, boolean repeat, int mods) {
				KeyEvent event = KeyEvent.fire(window, timestamp, key, scanCode, pressed, repeat, mods);
				rootWidgetAssembly.keyEvent(event);
				rootWidgetAssembly.childrenKeyEvent(event);
			}
			
		});
		
		//Mouse button events
		window.addInputCallback(new MouseButtonCallback() {

			@Override
			public void mouseButtonEvent(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods) {
				MouseButtonEvent event = MouseButtonEvent.fire(window, timestamp, mouseX, mouseY, button, pressed, mods);
				rootWidgetAssembly.mouseButtonEvent(event);
				rootWidgetAssembly.childrenMouseButtonEvent(event);
			}
			
		});
		
		//Mouse motion events
		window.addInputCallback(new MouseMotionCallback() {

			@Override
			public void mouseMotionEvent(Window window, long timestamp, double mouseX, double mouseY, double dx, double dy) {
				MouseMotionEvent event = MouseMotionEvent.fire(window, timestamp, mouseX, mouseY, dx, dy);
				rootWidgetAssembly.mouseMotionEvent(event);
				rootWidgetAssembly.childrenMouseMotionEvent(event);
			}
			
		});
		
		//Mouse scroll events
		window.addInputCallback(new MouseScrollCallback() {

			@Override
			public void scrollEvent(Window window, long timestamp, double mouseX, double mouseY, double xoffset, double yoffset) {
				MouseScrollEvent event = MouseScrollEvent.fire(window, timestamp, mouseX, mouseY, xoffset, yoffset);
				rootWidgetAssembly.mouseScrollEvent(event);
				rootWidgetAssembly.childrenMouseScrollEvent(event);
			}
			
		});
	}
}
