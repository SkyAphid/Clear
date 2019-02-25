package nokori.simple_windows.callback;

import nokori.simple_windows.Window;

public interface MouseMotionCallback extends InputCallback {
	public void mouseMotionEvent(Window window, long timestamp, double mouseX, double mouseY, double dx, double dy);
}
