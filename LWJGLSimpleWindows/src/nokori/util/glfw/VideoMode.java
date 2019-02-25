package nokori.util.glfw;

import org.lwjgl.glfw.GLFWVidMode;

public class VideoMode {
	
	private int width;
	private int height;

	private int refreshRate;
	
	private int redBits;
	private int greenBits;
	private int blueBits;
	
	private boolean fullscreenAllowed;
	
	public VideoMode(int width, int height) {
		this(width, height, 0, 0, 0);
	}
	
	public VideoMode(int width, int height, int redBits, int greenBits, int blueBits) {
		
		this.width = width;
		this.height = height;
		
		this.redBits = redBits;
		this.greenBits = greenBits;
		this.blueBits = blueBits;
		
		fullscreenAllowed = false;
	}
	
	public VideoMode(GLFWVidMode vidMode) {
		
		width = vidMode.width();
		height = vidMode.height();

		refreshRate = vidMode.refreshRate();
		
		redBits = vidMode.redBits();
		greenBits = vidMode.greenBits();
		blueBits = vidMode.blueBits();

		
		fullscreenAllowed = true;
	}

	@Override
	public boolean equals(Object obj) {
		VideoMode vm = (VideoMode) obj;
		return 
				width == vm.width &&
				height == vm.height &&
				refreshRate == vm.refreshRate &&
				redBits == vm.redBits &&
				greenBits == vm.greenBits &&
				blueBits == vm.blueBits;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getRefreshRate() {
		return refreshRate;
	}
	
	public int getRedBits() {
		return redBits;
	}
	
	public int getGreenBits() {
		return greenBits;
	}
	
	public int getBlueBits() {
		return blueBits;
	}
	
	public int getColorBitCount(){
		return redBits + greenBits + blueBits;
	}
	
	public boolean fullscreenAllowed() {
		return fullscreenAllowed;
	}
	
	@Override
	public String toString() {
		return "VideoMode{" + width + "x" + height + "x" + getColorBitCount() + " (" + redBits + "/" + greenBits + "/" + blueBits + ")" + "@" + refreshRate + "}";
	}
}
