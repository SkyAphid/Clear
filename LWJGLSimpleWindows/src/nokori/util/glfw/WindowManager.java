package nokori.util.glfw;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWJoystickCallback;
import org.lwjgl.system.MemoryUtil;

import nokori.util.glfw.callback.JoystickStateCallback;

public class WindowManager {

	private GLFWErrorCallback errorCallback; //Must be kept referenced.

	private Monitor[] monitors;
	
	private Cursor standardCursor;

	private long updates;
	private long updateMeasureTime;
	private long currentTimeout;

	private ArrayList<Window> windows;
	private int swapInterval;
	
	private Joystick[] joysticks;
	private GLFWJoystickCallback glfwJoystickCallback;
	private JoystickStateCallback joystickStateCallback;
	
	private volatile boolean running;
	private CountDownLatch latch;

	public WindowManager() throws GLFWException{
		windows = new ArrayList<>(); //Needs to happen before we start the thread for fullscreen focus handling.
		initGLFW();
	}
	
	private void initGLFW() throws GLFWException{
		glfwSetErrorCallback(errorCallback = new GLFWErrorCallback(){
			@Override
			public void invoke(int error, long description) {
				
				System.err.println("GLFW error occured");
				System.err.println("\t" + MemoryUtil.memUTF8(description));
				System.err.println("Stack trace:");
				
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				
				for(int i = 4; i < stack.length; i++){
					System.err.println(stack[i]);
				}
				
				System.err.println();
				
			}
		});
		
		//System.out.println("WindowManager: Initializing GLFW.");
		
		if(!glfwInit()){
			throw new GLFWException("Failed to initialize GLFW.");
		}
		
		//System.out.println("WindowManager: Getting monitor list.");
		PointerBuffer pointers = glfwGetMonitors();
		int count;
		
		if(pointers != null){
			count = pointers.limit();
			monitors = new Monitor[count];
			for(int i = 0; i < count; i++){
				monitors[i] = new Monitor(pointers.get(), i == 0);
			}
		}else{
			System.err.println("WARNING: glfwGetMonitors() returned null!");
			count = 0;
		}

		//System.out.println("WindowManager: Creating standard cursor.");
		standardCursor = new Cursor(glfwCreateStandardCursor(GLFW_ARROW_CURSOR));
		
		//System.out.println("WindowManager: Detecting joysticks.");
		joysticks = new Joystick[GLFW_JOYSTICK_LAST + 1];
		redetectJoysticks();
		
		glfwJoystickCallback = new GLFWJoystickCallback() {
			
			@Override
			public void invoke(int joystick, int event) {
				
				if(event == GLFW_CONNECTED){
					joysticks[joystick] = new Joystick(joystick);
				}
				
				if(joystickStateCallback != null){
					joystickStateCallback.joystickStateChanged(joysticks[joystick], System.nanoTime(), event == GLFW_CONNECTED);
				}
				
				if(event == GLFW_DISCONNECTED){
					joysticks[joystick] = null;
				}
			}
		};
		glfwSetJoystickCallback(glfwJoystickCallback);

		swapInterval = Window.SWAP_INTERVAL_UNDEFINED;
	}
	
	/**
	 * Calls swapBuffers() on all of the added windows and calls glfwPollEvents()
	 * 
	 * @param pollEvents - whether or not to call glfwPollEvents(). Set to false in cases where you want to call it manually.
	 */
	public void update(boolean pollEvents) {
		if (pollEvents) {
			glfwPollEvents();
		}
		
		for (int i = 0; i < windows.size(); i++) {
			windows.get(i).swapBuffers();
		}
	}

	private void redetectJoysticks(){
		for(int i = 0; i <= GLFW_JOYSTICK_LAST; i++){
			if(joysticks[i] != null && joystickStateCallback != null){
				joystickStateCallback.joystickStateChanged(joysticks[i], System.nanoTime(), false);
			}
			if(glfwJoystickPresent(i)){
				Joystick j = new Joystick(i);
				joysticks[i] = j;
				if(joystickStateCallback != null){
					joystickStateCallback.joystickStateChanged(j, System.nanoTime(), true);
				}
			}else{
				joysticks[i] = null;
			}
		}
	}
	
	public void setJoystickStateCallback(JoystickStateCallback joystickStateCallback) {
		WindowManager.this.joystickStateCallback = joystickStateCallback;

		for (int i = 0; i <= GLFW_JOYSTICK_LAST; i++) {
			Joystick j = joysticks[i];
			if (j != null) {
				joystickStateCallback.joystickStateChanged(j, System.nanoTime(), true);
			}
		}
	}

	public Monitor[] getMonitors() {
		return monitors;
	}
	
	public Monitor getPrimaryMonitor(){
		return monitors[0];
	}
	
	public Cursor getStandardCursor(){
		return standardCursor;
	}
	
	public Window createWindow(int x, int y, int width, int height, boolean resizable, boolean decorated)  throws GLFWException {
		return createWindow(x, y, width, height, resizable, decorated, false);
	}
	
	public Window createWindow(int x, int y, int width, int height, boolean resizable, boolean decorated, boolean debugContext)  throws GLFWException {
		PixelFormat pixelFormat = new PixelFormat(8, 8, 8);
		ContextParams contextParams = new ContextParams(3, 3, GLFW.GLFW_OPENGL_CORE_PROFILE, true, debugContext);
		return createWindow(x, y, width, height, pixelFormat, contextParams, resizable, decorated);
	}
	
