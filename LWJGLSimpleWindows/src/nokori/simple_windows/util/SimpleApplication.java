package nokori.simple_windows.util;

import nokori.simple_windows.GLFWException;
import nokori.simple_windows.Window;
import nokori.simple_windows.WindowManager;


public abstract class SimpleApplication {

	protected WindowManager windowManager;
	protected Window window;
	
	public SimpleApplication() {
		try {
			windowManager = new WindowManager();
		} catch (GLFWException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts a Simple Window-based program.
	 * 
	 * @param program - a class that extends this one, meant to be the root of the program
	 * @param args - the args passed through the main method
	 */
	public static void launch(SimpleApplication program, String[] args) {
		//Restarts the JVM if necessary on the first thread to ensure Mac compatibility
		if (JVMUtil.restartJVMOnFirstThread(true, args)) {
			return;
		}

		//Create Window and NuklearContext
		try {
			Window window = program.createWindow(program.windowManager);
			window.makeContextCurrent();
			
			program.window = window;

			//Initialize the program
			program.init(program.windowManager, window, args);
			
			//Run the program
			program.loop();
					
		} catch (GLFWException e) {
			e.printStackTrace();
			return;
		}
	}
	
	
	private void loop() {
		//Software loop
		while (!window.isCloseRequested()) {
			
			windowManager.update(true);
		}
		
		dispose();
		windowManager.dispose();
	}
	
	/**
	 * Called after the basic GLFW initialization is completed and before the program loop is started.
	 * 
	 * @param args - the args passed through the main method
	 * @param window - the newly created GLFW window
	 */
	public abstract void init(WindowManager windowManager, Window window, String[] args);

	/**
	 * Called from the program loop before rendering.
	 */
	public abstract void run();
	
	
	/**
	 * Creates the GLFW Window. Allows the user to customize it to their specific use-case.
	 * 
	 * @param windowManager
	 * @return
	 */
	public abstract Window createWindow(WindowManager windowManager) throws GLFWException;

	public WindowManager getWindowManager() {
		return windowManager;
	}
	
	public Window getWindow() {
		return window;
	}
	
	/**
	 * This is called at the end of this program's life, right before WindowManager and NuklearContext are disposed.
	 */
	protected abstract void dispose();
}
