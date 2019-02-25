package nokori.util.glfw;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFWVidMode;

public class Monitor {
	
	private static final double MM_TO_INCHES = 0.0393700787;
	
	private long pointer;
	private boolean primaryMonitor;
	
	private String name;
	
	private int virtualX, virtualY;
	
	private int physicalWidth, physicalHeight;
	private double physicalDiagonalInches;

	private ArrayList<VideoMode> videoModes;
	private VideoMode desktopVideoMode;

	public Monitor(long pointer, boolean primaryMonitor) {
		
		this.pointer = pointer;
		this.primaryMonitor = primaryMonitor;
		
		name = glfwGetMonitorName(pointer);
		
		stackPush();
		IntBuffer buf1 = stackMallocInt(1);
		IntBuffer buf2 = stackMallocInt(1);
		
		glfwGetMonitorPos(pointer, buf1, buf2);
		virtualX = buf1.get(0);
		virtualY = buf2.get(0);
		
		glfwGetMonitorPhysicalSize(pointer, buf1, buf2);
		physicalWidth = buf1.get(0);
		physicalHeight = buf2.get(0);
		physicalDiagonalInches = MM_TO_INCHES * Math.sqrt(physicalWidth * physicalWidth + physicalHeight * physicalHeight);
		
		stackPop();
		
		
		
		
		GLFWVidMode.Buffer videoModePointer = glfwGetVideoModes(pointer);
		
		int count = videoModePointer.remaining();
		videoModes = new ArrayList<>(count + 1);
		for(int i = 0; i < count; i++){
			videoModes.add(new VideoMode(videoModePointer.get(i)));
		}
		
		desktopVideoMode = new VideoMode(glfwGetVideoMode(pointer));
		if(!videoModes.contains(desktopVideoMode)){
			//System.out.println("The desktop video mode " + desktopVideoMode + " of monitor '" + name + "' is not in the list of available display modes. Adding it.");
			videoModes.add(desktopVideoMode);
		}
	}
	
	public long getPointer() {
		return pointer;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isPrimaryMonitor() {
		return primaryMonitor;
	}
	
	public int getVirtualX() {
		return virtualX;
	}
	
	public int getVirtualY() {
		return virtualY;
	}
	
	public int getPhysicalWidth() {
		return physicalWidth;
	}
	
	public int getPhysicalHeight() {
		return physicalHeight;
	}
	
	public double getPhysicalDiagonalInches() {
		return physicalDiagonalInches;
	}
	
	public ArrayList<VideoMode> getVideoModes() {
		return videoModes;
	}
	
	public VideoMode getDesktopVideoMode() {
		return desktopVideoMode;
	}
	
	public VideoMode getBestVideoMode(int width, int height){
		VideoMode result = null;
		for(int i = 0; i < videoModes.size(); i++){
			VideoMode vm = videoModes.get(i);
			//System.out.println(vm);
			if(vm.getWidth() == width && vm.getHeight() == height){
				
				if(
						result == null ||
						vm.getColorBitCount() > result.getColorBitCount() ||
						(vm.getColorBitCount() == result.getColorBitCount() && vm.getRefreshRate() > result.getRefreshRate())
				){
					result = vm;
				}
			}
		}
		return result;
	}
	
	
}