package nokori.simple_windows.callback;

import nokori.simple_windows.Window;

public interface ScrollCallback extends InputCallback {
	public void scrollEvent(Window window, long timestamp, double mouseX, double mouseY, double xoffset, double yoffset);
}
