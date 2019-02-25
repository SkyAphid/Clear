package nokori.util.glfw;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;

import nokori.util.glfw.callback.*;

public class Window {
	
	public static final int SWAP_INTERVAL_UNDEFINED = Integer.MIN_VALUE; //Symbolic value meaning driver defined default

	public static final int KEY_COUNT = 8192; //"Enough", no big deal memory-wise.
	
	
	private WindowManager parent;
	

	private long handle;
	private ContextParams contextParams;
	
	private boolean initialized;
	private boolean advancedSwapSupported;
	
	private int swapInterval;
	private boolean swapIntervalChanged;

	private int x, y;
	private int previousX, previousY;
	private AtomicLong changedPosition;
	
	private int framebufferWidth, framebufferHeight;
	private int previousWidth, previousHeight;
	private AtomicLong changedResolution;
	
	private int width, height;

	private double mouseX, mouseY;
	
	private boolean fullscreen;
	
	//Internal callbacks
	private GLFWKeyCallback glfwKeyCallback;
	private GLFWCharModsCallback glfwCharModsCallback;
	private GLFWCursorPosCallback glfwCursorPosCallback;
	private GLFWMouseButtonCallback glfwMouseButtonCallback;
	private GLFWScrollCallback glfwScrollCallback;
	private GLFWWindowPosCallback glfwWindowPosCallback;
	private GLFWFramebufferSizeCallback glfwFramebufferSizeCallback;
	private GLFWWindowSizeCallback glfwWindowSizeCallback;
	
	private ArrayList<KeyCallback> keyCallbacks = new ArrayList<>();
	private ArrayList<CharCallback> charCallbacks = new ArrayList<>();
	private ArrayList<MouseMotionCallback> mouseMotionCallbacks = new ArrayList<>();
	private ArrayList<MouseCallback> mouseCallbacks = new ArrayList<>();
	private ArrayList<ScrollCallback> scrollCallbacks = new ArrayList<>();

	private AtomicIntegerArray pressedKeys;
	
