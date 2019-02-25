package nokori.util.glfw;

import static org.lwjgl.glfw.GLFW.*;


import java.io.File;
import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;

public class Cursor {
	
	private long handle;
	
	public Cursor(File file, int hotX, int hotY) {
		int[] w = new int[1];
		int[] h = new int[1];
		int[] c = new int[1];
		
		ByteBuffer buffer = STBImage.stbi_load(file.getAbsolutePath(), w, h, c, 4);
		
		GLFWImage glfwImg = GLFWImage.malloc();
		glfwImg.set(w[0], h[0], buffer);
		
		handle = glfwCreateCursor(glfwImg, hotX, hotY);
		
		glfwImg.free();
		STBImage.stbi_image_free(buffer);
	}
	
	Cursor(long handle){
		this.handle = handle;
	}
	
	public long getHandle() {
		return handle;
	}
}