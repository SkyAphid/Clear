package nokori.clear.windows.util;

import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;


public abstract class WindowedApplication {

    protected WindowManager windowManager;
    protected Window window;

    public WindowedApplication(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public WindowedApplication() {
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
     * @param args    - the args passed through the main method
     */
    public static void launch(WindowedApplication program, String[] args) {
        launch(program, args, true);
    }

    /**
     * Starts a Simple Window-based program.
     *
     * @param program                 - a class that extends this one, meant to be the root of the program
     * @param args                    - the args passed through the main method
     * @param restartJVMOnFirstThread - if true, the JVM will be restarted on the first thread if not already. This ensures LWJGL3 compatibility on Mac.
     */
    public static void launch(WindowedApplication program, String[] args, boolean restartJVMOnFirstThread) {
        //Restarts the JVM if necessary on the first thread to ensure Mac compatibility
        if (restartJVMOnFirstThread && JVMUtil.restartJVMOnFirstThread(true, args)) {
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

    protected void loop() {
        //Software loop
        while (!window.isCloseRequested()) {
            run();
            windowManager.update(true);
        }

        endOfApplicationCallback();

        if (exitProgramOnEndOfApplication()) {
            windowManager.dispose();
            System.exit(0);
        } else {
            window.dispose();
        }
    }

    /**
     * Called after the basic GLFW initialization is completed and before the program loop is started.
     *
     * @param args   - the args passed through the main method
     * @param window - the newly created GLFW window
     */
    public abstract void init(WindowManager windowManager, Window window, String[] args);

    /**
     * Called from the program loop.
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
     * This overridable function allows you to change the end-of-life behavior for this WindowedApplication.
     *
     * @return true if this WindowApplication should shutdown the JVM and dispose the WindowManager at the end of its life.
     */
    protected boolean exitProgramOnEndOfApplication() {
        return true;
    }

    /**
     * This is called at the end of this program's life, right before WindowManager is disposed.
     */
    protected abstract void endOfApplicationCallback();
}
