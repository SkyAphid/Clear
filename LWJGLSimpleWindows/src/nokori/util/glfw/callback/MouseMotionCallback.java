package nokori.util.glfw.callback;

import nokori.util.glfw.Window;

public interface MouseMotionCallback extends InputCallback {
	public void mouseMotionEvent(Window window, long timestamp, double mouseX, double mouseY, double dx, double dy);
}
