package nokori.util.glfw;

import org.lwjgl.glfw.GLFW;

public class ContextParams {
	
	private int versionMajor, versionMinor;
	private int profile;
	private boolean forwardCompatible;
	
	private boolean debugContext;
	
	public ContextParams() {
		this(1, 1);
	}
	
	public ContextParams(int versionMajor, int versionMinor) {
		this(versionMajor, versionMinor, GLFW.GLFW_OPENGL_ANY_PROFILE);
	}
	
	public ContextParams(int versionMajor, int versionMinor, int profile) {
		this(versionMajor, versionMinor, profile, false);
	}
	
	public ContextParams(int versionMajor, int versionMinor, int profile, boolean forwardCompatible) {
		this(versionMajor, versionMinor, profile, forwardCompatible, false);
	}
	
	public ContextParams(int versionMajor, int versionMinor, int profile, boolean forwardCompatible, boolean debugContext) {
		this.versionMajor = versionMajor;
		this.versionMinor = versionMinor;
		this.profile = profile;
		this.forwardCompatible = forwardCompatible;
		
		this.debugContext = debugContext;
	}
	
	public int getVersionMajor() {
		return versionMajor;
	}
	
	public int getVersionMinor() {
		return versionMinor;
	}
	
	public int getProfile() {
		return profile;
	}
	
	public boolean isForwardCompatible() {
		return forwardCompatible;
	}
	
	public boolean isDebugContext() {
		return debugContext;
	}
}