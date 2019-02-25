package nokori.util.glfw.callback;

import nokori.util.glfw.Window;

public interface CharCallback extends InputCallback {
	public void charEvent(Window window, long timestamp, int codepoint, String c, int mods);
}