	public Window createWindow(int x, int y, int width, int height, PixelFormat pixelFormat, ContextParams contextParams, boolean resizable, boolean decorated) throws GLFWException {
		glfwDefaultWindowHints();

		glfwWindowHint(GLFW_RED_BITS, pixelFormat.getRedBits());
		glfwWindowHint(GLFW_GREEN_BITS, pixelFormat.getGreenBits());
		glfwWindowHint(GLFW_BLUE_BITS, pixelFormat.getBlueBits());

		glfwWindowHint(GLFW_ALPHA_BITS, pixelFormat.getAlphaBits());

		glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_TRUE);

		glfwWindowHint(GLFW_DEPTH_BITS, pixelFormat.getDepthBits());
		glfwWindowHint(GLFW_STENCIL_BITS, pixelFormat.getStencilBits());
		glfwWindowHint(GLFW_SAMPLES, pixelFormat.getMsaaSamples());

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, contextParams.getVersionMajor());
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, contextParams.getVersionMinor());

		glfwWindowHint(GLFW_OPENGL_PROFILE, contextParams.getProfile());
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, contextParams.isForwardCompatible() ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, contextParams.isDebugContext() ? GLFW_TRUE : GLFW_FALSE);

		glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE);

		glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE);

		long pointer = glfwCreateWindow(width, height, "GLFW window", NULL, NULL);

		glfwSetWindowPos(pointer, x, y);

		if (pointer == NULL) {
			throw new GLFWException("failed to create window");
		}

		Window w = new Window(WindowManager.this, pointer, contextParams, false, swapInterval);
		windows.add(w);
		return w;
	}
	
	public Window createFullscreenWindow(Monitor monitor, VideoMode videoMode, PixelFormat pixelFormat, ContextParams contextParams) throws GLFWException {
		glfwDefaultWindowHints();

		glfwWindowHint(GLFW_RED_BITS, videoMode.getRedBits());
		glfwWindowHint(GLFW_GREEN_BITS, videoMode.getGreenBits());
		glfwWindowHint(GLFW_BLUE_BITS, videoMode.getBlueBits());
		glfwWindowHint(GLFW_REFRESH_RATE, videoMode.getRefreshRate());

		glfwWindowHint(GLFW_ALPHA_BITS, pixelFormat.getAlphaBits());

		glfwWindowHint(GLFW_DEPTH_BITS, pixelFormat.getDepthBits());
		glfwWindowHint(GLFW_STENCIL_BITS, pixelFormat.getStencilBits());
		glfwWindowHint(GLFW_SAMPLES, pixelFormat.getMsaaSamples());

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, contextParams.getVersionMajor());
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, contextParams.getVersionMinor());
		glfwWindowHint(GLFW_OPENGL_PROFILE, contextParams.getProfile());
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, contextParams.isDebugContext() ? GLFW_TRUE : GLFW_FALSE);

		glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE);

		long pointer = glfwCreateWindow(videoMode.getWidth(), videoMode.getHeight(), "GLFW window", monitor.getPointer(), NULL);
		
		if (pointer == NULL) {
			throw new GLFWException("failed to create window");
		}

		Window w = new Window(WindowManager.this, pointer, contextParams, false, swapInterval);
		windows.add(w);

		return w;
	}
	
	void removeWindow(Window window) {
		windows.remove(window);
	}
	
	public void setVSyncEnabled(boolean vsync){
		setSwapInterval(vsync ? 1 : 0);
	}
	
	public void setSwapInterval(int swapInterval){
		this.swapInterval = swapInterval;
		for(int i = 0; i < windows.size(); i++){
			windows.get(i).setSwapInterval(swapInterval);
		}
	}
	
	public Joystick getJoystick(int index){
		return joysticks[index];
	}
	
	public void startEventLoop(){
		
		running = true;
		latch = new CountDownLatch(1);
		
		updates = 0;
		updateMeasureTime = System.nanoTime() + 100_000_000;
		currentTimeout = 1_000_000;
		
		while(running){
			handleEvents();
			
			updates++;
			
			if(System.nanoTime() > updateMeasureTime){
				
				if(updates < 95){
					currentTimeout = currentTimeout * 98 / 100;
					//System.out.println(updates + " updates, timeout adjusted: " + currentTimeout);
				}
				if(updates > 105){
					currentTimeout = currentTimeout * 100 / 98;
					//System.out.println(updates + " updates, timeout adjusted: " + currentTimeout);
				}
				
				if(currentTimeout < 10_000){
					currentTimeout = 10_000;
				}
				
				updateMeasureTime += 100_000_000;
				updates = 0;
			}
		}
		
		latch.countDown();
	}
	
	public void stopEventLoop(){
		System.out.println("WindowManager: Stopping GLFW event thread...");
		running = false;
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("WindowManager: GLFW event thread stopped.");
	}
	
	
	private void handleEvents() {
		glfwPollEvents();
		
		pollJoysticks();
		handleFullscreenFocus();
	}
	
	
	private void pollJoysticks() {
		long timestamp = System.nanoTime();
		for(int i = 0; i <= GLFW_JOYSTICK_LAST; i++){
			Joystick j = joysticks[i];
			if(j != null){
				j.poll(timestamp);
			}
		}
	}


	private void handleFullscreenFocus() {

		Window window = null;
		for(int i = 0; i < windows.size(); i++){
			Window w = windows.get(i);
			if(w.isFullscreen() && w.getWindowAttributeInternal(GLFW_FOCUSED)){
				window = w;
				break;
			}
		}
		
		boolean iconify;
		if(window == null){
			iconify = true;
		}else{
			iconify = false;
		}

		for(int i = 0; i < windows.size(); i++){
			Window w = windows.get(i);
			if(w.isFullscreen()){
				w.setIconified(iconify);
			}
		}
	}
	
	public void dispose(){
		for(int i = 0; i < windows.size(); i++){
			Window window = windows.get(i);
			window.dispose();
			glfwDestroyWindow(window.getHandle());
		}
		
		glfwTerminate();
		errorCallback.free();
	}
}