	Window(WindowManager parent, long handle, ContextParams contextParams, boolean fullscreen, int swapInterval) {
		
		this.parent = parent;
		this.handle = handle;
		this.contextParams = contextParams;
		
		stackPush();

		IntBuffer buf1 = stackMallocInt(1);
		IntBuffer buf2 = stackMallocInt(1);
		
		glfwGetWindowPos(handle, buf1, buf2);
		x = buf1.get(0);
		y = buf2.get(0);
		
		glfwGetFramebufferSize(handle, buf1, buf2);
		framebufferWidth = buf1.get(0);
		framebufferHeight = buf2.get(0);
		//System.out.println("Framebuffer size: " + width + ", " + height);

		
		glfwGetWindowSize(handle, buf1, buf2);
		width = buf1.get(0);
		height = buf2.get(0);
		//System.out.println("Window size: " + windowWidth + ", " + windowHeight);
		
		stackPop();
		
		//Change position
		changedPosition = new AtomicLong(0);
		
		//Resize
		changedResolution = new AtomicLong(0);
		
		pressedKeys = new AtomicIntegerArray(KEY_COUNT);
		

		glfwSetKeyCallback(handle, glfwKeyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				
				if(key >= 0 && key < KEY_COUNT){
					pressedKeys.set(key, action != GLFW_RELEASE ? 1 : 0);
				}
				
				for (KeyCallback keyCallback : keyCallbacks) {
					keyCallback.keyEvent(Window.this, System.nanoTime(), key, scancode, action != GLFW_RELEASE, action == GLFW_REPEAT, mods);
				}
			}
		});
		
		glfwSetCharModsCallback(handle, glfwCharModsCallback = new GLFWCharModsCallback() {
			@Override
			public void invoke(long window, int codepoint, int mods) {
				for (CharCallback charCallback : charCallbacks) {
					charCallback.charEvent(Window.this, System.nanoTime(), codepoint, String.valueOf(Character.toChars(codepoint)), mods);
				}
			}
		});
		
		glfwSetCursorPosCallback(handle, glfwCursorPosCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double x, double y) {
				double correctedX = x * framebufferWidth / width;
				double correctedY = y * framebufferHeight / height;

				double dx = correctedX - mouseX;
				double dy = correctedY - mouseY;
				
				for (MouseMotionCallback mouseMotionCallback : mouseMotionCallbacks) {
					mouseMotionCallback.mouseMotionEvent(Window.this, System.nanoTime(), correctedX, correctedY, dx, dy);
				}
				
				mouseX = correctedX;
				mouseY = correctedY;
			}
		});
		
		glfwSetMouseButtonCallback(handle, glfwMouseButtonCallback = new GLFWMouseButtonCallback() {
			
			@Override
			public void invoke(long window, int button, int action, int mods) {
				for (MouseCallback mouseCallback : mouseCallbacks){
					mouseCallback.mouseEvent(Window.this, System.nanoTime(), mouseX, mouseY, button, action == GLFW_PRESS, mods);
				}
			}
		});
		
		glfwSetScrollCallback(handle, glfwScrollCallback = new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				for (ScrollCallback scrollCallback : scrollCallbacks){
					scrollCallback.scrollEvent(Window.this, System.nanoTime(), mouseX, mouseY, xoffset, yoffset);
				}
			}
		});
		
		glfwSetWindowPosCallback(handle, glfwWindowPosCallback = new GLFWWindowPosCallback() {
			@Override
			public void invoke(long window, int wx, int wy) {
				x = wx;
				y = wy;
			}
		});
		
		glfwSetFramebufferSizeCallback(handle, glfwFramebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int newWidth, int newHeight) {
				
				if(newWidth == 0 || newHeight == 0){
					return;
				}
				
				long resolution = (long)newWidth << 32 | (newHeight & 0xFFFFFFFFL);
				changedResolution.getAndSet(resolution);
				
				framebufferWidth = newWidth;
				framebufferHeight = newHeight;
				
				//System.out.println("Framebuffer size: " + width + ", " + height);
			}
		});
		
		glfwSetWindowSizeCallback(handle, glfwWindowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int newWidth, int newHeight) {
				
				if(newWidth == 0 || newHeight == 0){
					return;
				}
				
				width = newWidth;
				height = newHeight;
				
				//System.out.println("Window size: " + width + ", " + height);
			}
		});
		
		initialized = false;

		this.swapInterval = SWAP_INTERVAL_UNDEFINED;
		swapIntervalChanged = false;
		setSwapInterval(swapInterval);
	}

	public void makeContextCurrent() {
		glfwMakeContextCurrent(handle);
		
		if(!initialized){
			GL.createCapabilities();
			
			advancedSwapSupported = 
					glfwExtensionSupported("WGL_EXT_swap_control_tear") ||
					glfwExtensionSupported("GLX_EXT_swap_control_tear");
			initialized = true;
		}
	}

	public void setSwapInterval(int swapInterval) {
		if(this.swapInterval != swapInterval){
			this.swapInterval = swapInterval;
			swapIntervalChanged = true;
		}
	}
	
	private void updateSwapInterval() {
		if(advancedSwapSupported){
			glfwSwapInterval(swapInterval);
		}else{
			glfwSwapInterval(Math.abs(swapInterval));
		}
		swapIntervalChanged = false;
	}
	
	public void swapBuffers(){
		if(glfwGetCurrentContext() != handle){
			throw new IllegalStateException("swapBuffers() called without the context of the window being current");
		}
		
		if (swapIntervalChanged){
			updateSwapInterval();
		}
		
		glfwSwapBuffers(handle);
	}
	
	public void setMouseGrabbed(boolean grabbed) {
		if (grabbed) {
			glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		} else {
			glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		}
	}
	
	public void setPosition(int x, int y){
		glfwSetWindowPos(handle, x, y);
	}
	
	public void setWindowed(int x, int y, VideoMode videoMode){
		setWindowed(x, y, videoMode.getWidth(), videoMode.getHeight());
	}
	
	public void setWindowed(int x, int y, int width, int height) {
		System.out.println("Setting decorated true...");
		glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_TRUE);
		System.out.println("Setting window monitor...");
		glfwSetWindowMonitor(handle, 0, x, y, width, height, 60); // Framerate ignored.
		fullscreen = false;
		System.out.println("Change to windowed complete.");
	}

	public void setBorderlessWindowed(int x, int y, VideoMode videoMode){
		setBorderlessWindowed(x, y, videoMode.getWidth(), videoMode.getHeight());
	}
	
	public void setBorderlessWindowed(int x, int y, int width, int height) {
		glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_FALSE);
		glfwSetWindowMonitor(handle, 0, x, y, width, height, 60);
		fullscreen = true;
	}
	
	public void setFullscreen(Monitor monitor, VideoMode videoMode){
		glfwSetWindowMonitor(handle, monitor.getPointer(), x, y, videoMode.getWidth(), videoMode.getHeight(), videoMode.getRefreshRate());
		fullscreen = true;
	}
	
	public void focus(){
		glfwFocusWindow(handle);
	}
	
	public void setSizeLimits(int minWidth, int minHeight){
		setSizeLimits(minWidth, minHeight, GLFW_DONT_CARE, GLFW_DONT_CARE);
	}
	
	public void setSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight){
		glfwSetWindowSizeLimits(handle, minWidth, minHeight, maxWidth, maxHeight);
	}
	
	public void setTitle(String title) {
		glfwSetWindowTitle(handle, title);
	}
	
	public void setCursor(Cursor cursor) {
		glfwSetCursor(handle, cursor.getHandle());
	}
	
	public void setMousePosition(int x, int y){
		glfwSetCursorPos(getHandle(), x, y);
	}
	
	public boolean isCloseRequested(){
		return glfwWindowShouldClose(handle);
	}
	
	//Maximize/minimize controls

	public void restore(){
		glfwRestoreWindow(handle);
	}
	
	public void maximize(){
		glfwMaximizeWindow(handle);
	}
	
	public void minimize(){
		glfwIconifyWindow(handle);
	}
	
	//window icon
	public void setIcons(GLFWImage.Buffer icons){
		glfwSetWindowIcon(handle, icons);
	}
	
	//clipboard
	public void setClipboardString(String string){
		glfwSetClipboardString(handle, string);
	}
	
	public String getClipboardString(){
		return glfwGetClipboardString(handle);
	}
	
	private boolean getWindowAttribute(int attribute){
		if (glfwGetWindowAttrib(handle, attribute) == GL_TRUE) {
			return true;
		} else {
			return false;
		}

	}
	
	public boolean hasFocus(){
		return getWindowAttribute(GLFW_FOCUSED);
	}
	
	public boolean isMinimized(){
		return getWindowAttribute(GLFW_ICONIFIED);
	}
	
	public boolean isResizable(){
		return getWindowAttribute(GLFW_RESIZABLE);
	}
	
	public boolean isVisible(){
		return getWindowAttribute(GLFW_VISIBLE);
	}
	
	public boolean isDecorated(){
		return getWindowAttribute(GLFW_DECORATED);
	}
	
	public boolean isAlwaysOnTop(){
		return getWindowAttribute(GLFW_FLOATING);
	}
	
	public boolean isMaximized(){
		return getWindowAttribute(GLFW_MAXIMIZED);
	}
	
	//Internals for multi-window fullscreen management on the GLFW thread
	boolean getWindowAttributeInternal(int attribute){
		return glfwGetWindowAttrib(handle, attribute) == GL_TRUE;
	}

	void setIconified(boolean iconified){
		if(iconified){
			glfwIconifyWindow(handle);
		}else{
			glfwRestoreWindow(handle);
		}
	}
	
	public boolean isFullscreen() {
		return fullscreen;
	}
	
	public void addInputCallback(InputCallback... callbacks) {
		for (int i = 0; i < callbacks.length; i++) {
			addInputCallback(callbacks[i]);
		}
	}

	public void addInputCallback(InputCallback callback) {
		if (callback instanceof KeyCallback) {
			keyCallbacks.add((KeyCallback) callback);
		}
		
		if (callback instanceof CharCallback) {
			charCallbacks.add((CharCallback) callback);
		}
		
		if (callback instanceof MouseMotionCallback) {
			mouseMotionCallbacks.add((MouseMotionCallback) callback);
		}
		
		if (callback instanceof MouseCallback) {
			mouseCallbacks.add((MouseCallback) callback);
		}
		
		if (callback instanceof ScrollCallback) {
			scrollCallbacks.add((ScrollCallback) callback);
		}
	}
	
	public void removeInputCallback(InputCallback... callbacks) {
		for (int i = 0; i < callbacks.length; i++) {
			removeInputCallback(callbacks[i]);
		}
	}
	
	public void removeInputCallback(InputCallback callback) {
		if (callback instanceof KeyCallback) {
			keyCallbacks.remove(callback);
		}
		
		if (callback instanceof CharCallback) {
			charCallbacks.remove(callback);
		}
		
		if (callback instanceof MouseMotionCallback) {
			mouseMotionCallbacks.remove(callback);
		}
		
		if (callback instanceof MouseCallback) {
			mouseCallbacks.remove(callback);
		}
		
		if (callback instanceof ScrollCallback) {
			scrollCallbacks.remove(callback);
		}
	}

	public ArrayList<KeyCallback> getKeyCallbacks() {
		return keyCallbacks;
	}

	public void setKeyCallbacks(ArrayList<KeyCallback> keyCallbacks) {
		this.keyCallbacks = keyCallbacks;
	}

	public ArrayList<CharCallback> getCharCallbacks() {
		return charCallbacks;
	}

	public void setCharCallbacks(ArrayList<CharCallback> charCallbacks) {
		this.charCallbacks = charCallbacks;
	}

	public ArrayList<MouseMotionCallback> getMouseMotionCallbacks() {
		return mouseMotionCallbacks;
	}

	public void setMouseMotionCallbacks(ArrayList<MouseMotionCallback> mouseMotionCallbacks) {
		this.mouseMotionCallbacks = mouseMotionCallbacks;
	}

	public ArrayList<MouseCallback> getMouseCallbacks() {
		return mouseCallbacks;
	}

	public void setMouseCallbacks(ArrayList<MouseCallback> mouseCallbacks) {
		this.mouseCallbacks = mouseCallbacks;
	}

	public ArrayList<ScrollCallback> getScrollCallbacks() {
		return scrollCallbacks;
	}

	public void setScrollCallbacks(ArrayList<ScrollCallback> scrollCallbacks) {
		this.scrollCallbacks = scrollCallbacks;
	}

	public long getHandle() {
		return handle;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getPreviousX() {
		return previousX;
	}

	public void setPreviousX(int previousX) {
		this.previousX = previousX;
	}

	public int getPreviousY() {
		return previousY;
	}

	public void setPreviousY(int previousY) {
		this.previousY = previousY;
	}

	/**
	 * Gets the width given by glfwFramebufferSize()
	 */
	public int getFramebufferWidth(){
		return framebufferWidth;
	}
	
	/**
	 * Gets the height given by glfwFramebufferSize()
	 * @return
	 */
	public int getFramebufferHeight() {
		return framebufferHeight;
	}
	
	/**
	 * Gets the width given by glfwWindowSize()
	 * @return
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Gets the height given by glfwWindowSize()
	 * @return
	 */
	public int getHeight() {
		return height;
	}
	
	public int getPreviousWidth(){
		return previousWidth;
	}
	
	public int getPreviousHeight(){
		return previousHeight;
	}
	
	public boolean wasResized(){
		if(changedResolution.get() == 0){
			return false;
		}
		
		previousWidth = framebufferWidth;
		previousHeight = framebufferHeight;
		
		long newRes;
		while((newRes = changedResolution.getAndSet(0)) != 0){
			framebufferWidth = (int)(newRes >> 32);
			framebufferHeight = (int)(newRes);
		}
		
		return true;
	}
	
	public boolean wasMoved(){
		if(changedPosition.get() == 0){
			return false;
		}
		
		previousX = x;
		previousY = y;
		
		long newPos;
		while((newPos = changedPosition.getAndSet(0)) != 0){
			x = (int)(newPos >> 32);
			y = (int)(newPos);
		}
		
		return true;
	}
	
	WindowManager getParent() {
		return parent;
	}
	
	public ContextParams getContextParams() {
		return contextParams;
	}
	
	public boolean isKeyDown(int key){
		if(key < 0 || key >= KEY_COUNT){
			return false;
		}
		
		return pressedKeys.get(key) == 1;
	}
	
	/**
	 * Adds the following files as icons of varying sizes for the window.
	 * 
	 * @param iconFiles - the array of files to check/load
	 */
	public void setIcon(File[] iconFiles) {
		setIcon(null, iconFiles);
	}
	
	/**
	 * Adds the following files as icons of varying sizes for the window.
	 * 
	 * @param filetype - only uses the Files that end with this file extension. Set to null to just use any file.
	 * @param iconFiles - the array of files to check/load
	 */
	public void setIcon(String filetype, File[] files) {
		
		/*
		 * Check the listed files for images that can be used as icons
		 */
		
		Stack<File> validFiles = new Stack<File>();
		
		for(int i = 0; i < files.length; i++) {
			File f = files[i];
			
			if (filetype != null && f.getName().endsWith(filetype)) {
				validFiles.push(f);
			}
		}
		
		/*
		 * Compile an array of valid icons
		 */
		
		File[] iconFiles = new File[validFiles.size()];
		
		for (int i = 0; i < iconFiles.length; i++) {
			iconFiles[i] = validFiles.pop();
		}
		
		/*
		 * Set the icons
		 */
		
		int numIcons = iconFiles.length;
		
		GLFWImage.Buffer icons = GLFWImage.malloc(numIcons);
		
		int[] w = new int[1];
		int[] h = new int[1];
		int[] c = new int[1];
		
		ByteBuffer[] datas = new ByteBuffer[numIcons];
		
		for(int i = 0; i < numIcons; i++) {
			ByteBuffer data = datas[i] = STBImage.stbi_load(iconFiles[i].getAbsolutePath(), w, h, c, 4);
			icons.get(i).set(w[0], h[0], data);
		}
		
		glfwSetWindowIcon(getHandle(), icons);
		
		icons.free();
		
		for(int i = 0; i < numIcons; i++) {
			STBImage.stbi_image_free(datas[i]);
		}
	}
	
	void dispose() {
		parent.removeWindow(Window.this);

		glfwDestroyWindow(handle);
		handle = 0;

		glfwKeyCallback.free();
		glfwCharModsCallback.free();
		glfwCursorPosCallback.free();
		glfwMouseButtonCallback.free();
		glfwScrollCallback.free();
		glfwWindowPosCallback.free();
		glfwFramebufferSizeCallback.free();
		glfwWindowSizeCallback.free();

	}
}