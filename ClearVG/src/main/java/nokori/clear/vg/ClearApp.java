package nokori.clear.vg;

import nokori.clear.vg.transition.TransitionManager;
import nokori.clear.vg.widget.assembly.RootWidgetAssembly;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetSynch;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;
import nokori.clear.windows.callback.*;
import nokori.clear.windows.event.*;
import nokori.clear.windows.util.WindowedApplication;
import org.joml.Vector4f;

import static org.lwjgl.nanovg.NanoVG.*;

/**
 * An application wrapper that allows users to quickly create ClearVG applications. 
 * 
 * This wrapper will still work whether you're starting the program or if you need to make a second window during runtime. It's extremely flexible.
 */
public abstract class ClearApp extends WindowedApplication {

	private Vector4f bgClearColor = new Vector4f(1f, 1f, 1f, 1f);
	
	private NanoVGContext context;

	private WidgetAssembly rootWidgetAssembly;
	
	private boolean paused = false;
	
	private ClearApp queueLaunch = null;
	private int queueLaunchFrameDelay = 0;
	
	/**
	 * Initializes the ClearApplication with a RootWidgetAssembly
	 */
	public ClearApp() {
		this(new RootWidgetAssembly());
	}
	
	public ClearApp(WidgetAssembly rootWidgetAssembly) {
		this.rootWidgetAssembly = rootWidgetAssembly;
	}
	
	public ClearApp(WindowManager windowManager, WidgetAssembly rootWidgetAssembly) {
		super(windowManager);
		this.rootWidgetAssembly = rootWidgetAssembly;
	}
	
	public void init() {
		init(windowManager, window, null);
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, String[] args) {
		ClearStaticResources.loadAllCursors();
		
		addInputCallbacks(window, rootWidgetAssembly);
		context = new NanoVGContext().init();
		
		//Calls synch() on the roots WidgetSynch that way it's up to date for the init() function
		for (int i = 0; i < rootWidgetAssembly.getNumChildren(); i++) {
			Widget w = rootWidgetAssembly.getChild(i);
			
			if (w instanceof WidgetSynch) {
				((WidgetSynch) w).synch(context);
			}
		}
		
		init(windowManager, window, context, rootWidgetAssembly, args);
	}
	
	public abstract void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args);

	@Override
	public void run() {
		/*
		 * Ticking
		 */
		
		if (!paused) {
			TransitionManager.tick();
			rootWidgetAssembly.tick(context, rootWidgetAssembly);
			rootWidgetAssembly.tickChildren(context, rootWidgetAssembly);
		}
		
		/*
		 * Rendering
		 */
		
		NanoVGContext.glClearFrame(window.getFramebufferWidth(), window.getFramebufferHeight(), bgClearColor.x(), bgClearColor.y(), bgClearColor.z(), bgClearColor.w());
        context.beginFrame(window.getWidth(), window.getHeight(), window.getFramebufferWidth(), window.getFramebufferHeight());
		
        rootWidgetAssembly.render(context, rootWidgetAssembly);
        rootWidgetAssembly.renderChildren(context, rootWidgetAssembly);

        if (paused) {
        	long vg = context.get();
        	
			ClearColor.LIGHT_GRAY.alpha(0.75f).tallocNVG(fill -> {
				nvgBeginPath(vg);
				nvgRect(vg, 0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
				nvgFillColor(vg, fill);
				nvgFill(vg);
				nvgClosePath(vg);
			});
        }

        context.endFrame();
        
        if (queueLaunch != null) {
        	if (queueLaunchFrameDelay > 0) {
        		queueLaunchFrameDelay--;
        	} else {
        		launch(queueLaunch, null, false);
        		queueLaunch = null;
        	}
        }
	}

	@Override
	protected void endOfApplicationCallback() {
		endOfNanoVGApplicationCallback();
		rootWidgetAssembly.dispose();
		
		if (exitProgramOnEndOfApplication()) {
			ClearStaticResources.destroyAllCursors();
			context.dispose();
		}
	}

	/**
	 * Called at the end of the program right before the rootWidgetAssembly and NanoVGContext are disposed.
	 */
	protected abstract void endOfNanoVGApplicationCallback();

	public WidgetAssembly getRootWidgetAssembly() {
		return rootWidgetAssembly;
	}
	
	private void addInputCallbacks(Window window, WidgetAssembly rootWidgetAssembly) {
		//Char events
		window.addInputCallback(new CharCallback() {
			
			@Override
			public void charEvent(Window window, long timestamp, int codepoint, String c, int mods) {
				if (!paused) {
					CharEvent event = CharEvent.fire(window, timestamp, codepoint, c, mods);
					rootWidgetAssembly.charEvent(window, event);
					rootWidgetAssembly.childrenCharEvent(window, event);
				}
			}
			
		});
		
		//Key events
		window.addInputCallback(new KeyCallback() {
			
			@Override
			public void keyEvent(Window window, long timestamp, int key, int scanCode, boolean pressed, boolean repeat, int mods) {
				if (!paused) {
					KeyEvent event = KeyEvent.fire(window, timestamp, key, scanCode, pressed, repeat, mods);
					rootWidgetAssembly.keyEvent(window, event);
					rootWidgetAssembly.childrenKeyEvent(window, event);
				}
			}
			
		});
		
		//Mouse button events
		window.addInputCallback(new MouseButtonCallback() {

			@Override
			public void mouseButtonEvent(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods) {
				if (!paused) {
					MouseButtonEvent event = MouseButtonEvent.fire(window, timestamp, mouseX, mouseY, button, pressed, mods);
					rootWidgetAssembly.mouseButtonEvent(window, event);
					rootWidgetAssembly.childrenMouseButtonEvent(window, event);
				}
			}
			
		});
		
		//Mouse motion events
		window.addInputCallback(new MouseMotionCallback() {

			@Override
			public void mouseMotionEvent(Window window, long timestamp, double mouseX, double mouseY, double dx, double dy) {
				if (!paused) {
					MouseMotionEvent event = MouseMotionEvent.fire(window, timestamp, mouseX, mouseY, dx, dy);
					rootWidgetAssembly.mouseMotionEvent(window, event);
					rootWidgetAssembly.childrenMouseMotionEvent(window, event);
				}
			}
			
		});
		
		//Mouse scroll events
		window.addInputCallback(new MouseScrollCallback() {

			@Override
			public void scrollEvent(Window window, long timestamp, double mouseX, double mouseY, double xoffset, double yoffset) {
				if (!paused) {
					MouseScrollEvent event = MouseScrollEvent.fire(window, timestamp, mouseX, mouseY, xoffset, yoffset);
					rootWidgetAssembly.mouseScrollEvent(window, event);
					rootWidgetAssembly.childrenMouseScrollEvent(window, event);
				}
			}
			
		});
	}

	public NanoVGContext getContext() {
		return context;
	}

	/**
	 * Whether or not this ClearApp is paused (ticking and input is disabled, but rendering will continue)
	 */
	public boolean isPaused() {
		return paused;
	}
	
	/**
	 * Toggles whether or not this ClearApp is paused. If true, ticking and input is disabled, but rendering will continue.
	 * 
	 * @param context
	 * @param paused
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
		window.setClosingEnabled(!paused);
	}

	/**
	 * Queues a ClearApp for launch after a one frame delay. This value is set to null after launch. It's recommended that you pause the ClearApp when you queue up a launch.
	 * 
	 * @param queueLaunch
	 */
	public void queueLaunch(ClearApp queueLaunch) {
		this.queueLaunch = queueLaunch;
		queueLaunchFrameDelay = 1;
	}
}
