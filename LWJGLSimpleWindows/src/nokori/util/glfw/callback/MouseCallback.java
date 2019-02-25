package nokori.util.glfw.callback;

import nokori.util.glfw.Window;

public interface MouseCallback extends InputCallback {
	public void mouseEvent(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods);
}
