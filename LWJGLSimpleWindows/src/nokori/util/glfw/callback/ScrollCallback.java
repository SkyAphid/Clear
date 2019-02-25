package nokori.util.glfw.callback;

import nokori.util.glfw.Window;

public interface ScrollCallback extends InputCallback {
	public void scrollEvent(Window window, long timestamp, double mouseX, double mouseY, double xoffset, double yoffset);
}
